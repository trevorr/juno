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

/**
 * Set operation.
 * 
 * @author Trevor Robinson
 */
public final class SetOperationDecl
    extends ExpressionDecl
{
    private ExpressionDecl expr;
    private Operator operator;
    private RangeListDecl ranges;

    public SetOperationDecl(ExpressionDecl expr)
    {
        this.expr = expr;
    }

    public SetOperationDecl(ExpressionDecl expr, Operator operator)
    {
        this.expr = expr;
        this.operator = operator;
    }

    public ExpressionDecl getExpr()
    {
        return expr;
    }

    public void setExpr(ExpressionDecl expr)
    {
        this.expr = expr;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    public RangeListDecl getRanges()
    {
        return ranges;
    }

    public void setRanges(RangeListDecl ranges)
    {
        this.ranges = ranges;
    }

    public String toString()
    {
        return expr + " " + operator + " " + ranges;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
