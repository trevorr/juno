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

package com.newisys.schemabuilder.juno;

import java.util.HashMap;
import java.util.Map;

import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.vera.VeraVariable;

/**
 * Provides a chainable mapping of Vera and Java variables to VarInfo objects
 * describing the translation of by-reference holder variables.
 * 
 * @author Trevor Robinson
 */
final class VarInfoMap
{
    private final Map<VeraVariable, VarInfo> veraMap = new HashMap<VeraVariable, VarInfo>();
    private final Map<JavaVariable, VarInfo> targetMap = new HashMap<JavaVariable, VarInfo>();
    private final VarInfoMap parent;

    public VarInfoMap()
    {
        this.parent = null;
    }

    public VarInfoMap(VarInfoMap parentMap)
    {
        this.parent = parentMap;
    }

    public void addInfo(VarInfo info)
    {
        veraMap.put(info.veraVar, info);
        targetMap.put(info.targetVar, info);
    }

    public VarInfo getInfo(VeraVariable var)
    {
        VarInfoMap cur = this;
        while (cur != null)
        {
            VarInfo info = cur.veraMap.get(var);
            if (info != null) return info;
            cur = cur.parent;
        }
        return null;
    }

    public VarInfo getInfo(JavaVariable var)
    {
        VarInfoMap cur = this;
        while (cur != null)
        {
            VarInfo info = cur.targetMap.get(var);
            if (info != null) return info;
            cur = cur.parent;
        }
        return null;
    }
}
