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

package com.newisys.schemaanalyzer.juno;

import java.util.Iterator;

import com.newisys.langschema.vera.*;

/**
 * Schema analyzer for compilation unit member objects.
 * 
 * @author Trevor Robinson
 */
final class CompUnitMemberAnalyzer
    extends AnalyzerModule
    implements VeraCompilationUnitMemberVisitor
{
    private final VeraCompilationUnit compUnit;

    public CompUnitMemberAnalyzer(
        VeraSchemaAnalyzer analyzer,
        VeraCompilationUnit compUnit)
    {
        super(analyzer);
        this.compUnit = compUnit;
    }

    public void visit(VeraBindVariable obj)
    {
        ++analyzer.bindCount;
        analyzer.bindSignalCount += obj.getMembers().size();
    }

    public void visit(VeraEnumeration obj)
    {
        ++analyzer.enumCount;
    }

    public void visit(VeraEnumerationElement obj)
    {
        // ignored
    }

    public void visit(VeraExpressionDefine obj)
    {
        ++analyzer.exprDefineCount;

        DADUAnalysis dadu = new DADUAnalysis();
        initDefineDADU(obj, dadu);
        VeraExpression expr = obj.getExpression();
        analyzeExpression(expr, AccessType.READ, compUnit, null, obj, null,
            dadu);
    }

    public void visit(VeraGlobalFunction obj)
    {
        ++analyzer.globalFuncCount;

        DADUAnalysis dadu = new DADUAnalysis();
        initFunctionDADU(obj, dadu);
        BlockAnalysis blockAnalysis = analyzeBlock(obj.getBody(), obj,
            compUnit, null, false, dadu);
        dadu = blockAnalysis.dadu;
        finalizeFunctionDADU(obj, dadu);
    }

    public void visit(VeraGlobalVariable obj)
    {
        ++analyzer.globalVarCount;
        assert (obj.getInitializer() == null);
    }

    public void visit(VeraHDLFunction obj)
    {
        ++analyzer.hdlFuncCount;
    }

    public void visit(VeraInterfaceType obj)
    {
        ++analyzer.interfaceCount;
        analyzer.interfaceSignalCount += obj.getMembers().size();
    }

    public void visit(VeraPortType obj)
    {
        ++analyzer.portCount;
        analyzer.portSignalCount += obj.getMembers().size();
    }

    public void visit(VeraProgram obj)
    {
        ++analyzer.programCount;
        DADUAnalysis dadu = new DADUAnalysis();
        analyzeBlock(obj.getBlock(), obj, compUnit, null, false, dadu);
    }

    public void visit(VeraRangeDefine obj)
    {
        ++analyzer.rangeDefineCount;

        DADUAnalysis dadu = new DADUAnalysis();
        initDefineDADU(obj, dadu);
        VeraExpression from = obj.getRange().getFrom();
        analyzeExpression(from, AccessType.READ, compUnit, null, obj, null,
            dadu);
        VeraExpression to = obj.getRange().getTo();
        analyzeExpression(to, AccessType.READ, compUnit, null, obj, null, dadu);
    }

    public void visit(VeraStatementDefine obj)
    {
        ++analyzer.stmtDefineCount;

        DADUAnalysis dadu = new DADUAnalysis();
        initDefineDADU(obj, dadu);
        VeraStatement stmt = obj.getStatement();
        analyzeBlockMember(stmt, obj, compUnit, null, false, dadu);
    }

    public void visit(VeraTypeDefine obj)
    {
        ++analyzer.typeDefineCount;
    }

    public void visit(VeraUDFFunction obj)
    {
        ++analyzer.udfFuncCount;
    }

    public void visit(VeraUserClass obj)
    {
        ++analyzer.classCount;
        ClassAnalysis analysis = analyzer.getOrCreateClassAnalysis(obj);
        ClassMemberAnalyzer cma = new ClassMemberAnalyzer(analyzer, obj,
            analysis);
        // process members in three passes:
        // pass 0: variables
        // pass 1: constructors
        // pass 2: anything else
        for (int pass = 0; pass <= 2; ++pass)
        {
            Iterator iter = obj.getMembers().iterator();
            while (iter.hasNext())
            {
                VeraClassMember member = (VeraClassMember) iter.next();
                int objPass;
                if (member instanceof VeraMemberVariable)
                {
                    objPass = 0;
                }
                else if (member instanceof VeraMemberFunction
                    && ((VeraMemberFunction) member).isConstructor())
                {
                    objPass = 1;
                }
                else
                {
                    objPass = 2;
                }
                if (pass == objPass) member.accept(cma);
            }

            if (pass == 1) cma.ctorsComplete();
        }
        cma.finalizeAnalysis();
    }
}
