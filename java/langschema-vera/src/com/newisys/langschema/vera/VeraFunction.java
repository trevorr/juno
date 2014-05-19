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
import java.util.Set;

import com.newisys.langschema.Function;

/**
 * Base class for Vera functions.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraFunction
    extends VeraSchemaObjectImpl
    implements Function
{
    protected final VeraName name;
    private final VeraFunctionType funcType;
    private VeraVisibility visibility;
    private VeraBlock body;
    private VeraLocalVariable returnVar;
    private boolean defined;

    public VeraFunction(VeraName name, VeraFunctionType funcType)
    {
        super(funcType.schema);
        this.name = name;
        this.funcType = funcType;
        this.visibility = VeraVisibility.PUBLIC;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraFunctionType getType()
    {
        return funcType;
    }

    public Set< ? extends VeraFunctionModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public VeraBlock getBody()
    {
        return body;
    }

    public void setBody(VeraBlock body)
    {
        this.body = body;
    }

    public VeraLocalVariable getReturnVar()
    {
        return returnVar;
    }

    public void setReturnVar(VeraLocalVariable returnVar)
    {
        this.returnVar = returnVar;
    }

    public boolean isDefined()
    {
        return defined;
    }

    public void setDefined(boolean defined)
    {
        this.defined = defined;
    }

    public String toDebugString()
    {
        return "function " + name;
    }
}
