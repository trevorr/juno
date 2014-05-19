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
 * Represents a Vera expect statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraExpectStatement
    extends VeraStatementImpl
{
    private static final long serialVersionUID = 4121981347192714548L;

    private final VeraExpectKind expectKind;
    private final List<VeraExpectTerm> expectTerms = new LinkedList<VeraExpectTerm>();
    private boolean expectAll;
    private VeraExpression delay;
    private VeraExpression window;
    private boolean soft;
    private boolean async;

    public VeraExpectStatement(VeraSchema schema, VeraExpectKind expectKind)
    {
        super(schema);
        this.expectKind = expectKind;
    }

    public VeraExpectKind getExpectKind()
    {
        return expectKind;
    }

    public List<VeraExpectTerm> getExpectTerms()
    {
        return expectTerms;
    }

    public void addExpectTerm(VeraExpectTerm term)
    {
        expectTerms.add(term);
    }

    public boolean isExpectAll()
    {
        return expectAll;
    }

    public void setExpectAll(boolean expectAll)
    {
        this.expectAll = expectAll;
    }

    public VeraExpression getDelay()
    {
        return delay;
    }

    public void setDelay(VeraExpression delay)
    {
        this.delay = delay;
    }

    public VeraExpression getWindow()
    {
        return window;
    }

    public void setWindow(VeraExpression window)
    {
        this.window = window;
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

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "expect statement";
    }
}
