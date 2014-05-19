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
 * Array access expression.
 * 
 * @author Trevor Robinson
 */
public final class ArrayAccessDecl
    extends ExpressionDecl
{
    private ExpressionDecl arrayExpr;
    private List indexExprs; // List<ExpressionDecl>

    public ArrayAccessDecl(ExpressionDecl arrayExpr)
    {
        this.arrayExpr = arrayExpr;
        this.indexExprs = new LinkedList();
    }

    public ExpressionDecl getArrayExpr()
    {
        return arrayExpr;
    }

    public void setArrayExpr(ExpressionDecl object)
    {
        this.arrayExpr = object;
    }

    public List getIndexExprs()
    {
        return indexExprs;
    }

    public void addIndexExpr(ExpressionDecl indexExpr)
    {
        indexExprs.add(indexExpr);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(arrayExpr);
        Iterator iter = indexExprs.iterator();
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
