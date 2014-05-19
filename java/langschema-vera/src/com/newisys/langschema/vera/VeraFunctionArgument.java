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

import com.newisys.langschema.FunctionArgument;

/**
 * Represents a Vera function argument.
 * 
 * @author Trevor Robinson
 */
public final class VeraFunctionArgument
    extends VeraVariable<VeraFunctionArgumentModifier>
    implements FunctionArgument
{
    private static final long serialVersionUID = 3256727294620807729L;

    private final VeraFunction function;
    private boolean optional;
    private int optionalLevel;
    private boolean returnsXZ;

    public VeraFunctionArgument(
        VeraName name,
        VeraType type,
        VeraFunction function)
    {
        super(name, type);
        this.function = function;
    }

    public void setName(VeraName name)
    {
        this.name = name;
    }

    public VeraFunction getFunction()
    {
        return function;
    }

    public boolean isByRef()
    {
        return modifiers.contains(VeraFunctionArgumentModifier.VAR);
    }

    public void setByRef(boolean byRef)
    {
        if (byRef)
        {
            modifiers.add(VeraFunctionArgumentModifier.VAR);
        }
        else
        {
            modifiers.remove(VeraFunctionArgumentModifier.VAR);
        }
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public int getOptionalLevel()
    {
        return optionalLevel;
    }

    public void setOptionalLevel(int optionalLevel)
    {
        this.optionalLevel = optionalLevel;
    }

    public boolean isReturnsXZ()
    {
        return returnsXZ;
    }

    public void setReturnsXZ(boolean returnsXZ)
    {
        this.returnsXZ = returnsXZ;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
