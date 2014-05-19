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
 * BitVector conversion and operation methods referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class BitVectorOp
{
    public static final BitVector BIT_0 = new BitVector(1, Bit.ZERO);
    public static final BitVector BIT_1 = new BitVector(1, Bit.ONE);
    public static final BitVector BIT_Z = new BitVector(1, Bit.Z);
    public static final BitVector BIT_X = new BitVector(1, Bit.X);

    private BitVectorOp()
    {
    }

    public static BitVector toBitVector(boolean[] ba)
    {
        final BitVectorBuffer buf = new BitVectorBuffer(ba.length, Bit.ZERO);
        for (int i = 0; i < ba.length; ++i)
        {
            if (ba[i]) buf.setBit(i, Bit.ONE);
        }
        return buf.toBitVector();
    }

    public static BitVector toBitVector(Bit b)
    {
        switch (b.getID())
        {
        case Bit.ID_ZERO:
            return BIT_0;
        case Bit.ID_ONE:
            return BIT_1;
        case Bit.ID_Z:
            return BIT_Z;
        default:
            return BIT_X;
        }
    }

    public static BitVector toBitVector(Object value)
    {
        if (value instanceof BitVector)
        {
            return (BitVector) value;
        }
        else if (value instanceof Bit)
        {
            return toBitVector((Bit) value);
        }
        else if (value instanceof Integer)
        {
            return new BitVector(32, ((Integer) value).intValue());
        }
        else if (value instanceof Long)
        {
            return new BitVector(64, ((Double) value).longValue());
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue() ? BIT_1 : BIT_0;
        }
        else if (value instanceof String)
        {
            return new BitVector(((String) value).getBytes());
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type");
        }
    }

    public static int toInt(BitVector bv)
    {
        if (bv.containsXZ())
        {
            throw new IllegalArgumentException("Value contains X/Z");
        }
        return bv.intValue();
    }

    public static int toShiftCount(BitVector bv, int max)
    {
        if (bv.containsXZ())
        {
            throw new IllegalArgumentException("Shift count cannot contain X/Z");
        }
        return Math.min(bv.intValue(), max);
    }

    public static int toShiftCount(BitVector bv)
    {
        return toShiftCount(bv, Integer.MAX_VALUE);
    }

    public static Bit equal(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (bv1
            .equals(bv2) ? Bit.ONE : Bit.ZERO);
    }

    public static Bit notEqual(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (!bv1
            .equals(bv2) ? Bit.ONE : Bit.ZERO);
    }

    public static Bit greater(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (bv1
            .compareTo(bv2) > 0 ? Bit.ONE : Bit.ZERO);
    }

    public static Bit greaterOrEqual(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (bv1
            .compareTo(bv2) >= 0 ? Bit.ONE : Bit.ZERO);
    }

    public static Bit less(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (bv1
            .compareTo(bv2) < 0 ? Bit.ONE : Bit.ZERO);
    }

    public static Bit lessOrEqual(BitVector bv1, BitVector bv2)
    {
        return (bv1.containsXZ() || bv2.containsXZ()) ? Bit.X : (bv1
            .compareTo(bv2) <= 0 ? Bit.ONE : Bit.ZERO);
    }

    public static BitVector concat(BitVector... bvs)
    {
        // calculate total length of result
        int total = 0;
        for (final BitVector bv : bvs)
        {
            total += bv.length();
        }

        // write individual bit vectors into buffer, starting with MSB
        final BitVectorBuffer result = new BitVectorBuffer(total);
        int pos = total;
        for (final BitVector bv : bvs)
        {
            int len = bv.length();
            result.setBits(pos - 1, pos - len, bv);
            pos -= len;
        }
        return result.toBitVector();
    }

    public static BitVector replicate(int count, BitVector bv)
    {
        // calculate total length of result
        final int len = bv.length();
        final int total = len * count;

        // replicate bit vector into buffer, starting with MSB
        final BitVectorBuffer result = new BitVectorBuffer(total);
        int pos = total;
        for (int i = 0; i < count; ++i)
        {
            result.setBits(pos - 1, pos - len, bv);
            pos -= len;
        }
        return result.toBitVector();
    }

    /**
     * Wrapper for BitVector.getBits() that allows high and low indices to be
     * swapped.
     *
     * @param bv the BitVector to get the bit slice from
     * @param high the "high" index
     * @param low the "low" index
     * @return the bit slice
     */
    public static BitVector getBits(BitVector bv, int high, int low)
    {
        if (high < low)
        {
            int temp = high;
            high = low;
            low = temp;
        }
        return bv.getBits(high, low);
    }

    /**
     * Wrapper for BitVector.getBits() that allows high and low indices to be
     * swapped, and allows the indices to extend beyond the length of the
     * BitVector. Any bits requested beyond the length of the BitVector are
     * returned as 0.
     *
     * @param bv the BitVector to get the bit slice from
     * @param high the "high" index
     * @param low the "low" index
     * @return the bit slice
     */
    public static BitVector getBitsChecked(BitVector bv, int high, int low)
    {
        if (high < low)
        {
            int temp = high;
            high = low;
            low = temp;
        }
        int inputLen = bv.length();
        int resultLen = high - low + 1;
        BitVector result;
        if (low < inputLen)
        {
            if (high >= inputLen)
            {
                high = inputLen - 1;
                result = bv.getBits(high, low).setLength(resultLen, Bit.ZERO);
            }
            else
            {
                result = bv.getBits(high, low);
            }
        }
        else
        {
            result = new BitVector(resultLen, Bit.ZERO);
        }
        return result;
    }

    /**
     * Wrapper for BitVector.setBits() that allows high and low indices to be
     * swapped.
     *
     * @param bv the BitVector to set the bit slice in
     * @param high the "high" index
     * @param low the "low" index
     * @param value the bit slice to store
     * @return the new BitVector with the bit slice written
     */
    public static BitVector setBits(
        BitVector bv,
        int high,
        int low,
        BitVector value)
    {
        if (high < low)
        {
            int temp = high;
            high = low;
            low = temp;
        }
        return bv.setBits(high, low, value);
    }

    /**
     * Generates a bit mask of index+1 bits with the index bit set to ONE, and
     * any lower-order bits set to ZERO.
     *
     * @param index the index of the bit to set to ONE
     * @return a bit vector representing a mask containing the given bit
     */
    public static BitVector getMask(int index)
    {
        BitVectorBuffer buf = new BitVectorBuffer(index + 1, Bit.ZERO);
        buf.setBit(index, Bit.ONE);
        return buf.toBitVector();
    }

    /**
     * Generates a bit mask of high+1 bits with each bit between high and low
     * (inclusive) set to ONE, and any lower-order bits set to ZERO.
     *
     * @param high the index of the highest bit to set to ONE
     * @param low the index of the lowest bit to set to ONE
     * @return a bit vector representing a mask containing the given bit range
     */
    public static BitVector getMask(int high, int low)
    {
        if (high < low)
        {
            int temp = high;
            high = low;
            low = temp;
        }
        BitVectorBuffer buf = new BitVectorBuffer(high + 1, Bit.ZERO);
        buf.fillBits(high, low, Bit.ONE);
        return buf.toBitVector();
    }

    public static BitVector promote(BitVector bv, int minLen)
    {
        return bv.length() < minLen ? bv.setLength(minLen) : bv;
    }
}
