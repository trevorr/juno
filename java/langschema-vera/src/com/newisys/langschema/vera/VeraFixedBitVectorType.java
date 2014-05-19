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

/**
 * Represents a Vera fixed-size bit vector type.
 * 
 * @author Trevor Robinson
 */
public final class VeraFixedBitVectorType
    extends VeraBitVectorType
{
    private static final long serialVersionUID = 3257281452742816819L;

    private final int size;

    public VeraFixedBitVectorType(VeraSchema schema, int size)
    {
        super(schema);
        assert (size <= MAX_SIZE);
        this.size = size;
    }

    public int getSize()
    {
        return size;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof VeraFixedBitVectorType)
        {
            VeraFixedBitVectorType other = (VeraFixedBitVectorType) obj;
            return size == other.size;
        }
        return false;
    }

    public int hashCode()
    {
        return VeraFixedBitVectorType.class.hashCode() ^ size;
    }

    public String toReferenceString()
    {
        return "bit[" + (size - 1) + ":0]";
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
