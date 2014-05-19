/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.langschema.vera;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Class;
import com.newisys.langschema.Interface;
import com.newisys.langschema.Type;

/**
 * Base class for Vera class types.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraClass
    extends VeraStructuredType<VeraClassMember>
    implements Class
{
    private static final long serialVersionUID = 3570944955271123003L;

    protected boolean virtual;
    protected VeraClass baseClass;
    protected final List<VeraExpression> baseCtorArgs = new LinkedList<VeraExpression>();

    public VeraClass(VeraSchema schema, VeraName name, VeraClass baseClass)
    {
        super(schema, name);
        this.baseClass = baseClass;
    }

    public Set<VeraClassModifier> getModifiers()
    {
        if (virtual) return Collections.singleton(VeraClassModifier.VIRTUAL);
        return Collections.emptySet();
    }

    public boolean isVirtual()
    {
        return virtual;
    }

    public List<VeraClass> getBaseClasses()
    {
        return Collections.singletonList(baseClass);
    }

    public VeraClass getBaseClass()
    {
        return baseClass;
    }

    public List< ? extends Interface> getBaseInterfaces()
    {
        return Collections.emptyList();
    }

    public List<VeraExpression> getBaseCtorArgs()
    {
        return baseCtorArgs;
    }

    public void addMember(VeraClassMember member)
    {
        assert (member.getStructuredType() == null);
        member.setClass(this);
        super.addMember(member);
    }

    public boolean isAssignableFrom(Type other)
    {
        // quick check for identical or null type
        if (other == this || other instanceof VeraNullType)
        {
            return true;
        }
        else if (other instanceof VeraClass)
        {
            // check whether this class is a superclass of the given class
            return isSuperclassOf((VeraClass) other);
        }
        return false;
    }

    public boolean isSuperclassOf(VeraClass other)
    {
        while (other != null)
        {
            if (other == this) return true;
            other = other.getBaseClass();
        }
        return false;
    }

    public String toDebugString()
    {
        return "class " + name;
    }
}
