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
 * <code>boolean</code> conversion and operation methods referenced by
 * translated code.
 * 
 * @author Trevor Robinson
 */
public final class BooleanOp
{
    private BooleanOp()
    {
    }

    public static boolean toBoolean(Bit b, boolean xzTrue)
    {
        return !xzTrue ? b == Bit.ONE : b != Bit.ZERO;
    }

    public static boolean toBoolean(BitVector bv)
    {
        return !bv.containsXZ() && bv.isNotZero();
    }

    public static boolean toBooleanXZTrue(BitVector bv)
    {
        return bv.containsXZ() || bv.isNotZero();
    }

    public static boolean toBoolean(
        BitVector bv,
        boolean lowBitOnly,
        boolean xzTrue)
    {
        if (!lowBitOnly)
        {
            return !xzTrue ? toBoolean(bv) : toBooleanXZTrue(bv);
        }
        else
        {
            return toBoolean(bv.getBit(0), xzTrue);
        }
    }

    public static boolean toBoolean(Object o)
    {
        return toBoolean(o, false, false);
    }

    public static boolean toBoolean(Object o, boolean lowBitOnly, boolean xzTrue)
    {
        if (o instanceof Boolean)
        {
            return ((Boolean) o).booleanValue();
        }
        else if (o instanceof Number)
        {
            long l = ((Number) o).longValue();
            if (lowBitOnly) l &= 1;
            return l != 0;
        }
        else if (o instanceof BitVector)
        {
            return toBoolean((BitVector) o, lowBitOnly, xzTrue);
        }
        else if (o instanceof Bit)
        {
            return toBoolean((Bit) o, xzTrue);
        }
        else if (o instanceof String)
        {
            return toBoolean((String) o);
        }
        else
        {
            throw new ClassCastException("Unknown value type");
        }
    }

    public static boolean toBoolean(String s)
    {
        return s != null && s.length() > 0;
    }

    public static boolean divide(boolean b1, boolean b2)
    {
        if (!b2)
        {
            throw new ArithmeticException("boolean divide by zero");
        }
        return b1 & b2;
    }

    public static boolean mod(boolean b1, boolean b2)
    {
        if (!b2)
        {
            throw new ArithmeticException("boolean divide by zero");
        }
        return false;
    }
}
