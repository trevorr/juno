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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Operation;
import com.newisys.langschema.OperationModifier;

/**
 * Base class for Vera operation expressions.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraOperation
    extends VeraExpression
    implements Operation
{
    private static final long serialVersionUID = 3879069680342341263L;

    protected final List<VeraExpression> operands = new LinkedList<VeraExpression>();

    public VeraOperation(VeraSchema schema)
    {
        super(schema);
    }

    public List<VeraExpression> getOperands()
    {
        return operands;
    }

    public VeraExpression getOperand(int index)
    {
        return operands.get(index);
    }

    public Set< ? extends OperationModifier> getModifiers()
    {
        return Collections.emptySet();
    }
}
