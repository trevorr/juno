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

import com.newisys.langschema.Literal;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaLogicalNot;
import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Builds equality operation expressions using Object.equals() (or in some
 * cases, Vera.equals()).
 * 
 * @author Trevor Robinson
 */
final class EqualsEqualityOperationBuilder
    extends TranslatorModule
    implements EqualityOperationBuilder
{
    private final boolean checkNull;

    public EqualsEqualityOperationBuilder(TranslatorModule xlatContext)
    {
        this(xlatContext, false);
    }

    public EqualsEqualityOperationBuilder(
        TranslatorModule xlatContext,
        boolean checkNull)
    {
        super(xlatContext);
        this.checkNull = checkNull;
    }

    public JavaExpression equal(JavaExpression op1, JavaExpression op2)
    {
        if (checkNull && !(op1 instanceof Literal))
        {
            return ExpressionBuilder.staticCall(types.junoType, "equals", op1,
                op2);
        }
        return ExpressionBuilder.memberCall(op1, "equals", op2);
    }

    public JavaExpression notEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaLogicalNot(schema, equal(op1, op2));
    }

    public JavaExpression exactEqual(JavaExpression op1, JavaExpression op2)
    {
        return equal(op1, op2);
    }

    public JavaExpression exactNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return notEqual(op1, op2);
    }

    public JavaExpression wildEqual(JavaExpression op1, JavaExpression op2)
    {
        return equal(op1, op2);
    }

    public JavaExpression wildNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return notEqual(op1, op2);
    }
}
