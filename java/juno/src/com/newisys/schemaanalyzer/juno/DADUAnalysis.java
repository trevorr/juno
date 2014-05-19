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

import com.newisys.langschema.vera.VeraVariable;

/**
 * Manages the current state of a DA/DU analysis. The class is simply a facade
 * around a DADUAllocator and a DADUState. It provides both index-based and
 * variable reference-based access to the DA/DU state, to optimize cases where
 * the index is known by the caller, and to simplify cases where it is not.
 * When the DA/DU analysis is split, to analyze a branch, only the DADUState is
 * duplicated; the same DADUAllocator is used by both instances.
 * 
 * @author Trevor Robinson
 */
final class DADUAnalysis
{
    private final DADUAllocator allocator;
    private final DADUState state;

    public DADUAnalysis()
    {
        allocator = new DADUAllocator();
        state = new DADUState();
    }

    private DADUAnalysis(DADUAnalysis other)
    {
        allocator = other.allocator;
        state = other.state.duplicate();
    }

    public DADUAnalysis duplicate()
    {
        return new DADUAnalysis(this);
    }

    public void merge(final DADUAnalysis other)
    {
        assert (other.allocator == allocator);
        state.merge(other.state);
    }

    public int beginBlock()
    {
        return allocator.getNextIndex();
    }

    public void endBlock(int index)
    {
        int nextIndex = allocator.getNextIndex();
        if (index < nextIndex)
        {
            allocator.revertToNextIndex(index);
            state.clear(index, nextIndex);
        }
    }

    public int alloc(final VeraVariable var)
    {
        final int index = allocator.alloc(var);
        state.init(index);
        return index;
    }

    public int getIndex(final VeraVariable var)
    {
        return allocator.getIndex(var);
    }

    public VeraVariable getVariable(final int index)
    {
        return allocator.getVariable(index);
    }

    public int getNextIndex()
    {
        return allocator.getNextIndex();
    }

    public void markAssigned(final int index)
    {
        state.markAssigned(index);
    }

    public void markAssigned(final VeraVariable var)
    {
        final int index = allocator.getIndex(var);
        assert (index >= 0);
        state.markAssigned(index);
    }

    public void markDead()
    {
        state.markDead(0, allocator.getNextIndex());
    }

    public void clearDU(final DADUAnalysis other)
    {
        state.clearDU(other.state);
    }

    public boolean containsDU()
    {
        return state.containsDU(0);
    }

    public boolean isDA(final int index)
    {
        return state.isDA(index);
    }

    public boolean isDA(final VeraVariable var)
    {
        final int index = allocator.getIndex(var);
        assert (index >= 0);
        return state.isDA(index);
    }

    public boolean isDU(final int index)
    {
        return state.isDU(index);
    }

    public boolean isDU(final VeraVariable var)
    {
        final int index = allocator.getIndex(var);
        assert (index >= 0);
        return state.isDU(index);
    }

    public String toString()
    {
        final StringBuffer buf = new StringBuffer(80);
        final int nextIndex = allocator.getNextIndex();
        for (int index = 0; index < nextIndex; ++index)
        {
            if (index > 0) buf.append(", ");
            final VeraVariable var = allocator.getVariable(index);
            final boolean isDA = state.isDA(index);
            boolean isDU = state.isDU(index);
            buf.append(var.getName());
            buf.append('[');
            buf.append(isDA ? (isDU ? "DEAD" : "DA") : (isDU ? "DU" : "?"));
            buf.append(']');
        }
        return buf.toString();
    }
}
