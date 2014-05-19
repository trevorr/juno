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

import com.newisys.langschema.ArrayType;
import com.newisys.langschema.Type;

/**
 * Base class for Vera array types.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraArrayType
    extends VeraType
    implements ArrayType
{
    protected final VeraType elementType;
    protected final VeraType[] indexTypes;

    public VeraArrayType(VeraType elementType, VeraType[] indexTypes)
    {
        super(elementType.schema);
        this.elementType = elementType;
        this.indexTypes = indexTypes;
    }

    public VeraType getElementType()
    {
        return elementType;
    }

    public VeraType[] getIndexTypes()
    {
        return indexTypes;
    }

    public boolean equals(Object obj)
    {
        if (this.getClass() == obj.getClass())
        {
            VeraArrayType other = (VeraArrayType) obj;
            if (elementType.equals(other.elementType))
            {
                if (indexTypes.length == other.indexTypes.length)
                {
                    for (int i = 0; i < indexTypes.length; ++i)
                    {
                        if (!indexTypes[i].equals(other.indexTypes[i]))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode()
    {
        int h = getClass().hashCode() ^ elementType.hashCode();
        for (int i = 0; i < indexTypes.length; ++i)
        {
            h = (h * 37) ^ indexTypes[i].hashCode();
        }
        return h;
    }

    public boolean isAssignableFrom(Type other)
    {
        // array types must be identical
        return equals(other);
    }
}
