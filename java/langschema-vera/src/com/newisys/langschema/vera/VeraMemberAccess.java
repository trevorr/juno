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

import com.newisys.langschema.MemberAccess;
import com.newisys.langschema.util.SemanticException;

/**
 * Represents a Vera member access expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraMemberAccess
    extends VeraExpression
    implements MemberAccess
{
    private static final long serialVersionUID = 3257564022969546288L;

    private final VeraExpression object;
    private final VeraStructuredTypeMember member;

    public VeraMemberAccess(
        VeraExpression object,
        VeraStructuredTypeMember member)
    {
        super(object.schema);
        if (member instanceof VeraMemberFunction)
        {
            VeraMemberFunction func = (VeraMemberFunction) member;
            setResultType(func.getType());
        }
        else if (member instanceof VeraMemberVariable)
        {
            VeraMemberVariable var = (VeraMemberVariable) member;
            setResultType(var.getType());
        }
        else if (member instanceof VeraPortSignal)
        {
            VeraPortSignal signal = (VeraPortSignal) member;
            setResultType(signal.getType());
        }
        else if (member instanceof VeraInterfaceSignal)
        {
            VeraInterfaceSignal signal = (VeraInterfaceSignal) member;
            setResultType(signal.getType());
        }
        else
        {
            throw new SemanticException("Invalid member access to "
                + member.getClass().getName());
        }
        this.object = object;
        this.member = member;
    }

    public boolean isAssignable()
    {
        return !(member instanceof VeraMemberFunction);
    }

    public boolean isConstant()
    {
        return false;
    }

    public VeraExpression getObject()
    {
        return object;
    }

    public VeraStructuredTypeMember getMember()
    {
        return member;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
