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
 * Synchronization statement term.
 * 
 * @author Trevor Robinson
 */
public final class SyncTerm
    extends VeraSourceObjectImpl
{
    private SyncEdge edge;
    private ExpressionDecl signal; // null for CLOCK
    private boolean async;

    public SyncTerm()
    {
        edge = SyncEdge.ANYEDGE;
        async = false;
    }

    public SyncTerm(SyncEdge edge, ExpressionDecl signal, boolean async)
    {
        this.edge = edge;
        this.signal = signal;
        this.async = async;
    }

    public SyncEdge getEdge()
    {
        return edge;
    }

    public void setEdge(SyncEdge edge)
    {
        this.edge = edge;
    }

    public ExpressionDecl getSignal()
    {
        return signal;
    }

    public void setSignal(ExpressionDecl signal)
    {
        this.signal = signal;
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
        // do nothing; visitor should handle terms in SyncDecl
    }
}
