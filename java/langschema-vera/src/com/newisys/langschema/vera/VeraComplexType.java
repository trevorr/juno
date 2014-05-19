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

import com.newisys.langschema.ComplexType;

/**
 * Base class for Vera named, complex data/object types, such as classes or
 * enumerations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraComplexType
    extends VeraType
    implements ComplexType
{
    protected final VeraName name;
    private VeraVisibility visibility;

    public VeraComplexType(VeraSchema schema, VeraName name)
    {
        super(schema);
        this.name = name;
        visibility = VeraVisibility.PUBLIC;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public String toReferenceString()
    {
        return name.getCanonicalName();
    }
}
