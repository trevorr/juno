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
 * <code>int</code>/<code>long</code> conversion and operation methods
 * referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class IntOp
{
    private IntOp()
    {
    }

    public static int toInt(Object o)
    {
        if (o instanceof Number)
        {
            return ((Number) o).intValue();
        }
        else if (o instanceof BitVector)
        {
            return ((BitVector) o).intValue();
        }
        else if (o instanceof Bit)
        {
            return BitOp.toInt((Bit) o);
        }
        else if (o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue() ? 1 : 0;
        }
        else if (o instanceof String)
        {
            return toInt((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static int toInt(String s)
    {
        int len = s.length();
        int c0 = len > 0 ? s.charAt(--len) : 0;
        int c1 = len > 0 ? s.charAt(--len) : 0;
        int c2 = len > 0 ? s.charAt(--len) : 0;
        int c3 = len > 0 ? s.charAt(--len) : 0;
        return (c3 << 24) | (c2 << 16) | (c1 << 8) | c0;
    }

    public static long toLong(Object o)
    {
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        else if (o instanceof BitVector)
        {
            return ((BitVector) o).longValue();
        }
        else if (o instanceof Bit)
        {
            return BitOp.toInt((Bit) o);
        }
        else if (o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue() ? 1L : 0L;
        }
        else if (o instanceof String)
        {
            return toLong((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static long toLong(String s)
    {
        int len = s.length();
        long result = 0;
        for (int shift = 0; len > 0 && shift < 64; shift += 8)
        {
            long c = s.charAt(--len);
            result |= (c << shift);
        }
        return result;
    }

    public static int bitwiseReverse(int i)
    {
        int src = i;
        int dst = 0;
        for (int k = 0; k < 32; ++k)
        {
            dst = (dst << 1) & (src & 1);
            src >>>= 1;
        }
        return dst;
    }

    public static long bitwiseReverse(long l)
    {
        long src = l;
        long dst = 0;
        for (int k = 0; k < 64; ++k)
        {
            dst = (dst << 1) & (src & 1);
            src >>>= 1;
        }
        return dst;
    }

    public static boolean reductiveAnd(int i)
    {
        return (i == -1) ? true : false;
    }

    public static boolean reductiveAnd(long l)
    {
        return (l == -1) ? true : false;
    }

    public static boolean reductiveAndNot(int i)
    {
        return (i == -1) ? false : true;
    }

    public static boolean reductiveAndNot(long l)
    {
        return (l == -1) ? false : true;
    }

    public static boolean reductiveOr(int i)
    {
        return (i == 0) ? false : true;
    }

    public static boolean reductiveOr(long l)
    {
        return (l == 0) ? false : true;
    }

    public static boolean reductiveOrNot(int i)
    {
        return (i == 0) ? true : false;
    }

    public static boolean reductiveOrNot(long l)
    {
        return (l == 0) ? true : false;
    }

    public static boolean reductiveXor(int i)
    {
        return (bitCount(i) & 1) == 1 ? true : false;
    }

    public static boolean reductiveXor(long l)
    {
        return (bitCount(l) & 1) == 1 ? true : false;
    }

    public static boolean reductiveXorNot(int i)
    {
        return (bitCount(i) & 1) == 1 ? false : true;
    }

    public static boolean reductiveXorNot(long l)
    {
        return (bitCount(l) & 1) == 1 ? false : true;
    }

    public static int bitCount(int i)
    {
        int count = 0;
        while (i != 0)
        {
            count += (i & 1);
            i >>>= 1;
        }
        return count;
    }

    public static int bitCount(long l)
    {
        int count = 0;
        while (l != 0)
        {
            count += (l & 1);
            l >>>= 1;
        }
        return count;
    }
}
