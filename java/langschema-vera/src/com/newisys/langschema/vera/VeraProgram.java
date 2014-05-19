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

import java.util.LinkedHashSet;
import java.util.Set;

import com.newisys.langschema.NamedObject;

/**
 * Represents a Vera program block.
 * 
 * @author Trevor Robinson
 */
public final class VeraProgram
    extends VeraSchemaObjectImpl
    implements NamedObject, VeraCompilationUnitMember
{
    private static final long serialVersionUID = 3256719602334315829L;

    private final VeraName name;
    private final VeraBlock block;
    private final Set<VeraCompilationUnitMember> shellMembers;
    private VeraCompilationUnit compUnit;

    public VeraProgram(VeraName name, VeraBlock block)
    {
        super(block.schema);
        this.name = name;
        this.block = block;
        this.shellMembers = new LinkedHashSet<VeraCompilationUnitMember>();
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraBlock getBlock()
    {
        return block;
    }

    public Set<VeraCompilationUnitMember> getShellMembers()
    {
        return shellMembers;
    }

    public void addShellMember(VeraCompilationUnitMember member)
    {
        shellMembers.add(member);
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
        return VeraVisibility.LOCAL;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "program " + name;
    }
}
