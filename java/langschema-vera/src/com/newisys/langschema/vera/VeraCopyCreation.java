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

import com.newisys.langschema.CopyCreation;

/**
 * Represents a Vera copy-creation expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraCopyCreation
    extends VeraExpression
    implements CopyCreation
{
    private static final long serialVersionUID = 3976739159731681333L;

    private final VeraComplexType type;
    private final VeraExpression source;

    public VeraCopyCreation(VeraComplexType type, VeraExpression source)
    {
        super(type.schema);
        assert (type instanceof VeraClass || type instanceof VeraPortType);
        setResultType(type);
        this.type = type;
        this.source = source;
    }

    public boolean isConstant()
    {
        return false;
    }

    public VeraComplexType getType()
    {
        return type;
    }

    public VeraExpression getSource()
    {
        return source;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
