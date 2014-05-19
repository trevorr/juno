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
 * Represents a Vera less-or-equal operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraLessOrEqual
    extends VeraComparisonOperation
{
    private static final long serialVersionUID = 3546084674452927024L;

    public VeraLessOrEqual(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
    }

    public Object evaluateConstant()
    {
        Bit result;
        VeraType type1 = op1.getResultType();
        VeraType type2 = op2.getResultType();
        VeraType commonType = getCommonIntegralType(type1, type2);
        Object o1 = op1.evaluateConstant();
        Object o2 = op2.evaluateConstant();
        Object io1 = toCommonIntegralType(o1, commonType);
        Object io2 = toCommonIntegralType(o2, commonType);
        if (commonType instanceof VeraIntegerType)
        {
            return IntegerOp.lessOrEqual((Integer) io1, (Integer) io2);
        }
        else if (commonType instanceof VeraBitVectorType)
        {
            BitVector bv1 = (BitVector) io1;
            BitVector bv2 = (BitVector) io2;
            if (bv1.containsXZ() || bv2.containsXZ())
            {
                result = Bit.X;
            }
            else
            {
                result = bv1.compareTo(bv2) <= 0 ? Bit.ONE : Bit.ZERO;
            }
        }
        else
        {
            assert (getResultType() instanceof VeraBitType);
            Bit b1 = (Bit) o1;
            Bit b2 = (Bit) o2;
            if (b1.isXZ() || b2.isXZ())
            {
                result = Bit.X;
            }
            else
            {
                result = b1.getID() <= b2.getID() ? Bit.ONE : Bit.ZERO;
            }
        }
        return result;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
