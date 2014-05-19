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

import com.newisys.langschema.Literal;
import com.newisys.verilog.util.BitVector;

/**
 * Represents a Vera bit vector literal expression.
 * 
 * @author Trevor Robinson
 */
public final class VeraBitVectorLiteral
    extends VeraExpression
    implements Literal
{
    private static final long serialVersionUID = 3618138961298731319L;

    private final BitVector value;
    private int radix;

    public VeraBitVectorLiteral(VeraSchema schema, BitVector value)
    {
        super(schema);
        setResultType(new VeraFixedBitVectorType(schema, value.length()));
        this.value = value;
    }

    public BitVector getValue()
    {
        return value;
    }

    public int getRadix()
    {
        return radix;
    }

    public void setRadix(int radix)
    {
        this.radix = radix;
    }

    public boolean isConstant()
    {
        return true;
    }

    public Object evaluateConstant()
    {
        return value;
    }

    public void accept(VeraExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
