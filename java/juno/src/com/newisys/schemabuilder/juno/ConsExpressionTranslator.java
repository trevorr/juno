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

import java.util.Iterator;

import com.newisys.langschema.constraint.*;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaStructuredTypeMember;
import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.vera.*;
import com.newisys.verilog.util.BitVector;

/**
 * Schema translator for random constraint expressions.
 * 
 * @author Trevor Robinson
 */
final class ConsExpressionTranslator
    extends TranslatorModule
    implements VeraExpressionVisitor
{
    private final ConsSchema consSchema;
    private ConsExpression result;

    public ConsExpressionTranslator(TranslatorModule xlatContext)
    {
        super(xlatContext);
        consSchema = new ConsSchema();
    }

    public ConsExpression getResult()
    {
        return result;
    }

    private ConsExpression translateNestedExpr(VeraExpression obj)
    {
        obj.accept(this);
        return result;
    }

    public void visit(VeraAdd obj)
    {
        result = new ConsAdd(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraAndReduction obj)
    {
        // DEFERRED: VeraAndReduction
        throw new UnsupportedOperationException();
    }

    public void visit(VeraArithmeticNegative obj)
    {
        result = new ConsUnaryMinus(translateNestedExpr(obj.getOperand(0)));
    }

    public void visit(VeraArrayAccess obj)
    {
        ConsArrayAccess caa = new ConsArrayAccess(translateNestedExpr(obj
            .getArray()));
        Iterator iter = obj.getIndices().iterator();
        while (iter.hasNext())
        {
            VeraExpression expr = (VeraExpression) iter.next();
            caa.addIndex(translateNestedExpr(expr));
        }
        result = caa;
    }

    public void visit(VeraArrayCreation obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraArrayInitializer obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssign obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignAdd obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignAnd obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignAndNot obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignDivide obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignLeftShift obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignModulo obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignMultiply obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignOr obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignOrNot obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignRightShift obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignSubtract obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignXor obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraAssignXorNot obj)
    {
        // assignment not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraBitSliceAccess obj)
    {
        // DEFERRED: VeraBitSliceAccess
        throw new UnsupportedOperationException();
    }

    public void visit(VeraBitVectorLiteral obj)
    {
        BitVector value = obj.getValue();
        // X/Z constants not allowed in constraints
        assert (!value.containsXZ());
        result = new ConsBitVectorLiteral(consSchema, value);
    }

    public void visit(VeraBitwiseAnd obj)
    {
        result = new ConsAnd(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraBitwiseAndNot obj)
    {
        result = new ConsBitwiseNot(new ConsAnd(translateNestedExpr(obj
            .getOperand(0)), translateNestedExpr(obj.getOperand(1))));
    }

    public void visit(VeraBitwiseNegative obj)
    {
        result = new ConsBitwiseNot(translateNestedExpr(obj.getOperand(0)));
    }

    public void visit(VeraBitwiseOr obj)
    {
        result = new ConsOr(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraBitwiseOrNot obj)
    {
        result = new ConsBitwiseNot(new ConsOr(translateNestedExpr(obj
            .getOperand(0)), translateNestedExpr(obj.getOperand(1))));
    }

    public void visit(VeraBitwiseReverse obj)
    {
        // DEFERRED: VeraBitwiseReverse
        throw new UnsupportedOperationException();
    }

    public void visit(VeraBitwiseXor obj)
    {
        result = new ConsXor(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraBitwiseXorNot obj)
    {
        result = new ConsBitwiseNot(new ConsXor(translateNestedExpr(obj
            .getOperand(0)), translateNestedExpr(obj.getOperand(1))));
    }

    public void visit(VeraConcatenation obj)
    {
        // DEFERRED: VeraConcatenation
        throw new UnsupportedOperationException();
    }

    public void visit(VeraConditional obj)
    {
        VeraExpression veraOp2 = obj.getOperand(1);
        VeraExpression veraOp3 = obj.getOperand(2);
        ConsExpression op1 = translateNestedExpr(obj.getOperand(0));
        if (veraOp3 instanceof VeraVoidLiteral)
        {
            // a ? b : void -> a => b
            result = new ConsImplication(op1, translateNestedExpr(veraOp2));
        }
        if (veraOp2 instanceof VeraVoidLiteral)
        {
            // a ? void : b -> !a => b
            result = new ConsImplication(new ConsLogicalNot(op1),
                translateNestedExpr(veraOp3));
        }
        else
        {
            result = new ConsConditional(op1, translateNestedExpr(veraOp2),
                translateNestedExpr(veraOp3));
        }
    }

    public void visit(VeraConstraintSet obj)
    {
        final ConsConstraintSet ccs = new ConsConstraintSet(consSchema);
        final Iterator iter = obj.getExprs().iterator();
        while (iter.hasNext())
        {
            VeraExpression expr = (VeraExpression) iter.next();
            ccs.addExpr(translateNestedExpr(expr));
        }
        result = ccs;
    }

    public void visit(VeraCopyCreation obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraDepthAccess obj)
    {
        // DEFERRED: VeraDepthAccess
        throw new UnsupportedOperationException();
    }

    public void visit(VeraDistSet obj)
    {
        final ConsDistSet cds = new ConsDistSet(translateNestedExpr(obj
            .getExpr()));
        translateSetMembers(obj, cds);
        result = cds;
    }

    public void visit(VeraDivide obj)
    {
        result = new ConsDivide(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraEnumValueReference obj)
    {
        final VeraEnumerationElement veraElem = obj.getElement();
        final JavaMemberVariable var = translateEnumElement(veraElem);
        result = new ConsVariableReference(consSchema, var);
    }

    public void visit(VeraEqual obj)
    {
        result = new ConsEqual(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraExactEqual obj)
    {
        // 4-state operators not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraExactNotEqual obj)
    {
        // 4-state operators not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraFunctionInvocation obj)
    {
        // DEFERRED: VeraFunctionInvocation
        throw new UnsupportedOperationException();
    }

    public void visit(VeraFunctionReference obj)
    {
        // DEFERRED: VeraFunctionReference
        throw new UnsupportedOperationException();
    }

    public void visit(VeraGreater obj)
    {
        result = new ConsGreater(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraGreaterOrEqual obj)
    {
        result = new ConsGreaterOrEqual(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraIfElseConstraint obj)
    {
        final ConsExpression ifExpr = translateNestedExpr(obj.getIfExpression());
        final ConsExpression thenExpr = translateNestedExpr(obj
            .getThenExpression());
        final VeraExpression veraElseExpr = obj.getElseExpression();
        if (veraElseExpr != null)
        {
            // "if (a) b; else c;" -> "{ a => b; !a => c; }"
            final ConsConstraintSet ccs = new ConsConstraintSet(consSchema);
            ccs.addExpr(new ConsImplication(ifExpr, thenExpr));
            ccs.addExpr(new ConsImplication(new ConsLogicalNot(ifExpr),
                translateNestedExpr(veraElseExpr)));
            result = ccs;
        }
        else
        {
            // "if (a) b;" -> "a => b;"
            result = new ConsImplication(ifExpr, thenExpr);
        }
    }

    public void visit(VeraImplicationConstraint obj)
    {
        result = new ConsImplication(translateNestedExpr(obj.getPredicate()),
            translateNestedExpr(obj.getConstraint()));
    }

    public void visit(VeraInSet obj)
    {
        final ConsInSet cis = new ConsInSet(translateNestedExpr(obj.getExpr()));
        translateSetMembers(obj, cis);
        result = cis;
    }

    private void translateSetMembers(
        VeraSetOperation veraSetOp,
        ConsSetOperation consSetOp)
    {
        final Iterator iter = veraSetOp.getMembers().iterator();
        while (iter.hasNext())
        {
            // translate next member
            final VeraSetMember veraMember = (VeraSetMember) iter.next();
            final ConsSetMember member;
            if (veraMember instanceof VeraSetValue)
            {
                VeraSetValue veraValue = (VeraSetValue) veraMember;
                member = new ConsSetValue(translateNestedExpr(veraValue
                    .getValue()));
            }
            else
            {
                VeraSetRange veraRange = (VeraSetRange) veraMember;
                member = new ConsSetRange(translateNestedExpr(veraRange
                    .getLow()), translateNestedExpr(veraRange.getHigh()));
            }

            // translate weight if present (i.e. dist operator)
            final VeraExpression veraWeight = veraMember.getWeight();
            if (veraWeight != null)
            {
                member.setWeight(translateNestedExpr(veraWeight), veraMember
                    .isWeightPerItem());
            }

            consSetOp.addMember(member);
        }
    }

    public void visit(VeraInstanceCreation obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraIntegerLiteral obj)
    {
        result = new ConsIntLiteral(consSchema, obj.getValue());
    }

    public void visit(VeraInterfaceReference obj)
    {
        // DEFERRED: VeraInterfaceReference
        throw new UnsupportedOperationException();
    }

    public void visit(VeraLeftShift obj)
    {
        result = new ConsLeftShift(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraLess obj)
    {
        result = new ConsLess(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraLessOrEqual obj)
    {
        result = new ConsLessOrEqual(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraLogicalAnd obj)
    {
        result = new ConsConditionalAnd(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraLogicalNegative obj)
    {
        result = new ConsLogicalNot(translateNestedExpr(obj.getOperand(0)));
    }

    public void visit(VeraLogicalOr obj)
    {
        result = new ConsConditionalOr(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraMemberAccess obj)
    {
        // translate object expression
        final VeraExpression veraObj = obj.getObject();
        final ConsExpression consObj = translateNestedExpr(veraObj);

        // translate member
        final VeraStructuredTypeMember veraMember = obj.getMember();
        final JavaStructuredTypeMember member = translateMember(veraMember);

        result = new ConsMemberAccess(consObj, member);
    }

    public void visit(VeraModulo obj)
    {
        result = new ConsModulo(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraMultiply obj)
    {
        result = new ConsMultiply(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraNotAndReduction obj)
    {
        // DEFERRED: VeraNotAndReduction
        throw new UnsupportedOperationException();
    }

    public void visit(VeraNotEqual obj)
    {
        result = new ConsNotEqual(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraNotInSet obj)
    {
        final ConsNotInSet cnis = new ConsNotInSet(translateNestedExpr(obj
            .getExpr()));
        translateSetMembers(obj, cnis);
        result = cnis;
    }

    public void visit(VeraNotOrReduction obj)
    {
        // DEFERRED: VeraNotOrReduction
        throw new UnsupportedOperationException();
    }

    public void visit(VeraNotXorReduction obj)
    {
        // DEFERRED: VeraNotXorReduction
        throw new UnsupportedOperationException();
    }

    public void visit(VeraNullLiteral obj)
    {
        result = new ConsNullLiteral(consSchema);
    }

    public void visit(VeraOrReduction obj)
    {
        // DEFERRED: VeraOrReduction
        throw new UnsupportedOperationException();
    }

    public void visit(VeraPostDecrement obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraPostIncrement obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraPreDecrement obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraPreIncrement obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraReplication obj)
    {
        // DEFERRED: VeraReplication
        throw new UnsupportedOperationException();
    }

    public void visit(VeraRightShift obj)
    {
        final VeraExpression veraOp1 = obj.getOperand(0);
        final ConsExpression op1 = translateNestedExpr(veraOp1);
        final ConsExpression op2 = translateNestedExpr(obj.getOperand(1));
        if (veraOp1.getResultType() instanceof VeraIntegerType)
        {
            result = new ConsSignedRightShift(op1, op2);
        }
        else
        {
            result = new ConsUnsignedRightShift(op1, op2);
        }
    }

    public void visit(VeraSignalReference obj)
    {
        // DEFERRED: VeraSignalReference
        throw new UnsupportedOperationException();
    }

    public void visit(VeraStringLiteral obj)
    {
        result = new ConsStringLiteral(consSchema, obj.getValue());
    }

    public void visit(VeraSubtract obj)
    {
        result = new ConsSubtract(translateNestedExpr(obj.getOperand(0)),
            translateNestedExpr(obj.getOperand(1)));
    }

    public void visit(VeraSuperReference obj)
    {
        final VeraClass veraType = obj.getType();
        final JavaRawAbstractClass type = (JavaRawAbstractClass) translateType(veraType);
        result = new ConsSuperReference(consSchema, type);
    }

    public void visit(VeraSystemClockReference obj)
    {
        // not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraThisReference obj)
    {
        final VeraClass veraType = obj.getType();
        final JavaRawAbstractClass type = (JavaRawAbstractClass) translateType(veraType);
        result = new ConsThisReference(consSchema, type);
    }

    public void visit(VeraVariableReference obj)
    {
        final VeraVariable veraVar = obj.getVariable();
        final JavaVariable var = translateVariable(veraVar);
        result = new ConsVariableReference(consSchema, var);
    }

    public void visit(VeraVoidLiteral obj)
    {
        // 'void' only valid in conditional (?:) operator
        throw new UnsupportedOperationException();
    }

    public void visit(VeraWildEqual obj)
    {
        // 4-state operators not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraWildNotEqual obj)
    {
        // 4-state operators not allowed in constraints
        throw new UnsupportedOperationException();
    }

    public void visit(VeraXorReduction obj)
    {
        // DEFERRED: VeraXorReduction
        throw new UnsupportedOperationException();
    }
}
