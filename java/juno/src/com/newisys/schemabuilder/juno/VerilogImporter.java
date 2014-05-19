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
import java.util.LinkedHashMap;
import java.util.Map;

import com.newisys.langschema.BlankLine;
import com.newisys.langschema.BlockComment;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.langschema.vera.VeraBitVectorType;
import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraExpression;
import com.newisys.langschema.vera.VeraExpressionDefine;
import com.newisys.langschema.vera.VeraIntegerType;
import com.newisys.langschema.vera.VeraRangeDefine;
import com.newisys.langschema.vera.VeraType;

/**
 * Used to provide dynamic access to #defines included from Verilog files.
 * 
 * @author Trevor Robinson
 */
final class VerilogImporter
{
    private final JoveSchema schema;
    private final PackageNamer packageNamer;
    private final SchemaTypes types;

    private final Map<String, JavaMemberVariable> files = new LinkedHashMap<String, JavaMemberVariable>();
    private JavaRawClass importCls;
    private JavaVariableReference definesVarRef;

    public VerilogImporter(
        JoveSchema schema,
        PackageNamer packageNamer,
        SchemaTypes types)
    {
        this.schema = schema;
        this.packageNamer = packageNamer;
        this.types = types;
    }

    private void createImportClass()
    {
        if (importCls != null) return;

        // create public final class
        final String pkgName = packageNamer.getExternalPackage();
        final JavaPackage pkg = schema.getPackage(pkgName, true);
        final JavaRawClass cls = new JavaRawClass(schema, "VerilogImports", pkg);
        cls.setBaseClass(types.objectType);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);
        pkg.addMember(cls);
        importCls = cls;

        // create empty private ctor to prevent instantiation
        final JavaConstructor ctor = cls.newConstructor();
        ctor.setVisibility(JavaVisibility.PRIVATE);
        ctor.setBody(new JavaBlock(schema));

        // add defines field (and leave default access)
        final JavaMemberVariable definesVar = cls.newField("defines",
            types.definesType);
        definesVar.addModifier(JavaVariableModifier.STATIC);
        definesVar.addModifier(JavaVariableModifier.FINAL);
        definesVar.setInitializer(ExpressionBuilder
            .newInstance(types.definesType));
        definesVarRef = new JavaVariableReference(definesVar);
    }

    private JavaMemberVariable createVar(
        String srcPath,
        String id,
        JavaType type)
    {
        createImportClass();

        final File srcFile = new File(srcPath);
        final String srcName = srcFile.getName();
        final JavaMemberVariable prevVar = files.get(srcName);
        if (prevVar == null)
        {
            // add static initializer block for reading defines file
            JavaInitializerBlock initBlock = new JavaInitializerBlock(schema,
                true);
            initBlock
                .addAnnotation(new BlockComment(" Imports from " + srcName));
            initBlock.addAnnotation(BlankLine.LEADING);
            initBlock.addMember(new JavaExpressionStatement(ExpressionBuilder
                .memberCall(definesVarRef, "readFile", new JavaStringLiteral(
                    schema, srcName))));
            importCls.addMember(initBlock);
        }

        // create field for define
        final JavaMemberVariable var = new JavaMemberVariable(TranslatorModule
            .fixID(id), type);
        var.setVisibility(JavaVisibility.PUBLIC);
        var.addModifier(JavaVariableModifier.STATIC);
        var.addModifier(JavaVariableModifier.FINAL);
        final String methodID;
        if (type instanceof JavaIntType)
            methodID = "getInt";
        else if (type instanceof JavaLongType)
            methodID = "getLong";
        else if (schema.isBitVector(type))
            methodID = "getBitVector";
        else if (type == types.bitRangeType)
            methodID = "getBitRange";
        else
            throw new AssertionError("Unexpected Verilog define type: " + type);
        var.setInitializer(ExpressionBuilder.memberCall(definesVarRef,
            methodID, new JavaStringLiteral(schema, id)));

        // add field to class after previous field from same file
        if (prevVar == null)
        {
            importCls.addMember(var);
        }
        else
        {
            importCls.addMemberAfter(var, prevVar);
        }
        files.put(srcName, var);

        return var;
    }

    public JavaMemberVariable translateDefine(VeraExpressionDefine define)
    {
        assert (define.isVerilogImport() && !define.hasArguments());
        final VeraCompilationUnit compUnit = define.getCompilationUnit();
        final String srcPath = compUnit.getSourcePath();
        final String id = define.getName().getIdentifier();
        final VeraExpression expr = define.getExpression();
        final VeraType veraType = expr.getResultType();
        final JavaType type;
        if (veraType instanceof VeraIntegerType)
        {
            type = schema.intType;
        }
        else if (veraType instanceof VeraBitVectorType)
        {
            type = schema.bitVectorType;
        }
        else
        {
            throw new AssertionError("Unexpected Verilog define type: "
                + veraType);
        }
        final JavaMemberVariable var = createVar(srcPath, id, type);
        return var;
    }

    public JavaMemberVariable translateDefine(VeraRangeDefine define)
    {
        assert (define.isVerilogImport() && !define.hasArguments());
        final VeraCompilationUnit compUnit = define.getCompilationUnit();
        final String srcPath = compUnit.getSourcePath();
        final String id = define.getName().getIdentifier();
        final JavaMemberVariable var = createVar(srcPath, id,
            types.bitRangeType);
        return var;
    }
}
