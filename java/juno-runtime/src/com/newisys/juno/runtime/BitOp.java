/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.juno.runtime;

import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Bit conversion and operation methods referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class BitOp
{
    private BitOp()
    {
    }

    public static Bit toBit(int i)
    {
        return (i & 1) != 0 ? Bit.ONE : Bit.ZERO;
    }

    public static Bit toBit(long l)
    {
        return (l & 1) != 0 ? Bit.ONE : Bit.ZERO;
    }

    public static Bit toBit(Integer i)
    {
        return (i != null) ? toBit(i.intValue()) : Bit.X;
    }

    public static Bit toBit(Long l)
    {
        return (l != null) ? toBit(l.intValue()) : Bit.X;
    }

    public static Bit toBit(BitVector bv)
    {
        return bv.getBit(0);
    }

    public static Bit toBit(String s)
    {
        return BooleanOp.toBoolean(s) ? Bit.ONE : Bit.ZERO;
    }

    public static Bit toBit(Object o)
    {
        if (o instanceof Bit)
        {
            return (Bit) o;
        }
        else if (o instanceof Number)
        {
            return toBit(((Number) o).intValue());
        }
        else if (o instanceof BitVector)
        {
            return toBit((BitVector) o);
        }
        else if (o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue() ? Bit.ONE : Bit.ZERO;
        }
        else if (o instanceof String)
        {
            return toBit((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static Bit toLogicalBit(int i)
    {
        return i != 0 ? Bit.ONE : Bit.ZERO;
    }

    public static Bit toLogicalBit(long l)
    {
        return l != 0 ? Bit.ONE : Bit.ZERO;
    }

    public static Bit toLogicalBit(Integer i)
    {
        return (i != null) ? toLogicalBit(i.intValue()) : Bit.X;
    }

    public static Bit toLogicalBit(Long l)
    {
        return (l != null) ? toLogicalBit(l.intValue()) : Bit.X;
    }

    public static Bit toLogicalBit(BitVector bv)
    {
        return bv.containsXZ() ? Bit.X : (bv.isNotZero() ? Bit.ONE : Bit.ZERO);
    }

    public static BitVector toBitVector(int len, Bit b)
    {
        return new BitVectorBuffer(len, Bit.ZERO).setBit(0, b).toBitVector();
    }

    public static int toInt(Bit b)
    {
        if (b.isXZ())
        {
            throw new IllegalArgumentException("Value is X/Z");
        }
        return b == Bit.ONE ? 1 : 0;
    }

    public static int toShiftCount(Bit b)
    {
        if (b.isXZ())
        {
            throw new IllegalArgumentException("Shift count cannot be X/Z");
        }
        return b == Bit.ONE ? 1 : 0;
    }

    public static Bit divide(Bit b1, Bit b2)
    {
        if (b2 == Bit.ZERO)
        {
            throw new ArithmeticException("Bit divide by zero");
        }
        return b1.and(b2);
    }

    public static Bit mod(Bit b1, Bit b2)
    {
        if (b2 == Bit.ZERO)
        {
            throw new ArithmeticException("Bit divide by zero");
        }
        return b1.isXZ() || b2.isXZ() ? Bit.X : Bit.ZERO;
    }

    public static Bit equal(Bit b1, Bit b2)
    {
        return (!b1.isXZ() && !b2.isXZ()) ? (b1 == b2 ? Bit.ONE : Bit.ZERO)
            : Bit.X;
    }

    public static Bit notEqual(Bit b1, Bit b2)
    {
        return (!b1.isXZ() && !b2.isXZ()) ? (b1 != b2 ? Bit.ONE : Bit.ZERO)
            : Bit.X;
    }

    public static Bit wildEqual(Bit b1, Bit b2)
    {
        return (!b1.isXZ() && !b2.isXZ()) ? (b1 == b2 ? Bit.ONE : Bit.ZERO)
            : Bit.ONE;
    }

    public static Bit wildNotEqual(Bit b1, Bit b2)
    {
        return (!b1.isXZ() && !b2.isXZ()) ? (b1 != b2 ? Bit.ONE : Bit.ZERO)
            : Bit.ZERO;
    }
}
