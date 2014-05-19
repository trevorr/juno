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

import com.newisys.verilog.util.BitVector;

/**
 * BitVector-to-BitVector associative array.
 * 
 * @author Trevor Robinson
 */
public final class BitBitAssocArray
    extends BitAssocArray<BitVector>
{
    private final int valueLength;

    /**
     * Creates a BitBitAssocArray which has a default value of X with the
     * specified number of bits.
     *
     * @param valueLength the length of the default value
     */
    public BitBitAssocArray(int valueLength)
    {
        super(new BitVector(valueLength));
        this.valueLength = valueLength;
    }

    /**
     * Creates a BitBitAssocArray that is a copy of the specified array.
     *
     * @param other the array to copy
     */
    public BitBitAssocArray(BitBitAssocArray other)
    {
        super(other);
        this.valueLength = other.valueLength;
    }

    /**
     * Returns the length in bits of the default value
     *
     * @return the length of the default value
     */
    public int getValueLength()
    {
        return valueLength;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public BitBitAssocArray clone()
    {
        return new BitBitAssocArray(this);
    }
}
