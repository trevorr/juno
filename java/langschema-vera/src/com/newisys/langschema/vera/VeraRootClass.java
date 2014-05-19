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
 * Represents the (unnamed) Vera root class, which contains the built-in
 * functions of every class.
 * 
 * @author Trevor Robinson
 */
public class VeraRootClass
    extends VeraSystemClass
{
    private static final long serialVersionUID = 3691034374740916534L;

    VeraRootClass(VeraSchema schema)
    {
        super(schema, new VeraName("<root>", VeraNameKind.TYPE, null), null);
    }

    void defineRootClass()
    {
        VeraMemberFunction f;
        VeraFunctionArgument a;

        f = defineMemberFunction("constraint_mode", schema.integerType);
        addArgument(f, "action", schema.integerType);
        addOptArgument(f, "constraint_name", schema.stringType);

        f = defineMemberFunction("finalize", schema.voidType);
        f.setVirtual(true);
        // no arguments
        
        f = defineMemberFunction("object_compare", schema.integerType);
        addArgument(f, "other", schema.magicType);

        f = defineMemberFunction("object_copy", schema.magicType);
        // no arguments

        f = defineMemberFunction("object_print", schema.voidType);
        addOptArgument(f, "fd", 1);
        addOptArgument(f, "attributes", schema.stringType);

        f = defineMemberFunction("pack", schema.integerType);
        addArgument(f, "array", schema.magicType);
        a = addArgument(f, "index", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "left", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "right", schema.integerType);
        a.setByRef(true);

        f = defineMemberFunction("post_pack", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("post_randomize", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("post_unpack", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("pre_pack", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("pre_randomize", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("pre_unpack", schema.voidType);
        f.setVirtual(true);
        // no arguments

        f = defineMemberFunction("rand_mode", schema.integerType);
        addArgument(f, "action", schema.integerType);
        addOptArgument(f, "variable_name", schema.stringType);
        addOptArgument(f, "index", schema.integerType);

        f = defineMemberFunction("randomize", schema.integerType);
        // no arguments

        f = defineMemberFunction("unpack", schema.bitVector64Type);
        addArgument(f, "array", schema.magicType);
        a = addArgument(f, "index", schema.bitVector64Type);
        a.setByRef(true);
        a = addArgument(f, "left", schema.bitVector64Type);
        a.setByRef(true);
        a = addArgument(f, "right", schema.bitVector64Type);
        a.setByRef(true);
    }
}
