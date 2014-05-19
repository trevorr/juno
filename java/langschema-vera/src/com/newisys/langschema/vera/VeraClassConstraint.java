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

import com.newisys.langschema.NamedObject;

/**
 * Represents a Vera class random contraint set.
 * 
 * @author Trevor Robinson
 */
public final class VeraClassConstraint
    extends VeraConstraintSet
    implements NamedObject, VeraClassMember
{
    private static final long serialVersionUID = 3257007674313880116L;

    private final VeraName name;
    private VeraClass cls;
    private boolean defined;

    public VeraClassConstraint(VeraSchema schema, VeraName name)
    {
        super(schema);
        this.name = name;
    }

    public VeraName getName()
    {
        return name;
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
        return VeraVisibility.PUBLIC;
    }

    public boolean isDefined()
    {
        return defined;
    }

    public void setDefined(boolean defined)
    {
        this.defined = defined;
    }

    public void accept(VeraClassMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
