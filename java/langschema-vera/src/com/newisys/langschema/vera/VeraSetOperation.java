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

/**
 * Base class for Vera set membership operations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraSetOperation
    extends VeraExpression
{
    private static final long serialVersionUID = 8848261015198728687L;

    private final VeraExpression expr;
    private final List<VeraSetMember> members = new LinkedList<VeraSetMember>();

    public VeraSetOperation(VeraExpression expr)
    {
        super(expr.schema);
        setResultType(schema.bitType);
        this.expr = expr;
    }

    public boolean isConstant()
    {
        // treat set operations as non-constant, because a) they are only usable
        // in random constraint blocks and b) it's easier
        return false;
    }

    public VeraExpression getExpr()
    {
        return expr;
    }

    public List<VeraSetMember> getMembers()
    {
        return members;
    }

    public void addMember(VeraSetMember expr)
    {
        members.add(expr);
    }
}
