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

import com.newisys.langschema.ReturnStatement;

/**
 * Represents a Vera return statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraReturnStatement
    extends VeraStatementImpl
    implements ReturnStatement
{
    private static final long serialVersionUID = 3546926878884900913L;

    public VeraReturnStatement(VeraSchema schema)
    {
        super(schema);
    }

    public VeraExpression getReturnValue()
    {
        // Vera does not specify the return value in the 'return' statement
        return null;
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
