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
 * Represents a Vera signal bind member.
 * 
 * @author Trevor Robinson
 */
public final class VeraBindMember
    extends VeraSchemaObjectImpl
{
    private static final long serialVersionUID = 4050197535929152305L;

    private final VeraPortSignal portSignal;
    private final VeraExpression interfaceExpr;

    public VeraBindMember(
        VeraPortSignal portSignal,
        VeraExpression interfaceExpr)
    {
        super(portSignal.schema);
        this.portSignal = portSignal;
        this.interfaceExpr = interfaceExpr;
    }

    public VeraPortSignal getPortSignal()
    {
        return portSignal;
    }

    public VeraExpression getInterfaceExpr()
    {
        return interfaceExpr;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "bind of " + portSignal.getName();
    }
}
