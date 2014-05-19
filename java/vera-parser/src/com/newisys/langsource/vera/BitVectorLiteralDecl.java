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

import com.newisys.verilog.util.BitVector;

/**
 * Bit vector literal expression.
 * 
 * @author Trevor Robinson
 */
public final class BitVectorLiteralDecl
    extends LiteralDecl
{
    private BitVector value;
    private int radix;

    public BitVectorLiteralDecl(BitVector value)
    {
        super(LiteralKind.BIT_VECTOR);
        this.value = value;
        this.radix = 10;
    }

    public BitVector getValue()
    {
        return value;
    }

    public void setValue(BitVector value)
    {
        this.value = value;
    }

    public int getRadix()
    {
        return radix;
    }

    public void setRadix(int radix)
    {
        this.radix = radix;
    }

    public String toString()
    {
        return value.toString();
    }

    public static BitVectorLiteralDecl parse(String src)
    {
        BitVector bv = new BitVector(src);
        BitVectorLiteralDecl result = new BitVectorLiteralDecl(bv);

        int tickPos = src.indexOf('\'');
        if (tickPos >= 0)
        {
            int radix = 10;
            switch (src.charAt(tickPos + 1))
            {
            case 'h':
                radix = 16;
                break;
            case 'o':
                radix = 8;
                break;
            case 'b':
                radix = 2;
                break;
            }
            result.setRadix(radix);
        }

        return result;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
