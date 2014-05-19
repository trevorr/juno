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

import java.util.ArrayList;
import java.util.HashMap;

import com.newisys.langschema.vera.VeraVariable;

/**
 * Maintains a mapping of variables to integer indices used to reference DA/DU
 * state for each variable (by DADUState). The indices are maintained as a
 * stack: As DA/DU analysis descends into nested block scopes, indices are
 * sequentially allocated to each variable encountered. Upon leaving a block
 * scope, all variables from the scope are popped from the top of the stack. 
 * 
 * @author Trevor Robinson
 */
final class DADUAllocator
{
    private static final int INITIAL_CAPACITY = 32;

    private final ArrayList<VeraVariable> varArray = new ArrayList<VeraVariable>(
        INITIAL_CAPACITY);
    private final HashMap<VeraVariable, Integer> varIndexMap = new HashMap<VeraVariable, Integer>(
        INITIAL_CAPACITY);

    public int getNextIndex()
    {
        return varArray.size();
    }

    public void revertToNextIndex(final int index)
    {
        int size = varArray.size();
        assert (index <= size);
        while (size > index)
        {
            --size;
            VeraVariable var = varArray.remove(size);
            varIndexMap.remove(var);
        }
    }

    public int alloc(final VeraVariable var)
    {
        int index = varArray.size();
        varArray.add(var);
        varIndexMap.put(var, index);
        return index;
    }

    public VeraVariable getVariable(final int index)
    {
        return varArray.get(index);
    }

    public int getIndex(final VeraVariable var)
    {
        final Integer indexObj = varIndexMap.get(var);
        return indexObj != null ? indexObj.intValue() : -1;
    }

    @Override
    public String toString()
    {
        final int size = varArray.size();
        final StringBuffer buf = new StringBuffer(size * 20);
        for (int index = 0; index < size; ++index)
        {
            if (index > 0) buf.append(", ");
            buf.append(index);
            buf.append(':');
            final VeraVariable var = getVariable(index);

            buf.append(var.getName());
        }
        return buf.toString();
    }
}
