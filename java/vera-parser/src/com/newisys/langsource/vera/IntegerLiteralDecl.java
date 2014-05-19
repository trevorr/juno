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
 * Integer literal expression.
 * 
 * @author Trevor Robinson
 */
public final class IntegerLiteralDecl
    extends LiteralDecl
{
    private int value;

    public IntegerLiteralDecl(int value)
    {
        super(LiteralKind.INTEGER);
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public String toString()
    {
        return String.valueOf(value);
    }

    public static int parse(String src)
    {
        int value = 0;
        int len = src.length();
        for (int i = 0; i < len; ++i)
        {
            char c = src.charAt(i);
            if (c == '_') continue;

            int digit = Character.digit(c, 10);
            if (digit < 0)
            {
                throw new NumberFormatException("Invalid digit in '" + src
                    + "'");
            }

            int prevValue = value;
            value = value * 10 + digit;
            if (value < prevValue)
            {
                throw new NumberFormatException("Integer overflow for '" + src
                    + "'");
            }
        }
        return value;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
