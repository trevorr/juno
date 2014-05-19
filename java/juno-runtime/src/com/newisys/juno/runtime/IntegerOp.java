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
 * Integer conversion and operation methods referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class IntegerOp
{
    private final static Integer INTEGER_ZERO = new Integer(0);
    private final static Integer INTEGER_ONE = new Integer(1);

    private IntegerOp()
    {
    }

    public static boolean toBoolean(Integer i)
    {
        return i != null && i.intValue() != 0;
    }

    public static boolean lowBitToBoolean(Integer i)
    {
        return i != null && (i.intValue() & 1) != 0;
    }

    public static boolean toBooleanXZTrue(Integer i)
    {
        return i == null || i.intValue() != 0;
    }

    public static boolean lowBitToBooleanXZTrue(Integer i)
    {
        return i == null || (i.intValue() & 1) != 0;
    }

    public static BitVector toBitVector(Integer i)
    {
        return toBitVector(i, 32, false);
    }

    public static BitVector toBitVector(Integer i, int len)
    {
        return toBitVector(i, len, false);
    }

    public static BitVector toBitVector(Integer i, int len, boolean signExtend)
    {
        return i != null ? new BitVector(len, i.intValue(), signExtend)
            : new BitVector(len, Bit.X);
    }

    public static int toInt(Integer i)
    {
        if (i == null)
        {
            throw new IllegalArgumentException("Value is undefined");
        }
        return i.intValue();
    }

    public static int toShiftCount(Integer i, int max)
    {
        if (i == null)
        {
            throw new IllegalArgumentException(
                "Shift count cannot be undefined");
        }
        return Math.min(i.intValue(), max);
    }

    public static int toShiftCount(Integer i)
    {
        return toShiftCount(i, Integer.MAX_VALUE);
    }

    public static Integer toInteger(Bit b)
    {
        switch (b.getID())
        {
        case 0:
            return INTEGER_ZERO;
        case 1:
            return INTEGER_ONE;
        default:
            return null;
        }
    }

    public static Integer toInteger(BitVector bv)
    {
        return bv.containsXZ() ? null : new Integer(bv.intValue());
    }

    public static Integer toInteger(BitVector bv, boolean signed)
    {
        if (bv.containsXZ())
        {
            return null;
        }
        else
        {
            int i = bv.intValue();
            if (signed)
            {
                int len = bv.length();
                if (len < 32)
                {
                    int mask = 1 << (len - 1);
                    if ((i & mask) != 0)
                    {
                        i |= ~(mask - 1);
                    }
                }
            }
            return new Integer(i);
        }
    }

    public static Integer toInteger(Long l)
    {
        return l != null ? new Integer(l.intValue()) : null;
    }

    public static Integer toInteger(String s)
    {
        return new Integer(IntOp.toInt(s));
    }

    public static Integer toInteger(Object o)
    {
        if (o instanceof Integer || o == null)
        {
            return (Integer) o;
        }
        else if (o instanceof BitVector)
        {
            return toInteger((BitVector) o);
        }
        else if (o instanceof Bit)
        {
            return toInteger((Bit) o);
        }
        else if (o instanceof Number)
        {
            return new Integer(((Number) o).intValue());
        }
        else if (o instanceof Boolean)
        {
            return new Integer(((Boolean) o).booleanValue() ? 1 : 0);
        }
        else if (o instanceof String)
        {
            return toInteger((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static Integer add(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            + i2.intValue()) : null;
    }

    public static Integer subtract(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            - i2.intValue()) : null;
    }

    public static Integer multiply(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            * i2.intValue()) : null;
    }

    public static Integer divide(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            / i2.intValue()) : null;
    }

    public static Integer mod(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            % i2.intValue()) : null;
    }

    public static Integer negate(Integer i)
    {
        return (i != null) ? new Integer(-i.intValue()) : null;
    }

    public static Integer inc(Integer i)
    {
        return (i != null) ? new Integer(i.intValue() + 1) : null;
    }

    public static Integer dec(Integer i)
    {
        return (i != null) ? new Integer(i.intValue() - 1) : null;
    }

    public static Integer bitwiseAnd(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            & i2.intValue()) : null;
    }

    public static Integer bitwiseAndNot(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(~(i1.intValue() & i2
            .intValue())) : null;
    }

    public static Integer bitwiseOr(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            | i2.intValue()) : null;
    }

    public static Integer bitwiseOrNot(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(~(i1.intValue() | i2
            .intValue())) : null;
    }

    public static Integer bitwiseXor(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(i1.intValue()
            ^ i2.intValue()) : null;
    }

    public static Integer bitwiseXorNot(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? new Integer(~(i1.intValue() ^ i2
            .intValue())) : null;
    }

    public static Integer bitwiseNot(Integer i)
    {
        return (i != null) ? new Integer(~i.intValue()) : null;
    }

    public static Integer bitwiseReverse(Integer i)
    {
        return (i != null) ? new Integer(IntOp.bitwiseReverse(i.intValue()))
            : null;
    }

    public static Bit reductiveAnd(Integer i)
    {
        return (i != null) ? (i.intValue() == -1 ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveAndNot(Integer i)
    {
        return (i != null) ? (i.intValue() == -1 ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Bit reductiveOr(Integer i)
    {
        return (i != null) ? (i.intValue() == 0 ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Bit reductiveOrNot(Integer i)
    {
        return (i != null) ? (i.intValue() == 0 ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveXor(Integer i)
    {
        return (i != null) ? ((IntOp.bitCount(i.intValue()) & 1) == 1 ? Bit.ONE
            : Bit.ZERO) : Bit.X;
    }

    public static Bit reductiveXorNot(Integer i)
    {
        return (i != null) ? ((IntOp.bitCount(i.intValue()) & 1) == 1
            ? Bit.ZERO : Bit.ONE) : Bit.X;
    }

    public static Integer logicalAnd(Integer i1, Integer i2)
    {
        return toBoolean(i1) && toBoolean(i2) ? INTEGER_ONE : INTEGER_ZERO;
    }

    public static Integer logicalOr(Integer i1, Integer i2)
    {
        return toBoolean(i1) || toBoolean(i2) ? INTEGER_ONE : INTEGER_ZERO;
    }

    public static Integer logicalNegative(Integer i)
    {
        return (i != null) ? (i.intValue() == 0 ? INTEGER_ONE : INTEGER_ZERO)
            : null;
    }

    public static Bit equal(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() == i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit notEqual(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() != i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit exactEqual(Integer i1, Integer i2)
    {
        return (i1 != null ? i2 != null && i1.intValue() == i2.intValue()
            : i2 == null) ? Bit.ONE : Bit.ZERO;
    }

    public static Bit exactNotEqual(Integer i1, Integer i2)
    {
        return (i1 != null ? i2 == null || i1.intValue() != i2.intValue()
            : i2 != null) ? Bit.ONE : Bit.ZERO;
    }

    public static Bit wildEqual(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() == i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.ONE;
    }

    public static Bit wildNotEqual(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() != i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.ZERO;
    }

    public static Bit greater(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() > i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit greaterOrEqual(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() >= i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit less(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() < i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Bit lessOrEqual(Integer i1, Integer i2)
    {
        return (i1 != null && i2 != null) ? (i1.intValue() <= i2.intValue()
            ? Bit.ONE : Bit.ZERO) : Bit.X;
    }

    public static Integer shiftLeft(Integer i1, Integer i2)
    {
        return (i1 != null)
            ? new Integer(i1.intValue() << toShiftCount(i2, 31)) : null;
    }

    public static Integer shiftRight(Integer i1, Integer i2)
    {
        return (i1 != null)
            ? new Integer(i1.intValue() >> toShiftCount(i2, 31)) : null;
    }
}
