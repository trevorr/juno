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

import com.newisys.langschema.Literal;

/**
 * Represents a Vera string literal expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraStringLiteral
    extends VeraExpression
    implements Literal
{
    private static final long serialVersionUID = 3258411737895416116L;

    private final String value;

    public VeraStringLiteral(VeraSchema schema, String value)
    {
        super(schema);
        setResultType(schema.stringType);
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public boolean isConstant()
    {
        return true;
    }

    public Object evaluateConstant()
    {
        return value;
    }

    public static String escape(String src)
    {
        StringBuffer buf = new StringBuffer();
        int end = src.length();
        for (int i = 0; i < end; ++i)
        {
            char c = src.charAt(i);
            switch (c)
            {
            case 0x0A:
                buf.append("\\n");
                break;
            case 0x09:
                buf.append("\\t");
                break;
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
