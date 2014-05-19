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
 * Represents a Vera if-else constraint operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraIfElseConstraint
    extends VeraTernaryOperation
{
    private static final long serialVersionUID = 3689347715246994225L;

    public VeraIfElseConstraint(
        VeraExpression ifExpr,
        VeraExpression thenExpr,
        VeraExpression elseExpr)
    {
        super(ifExpr, thenExpr, elseExpr);
        if (!ifExpr.getResultType().isIntegralConvertible()
            || !thenExpr.getResultType().isIntegralConvertible()
            || (elseExpr != null && !elseExpr.getResultType()
                .isIntegralConvertible()))
        {
            throw new SemanticException("Incorrect operand types");
        }
        setResultType(schema.bitType);
    }

    public VeraExpression getIfExpression()
    {
        return op1;
    }

    public VeraExpression getThenExpression()
    {
        return op2;
    }

    public VeraExpression getElseExpression()
    {
        return op3;
    }

    public Object evaluateConstant()
    {
        boolean b1 = toBoolean(op1.evaluateConstant());
        return b1 ? op2.evaluateConstant() : (op3 != null ? op3
            .evaluateConstant() : Boolean.TRUE);
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
