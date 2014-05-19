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

import com.newisys.langschema.WhileStatement;

/**
 * Represents a Vera 'while' loop.
 * 
 * @author Trevor Robinson
 */
public final class VeraWhileStatement
    extends VeraStatementImpl
    implements WhileStatement
{
    private static final long serialVersionUID = 3258688797578245433L;

    private final VeraExpression condition;
    private final VeraStatement statement;

    public VeraWhileStatement(VeraExpression condition, VeraStatement statement)
    {
        super(condition.schema);
        this.condition = condition;
        statement.setContainingStatement(this);
        this.statement = statement;
    }

    public VeraExpression getCondition()
    {
        return condition;
    }

    public VeraStatement getStatement()
    {
        return statement;
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "while statement";
    }
}
