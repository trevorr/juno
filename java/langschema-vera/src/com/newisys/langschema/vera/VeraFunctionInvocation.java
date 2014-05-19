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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.FunctionInvocation;
import com.newisys.langschema.util.SemanticException;

/**
 * Represents a Vera function invocation expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraFunctionInvocation
    extends VeraExpression
    implements FunctionInvocation
{
    private static final long serialVersionUID = 3257283651749163573L;

    private final VeraExpression function;
    private final List<VeraExpression> arguments = new LinkedList<VeraExpression>();
    private final List<VeraExpression> constraints = new LinkedList<VeraExpression>();

    public VeraFunctionInvocation(VeraExpression function)
    {
        super(function.schema);
        VeraType resultType = function.getResultType();
        if (!(resultType instanceof VeraFunctionType))
        {
            throw new SemanticException("Function reference expected");
        }
        VeraFunctionType funcType = (VeraFunctionType) resultType;
        setResultType(funcType.getReturnType());
        this.function = function;
    }

    public boolean isConstant()
    {
        return false;
    }

    public VeraExpression getFunction()
    {
        return function;
    }

    public List<VeraExpression> getArguments()
    {
        return arguments;
    }

    public void addArgument(VeraExpression expr)
    {
        arguments.add(expr);
    }

    public List<VeraExpression> getConstraints()
    {
        return constraints;
    }

    public void addConstraint(VeraExpression expr)
    {
        constraints.add(expr);
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
