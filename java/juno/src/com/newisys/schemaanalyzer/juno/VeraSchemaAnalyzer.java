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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.vera.*;
import com.newisys.util.logging.IndentLogger;

/**
 * Performs various analyses on a Vera schema and provides access to the
 * results. The analyses performed include the following:
 * <ul>
 * <li>variable read/write analysis (is a variable read/written, read when
 * unassigned, written when assigned, initialized with side effects?)</li>
 * <li>X/Z propagation (is a variable directly or indirectly assigned an X/Z
 * or observed as X/Z?)</li>
 * <li>string usage (is a string modified?, is its Vera error or matching
 * metadata accessed?)</li>
 * <li>field usage (what is the most restrictive scope of accesses to a
 * field?, is a field definitely assigned in the constructor?, is it ever
 * assigned outside the constructor?)</li>
 * <li>by-reference argument usage (what variables are aliased by by-reference
 * arguments?)</li>
 * <li>fork analysis (what variables are read or written in a forked thread?)</li>
 * <li>wait_var analysis (what variables are directly or indirectly the target
 * of a wait_var call?)</li>
 * <li>counting of schema object types</li>
 * </ul>
 * 
 * @author Trevor Robinson
 */
public final class VeraSchemaAnalyzer
{
    private final Map<VeraSchemaObject, Object> analyses = new HashMap<VeraSchemaObject, Object>();
    final IndentLogger log;

    int classCount = 0;
    int fieldCount = 0;
    int constraintCount = 0;
    int methodCount = 0;
    int enumCount = 0;
    int globalVarCount = 0;
    int globalFuncCount = 0;
    int programCount = 0;
    int interfaceCount = 0;
    int interfaceSignalCount = 0;
    int portCount = 0;
    int portSignalCount = 0;
    int bindCount = 0;
    int bindSignalCount = 0;
    int hdlFuncCount = 0;
    int udfFuncCount = 0;
    int localVarCount = 0;
    int statementCount = 0;
    int exprDefineCount = 0;
    int rangeDefineCount = 0;
    int stmtDefineCount = 0;
    int typeDefineCount = 0;

    public VeraSchemaAnalyzer(IndentLogger log)
    {
        this.log = log;
    }

    public void analyze(VeraSchema schema)
    {
        // analyze all members of schema
        SchemaMemberAnalyzer sma = new SchemaMemberAnalyzer(this);
        Iterator iter = schema.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraSchemaMember obj = (VeraSchemaMember) iter.next();
            obj.accept(sma);
        }

