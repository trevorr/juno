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
 * Represents a Vera conditional (?:) operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraConditional
    extends VeraTernaryOperation
{
    private static final long serialVersionUID = 3760843484630037299L;

    public VeraConditional(
        VeraExpression op1,
        VeraExpression op2,
        VeraExpression op3)
    {
        super(op1, op2, op3);
        VeraType type2 = op2.getResultType();
        VeraType type3 = op3.getResultType();
        setResultType(getConditionalType(type2, type3));
    }

    private static VeraType getConditionalType(VeraType type1, VeraType type2)
    {
        VeraType result;
        if (type1.isStrictIntegral() && type2.isStrictIntegral())
        {
            result = getCommonIntegralType(type1, type2);
        }
        else if (isRefOrEnumType(type1)
            && (type2.equals(type1) || type2 instanceof VeraNullType))
        {
            result = type1;
        }
        else if (type1 instanceof VeraNullType && isRefOrEnumType(type2))
        {
            result = type2;
        }
        else
        {
            throw new SemanticException("Incompatible types in conditional");
        }
        return result;
    }

    public Object evaluateConstant()
    {
        boolean b1 = toBoolean(op1.evaluateConstant());
        Object result = b1 ? op2.evaluateConstant() : op3.evaluateConstant();
        VeraType resultType = getResultType();
        if (resultType.isStrictIntegral())
        {
            result = toCommonIntegralType(result, resultType);
        }
        return result;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
