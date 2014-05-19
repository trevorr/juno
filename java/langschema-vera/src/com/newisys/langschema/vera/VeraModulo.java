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

import com.newisys.juno.runtime.IntegerOp;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;

/**
 * Represents a Vera modulo operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraModulo
    extends VeraBinaryArithmeticOperation
{
    private static final long serialVersionUID = 3258134652274424119L;

    public VeraModulo(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
    }

    public Object evaluateConstant()
    {
        VeraType resultType = getResultType();
        Object o1 = toCommonIntegralType(op1.evaluateConstant(), resultType);
        Object o2 = toCommonIntegralType(op2.evaluateConstant(), resultType);
        if (resultType instanceof VeraIntegerType)
        {
            return IntegerOp.mod((Integer) o1, (Integer) o2);
        }
        else if (resultType instanceof VeraBitVectorType)
        {
            BitVector bv1 = (BitVector) o1;
            BitVector bv2 = (BitVector) o2;
            return bv1.mod(bv2);
        }
        else
        {
            assert (resultType instanceof VeraBitType);
            Bit b1 = (Bit) o1;
            Bit b2 = (Bit) o2;
            if (b2 == Bit.ZERO)
            {
                throw new ArithmeticException("Bit divide by zero");
            }
            return b1.isXZ() || b2.isXZ() ? Bit.X : Bit.ZERO;
        }
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
