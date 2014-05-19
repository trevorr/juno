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

/**
 * Builds equality operation expressions using the Java operators (== and !=).
 * 
 * @author Trevor Robinson
 */
final class PrimitiveEqualityOperationBuilder
    extends TranslatorModule
    implements EqualityOperationBuilder
{
    public PrimitiveEqualityOperationBuilder(TranslatorModule xlatContext)
    {
        super(xlatContext);
    }

    public JavaExpression equal(JavaExpression op1, JavaExpression op2)
    {
        return new JavaEqual(schema, op1, op2);
    }

    public JavaExpression notEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaNotEqual(schema, op1, op2);
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
        return new JavaEqual(schema, op1, op2);
    }

    public JavaExpression wildNotEqual(JavaExpression op1, JavaExpression op2)
    {
        return new JavaNotEqual(schema, op1, op2);
    }
}
