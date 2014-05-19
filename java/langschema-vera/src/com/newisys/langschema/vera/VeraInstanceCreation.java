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

import com.newisys.langschema.InstanceCreation;

/**
 * Represents a Vera instance creation expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraInstanceCreation
    extends VeraExpression
    implements InstanceCreation
{
    private static final long serialVersionUID = 3257850986847679796L;

    private final VeraComplexType type;
    private final List<VeraExpression> arguments = new LinkedList<VeraExpression>();

    public VeraInstanceCreation(VeraComplexType type)
    {
        super(type.schema);
        assert (type instanceof VeraClass || type instanceof VeraPortType);
        setResultType(type);
        this.type = type;
    }

    public boolean isConstant()
    {
        return false;
    }

    public VeraComplexType getType()
    {
        return type;
    }

    public List<VeraExpression> getArguments()
    {
        return arguments;
    }

    public void addArgument(VeraExpression expr)
    {
        arguments.add(expr);
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
