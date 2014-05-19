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
 * Represents the Vera string type.
 * 
 * @author Trevor Robinson
 */
public final class VeraStringType
    extends VeraSystemClass
{
    private static final long serialVersionUID = 3690199823924212279L;

    VeraStringType(VeraSchema schema, VeraClass rootClass)
    {
        super(schema, new VeraName("string", VeraNameKind.TYPE, null),
            rootClass);
    }

    void defineStringClass()
    {
        VeraFunction f;

        f = defineMemberFunction("atobin", schema.bitVectorType);
        // no arguments

        f = defineMemberFunction("atohex", schema.bitVectorType);
        // no arguments

        f = defineMemberFunction("atoi", schema.integerType);
        // no arguments

        f = defineMemberFunction("atooct", schema.bitVectorType);
        // no arguments

        f = defineMemberFunction("backref", schema.stringType);
        addArgument(f, "index", schema.integerType);

        f = defineMemberFunction("bittostr", schema.voidType);
        addArgument(f, "bits", schema.bitVectorType);

        f = defineMemberFunction("compare", schema.integerType);
        addArgument(f, "other", schema.stringType);

        f = defineMemberFunction("get_status", schema.integerType);
        // no arguments

        f = defineMemberFunction("get_status_msg", schema.stringType);
        // no arguments

        f = defineMemberFunction("getc", schema.integerType);
        addArgument(f, "index", schema.integerType);

        f = defineMemberFunction("hash", schema.integerType);
        addArgument(f, "size", schema.integerType);

        f = defineMemberFunction("icompare", schema.integerType);
        addArgument(f, "other", schema.stringType);

        f = defineMemberFunction("itoa", schema.voidType);
        addArgument(f, "i", schema.integerType);

        f = defineMemberFunction("len", schema.integerType);
        // no arguments

        f = defineMemberFunction("match", schema.integerType);
        addArgument(f, "pattern", schema.stringType);

        f = defineMemberFunction("postmatch", schema.stringType);
        // no arguments

        f = defineMemberFunction("prematch", schema.stringType);
        // no arguments

        f = defineMemberFunction("putc", schema.voidType);
        addArgument(f, "index", schema.integerType);
        addArgument(f, "char", schema.magicType);

        f = defineMemberFunction("search", schema.integerType);
        addArgument(f, "pattern", schema.stringType);

        f = defineMemberFunction("substr", schema.stringType);
        addArgument(f, "first", schema.integerType);
        addOptArgument(f, "last", schema.integerType);

        f = defineMemberFunction("thismatch", schema.stringType);
        // no arguments

        f = defineMemberFunction("tolower", schema.stringType);
        // no arguments

        f = defineMemberFunction("toupper", schema.stringType);
        // no arguments
    }

    public boolean isAssignableFrom(Type other)
    {
        return other instanceof VeraStringType || other instanceof VeraNullType;
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
