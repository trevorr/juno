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
 * Represents a Vera reductive-or-not operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraNotOrReduction
    extends VeraReduction
{
    private static final long serialVersionUID = 3690480229370245431L;

    public VeraNotOrReduction(VeraExpression op1)
    {
        super(op1);
    }

    public Object evaluateConstant()
    {
        Object o = toIntegral(op1.evaluateConstant());
        if (o == null || o instanceof Integer)
        {
            return IntegerOp.reductiveOrNot((Integer) o);
        }
        else if (o instanceof BitVector)
        {
            return ((BitVector) o).reductiveOr().not();
        }
        else
        {
            assert (o instanceof Bit);
            return ((Bit) o).not();
        }
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
