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
import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Builds various operation expressions for Integer-type operands.
 * 
 * @author Trevor Robinson
 */
final class IntegerOperationBuilder
    extends TranslatorModule
    implements ArithmeticOperationBuilder, ShiftOperationBuilder,
    EqualityOperationBuilder, RelationalOperationBuilder
{
    public IntegerOperationBuilder(TranslatorModule xlatContext)
    {
        super(xlatContext);
    }

    private JavaExpression doIntegerOpCall(String methodID, JavaExpression op1)
    {
        return ExpressionBuilder.staticCall(types.integerOpType, methodID, op1);
    }

    private JavaExpression doIntegerOpCall(
        String methodID,
        JavaExpression op1,
        JavaExpression op2)
    {
        return ExpressionBuilder.staticCall(types.integerOpType, methodID, op1,
            op2);
    }

    public JavaExpression add(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("add", op1, op2);
    }

    public JavaExpression subtract(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("subtract", op1, op2);
    }

    public JavaExpression multiply(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("multiply", op1, op2);
    }

    public JavaExpression divide(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("divide", op1, op2);
    }

    public JavaExpression mod(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("mod", op1, op2);
    }

    public JavaExpression negate(JavaExpression op1)
    {
        return doIntegerOpCall("negate", op1);
    }

    public JavaExpression and(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseAnd", op1, op2);
    }

    public JavaExpression andNot(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseAndNot", op1, op2);
    }

    public JavaExpression or(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseOr", op1, op2);
    }

    public JavaExpression orNot(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseOrNot", op1, op2);
    }

    public JavaExpression xor(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseXor", op1, op2);
    }

    public JavaExpression xorNot(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("bitwiseXorNot", op1, op2);
    }

    public JavaExpression not(JavaExpression op1)
    {
        return doIntegerOpCall("bitwiseNot", op1);
    }

    public JavaExpression reverse(JavaExpression op1)
    {
        return doIntegerOpCall("bitwiseReverse", op1);
    }

    public JavaExpression reductiveAnd(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveAnd", op1);
    }

    public JavaExpression reductiveAndNot(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveAndNot", op1);
    }

    public JavaExpression reductiveOr(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveOr", op1);
    }

    public JavaExpression reductiveOrNot(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveOrNot", op1);
    }

    public JavaExpression reductiveXor(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveXor", op1);
    }

    public JavaExpression reductiveXorNot(JavaExpression op1)
    {
        return doIntegerOpCall("reductiveXorNot", op1);
    }

    public JavaExpression shiftLeft(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("shiftLeft", op1, op2);
    }

    public JavaExpression shiftRight(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("shiftRight", op1, op2);
    }

    public JavaExpression equal(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("equal", op1, op2);
    }

    public JavaExpression notEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("notEqual", op1, op2);
    }

    public JavaExpression exactEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("exactEqual", op1, op2);
    }

    public JavaExpression exactNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("exactNotEqual", op1, op2);
    }

    public JavaExpression wildEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("wildEqual", op1, op2);
    }

    public JavaExpression wildNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("wildNotEqual", op1, op2);
    }

    public JavaExpression greater(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("greater", op1, op2);
    }

    public JavaExpression greaterOrEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("greaterOrEqual", op1, op2);
    }

    public JavaExpression less(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("less", op1, op2);
    }

    public JavaExpression lessOrEqual(JavaExpression op1, JavaExpression op2)
    {
        return doIntegerOpCall("lessOrEqual", op1, op2);
    }
}
