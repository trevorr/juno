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
import com.newisys.langschema.VoidType;

/**
 * Represents the Vera void type.
 * 
 * @author Trevor Robinson
 */
public final class VeraVoidType
    extends VeraType
    implements VoidType
{
    private static final long serialVersionUID = 4050483434621449526L;

    VeraVoidType(VeraSchema schema)
    {
        super(schema);
    }

    public boolean isAssignableFrom(Type other)
    {
        // any type is assignable to void
        return true;
    }

    public String toReferenceString()
    {
        return "void";
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
