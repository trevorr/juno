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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.NamedObject;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.vera.*;
import com.newisys.verilog.util.BitVector;

/**
 * Base class for Vera analyzer modules. Defines common methods used by the
 * concrete analyzer classes.
 * 
 * @author Trevor Robinson
 */
abstract class AnalyzerModule
{
    protected final VeraSchemaAnalyzer analyzer;

    public AnalyzerModule(VeraSchemaAnalyzer analyzer)
    {
        this.analyzer = analyzer;
    }

    protected BlockAnalysis analyzeBlock(
        VeraBlock block,
        SchemaObject context,
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        boolean isCtor,
        DADUAnalysis dadu)
    {
        BlockAnalysis analysis;
        if (block != null)
        {
            analysis = analyzeBlockMember(block, context, compUnit, cls,
                isCtor, dadu);
            analyzer.setBlockAnalysis(block, analysis);
        }
        else
        {
            analysis = new BlockAnalysis(dadu);
        }
        return analysis;
    }

    protected BlockAnalysis analyzeBlockMember(
        VeraBlockMember blockMember,
        SchemaObject context,
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        boolean isCtor,
        DADUAnalysis dadu)
    {
        BlockAnalysis analysis = new BlockAnalysis(dadu);
        BlockMemberAnalyzer bma = new BlockMemberAnalyzer(analyzer, analysis,
            context, compUnit, cls, isCtor);
        blockMember.accept(bma);
        bma.finalizeAnalysis();
        return analysis;
    }

    protected ExpressionAnalysis analyzeExpression(
        VeraExpression expr,
        AccessType accessType,
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        SchemaObject context,
        BlockAnalysis blockAnalysis,
        DADUAnalysis daduAnalysis)
    {
        ExpressionAnalysis analysis = new ExpressionAnalysis(compUnit, cls,
            context, blockAnalysis, daduAnalysis);
        analyzeNestedExpression(expr, analysis, accessType, true);
        return analysis;
    }

    protected ExpressionAnalysis analyzeCondition(
        VeraExpression expr,
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        SchemaObject context,
        BlockAnalysis blockAnalysis,
        DADUAnalysis daduAnalysis)
    {
        ExpressionAnalysis analysis = new ExpressionAnalysis(compUnit, cls,
            context, blockAnalysis, daduAnalysis);
        analyzeNestedCondition(expr, analysis, AccessType.READ, true);
        return analysis;
    }

    protected void analyzeNestedExpression(
        VeraExpression expr,
        ExpressionAnalysis analysis,
        AccessType accessType,
        boolean propagateXZ)
    {
        ExpressionAnalyzer ea = new ExpressionAnalyzer(analyzer, analysis,
            accessType, propagateXZ);
        expr.accept(ea);

        analysis.mergeDADU();
    }

    protected void analyzeNestedCondition(
        VeraExpression expr,
        ExpressionAnalysis analysis,
        AccessType accessType,
        boolean propagateXZ)
    {
        ExpressionAnalyzer ea = new ExpressionAnalyzer(analyzer, analysis,
            accessType, propagateXZ);
        expr.accept(ea);

        boolean isLiteral;
        boolean isTrue;
        if (expr instanceof VeraBitVectorLiteral)
        {
            isLiteral = true;
            BitVector value = ((VeraBitVectorLiteral) expr).getValue();
            isTrue = !value.containsXZ() && !value.isZero();
        }
        else if (expr instanceof VeraIntegerLiteral)
        {
            isLiteral = true;
            isTrue = ((VeraIntegerLiteral) expr).getValue() != 0;
        }
        else if (expr instanceof VeraEnumValueReference)
        {
            isLiteral = true;
            isTrue = ((VeraEnumValueReference) expr).getElement().getValue() != 0;
        }
        else
        {
            isLiteral = false;
            isTrue = false;
        }

        if (isLiteral)
        {
            analysis.mergeDADU();
            DADUAnalysis liveDADU = analysis.daduUncond;
            DADUAnalysis deadDADU = liveDADU.duplicate();
            deadDADU.markDead();
            if (isTrue)
            {
                analysis.daduWhenTrue = liveDADU;
                analysis.daduWhenFalse = deadDADU;
            }
            else
            {
                analysis.daduWhenFalse = liveDADU;
                analysis.daduWhenTrue = deadDADU;
            }
        }
        else
        {
            analysis.splitDADU();
        }
    }

