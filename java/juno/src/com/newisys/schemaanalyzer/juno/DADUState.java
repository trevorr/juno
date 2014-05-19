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

package com.newisys.schemaanalyzer.juno;

import java.util.BitSet;

/**
 * Compactly represents the current state of a DA/DU analysis. The interface of
 * this class is in terms of integer indexes assigned (by DADUAllocator) to
 * each variable in the analysis.
 * 
 * @author Trevor Robinson
 */
final class DADUState
{
    private final BitSet daBits;
    private final BitSet duBits;

    public DADUState()
    {
        daBits = new BitSet();
        duBits = new BitSet();
    }

    private DADUState(DADUState other)
    {
        daBits = (BitSet) other.daBits.clone();
        duBits = (BitSet) other.duBits.clone();
    }

    public DADUState duplicate()
    {
        return new DADUState(this);
    }

    public void merge(final DADUState other)
    {
        daBits.and(other.daBits);
        duBits.and(other.duBits);
    }

    public void init(final int index)
    {
        daBits.clear(index);
        duBits.set(index);
    }

    public void clear(final int fromIndex, final int toIndex)
    {
        daBits.clear(fromIndex, toIndex);
        duBits.clear(fromIndex, toIndex);
    }

    public void markAssigned(final int index)
    {
        daBits.set(index);
        duBits.clear(index);
    }

    public void markDead(final int fromIndex, final int toIndex)
    {
        daBits.set(fromIndex, toIndex);
        duBits.set(fromIndex, toIndex);
    }

    public void clearDU(final DADUState other)
    {
        duBits.andNot(other.duBits);
    }

    public void assignDU(final DADUState other)
    {
        duBits.clear();
        duBits.or(other.duBits);
    }

    public boolean containsDU(final int fromIndex)
    {
        return duBits.nextSetBit(fromIndex) >= 0;
    }

    public boolean isDA(final int index)
    {
        return daBits.get(index);
    }

    public boolean isDU(final int index)
    {
        return duBits.get(index);
    }

    @Override
    public String toString()
    {
        return "DA:" + daBits + ", DU:" + duBits;
    }
}
