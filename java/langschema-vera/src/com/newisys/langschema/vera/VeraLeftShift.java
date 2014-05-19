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

import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;

/**
 * Represents a Vera left shift operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraLeftShift
    extends VeraShiftOperation
{
    private static final long serialVersionUID = 3256722875032351027L;

    public VeraLeftShift(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
    }

    public Object evaluateConstant()
    {
        Object o1 = toIntegral(op1.evaluateConstant());
        int count = getShiftCount(op2.evaluateConstant());
        if (o1 instanceof Integer)
        {
            int i1 = ((Integer) o1).intValue();
            return new Integer(i1 << count);
        }
        else if (o1 instanceof BitVector)
        {
            BitVector bv1 = (BitVector) o1;
            return bv1.shiftLeft(count);
        }
        else if (o1 instanceof Bit)
        {
            return count == 0 ? o1 : Bit.ZERO;
        }
        else
        {
            assert (o1 == null);
            return null;
        }
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
