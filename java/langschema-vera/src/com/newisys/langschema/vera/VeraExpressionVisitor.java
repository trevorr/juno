/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.langschema.vera;

/**
 * Visitor over Vera expressions.
 * 
 * @author Trevor Robinson
 */
public interface VeraExpressionVisitor
{
    void visit(VeraAdd obj);

    void visit(VeraAndReduction obj);

    void visit(VeraArithmeticNegative obj);

    void visit(VeraArrayAccess obj);

    void visit(VeraArrayCreation obj);

    void visit(VeraArrayInitializer obj);

    void visit(VeraAssign obj);

    void visit(VeraAssignAdd obj);

    void visit(VeraAssignAnd obj);

    void visit(VeraAssignAndNot obj);

    void visit(VeraAssignDivide obj);

    void visit(VeraAssignLeftShift obj);

    void visit(VeraAssignModulo obj);

    void visit(VeraAssignMultiply obj);

    void visit(VeraAssignOr obj);

    void visit(VeraAssignOrNot obj);

    void visit(VeraAssignRightShift obj);

    void visit(VeraAssignSubtract obj);

    void visit(VeraAssignXor obj);

    void visit(VeraAssignXorNot obj);

    void visit(VeraBitSliceAccess obj);

    void visit(VeraBitVectorLiteral obj);

    void visit(VeraBitwiseAnd obj);

    void visit(VeraBitwiseAndNot obj);

    void visit(VeraBitwiseNegative obj);

    void visit(VeraBitwiseOr obj);

    void visit(VeraBitwiseOrNot obj);

    void visit(VeraBitwiseReverse obj);

    void visit(VeraBitwiseXor obj);

    void visit(VeraBitwiseXorNot obj);

    void visit(VeraConcatenation obj);

    void visit(VeraConditional obj);

    void visit(VeraConstraintSet obj);

    void visit(VeraCopyCreation obj);

    void visit(VeraDepthAccess obj);

    void visit(VeraDistSet obj);

    void visit(VeraDivide obj);

    void visit(VeraEnumValueReference obj);

    void visit(VeraEqual obj);

    void visit(VeraExactEqual obj);

    void visit(VeraExactNotEqual obj);

    void visit(VeraFunctionInvocation obj);

    void visit(VeraFunctionReference obj);

    void visit(VeraGreater obj);

    void visit(VeraGreaterOrEqual obj);

    void visit(VeraIfElseConstraint obj);

    void visit(VeraImplicationConstraint obj);

    void visit(VeraInSet obj);

    void visit(VeraInstanceCreation obj);

    void visit(VeraIntegerLiteral obj);

    void visit(VeraInterfaceReference obj);

    void visit(VeraLeftShift obj);

    void visit(VeraLess obj);

    void visit(VeraLessOrEqual obj);

    void visit(VeraLogicalAnd obj);

    void visit(VeraLogicalNegative obj);

    void visit(VeraLogicalOr obj);

    void visit(VeraMemberAccess obj);

    void visit(VeraModulo obj);

    void visit(VeraMultiply obj);

    void visit(VeraNotAndReduction obj);

    void visit(VeraNotEqual obj);

    void visit(VeraNotInSet obj);

    void visit(VeraNotOrReduction obj);

    void visit(VeraNotXorReduction obj);

    void visit(VeraNullLiteral obj);

    void visit(VeraOrReduction obj);

    void visit(VeraPostDecrement obj);

    void visit(VeraPostIncrement obj);

    void visit(VeraPreDecrement obj);

    void visit(VeraPreIncrement obj);

    void visit(VeraReplication obj);

    void visit(VeraRightShift obj);

    void visit(VeraSignalReference obj);

    void visit(VeraStringLiteral obj);

    void visit(VeraSubtract obj);

    void visit(VeraSuperReference obj);

    void visit(VeraSystemClockReference obj);

    void visit(VeraThisReference obj);

    void visit(VeraVariableReference obj);

    void visit(VeraVoidLiteral obj);

    void visit(VeraWildEqual obj);

    void visit(VeraWildNotEqual obj);

    void visit(VeraXorReduction obj);
}
