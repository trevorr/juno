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

import com.newisys.langschema.JoinKind;

/**
 * Fork/join statement.
 * 
 * @author Trevor Robinson
 */
public final class ForkJoinDecl
    extends StatementDecl
{
    private final List statements; // List<StatementDecl>
    private JoinKind joinKind;

    public ForkJoinDecl()
    {
        statements = new LinkedList();
        joinKind = JoinKind.ALL;
    }

    public void addStatement(StatementDecl stmt)
    {
        statements.add(stmt);
    }

    public List getStatements()
    {
        return statements;
    }

    public JoinKind getJoinKind()
    {
        return joinKind;
    }

    public void setJoinKind(JoinKind joinKind)
    {
        this.joinKind = joinKind;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("fork ");
        Iterator iter = statements.iterator();
        while (iter.hasNext())
        {
            buf.append(iter.next());
            buf.append(' ');
        }
        buf.append("join ");
        buf.append(joinKind);
        return buf.toString();
    }
}
