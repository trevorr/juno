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

import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaFunctionInvocation;
import com.newisys.langschema.java.JavaLogicalNot;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Builds various operation expressions for BitVector-type operands.
 * 
 * @author Trevor Robinson
 */
final class BitVectorOperationBuilder
    extends TranslatorModule
    implements ArithmeticOperationBuilder, ShiftOperationBuilder,
    EqualityOperationBuilder, RelationalOperationBuilder
{
    public BitVectorOperationBuilder(TranslatorModule xlatContext)
    {
        super(xlatContext);
    }

    public JavaExpression memberOpSame(JavaExpression op1, String methodID)
    {
        JavaFunctionInvocation invoke = ExpressionBuilder.memberCall(op1,
            methodID);
        JavaType type1 = op1.getResultType();
        invoke.setResultType(type1);
        return invoke;
    }

    public JavaExpression memberOpFirst(
        JavaExpression op1,
        String methodID,
        JavaExpression op2)
    {
        JavaFunctionInvocation invoke = ExpressionBuilder.memberCall(op1,
            methodID, op2);
        JavaType type1 = op1.getResultType();
        invoke.setResultType(type1);
        return invoke;
    }

    public JavaExpression memberOpLargest(
        JavaExpression op1,
        String methodID,
        JavaExpression op2)
    {
        JavaFunctionInvocation invoke = ExpressionBuilder.memberCall(op1,
            methodID, op2);
        JavaType type1 = op1.getResultType();
        JavaType type2 = op2.getResultType();
        int len1 = schema.getBitVectorSize(type1);
        int len2 = schema.getBitVectorSize(type2);
        if (len1 > 0 && len2 > 0)
        {
            invoke.setResultType(len2 > len1 ? type2 : type1);
        }
        return invoke;
    }

    private JavaExpression doBitVectorOpCall(
        String methodID,
        JavaExpression op1,
        JavaExpression op2)
    {
        return ExpressionBuilder.staticCall(types.bitVectorOpType, methodID,
            op1, op2);
    }

    public JavaExpression add(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "add", op2);
    }

    public JavaExpression subtract(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "subtract", op2);
    }

    public JavaExpression multiply(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "multiply", op2);
    }

    public JavaExpression divide(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "divide", op2);
    }

    public JavaExpression mod(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "mod", op2);
    }

    public JavaExpression negate(JavaExpression op1)
    {
        return memberOpSame(op1, "negate");
    }

    public JavaExpression and(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "and", op2);
    }

    public JavaExpression andNot(JavaExpression op1, JavaExpression op2)
    {
        return memberOpSame(memberOpLargest(op1, "and", op2), "not");
    }

    public JavaExpression or(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "or", op2);
    }

    public JavaExpression orNot(JavaExpression op1, JavaExpression op2)
    {
        return memberOpSame(memberOpLargest(op1, "or", op2), "not");
    }

    public JavaExpression xor(JavaExpression op1, JavaExpression op2)
    {
        return memberOpLargest(op1, "xor", op2);
    }

    public JavaExpression xorNot(JavaExpression op1, JavaExpression op2)
    {
        return memberOpSame(memberOpLargest(op1, "xor", op2), "not");
    }

    public JavaExpression not(JavaExpression op1)
    {
        return memberOpSame(op1, "not");
    }

    public JavaExpression reverse(JavaExpression op1)
    {
        return memberOpSame(op1, "reverse");
    }

    public JavaExpression reductiveAnd(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "reductiveAnd");
    }

    public JavaExpression reductiveAndNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "reductiveAnd"), "not");
    }

    public JavaExpression reductiveOr(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "reductiveOr");
    }

    public JavaExpression reductiveOrNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "reductiveOr"), "not");
    }

    public JavaExpression reductiveXor(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "reductiveXor");
    }

    public JavaExpression reductiveXorNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "reductiveXor"), "not");
    }

    public JavaExpression shiftLeft(JavaExpression op1, JavaExpression op2)
    {
        return memberOpFirst(op1, "shiftLeft", op2);
    }

    public JavaExpression shiftRight(JavaExpression op1, JavaExpression op2)
    {
        return memberOpFirst(op1, "shiftRight", op2);
    }

    public JavaExpression equal(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("equal", op1, op2);
    }

    public JavaExpression notEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("notEqual", op1, op2);
    }

    public JavaExpression exactEqual(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "equalsExact", op2);
    }

    public JavaExpression exactNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, ExpressionBuilder.memberCall(op1,
            "equalsExact", op2));
    }

    public JavaExpression wildEqual(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "equalsWild", op2);
    }

    public JavaExpression wildNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, ExpressionBuilder.memberCall(op1,
            "equalsWild", op2));
    }

    public JavaExpression greater(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("greater", op1, op2);
    }

    public JavaExpression greaterOrEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("greaterOrEqual", op1, op2);
    }

    public JavaExpression less(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("less", op1, op2);
    }

    public JavaExpression lessOrEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitVectorOpCall("lessOrEqual", op1, op2);
    }
}
