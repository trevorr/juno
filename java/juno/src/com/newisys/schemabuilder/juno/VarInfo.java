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

import com.newisys.langschema.java.JavaArrayAccess;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaIntLiteral;
import com.newisys.langschema.java.JavaSchema;
import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.java.JavaVariableReference;
import com.newisys.langschema.vera.VeraVariable;

/**
 * Represents translation information for a particular Vera variable.
 * Used for dealing with holder variables when translating by-reference
 * arguments.
 * 
 * @author Trevor Robinson
 */
final class VarInfo
{
    final JavaSchema schema;
    final VeraVariable veraVar;
    final JavaVariable targetVar;
    final boolean holderVar;

    public VarInfo(
        JavaSchema schema,
        VeraVariable veraVar,
        JavaVariable targetVar,
        boolean holderVar)
    {
        this.schema = schema;
        this.veraVar = veraVar;
        this.targetVar = targetVar;
        this.holderVar = holderVar;
    }

    public JavaVariable getTargetVar()
    {
        return targetVar;
    }

    public boolean isHolderVar()
    {
        return holderVar;
    }

    public JavaExpression getReference()
    {
        JavaVariableReference varRef = new JavaVariableReference(targetVar);
        if (holderVar)
        {
            JavaArrayAccess varAccess = new JavaArrayAccess(varRef);
            varAccess.addIndex(new JavaIntLiteral(schema, 0));
            return varAccess;
        }
        else
        {
            return varRef;
        }
    }

    public JavaExpression getHolderReference()
    {
        assert (holderVar);
        JavaVariableReference varRef = new JavaVariableReference(targetVar);
        return varRef;
    }
}
