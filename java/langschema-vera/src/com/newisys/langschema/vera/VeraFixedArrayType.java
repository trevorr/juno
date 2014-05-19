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

import java.util.Arrays;

import com.newisys.langschema.FixedArrayType;

/**
 * Represents a Vera fixed-dimension array type.
 * 
 * @author Trevor Robinson
 */
public final class VeraFixedArrayType
    extends VeraArrayType
    implements FixedArrayType
{
    private static final long serialVersionUID = 3978711687920498230L;

    private final int[] dimensions;

    public VeraFixedArrayType(VeraType elementType, int[] dimensions)
    {
        super(elementType, createIndexTypes(elementType.schema,
            dimensions.length));
        this.dimensions = dimensions;
    }

    private static VeraType[] createIndexTypes(VeraSchema schema, int dimensions)
    {
        VeraType[] types = new VeraType[dimensions];
        Arrays.fill(types, schema.integerType);
        return types;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    public int[] getLowBounds()
    {
        int[] lowBounds = new int[dimensions.length];
        return lowBounds;
    }

    public int[] getHighBounds()
    {
        int[] highBounds = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; ++i)
        {
            highBounds[i] = dimensions[i] - 1;
        }
        return highBounds;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof VeraFixedArrayType && super.equals(obj))
        {
            VeraFixedArrayType other = (VeraFixedArrayType) obj;
            for (int i = 0; i < dimensions.length; ++i)
            {
                if (dimensions[i] != other.dimensions[i])
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode()
    {
        int h = super.hashCode();
        for (int i = 0; i < dimensions.length; ++i)
        {
            h = (h * 37) ^ dimensions[i];
        }
        return h;
    }

    public String toReferenceString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(elementType);
        for (int i = 0; i < dimensions.length; ++i)
        {
            buf.append('[');
            buf.append(dimensions[i]);
            buf.append(']');
        }
        return buf.toString();
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
