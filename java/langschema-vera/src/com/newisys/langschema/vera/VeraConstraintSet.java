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

/**
 * Represents a Vera random contraint set.
 * 
 * @author Trevor Robinson
 */
public class VeraConstraintSet
    extends VeraExpression
{
    private static final long serialVersionUID = 3256728368346380336L;

    private final List<VeraExpression> exprs = new LinkedList<VeraExpression>();
    private boolean constant = true;

    public VeraConstraintSet(VeraSchema schema)
    {
        super(schema);
        setResultType(schema.bitType);
    }

    public final List<VeraExpression> getExprs()
    {
        return exprs;
    }

    public final void addExpr(VeraExpression expr)
    {
        assert (expr.getResultType().isIntegralConvertible());
        exprs.add(expr);
        if (!expr.isConstant()) constant = false;
    }

    public boolean isConstant()
    {
        return constant;
    }

    public Object evaluateConstant()
    {
        for (VeraExpression expr : exprs)
        {
            if (!toBoolean(expr.evaluateConstant())) return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
