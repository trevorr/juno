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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.CompilationUnit;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Vera compilation unit.
 * 
 * @author Trevor Robinson
 */
public final class VeraCompilationUnit
    extends VeraSchemaObjectImpl
    implements CompilationUnit, VeraSchemaMember
{
    private static final long serialVersionUID = 3257572819045922614L;

    private final String sourcePath;
    private final VeraName name;
    private final List<VeraCompilationUnitMember> members = new LinkedList<VeraCompilationUnitMember>();
    private final NameTable nameTable = new NameTable();
    private boolean complete;

    public VeraCompilationUnit(VeraSchema schema, String sourcePath)
    {
        super(schema);
        this.sourcePath = sourcePath;
        this.name = new VeraName(sourcePath, VeraNameKind.COMP_UNIT, null);
    }

    public VeraName getName()
    {
        return name;
    }

    public String getSourcePath()
    {
        return sourcePath;
    }

    public List<VeraCompilationUnitMember> getMembers()
    {
        return members;
    }

    public void addMember(VeraCompilationUnitMember member)
    {
        assert (member.getCompilationUnit() == null);
        member.setCompilationUnit(this);
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public void removeMember(VeraCompilationUnitMember member)
    {
        assert (member.getCompilationUnit() == this);
        member.setCompilationUnit(null);
        members.remove(member);
        if (member instanceof NamedObject)
        {
            nameTable.removeObject((NamedObject) member);
        }
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean isComplete()
    {
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "compilation unit " + sourcePath;
    }
}
