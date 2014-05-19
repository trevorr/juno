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

import com.newisys.langschema.MemberVariable;

/**
 * Represents a Vera class member variable.
 * 
 * @author Trevor Robinson
 */
public final class VeraMemberVariable
    extends VeraVariable<VeraVariableModifier>
    implements MemberVariable, VeraClassMember
{
    private static final long serialVersionUID = 3618142268490331448L;

    private VeraClass cls;
    private VeraVisibility visibility;
    private VeraExpression randomSize;
    private boolean defined;

    public VeraMemberVariable(VeraName name, VeraType type)
    {
        super(name, type);
    }

    public VeraClass getStructuredType()
    {
        return cls;
    }

    public void setClass(VeraClass cls)
    {
        this.cls = cls;
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public VeraExpression getRandomSize()
    {
        return randomSize;
    }

    public void setRandomSize(VeraExpression randomSize)
    {
        this.randomSize = randomSize;
    }

    public boolean isDefined()
    {
        return defined;
    }

    public void setDefined(boolean defined)
    {
        this.defined = defined;
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
