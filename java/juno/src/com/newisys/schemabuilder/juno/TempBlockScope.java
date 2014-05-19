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

import java.util.Iterator;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Scope;
import com.newisys.langschema.util.NameTable;

/**
 * Scope implementation that maintains a name table and also delegates lookups
 * to a base scope (if any). Used to maintain temporary block scopes during
 * translation, so that temporary variables can be assigned unique names.
 * 
 * @author Trevor Robinson
 */
class TempBlockScope
    implements Scope
{
    private final Scope baseScope;
    private final NameTable nameTable = new NameTable();

    public TempBlockScope()
    {
        this.baseScope = null;
    }

    public TempBlockScope(Scope baseScope)
    {
        this.baseScope = baseScope;
    }

    public void addObject(NamedObject obj)
    {
        nameTable.addObject(obj);
    }

    public void removeObject(NamedObject obj)
    {
        nameTable.removeObject(obj);
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        Iterator< ? extends NamedObject> iter = nameTable.lookupObjects(
            identifier, kind);
        if (!iter.hasNext() && baseScope != null)
        {
            iter = baseScope.lookupObjects(identifier, kind);
        }
        return iter;
    }
}
