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

/**
 * Represents a Vera drive or sample statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraDriveSampleStatement
    extends VeraStatementImpl
{
    private static final long serialVersionUID = 3257565092449957424L;

    private final VeraExpression destination;
    private final VeraExpression source;
    private VeraExpression delay;
    private boolean nonBlocking;
    private boolean soft;
    private boolean async;

    public VeraDriveSampleStatement(
        VeraExpression destination,
        VeraExpression source)
    {
        super(destination.schema);
        this.destination = destination;
        this.source = source;
    }

    public VeraExpression getDestination()
    {
        return destination;
    }

    public VeraExpression getSource()
    {
        return source;
    }

    public VeraExpression getDelay()
    {
        return delay;
    }

    public void setDelay(VeraExpression delay)
    {
        this.delay = delay;
    }

    public boolean isNonBlocking()
    {
        return nonBlocking;
    }

    public void setNonBlocking(boolean nonBlocking)
    {
        this.nonBlocking = nonBlocking;
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
        return "drive/sample statement";
    }
}
