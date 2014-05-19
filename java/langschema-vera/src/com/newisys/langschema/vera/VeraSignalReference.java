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
 * Represents a Vera signal reference expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraSignalReference
    extends VeraExpression
{
    private static final long serialVersionUID = 3545521724532602681L;

    private final VeraInterfaceSignal signal;

    public VeraSignalReference(VeraInterfaceSignal signal)
    {
        super(signal.schema);
        setResultType(signal.getType());
        this.signal = signal;
    }

    public VeraInterfaceSignal getSignal()
    {
        return signal;
    }

    public boolean isAssignable()
    {
        return signal.getDirection() != VeraSignalDirection.INPUT;
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
