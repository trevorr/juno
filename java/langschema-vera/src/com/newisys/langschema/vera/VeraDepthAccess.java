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
 * Represents a Vera signal back-reference expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraDepthAccess
    extends VeraExpression
{
    private static final long serialVersionUID = 4051048553600137012L;

    private final VeraExpression signal;
    private final int depth;

    public VeraDepthAccess(VeraExpression signal, int depth)
    {
        super(signal.schema);
        assert ((signal instanceof VeraMemberAccess && ((VeraMemberAccess) signal)
            .getMember() instanceof VeraPortSignal) || signal instanceof VeraSignalReference);
        setResultType(signal.getResultType());
        this.signal = signal;
        this.depth = depth;
    }

    public boolean isAssignable()
    {
        return false;
    }

    public boolean isConstant()
    {
        return false;
    }

    public VeraExpression getSignal()
    {
        return signal;
    }

    public int getDepth()
    {
        return depth;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
