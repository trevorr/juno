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
import com.newisys.langschema.vera.VeraClass;
import com.newisys.langschema.vera.VeraClassMember;
import com.newisys.langschema.vera.VeraVisibility;

/**
 * Scope delegate that delegates to a) the given class, then b) its successive
 * base classes, and finally c) the enclosing scope (if any). When delegating
 * to base classes, only members with non-local visibility are considered.
 * 
 * @author Trevor Robinson
 */
public final class VeraClassScope
    extends VeraScopeDelegate
{
    private final VeraClass cls;

    public VeraClassScope(VeraClass cls, VeraScopeDelegate enclosingScope)
    {
        super(enclosingScope);
        this.cls = cls;
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        Iterator< ? extends NamedObject> iter = cls.lookupObjects(identifier,
            kind);
        VeraClass currentClass = cls;
        while (!iter.hasNext())
        {
            currentClass = currentClass.getBaseClass();
            if (currentClass == null)
            {
                if (enclosingScope != null)
                {
                    iter = enclosingScope.lookupObjects(identifier, kind);
                }
                break;
            }
            iter = new NonLocalIterator(currentClass.lookupObjects(identifier,
                kind));
        }
        return iter;
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
                VeraClassMember member = (VeraClassMember) obj;
                if (member.getVisibility() != VeraVisibility.LOCAL)
                {
                    next = obj;
                    break;
                }
            }
        }
    }
}
