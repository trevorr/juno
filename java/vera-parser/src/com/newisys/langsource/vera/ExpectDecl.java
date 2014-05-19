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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.vera.VeraExpectKind;

/**
 * Expect statement.
 * 
 * @author Trevor Robinson
 */
public final class ExpectDecl
    extends StatementDecl
{
    private VeraExpectKind expectKind = VeraExpectKind.SIMPLE;
    private ExpressionDecl delayExpr;
    private ExpressionDecl windowExpr;
    private final List expectExprs = new LinkedList(); // List<ExpectExprDecl>
    private boolean expectAll = true;
    private boolean soft = false;
    private boolean async = false;

    public VeraExpectKind getExpectKind()
    {
        return expectKind;
    }

    public void setExpectKind(VeraExpectKind expectKind)
    {
        this.expectKind = expectKind;
    }

    public ExpressionDecl getDelayExpr()
    {
        return delayExpr;
    }

    public void setDelayExpr(ExpressionDecl delayExpr)
    {
        this.delayExpr = delayExpr;
    }

    public ExpressionDecl getWindowExpr()
    {
        return windowExpr;
    }

    public void setWindowExpr(ExpressionDecl windowExpr)
    {
        this.windowExpr = windowExpr;
    }

    public void addExpectExpr(ExpectExprDecl expr)
    {
        expectExprs.add(expr);
    }

    public List getExpectExprs()
    {
        return expectExprs;
    }

    public boolean isExpectAll()
    {
        return expectAll;
    }

    public void setExpectAll(boolean expectAll)
    {
        this.expectAll = expectAll;
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
