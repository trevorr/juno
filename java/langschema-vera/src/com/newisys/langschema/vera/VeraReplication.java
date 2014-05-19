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
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Represents a Vera bit vector or string replication operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraReplication
    extends VeraBinaryOperation
{
    private static final long serialVersionUID = 3689351022455631927L;

    public VeraReplication(VeraExpression op1, VeraExpression op2)
    {
        super(op1, op2);
        if (!isValidTypes(op1.getResultType(), op2.getResultType()))
        {
            throw new SemanticException("Incorrect operand types");
        }
        if (op2.getResultType() instanceof VeraStringType)
        {
            setResultType(schema.stringType);
        }
        else
        {
            setResultType(schema.bitVectorType);
        }
    }

    protected static boolean isValidTypes(VeraType type1, VeraType type2)
    {
        return type1.isIntegralConvertible()
            && (type2.isIntegralConvertible() || type2 instanceof VeraStringType);
    }

    public Object evaluateConstant()
    {
        Integer i1 = toInteger(op1.evaluateConstant());
        if (i1 == null)
        {
            return null;
        }
        int count = i1.intValue();
        if (count <= 0)
        {
            return null;
        }
        Object o2 = op2.evaluateConstant();
        if (getResultType() instanceof VeraStringType)
        {
            String str = o2.toString();
            StringBuffer buf = new StringBuffer(str.length() * count);
            for (int i = 0; i < count; ++i)
            {
                buf.append(str);
            }
            return buf.toString();
        }
        else
        {
            BitVector bv = toBitVector(o2);
            int len = bv.length();
            int size = len * count;
            BitVectorBuffer buf = new BitVectorBuffer(size);
            int lowBit = 0;
            while (lowBit < size)
            {
                int highBit = lowBit + len - 1;
                buf.setBits(highBit, lowBit, bv);
                lowBit += len;
            }
            return buf.toBitVector();
        }
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
