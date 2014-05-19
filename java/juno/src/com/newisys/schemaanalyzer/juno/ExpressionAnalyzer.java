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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.newisys.langschema.CompilationUnit;
import com.newisys.langschema.Function;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.vera.*;

/**
 * Schema analyzer for expression objects.
 * 
 * @author Trevor Robinson
 */
final class ExpressionAnalyzer
    extends AnalyzerModule
    implements VeraExpressionVisitor
{
    private final ExpressionAnalysis analysis;
    private final AccessType accessType;
    private final boolean propagateXZ;

    public ExpressionAnalyzer(
        VeraSchemaAnalyzer analyzer,
        ExpressionAnalysis analysis,
        AccessType accessType,
        boolean propagateXZ)
    {
        super(analyzer);
        this.analysis = analysis;
        this.accessType = accessType;
        this.propagateXZ = propagateXZ;
    }

    private static final int ASSIGN_NONE = 0;
    private static final int ASSIGN_FIRST = 1;
    private static final int ASSIGN_OP_FIRST = 2;
    private static final int ASSIGN_ALL = 3;

    private static final int XZ_NONE = 0;
    private static final int XZ_FIRST = 1;
    private static final int XZ_ALL = 2;

    private void analyzeOperation(
        VeraOperation operation,
        int assignType,
        int xzPropType,
        boolean leftToRight)
    {
        VeraExpression assignExpr = null;
        final List<VeraExpression> operands = operation.getOperands();
        final int startIndex = leftToRight ? 0 : operands.size();
        final ListIterator<VeraExpression> iter = operands
            .listIterator(startIndex);
        while (leftToRight ? iter.hasNext() : iter.hasPrevious())
        {
            final VeraExpression expr;
            final boolean isFirst;
            if (leftToRight)
            {
                isFirst = !iter.hasPrevious();
                expr = iter.next();
            }
            else
            {
                expr = iter.previous();
                isFirst = !iter.hasPrevious();
            }
            final AccessType nestedAccessType;
            switch (assignType)
            {
            case ASSIGN_NONE:
                nestedAccessType = AccessType.READ;
                break;
            case ASSIGN_FIRST:
                nestedAccessType = isFirst ? AccessType.WRITE : AccessType.READ;
                if (isFirst) assignExpr = expr;
                break;
            case ASSIGN_OP_FIRST:
                nestedAccessType = isFirst ? AccessType.READ_WRITE
                    : AccessType.READ;
                if (isFirst) assignExpr = expr;
                break;
            case ASSIGN_ALL:
                nestedAccessType = AccessType.WRITE;
                break;
            default:
                throw new Error("Invalid access type");
            }
            final boolean nestedPropagateXZ = propagateXZ
                && nestedAccessType == AccessType.READ
                && (xzPropType == XZ_ALL || (xzPropType == XZ_FIRST && isFirst));
            analyzeNestedExpression(expr, analysis, nestedAccessType,
                nestedPropagateXZ);
        }
        if (assignExpr != null)
        {
            applyAssignAnalysis(assignExpr, analysis);
        }
    }

    public void visit(VeraAdd obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraAndReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraArithmeticNegative obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraArrayAccess obj)
    {
        // treat bit writes as read/writes
        VeraExpression arrayExpr = obj.getArray();
        boolean bitAccess = arrayExpr.getResultType() instanceof VeraBitVectorType;
        analyzeNestedExpression(arrayExpr, analysis, bitAccess ? AccessType
            .getInstance(true, accessType.isWrite()) : AccessType.READ, false);

        Iterator iter = obj.getIndices().iterator();
        while (iter.hasNext())
        {
            VeraExpression indexExpr = (VeraExpression) iter.next();
            analyzeNestedExpression(indexExpr, analysis, AccessType.READ, false);
        }

        // update X/Z propagation analysis
        if (propagateXZ)
        {
            analysis.markUncondXZ(XZSourceType.ARRAY);
        }
    }

    public void visit(VeraArrayCreation obj)
    {
        VeraExpression sizeExpr = obj.getSizeExpr();
        analyzeNestedExpression(sizeExpr, analysis, AccessType.READ, false);

        VeraExpression srcExpr = obj.getSourceExpr();
        if (srcExpr != null)
        {
            analyzeNestedExpression(srcExpr, analysis, AccessType.READ, false);
        }
    }

    public void visit(VeraArrayInitializer obj)
    {
        Iterator iter = obj.getElements().iterator();
        while (iter.hasNext())
        {
            VeraExpression elemExpr = (VeraExpression) iter.next();
            analyzeNestedExpression(elemExpr, analysis, AccessType.READ, false);
        }
    }

    public void visit(VeraAssign obj)
    {
        analyzeOperation(obj, ASSIGN_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignAdd obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignAnd obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignAndNot obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignDivide obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignLeftShift obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignModulo obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignMultiply obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignOr obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignOrNot obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignRightShift obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignSubtract obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignXor obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraAssignXorNot obj)
    {
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_ALL, false);
    }

    public void visit(VeraBitSliceAccess obj)
    {
        // treat bit slice writes as read/writes
        VeraExpression arrayExpr = obj.getArray();
        analyzeNestedExpression(arrayExpr, analysis, AccessType.getInstance(
            true, accessType.isWrite()), propagateXZ);

        VeraRange range = obj.getRange();

        VeraExpression highExpr = range.getFrom();
        analyzeNestedExpression(highExpr, analysis, AccessType.READ, false);

        VeraExpression lowExpr = range.getTo();
        analyzeNestedExpression(lowExpr, analysis, AccessType.READ, false);
    }

    public void visit(VeraBitVectorLiteral obj)
    {
        // update X/Z propagation analysis
        if (propagateXZ && obj.getValue().containsXZ())
        {
            analysis.markUncondXZ(XZSourceType.LITERAL);
        }
    }

    public void visit(VeraBitwiseAnd obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseAndNot obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseNegative obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseOr obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseOrNot obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseReverse obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseXor obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraBitwiseXorNot obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraConcatenation obj)
    {
        analyzeOperation(obj, accessType.isWrite() ? ASSIGN_ALL : ASSIGN_NONE,
            XZ_ALL, true);
    }

    public void visit(VeraConditional obj)
    {
        VeraExpression condExpr = obj.getOperand(0);
        analyzeNestedCondition(condExpr, analysis, AccessType.READ, false);

        DADUAnalysis daduBeforeElse = analysis.daduWhenFalse;
        analysis.daduUncond = analysis.daduWhenTrue;

        VeraExpression trueExpr = obj.getOperand(1);
        VeraExpression falseExpr = obj.getOperand(2);
        if (trueExpr.getResultType().isIntegralConvertible()
            && falseExpr.getResultType().isIntegralConvertible())
        {
            analyzeNestedCondition(trueExpr, analysis, AccessType.READ,
                propagateXZ);

            DADUAnalysis daduAfterThenWhenTrue = analysis.daduWhenTrue
                .duplicate();
            DADUAnalysis daduAfterThenWhenFalse = analysis.daduWhenFalse
                .duplicate();
            analysis.daduUncond = daduBeforeElse;

            analyzeNestedCondition(falseExpr, analysis, AccessType.READ,
                propagateXZ);

            analysis.daduWhenTrue.merge(daduAfterThenWhenTrue);
            analysis.daduWhenFalse.merge(daduAfterThenWhenFalse);
        }
        else
        {
            analyzeNestedExpression(trueExpr, analysis, AccessType.READ,
                propagateXZ);

            DADUAnalysis daduAfterThen = analysis.daduUncond.duplicate();
            analysis.daduUncond = daduBeforeElse;

            analyzeNestedExpression(falseExpr, analysis, AccessType.READ,
                propagateXZ);

            analysis.daduUncond.merge(daduAfterThen);
        }
    }

    public void visit(VeraConstraintSet obj)
    {
        // ignored for current analyses
    }

    public void visit(VeraCopyCreation obj)
    {
        analysis.sideEffects = true;

        VeraExpression srcExpr = obj.getSource();
        if (srcExpr != null)
        {
            analyzeNestedExpression(srcExpr, analysis, AccessType.READ, false);
        }
    }

    public void visit(VeraDepthAccess obj)
    {
        VeraExpression signalExpr = obj.getSignal();
        analyzeNestedExpression(signalExpr, analysis, AccessType.READ, false);
    }

    public void visit(VeraDistSet obj)
    {
        // ignored for current analyses
    }

    public void visit(VeraDivide obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_FIRST, true);
    }

    public void visit(VeraEnumValueReference obj)
    {
        // do nothing
    }

    public void visit(VeraEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraExactEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraExactNotEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraFunctionInvocation obj)
    {
        analysis.sideEffects = true;

        VeraExpression funcExpr = obj.getFunction();
        analyzeNestedExpression(funcExpr, analysis, AccessType.READ, false);

        final VeraFunction func;
        boolean inlinedFunc = false;
        VeraVariable thisVar = null;
        VariableAnalysis thisVarAnalysis = null;
        boolean thisWrite = false;
        if (funcExpr instanceof VeraFunctionReference)
        {
            VeraFunctionReference funcRef = (VeraFunctionReference) funcExpr;
            func = funcRef.getFunction();
            String funcName = func.getName().getCanonicalName();
            if (funcName.equals("cast_assign")
                || funcName.equals("assoc_index") || funcName.equals("sprintf"))
            {
                inlinedFunc = true;
            }
        }
        else
        {
            assert (funcExpr instanceof VeraMemberAccess);
            VeraMemberAccess memberExpr = (VeraMemberAccess) funcExpr;
            StructuredTypeMember member = memberExpr.getMember();
            assert (member instanceof VeraMemberFunction);
            func = (VeraMemberFunction) member;

            final VeraExpression thisExpr = memberExpr.getObject();
            final VeraType thisType = thisExpr.getResultType();
            if (thisType instanceof VeraStringType)
            {
                thisVar = getReferencedVariable(thisExpr);
                thisVarAnalysis = analyzer.getOrCreateVariableAnalysis(thisVar,
                    null);
                final String funcID = func.getName().getIdentifier();

                if (funcID.startsWith("get_status")
                    || funcID.equals("prematch") || funcID.equals("postmatch")
                    || funcID.equals("thismatch") || funcID.equals("backref"))
                {
                    thisVarAnalysis.markReadStringState(analyzer);
                }
                else if (funcID.equals("putc") || funcID.equals("match"))
                {
                    thisVarAnalysis.markWriteStringState(analyzer);
                }

                if (funcID.equals("putc") || funcID.equals("itoa")
                    || funcID.equals("bittostr"))
                {
                    thisWrite = true;
                }
            }
        }

        List actualArgs = obj.getArguments();
        analyzeInvokeArgs(actualArgs, func, analysis, propagateXZ, inlinedFunc);

        if (thisWrite)
        {
            processVarAccess(thisVar, thisVarAnalysis, false, true);
        }
    }

    public void visit(VeraFunctionReference obj)
    {
        Function func = obj.getFunction();
        if (func instanceof VeraMemberFunction)
        {
            analysis.referencesInstance = true;
        }
    }

    public void visit(VeraGreater obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraGreaterOrEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraIfElseConstraint obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraImplicationConstraint obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraInSet obj)
    {
        // ignored for current analyses
    }

    public void visit(VeraInstanceCreation obj)
    {
        analysis.sideEffects = true;

        List actualArgs = obj.getArguments();
        if (actualArgs.size() > 0)
        {
            VeraClass cls = (VeraClass) obj.getType();
            Iterator iter = cls.lookupObjects("new", VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                VeraFunction func = (VeraFunction) iter.next();
                assert (!iter.hasNext()) : "Multiple new() tasks found";
                analyzeInvokeArgs(actualArgs, func, analysis, false, false);
            }
            else
            {
                assert false : "new() task not found";
            }
        }
    }

    public void visit(VeraIntegerLiteral obj)
    {
        // do nothing
    }

    public void visit(VeraInterfaceReference obj)
    {
        // do nothing
    }

    public void visit(VeraLeftShift obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_FIRST, true);
    }

    public void visit(VeraLess obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraLessOrEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraLogicalAnd obj)
    {
        VeraExpression lhsExpr = obj.getOperand(0);
        analyzeNestedCondition(lhsExpr, analysis, AccessType.READ, false);

        DADUAnalysis daduWhenFalseLHS = analysis.daduWhenFalse;
        analysis.daduUncond = analysis.daduWhenTrue;

        VeraExpression rhsExpr = obj.getOperand(1);
        analyzeNestedCondition(rhsExpr, analysis, AccessType.READ, false);

        analysis.daduWhenFalse.merge(daduWhenFalseLHS);
    }

    public void visit(VeraLogicalNegative obj)
    {
        VeraExpression expr = obj.getOperand(0);
        analyzeNestedCondition(expr, analysis, AccessType.READ, propagateXZ);

        DADUAnalysis swapTemp = analysis.daduWhenTrue;
        analysis.daduWhenTrue = analysis.daduWhenFalse;
        analysis.daduWhenFalse = swapTemp;
    }

    public void visit(VeraLogicalOr obj)
    {
        VeraExpression lhsExpr = obj.getOperand(0);
        analyzeNestedCondition(lhsExpr, analysis, AccessType.READ, false);

        DADUAnalysis daduWhenTrueLHS = analysis.daduWhenTrue;
        analysis.daduUncond = analysis.daduWhenFalse;

        VeraExpression rhsExpr = obj.getOperand(1);
        analyzeNestedCondition(rhsExpr, analysis, AccessType.READ, false);

        analysis.daduWhenTrue.merge(daduWhenTrueLHS);
    }

    public void visit(VeraMemberAccess obj)
    {
        VeraExpression objExpr = obj.getObject();
        analyzeNestedExpression(objExpr, analysis, AccessType.READ, false);

        StructuredTypeMember member = obj.getMember();
        if (member instanceof VeraMemberVariable)
        {
            VeraMemberVariable memberVar = (VeraMemberVariable) member;
            processVarAccess(memberVar, accessType.isRead(), accessType
                .isWrite());
        }
        else if (propagateXZ
            && (member instanceof VeraInterfaceSignal || member instanceof VeraPortSignal))
        {
            analysis.markUncondXZ(XZSourceType.SIGNAL);
        }
    }

    private boolean sameDir(String path1, String path2)
    {
        File f1 = new File(path1);
        File f2 = new File(path2);
        return f1.getParentFile().equals(f2.getParentFile());
    }

    public void visit(VeraModulo obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_FIRST, true);
    }

    public void visit(VeraMultiply obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraNotAndReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraNotEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraNotInSet obj)
    {
        // ignored for current analyses
    }

    public void visit(VeraNotOrReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraNotXorReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraNullLiteral obj)
    {
        // do nothing
    }

    public void visit(VeraOrReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraPostDecrement obj)
    {
        analysis.postIncDec = true;
        analysis.sideEffects = true;
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_NONE, true);
    }

    public void visit(VeraPostIncrement obj)
    {
        analysis.postIncDec = true;
        analysis.sideEffects = true;
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_NONE, true);
    }

    public void visit(VeraPreDecrement obj)
    {
        analysis.sideEffects = true;
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_NONE, true);
    }

    public void visit(VeraPreIncrement obj)
    {
        analysis.sideEffects = true;
        analyzeOperation(obj, ASSIGN_OP_FIRST, XZ_NONE, true);
    }

    public void visit(VeraReplication obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraRightShift obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_FIRST, true);
    }

    public void visit(VeraSignalReference obj)
    {
        // update X/Z propagation analysis
        if (propagateXZ)
        {
            analysis.markUncondXZ(XZSourceType.SIGNAL);
        }
    }

    public void visit(VeraStringLiteral obj)
    {
        // do nothing
    }

    public void visit(VeraSubtract obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }

    public void visit(VeraSuperReference obj)
    {
        analysis.referencesInstance = true;
    }

    public void visit(VeraSystemClockReference obj)
    {
        // do nothing
    }

    public void visit(VeraThisReference obj)
    {
        analysis.referencesInstance = true;
    }

    public void visit(VeraVariableReference obj)
    {
        final VeraVariable var = obj.getVariable();
        final boolean isRead = accessType.isRead();
        final boolean isWrite = accessType.isWrite();

        // track variable references in block
        analysis.addLocalAccess(var, isRead, isWrite);

        // update expression reference modes analysis
        if (var instanceof VeraMemberVariable)
        {
            if (!var.getModifiers().contains(VeraVariableModifier.STATIC))
            {
                analysis.referencesInstance = true;
            }
        }
        else if (var instanceof VeraLocalVariable)
        {
            analysis.referencesLocalNonArg = true;
        }

        processVarAccess(var, isRead, isWrite);
    }

    private void processVarAccess(
        final VeraVariable var,
        final boolean isRead,
        final boolean isWrite)
    {
        VariableAnalysis varAnalysis = analyzer.getOrCreateVariableAnalysis(
            var, null);
        processVarAccess(var, varAnalysis, isRead, isWrite);
    }

    private void processVarAccess(
        final VeraVariable var,
        final VariableAnalysis varAnalysis,
        final boolean isRead,
        final boolean isWrite)
    {
        // required visibility analysis for fields and globals
        CompilationUnit varCompUnit = null;
        if (var instanceof VeraMemberVariable)
        {
            VeraMemberVariable memberVar = (VeraMemberVariable) var;
            VeraUserClass varCls = (VeraUserClass) memberVar
                .getStructuredType();
            boolean inCtor;
            if (varCls != analysis.cls)
            {
                // isSuperclassOf(null) returns false
                if (!varCls.isSuperclassOf(analysis.cls))
                {
                    varAnalysis.markFieldAccessOutsideSubClass();
                }
                else
                {
                    varAnalysis.markFieldAccessOutsideClass();
                }

                varCompUnit = varCls.getCompilationUnit();
                inCtor = false;
            }
            else
            {
                inCtor = analysis.context instanceof VeraMemberFunction
                    && ((VeraMemberFunction) analysis.context).isConstructor();
            }
            if (isWrite && !inCtor)
            {
                varAnalysis.markFieldWriteOutsideCtor();
            }
        }
        else if (var instanceof VeraCompilationUnitMember)
        {
            VeraCompilationUnitMember member = (VeraCompilationUnitMember) var;
            varCompUnit = member.getCompilationUnit();
        }
        if (varCompUnit != null)
        {
            String varSrcPath = varCompUnit.getSourcePath();
            String srcPath = analysis.compUnit.getSourcePath();
            if (!sameDir(varSrcPath, srcPath))
            {
                varAnalysis.markFieldAccessOutsideDirectory();
            }
        }

        // update unread/unwritten variable analysis
        if (isRead)
        {
            varAnalysis.markReadAccess();
        }
        if (isWrite)
        {
            varAnalysis.markWriteAccess();
        }

        // update variable DA/DU analysis
        final DADUAnalysis dadu = analysis.daduUncond;
        final int varIndex = dadu.getIndex(var);
        final boolean daduTracked = varIndex >= 0;
        if (daduTracked)
        {
            if (isRead)
            {
                if (!dadu.isDA(varIndex))
                {
                    varAnalysis.markReadUnassigned();

                    if (!varAnalysis.isXZKnown())
                    {
                        varAnalysis.markAssignedXZ(XZSourceType.UNINIT, null);
                    }
                }
            }
            if (isWrite)
            {
                if (!dadu.isDU(varIndex))
                {
                    varAnalysis.markWriteAssigned();
                }
                dadu.markAssigned(varIndex);
            }
        }
        else
        {
            assert ((var instanceof VeraMemberVariable && ((VeraMemberVariable) var)
                .getStructuredType() != analysis.cls)
                || var instanceof VeraGlobalVariable || var instanceof VeraBindVariable);
        }

        // update X/Z propagation analysis
        if (propagateXZ)
        {
            analysis.addXZPropagatesFrom(varAnalysis);
        }
    }

    public void visit(VeraVoidLiteral obj)
    {
        // do nothing
    }

    public void visit(VeraWildEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraWildNotEqual obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_NONE, true);
    }

    public void visit(VeraXorReduction obj)
    {
        analyzeOperation(obj, ASSIGN_NONE, XZ_ALL, true);
    }
}
