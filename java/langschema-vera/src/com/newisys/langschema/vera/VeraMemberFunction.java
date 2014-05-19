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
import java.util.HashSet;
import java.util.Set;

import com.newisys.langschema.MemberFunction;

/**
 * Represents a Vera class member function.
 * 
 * @author Trevor Robinson
 */
public final class VeraMemberFunction
    extends VeraFunction
    implements MemberFunction, VeraClassMember
{
    private static final long serialVersionUID = 3257846580261237560L;

    private boolean virtual;
    private boolean pureVirtual;
    private boolean implicit;
    private Set<VeraFunctionModifier> modifiers;
    private VeraClass cls;

    public VeraMemberFunction(VeraName name, VeraFunctionType funcType)
    {
        super(name, funcType);
        modifiers = Collections.emptySet();
    }

    public Set<VeraFunctionModifier> getModifiers()
    {
        return modifiers;
    }

    private void setModifier(VeraFunctionModifier modifier, boolean on)
    {
        if (on)
        {
            if (modifiers == Collections.EMPTY_SET)
            {
                modifiers = new HashSet<VeraFunctionModifier>();
            }
            modifiers.add(modifier);
        }
        else if (modifiers.contains(modifier))
        {
            modifiers.remove(modifier);
        }
    }

    public boolean isConstructor()
    {
        return getName().getIdentifier().equals("new");
    }

    public boolean isVirtual()
    {
        return virtual;
    }

    public void setVirtual(boolean virtual)
    {
        this.virtual = virtual;
        setModifier(VeraFunctionModifier.VIRTUAL, virtual);
    }

    public boolean isPureVirtual()
    {
        return pureVirtual;
    }

    public void setPureVirtual(boolean pureVirtual)
    {
        this.pureVirtual = pureVirtual;
    }

    public boolean isImplicit()
    {
        return implicit;
    }

    public void setImplicit(boolean implicit)
    {
        this.implicit = implicit;
        setModifier(VeraFunctionModifier.IMPLICIT, implicit);
    }

    public VeraClass getStructuredType()
    {
        return cls;
    }

    public void setClass(VeraClass cls)
    {
        this.cls = cls;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraClassMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
