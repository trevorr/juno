/*
 * Juno - OpenVera (TM) to Jove Translator
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * VERA and OpenVera are trademarks or registered trademarks of Synopsys, Inc.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.schemabuilder.juno;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.newisys.dv.ifgen.IfgenJavaTranslator;
import com.newisys.dv.ifgen.IfgenTranslatorException;
import com.newisys.dv.ifgen.schema.*;
import com.newisys.langschema.CompilationUnit;
import com.newisys.langschema.Container;
import com.newisys.langschema.Name;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.StructuredType;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.Visibility;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.langschema.jove.JoveFixedArrayType;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.langschema.vera.*;
import com.newisys.schemaanalyzer.juno.BlockAnalysis;
import com.newisys.schemaanalyzer.juno.ClassAnalysis;
import com.newisys.schemaanalyzer.juno.FunctionAnalysis;
import com.newisys.schemaanalyzer.juno.VariableAnalysis;
import com.newisys.schemaanalyzer.juno.VeraSchemaAnalyzer;
import com.newisys.util.MathUtils;
import com.newisys.util.logging.IndentLogger;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.Bit;

/**
 * Base class for the various modules of the translator. Used to share common
 * methods and references to objects providing common services.
 * 
 * @author Trevor Robinson
 */
class TranslatorModule
{
    protected final JoveSchema schema;
    protected final IfgenSchema ifSchema;
    protected final VeraSchemaAnalyzer analyzer;
    protected final PackageNamer packageNamer;
    protected final IndentLogger log;
    protected final ManualTranslationMap manXlatMap;
    protected final FactoryTranslationMap factoryXlatMap;
    protected final SchemaTypes types;
    protected final ExpressionConverter exprConv;
    protected final BuiltinFunctionTranslatorMap builtinFuncMap;
    protected final TranslatedObjectMap xlatObjMap;
    protected final IfgenJavaTranslator ifgenXlat;
    protected final VerilogImporter vlogImporter;

    private final Map<Container, JavaRawClass> globalClasses;

    public TranslatorModule(
        JoveSchema schema,
        IfgenSchema ifSchema,
        VeraSchemaAnalyzer analyzer,
        PackageNamer packageNamer,
        IndentLogger log,
        SchemaTypes types,
        ExpressionConverter exprConv,
        ManualTranslationMap manXlatMap,
        FactoryTranslationMap factoryXlatMap)
    {
        this.schema = schema;
        this.ifSchema = ifSchema;
        this.analyzer = analyzer;
        this.packageNamer = packageNamer;
        this.log = log;
        this.manXlatMap = manXlatMap;
        this.factoryXlatMap = factoryXlatMap;
        this.types = types;
        this.exprConv = exprConv;
        this.builtinFuncMap = new BuiltinFunctionTranslatorMap(types);
        this.xlatObjMap = new TranslatedObjectMap();
        this.ifgenXlat = new IfgenJavaTranslator(schema);
        this.vlogImporter = new VerilogImporter(schema, packageNamer, types);

        this.globalClasses = new HashMap<Container, JavaRawClass>();
    }

    public TranslatorModule(TranslatorModule other)
    {
        this.schema = other.schema;
        this.ifSchema = other.ifSchema;
        this.analyzer = other.analyzer;
        this.packageNamer = other.packageNamer;
        this.log = other.log;
        this.manXlatMap = other.manXlatMap;
        this.factoryXlatMap = other.factoryXlatMap;
        this.types = other.types;
        this.exprConv = other.exprConv;
        this.builtinFuncMap = other.builtinFuncMap;
        this.xlatObjMap = other.xlatObjMap;
        this.ifgenXlat = other.ifgenXlat;
        this.vlogImporter = other.vlogImporter;

        this.globalClasses = other.globalClasses;
    }

    protected void logEnter(String msg)
    {
        log.println(msg);
        log.incIndent();
    }

    protected void log(String msg)
    {
        log.println(msg);
    }

    protected void logExit()
    {
        log.decIndent();
    }

    protected static String fixID(String id)
    {
        return JavaKeywords.isKeyword(id) ? "_" + id : id;
    }

    private JavaPackage getPackage(CompilationUnit compUnit)
    {
        final String path = compUnit.getSourcePath();
        final String pkgName = packageNamer.getPackageName(path);
        final JavaPackage pkg = schema.getPackage(pkgName, true);
        return pkg;
    }

    protected JavaPackage translateCompUnit(VeraCompilationUnit obj)
    {
        // check whether compilation unit has already been translated
        JavaPackage pkg = (JavaPackage) xlatObjMap.getJavaObject(obj);
        if (pkg != null) return pkg;

        final String path = obj.getSourcePath();
        logEnter("Translating compilation unit: " + path);

        // get the containing package
        pkg = getPackage(obj);
        xlatObjMap.addJavaObject(obj, pkg);

        // translate the compilation unit members
        final CompUnitMemberTranslator xlat = new CompUnitMemberTranslator(
            this, pkg);
        final Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraCompilationUnitMember member = (VeraCompilationUnitMember) iter
                .next();
            member.accept(xlat);
        }

