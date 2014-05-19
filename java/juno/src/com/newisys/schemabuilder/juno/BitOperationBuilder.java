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

import com.newisys.langschema.java.JavaEqual;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaNotEqual;
import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Builds various operation expressions for Bit-type operands.
 * 
 * @author Trevor Robinson
 */
final class BitOperationBuilder
    extends TranslatorModule
    implements ArithmeticOperationBuilder, EqualityOperationBuilder
{
    public BitOperationBuilder(TranslatorModule xlatContext)
    {
        super(xlatContext);
    }

    private JavaExpression doBitOpCall(
        String methodID,
        JavaExpression arg1,
        JavaExpression arg2)
    {
        return ExpressionBuilder.staticCall(types.bitOpType, methodID, arg1,
            arg2);
    }

    public JavaExpression add(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "xor", op2);
    }

    public JavaExpression subtract(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "xor", op2);
    }

    public JavaExpression multiply(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "and", op2);
    }

    public JavaExpression divide(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("divide", op1, op2);
    }

    public JavaExpression mod(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("mod", op1, op2);
    }

    public JavaExpression negate(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "ztox");
    }

    public JavaExpression and(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "and", op2);
    }

    public JavaExpression andNot(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "and", op2), "not");
    }

    public JavaExpression or(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "or", op2);
    }

    public JavaExpression orNot(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "or", op2), "not");
    }

    public JavaExpression xor(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(op1, "xor", op2);
    }

    public JavaExpression xorNot(JavaExpression op1, JavaExpression op2)
    {
        return ExpressionBuilder.memberCall(ExpressionBuilder.memberCall(op1,
            "xor", op2), "not");
    }

    public JavaExpression not(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "not");
    }

    public JavaExpression reverse(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression reductiveAnd(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "ztox");
    }

    public JavaExpression reductiveAndNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "not");
    }

    public JavaExpression reductiveOr(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "ztox");
    }

    public JavaExpression reductiveOrNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "not");
    }

    public JavaExpression reductiveXor(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "ztox");
    }

    public JavaExpression reductiveXorNot(JavaExpression op1)
    {
        return ExpressionBuilder.memberCall(op1, "not");
    }

    public JavaExpression equal(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("equal", op1, op2);
    }

    public JavaExpression notEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("notEqual", op1, op2);
    }

    public JavaExpression exactEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaEqual(schema, op1, op2);
    }

    public JavaExpression exactNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaNotEqual(schema, op1, op2);
    }

    public JavaExpression wildEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("wildEqual", op1, op2);
    }

    public JavaExpression wildNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return doBitOpCall("wildNotEqual", op1, op2);
    }
}
