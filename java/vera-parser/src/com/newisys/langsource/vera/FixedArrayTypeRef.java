/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.langsource.vera;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Fixed array type reference.
 * 
 * @author Trevor Robinson
 */
public final class FixedArrayTypeRef
    extends ArrayTypeRef
{
    private List dimensions; // List<ExpressionDecl>

    public FixedArrayTypeRef(TypeRef elementTypeRef)
    {
        super(ArrayKind.FIXED, elementTypeRef);
        this.dimensions = new LinkedList();
    }

    public List getDimensions()
    {
        return dimensions;
    }

    public void addDimension(ExpressionDecl expr)
    {
        dimensions.add(expr);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer(elementTypeRef.toString());
        Iterator iter = dimensions.iterator();
        while (iter.hasNext())
        {
            buf.append('[');
            buf.append(iter.next());
            buf.append(']');
        }
        return buf.toString();
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
