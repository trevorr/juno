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

import com.newisys.langschema.java.JavaAnd;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaLogicalNot;
import com.newisys.langschema.java.JavaOr;
import com.newisys.langschema.java.JavaXor;
import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Builds various operation expressions for boolean-type operands.
 * 
 * @author Trevor Robinson
 */
final class BooleanOperationBuilder
    extends TranslatorModule
    implements ArithmeticOperationBuilder
{
    public BooleanOperationBuilder(TranslatorModule xlatContext)
    {
        super(xlatContext);
    }

    private JavaExpression doBooleanOpCall(
        String methodID,
        JavaExpression op1,
        JavaExpression op2)
    {
        return ExpressionBuilder.staticCall(types.booleanOpType, methodID, op1,
            op2);
    }

    public JavaExpression add(JavaExpression op1, JavaExpression op2)
    {
        return new JavaXor(schema, op1, op2);
    }

    public JavaExpression subtract(JavaExpression op1, JavaExpression op2)
    {
        return new JavaXor(schema, op1, op2);
    }

    public JavaExpression multiply(JavaExpression op1, JavaExpression op2)
    {
        return new JavaAnd(schema, op1, op2);
    }

    public JavaExpression divide(JavaExpression op1, JavaExpression op2)
    {
        return doBooleanOpCall("divide", op1, op2);
    }

    public JavaExpression mod(JavaExpression op1, JavaExpression op2)
    {
        return doBooleanOpCall("mod", op1, op2);
    }

    public JavaExpression negate(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression and(JavaExpression op1, JavaExpression op2)
    {
        return new JavaAnd(schema, op1, op2);
    }

    public JavaExpression andNot(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, new JavaAnd(schema, op1, op2));
    }

    public JavaExpression or(JavaExpression op1, JavaExpression op2)
    {
        return new JavaOr(schema, op1, op2);
    }

    public JavaExpression orNot(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, new JavaOr(schema, op1, op2));
    }

    public JavaExpression xor(JavaExpression op1, JavaExpression op2)
    {
        return new JavaXor(schema, op1, op2);
    }

    public JavaExpression xorNot(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, new JavaXor(schema, op1, op2));
    }

    public JavaExpression not(JavaExpression op1)
    {
        return new JavaLogicalNot(schema, op1);
    }

    public JavaExpression reverse(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression reductiveAnd(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression reductiveAndNot(JavaExpression op1)
    {
        return new JavaLogicalNot(schema, op1);
    }

    public JavaExpression reductiveOr(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression reductiveOrNot(JavaExpression op1)
    {
        return new JavaLogicalNot(schema, op1);
    }

    public JavaExpression reductiveXor(JavaExpression op1)
    {
        return op1;
    }

    public JavaExpression reductiveXorNot(JavaExpression op1)
    {
        return new JavaLogicalNot(schema, op1);
    }
}
