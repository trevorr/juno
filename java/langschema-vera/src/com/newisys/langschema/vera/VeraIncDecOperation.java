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

import com.newisys.langschema.util.SemanticException;

/**
 * Base class for Vera unary increment/decrement operations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraIncDecOperation
    extends VeraUnaryOperation
{
    public VeraIncDecOperation(VeraExpression op1)
    {
        super(op1);
        if (!op1.getResultType().isIntegralConvertible())
        {
            throw new SemanticException("Incorrect operand type");
        }
        setResultType(op1.getResultType());
    }

    public boolean isConstant()
    {
        return false;
    }
}