        logExit();
        return pkg;
    }

    protected JavaPackage translateCompUnitOf(VeraCompilationUnitMember obj)
    {
        return translateCompUnit(obj.getCompilationUnit());
    }

    protected JavaRawClass translateClass(
        VeraUserClass obj,
        JavaPackage pkg,
        boolean translateMembers)
    {
        // check whether class has already been translated
        JavaRawClass lookupCls = (JavaRawClass) xlatObjMap.getJavaObject(obj);
        if (lookupCls != null)
        {
            if (translateMembers && lookupCls instanceof TranslatedClass)
            {
                translateClassMembers(obj, pkg, (TranslatedClass) lookupCls);
            }
            return lookupCls;
        }

        final String id = obj.getName().getIdentifier();
        logEnter("Translating class: " + id);

        try
        {
            // check whether class has been manually translated
            lookupCls = (JavaRawClass) manXlatMap.getClass(id);
            if (lookupCls != null)
            {
                log("Using manually translated class: " + lookupCls.getName());
                xlatObjMap.addJavaObject(obj, lookupCls);
                return lookupCls;
            }

            // make sure class is actually defined
            if (obj.isTypedefOnly())
            {
                throw new RuntimeException(
                    "Cannot translate typedef-only class: " + id);
            }
            if (!obj.isDefined())
            {
                if (id.equals("_Synopsys_Vera_List_"))
                {
                    // _Synopsys_Vera_List_ is magically defined
                    obj.setDefined(true);
                }
                else if (id.equals("OVAAssert") || id.equals("OVAEngine")
                    || id.equals("OVAEvent"))
                {
                    logExit();
                    return (JavaRawClass) schema
                        .getTypeForSystemClass("com.newisys.juno.runtime." + id);
                }
                else
                {
                    log("Warning: Converting extern-only class: " + id);
                }
            }

            // create the Java class
            TranslatedClass cls = new TranslatedClass(schema, fixID(id), pkg);
            cls.addAnnotations(obj.getAnnotations());
            pkg.addMember(cls);
            xlatObjMap.addJavaObject(obj, cls);

            // set the visibility (local classes remain package-protected)
            if (obj.getVisibility() != VeraVisibility.LOCAL)
            {
                cls.setVisibility(JavaVisibility.PUBLIC);
            }

            // mark virtual classes as abstract
            if (obj.isVirtual())
            {
                cls.addModifier(JavaTypeModifier.ABSTRACT);
            }

            // set the base class
            final VeraClass veraBase = obj.getBaseClass();
            final JavaRawClass baseClass;
            if (veraBase instanceof VeraUserClass)
            {
                baseClass = translateClass((VeraUserClass) veraBase,
                    translateMembers);
                assert (baseClass != null);
            }
            else
            {
                assert (veraBase == obj.getVeraSchema().rootClass);
                baseClass = types.junoObjectType;
            }
            cls.setBaseClass(baseClass);

            if (translateMembers)
            {
                translateClassMembers(obj, pkg, cls);
            }

            return cls;
        }
        finally
        {
            logExit();
        }
    }

    private void translateClassMembers(
        VeraUserClass obj,
        JavaPackage pkg,
        TranslatedClass cls)
    {
        // avoid multiple member translations
        if (cls.membersTranslated) return;
        cls.membersTranslated = true;

        // translate the class members
        final ClassMemberTranslator xlat = new ClassMemberTranslator(this, pkg,
            cls);
        final Iterator memberIter = obj.getMembers().iterator();
        while (memberIter.hasNext())
        {
            VeraClassMember member = (VeraClassMember) memberIter.next();
            member.accept(xlat);
        }

        // Java non-abstract classes must implement all inherited abstract
        // methods; older versions of Vera did not require this
        if (!cls.getModifiers().contains(JavaTypeModifier.ABSTRACT))
        {
            implementAbstractMethods(cls);
        }
    }

    private void implementAbstractMethods(JavaRawAbstractClass cls)
    {
        final Collection methods = cls.getNonOverriddenMethods();
        final Iterator methodIter = methods.iterator();
        while (methodIter.hasNext())
        {
            final JavaFunction method = (JavaFunction) methodIter.next();
            if (method.getModifiers().contains(JavaFunctionModifier.ABSTRACT))
            {
                String id = method.getName().getIdentifier();
                JavaFunctionType origType = method.getType();

                log("Warning: Class '" + cls.toReferenceString()
                    + "' must implement method '" + id + "'; generating stub");

                JavaFunctionType funcType = new JavaFunctionType(origType);
                JavaFunction func = new JavaFunction(id, funcType);
                func.setVisibility(method.getVisibility());

                JavaBlock body = new JavaBlock(schema);
                body.addMember(new JavaThrowStatement(ExpressionBuilder
                    .newInstance(types.absCallErrorType)));
                func.setBody(body);

                cls.addMember(func);
            }
        }
    }

    protected JavaRawClass translateClass(
        VeraUserClass obj,
        boolean translateMembers)
    {
        return translateClass(obj, translateCompUnitOf(obj), translateMembers);
    }

    protected JavaRawClass translateClassOf(VeraClassMember obj)
    {
        VeraClass veraClass = (VeraClass) obj.getStructuredType();
        return veraClass != null ? (JavaRawClass) translateType(veraClass)
            : null;
    }

    private JavaRawClass getClassForGlobal(
        Container container,
        String id,
        JavaPackage pkg)
    {
        // check whether class has already been created for this container
        JavaRawClass cls = globalClasses.get(container);
        if (cls != null) return cls;

        // in case two Vera containers have the same Java class name (e.g.
        // foo.vr and foo.vri -> FooGlobals), search for class by name
        Iterator iter = pkg.lookupObjects(id, JavaNameKind.TYPE);
        if (iter.hasNext())
        {
            cls = (JavaRawClass) iter.next();
            return cls;
        }

        // create public final class
        cls = new JavaRawClass(schema, id, pkg);
        cls.setBaseClass(types.objectType);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);
        pkg.addMember(cls);
        globalClasses.put(container, cls);

        // create empty private ctor to prevent instantiation
        JavaConstructor ctor = cls.newConstructor();
        ctor.setVisibility(JavaVisibility.PRIVATE);
        ctor.setBody(new JavaBlock(schema));

        return cls;
    }

    private JavaRawClass getClassForGlobal(
        VeraCompilationUnitMember obj,
        JavaPackage pkg)
    {
        final CompilationUnit compUnit = obj.getCompilationUnit();
        final String id = getIDForCompUnit(compUnit, true) + "Globals";
        return getClassForGlobal(compUnit, id, pkg);
    }

    protected static String getIDForCompUnit(
        CompilationUnit compUnit,
        boolean uppercase)
    {
        File srcFile = new File(compUnit.getSourcePath());
        String srcName = srcFile.getName();
        int dotPos = srcName.lastIndexOf('.');
        if (dotPos >= 0) srcName = srcName.substring(0, dotPos);
        return makeJavaIdentifier(srcName, uppercase);
    }

    protected static String makeJavaIdentifier(String str, boolean uppercase)
    {
        StringBuffer buf = null;
        int len = str.length();
        for (int i = 0; i < len; ++i)
        {
            char c = str.charAt(i);
            boolean valid = (i == 0) ? Character.isJavaIdentifierStart(c)
                : Character.isJavaIdentifierPart(c);

            char newc = c;
            if (!valid)
            {
                newc = '_';
            }
            else if (i == 0 && uppercase)
            {
                newc = Character.toUpperCase(c);
            }

            if (newc != c)
            {
                if (buf == null)
                {
                    buf = new StringBuffer(len);
                    buf.append(str.substring(0, i));
                }
                buf.append(newc);
            }
            else if (buf != null)
            {
                buf.append(newc);
            }
        }
        return buf != null ? buf.toString() : str;
    }

    protected JavaFunction translateGlobalFunction(
        VeraGlobalFunction obj,
        JavaPackage pkg)
    {
        // check whether function has already been translated
        JavaFunction func = (JavaFunction) xlatObjMap.getJavaObject(obj);
        if (func != null) return func;

        String id = obj.getName().getIdentifier();
        logEnter("Translating global function: " + id);

        // translate function type
        final VeraFunctionType veraFuncType = obj.getType();
        final JavaFunctionType funcType = translateFunctionType(veraFuncType,
            obj, true);

        // translate visibility
        final JavaVisibility vis = obj.getVisibility() == VeraVisibility.LOCAL
            ? JavaVisibility.DEFAULT : JavaVisibility.PUBLIC;

        // get containing class for function
        final JavaRawClass cls = getClassForGlobal(obj, pkg);

        // ensure identifier is not a Java keyword
        id = fixID(id);

        // create Java static function
        func = new JavaFunction(id, funcType);
        func.addAnnotations(obj.getAnnotations());
        func.setVisibility(vis);
        func.addModifier(JavaFunctionModifier.STATIC);
        cls.addMember(func);
        xlatObjMap.addJavaObject(obj, func);

        // translate function body
        final VeraBlock veraBody = obj.getBody();
        final JavaBlock body = translateBlock(veraBody, obj, veraFuncType,
            func, funcType, false);
        func.setBody(body);

        logExit();
        return func;
    }

    protected JavaFunction translateGlobalFunction(VeraGlobalFunction obj)
    {
        return translateGlobalFunction(obj, translateCompUnitOf(obj));
    }

    protected JavaFunction translateHDLFunction(
        VeraHDLFunction obj,
        JavaPackage pkg)
    {
        // check whether function has already been translated
        JavaFunction func = (JavaFunction) xlatObjMap.getJavaObject(obj);
        if (func != null) return func;

        final String veraID = obj.getName().getIdentifier();
        logEnter("Translating HDL function: " + veraID);

        // ensure identifier is not a Java keyword
        final String id = fixID(veraID);
        // translate Vera hdl_task to Ifgen hdl_task
        final IfgenPackage ifPkg = ifSchema.getPackage(pkg.getName()
            .getCanonicalName(), true);
        final IfgenName ifName = new IfgenName(id, IfgenNameKind.METHOD, ifPkg);
        final IfgenHDLTask ifTask = new IfgenHDLTask(ifSchema, ifName);
        ifTask.setInstancePath(new IfgenModuleDef(ifSchema, IfgenUnresolvedName.parse(obj.getInstPath()), false));
        ifTask.addAnnotations(obj.getAnnotations());
        ifPkg.addMember(ifTask);
        xlatObjMap.addIfgenObject(obj, ifTask);

        // translate arguments
        final VeraFunctionType veraFuncType = obj.getType();
        for (final VeraFunctionArgument arg : veraFuncType.getArguments())
        {
            final String argID = arg.getName().getIdentifier();
            final IfgenName ifArgName = new IfgenName(fixID(argID),
                IfgenNameKind.EXPRESSION);
            final IfgenDirection ifArgDir = arg.isByRef()
                ? IfgenDirection.INOUT : IfgenDirection.INPUT;
            final VeraType argType = arg.getType();
            final IfgenType ifArgType;
            final int argSize;
            if (argType instanceof VeraBitVectorType)
            {
                VeraBitVectorType bvType = (VeraBitVectorType) argType;
                ifArgType = ifSchema.BIT_TYPE;
                argSize = bvType.getSize();
            }
            else if (argType instanceof VeraBitType)
            {
                ifArgType = ifSchema.BIT_TYPE;
                argSize = 1;
            }
            else if (argType instanceof VeraIntegerType)
            {
                ifArgType = ifSchema.INTEGER_TYPE;
                argSize = 1;
            }
            else
            {
                throw new RuntimeException("Unexpected argument type '"
                    + argType + "' for hdl_task '" + veraID + "'");
            }
            final IfgenTaskArg ifArg = new IfgenTaskArg(ifSchema, ifArgName,
                ifArgDir, ifArgType, argSize);
            ifArg.addAnnotations(arg.getAnnotations());
            ifTask.addArgument(ifArg);
            xlatObjMap.addIfgenObject(arg, ifArg);
        }

        // translate function type
        final JavaFunctionType funcType = translateFunctionType(veraFuncType,
            obj, true);

        // get containing class for function
        final JavaRawClass cls = getClassForGlobal(obj, pkg);

        // create Java static function
        func = new JavaFunction(id, funcType);
        func.addAnnotations(obj.getAnnotations());
        func.setVisibility(JavaVisibility.PUBLIC);
        func.addModifier(JavaFunctionModifier.STATIC);
        cls.addMember(func);
        xlatObjMap.addJavaObject(obj, func);

        // generate function body:
        // DV.simulation.callVerilogTask("taskname", new Object[] { args });
        final JavaVariableReference simRef = new JavaVariableReference(
            types.dvType.getField("simulation"));
        final JavaArrayInitializer argsInitExpr = new JavaArrayInitializer(
            types.objectArrayType);
        final Iterator argIter = funcType.getArguments().iterator();
        while (argIter.hasNext())
        {
            JavaFunctionArgument arg = (JavaFunctionArgument) argIter.next();
            argsInitExpr.addElement(exprConv
                .toObject(new JavaVariableReference(arg)));
        }
        final JavaArrayCreation argsNewExpr = new JavaArrayCreation(
            types.objectArrayType);
        argsNewExpr.setInitializer(argsInitExpr);
        final JavaFunctionInvocation callExpr = ExpressionBuilder.memberCall(
            simRef, "callVerilogTask", new JavaStringLiteral(schema, veraID),
            argsNewExpr);
        final JavaBlock body = new JavaBlock(schema);
        body.addMember(new JavaExpressionStatement(callExpr));
        func.setBody(body);

        logExit();
        return func;
    }

    protected JavaFunctor translateMemberFunctionOrCtor(
        VeraMemberFunction obj,
        JavaRawAbstractClass cls)
    {
        if (obj.isConstructor())
        {
            return translateConstructor(obj, cls);
        }
        else
        {
            return translateMemberFunction(obj, cls);
        }
    }

    protected JavaConstructor translateConstructor(
        VeraMemberFunction obj,
        JavaRawAbstractClass cls)
    {
        // check whether constructor has already been translated
        JavaConstructor ctor = (JavaConstructor) xlatObjMap.getJavaObject(obj);
        if (ctor != null) return ctor;

        final String clsID = cls.getName().getIdentifier();
        final String id = obj.getName().getIdentifier();
        logEnter("Translating constructor: " + clsID + "." + id);

        try
        {
            // if this is a hand-translated class, just look up a constructor
            // (it doesn't matter if there are overloaded versions; the
            // expression translator will find the right one itself)
            if (!(cls instanceof TranslatedClass))
            {
                final Set ctors = cls.getConstructors(cls);
                if (ctors.isEmpty())
                {
                    throw new RuntimeException(
                        "Manually translated constructor not found for " + obj
                            + " in " + cls);
                }
                ctor = (JavaConstructor) ctors.iterator().next();
                xlatObjMap.addJavaObject(obj, ctor);
                return ctor;
            }

            // translate function type
            final VeraFunctionType veraFuncType = obj.getType();
            final JavaFunctionType funcType = translateFunctionType(
                veraFuncType, obj, true);

            // translate visibility
            final JavaVisibility vis = translateMemberVisibility(obj
                .getVisibility());

            // create Java constructor
            ctor = new JavaConstructor(funcType);
            ctor.addAnnotations(obj.getAnnotations());
            ctor.setVisibility(vis);
            cls.addMember(ctor);
            xlatObjMap.addJavaObject(obj, ctor);

            if (!obj.isImplicit())
            {
                // get the analysis for this class
                final VeraUserClass veraCls = (VeraUserClass) obj
                    .getStructuredType();
                final ClassAnalysis clsAnalysis = analyzer
                    .getClassAnalysis(veraCls);
                final boolean makeDefCtor = clsAnalysis.needDefaultCtor()
                    && !clsAnalysis.hasDefaultCtor();
                final boolean transformSuperCall = makeDefCtor
                    || clsAnalysis.transformSuperCall();

                // translate constructor body
                final VeraBlock veraBody = obj.getBody();
                JavaBlock body = translateBlock(veraBody, null, veraFuncType,
                    ctor, funcType, transformSuperCall);

                // check whether default constructor must be introduced
                if (makeDefCtor)
                {
                    // add an empty, protected default constructor
                    final JavaConstructor defCtor = new JavaConstructor(schema);
                    defCtor.setVisibility(JavaVisibility.PROTECTED);
                    defCtor.setBody(new JavaBlock(schema));
                    cls.addMember(defCtor);

                    // create init() method using translated constructor body
                    final JavaFunctionType initType = new JavaFunctionType(
                        funcType);
                    final JavaFunction initFunc = new JavaFunction("init",
                        initType);
                    initFunc.setVisibility(JavaVisibility.PROTECTED);
                    initFunc.setBody(body);
                    cls.addMember(initFunc);

                    // generate init() call for body of translated constructor
                    final JavaFunctionInvocation initCall = new JavaFunctionInvocation(
                        new JavaFunctionReference(initFunc));
                    final Iterator argIter = funcType.getArguments().iterator();
                    while (argIter.hasNext())
                    {
                        final JavaFunctionArgument arg = (JavaFunctionArgument) argIter
                            .next();
                        initCall.addArgument(new JavaVariableReference(arg));
                    }
                    body = new JavaBlock(schema);
                    body.addMember(new JavaExpressionStatement(initCall));
                }

                ctor.setBody(body);
            }
            else
            {
                // generate empty body for implicit constructor
                // TODO: generate super() call using base ctor args if present
                // TODO: add implicit modifier to Java constructor?
                ctor.setBody(new JavaBlock(schema));
            }

            // generate additional constructor without optional arguments
            if (hasOptionalArgs(veraFuncType))
            {
                final JavaFunctionType optFuncType = translateFunctionType(
                    veraFuncType, obj, false);
                final JavaConstructor optCtor = new JavaConstructor(optFuncType);
                optCtor.setVisibility(vis);
                cls.addMember(optCtor);
                final JavaBlock optBody = new JavaBlock(schema);
                final JavaConstructorInvocation optCtorCall = new JavaConstructorInvocation(
                    new JavaConstructorReference(ctor));
                final Iterator veraArgIter = veraFuncType.getArguments()
                    .iterator();
                final Iterator javaArgIter = funcType.getArguments().iterator();
                final Iterator optArgIter = optFuncType.getArguments()
                    .iterator();
                while (veraArgIter.hasNext())
                {
                    VeraFunctionArgument veraArg = (VeraFunctionArgument) veraArgIter
                        .next();
                    JavaFunctionArgument javaArg = (JavaFunctionArgument) javaArgIter
                        .next();
                    JavaExpression optArgExpr;
                    if (veraArg.isOptional())
                    {
                        VeraExpression veraArgExpr = veraArg.getInitializer();
                        assert (veraArgExpr != null);
                        JavaType javaArgType = javaArg.getType();
                        ConvertedExpression convExpr = translateExpr(
                            veraArgExpr, null, cls, null, null, javaArgType,
                            javaArgType);
                        optArgExpr = convExpr.toBlockExpr(optBody, "arg");
                        JavaType optExprType = optArgExpr.getResultType();
                        optArgExpr = convertRHS(optArgExpr, optExprType,
                            javaArgType, true);
                    }
                    else
                    {
                        JavaFunctionArgument optArg = (JavaFunctionArgument) optArgIter
                            .next();
                        optArgExpr = new JavaVariableReference(optArg);
                    }
                    optCtorCall.addArgument(optArgExpr);
                }
                optBody.addMember(new JavaExpressionStatement(optCtorCall));
                optCtor.setBody(optBody);
            }

            return ctor;
        }
        finally
        {
            logExit();
        }
    }

    private JavaFunction translateMemberFunction(
        VeraMemberFunction obj,
        JavaRawAbstractClass cls)
    {
        // check whether function has already been translated
        JavaFunction func = (JavaFunction) xlatObjMap.getJavaObject(obj);
        if (func != null) return func;

        String clsID = cls.getName().getIdentifier();
        String id = obj.getName().getIdentifier();
        logEnter("Translating member function: " + clsID + "." + id);

        try
        {
            // if this is a hand-translated class, just look up the method by name
            // (it doesn't matter if there are overloaded versions; the expression
            // translator will find the right one itself)
            if (!(cls instanceof TranslatedClass))
            {
                final Set methods = cls.getMethods(id, cls);
                if (methods.isEmpty())
                {
                    throw new RuntimeException(
                        "Manually translated method not found for " + obj
                            + " in " + cls);
                }
                func = (JavaFunction) methods.iterator().next();
                xlatObjMap.addJavaObject(obj, func);
                return func;
            }

            // get global analysis for function
            FunctionAnalysis funcAnalysis = analyzer.getFunctionAnalysis(obj);
            if (funcAnalysis != null && funcAnalysis.isNonVirtualOverride())
            {
                // if function is involved in non-virtual overriding,
                // prefix its identifier with the class identifier
                id = clsID + "_" + id;
            }

            // translate function type
            final VeraFunctionType veraFuncType = obj.getType();
            final JavaFunctionType funcType = translateFunctionType(
                veraFuncType, obj, true);

            // translate visibility
            final JavaVisibility vis = translateMemberVisibility(obj
                .getVisibility());

            // ensure identifier is not a Java keyword
            id = fixID(id);

            // check for illegal method override
            boolean anyIllegal = false;
            final String newID = buildID("juno", id);
            final JavaType[] argTypes = getArgTypes(funcType);
            final Set<JavaFunction> methods = cls.getMethods(id, argTypes,
                true, true, cls);
            final Iterator<JavaFunction> methodIter = methods.iterator();
            while (methodIter.hasNext())
            {
                final JavaFunction method = methodIter.next();
                final StructuredType methodType = method.getStructuredType();

                // any methods found should be in a base class/interface
                assert (methodType != cls);

                boolean thisIllegal = false;
                if (method.getModifiers().contains(JavaFunctionModifier.FINAL))
                {
                    log("Warning: Member function '" + cls.toReferenceString()
                        + "." + id + "' conflicts with final method in "
                        + methodType.getName() + "; renaming to " + newID);
                    thisIllegal = true;
                }
                if (vis.isLessVisible(method.getVisibility()))
                {
                    log("Warning: Member function '" + cls.toReferenceString()
                        + "." + id + "' conflicts with method in "
                        + methodType.getName()
                        + " with greater visibility; renaming to " + newID);
                    thisIllegal = true;
                }
                JavaType newReturnType = funcType.getReturnType();
                JavaType baseReturnType = method.getType().getReturnType();
                if (!newReturnType.equals(baseReturnType))
                {
                    log("Warning: Member function '" + cls.toReferenceString()
                        + "." + id + "' (returning "
                        + newReturnType.toReferenceString()
                        + ") conflicts with method in " + methodType.getName()
                        + " (returning " + baseReturnType.toReferenceString()
                        + ") due to different return types; renaming to "
                        + newID);
                    thisIllegal = true;
                }

                if (thisIllegal)
                {
                    // any illegal override should be from a non-translated class,
                    // such as java.lang.Object
                    assert (!(methodType instanceof TranslatedClass));
                    anyIllegal = true;
                }
            }
            if (anyIllegal) id = newID;

            // create Java function
            func = new JavaFunction(id, funcType);
            func.addAnnotations(obj.getAnnotations());
            func.setVisibility(vis);
            cls.addMember(func);
            xlatObjMap.addJavaObject(obj, func);

            // translate non-virtual functions as final
            if (!obj.isVirtual())
            {
                func.addModifier(JavaFunctionModifier.FINAL);
            }

            // translate function body
            if (obj.isPureVirtual())
            {
                assert (obj.isVirtual());
                func.addModifier(JavaFunctionModifier.ABSTRACT);
            }
            else
            {
                final VeraBlock veraBody = obj.getBody();
                if (veraBody == null)
                {
                    log("Warning: Member function '" + cls.toReferenceString()
                        + "." + id + "' was not translated; generating stub");
                }

                final JavaBlock body = translateBlock(veraBody, obj,
                    veraFuncType, func, funcType, false);
                func.setBody(body);
            }

            return func;
        }
        finally
        {
            logExit();
        }
    }

    protected static JavaType[] getArgTypes(JavaFunctionType funcType)
    {
        final List<JavaFunctionArgument> args = funcType.getArguments();
        final JavaType[] argTypes = new JavaType[args.size()];
        final Iterator<JavaFunctionArgument> argIter = args.iterator();
        int argIndex = 0;
        while (argIter.hasNext())
        {
            JavaFunctionArgument arg = argIter.next();
            argTypes[argIndex++] = arg.getType();
        }
        return argTypes;
    }

    protected static JavaExpression[] getArgArray(List<JavaExpression> args)
    {
        final JavaExpression[] argArray = new JavaExpression[args.size()];
        args.toArray(argArray);
        return argArray;
    }

    protected static String buildID(String part1, String part2)
    {
        return part1 + Character.toUpperCase(part2.charAt(0))
            + part2.substring(1);
    }

    protected boolean isReturnXZ(
        VeraFunction func,
        FunctionAnalysis baseFuncAnalysis)
    {
        boolean xz;
        if (baseFuncAnalysis != null)
        {
            xz = baseFuncAnalysis.isReturnXZInOverride();
        }
        else
        {
            VeraFunctionType funcType = func.getType();
            VeraLocalVariable returnVar = func.getReturnVar();
            xz = funcType.isReturnsXZ();
            if (!xz && returnVar != null)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(returnVar);
                xz = varAnalysis == null || !varAnalysis.isNotAssignedXZ();
            }
        }
        return xz;
    }

    protected boolean isArgumentXZ(
        VeraFunction func,
        int index,
        FunctionAnalysis baseFuncAnalysis)
    {
        boolean xz;
        if (baseFuncAnalysis != null)
        {
            xz = baseFuncAnalysis.isArgumentXZInOverride(index);
        }
        else
        {
            VeraFunctionType funcType = func.getType();
            VeraFunctionArgument arg = funcType.getArguments().get(index);
            xz = arg.isReturnsXZ();
            if (!xz)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(arg);
                xz = varAnalysis == null || !varAnalysis.isNotAssignedXZ();
            }
        }
        return xz;
    }

    protected boolean isArgumentStatefulString(
        VeraFunction func,
        int index,
        FunctionAnalysis baseFuncAnalysis)
    {
        boolean stateful;
        if (baseFuncAnalysis != null)
        {
            stateful = baseFuncAnalysis
                .isArgumentStatefulStringInOverride(index);
        }
        else
        {
            VeraFunctionType funcType = func.getType();
            VeraFunctionArgument arg = funcType.getArguments().get(index);
            VariableAnalysis varAnalysis = analyzer.getVariableAnalysis(arg);
            stateful = varAnalysis == null
                || varAnalysis.isNeedStatefulString();
        }
        return stateful;
    }

    private boolean hasOptionalArgs(VeraFunctionType veraFuncType)
    {
        Iterator iter = veraFuncType.getArguments().iterator();
        while (iter.hasNext())
        {
            VeraFunctionArgument veraArg = (VeraFunctionArgument) iter.next();
            if (veraArg.isOptional()) return true;
        }
        return false;
    }

    private JavaFunctionType translateFunctionType(
        VeraFunctionType veraFuncType,
        VeraFunction func,
        boolean includeOptional)
    {
        // get function analysis of base function
        FunctionAnalysis baseFuncAnalysis = analyzer
            .getBaseFunctionAnalysis(func);

        // translate return type
        final VeraType veraReturnType = veraFuncType.getReturnType();
        final JavaType returnType;
        if (veraReturnType != null)
        {
            returnType = translateType(veraReturnType, isReturnXZ(func,
                baseFuncAnalysis), false);
        }
        else
        {
            returnType = schema.voidType;
        }

        // create Java function type
        final JavaFunctionType funcType = new JavaFunctionType(returnType);

        // translate arguments
        Iterator iter = veraFuncType.getArguments().iterator();
        for (int index = 0; iter.hasNext(); ++index)
        {
            VeraFunctionArgument veraArg = (VeraFunctionArgument) iter.next();
            if (!includeOptional && veraArg.isOptional()) continue;
            String argID = veraArg.getName().getIdentifier();
            final VeraType veraArgType = veraArg.getType();
            final boolean byRef = veraArg.isByRef();
            JavaType argType = translateType(veraArgType, isArgumentXZ(func,
                index, baseFuncAnalysis), byRef
                && isArgumentStatefulString(func, index, baseFuncAnalysis));
            final boolean needsClone = needsClone(argType);
            boolean argFinal = needsClone;

            if (!needsClone)
            {
                if (byRef)
                {
                    argID += "_ref";
                    argType = getRefHolderType(argType);
                    argFinal = true;
                }
                else
                {
                    final VariableAnalysis varAnalysis = analyzer
                        .getVariableAnalysis(veraArg);
                    if (varAnalysis != null && !varAnalysis.isWriteAssigned())
                    {
                        log("Note: Marking argument '" + argID + "' in "
                            + func.getName() + " as final");
                        argFinal = true;
                    }
                }
            }

            JavaFunctionArgument arg = new JavaFunctionArgument(fixID(argID),
                argType);
            if (argFinal) arg.addModifier(JavaVariableModifier.FINAL);
            //addLengthAnnotation(arg, argType);
            funcType.addArgument(arg);
            xlatObjMap.addJavaObject(veraArg, arg);
        }
        return funcType;
    }

    protected boolean needsClone(JavaType type)
    {
        // true for types that have value/copy semantics in Vera,
        // but have mutable reference semantics in Java
        return type instanceof JoveFixedArrayType
            || exprConv.isAssocArray(type) || type == types.junoStringType;
    }

    protected boolean needHolderVar(
        JavaType javaType,
        VeraVariable veraVar,
        VariableAnalysis varAnalysis)
    {
        if (!needsClone(javaType) && varAnalysis != null)
        {
            if (varAnalysis.isPassedByRefNVA())
            {
                // variables passed by-ref need holder (optimization)
                return true;
            }
            if ((varAnalysis.isForkRead() || varAnalysis.isForkWrite())
                && !veraVar.hasModifier(VeraVariableModifier.SHADOW))
            {
                // non-shadow variables read or written in fork need holder
                return true;
            }
        }
        return false;
    }

    protected JavaType getRefHolderType(JavaType argType)
    {
        return schema.getArrayType(argType, 1);
    }

    protected JavaBlock translateBlock(
        VeraBlock veraBlock,
        VeraFunction veraFunc,
        VeraFunctionType veraFuncType,
        JavaClassMember member,
        JavaFunctionType funcType,
        boolean transformSuperNewCall)
    {
        JavaBlock block = new JavaBlock(schema);
        final TempBlockScope tempScope = new TempBlockScope(funcType);

        // get the analysis for this block
        final BlockAnalysis analysis = analyzer.getBlockAnalysis(veraBlock);

        // add variable reference mappers for by-ref arguments if present
        VarInfoMap varInfoMap = new VarInfoMap();
        if (veraFuncType != null)
        {
            for (VeraFunctionArgument veraArg : veraFuncType.getArguments())
            {
                JavaVariable javaArg = translateVariable(veraArg);
                JavaType javaArgType = javaArg.getType();

                VarInfo info = null;
                if (veraArg.isByRef())
                {
                    if (!needsClone(javaArgType))
                    {
                        // argument has been translated as by-ref holder
                        info = new VarInfo(schema, veraArg, javaArg, true);
                    }
                }
                else
                {
                    VariableAnalysis varAnalysis = analyzer
                        .getVariableAnalysis(veraArg);

                    // need to introduce JunoString for by-value argument?
                    if (varAnalysis != null
                        && varAnalysis.isNeedStatefulString())
                    {
                        // create local variable (JunoString)
                        String id = javaArg.getName().getIdentifier();
                        JavaRawClass junoStringType = types.junoStringType;
                        JavaLocalVariable localVar = VarBuilder.createLocalVar(
                            tempScope, id + "_JunoString", junoStringType);
                        tempScope.addObject(localVar);
                        localVar.addModifier(JavaVariableModifier.FINAL);
                        localVar
                            .setInitializer(ExpressionBuilder.newInstance(
                                junoStringType, new JavaVariableReference(
                                    javaArg)));
                        block.addMember(localVar);

                        // translate variable references to JunoString
                        info = new VarInfo(schema, veraArg, localVar, false);
                    }
                    // need to introduce holder for by-value argument?
                    else if (needHolderVar(javaArgType, veraArg, varAnalysis))
                    {
                        // create local variable (holder)
                        String id = javaArg.getName().getIdentifier();
                        JavaArrayType holderType = schema.getArrayType(
                            javaArgType, 1);
                        JavaLocalVariable localVar = VarBuilder.createLocalVar(
                            tempScope, id + "_holder", holderType);
                        tempScope.addObject(localVar);
                        localVar.addModifier(JavaVariableModifier.FINAL);
                        JavaArrayInitializer arrayInit = new JavaArrayInitializer(
                            holderType);
                        arrayInit
                            .addElement(new JavaVariableReference(javaArg));
                        localVar.setInitializer(arrayInit);
                        block.addMember(localVar);

                        // translate variable references to holder
                        info = new VarInfo(schema, veraArg, localVar, true);
                    }
                }

                if (info != null)
                {
                    varInfoMap.addInfo(info);
                }
            }
        }

        final JavaRawAbstractClass cls = (JavaRawAbstractClass) member
            .getStructuredType();

        // add return variable to block if present
        JavaLocalVariable returnVar = null;
        if (veraFunc != null)
        {
            returnVar = translateReturnVar(veraFunc, funcType, cls, member,
                block, tempScope, varInfoMap);
        }

        if (veraBlock != null)
        {
            // translate the block members
            final BlockMemberTranslator xlat = new BlockMemberTranslator(this,
                block, tempScope, varInfoMap, returnVar, cls, member);
            xlat.setTransformSuperNewCall(transformSuperNewCall);
            xlat.translateBlockMembers(veraBlock);
            block.addAnnotations(veraBlock.getAnnotations());
        }
        else
        {
            // generate stub for missing body of UDF or untranslated function
            block.addMember(new JavaThrowStatement(ExpressionBuilder
                .newInstance(types.unsuppOpType)));
        }

        // make sure last statement returns result variable
        if (returnVar != null)
        {
            List members = block.getMembers();
            Object lastMember = members.get(members.size() - 1);
            if (!(lastMember instanceof JavaReturnStatement))
            {
                addReturnStatement(block, returnVar, funcType.getReturnType(),
                    varInfoMap);
            }
        }

        // create thread context if necessary
        block = checkThreadContext(block, analysis);

        return block;
    }

    protected JavaReturnStatement addReturnStatement(
        JavaBlock block,
        JavaLocalVariable returnVar,
        JavaType returnType,
        VarInfoMap varInfoMap)
    {
        JavaExpression returnExpr = null;
        if (returnVar != null)
        {
            VarInfo info = varInfoMap.getInfo(returnVar);
            if (info != null)
            {
                returnExpr = info.getReference();
            }
            else
            {
                returnExpr = new JavaVariableReference(returnVar);
            }
            returnExpr = exprConv.toType(returnType, returnExpr);
        }
        JavaReturnStatement stmt = new JavaReturnStatement(schema, returnExpr);
        block.addMember(stmt);
        return stmt;
    }

    protected JavaBlock checkThreadContext(
        JavaBlock block,
        BlockAnalysis analysis)
    {
        // block ->
        // enter_context();
        // try { <block> }
        // finally { leave_context(); }
        if (analysis != null && analysis.isNeedThreadContext())
        {
            JavaBlock ctxBlock = new JavaBlock(schema);

            ctxBlock.addMember(new JavaExpressionStatement(ExpressionBuilder
                .staticCall(types.junoType, "enter_context")));

            JavaTryStatement tryStmt = new JavaTryStatement(block);
            ctxBlock.addMember(tryStmt);

            JavaBlock finallyBlock = new JavaBlock(schema);
            finallyBlock.addMember(new JavaExpressionStatement(
                ExpressionBuilder.staticCall(types.junoType, "leave_context")));
            tryStmt.setFinallyBlock(finallyBlock);

            block = ctxBlock;
        }
        return block;
    }

    // cls and assocMember are only required for static variables
    protected JavaVariable translateLocalVariable(
        VeraLocalVariable obj,
        boolean isReturnVar,
        JavaRawAbstractClass cls,
        JavaClassMember assocMember,
        JavaBlock block,
        TempBlockScope tempScope,
        JavaLocalVariable returnVar,
        VarInfoMap varInfoMap)
    {
        String id = obj.getName().getIdentifier();

        // get the analysis for this variable
        final VariableAnalysis varAnalysis = analyzer.getVariableAnalysis(obj);
        if (!isReturnVar && varAnalysis != null && !varAnalysis.isReadAccess())
        {
            // suppress translation of unreferenced variables
            // NOTE: unread (but assigned) variables could technically be
            // suppressed as well, but that would involve translating their
            // assignment expressions into statements
            if (!varAnalysis.isWriteAccess())
            {
                log("Warning: Suppressing translation of unreferenced local '"
                    + id + "' in " + describeClassMember(assocMember));
                return null;
            }
            else
            {
                String msg = "Warning: Local variable '" + id + "' in "
                    + describeClassMember(assocMember) + " is never read";
                if (varAnalysis.isWriteSideEffects())
                {
                    msg += ", but assignment has side effects";
                }
                log(msg);
            }
        }

        // create variable
        JavaVariable var;
        id = fixID(id);
        VeraType veraType = obj.getType();
        JavaType type = translateType(veraType, varAnalysis == null
            || !varAnalysis.isNotAssignedXZ(), varAnalysis == null
            || varAnalysis.isNeedStatefulString());
        VeraExpression veraInitExpr = obj.getInitializer();
        if (obj.hasModifier(VeraVariableModifier.STATIC))
        {
            // Vera static local variable becomes Java static member variable
            JavaMemberVariable memberVar = VarBuilder.createMemberVar(cls, id,
                type);
            memberVar.setVisibility(JavaVisibility.PRIVATE);
            memberVar.addModifier(JavaVariableModifier.STATIC);
            cls.addMemberBefore(memberVar, assocMember);
            var = memberVar;

            // cloned variables are final
            if (needsClone(type))
            {
                memberVar.addModifier(JavaVariableModifier.FINAL);
            }

            // translate initializer expression
            JavaExpression initExpr;
            if (veraInitExpr != null)
            {
                ConvertedExpression convExpr = translateExpr(veraInitExpr,
                    tempScope, cls, varInfoMap, returnVar, type, type);
                convertInitializer(convExpr, type);
                initExpr = convExpr.toMemberInitExpr(cls, memberVar);
            }
            else
            {
                initExpr = getInitValue(type, false, false);
            }
            var.setInitializer(initExpr);

            // create wait_var event if necessary
            checkWaitVarMember(obj, memberVar, varAnalysis, cls, assocMember);
        }
        else
        {
            JavaLocalVariable localVar;
            if (needHolderVar(type, obj, varAnalysis))
            {
                // create local variable (holder)
                JavaArrayType holderType = schema.getArrayType(type, 1);
                localVar = VarBuilder.createLocalVar(tempScope, id + "_holder",
                    holderType);
                tempScope.addObject(localVar);
                var = localVar;

                // holder variables are always final
                localVar.addModifier(JavaVariableModifier.FINAL);

                // translate initializer expression
                JavaArrayInitializer arrayInit = new JavaArrayInitializer(
                    holderType);
                if (veraInitExpr != null)
                {
                    ConvertedExpression convExpr = translateExpr(veraInitExpr,
                        tempScope, cls, varInfoMap, returnVar, type, type);
                    convertInitializer(convExpr, type);
                    arrayInit.addElement(convExpr.getResultExpr());
                    convExpr.setResultExpr(arrayInit);
                    convExpr.toLocalVar(block, localVar);
                }
                else
                {
                    arrayInit.addElement(getInitValue(type, true, false));
                    localVar.setInitializer(arrayInit);
                    block.addMember(localVar);
                }

                // record that variable was translated as holder
                varInfoMap.addInfo(new VarInfo(schema, obj, localVar, true));
            }
            else
            {
                // create local variable
                localVar = VarBuilder.createLocalVar(tempScope, id, type);
                tempScope.addObject(localVar);
                var = localVar;

                boolean needInit = true;
                if (needsClone(type))
                {
                    // cloned variables are always final and initialized
                    var.addModifier(JavaVariableModifier.FINAL);
                }
                else if (varAnalysis != null && !varAnalysis.isReadUnassigned())
                {
                    // local variables not read when unassigned and not written
                    // when assigned can be marked as final
                    if (varAnalysis.isWaitVar())
                    {
                        // the wait_var checking code does not support final
                        // fields since they are uninitialized before the
                        // assignment (i.e. they have no old value that can be
                        // read); such code is nonsensical and should be fixed
                        // in Vera
                        log("Warning: Local variable '" + obj.getName()
                            + "' in " + describeClassMember(assocMember)
                            + " is never changed and should be marked final, "
                            + "but is a wait_var target");
                    }
                    else if (!varAnalysis.isWriteAssigned())
                    {
                        log("Note: Marking local variable '" + obj.getName()
                            + "' in " + describeClassMember(assocMember)
                            + " as final");
                        var.addModifier(JavaVariableModifier.FINAL);
                        needInit = false;
                    }
                    else
                    {
                        // variable is not final, but does not need to be
                        // initialized, since it is not read when unassigned
                        // TODO: this case does not handle initialization of
                        // forked shadow variables; need separate DA/DU analysis
                        // of shadow variables to optimize away unnecessary init
                        needInit = varAnalysis.isForkRead();
                    }
                }

                // translate initializer expression
                if (veraInitExpr != null)
                {
                    ConvertedExpression convExpr = translateExpr(veraInitExpr,
                        tempScope, cls, varInfoMap, returnVar, type, type);
                    convertInitializer(convExpr, type);
                    convExpr.toLocalVar(block, localVar);
                }
                else
                {
                    if (needInit)
                    {
                        localVar
                            .setInitializer(getInitValue(type, true, false));
                    }
                    block.addMember(localVar);
                }
            }

            // create wait_var event if necessary
            checkWaitVarLocal(obj, localVar, varAnalysis, block, tempScope);
        }
        var.addAnnotations(obj.getAnnotations());
        //addLengthAnnotation(var, type);
        xlatObjMap.addJavaObject(obj, var);
        return var;
    }

    protected String describeClassMember(JavaClassMember member)
    {
        if (member instanceof NamedObject)
        {
            return ((NamedObject) member).getName().getCanonicalName();
        }
        else
        {
            StructuredType cls = member.getStructuredType();
            Name clsName = cls.getName();
            if (member instanceof JavaConstructor)
            {
                return clsName.getCanonicalName() + "."
                    + clsName.getIdentifier();
            }
            else
            {
                assert (member instanceof JavaInitializerBlock);
                return "initializer in " + clsName;
            }
        }
    }

    private JavaLocalVariable translateReturnVar(
        VeraFunction veraFunc,
        JavaFunctionType funcType,
        JavaRawAbstractClass cls,
        JavaClassMember assocMember,
        JavaBlock block,
        TempBlockScope tempScope,
        VarInfoMap varInfoMap)
    {
        JavaLocalVariable returnVar = null;

        // create return variable if function result is non-void
        JavaType returnType = funcType.getReturnType();
        if (!(returnType instanceof JavaVoidType))
        {
            VeraLocalVariable veraReturnVar = veraFunc.getReturnVar();
            if (veraReturnVar != null)
            {
                // translate return variable
                returnVar = (JavaLocalVariable) translateLocalVariable(
                    veraReturnVar, true, cls, assocMember, block, tempScope,
                    null, varInfoMap);
            }
            else
            {
                // UDF functions do not have a return variable
            }
        }

        return returnVar;
    }

    protected void checkWaitVarLocal(
        VeraVariable veraVar,
        JavaLocalVariable javaVar,
        VariableAnalysis varAnalysis,
        JavaBlock block,
        TempBlockScope tempScope)
    {
        if (varAnalysis != null && varAnalysis.isWaitVar())
        {
            final VeraType veraType = veraVar.getType();
            final JavaType eventType = getWaitVarEventType(veraType);
            final JavaLocalVariable eventVar = VarBuilder.createLocalVar(
                tempScope, getEventVarID(veraVar), eventType);
            tempScope.addObject(eventVar);
            eventVar.addModifier(JavaVariableModifier.FINAL);
            eventVar.setInitializer(getInitValue(eventType, true, false));
            block.addMember(eventVar);
            xlatObjMap.addWaitVarEvent(javaVar, eventVar);
        }
    }

    private String getEventVarID(VeraVariable veraVar)
    {
        return fixID(veraVar.getName().getIdentifier()) + "_changed";
    }

    protected JavaMemberVariable translateGlobalVariable(
        VeraGlobalVariable obj,
        JavaPackage pkg)
    {
        // check whether variable has already been translated
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap
            .getJavaObject(obj);
        if (var != null) return var;

        String id = obj.getName().getIdentifier();
        logEnter("Translating global variable: " + id);

        // get the analysis for this variable
        final VariableAnalysis varAnalysis = analyzer.getVariableAnalysis(obj);

        // translate variable type
        final JavaType type = translateType(obj.getType(), varAnalysis == null
            || !varAnalysis.isNotAssignedXZ(), varAnalysis == null
            || varAnalysis.isNeedStatefulString());

        // get containing class for variable
        final JavaRawClass cls = getClassForGlobal(obj, pkg);

        // ensure identifier is not a Java keyword
        id = fixID(id);

        // create the Java variable
        var = new JavaMemberVariable(id, type);
        var.addAnnotations(obj.getAnnotations());
        var.setVisibility(JavaVisibility.PUBLIC);
        var.addModifier(JavaVariableModifier.STATIC);
        if (needsClone(type))
        {
            var.addModifier(JavaVariableModifier.FINAL);
        }
        //addLengthAnnotation(var, type);
        cls.addMember(var);
        xlatObjMap.addJavaObject(obj, var);

        // set initializer expression if necessary
        var.setInitializer(getInitValue(type, false, false));

        // create wait_var event if necessary
        checkWaitVarMember(obj, var, varAnalysis, cls, null);

        logExit();
        return var;
    }

    protected JavaMemberVariable translateGlobalVariable(VeraGlobalVariable obj)
    {
        return translateGlobalVariable(obj, translateCompUnitOf(obj));
    }

    protected JavaMemberVariable translateMemberVariable(
        VeraMemberVariable obj,
        JavaRawClass cls)
    {
        // check whether variable has already been translated
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap
            .getJavaObject(obj);
        if (var != null) return var;

        final String clsID = cls.getName().getIdentifier();
        String id = obj.getName().getIdentifier();
        logEnter("Translating member variable: " + clsID + "." + id);

        try
        {
            // if this is a hand-translated class, just look up the field by name
            if (!(cls instanceof TranslatedClass))
            {
                try
                {
                    var = cls.getField(id, cls);
                }
                catch (FieldNotFoundException e)
                {
                    throw new RuntimeException(
                        "Manually translated field not found for '"
                            + obj.getName() + "' in '" + cls.getName() + "'");
                }
                xlatObjMap.addJavaObject(obj, var);
                return var;
            }

            // get the analysis for this variable
            final VariableAnalysis varAnalysis = analyzer
                .getVariableAnalysis(obj);

            if (varAnalysis != null && !varAnalysis.isReadAccess())
            {
                // suppress translation of unreferenced variables
                // NOTE: unread (but assigned) variables could technically be
                // suppressed as well, but that would involve translating their
                // assignment expressions into statements
                if (!varAnalysis.isWriteAccess())
                {
                    log("Warning: Suppressing translation of unreferenced field: "
                        + obj.getName());
                    return null;
                }
                else
                {
                    String msg = "Warning: Field '" + obj.getName()
                        + "' is never read";
                    if (varAnalysis.isWriteSideEffects())
                    {
                        msg += ", but assignment has side effects";
                    }
                    log(msg);
                }
            }

            // determine Java variable visibility
            JavaVisibility vis = translateMemberVisibility(obj.getVisibility());
            if (varAnalysis != null)
            {
                // determine strictest visibility based on usage analysis
                // NOTE: private fields accessed from forks (which are translated
                // to inner classes) are given default access to avoid synthetic
                // accessor
                final JavaVisibility genVis;
                if (!varAnalysis.isFieldAccessOutsideClass()
                    && !varAnalysis.isForkRead() && !varAnalysis.isForkWrite())
                {
                    genVis = JavaVisibility.PRIVATE;
                }
                else if (!varAnalysis.isFieldAccessOutsideDirectory())
                {
                    genVis = JavaVisibility.DEFAULT;
                }
                else if (!varAnalysis.isFieldAccessOutsideSubClass())
                {
                    genVis = JavaVisibility.PROTECTED;
                }
                else
                {
                    genVis = JavaVisibility.PUBLIC;
                }

                if (genVis != vis)
                {
                    log("Note: Changing visibility of field '" + obj.getName()
                        + "' from " + vis + " to " + genVis);
                    vis = genVis;
                }
            }

            // create the Java variable
            id = fixID(id);
            final VeraType veraType = obj.getType();
            final JavaType type = translateType(veraType, varAnalysis == null
                || !varAnalysis.isNotAssignedXZ(), varAnalysis == null
                || varAnalysis.isNeedStatefulString());
            var = new JavaMemberVariable(id, type);
            var.addAnnotations(obj.getAnnotations());
            var.setVisibility(vis);
            final Set veraModifiers = obj.getModifiers();
            final boolean isStatic = veraModifiers
                .contains(VeraVariableModifier.STATIC);
            if (isStatic)
            {
                var.addModifier(JavaVariableModifier.STATIC);
            }
            boolean needInit = true;
            if (needsClone(type))
            {
                // cloned variables are always final and initialized
                var.addModifier(JavaVariableModifier.FINAL);
            }
            else if (!isStatic && varAnalysis != null
                && !varAnalysis.isFieldUnassignedInCtor()
                && !varAnalysis.isReadUnassigned()
                && !classHasTransformedCtor(obj))
            {
                if (varAnalysis.isWaitVar())
                {
                    // the wait_var checking code does not support final fields
                    // since they are uninitialized before the assignment (i.e.
                    // they have no old value that can be read); such code is
                    // nonsensical and should be fixed in Vera
                    log("Warning: Field '"
                        + obj.getName()
                        + "' is never changed"
                        + " and should be marked final, but is a wait_var target");
                }
                else if (!varAnalysis.isFieldWriteOutsideCtor()
                    && !varAnalysis.isWriteAssigned())
                {
                    // instance variables assigned exactly once in ctor and not
                    // elsewhere, and not read when unassigned, can be marked as final
                    log("Note: Marking field '" + obj.getName() + "' as final");
                    var.addModifier(JavaVariableModifier.FINAL);
                    needInit = false;
                }
                else
                {
                    // variable is not final, but does not need to be
                    // initialized, since it is not read when unassigned and is
                    // assigned in the ctor
                    needInit = false;
                }
            }
            if (obj.hasModifier(VeraVariableModifier.RANDC))
            {
                // add Randc annotation
                var.addAnnotation(new JavaAnnotation(types.randcType));
                addLengthAnnotation(var, type);
            }
            else if (obj.hasModifier(VeraVariableModifier.RAND))
            {
                // add Rand annotation
                var.addAnnotation(new JavaAnnotation(types.randType));
                addLengthAnnotation(var, type);
            }
            // TODO: packing modifiers
            cls.addMember(var);
            xlatObjMap.addJavaObject(obj, var);

            // translate initializer expression
            VeraExpression veraInitExpr = obj.getInitializer();
            if (veraInitExpr != null)
            {
                ConvertedExpression convExpr = translateExpr(veraInitExpr,
                    null, cls, null, null, type, type);
                convertInitializer(convExpr, type);
                JavaExpression initExpr = convExpr.toMemberInitExpr(cls, var);
                var.setInitializer(initExpr);
            }
            else if (needInit)
            {
                JavaExpression initExpr = getInitValue(type, false, false);
                var.setInitializer(initExpr);
            }

            // TODO: random size
            assert (obj.getRandomSize() == null);

            // create wait_var event if necessary
            checkWaitVarMember(obj, var, varAnalysis, cls, null);

            return var;
        }
        finally
        {
            logExit();
        }
    }

    private void addLengthAnnotation(JavaVariable var, JavaType type)
    {
        if (type instanceof JoveBitVectorType)
        {
            // add Length annotation
            final JavaAnnotation ann = new JavaAnnotation(types.lengthType);
            final int len = ((JoveBitVectorType) type).getSize();
            ann.setElementValue("value", new JavaIntLiteral(schema, len));
            var.addAnnotation(ann);
        }
    }

    private boolean classHasTransformedCtor(VeraClassMember member)
    {
        VeraUserClass cls = (VeraUserClass) member.getStructuredType();
        ClassAnalysis analysis = analyzer.getClassAnalysis(cls);
        return (analysis.needDefaultCtor() && !analysis.hasDefaultCtor())
            || analysis.transformSuperCall();
    }

    protected void checkWaitVarMember(
        VeraVariable veraVar,
        JavaMemberVariable javaVar,
        VariableAnalysis varAnalysis,
        JavaRawAbstractClass cls,
        JavaClassMember assocMember)
    {
        if (varAnalysis != null && varAnalysis.isWaitVar())
        {
            final VeraType veraType = veraVar.getType();
            final JavaType eventType = getWaitVarEventType(veraType);
            final JavaMemberVariable eventVar = VarBuilder.createMemberVar(cls,
                getEventVarID(veraVar), eventType);
            JavaVisibility vis = javaVar.getVisibility();
            eventVar.setVisibility(vis);
            if (javaVar.hasModifier(JavaVariableModifier.STATIC))
            {
                eventVar.addModifier(JavaVariableModifier.STATIC);
            }
            eventVar.addModifier(JavaVariableModifier.FINAL);
            eventVar.setInitializer(getInitValue(eventType, true, false));
            if (assocMember != null)
            {
                cls.addMemberBefore(eventVar, assocMember);
            }
            else
            {
                cls.addMember(eventVar);
            }
            xlatObjMap.addWaitVarEvent(javaVar, eventVar);
        }
    }

    private static JavaVisibility translateMemberVisibility(Visibility veraVis)
    {
        JavaVisibility vis;
        if (veraVis == VeraVisibility.PUBLIC)
        {
            vis = JavaVisibility.PUBLIC;
        }
        else if (veraVis == VeraVisibility.PROTECTED)
        {
            vis = JavaVisibility.PROTECTED;
        }
        else
        {
            assert (veraVis == VeraVisibility.LOCAL);
            vis = JavaVisibility.PRIVATE;
        }
        return vis;
    }

    protected final JavaType getWaitVarEventType(VeraType veraType)
    {
        JavaType eventType = types.junoEventType;
        if (veraType instanceof VeraArrayType)
        {
            eventType = TypeTranslator.translateArrayType(this,
                (VeraArrayType) veraType, eventType);
        }
        return eventType;
    }

    protected final JavaExpression getWaitVarEventRef(JavaExpression varRefExpr)
    {
        JavaExpression result = null;
        if (hasMemberWaitVarEvent(varRefExpr))
        {
            result = getMemberWaitVarEvent(varRefExpr);
        }
        else if (varRefExpr instanceof JavaVariableReference)
        {
            JavaVariableReference varRef = (JavaVariableReference) varRefExpr;
            JavaVariable var = varRef.getVariable();
            JavaVariable eventVar = xlatObjMap.getWaitVarEvent(var);
            if (eventVar != null)
            {
                result = new JavaVariableReference(eventVar);
            }
        }
        else if (varRefExpr instanceof JavaMemberAccess)
        {
            JavaMemberAccess ma = (JavaMemberAccess) varRefExpr;
            StructuredTypeMember member = ma.getMember();
            assert (member instanceof JavaMemberVariable);
            JavaMemberVariable var = (JavaMemberVariable) member;
            JavaVariable eventVar = xlatObjMap.getWaitVarEvent(var);
            if (eventVar != null)
            {
                assert (eventVar instanceof JavaMemberVariable);
                JavaExpression object = ma.getObject();
                result = new JavaMemberAccess(object,
                    (JavaMemberVariable) eventVar);
            }
        }
        else if (varRefExpr instanceof JavaArrayAccess)
        {
            JavaArrayAccess aa = (JavaArrayAccess) varRefExpr;
            JavaExpression arrayExpr = aa.getArray();
            JavaExpression arrayEventVar = getWaitVarEventRef(arrayExpr);
            if (arrayEventVar != null)
            {
                if (arrayEventVar.getResultType() instanceof JavaArrayType)
                {
                    // map wait_var on array elem to event array elem
                    JavaArrayAccess eventVarAccess = new JavaArrayAccess(
                        arrayEventVar);
                    Iterator iter = aa.getIndices().iterator();
                    while (iter.hasNext())
                    {
                        eventVarAccess.addIndex((JavaExpression) iter.next());
                    }
                    result = eventVarAccess;
                }
                else
                {
                    // map wait_var on ref holder to event var ref
                    result = arrayEventVar;
                }
            }
        }
        return result;
    }

    protected final boolean hasMemberWaitVarEvent(JavaExpression varRefExpr)
    {
        return varRefExpr.getResultType() == types.junoStringType;
    }

    protected final JavaExpression getMemberWaitVarEvent(
        JavaExpression varRefExpr)
    {
        return ExpressionBuilder.memberCall(varRefExpr, "getChangeEvent");
    }

    protected JavaEnum translateEnum(
        VeraEnumeration obj,
        JavaPackage pkg,
        JavaRawAbstractClass outerClass)
    {
        // check whether enumeration has already been translated
        JavaEnum cls = (JavaEnum) xlatObjMap.getJavaObject(obj);
        if (cls != null) return cls;

        final String id = obj.getName().getIdentifier();
        if (outerClass == null)
        {
            logEnter("Translating enumeration: " + id);
        }
        else
        {
            logEnter("Translating member enumeration: " + id);
        }

        // create the Java class
        cls = new JavaEnum(schema, fixID(id), pkg);
        cls.addAnnotations(obj.getAnnotations());
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addBaseInterface(types.junoEnumType.parameterize(cls));
        xlatObjMap.addJavaObject(obj, cls);

        // handle class-scope enumeration
        if (outerClass != null)
        {
            outerClass.addMember(cls);
        }
        else
        {
            pkg.addMember(cls);
        }

        // define value field
        final JavaMemberVariable valueVar = cls.newField("value",
            schema.integerWrapperType);
        valueVar.setVisibility(JavaVisibility.PRIVATE);
        valueVar.addModifier(JavaVariableModifier.FINAL);
        final JavaVariableReference valueVarRef = new JavaVariableReference(
            valueVar);

        // define value ctor
        {
            final JavaConstructor ctor = cls.newConstructor();
            final JavaFunctionType ctorType = ctor.getType();
            final JavaFunctionArgument valueArg = new JavaFunctionArgument(
                "value", schema.integerWrapperType);
            ctorType.addArgument(valueArg);
            final JavaBlock ctorBody = new JavaBlock(schema);
            final JavaMemberAccess thisValueAccess = new JavaMemberAccess(
                new JavaThisReference(cls), valueVar);
            ctorBody.addMember(new JavaExpressionStatement(new JavaAssign(
                schema, thisValueAccess, new JavaVariableReference(valueArg))));
            ctor.setBody(ctorBody);
        }

        // create the UNDEFINED enumeration constant
        final JavaMemberVariable undefinedVar = cls.newValue("UNDEFINED",
            false, new JavaNullLiteral(schema));
        undefinedVar.addAnnotation(new JavaAnnotation(types.randExcludeType));

        // translate the enumeration elements
        final List<VeraEnumerationElement> elements = obj.getMembers();
        final int elemCount = elements.size();
        assert (elemCount > 0);
        int largestValue = 1;
        boolean negValue = false;
        for (final VeraEnumerationElement elem : elements)
        {
            // create enumeration constant
            final String elemID = elem.getName().getIdentifier();
            int intValue = elem.getValue();
            final JavaMemberVariable var = cls.newValue(fixID(elemID), false,
                new JavaIntLiteral(schema, intValue));
            var.addAnnotations(elem.getAnnotations());
            xlatObjMap.addJavaObject(elem, var);

            // remember largest value and whether any values are negative
            if (intValue < 0)
            {
                negValue = true;
                intValue = -intValue;
            }
            if (intValue > largestValue)
            {
                largestValue = intValue;
            }
        }

        // implement next()/previous() methods
        JavaFunction advanceFunc = types.junoEnumUtilType.getMethod("advance",
            cls, schema.intType);
        JavaFunctionReference advanceRef = new JavaFunctionReference(
            advanceFunc);

        // implement next()
        {
            JavaFunction func = cls.newMethod("next", cls);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            JavaFunctionInvocation advanceCall = new JavaFunctionInvocation(
                advanceRef);
            advanceCall.addArgument(new JavaThisReference(cls));
            advanceCall.addArgument(new JavaIntLiteral(schema, 1));
            body.addMember(new JavaReturnStatement(schema, advanceCall));
        }

        // implement next(int)
        {
            JavaFunction func = cls.newMethod("next", cls);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaFunctionType funcType = func.getType();
            JavaFunctionArgument arg = funcType.newArgument("count",
                schema.intType);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            JavaFunctionInvocation advanceCall = new JavaFunctionInvocation(
                advanceRef);
            advanceCall.addArgument(new JavaThisReference(cls));
            advanceCall.addArgument(new JavaVariableReference(arg));
            body.addMember(new JavaReturnStatement(schema, advanceCall));
        }

        // implement previous()
        {
            JavaFunction func = cls.newMethod("previous", cls);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            JavaFunctionInvocation advanceCall = new JavaFunctionInvocation(
                advanceRef);
            advanceCall.addArgument(new JavaThisReference(cls));
            advanceCall.addArgument(new JavaIntLiteral(schema, -1));
            body.addMember(new JavaReturnStatement(schema, advanceCall));
        }

        // implement previous(int)
        {
            JavaFunction func = cls.newMethod("previous", cls);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaFunctionType funcType = func.getType();
            JavaFunctionArgument arg = funcType.newArgument("count",
                schema.intType);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            JavaFunctionInvocation advanceCall = new JavaFunctionInvocation(
                advanceRef);
            advanceCall.addArgument(new JavaThisReference(cls));
            advanceCall.addArgument(new JavaUnaryMinus(schema,
                new JavaVariableReference(arg)));
            body.addMember(new JavaReturnStatement(schema, advanceCall));
        }

        // implement forValue()/getForValue()
        for (int i = 0; i < 2; ++i)
        {
            String funcID = i == 0 ? "forValue" : "getForValue";
            JavaFunction func = cls.newMethod(funcID, cls);
            func.setVisibility(JavaVisibility.PUBLIC);
            if (i == 0) func.addModifier(JavaFunctionModifier.STATIC);

            JavaFunctionType funcType = func.getType();
            JavaFunctionArgument valueArg = funcType.newArgument("value",
                schema.integerWrapperType);
            JavaFunctionArgument checkedArg = funcType.newArgument("checked",
                schema.booleanType);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            JavaFunction forValueFunc = types.junoEnumUtilType.getMethod(
                "forValue", schema.getClassType(), schema.integerWrapperType,
                schema.booleanType);
            JavaFunctionReference forValueRef = new JavaFunctionReference(
                forValueFunc);
            JavaFunctionInvocation forValueCall = new JavaFunctionInvocation(
                forValueRef);
            forValueCall.addArgument(new JavaTypeLiteral(cls));
            forValueCall.addArgument(new JavaVariableReference(valueArg));
            forValueCall.addArgument(new JavaVariableReference(checkedArg));
            body.addMember(new JavaReturnStatement(schema, forValueCall));
        }

        // implement getPackedSize()
        {
            JavaFunction func = cls.newMethod("getPackedSize", schema.intType);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            int packedSize = MathUtils.log2(largestValue) + 1;
            if (negValue) ++packedSize;
            body.addMember(new JavaReturnStatement(schema, new JavaIntLiteral(
                schema, packedSize)));
        }

        // implement isSigned()
        {
            JavaFunction func = cls.newMethod("isSigned", schema.booleanType);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            body.addMember(new JavaReturnStatement(schema,
                new JavaBooleanLiteral(schema, negValue)));
        }

        // implement isDefined()
        {
            JavaFunction func = cls.newMethod("isDefined", schema.booleanType);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            body.addMember(new JavaReturnStatement(schema, new JavaNotEqual(
                schema, valueVarRef, new JavaNullLiteral(schema))));
        }

        // implement toInt()
        {
            JavaFunction func = cls.newMethod("toInt", schema.intType);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            body.addMember(new JavaReturnStatement(schema, valueVarRef));
        }

        // implement toInteger()
        {
            JavaFunction func = cls.newMethod("toInteger",
                schema.integerWrapperType);
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            body.addMember(new JavaReturnStatement(schema, valueVarRef));
        }

        // implement toString()
        {
            JavaFunction func = cls.newMethod("toString", schema
                .getStringType());
            func.setVisibility(JavaVisibility.PUBLIC);

            JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            body.addMember(new JavaReturnStatement(schema, ExpressionBuilder
                .staticCall(types.junoEnumUtilType, "toString",
                    new JavaThisReference(cls))));
        }

        logExit();
        return cls;
    }

    protected JavaEnum translateEnum(VeraEnumeration obj)
    {
        return translateEnum(obj, translateCompUnitOf(obj),
            translateClassOf(obj));
    }

    protected JavaMemberVariable translateEnumElement(VeraEnumerationElement obj)
    {
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap
            .getJavaObject(obj);
        if (var == null)
        {
            final VeraEnumeration veraEnum = obj.getEnumeration();
            final JavaEnum enumClass = translateEnum(veraEnum);
            final String elemID = obj.getName().getIdentifier();
            var = enumClass.getField(elemID);
        }
        return var;
    }

    protected JavaRawClass translatePort(VeraPortType obj, JavaPackage pkg)
    {
        // check whether port has already been translated
        JavaRawClass cls = (JavaRawClass) xlatObjMap.getJavaObject(obj);
        if (cls != null) return cls;

        String id = obj.getName().getIdentifier();
        logEnter("Translating port: " + id);

        // translate Vera port to Ifgen port
        id = elaborateID(id, "Port");
        final IfgenPackage ifPkg = ifSchema.getPackage(pkg.getName()
            .getCanonicalName(), true);
        final IfgenName ifName = new IfgenName(id, IfgenNameKind.TYPE, ifPkg);
        final IfgenPort ifPort = new IfgenPort(ifSchema, ifName);
        ifPort.addAnnotations(obj.getAnnotations());
        ifPkg.addMember(ifPort);
        xlatObjMap.addIfgenObject(obj, ifPort);

        // translate port signals
        for (final VeraPortSignal signal : obj.getMembers())
        {
            final String signalID = signal.getName().getIdentifier();
            final IfgenPortSignal ifSignal = new IfgenPortSignal(ifPort,
                fixID(signalID), IfgenDirection.INOUT, false);
            ifSignal.addAnnotations(signal.getAnnotations());
            ifPort.addMember(ifSignal);
            xlatObjMap.addIfgenObject(signal, ifSignal);
        }

        // translate Ifgen port to Java class
        cls = ifgenXlat.translatePort(ifPort);
        xlatObjMap.addJavaObject(obj, cls);

        // put translated signals in xlatObjMap
        for (final VeraPortSignal signal : obj.getMembers())
        {
            final IfgenPortSignal ifSignal = (IfgenPortSignal) xlatObjMap
                .getIfgenObject(signal);
            final JavaMemberVariable var = ifgenXlat
                .translatePortSignal(ifSignal);
            xlatObjMap.addJavaObject(signal, var);
        }

        logExit();
        return cls;
    }

    protected JavaRawClass translatePort(VeraPortType obj)
    {
        return translatePort(obj, translateCompUnitOf(obj));
    }

    protected JavaRawClass translateInterface(
        VeraInterfaceType obj,
        JavaPackage pkg)
    {
        // check whether interface has already been translated
        JavaRawClass cls = (JavaRawClass) xlatObjMap.getJavaObject(obj);
        if (cls != null) return cls;

        String id = obj.getName().getIdentifier();
        logEnter("Translating interface: " + id);
        // translate Vera interface to Ifgen interface
        id = elaborateID(id, "Interface", "intf");
        final IfgenPackage ifPkg = ifSchema.getPackage(pkg.getName()
            .getCanonicalName(), true);
        final IfgenName ifName = new IfgenName(id, IfgenNameKind.TYPE, ifPkg);
        final IfgenInterface ifIntf = new IfgenInterface(ifSchema, ifName);
        ifIntf.addAnnotations(obj.getAnnotations());
        ifPkg.addMember(ifIntf);
        xlatObjMap.addIfgenObject(obj, ifIntf);

        // translate interface signals
        // TODO: extrapolate default module prefix
        HashMap<IfgenSampleDef, Integer> sampleUsage = new HashMap<IfgenSampleDef, Integer>();
        HashMap<IfgenDriveDef, Integer> driveUsage = new HashMap<IfgenDriveDef, Integer>();
        for (final VeraInterfaceSignal signal : obj.getMembers())
        {
            final String signalID = signal.getName().getIdentifier();
            final VeraSignalKind kind = signal.getKind();
            final IfgenSignalType ifType;
            if (kind == VeraSignalKind.CLOCK)
            {
                ifType = IfgenSignalType.CLOCK;
            }
            else
            {
                assert (kind == VeraSignalKind.NORMAL);
                final VeraSignalDirection dir = signal.getDirection();
                switch (dir)
                {
                case INPUT:
                    ifType = IfgenSignalType.INPUT;
                    break;
                case OUTPUT:
                    ifType = IfgenSignalType.OUTPUT;
                    break;
                case INOUT:
                    ifType = IfgenSignalType.INOUT;
                    break;
                default:
                    throw new AssertionError("Unknown direction: " + dir);
                }
            }
            final int width = signal.getWidth();
            final IfgenInterfaceSignal ifSignal = new IfgenInterfaceSignal(
                ifIntf, fixID(signalID), ifType, width);
            ifSignal.addAnnotations(signal.getAnnotations());
            if (ifType.isInput() && ifType != IfgenSignalType.CLOCK)
            {
                final IfgenEdge edge = getIfgenEdge(signal.getSampleEdges());
                final int skew = signal.getSampleSkew();
                final IfgenSampleDef sampleDef = new IfgenSampleDef(ifSchema,
                    edge, skew);
                ifSignal.setSample(sampleDef);
                incUsage(sampleUsage, sampleDef);
                ifSignal.setSampleDepth(signal.getSampleDepth() + 1);
            }
            if (ifType.isOutput())
            {
                final IfgenEdge edge = getIfgenEdge(signal.getDriveEdges());
                final int skew = signal.getDriveSkew();
                final IfgenDriveDef driveDef = new IfgenDriveDef(ifSchema,
                    edge, skew);
                ifSignal.setDrive(driveDef);
                incUsage(driveUsage, driveDef);
            }
            final String node = signal.getHDLNode();
            if (node != null)
            {
                ifSignal.setHDLNode(new IfgenHDLSignalRef(
                    new IfgenStringLiteral(ifSchema, node)));
            }
            ifIntf.addMember(ifSignal);
            xlatObjMap.addIfgenObject(signal, ifSignal);
        }

        // insert default sample/drive specification
        final IfgenSampleDef topSample = getTopUsage(sampleUsage);
        if (topSample != null)
        {
            ifIntf.addMember(0, topSample);
        }
        final IfgenDriveDef topDrive = getTopUsage(driveUsage);
        if (topDrive != null)
        {
            ifIntf.addMember(0, topDrive);
        }

        // translate Ifgen interface to Java class
        cls = ifgenXlat.translateInterface(ifIntf);
        xlatObjMap.addJavaObject(obj, cls);

        // put translated signals in xlatObjMap
        for (final VeraInterfaceSignal signal : obj.getMembers())
        {
            final IfgenInterfaceSignal ifSignal = (IfgenInterfaceSignal) xlatObjMap
                .getIfgenObject(signal);
            final JavaMemberVariable var = ifgenXlat
                .translateInterfaceSignal(ifSignal);
            xlatObjMap.addJavaObject(signal, var);
        }

        logExit();
        return cls;
    }

    private IfgenEdge getIfgenEdge(EdgeSet edges)
    {
        final IfgenEdge edge;
        if (edges.contains(EdgeSet.ANYEDGE))
        {
            edge = IfgenEdge.ANYEDGE;
        }
        else if (edges.contains(EdgeSet.POSEDGE))
        {
            edge = IfgenEdge.POSEDGE;
        }
        else if (edges.contains(EdgeSet.NEGEDGE))
        {
            edge = IfgenEdge.NEGEDGE;
        }
        else
        {
            throw new AssertionError("Unexpected edge set: " + edges);
        }
        return edge;
    }

    protected JavaRawClass translateInterface(VeraInterfaceType obj)
    {
        return translateInterface(obj, translateCompUnitOf(obj));
    }

    protected JavaMemberVariable translateInterfaceSignal(
        VeraInterfaceSignal obj)
    {
        translateInterface(obj.getStructuredType());
        final JavaMemberVariable var = (JavaMemberVariable) xlatObjMap
            .getJavaObject(obj);
        assert (var != null);
        return var;
    }

    private IfgenInterfaceSignal getIfgenInterfaceSignal(VeraInterfaceSignal obj)
    {
        translateInterface(obj.getStructuredType());
        final IfgenInterfaceSignal signal = (IfgenInterfaceSignal) xlatObjMap
            .getIfgenObject(obj);
        assert (signal != null);
        return signal;
    }

    protected JavaMemberVariable translateBind(
        VeraBindVariable obj,
        JavaPackage pkg)
    {
        // check whether bind has already been translated
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap
            .getJavaObject(obj);
        if (var != null) return var;

        String id = obj.getName().getIdentifier();
        logEnter("Translating bind: " + id);

        // get Ifgen port
        final VeraPortType port = obj.getPort();
        translatePort(port);
        final IfgenPort ifPort = (IfgenPort) xlatObjMap.getIfgenObject(port);

        // translate Vera bind to Ifgen bind
        id = elaborateID(id, "Bind");
        final IfgenPackage ifPkg = ifSchema.getPackage(pkg.getName()
            .getCanonicalName(), true);
        final IfgenName ifName = new IfgenName(id, IfgenNameKind.EXPRESSION,
            ifPkg);
        final IfgenBind ifBind = new IfgenBind(ifSchema, ifName, ifPort);
        ifBind.addAnnotations(obj.getAnnotations());
        ifPkg.addMember(ifBind);
        xlatObjMap.addIfgenObject(obj, ifBind);

        // translate bind members
        HashMap<IfgenInterface, Integer> intfUsage = new HashMap<IfgenInterface, Integer>();
        for (final VeraBindMember member : obj.getMembers())
        {
            final VeraPortSignal portSignal = member.getPortSignal();
            final IfgenPortSignal ifPortSignal = (IfgenPortSignal) xlatObjMap
                .getIfgenObject(portSignal);
            final IfgenSignalRef ifSignalRef;
            final VeraExpression veraExpr = member.getInterfaceExpr();
            if (veraExpr instanceof VeraVoidLiteral)
            {
                continue;
            }
            else if (veraExpr instanceof VeraConcatenation)
            {
                final VeraConcatenation veraConcat = (VeraConcatenation) veraExpr;
                final IfgenConcatSignalRef ifConcat = new IfgenConcatSignalRef();
                for (final VeraExpression veraElem : veraConcat.getOperands())
                {
                    IfgenSignalRef elemRef = translateSignalRef(veraElem);
                    IfgenInterface intf = getSignalRefIntf(elemRef);
                    incUsage(intfUsage, intf);
                    ifConcat.addMember(elemRef);
                }
                ifSignalRef = ifConcat;
            }
            else
            {
                ifSignalRef = translateSignalRef(veraExpr);
                IfgenInterface intf = getSignalRefIntf(ifSignalRef);
                incUsage(intfUsage, intf);
            }
            final IfgenBindSignal ifBindSignal = new IfgenBindSignal(ifBind,
                ifPortSignal, ifSignalRef);
            ifBindSignal.addAnnotations(member.getAnnotations());
            ifBind.addMember(ifBindSignal);
        }

        // insert default interface specification
        final IfgenInterface topIntf = getTopUsage(intfUsage);
        if (topIntf != null)
        {
            ifBind.addMember(0, new IfgenInterfaceDef(topIntf));
        }

        // translate Ifgen bind to Java member variable
        try
        {
            var = ifgenXlat.translateBind(ifBind);
        }
        catch (IfgenTranslatorException e)
        {
            throw new RuntimeException(e);
        }
        xlatObjMap.addJavaObject(obj, var);

        logExit();
        return var;
    }

    private static String elaborateID(
        String id,
        String suffix,
        String... altPatterns)
    {
        final String lcID = id.toLowerCase();
        final String lcSuffix = suffix.toLowerCase();
        if (!lcID.contains(lcSuffix) && !containsAny(lcID, altPatterns))
        {
            if (id.indexOf('_') < 0
                && Pattern.compile("[A-Z]").matcher(id).find())
            {
                id = id + suffix;
            }
            else
            {
                id = id + "_" + lcSuffix;
            }
        }
        else
        {
            id = fixID(id);
        }
        return id;
    }

    private static boolean containsAny(String s, String[] subs)
    {
        for (String sub : subs)
        {
            if (s.contains(sub)) return true;
        }
        return false;
    }

    private <K> void incUsage(final Map<K, Integer> usageMap, K key)
    {
        Integer oldCount = usageMap.get(key);
        int newCount = (oldCount != null) ? oldCount + 1 : 1;
        usageMap.put(key, newCount);
    }

    private <K> K getTopUsage(final Map<K, Integer> usageMap)
    {
        int highCount = 0;
        K highKey = null;
        for (Map.Entry<K, Integer> e : usageMap.entrySet())
        {
            int count = e.getValue();
            if (count > highCount)
            {
                highCount = count;
                highKey = e.getKey();
            }
        }
        return highKey;
    }

    private IfgenSignalRef translateSignalRef(VeraExpression expr)
    {
        // extract signal reference and possibly bit range from expression
        final VeraSignalReference veraSignalRef;
        final VeraExpression veraHighBit, veraLowBit;
        if (expr instanceof VeraBitSliceAccess)
        {
            VeraBitSliceAccess bsa = (VeraBitSliceAccess) expr;
            veraSignalRef = (VeraSignalReference) bsa.getArray();
            VeraRange range = bsa.getRange();
            veraHighBit = range.getFrom();
            veraLowBit = range.getTo();
        }
        else if (expr instanceof VeraArrayAccess)
        {
            VeraArrayAccess aa = (VeraArrayAccess) expr;
            veraSignalRef = (VeraSignalReference) aa.getArray();
            List indices = aa.getIndices();
            assert (indices.size() == 1);
            veraHighBit = (VeraExpression) indices.get(0);
            veraLowBit = null;
        }
        else if (expr instanceof VeraSignalReference)
        {
            veraSignalRef = (VeraSignalReference) expr;
            veraHighBit = null;
            veraLowBit = null;
        }
        else
        {
            throw new RuntimeException("Unexpected expression type in bind: "
                + expr);
        }

        // translate the signal reference
        VeraInterfaceSignal veraSignal = veraSignalRef.getSignal();
        IfgenInterfaceSignal ifSignal = getIfgenInterfaceSignal(veraSignal);
        IfgenSignalRef signalRef = new IfgenInterfaceSignalRef(ifSignal);

        // check for partial signal reference
        if (veraHighBit != null)
        {
            int highBit = evalIntExpr(veraHighBit);
            int lowBit = (veraLowBit != null) ? evalIntExpr(veraLowBit)
                : highBit;
            signalRef = new IfgenSliceSignalRef(signalRef, highBit, lowBit);
        }

        return signalRef;
    }

    private IfgenInterface getSignalRefIntf(IfgenSignalRef ref)
    {
        if (ref instanceof IfgenSliceSignalRef)
        {
            IfgenSliceSignalRef sliceRef = (IfgenSliceSignalRef) ref;
            ref = sliceRef.getSignal();
        }
        if (ref instanceof IfgenInterfaceSignalRef)
        {
            IfgenInterfaceSignalRef intfRef = (IfgenInterfaceSignalRef) ref;
            return intfRef.getSignal().getInterface();
        }
        throw new AssertionError("Unexpected signal reference type: "
            + ref.getClass());
    }

    private int evalIntExpr(VeraExpression expr)
    {
        final Integer result = VeraExpression
            .toInteger(expr.evaluateConstant());
        if (result == null)
        {
            throw new RuntimeException("Integer expression expected: " + expr);
        }
        return result.intValue();
    }

    protected JavaMemberVariable translateBind(VeraBindVariable obj)
    {
        return translateBind(obj, translateCompUnitOf(obj));
    }

    protected JavaType translateType(
        VeraType obj,
        boolean allowXZ,
        boolean statefulString)
    {
        TypeTranslator xlat = new TypeTranslator(this, allowXZ, statefulString);
        obj.accept(xlat);
        return xlat.getType();
    }

    protected JavaType translateType(VeraType obj)
    {
        return translateType(obj, true, true);
    }

    protected JavaExpression getInitValue(
        JavaType type,
        boolean required,
        boolean needFactory)
    {
        JavaExpression expr = null;
        if (type instanceof JavaStructuredType)
        {
            if (type == schema.bitType)
            {
                expr = exprConv.getBitExpr(Bit.X);
            }
            else if (type instanceof JoveBitVectorType)
            {
                int size = ((JoveBitVectorType) type).getSize();
                expr = exprConv.getBitVectorExpr(size);
            }
            else if (type == types.junoStringType)
            {
                if (needFactory)
                {
                    expr = new JavaVariableReference(
                        types.junoStringValueFactoryType.getField("INSTANCE"));
                }
                else
                {
                    expr = ExpressionBuilder.newInstance(types.junoStringType);
                }
            }
            else if (type == types.junoEventType)
            {
                if (needFactory)
                {
                    expr = new JavaVariableReference(
                        types.junoEventValueFactoryType.getField("INSTANCE"));
                }
                else
                {
                    expr = ExpressionBuilder.newInstance(types.junoEventType);
                }
            }
            else if (exprConv.isEnum(type))
            {
                JavaAbstractClass enumCls = (JavaAbstractClass) type;
                expr = new JavaVariableReference(enumCls.getField("UNDEFINED"));
            }
            else if (type instanceof JoveAssocArrayType)
            {
                JoveAssocArrayType assocType = (JoveAssocArrayType) type;
                JavaExpression initExpr = getInitValue(assocType
                    .getElementType(), true, true);
                if (initExpr.getResultType() instanceof JavaNullType)
                {
                    // avoid ambiguous method invocation
                    initExpr = new JavaCastExpression(types.objectType,
                        initExpr);
                }
                expr = ExpressionBuilder.newInstance(assocType, initExpr);
            }
            else if (type instanceof JoveFixedArrayType)
            {
                final JoveFixedArrayType arrayType = (JoveFixedArrayType) type;
                final JavaArrayCreation newExpr = new JavaArrayCreation(
                    arrayType);
                final int[] dimensions = arrayType.getDimensions();
                for (int i = 0; i < dimensions.length; ++i)
                {
                    JavaIntLiteral dimExpr = new JavaIntLiteral(schema,
                        dimensions[i]);
                    newExpr.addDimension(dimExpr);
                }

                final JavaType elemType = arrayType.getElementType();
                JavaExpression elemInitExpr = getInitValue(elemType, false,
                    true);
                if (elemInitExpr != null)
                {
                    if (elemInitExpr.getResultType() instanceof JavaNullType)
                    {
                        // avoid ambiguous method invocation
                        elemInitExpr = new JavaCastExpression(types.objectType,
                            elemInitExpr);
                    }
                    final JavaExpression[] args = { newExpr, elemInitExpr,
                        new JavaIntLiteral(schema, 0) };
                    expr = ExpressionBuilder.checkDowncast(ExpressionBuilder
                        .staticCall(types.junoType, "initArray", args, null),
                        arrayType);
                }
                else
                {
                    expr = newExpr;
                }
            }
            else if (required)
            {
                expr = new JavaNullLiteral(schema);
            }
        }
        else if (required)
        {
            if (type instanceof JavaDoubleType)
            {
                expr = new JavaDoubleLiteral(schema, 0);
            }
            else if (type instanceof JavaFPType)
            {
                expr = new JavaFloatLiteral(schema, 0);
            }
            else if (type instanceof JavaLongType)
            {
                expr = new JavaLongLiteral(schema, 0);
            }
            else if (type instanceof JavaIntegralType)
            {
                expr = new JavaIntLiteral(schema, 0);
            }
            else if (type instanceof JavaBooleanType)
            {
                expr = new JavaBooleanLiteral(schema, false);
            }
            else
            {
                expr = new JavaNullLiteral(schema);
            }
        }
        return expr;
    }

    protected JavaFunctor translateFunction(VeraFunction obj)
    {
        // check whether function has already been translated
        JavaFunctor func = (JavaFunctor) xlatObjMap.getJavaObject(obj);
        if (func != null) return func;

        // delegate translation based on function kind
        if (obj instanceof VeraGlobalFunction)
        {
            func = translateGlobalFunction((VeraGlobalFunction) obj);
        }
        else
        {
            assert (obj instanceof VeraMemberFunction);
            VeraMemberFunction memberFunc = (VeraMemberFunction) obj;
            func = translateMemberFunctionOrCtor(memberFunc,
                translateClassOf(memberFunc));
        }
        return func;
    }

    protected JavaVariable translateVariable(VeraVariable obj)
    {
        // check whether variable has already been translated
        // (function arguments and local variables are found here)
        JavaVariable var = (JavaVariable) xlatObjMap.getJavaObject(obj);
        if (var != null) return var;

        // delegate translation based on variable kind
        if (obj instanceof VeraGlobalVariable)
        {
            var = translateGlobalVariable((VeraGlobalVariable) obj);
        }
        else if (obj instanceof VeraMemberVariable)
        {
            VeraMemberVariable memberVar = (VeraMemberVariable) obj;
            var = translateMemberVariable(memberVar,
                translateClassOf(memberVar));
        }
        else if (obj instanceof VeraBindVariable)
        {
            var = translateBind((VeraBindVariable) obj);
        }
        return var;
    }

    protected JavaStructuredTypeMember translateMember(
        VeraStructuredTypeMember obj)
    {
        // check whether member has already been translated
        JavaStructuredTypeMember member = (JavaStructuredTypeMember) xlatObjMap
            .getJavaObject(obj);
        if (member != null) return member;

        // delegate translation based on member kind
        if (obj instanceof VeraMemberFunction)
        {
            VeraMemberFunction memberFunc = (VeraMemberFunction) obj;
            member = translateMemberFunction(memberFunc,
                translateClassOf(memberFunc));
        }
        else if (obj instanceof VeraMemberVariable)
        {
            VeraMemberVariable memberVar = (VeraMemberVariable) obj;
            member = translateMemberVariable(memberVar,
                translateClassOf(memberVar));
        }
        else
        {
            assert (obj instanceof VeraPortSignal);
            VeraPortSignal portSignal = (VeraPortSignal) obj;
            VeraPortType port = portSignal.getStructuredType();
            translatePort(port);
            member = (JavaMemberVariable) xlatObjMap.getJavaObject(obj);
            assert (member != null);
        }
        return member;
    }

    protected final ConvertedExpression translateExpr(
        VeraExpression veraExpr,
        TempBlockScope tempScope,
        JavaStructuredType containingType,
        VarInfoMap varInfoMap,
        JavaLocalVariable returnVar,
        JavaType promoteType,
        JavaType desiredResultType,
        boolean sampleAsync)
    {
        if (tempScope == null) tempScope = new TempBlockScope();
        ConvertedExpression result = new ConvertedExpression(schema, tempScope);
        final VeraDefineReference<VeraExpressionDefine> defineRef = veraExpr
            .getDefineRef();
        if (defineRef != null)
        {
            final VeraExpressionDefine define = defineRef.getDefine();
            final List<VeraExpression> veraArgExprs = defineRef.getArguments();
            List<ConvertedExpression> convExprs = null;
            List<JavaExpression> javaArgExprs = null;
            if (!veraArgExprs.isEmpty())
            {
                convExprs = new ArrayList<ConvertedExpression>(veraArgExprs
                    .size());
                javaArgExprs = new ArrayList<JavaExpression>(veraArgExprs
                    .size());
                for (final VeraExpression veraArgExpr : veraArgExprs)
                {
                    // TODO: handle define argument with side effects
                    final ConvertedExpression convExpr = translateExpr(
                        veraArgExpr, tempScope, containingType, varInfoMap,
                        returnVar, promoteType, desiredResultType, true);
                    convExprs.add(convExpr);
                    javaArgExprs.add(convExpr.getResultExpr());
                }
            }
            final JavaClassMember member = translateExpressionDefine(define,
                javaArgExprs);
            if (member != null)
            {
                final JavaExpression resultExpr;
                if (member instanceof JavaMemberVariable)
                {
                    resultExpr = new JavaVariableReference(
                        (JavaMemberVariable) member);
                }
                else
                {
                    final JavaFunctionInvocation callExpr = new JavaFunctionInvocation(
                        new JavaFunctionReference((JavaFunction) member));
                    if (convExprs != null)
                    {
                        for (final ConvertedExpression convExpr : convExprs)
                        {
                            callExpr.addArgument(convExpr
                                .mergeIntoResult(result));
                        }
                    }
                    resultExpr = callExpr;
                }
                result.setResultExpr(resultExpr);
            }
        }
        if (result.getResultExpr() == null)
        {
            ExpressionTranslator xlat = new ExpressionTranslator(this,
                containingType, varInfoMap, returnVar, promoteType,
                desiredResultType, result);
            xlat.sampleAsync = sampleAsync;
            veraExpr.accept(xlat);
        }
        return result;
    }

    protected final ConvertedExpression translateExpr(
        VeraExpression veraExpr,
        TempBlockScope tempScope,
        JavaStructuredType containingType,
        VarInfoMap varInfoMap,
        JavaLocalVariable returnVar,
        JavaType promoteType,
        JavaType desiredResultType)
    {
        return translateExpr(veraExpr, tempScope, containingType, varInfoMap,
            returnVar, promoteType, desiredResultType, true);
    }

    protected void convertInitializer(
        ConvertedExpression convExpr,
        JavaType type)
    {
        JavaExpression rhs = convExpr.getResultExpr();
        JavaType rhsType = rhs.getResultType();
        rhs = convertRHS(rhs, rhsType, type, true);
        convExpr.setResultExpr(rhs);
    }

    protected JavaExpression convertRHS(
        JavaExpression rhs,
        JavaType lhsType,
        boolean initializing)
    {
        JavaType rhsType = rhs.getResultType();
        return convertRHS(rhs, rhsType, lhsType, initializing);
    }

    protected boolean needRHSConversion(JavaType lhsType, JavaType rhsType)
    {
        return (!lhsType.equals(rhsType) && !lhsType.isAssignableFrom(rhsType))
            || schema.getBitVectorSize(lhsType) != schema
                .getBitVectorSize(rhsType);
    }

    protected JavaExpression convertRHS(
        JavaExpression rhs,
        JavaType rhsType,
        JavaType lhsType,
        boolean initializing)
    {
        // attempt type conversion if necessary
        if (needRHSConversion(lhsType, rhsType))
        {
            rhs = exprConv.toType(lhsType, rhs);
        }
        // otherwise clone object if necessary
        else if (initializing)
        {
            if (needsClone(rhsType)
                && (rhs instanceof JavaVariableReference || rhs instanceof JavaMemberAccess))
            {
                rhs = getCloneExpr(rhs);
            }
            else if (rhsType instanceof JavaNullType && needsClone(lhsType))
            {
                rhs = ExpressionBuilder.newInstance((JavaRawClass) lhsType);
            }
        }
        return rhs;
    }

    protected JavaExpression getCloneExpr(JavaExpression expr)
    {
        JavaType type = expr.getResultType();
        if (type instanceof JavaArrayType)
        {
            // (Foo[]) foo.clone()
            return new JavaCastExpression(type, ExpressionBuilder.memberCall(
                expr, "clone"));
        }
        else
        {
            // new Foo(foo)
            return ExpressionBuilder.newInstance((JavaClass) type, expr);
        }
    }

    protected JavaExpression getNotExpr(JavaExpression expr)
    {
        assert (expr.getResultType() == schema.booleanType);
        if (expr instanceof JavaLogicalNot)
        {
            return ((JavaLogicalNot) expr).getOperand(0);
        }
        else
        {
            return new JavaLogicalNot(schema, expr);
        }
    }

    public JavaExpression getEdgeExpr(EdgeSet e)
    {
        final String name;
        final byte mask = e.getMask();
        switch (mask)
        {
        case EdgeSet.POSEDGE_MASK:
            name = "POSEDGE";
            break;
        case EdgeSet.NEGEDGE_MASK:
            name = "NEGEDGE";
            break;
        case EdgeSet.ANYEDGE_MASK:
            name = "ANYEDGE";
            break;
        case EdgeSet.MASK_01:
            name = "EDGE_01";
            break;
        case EdgeSet.MASK_0X:
            name = "EDGE_0X";
            break;
        case EdgeSet.MASK_10:
            name = "EDGE_10";
            break;
        case EdgeSet.MASK_1X:
            name = "EDGE_1X";
            break;
        case EdgeSet.MASK_X0:
            name = "EDGE_X0";
            break;
        case EdgeSet.MASK_X1:
            name = "EDGE_X1";
            break;
        case 0:
            name = "NO_EDGE";
            break;
        default:
            name = null;
        }

        JavaExpression edgeExpr;
        if (name != null)
        {
            edgeExpr = new JavaVariableReference(types.edgeSetType
                .getField(name));
        }
        else
        {
            edgeExpr = ExpressionBuilder.newInstance(types.edgeSetType,
                new JavaIntLiteral(schema, mask));
        }
        return edgeExpr;
    }

    protected void checkUpdate(
        ConvertedExpression result,
        JavaExpression oldValue,
        JavaExpression newValue,
        boolean primitive,
        JavaExpression updateEvent)
    {
        JavaExpression changedExpr;
        if (primitive)
        {
            changedExpr = new JavaNotEqual(schema, newValue, oldValue);
        }
        else
        {
            changedExpr = new JavaLogicalNot(schema, ExpressionBuilder
                .staticCall(types.junoType, "equals", newValue, oldValue));
        }

        JavaExpression triggerCall = getTriggerCall(updateEvent);
        JavaBlock triggerBlock = new JavaBlock(schema);
        triggerBlock.addMember(new JavaExpressionStatement(triggerCall));

        JavaIfStatement checkStmt = new JavaIfStatement(changedExpr,
            triggerBlock);
        result.addUpdateMember(checkStmt);
    }

    private JavaExpression getTriggerCall(JavaExpression eventExpr)
    {
        JavaArrayType veraEventArrayType = schema.getArrayType(
            types.junoEventType, 1);
        JavaArrayInitializer eventsInitExpr = new JavaArrayInitializer(
            veraEventArrayType);
        eventsInitExpr.addElement(eventExpr);
        JavaArrayCreation eventsExpr = new JavaArrayCreation(veraEventArrayType);
        eventsExpr.setInitializer(eventsInitExpr);
        JavaExpression triggerCall = ExpressionBuilder.staticCall(
            types.junoType, "trigger", eventsExpr);
        return triggerCall;
    }

    protected JavaClassMember translateExpressionDefine(
        VeraExpressionDefine obj,
        List<JavaExpression> javaArgExprs,
        JavaPackage pkg)
    {
        if (!obj.hasArguments())
        {
            // check whether no-arg define has already been translated
            JavaClassMember member = (JavaClassMember) xlatObjMap
                .getJavaObject(obj);
            if (member != null) return member;

            // check whether define value is imported from Verilog
            if (obj.isVerilogImport())
            {
                member = vlogImporter.translateDefine(obj);
                if (member != null) xlatObjMap.addJavaObject(obj, member);
                return member;
            }
        }
        else
        {
            // cannot translate function macro without argument types
            if (javaArgExprs == null) return null;
        }

        // get containing class for translated define
        final JavaRawClass cls = getClassForGlobal(obj, pkg);

        // get identifier of define
        String id = obj.getName().getIdentifier();

        // ensure identifier is not a Java keyword
        id = fixID(id);

        // attempt to look up method for function define
        VarInfoMap varInfoMap = null;
        List<JavaFunctionArgument> argVarList = null;
        if (obj.hasArguments())
        {
            final JavaType[] argTypes = new JavaType[javaArgExprs.size()];
            int index = 0;
            for (JavaExpression argExpr : javaArgExprs)
            {
                JavaType argType = argExpr.getResultType();
                // generalize argument type
                if (argType instanceof JoveBitVectorType)
                {
                    argType = schema.bitVectorType;
                }
                argTypes[index++] = argType;
            }

            try
            {
                final JavaFunction method = cls.getMethod(id, argTypes, cls);
                return method;
            }
            catch (MethodNotFoundException e)
            {
                // ignored
            }
            catch (AmbiguousMethodException e)
            {
                // ignored
            }

            varInfoMap = new VarInfoMap();
            argVarList = new LinkedList<JavaFunctionArgument>();
            final List<VeraDefineArgument> defArgs = obj.getArguments();
            index = 0;
            for (VeraDefineArgument argDefn : defArgs)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(argDefn);
                if (varAnalysis != null && varAnalysis.isWriteAccess())
                {
                    // cannot translate define with assignment to argument
                    return null;
                }
                String argID = argDefn.getName().getIdentifier();
                JavaType argType = argTypes[index];
                JavaFunctionArgument arg = new JavaFunctionArgument(argID,
                    argType);
                varInfoMap.addInfo(new VarInfo(schema, argDefn, arg, false));
                argVarList.add(arg);
                ++index;
            }
        }

        // translate expression
        final VeraExpression veraExpr = obj.getExpression();
        final ConvertedExpression convExpr = translateExpr(veraExpr, null, cls,
            varInfoMap, null, null, null);
        JavaType type = convExpr.getResultExpr().getResultType();

        // cannot translate defines for null expressions
        if (type instanceof JavaNullType) return null;

        logEnter("Translating expression define: " + id);

        // translate visibility
        final JavaVisibility vis = obj.getVisibility() == VeraVisibility.LOCAL
            ? JavaVisibility.DEFAULT : JavaVisibility.PUBLIC;

        // generalize return type
        if (type instanceof JoveBitVectorType)
        {
            type = schema.bitVectorType;
        }

        // translate constant expression as variable, non-constant as function
        JavaClassMember member;
        if (!obj.hasArguments() && veraExpr.isConstant())
        {
            // create Java static final member variable
            final JavaMemberVariable var = new JavaMemberVariable(id, type);
            var.addAnnotations(obj.getAnnotations());
            var.setVisibility(vis);
            var.addModifier(JavaVariableModifier.STATIC);
            var.addModifier(JavaVariableModifier.FINAL);
            member = var;

            // set initializer
            final JavaExpression initExpr = convExpr.toMemberInitExpr(cls, var);
            var.setInitializer(initExpr);
        }
        else
        {
            // create Java static function
            final JavaFunctionType funcType = new JavaFunctionType(type,
                argVarList);
            final JavaFunction func = new JavaFunction(id, funcType);
            func.addAnnotations(obj.getAnnotations());
            func.setVisibility(vis);
            func.addModifier(JavaFunctionModifier.STATIC);
            member = func;

            // create function body
            final JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            // inject statements from converted expression into body
            final JavaExpression resultExpr = convExpr.toBlockExpr(body,
                "result");
            assert (resultExpr != null);

            // add return statement at end of body
            body.addMember(new JavaReturnStatement(schema, resultExpr));
        }
        cls.addMember(member);
        if (!obj.hasArguments()) xlatObjMap.addJavaObject(obj, member);

        logExit();
        return member;
    }

    protected JavaClassMember translateExpressionDefine(
        VeraExpressionDefine obj,
        List<JavaExpression> javaArgExprs)
    {
        return translateExpressionDefine(obj, javaArgExprs,
            translateCompUnitOf(obj));
    }

    protected JavaClassMember translateRangeDefine(
        VeraRangeDefine obj,
        JavaPackage pkg)
    {
        // range macros with arguments are not supported
        if (obj.hasArguments()) return null;

        // check whether define has already been translated
        JavaClassMember member = (JavaClassMember) xlatObjMap
            .getJavaObject(obj);
        if (member != null) return member;

        // check whether define value is imported from Verilog
        if (obj.isVerilogImport())
        {
            member = vlogImporter.translateDefine(obj);
            if (member != null) xlatObjMap.addJavaObject(obj, member);
            return member;
        }

        // get containing class for translated define
        final JavaRawClass cls = getClassForGlobal(obj, pkg);

        // get identifier of define
        String id = obj.getName().getIdentifier();

        // ensure identifier is not a Java keyword
        id = fixID(id);

        // attempt to look up method for function define
        VarInfoMap varInfoMap = null;
        List<JavaFunctionArgument> argVarList = null;

        // translate expression
        final VeraRange range = obj.getRange();
        final VeraExpression veraFromExpr = range.getFrom();
        final VeraExpression veraToExpr = range.getTo();
        final ConvertedExpression fromConvExpr = translateExpr(veraFromExpr,
            null, cls, varInfoMap, null, null, schema.intType);
        final ConvertedExpression toConvExpr = translateExpr(veraToExpr, null,
            cls, varInfoMap, null, null, schema.intType);

        // check that range expressions are integral
        final JavaType fromType = fromConvExpr.getResultExpr().getResultType();
        final JavaType toType = toConvExpr.getResultExpr().getResultType();
        if (!fromType.isIntegralConvertible()
            || !toType.isIntegralConvertible()) return null;

        fromConvExpr.convertResultExpr(schema.intType, exprConv);
        toConvExpr.convertResultExpr(schema.intType, exprConv);

        logEnter("Translating range define: " + id);

        // translate visibility
        final JavaVisibility vis = obj.getVisibility() == VeraVisibility.LOCAL
            ? JavaVisibility.DEFAULT : JavaVisibility.PUBLIC;

        // translate constant expression as variable, non-constant as function
        if (veraFromExpr.isConstant() && veraToExpr.isConstant()
            && fromConvExpr.isSimpleExpr() && toConvExpr.isSimpleExpr())
        {
            // create Java static final member variable
            final JavaMemberVariable var = new JavaMemberVariable(id,
                types.bitRangeType);
            var.addAnnotations(obj.getAnnotations());
            var.setVisibility(vis);
            var.addModifier(JavaVariableModifier.STATIC);
            var.addModifier(JavaVariableModifier.FINAL);
            member = var;

            // set initializer
            final JavaInstanceCreation rangeExpr = ExpressionBuilder
                .newInstance(types.bitRangeType, fromConvExpr.getResultExpr(),
                    toConvExpr.getResultExpr());
            var.setInitializer(rangeExpr);
        }
        else
        {
            // create Java static function
            final JavaFunctionType funcType = new JavaFunctionType(
                types.bitRangeType, argVarList);
            final JavaFunction func = new JavaFunction(id, funcType);
            func.addAnnotations(obj.getAnnotations());
            func.setVisibility(vis);
            func.addModifier(JavaFunctionModifier.STATIC);
            member = func;

            // create function body
            final JavaBlock body = new JavaBlock(schema);
            func.setBody(body);

            // inject statements from converted expressions into body
            final JavaExpression fromExpr = fromConvExpr.toBlockExpr(body,
                "high");
            final JavaExpression toExpr = toConvExpr.toBlockExpr(body, "low");
            assert (fromExpr != null && toExpr != null);

            // add return statement at end of body
            final JavaInstanceCreation rangeExpr = ExpressionBuilder
                .newInstance(types.bitRangeType, fromExpr, toExpr);
            body.addMember(new JavaReturnStatement(schema, rangeExpr));
        }
        cls.addMember(member);
        if (!obj.hasArguments()) xlatObjMap.addJavaObject(obj, member);

        logExit();
        return member;
    }

    protected JavaClassMember translateRangeDefine(VeraRangeDefine obj)
    {
        return translateRangeDefine(obj, translateCompUnitOf(obj));
    }
}
