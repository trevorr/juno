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
import com.newisys.verilog.util.BitVector;

/**
 * Base class for Vera comparison operations.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraComparisonOperation
    extends VeraBinaryOperation
{
    public VeraComparisonOperation(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
        if (!isValidTypes(op1.getResultType(), op2.getResultType()))
        {
            throw new SemanticException("Incorrect operand types");
        }
        setResultType(schema.bitType);
    }

    protected boolean isWildEqual()
    {
        boolean result;
        Object o1 = op1.evaluateConstant();
        Object o2 = op2.evaluateConstant();
        VeraType type1 = op1.getResultType();
        VeraType type2 = op2.getResultType();
        if (type1.isStrictIntegral() || type2.isStrictIntegral())
        {
            VeraType commonType = getCommonIntegralType(type1, type2);
            Object io1 = toCommonIntegralType(o1, commonType);
            Object io2 = toCommonIntegralType(o2, commonType);
            if (commonType instanceof VeraBitVectorType)
            {
                result = ((BitVector) io1).equalsWild((BitVector) io2);
            }
            else if (io1 == null || io2 == null)
            {
                result = true;
            }
            else
            {
                result = io1.equals(io2);
            }
        }
        else
        {
            result = nullEquals(o1, o2);
        }
        return result;
    }
}
