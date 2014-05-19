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

import com.newisys.langschema.ArrayAccess;

/**
 * Represents a Vera array access expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraArrayAccess
    extends VeraExpression
    implements ArrayAccess
{
    private static final long serialVersionUID = 3545518417425020721L;

    private final VeraExpression arrayExpr;
    private final List<VeraExpression> indices = new LinkedList<VeraExpression>();

    public VeraArrayAccess(VeraExpression array)
    {
        super(array.schema);
        VeraType resultType = array.getResultType();
        if (resultType instanceof VeraArrayType)
        {
            VeraArrayType arrayType = (VeraArrayType) resultType;
            setResultType(arrayType.getElementType());
        }
        else
        {
            assert (resultType instanceof VeraBitVectorType);
            setResultType(schema.bitType);
        }
        this.arrayExpr = array;
    }

    public VeraExpression getArray()
    {
        return arrayExpr;
    }

    public List<VeraExpression> getIndices()
    {
        return indices;
    }

    public void addIndex(VeraExpression expr)
    {
        indices.add(expr);
    }

    public boolean isAssignable()
    {
        return true;
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
