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
 * Represents the Vera integer type.
 * 
 * @author Trevor Robinson
 */
public final class VeraIntegerType
    extends VeraPrimitiveType
{
    private static final long serialVersionUID = 3544948857550025785L;

    VeraIntegerType(VeraSchema schema)
    {
        super(schema);
    }

    public boolean isAssignableFrom(Type other)
    {
        return other instanceof VeraIntegerType
            || other instanceof VeraBitVectorType
            || other instanceof VeraBitType || other instanceof VeraEnumeration
            || other instanceof VeraStringType;
    }

    public boolean isStrictIntegral()
    {
        return true;
    }

    public int getBitCount()
    {
        return 32;
    }

    public String toReferenceString()
    {
        return "integer";
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
