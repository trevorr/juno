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

import com.newisys.langschema.Expression;
import com.newisys.langschema.ForStatement;

/**
 * Represents a Vera 'for' loop.
 * 
 * @author Trevor Robinson
 */
public final class VeraForStatement
    extends VeraStatementImpl
    implements ForStatement
{
    private static final long serialVersionUID = 3256444685755954224L;

    private final List<VeraBlockMember> initStmtList = new LinkedList<VeraBlockMember>();
    private VeraExpression condition;
    private final List<VeraExpressionStatement> updateStmtList = new LinkedList<VeraExpressionStatement>();
    private VeraStatement statement;

    public VeraForStatement(VeraSchema schema)
    {
        super(schema);
    }

    public List<VeraBlockMember> getInitStatements()
    {
        return initStmtList;
    }

    public void addInitStatement(VeraBlockMember stmt)
    {
        stmt.setContainingStatement(this);
        initStmtList.add(stmt);
    }

    public Expression getCondition()
    {
        return condition;
    }

    public void setCondition(VeraExpression condition)
    {
        this.condition = condition;
    }

    public List<VeraExpressionStatement> getUpdateStatements()
    {
        return updateStmtList;
    }

    public void addUpdateStatement(VeraExpressionStatement stmt)
    {
        stmt.setContainingStatement(this);
        updateStmtList.add(stmt);
    }

    public VeraStatement getStatement()
    {
        return statement;
    }

    public void setStatement(VeraStatement stmt)
    {
        stmt.setContainingStatement(this);
        this.statement = stmt;
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "for statement";
    }
}