    protected void analyzeInvokeArgs(
        List actualArgs,
        VeraFunction func,
        ExpressionAnalysis analysis,
        boolean propagateXZ,
        boolean inlinedFunc)
    {
        // save X/Z analysis for invocation
        XZSourceType savedUncondXZ = analysis.uncondXZType;
        Set<VariableAnalysis> savedXZPropagatesFrom = analysis.xzPropagatesFrom;

        VeraFunctionType funcType = func.getType();
        List formalArgs = funcType.getArguments();
        Iterator formalIter = formalArgs.iterator();
        Iterator actualIter = actualArgs.iterator();
        while (actualIter.hasNext())
        {
            VeraExpression argExpr = (VeraExpression) actualIter.next();

            VeraFunctionArgument formalArg;
            VariableAnalysis formalArgAnalysis;
            boolean formalByRef;
            if (formalIter.hasNext())
            {
                formalArg = (VeraFunctionArgument) formalIter.next();
                formalArgAnalysis = analyzer.getOrCreateVariableAnalysis(
                    formalArg, func);
                formalByRef = formalArg.isByRef();
            }
            else
            {
                assert (funcType.isVarArgs());
                formalArg = null;
                formalArgAnalysis = null;
                formalByRef = funcType.isVarArgsByRef();
            }

            boolean actualByRef = formalByRef && isVariableReference(argExpr);
            if (actualByRef)
            {
                if (!inlinedFunc)
                {
                    // remember that this expression contains a by-ref argument
                    analysis.byRefArgument = true;
                }

                VeraVariable var = getReferencedVariable(argExpr);
                assert (var != null);
                VariableAnalysis varAnalysis = analyzer
                    .getOrCreateVariableAnalysis(var, analysis.context);
                if (formalArg != null)
                {
                    if (!inlinedFunc)
                    {
                        // remember that this variable is passed by-ref to this
                        // formal argument
                        varAnalysis.addByRefArgUse(formalArg, analyzer);
                    }

                    // if this function has a body to analyze, X/Z analysis for
                    // by-ref formal args propagates back to actual argument
                    // variables; otherwise, we must rely on the annotation
                    // associated with the function
                    if (func.getBody() != null)
                    {
                        varAnalysis.trackXZPropagation(formalArgAnalysis);
                    }
                    else if (formalArg.isReturnsXZ()
                        && !varAnalysis.isXZKnown())
                    {
                        varAnalysis.markAssignedXZ(XZSourceType.EXTERNAL, null);
                    }
                }
                else
                {
                    if (!inlinedFunc)
                    {
                        // remember that this variable is passed as a by-ref
                        // var-arg to this function
                        varAnalysis.addByRefVarArgUse(func, analyzer);
                    }

                    // assume that by-ref var-args can always be assigned X/Z
                    // (sscanf is currently the only by-ref var-arg function,
                    // and it will return X/Z)
                    if (!varAnalysis.isXZKnown())
                    {
                        varAnalysis.markAssignedXZ(XZSourceType.EXTERNAL, null);
                    }
                }
            }

            // clear X/Z analysis for argument
            analysis.uncondXZType = null;
            analysis.xzPropagatesFrom = null;

            analyzeNestedExpression(argExpr, analysis, actualByRef
                ? AccessType.READ_WRITE : AccessType.READ, !inlinedFunc);

            // update X/Z propagation analysis for argument
            if (formalArg != null && !inlinedFunc)
            {
                applyAssignAnalysis(formalArgAnalysis, analysis);
            }
        }

        // restore X/Z analysis for invocation
        analysis.uncondXZType = savedUncondXZ;
        analysis.xzPropagatesFrom = savedXZPropagatesFrom;

        // update X/Z propagation analysis for expression
        if (propagateXZ)
        {
            VeraLocalVariable returnVar = func.getReturnVar();
            if (returnVar != null)
            {
                VariableAnalysis returnVarAnalysis = analyzer
                    .getOrCreateVariableAnalysis(returnVar, func);
                analysis.addXZPropagatesFrom(returnVarAnalysis);
            }
            else if (funcType.isReturnsXZ())
            {
                // for built-in functions, we have no return variable to
                // propagate from; instead, we must rely on the annotation
                // associated with the function
                analysis.markUncondXZ(XZSourceType.EXTERNAL);
            }
        }
    }

    protected static boolean isVariableReference(VeraExpression varExpr)
    {
        // if expression is array access, use array expression instead
        VeraExpression varRef;
        if (varExpr instanceof VeraArrayAccess)
        {
            varRef = ((VeraArrayAccess) varExpr).getArray();
            if (!(varRef.getResultType() instanceof VeraArrayType))
            {
                // bit accesses are not considered variable references
                return false;
            }
        }
        else
        {
            varRef = varExpr;
        }

        // get variable
        if (varRef instanceof VeraVariableReference)
        {
            return true;
        }
        else if (varRef instanceof VeraMemberAccess)
        {
            StructuredTypeMember member = ((VeraMemberAccess) varRef)
                .getMember();
            return member instanceof VeraVariable;
        }
        return false;
    }

