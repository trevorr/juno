/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.schemabuilder.vera;

import java.util.Iterator;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Scope delegate that delegates to a) its own name table, and then b) the
 * enclosing scope (if any).
 * 
 * @author Trevor Robinson
 */
public final class VeraAnonymousScope
    extends VeraScopeDelegate
{
    private final NameTable nameTable = new NameTable();

    public VeraAnonymousScope()
    {
        super(null);
    }

    public VeraAnonymousScope(VeraScopeDelegate enclosingScope)
    {
        super(enclosingScope);
    }

    public void addObject(NamedObject obj)
    {
        nameTable.addObject(obj);
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        Iterator< ? extends NamedObject> iter = nameTable.lookupObjects(
            identifier, kind);
        if (!iter.hasNext() && enclosingScope != null)
        {
            iter = enclosingScope.lookupObjects(identifier, kind);
        }
        return iter;
    }
}
