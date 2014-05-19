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

import java.io.Serializable;

import com.newisys.verilog.EdgeSet;

/**
 * Represents a term in a Vera signal synchronization statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraSyncTerm
    implements Serializable
{
    private static final long serialVersionUID = 3834309527276697140L;

    private final VeraExpression signal;
    private final EdgeSet edgeSet;
    private final boolean async;

    public VeraSyncTerm(VeraExpression signal, EdgeSet edgeSet, boolean async)
    {
        this.signal = signal;
        this.edgeSet = edgeSet;
        this.async = async;
    }

    public VeraExpression getSignal()
    {
        return signal;
    }

    public EdgeSet getEdgeSet()
    {
        return edgeSet;
    }

    public boolean isAsync()
    {
        return async;
    }
}
