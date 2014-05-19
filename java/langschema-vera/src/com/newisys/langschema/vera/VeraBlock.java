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

import com.newisys.langschema.Block;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Vera statement block.
 * 
 * @author Trevor Robinson
 */
public final class VeraBlock
    extends VeraStatementImpl
    implements Block
{
    private static final long serialVersionUID = 3256439188113862705L;

    private final List<VeraBlockMember> members = new LinkedList<VeraBlockMember>();
    private final NameTable nameTable = new NameTable();

    public VeraBlock(VeraSchema schema)
    {
        super(schema);
    }

    public List<VeraBlockMember> getMembers()
    {
        return members;
    }

    public void addMember(VeraBlockMember member)
    {
        member.setContainingStatement(this);
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "block (" + members.size() + " members)";
    }
}
