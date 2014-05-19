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
import java.util.LinkedHashSet;
import java.util.Set;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraCompilationUnitMember;
import com.newisys.langschema.vera.VeraVisibility;

/**
 * Scope delegate that delegates to a) a given set of non-header compilation
 * units, then b) a given set of header compilation units, and finally c) the
 * enclosing scope (if any). When delegating to header compilation units (i.e.
 * those included by a #include of a .vrh file), only members with non-local
 * visibility are considered.
 * 
 * @author Trevor Robinson
 */
public final class VeraCompilationUnitScope
    extends VeraScopeDelegate
{
    private final Set<VeraCompilationUnit> compUnits = new LinkedHashSet<VeraCompilationUnit>();
    private final Set<VeraCompilationUnit> headerCompUnits = new LinkedHashSet<VeraCompilationUnit>();
    private VeraCompilationUnit currentCompUnit;

    public VeraCompilationUnitScope(VeraScopeDelegate enclosingScope)
    {
        super(enclosingScope);
        assert (enclosingScope != null);
    }

    public void addCompilationUnit(VeraCompilationUnit compUnit, boolean header)
    {
        if (header)
        {
            headerCompUnits.add(compUnit);
        }
        else
        {
            compUnits.add(compUnit);
        }
    }

    public void setCurrentUnit(VeraCompilationUnit compUnit)
    {
        currentCompUnit = compUnit;
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        // search in non-header compilation units
        Iterator<VeraCompilationUnit> compUnitIter = compUnits.iterator();
        while (compUnitIter.hasNext())
        {
            VeraCompilationUnit compUnit = compUnitIter.next();
            Iterator<NamedObject> iter = compUnit.lookupObjects(identifier,
                kind);
            if (iter.hasNext()) return iter;
        }

        // search in header compilation units
        compUnitIter = headerCompUnits.iterator();
        while (compUnitIter.hasNext())
        {
            VeraCompilationUnit compUnit = compUnitIter.next();
            Iterator<NamedObject> iter = compUnit.lookupObjects(identifier,
                kind);
            if (compUnit != currentCompUnit)
            {
                iter = new NonLocalIterator(iter);
            }
            if (iter.hasNext()) return iter;
        }

        // search in enclosing scope
        return enclosingScope.lookupObjects(identifier, kind);
    }

    private static class NonLocalIterator
        implements Iterator<NamedObject>
    {
        private final Iterator<NamedObject> iter;
        private NamedObject next;

        public NonLocalIterator(Iterator<NamedObject> iter)
        {
            this.iter = iter;
            findNext();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Iterator.remove()");
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public NamedObject next()
        {
            NamedObject result = next;
            findNext();
            return result;
        }

        private void findNext()
        {
            next = null;
            while (iter.hasNext())
            {
                NamedObject obj = iter.next();
                VeraCompilationUnitMember member = (VeraCompilationUnitMember) obj;
                if (member.getVisibility() != VeraVisibility.LOCAL)
                {
                    next = obj;
                    break;
                }
            }
        }
    }
}
