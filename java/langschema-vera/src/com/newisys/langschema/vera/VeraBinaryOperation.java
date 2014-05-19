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
 * Base class for Vera binary operations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraBinaryOperation
    extends VeraOperation
{
    protected final VeraExpression op1;
    protected final VeraExpression op2;

    public VeraBinaryOperation(VeraExpression op1, VeraExpression op2)
    {
        super(op1.schema);
        this.op1 = op1;
        operands.add(op1);
        this.op2 = op2;
        operands.add(op2);
    }

    protected static boolean isValidTypes(VeraType type1, VeraType type2)
    {
        return type1.isIntegralConvertible() && type2.isIntegralConvertible();
    }

    public boolean isConstant()
    {
        return op1.isConstant() && op2.isConstant();
    }
}
