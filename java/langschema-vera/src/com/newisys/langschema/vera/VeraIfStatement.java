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

import com.newisys.langschema.IfStatement;

/**
 * Represents a Vera 'if' statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraIfStatement
    extends VeraStatementImpl
    implements IfStatement
{
    private static final long serialVersionUID = 3256718485626107440L;

    private final VeraExpression condition;
    private final VeraStatement thenStatement;
    private final VeraStatement elseStatement;

    public VeraIfStatement(
        VeraExpression condition,
        VeraStatement thenStatement,
        VeraStatement elseStatement)
    {
        super(condition.schema);
        this.condition = condition;
        thenStatement.setContainingStatement(this);
        this.thenStatement = thenStatement;
        if (elseStatement != null)
        {
            elseStatement.setContainingStatement(this);
        }
        this.elseStatement = elseStatement;
    }

    public VeraExpression getCondition()
    {
        return condition;
    }

    public VeraStatement getThenStatement()
    {
        return thenStatement;
    }

    public VeraStatement getElseStatement()
    {
        return elseStatement;
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "if statement";
    }
}
