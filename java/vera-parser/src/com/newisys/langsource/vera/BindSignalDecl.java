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

/**
 * Bind signal declaration.
 * 
 * @author Trevor Robinson
 */
public final class BindSignalDecl
    extends VeraSourceObjectImpl
{
    private String portMember;
    private final List signalRanges = new LinkedList(); // List<SignalRangeDecl>

    public String getPortMember()
    {
        return portMember;
    }

    public void setPortMember(String portMember)
    {
        this.portMember = portMember;
    }

    public void addSignalRange(SignalRangeDecl signalRange)
    {
        signalRanges.add(signalRange);
    }

    public List getSignalRanges()
    {
        return signalRanges;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
