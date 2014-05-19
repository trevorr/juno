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

import java.util.HashSet;
import java.util.Set;

import com.newisys.langschema.Variable;
import com.newisys.langschema.VariableModifier;

/**
 * Base class for Vera variables.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraVariable<M extends VariableModifier>
    extends VeraSchemaObjectImpl
    implements Variable
{
    // NOTE: name is not final because function arguments can change names
    protected VeraName name;
    private final VeraType type;
    protected final Set<M> modifiers = new HashSet<M>();
    private VeraExpression initializer;

    public VeraVariable(VeraName name, VeraType type)
    {
        super(type.schema);
        this.name = name;
        this.type = type;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraType getType()
    {
        return type;
    }

    public Set<M> getModifiers()
    {
        return modifiers;
    }

    public boolean hasModifier(VariableModifier mod)
    {
        return modifiers.contains(mod);
    }

    public void addModifier(M mod)
    {
        modifiers.add(mod);
    }

    public VeraExpression getInitializer()
    {
        return initializer;
    }

    public void setInitializer(VeraExpression initializer)
    {
        this.initializer = initializer;
    }

    public String toDebugString()
    {
        return "variable " + name + " of type " + type.toReferenceString();
    }
}
