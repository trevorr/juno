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

/**
 * Long conversion and operation methods referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class LongWrapperOp
{
    private final static Long LONG_ZERO = new Long(0);
    private final static Long LONG_ONE = new Long(1);

    private LongWrapperOp()
    {
    }

    public static boolean toBoolean(Long l)
    {
        return l != null && l.longValue() != 0;
    }

    public static boolean lowBitToBoolean(Long l)
    {
        return l != null && (l.longValue() & 1) != 0;
    }

    public static boolean toBooleanXZTrue(Long l)
    {
        return l == null || l.longValue() != 0;
    }

    public static boolean lowBitToBooleanXZTrue(Long l)
    {
        return l == null || (l.longValue() & 1) != 0;
    }

    public static BitVector toBitVector(Long l)
    {
        return toBitVector(l, 32, false);
    }

    public static BitVector toBitVector(Long l, int len)
    {
        return toBitVector(l, len, false);
    }

    public static BitVector toBitVector(Long l, int len, boolean signExtend)
    {
        return l != null ? new BitVector(len, l.longValue(), signExtend)
            : new BitVector(len, Bit.X);
    }

    public static int toInt(Long l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException("Value is undefined");
        }
        return l.intValue();
    }

    public static long toLong(Long l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException("Value is undefined");
        }
        return l.longValue();
    }

    public static int toShiftCount(Long l, int max)
    {
        if (l == null)
        {
            throw new IllegalArgumentException(
                "Shift count cannot be undefined");
        }
        return (int) Math.min(l.longValue(), max);
    }

    public static int toShiftCount(Long l)
    {
        return toShiftCount(l, Integer.MAX_VALUE);
    }

    public static Long toLongWrapper(Bit b)
    {
        switch (b.getID())
        {
        case 0:
            return LONG_ZERO;
        case 1:
            return LONG_ONE;
        default:
            return null;
        }
    }

    public static Long toLongWrapper(BitVector bv)
    {
        return bv.containsXZ() ? null : new Long(bv.longValue());
    }

    public static Long toLongWrapper(BitVector bv, boolean signed)
    {
        if (bv.containsXZ())
        {
            return null;
        }
        else
        {
            long l = bv.longValue();
            if (signed)
            {
                int len = bv.length();
                if (len < 64)
                {
                    long mask = 1 << (len - 1);
                    if ((l & mask) != 0)
                    {
                        l |= ~(mask - 1);
                    }
                }
            }
            return new Long(l);
        }
    }

    public static Long toLongWrapper(Integer i)
    {
        return i != null ? new Long(i.longValue()) : null;
    }

    public static Long toLongWrapper(String s)
    {
        return new Long(IntOp.toLong(s));
    }

    public static Long toLongWrapper(Object o)
    {
        if (o instanceof Long || o == null)
        {
            return (Long) o;
        }
        else if (o instanceof BitVector)
        {
            return toLongWrapper((BitVector) o);
        }
        else if (o instanceof Bit)
        {
            return toLongWrapper((Bit) o);
        }
        else if (o instanceof Number)
        {
            return new Long(((Number) o).longValue());
        }
        else if (o instanceof Boolean)
        {
            return new Long(((Boolean) o).booleanValue() ? 1L : 0L);
        }
        else if (o instanceof String)
        {
            return toLongWrapper((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static Long add(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            + l2.longValue()) : null;
    }

    public static Long subtract(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            - l2.longValue()) : null;
    }

    public static Long multiply(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            * l2.longValue()) : null;
    }

    public static Long divide(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            / l2.longValue()) : null;
    }

    public static Long mod(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            % l2.longValue()) : null;
    }

    public static Long negate(Long l)
    {
        return (l != null) ? new Long(-l.longValue()) : null;
    }

    public static Long inc(Long l)
    {
        return (l != null) ? new Long(l.longValue() + 1) : null;
    }

    public static Long dec(Long l)
    {
        return (l != null) ? new Long(l.longValue() - 1) : null;
    }

    public static Long bitwiseAnd(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            & l2.longValue()) : null;
    }

    public static Long bitwiseAndNot(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(~(l1.longValue() & l2
            .longValue())) : null;
    }

    public static Long bitwiseOr(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            | l2.longValue()) : null;
    }

    public static Long bitwiseOrNot(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(~(l1.longValue() | l2
            .longValue())) : null;
    }

    public static Long bitwiseXor(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(l1.longValue()
            ^ l2.longValue()) : null;
    }

    public static Long bitwiseXorNot(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? new Long(~(l1.longValue() ^ l2
            .longValue())) : null;
    }

    public static Long bitwiseNot(Long l)
    {
        return (l != null) ? new Long(~l.longValue()) : null;
    }

    public static Long bitwiseReverse(Long l)
    {
        return (l != null) ? new Long(IntOp.bitwiseReverse(l.longValue()))
            : null;
    }

    public static Bit reductiveAnd(Long l)
    {
        return (l != null) ? (l.longValue() == -1 ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveAndNot(Long l)
    {
        return (l != null) ? (l.longValue() == -1 ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Bit reductiveOr(Long l)
    {
        return (l != null) ? (l.longValue() == 0 ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Bit reductiveOrNot(Long l)
    {
        return (l != null) ? (l.longValue() == 0 ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveXor(Long l)
    {
        return (l != null) ? ((IntOp.bitCount(l.longValue()) & 1) == 1
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveXorNot(Long l)
    {
        return (l != null) ? ((IntOp.bitCount(l.longValue()) & 1) == 1
            ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Long logicalAnd(Long l1, Long l2)
    {
        return toBoolean(l1) && toBoolean(l2) ? LONG_ONE : LONG_ZERO;
    }

    public static Long logicalOr(Long l1, Long l2)
    {
        return toBoolean(l1) || toBoolean(l2) ? LONG_ONE : LONG_ZERO;
    }

    public static Long logicalNegative(Long l)
    {
        return (l != null) ? (l.longValue() == 0 ? LONG_ONE : LONG_ZERO) : null;
    }

    public static Bit equal(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() == l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit notEqual(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() != l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit exactEqual(Long l1, Long l2)
    {
        return (l1 != null ? l2 != null && l1.longValue() == l2.longValue()
            : l2 == null) ? Bit.ONE : Bit.ZERO;
    }

    public static Bit exactNotEqual(Long l1, Long l2)
    {
        return (l1 != null ? l2 == null || l1.longValue() != l2.longValue()
            : l2 != null) ? Bit.ONE : Bit.ZERO;
    }

    public static Bit wildEqual(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() == l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.ONE;
    }

    public static Bit wildNotEqual(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() != l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.ZERO;
    }

    public static Bit greater(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() > l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit greaterOrEqual(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() >= l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit less(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() < l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit lessOrEqual(Long l1, Long l2)
    {
        return (l1 != null && l2 != null) ? (l1.longValue() <= l2.longValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Long shiftLeft(Long l1, Long l2)
    {
        return (l1 != null) ? new Long(l1.longValue() << toShiftCount(l2, 63))
            : null;
    }

    public static Long shiftRight(Long l1, Long l2)
    {
        return (l1 != null) ? new Long(l1.longValue() >> toShiftCount(l2, 63))
            : null;
    }
}
