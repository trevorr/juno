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

import com.newisys.langschema.ForkStatement;
import com.newisys.langschema.JoinKind;

/**
 * Represents a Vera fork statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraForkStatement
    extends VeraStatementImpl
    implements ForkStatement
{
    private static final long serialVersionUID = 3977860670165103673L;

    private final List<VeraStatement> stmtList = new LinkedList<VeraStatement>();
    private final JoinKind joinKind;

    public VeraForkStatement(VeraSchema schema, JoinKind joinKind)
    {
        super(schema);
        this.joinKind = joinKind;
    }

    public List<VeraStatement> getForkedStatements()
    {
        return stmtList;
    }

    public void addForkedStatements(VeraStatement stmt)
    {
        stmt.setContainingStatement(this);
        stmtList.add(stmt);
    }

    public JoinKind getJoinKind()
    {
        return joinKind;
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "fork statement";
    }
}
