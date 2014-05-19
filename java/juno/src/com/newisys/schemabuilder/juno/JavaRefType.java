/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.schemabuilder.juno;

import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaTypeImpl;
import com.newisys.langschema.java.JavaTypeVisitor;

/**
 * Used internally to simulate a Java reference type. Used in the translation
 * of by-reference function arguments.
 * 
 * @author Trevor Robinson
 */
class JavaRefType
    extends JavaTypeImpl
{
    private static final long serialVersionUID = 1L;

    final JavaType targetType;

    public JavaRefType(JavaType targetType)
    {
        super(targetType.getSchema());
        this.targetType = targetType;
    }

    public JavaType toRealType()
    {
        return schema.getArrayType(targetType, 1);
    }

    public boolean isSubtype(JavaType type)
    {
        return targetType.isSubtype(type);
    }

    public String toInternalName()
    {
        throw new UnsupportedOperationException();
    }

    public String toReferenceString()
    {
        return targetType.toReferenceString() + "&";
    }

    public void accept(JavaTypeVisitor visitor)
    {
        // do nothing
    }
}
