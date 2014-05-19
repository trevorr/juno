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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Statement block.
 * 
 * @author Trevor Robinson
 */
public final class BlockDecl
    extends StatementDecl
{
    private List localVars; // List<LocalVarDecl>
    private List statements; // List<StatementDecl>

    public BlockDecl()
    {
        localVars = new LinkedList();
        statements = new LinkedList();
    }

    public void addLocalVar(LocalVarDecl var)
    {
        localVars.add(var);
    }

    public List getLocalVars()
    {
        return localVars;
    }

    public void addStatement(StatementDecl stmt)
    {
        statements.add(stmt);
    }

    public List getStatements()
    {
        return statements;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        boolean first = false;
        Iterator iter = localVars.iterator();
        while (iter.hasNext())
        {
            if (!first) buf.append(' ');
            buf.append(iter.next());
        }
        iter = statements.iterator();
        while (iter.hasNext())
        {
            if (!first) buf.append(' ');
            buf.append(iter.next());
        }
        buf.append(" }");
        return buf.toString();
    }
}
