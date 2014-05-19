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

import com.newisys.langschema.ArrayInitializer;

/**
 * Represents a Vera array initializer expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraArrayInitializer
    extends VeraExpression
    implements ArrayInitializer
{
    private static final long serialVersionUID = 4048796745229939504L;

    private final List<VeraExpression> elements = new LinkedList<VeraExpression>();

    public VeraArrayInitializer(VeraArrayType type)
    {
        super(type.schema);
        setResultType(type);
    }

    public List<VeraExpression> getElements()
    {
        return elements;
    }

    public void addElement(VeraExpression elem)
    {
        elements.add(elem);
    }

    public boolean isConstant()
    {
        return false;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
