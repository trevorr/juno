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

import com.newisys.langschema.Type;

/**
 * Base class for Vera bit vector types.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraBitVectorType
    extends VeraPrimitiveType
{
    // Vera 6.2 allows 65535 bits (not 65536!)
    public static final int MAX_SIZE = 65535;

    public VeraBitVectorType(VeraSchema schema)
    {
        super(schema);
    }

    public int getSize()
    {
        return -1;
    }

    public boolean isAssignableFrom(Type other)
    {
        return other instanceof VeraBitVectorType
            || other instanceof VeraBitType || other instanceof VeraIntegerType
            || other instanceof VeraEnumeration
            || other instanceof VeraStringType;
    }

    public boolean isStrictIntegral()
    {
        return true;
    }

    public int getBitCount()
    {
        return getSize();
    }

    public String toReferenceString()
    {
        return "bit[?:0]";
    }
}
