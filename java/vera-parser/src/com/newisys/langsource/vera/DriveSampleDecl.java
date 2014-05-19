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
 * Signal drive/sample statement.
 * 
 * @author Trevor Robinson
 */
public final class DriveSampleDecl
    extends StatementDecl
{
    private ExpressionDecl delayExpr;
    private ExpressionDecl destExpr;
    private Operator operator;
    private ExpressionDecl valueExpr;
    private boolean soft = false;
    private boolean async = false;

    public ExpressionDecl getDelayExpr()
    {
        return delayExpr;
    }

    public void setDelayExpr(ExpressionDecl delayExpr)
    {
        this.delayExpr = delayExpr;
    }

    public ExpressionDecl getDestExpr()
    {
        return destExpr;
    }

    public void setDestExpr(ExpressionDecl destExpr)
    {
        this.destExpr = destExpr;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    public ExpressionDecl getValueExpr()
    {
        return valueExpr;
    }

    public void setValueExpr(ExpressionDecl valueExpr)
    {
        this.valueExpr = valueExpr;
    }

    public boolean isSoft()
    {
        return soft;
    }

    public void setSoft(boolean soft)
    {
        this.soft = soft;
    }

    public boolean isAsync()
    {
        return async;
    }

    public void setAsync(boolean async)
    {
        this.async = async;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
