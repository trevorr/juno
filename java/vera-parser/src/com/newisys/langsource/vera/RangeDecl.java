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

import com.newisys.parser.util.MacroRef;

/**
 * Bit or value range.
 * 
 * @author Trevor Robinson
 */
public class RangeDecl
    extends VeraSourceObjectImpl
    implements MacroDecl
{
    protected ExpressionDecl from;
    protected ExpressionDecl to;
    private MacroRef firstRef;
    private MacroRef lastRef;

    public final ExpressionDecl getFrom()
    {
        return from;
    }

    public void setFrom(ExpressionDecl low)
    {
        this.from = low;
    }

    public final ExpressionDecl getTo()
    {
        return to;
    }

    public void setTo(ExpressionDecl high)
    {
        this.to = high;
    }

    public MacroRef getFirstExpandedFrom()
    {
        return firstRef;
    }

    public MacroRef getLastExpandedFrom()
    {
        return lastRef;
    }

    public void setExpandedFrom(MacroRef firstRef, MacroRef lastRef)
    {
        this.firstRef = firstRef;
        this.lastRef = lastRef;
    }

    public String toString()
    {
        return (from == to) ? from.toString() : from + ":" + to;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        // do nothing
    }
}
