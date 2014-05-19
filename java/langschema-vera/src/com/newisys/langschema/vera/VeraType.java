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

import com.newisys.langschema.Type;
import com.newisys.langschema.TypeModifier;

/**
 * Base class for all Vera types.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraType
    extends VeraSchemaObjectImpl
    implements Type, VeraDefineReferrer<VeraTypeDefine>
{
    private VeraDefineReference<VeraTypeDefine> defineRef;

    public VeraType(VeraSchema schema)
    {
        super(schema);
    }

    public VeraDefineReference<VeraTypeDefine> getDefineRef()
    {
        return defineRef;
    }

    public void setDefineRef(VeraDefineReference<VeraTypeDefine> defineRef)
    {
        this.defineRef = defineRef;
    }

    public Set< ? extends TypeModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    public boolean isStrictIntegral()
    {
        return false;
    }

    public boolean isIntegralConvertible()
    {
        return isStrictIntegral();
    }

    public int getBitCount()
    {
        throw new UnsupportedOperationException(
            "Type is not integral convertible");
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        accept((VeraTypeVisitor) visitor);
    }

    public abstract void accept(VeraTypeVisitor visitor);

    public abstract String toReferenceString();

    public String toDebugString()
    {
        // for non-complex types, the short string is the reference string with
        // " type" appended
        return toReferenceString() + " type";
    }
}
