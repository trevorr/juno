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

/**
 * String literal expression.
 * 
 * @author Trevor Robinson
 */
public final class StringLiteralDecl
    extends LiteralDecl
{
    private String value;

    public StringLiteralDecl(String text)
    {
        super(LiteralKind.STRING);
        this.value = text;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String text)
    {
        this.value = text;
    }

    public String toString()
    {
        return '"' + escape(value) + '"';
    }

    public static String parse(String src)
    {
        if (!src.startsWith("\"") || !src.endsWith("\""))
        {
            throw new IllegalArgumentException(
                "Double-quote delimited string expected");
        }

        // strip out escaped newlines
        StringBuffer buf = new StringBuffer();
        int end = src.length() - 1;
        for (int i = 1; i < end; ++i)
        {
            char c = src.charAt(i);
            if (c == '\\' && i + 1 < end)
            {
                char c2 = src.charAt(i + 1);
                if (c2 == '\n')
                {
                    ++i;
                    continue;
                }
                else if (c2 == '\r')
                {
                    ++i;
                    if (i + 1 < end && src.charAt(i + 1) == '\n')
                    {
                        ++i;
                    }
                    continue;
                }
            }
            buf.append(c);
        }
        return buf.toString();
    }

    public static String unescape(String src)
    {
        StringBuffer buf = new StringBuffer();
        int end = src.length();
        for (int i = 0; i < end; ++i)
        {
            char c = src.charAt(i);
            if (c == '\\' && ++i < end)
            {
                char c2 = src.charAt(i);
                switch (c2)
                {
                case 'b':
                    c2 = 0x08;
                    break;
                case 't':
                    c2 = 0x09;
                    break;
                case 'n':
                    c2 = 0x0A;
                    break;
                case 'f':
                    c2 = 0x0C;
                    break;
                case 'r':
                    c2 = 0x0D;
                    break;
                }
                c = c2;
            }
            buf.append(c);
        }
        return buf.toString();
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
            case 0x08:
                buf.append("\\b");
                break;
            case 0x09:
                buf.append("\\t");
                break;
            case 0x0A:
                buf.append("\\n");
                break;
            case 0x0C:
                buf.append("\\f");
                break;
            case 0x0D:
                buf.append("\\r");
                break;
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                if (c < 0x20 || c >= 0x007F)
                {
                    buf.append("\\u");
                    buf.append(Character.forDigit((c >> 12) & 0xF, 16));
                    buf.append(Character.forDigit((c >> 8) & 0xF, 16));
                    buf.append(Character.forDigit((c >> 4) & 0xF, 16));
                    buf.append(Character.forDigit((c >> 0) & 0xF, 16));
                }
                else
                {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
