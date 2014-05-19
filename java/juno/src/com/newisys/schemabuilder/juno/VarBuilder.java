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

import java.util.Iterator;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.Scope;
import com.newisys.langschema.java.JavaLocalVariable;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaNameKind;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaType;

/**
 * Provides utility methods for creating new Java variable declarations.
 * 
 * @author Trevor Robinson
 */
final class VarBuilder
{
    public static JavaLocalVariable createLocalVar(
        Scope localScope,
        String id,
        JavaType type)
    {
        id = uniquifyID(localScope, id, JavaNameKind.EXPRESSION);
        JavaLocalVariable var = new JavaLocalVariable(id, type);
        return var;
    }

    public static JavaMemberVariable createMemberVar(
        JavaRawAbstractClass cls,
        String id,
        JavaType type)
    {
        id = uniquifyID(cls, id, JavaNameKind.EXPRESSION);
        JavaMemberVariable var = new JavaMemberVariable(id, type);
        return var;
    }

    public static String uniquifyID(Scope scope, String id, NameKind kind)
    {
        String curID = id;
        if (containsID(scope, curID, kind))
        {
            boolean endsWithDigit = Character.isDigit(id
                .charAt(id.length() - 1));
            int nextCount = 2;
            do
            {
                curID = id + (endsWithDigit ? "_" : "") + nextCount++;
            }
            while (containsID(scope, curID, kind));
        }
        return curID;
    }

    public static boolean containsID(Scope scope, String id, NameKind kind)
    {
        Iterator iter = scope.lookupObjects(id, kind);
        return iter.hasNext();
    }
}
