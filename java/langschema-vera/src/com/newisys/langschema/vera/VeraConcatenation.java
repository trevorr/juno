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

import java.util.Iterator;

import com.newisys.langschema.util.SemanticException;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Represents a Vera concatenation operation.
 * 
 * @author Trevor Robinson
 */
public final class VeraConcatenation
    extends VeraNaryOperation
{
    private static final long serialVersionUID = 3256722862113829425L;

    private boolean assignable;

    public VeraConcatenation(VeraSchema schema)
    {
        super(schema);
    }

    public void addOperand(VeraExpression expr)
    {
        setResultType(null);
        super.addOperand(expr);
    }

    public VeraType getResultType()
    {
        VeraType resultType = super.getResultType();
        if (resultType == null)
        {
            boolean first = true;
            int totalBitCount = 0;
            assignable = true;
            Iterator iter = operands.iterator();
            while (iter.hasNext())
            {
                VeraExpression expr = (VeraExpression) iter.next();
                assignable &= expr.isAssignable();
                VeraType type = expr.getResultType();
                if (first)
                {
                    if (type instanceof VeraStringType)
                    {
                        resultType = type;
                    }
                    else
                    {
                        totalBitCount = type.getBitCount();
                    }
                    first = false;
                }
                else
                {
                    if ((type instanceof VeraStringType) != (totalBitCount == 0))
                    {
                        throw new SemanticException(
                            "Concatenation of string and non-string");
                    }

                    if (totalBitCount > 0)
                    {
                        int curBitCount = type.getBitCount();
                        if (curBitCount > 0)
                        {
                            totalBitCount += curBitCount;
                            if (totalBitCount > VeraBitVectorType.MAX_SIZE)
                            {
                                totalBitCount = VeraBitVectorType.MAX_SIZE;
                            }
                        }
                        else
                        {
                            totalBitCount = -1;
                        }
                    }
                }
            }
            if (totalBitCount > 0)
            {
                resultType = new VeraFixedBitVectorType(schema, totalBitCount);
            }
            else if (totalBitCount < 0)
            {
                resultType = schema.bitVectorType;
            }
            setResultType(resultType);
        }
        return resultType;
    }

    public boolean isAssignable()
    {
        getResultType();
        return assignable;
    }

    public Object evaluateConstant()
    {
        VeraType resultType = getResultType();
        if (resultType instanceof VeraStringType)
        {
            StringBuffer buf = new StringBuffer();
            for (VeraExpression expr : operands)
            {
                Object o = expr.evaluateConstant();
                buf.append(o);
            }
            return buf.toString();
        }
        else
        {
            int size = ((VeraBitVectorType) resultType).getSize();
            int lowBit = size;
            BitVectorBuffer buf = new BitVectorBuffer(size);
            for (VeraExpression expr : operands)
            {
                BitVector bv = toBitVector(expr.evaluateConstant());
                int highBit = lowBit - 1;
                lowBit -= bv.length();
                assert (lowBit >= 0);
                buf.setBits(highBit, lowBit, bv);
            }
            return buf.toBitVector();
        }
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
