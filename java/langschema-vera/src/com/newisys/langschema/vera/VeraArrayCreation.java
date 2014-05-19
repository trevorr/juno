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

import java.util.Collections;
import java.util.List;

import com.newisys.langschema.ArrayCreation;

/**
 * Represents a Vera array creation expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraArrayCreation
    extends VeraExpression
    implements ArrayCreation
{
    private static final long serialVersionUID = 3258407331174889523L;

    private final VeraDynamicArrayType type;
    private final VeraExpression sizeExpr;
    private VeraExpression sourceExpr;

    public VeraArrayCreation(VeraDynamicArrayType type, VeraExpression sizeExpr)
    {
        super(type.schema);
        setResultType(type);
        this.type = type;
        this.sizeExpr = sizeExpr;
    }

    public VeraDynamicArrayType getType()
    {
        return type;
    }

    public List<VeraExpression> getDimensions()
    {
        return Collections.singletonList(sizeExpr);
    }

    public VeraExpression getSizeExpr()
    {
        return sizeExpr;
    }

    public VeraExpression getSourceExpr()
    {
        return sourceExpr;
    }

    public void setSourceExpr(VeraExpression sourceExpr)
    {
        this.sourceExpr = sourceExpr;
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