    protected static VeraVariable getReferencedVariable(VeraExpression varExpr)
    {
        // if expression is array access, use array expression instead
        VeraExpression varRef;
        if (varExpr instanceof VeraArrayAccess)
        {
            varRef = ((VeraArrayAccess) varExpr).getArray();
        }
        else if (varExpr instanceof VeraBitSliceAccess)
        {
            varRef = ((VeraBitSliceAccess) varExpr).getArray();
        }
        else
        {
            varRef = varExpr;
        }

        // get variable; return null for signal, this, super, etc.
        VeraVariable var = null;
        if (varRef instanceof VeraVariableReference)
        {
            var = ((VeraVariableReference) varRef).getVariable();
        }
        else if (varRef instanceof VeraMemberAccess)
        {
            StructuredTypeMember member = ((VeraMemberAccess) varRef)
                .getMember();
            if (member instanceof VeraVariable)
            {
                var = (VeraVariable) member;
            }
        }
        return var;
    }

    protected void applyAssignAnalysis(
        VeraExpression lhsExpr,
        ExpressionAnalysis analysis)
    {
        // recursively apply to each term of left-hand concatenation
        if (lhsExpr instanceof VeraConcatenation)
        {
            VeraConcatenation concat = (VeraConcatenation) lhsExpr;
            Iterator iter = concat.getOperands().iterator();
            while (iter.hasNext())
            {
                VeraExpression expr = (VeraExpression) iter.next();
                applyAssignAnalysis(expr, analysis);
            }
        }
        else
        {
            // get variable referenced by expression (null for signals, etc.)
            VeraVariable var = getReferencedVariable(lhsExpr);
            if (var != null)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getOrCreateVariableAnalysis(var, analysis.context);
                applyAssignAnalysis(varAnalysis, analysis);
            }
        }
    }

    protected void applyAssignAnalysis(
        VariableAnalysis varAnalysis,
        ExpressionAnalysis analysis)
    {
        // update X/Z propagation analysis
        if (!varAnalysis.isXZKnown())
        {
            if (analysis.isUncondXZ())
            {
                varAnalysis.markAssignedXZ(analysis.uncondXZType, null);
            }
            else
            {
                Iterator iter = analysis.getXZPropagatesFrom().iterator();
                while (iter.hasNext())
                {
                    VariableAnalysis other = (VariableAnalysis) iter.next();
                    varAnalysis.trackXZPropagation(other);
                }
            }
        }

        // update write side-effects analysis
        if (analysis.sideEffects)
        {
            varAnalysis.markWriteSideEffects();
        }
    }

    protected static void finalizeLocalVars(Collection localVarAnalyses)
    {
        // mark local variables as out of scope
        Iterator iter = localVarAnalyses.iterator();
        while (iter.hasNext())
        {
            VariableAnalysis varAnalysis = (VariableAnalysis) iter.next();
            varAnalysis.markScopeComplete();
            iter.remove();
        }
    }

    protected void initFunctionDADU(VeraFunction func, DADUAnalysis dadu)
    {
        // initialize DA/DU tracking for arguments
        Iterator iter = func.getType().getArguments().iterator();
        while (iter.hasNext())
        {
            VeraFunctionArgument arg = (VeraFunctionArgument) iter.next();

            // create analysis object for this variable (to track context)
            analyzer.getOrCreateVariableAnalysis(arg, func);

            // initialize DA/DU tracking for argument
            int daduIndex = dadu.alloc(arg);

            // mark all arguments as assigned
            dadu.markAssigned(daduIndex);
        }

        // initialize DA/DU tracking for return variable (if any)
        VeraLocalVariable returnVar = func.getReturnVar();
        if (returnVar != null)
        {
            dadu.alloc(returnVar);
        }
    }

    protected void finalizeFunctionDADU(VeraFunction func, DADUAnalysis dadu)
    {
        // simulate final read of return variable for DA/DU analysis
        VeraLocalVariable returnVar = func.getReturnVar();
        if (returnVar != null && !dadu.isDA(returnVar))
        {
            VariableAnalysis varAnalysis = analyzer
                .getOrCreateVariableAnalysis(returnVar, func);

            varAnalysis.markReadUnassigned();

            if (!varAnalysis.isXZKnown())
            {
                varAnalysis.markAssignedXZ(XZSourceType.UNINIT, null);
            }
        }
    }

    protected void initDefineDADU(VeraDefine define, DADUAnalysis dadu)
    {
        // initialize DA/DU tracking for arguments
        Iterator iter = define.getArguments().iterator();
        while (iter.hasNext())
        {
            VeraDefineArgument arg = (VeraDefineArgument) iter.next();

            // create analysis object for this variable (to track context)
            analyzer.getOrCreateVariableAnalysis(arg, define);

            // initialize DA/DU tracking for argument
            int daduIndex = dadu.alloc(arg);

            // mark all arguments as assigned
            dadu.markAssigned(daduIndex);
        }
    }

    protected static String getDescription(SchemaObject obj)
    {
        if (obj instanceof NamedObject)
        {
            return ((NamedObject) obj).getName().getCanonicalName();
        }
        else
        {
            return obj.toDebugString();
        }
    }
}