        // mark scope complete for remaining variables
        final boolean checkedCompletion = false;
        iter = analyses.values().iterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof VariableAnalysis)
            {
                VariableAnalysis varAnalysis = (VariableAnalysis) obj;
                if (!varAnalysis.isXZKnown())
                {
                    if (!checkedCompletion)
                    {
                        varAnalysis.markNotAssignedXZ();
                    }
                    else if (!varAnalysis.isScopeComplete())
                    {
                        varAnalysis.markScopeComplete();
                    }
                }
            }
        }
        if (checkedCompletion)
        {
            iter = analyses.values().iterator();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof VariableAnalysis)
                {
                    VariableAnalysis varAnalysis = (VariableAnalysis) obj;
                    assert varAnalysis.isAssignedXZ() != varAnalysis
                        .isNotAssignedXZ() : varAnalysis + ": isAssignedXZ{"
                        + varAnalysis.isAssignedXZ() + "} != isNotAssignedXZ{"
                        + varAnalysis.isNotAssignedXZ() + "}";
                }
            }
        }

        dumpXZAnalysis();

        // resolve X/Z analysis among method overrides
        iter = analyses.values().iterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof FunctionAnalysis)
            {
                FunctionAnalysis funcAnalysis = (FunctionAnalysis) obj;
                if (funcAnalysis.getOverridden() == null)
                {
                    int argCount = funcAnalysis.getArgumentCount();
                    for (int i = -1; i < argCount; ++i)
                    {
                        boolean xz = analyzeArgXZInOverride(funcAnalysis, i,
                            true);
                        if (xz) analyzeArgXZInOverride(funcAnalysis, i, false);
                    }
                    for (int i = 0; i < argCount; ++i)
                    {
                        boolean stateful = analyzeArgStatefulStringInOverride(
                            funcAnalysis, i, true);
                        if (stateful)
                            analyzeArgStatefulStringInOverride(funcAnalysis, i,
                                false);
                    }
                }
            }
        }
    }

    private void dumpXZAnalysis()
    {
        int notXZCount = 0, totalCount = 0;
        int[] sourceTypeCount = new int[XZSourceType.NUM_CODES];

        log.println("X/Z analysis summary:");
        log.incIndent();
        Iterator iter = analyses.values().iterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof VariableAnalysis)
            {
                VariableAnalysis varAnalysis = (VariableAnalysis) obj;
                if (varAnalysis.isXZType())
                {
                    if (varAnalysis.isNotAssignedXZ())
                    {
                        ++notXZCount;
                    }
                    else
                    {
                        XZSourceType sourceType = varAnalysis.getXZSourceType();
                        if (sourceType != XZSourceType.ASSIGNMENT)
                        {
                            int destCount = dumpXZSource(varAnalysis,
                                sourceType);
                            sourceTypeCount[sourceType.getCode()] += destCount;
                        }
                    }
                    ++totalCount;
                }
            }
        }
        log.decIndent();

        for (int i = 1; i < XZSourceType.NUM_CODES; ++i)
        {
            log.println(sourceTypeCount[i] + " variables X/Z due to "
                + XZSourceType.getInstance(i)
                + " (or assignments from such variables)");
        }
        log.println(notXZCount + " of " + totalCount
            + " bit/integer variables provably not X/Z");
    }

    private int dumpXZSource(
        VariableAnalysis varAnalysis,
        XZSourceType sourceType)
    {
        log.println(varAnalysis + " can be X/Z due to " + sourceType);
        int destCount = 1;
        Set dests = varAnalysis.getXZDestinations();
        if (!dests.isEmpty())
        {
            log.incIndent();
            Iterator iter = dests.iterator();
            while (iter.hasNext())
            {
                VariableAnalysis destVar = (VariableAnalysis) iter.next();
                destCount += dumpXZSource(destVar, XZSourceType.ASSIGNMENT);
            }
            log.decIndent();
        }
        return destCount;
    }

    private boolean analyzeArgXZInOverride(
        FunctionAnalysis funcAnalysis,
        int index,
        boolean check)
    {
        boolean xz = true;
        if (check)
        {
            VeraFunctionType funcType = funcAnalysis.func.getType();
            if (index < 0)
            {
                VeraLocalVariable returnVar = funcAnalysis.func.getReturnVar();
                xz = funcType.isReturnsXZ();
                if (!xz && returnVar != null)
                {
                    VariableAnalysis varAnalysis = getVariableAnalysis(returnVar);
                    xz = varAnalysis == null || !varAnalysis.isNotAssignedXZ();
                }
            }
            else
            {
                VeraFunctionArgument arg = funcType.getArguments().get(index);
                xz = arg.isReturnsXZ();
                if (!xz)
                {
                    VariableAnalysis varAnalysis = getVariableAnalysis(arg);
                    xz = varAnalysis == null || !varAnalysis.isNotAssignedXZ();
                }
            }
            if (xz) return true;
        }

        for (FunctionAnalysis overrideAnalysis : funcAnalysis
            .getDirectOverrides())
        {
            xz |= analyzeArgXZInOverride(overrideAnalysis, index, check);
            if (check && xz) break;
        }

        if (!check)
        {
            if (index < 0)
            {
                funcAnalysis.setReturnXZInOverride(true);
            }
            else
            {
                funcAnalysis.setArgumentXZInOverride(index, true);
            }
        }

        return xz;
    }

    private boolean analyzeArgStatefulStringInOverride(
        FunctionAnalysis funcAnalysis,
        int index,
        boolean check)
    {
        assert (index >= 0);
        boolean stateful = true;
        if (check)
        {
            VeraFunctionType funcType = funcAnalysis.func.getType();
            VeraFunctionArgument arg = funcType.getArguments().get(index);
            VariableAnalysis varAnalysis = getVariableAnalysis(arg);
            stateful = varAnalysis != null
                && varAnalysis.isNeedStatefulString();
            if (stateful) return true;
        }

        for (FunctionAnalysis overrideAnalysis : funcAnalysis
            .getDirectOverrides())
        {
            stateful |= analyzeArgStatefulStringInOverride(overrideAnalysis,
                index, check);
            if (check && stateful) break;
        }

        if (!check)
        {
            funcAnalysis.setArgumentStatefulStringInOverride(index, true);
        }

        return stateful;
    }

    public ClassAnalysis getClassAnalysis(VeraUserClass cls)
    {
        ClassAnalysis analysis = (ClassAnalysis) analyses.get(cls);
        assert (analysis == null || analysis.cls == cls);
        return analysis;
    }

    ClassAnalysis getOrCreateClassAnalysis(VeraUserClass cls)
    {
        ClassAnalysis analysis = getClassAnalysis(cls);
        if (analysis == null)
        {
            analysis = new ClassAnalysis(cls);
            analyses.put(cls, analysis);
        }
        return analysis;
    }

    public VariableAnalysis getVariableAnalysis(VeraVariable var)
    {
        VariableAnalysis analysis = (VariableAnalysis) analyses.get(var);
        assert (analysis == null || analysis.var == var);
        return analysis;
    }

    VariableAnalysis getOrCreateVariableAnalysis(
        VeraVariable var,
        SchemaObject context)
    {
        VariableAnalysis analysis = getVariableAnalysis(var);
        if (analysis == null)
        {
            analysis = new VariableAnalysis(var, context);
            analyses.put(var, analysis);
        }
        else if (context != null && analysis.context == null)
        {
            analysis.context = context;
        }
        return analysis;
    }

    public FunctionAnalysis getFunctionAnalysis(VeraFunction func)
    {
        FunctionAnalysis analysis = (FunctionAnalysis) analyses.get(func);
        assert (analysis == null || analysis.func == func);
        return analysis;
    }

    FunctionAnalysis getOrCreateFunctionAnalysis(VeraFunction func)
    {
        FunctionAnalysis analysis = getFunctionAnalysis(func);
        if (analysis == null)
        {
            analysis = new FunctionAnalysis(this, func);
            analyses.put(func, analysis);
        }
        return analysis;
    }

    public FunctionAnalysis getBaseFunctionAnalysis(VeraFunction func)
    {
        FunctionAnalysis analysis = getFunctionAnalysis(func);
        while (analysis != null)
        {
            FunctionAnalysis baseAnalysis = analysis.getOverridden();
            if (baseAnalysis == null) break;
            analysis = baseAnalysis;
        }
        return analysis;
    }

    public BlockAnalysis getBlockAnalysis(VeraStatement stmt)
    {
        BlockAnalysis analysis = (BlockAnalysis) analyses.get(stmt);
        return analysis;
    }

    void setBlockAnalysis(VeraStatement stmt, BlockAnalysis analysis)
    {
        analyses.put(stmt, analysis);
    }

    public void dumpCounts()
    {
        log.println("Classes: " + classCount + " [Fields: " + fieldCount
            + ", Constraints: " + constraintCount + ", Methods: " + methodCount
            + "]");
        log.println("Enums: " + enumCount);
        log.println("Global variables: " + globalVarCount);
        log.println("Global functions: " + globalFuncCount);
        log.println("Program blocks: " + programCount);
        log.println("Interfaces: " + interfaceCount + " [Signals: "
            + interfaceSignalCount + "]");
        log.println("Ports: " + portCount + " [Signals: " + portSignalCount
            + "]");
        log.println("Binds: " + bindCount + " [Signals: " + bindSignalCount
            + "]");
        log.println("HDL functions: " + hdlFuncCount);
        log.println("UDF functions: " + udfFuncCount);
        log.println("Local variables: " + localVarCount);
        log.println("Statements: " + statementCount);
        log.println("Expression defines: " + exprDefineCount);
        log.println("Range defines: " + rangeDefineCount);
        log.println("Statement defines: " + stmtDefineCount);
        log.println("Type defines: " + typeDefineCount);
    }
}
