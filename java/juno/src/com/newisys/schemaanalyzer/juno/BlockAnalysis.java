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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.vera.VeraVariable;

/**
 * Contains the analysis state for a particular statement block.
 * 
 * @author Trevor Robinson
 */
public final class BlockAnalysis
{
    private final BlockAnalysis outerAnalysis;
    private final Map<VeraVariable, AccessType> localAccessMap = new HashMap<VeraVariable, AccessType>();
    private boolean needThreadContext;
    DADUAnalysis dadu;

    public BlockAnalysis(DADUAnalysis dadu)
    {
        this.outerAnalysis = null;
        this.dadu = dadu;
    }

    public BlockAnalysis(BlockAnalysis outerAnalysis)
    {
        this.outerAnalysis = outerAnalysis;
        this.dadu = outerAnalysis.dadu;
    }

    private AccessType getAccess(VeraVariable var)
    {
        AccessType access = localAccessMap.get(var);
        return access != null ? access : AccessType.VOID;
    }

    public Set<VeraVariable> getLocalRefs()
    {
        return localAccessMap.keySet();
    }

    public boolean isLocalRead(VeraVariable var)
    {
        return getAccess(var).isRead();
    }

    public boolean isLocalWrite(VeraVariable var)
    {
        return getAccess(var).isWrite();
    }

    void addLocalAccess(VeraVariable var, boolean read, boolean write)
    {
        AccessType oldAccess = getAccess(var);
        AccessType newAccess = AccessType.getInstance(read
            || oldAccess.isRead(), write || oldAccess.isWrite());
        localAccessMap.put(var, newAccess);

        if (outerAnalysis != null)
        {
            outerAnalysis.addLocalAccess(var, read, write);
        }
    }

    void removeLocalAccess(VeraVariable var)
    {
        localAccessMap.remove(var);

        if (outerAnalysis != null)
        {
            outerAnalysis.removeLocalAccess(var);
        }
    }

    public boolean isNeedThreadContext()
    {
        return needThreadContext;
    }

    void setNeedThreadContext(boolean needThreadContext)
    {
        this.needThreadContext = needThreadContext;
    }
}
