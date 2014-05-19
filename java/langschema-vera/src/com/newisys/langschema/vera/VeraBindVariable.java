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
 * Represents a Vera signal bind variable. This is simply the normal Vera bind
 * construct. It is equivalent to a pre-initialized, unassignable, global
 * variable of the given port type.
 * 
 * @author Trevor Robinson
 */
public final class VeraBindVariable
    extends VeraVariable
    implements VeraCompilationUnitMember
{
    private static final long serialVersionUID = 3258131370952898102L;

    private final VeraPortType port;
    private final List<VeraBindMember> members = new LinkedList<VeraBindMember>();
    private VeraCompilationUnit compUnit;

    public VeraBindVariable(VeraName name, VeraPortType port)
    {
        super(name, port);
        this.port = port;
    }

    public VeraPortType getPort()
    {
        return port;
    }

    public List<VeraBindMember> getMembers()
    {
        return members;
    }

    public void addMember(VeraBindMember member)
    {
        members.add(member);
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public VeraVisibility getVisibility()
    {
        return VeraVisibility.PUBLIC;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
