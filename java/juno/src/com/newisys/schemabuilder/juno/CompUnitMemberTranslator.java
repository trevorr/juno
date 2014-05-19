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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.newisys.dv.ifgen.schema.*;
import com.newisys.langschema.CompilationUnit;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.*;

/**
 * Schema translator for compilation unit members.
 * 
 * @author Trevor Robinson
 */
final class CompUnitMemberTranslator
    extends TranslatorModule
    implements VeraCompilationUnitMemberVisitor
{
    private final JavaPackage pkg;

    public CompUnitMemberTranslator(
        TranslatorModule xlatContext,
        JavaPackage pkg)
    {
        super(xlatContext);
        this.pkg = pkg;
    }

    public void visit(VeraBindVariable obj)
    {
        translateBind(obj, pkg);
    }

    public void visit(VeraEnumeration obj)
    {
        translateEnum(obj, pkg, null);
    }

    public void visit(VeraEnumerationElement obj)
    {
        // ignored; processed as part of enumeration
    }

    public void visit(VeraExpressionDefine obj)
    {
        // lazy-translate Verilog import defines
        if (!obj.isVerilogImport())
        {
            translateExpressionDefine(obj, null, pkg);
        }
    }

    public void visit(VeraGlobalFunction obj)
    {
        translateGlobalFunction(obj, pkg);
    }

    public void visit(VeraGlobalVariable obj)
    {
        translateGlobalVariable(obj, pkg);
    }

    public void visit(VeraHDLFunction obj)
    {
        translateHDLFunction(obj, pkg);
    }

    public void visit(VeraInterfaceType obj)
    {
        translateInterface(obj, pkg);
    }

    public void visit(VeraPortType obj)
    {
        translatePort(obj, pkg);
    }

    public void visit(VeraProgram obj)
    {
        String id = obj.getName().getIdentifier();
        logEnter("Translating program: " + obj.getName());

        // uniquify program name by prefixing compilation unit
        final CompilationUnit compUnit = obj.getCompilationUnit();
        id = buildID(getIDForCompUnit(compUnit, true), "Program");

        // create the Java class
        final JavaRawClass cls = new JavaRawClass(schema, id, pkg);
        cls.addAnnotations(obj.getAnnotations());
        cls.setBaseClass(types.dvAppType);
        cls.setVisibility(JavaVisibility.PUBLIC);
        pkg.addMember(cls);
        xlatObjMap.addJavaObject(obj, cls);

        // create constructor
        {
            final JavaFunctionType ctorType = new JavaFunctionType(
                schema.voidType);
            final JavaFunctionArgument dvSimArg = new JavaFunctionArgument(
                "dvSim", types.dvSimType);
            ctorType.addArgument(dvSimArg);
            final JavaConstructor ctor = new JavaConstructor(ctorType);
            ctor.setVisibility(JavaVisibility.PUBLIC);
            {
                final JavaBlock body = new JavaBlock(schema);
                final JavaVariableReference dvSimRef = new JavaVariableReference(
                    dvSimArg);

                // super(dvSim);
                final JavaConstructor baseCtor = types.dvAppType
                    .getConstructor(new JavaType[] { types.dvSimType }, cls);
                final JavaConstructorInvocation baseCtorCall = new JavaConstructorInvocation(
                    new JavaConstructorReference(baseCtor));
                baseCtorCall.addArgument(dvSimRef);
                body.addMember(new JavaExpressionStatement(baseCtorCall));

                ctor.setBody(body);
            }
            cls.addMember(ctor);
        }

        // override run method
        final JavaFunction runMethod = cls.newMethod("run", schema.voidType);
        runMethod.setVisibility(JavaVisibility.PUBLIC);
        final VeraBlock veraBlock = obj.getBlock();
        final JavaBlock runBody = translateBlock(veraBlock, null, null,
            runMethod, null, false);
        runMethod.setBody(runBody);

        // add call to Vera.initialize(); this is called in run() instead of
        // the constructor for several reasons:
        // a) it initializes Vera.systemClock; run() occurs after any Verilog
        //    initial blocks, and specifically the initial block in the Jove
        //    shell that registers all the signals
        // b) it must be called from a SimulationThread to properly seed the RNG
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.junoType, "initialize");
        runBody.addMemberAt(new JavaExpressionStatement(callExpr), 0);

        // create Verilog shell if necessary
        final Set<VeraCompilationUnitMember> shellMembers = obj
            .getShellMembers();
        final IfgenPackage ifPkg = ifSchema.getPackage(pkg.getName()
            .getCanonicalName(), true);
        if (!shellMembers.isEmpty() || ifPkg.getMembers().size() > 0)
        {
            final IfgenTestbench ifTestbench = new IfgenTestbench(ifSchema,
                new IfgenName("vera_shell", IfgenNameKind.EXPRESSION),
                Collections.<IfgenVariableDecl> emptyList());
            ifPkg.addMember(ifTestbench);

            Set<IfgenPackage> memberPkgs = new LinkedHashSet<IfgenPackage>();
            memberPkgs.add(ifPkg);
            
            for (VeraCompilationUnitMember member : shellMembers)
            {
                if (member instanceof VeraInterfaceType)
                {
                    VeraInterfaceType intf = (VeraInterfaceType) member;
                    log("Found shell interface: " + intf.getName());
                    translateInterface(intf, pkg);
                    IfgenInterface ifIntf = (IfgenInterface) xlatObjMap
                        .getIfgenObject(intf);
                    assert (ifIntf != null);
                    memberPkgs.add(ifIntf.getPackage());
                    //ifShell.addMember(new IfgenShellMember(ifIntf));
                }
                else if (member instanceof VeraHDLFunction)
                {
                    VeraHDLFunction func = (VeraHDLFunction) member;
                    log("Found shell hdl_task: " + func.getName());
                    translateHDLFunction(func, pkg);
                    IfgenHDLTask ifTask = (IfgenHDLTask) xlatObjMap
                        .getIfgenObject(func);
                    assert (ifTask != null);
                    memberPkgs.add(ifTask.getPackage());
                    //ifShell.addMember(new IfgenShellMember(ifTask));
                }
                else if (member instanceof VeraGlobalFunction)
                {
                    VeraGlobalFunction func = (VeraGlobalFunction) member;
                    log("Found shell exported task: " + func.getName());
                    translateGlobalFunction(func, pkg);
                    IfgenHVLTask ifTask = (IfgenHVLTask) xlatObjMap
                        .getIfgenObject(func);
                    assert (ifTask != null);
                    memberPkgs.add(ifTask.getPackage());
                    //ifShell.addMember(new IfgenShellMember(ifTask));
                }
                else
                {
                    throw new AssertionError("Unexpected shell member type: "
                        + member.getClass().getName());
                }
            }
            for (IfgenPackage memberPkg : memberPkgs)
            {
                log("Adding package to shell: " + memberPkg.getName());
                final IfgenUnresolvedName qname = new IfgenUnresolvedName(
                    memberPkg.getName());
                ifTestbench.addImportDecl(new IfgenWildname(qname, true));
            }
        }
        else
        {
            log("No shell members present");
        }

        logExit();
    }

    public void visit(VeraRangeDefine obj)
    {
        // lazy-translate Verilog import defines
        if (!obj.isVerilogImport())
        {
            translateRangeDefine(obj, pkg);
        }
    }

    public void visit(VeraStatementDefine obj)
    {
        // TODO: visit VeraStatementDefine
    }

    public void visit(VeraTypeDefine obj)
    {
        // TODO: visit VeraTypeDefine
    }

    public void visit(VeraUDFFunction obj)
    {
        translateGlobalFunction(obj, pkg);
    }

    public void visit(VeraUserClass obj)
    {
        translateClass(obj, pkg, true);
    }
}
