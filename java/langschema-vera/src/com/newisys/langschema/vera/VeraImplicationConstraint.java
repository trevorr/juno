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
 * Represents a Vera implication constraint operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraImplicationConstraint
    extends VeraBinaryOperation
{
    private static final long serialVersionUID = 3257284729785955639L;

    public VeraImplicationConstraint(
        VeraExpression predicate,
        VeraExpression constraint)
    {
        super(predicate, constraint);
        if (!isValidTypes(predicate.getResultType(), constraint.getResultType()))
        {
            throw new SemanticException("Incorrect operand types");
        }
        setResultType(schema.bitType);
    }

    public VeraExpression getPredicate()
    {
        return op1;
    }

    public VeraExpression getConstraint()
    {
        return op2;
    }

    public Object evaluateConstant()
    {
        boolean b1 = toBoolean(op1.evaluateConstant());
        return b1 ? op2.evaluateConstant() : Boolean.TRUE;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
