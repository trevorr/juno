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

import com.newisys.langschema.util.SemanticException;

/**
 * Base class for Vera assignment operations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraAssignment
    extends VeraBinaryOperation
{
    private static final long serialVersionUID = -8161227644812184570L;

    public VeraAssignment(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
        if (!op1.isAssignable())
        {
            throw new SemanticException(
                "Left-hand expression is not assignable");
        }
        setResultType(schema.voidType);
    }

    protected static boolean isValidTypes(VeraType type1, VeraType type2)
    {
        return type1.isAssignableFrom(type2);
    }

    public boolean isConstant()
    {
        return false;
    }
}
