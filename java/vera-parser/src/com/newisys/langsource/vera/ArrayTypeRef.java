/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.langsource.vera;

/**
 * Array type references.
 * 
 * @author Trevor Robinson
 */
public class ArrayTypeRef
    extends TypeRef
{
    private ArrayKind arrayKind;
    protected TypeRef elementTypeRef;

    public ArrayTypeRef(ArrayKind arrayKind, TypeRef elementTypeRef)
    {
        super(TypeRefKind.ARRAY);
        this.arrayKind = arrayKind;
        this.elementTypeRef = elementTypeRef;
    }

    public final ArrayKind getArrayKind()
    {
        return arrayKind;
    }

    public final TypeRef getElementTypeRef()
    {
        return elementTypeRef;
    }

    public String toString()
    {
        String suffix;
        if (arrayKind == ArrayKind.DYNAMIC)
        {
            suffix = "[*]";
        }
        else if (arrayKind == ArrayKind.STRING_ASSOCIATIVE)
        {
            suffix = "[string]";
        }
        else
        {
            suffix = "[]";
        }
        return elementTypeRef.toString() + suffix;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
