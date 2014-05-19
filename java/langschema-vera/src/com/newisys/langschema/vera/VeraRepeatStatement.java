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

/**
 * Represents a Vera repeat loop.
 * 
 * @author Trevor Robinson
 */
public final class VeraRepeatStatement
    extends VeraStatementImpl
{
    private static final long serialVersionUID = 3762533408148041783L;

    private final VeraExpression count;
    private final VeraStatement statement;

    public VeraRepeatStatement(VeraExpression count, VeraStatement statement)
    {
        super(count.schema);
        this.count = count;
        statement.setContainingStatement(this);
        this.statement = statement;
    }

    public VeraExpression getCondition()
    {
        return count;
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
        return "repeat statement";
    }
}
