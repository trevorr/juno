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

import com.newisys.langschema.Expression;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;

/**
 * Base class for Vera expressions.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraExpression
    extends VeraSchemaObjectImpl
    implements Expression, VeraDefineReferrer<VeraExpressionDefine>
{
    final static long serialVersionUID = -1004295860595142204L;

    protected final static Integer INTEGER_ZERO = new Integer(0);
    protected final static Integer INTEGER_ONE = new Integer(1);

    private VeraType resultType;
    private VeraDefineReference<VeraExpressionDefine> defineRef;

    public VeraExpression(VeraSchema schema)
    {
        super(schema);
    }

    public VeraType getResultType()
    {
        return resultType;
    }

    protected final void setResultType(VeraType resultType)
    {
        this.resultType = resultType;
    }

    public VeraDefineReference<VeraExpressionDefine> getDefineRef()
    {
        return defineRef;
    }

    public void setDefineRef(VeraDefineReference<VeraExpressionDefine> defineRef)
    {
        this.defineRef = defineRef;
    }

    public boolean isAssignable()
    {
        return false;
    }

    public Object evaluateConstant()
    {
        throw new RuntimeException("Cannot evaluate non-constant expression");
    }

    protected static boolean isRefType(VeraType type)
    {
        return type instanceof VeraClass || type instanceof VeraPortType
            || type instanceof VeraEventType || type instanceof VeraNullType;
    }

    protected static boolean isRefOrEnumType(VeraType type)
    {
        return isRefType(type) || type instanceof VeraEnumeration;
    }

    protected static VeraType getIntegralType(VeraType type)
    {
        if (type instanceof VeraEnumeration)
        {
            return type.schema.integerType;
        }
        else if (type instanceof VeraMagicType
            || type instanceof VeraIntegerType
            || type instanceof VeraBitVectorType || type instanceof VeraBitType)
        {
            return type;
        }
        else
        {
            throw new RuntimeException("Integral type expected");
        }
    }

    protected static Object toIntegral(Object o)
    {
        if (o instanceof Integer || o instanceof BitVector || o instanceof Bit
            || o == null)
        {
            return o;
        }
        else if (o instanceof VeraEnumerationElement)
        {
            return new Integer(((VeraEnumerationElement) o).getValue());
        }
        else
        {
            throw new RuntimeException("Cannot convert "
                + o.getClass().getName() + " to integral type");
        }
    }

    public static Integer toInteger(Object o)
    {
        Object io = toIntegral(o);
        if (io instanceof Integer)
        {
            return (Integer) io;
        }
        else if (io instanceof BitVector)
        {
            BitVector bv = (BitVector) io;
            if (!bv.containsXZ())
            {
                return new Integer(bv.intValue());
            }
        }
        else if (io instanceof Bit)
        {
            if (io == Bit.ZERO)
            {
                return INTEGER_ZERO;
            }
            if (io == Bit.ONE)
            {
                return INTEGER_ONE;
            }
        }
        return null;
    }

    public static boolean toBoolean(Object o)
    {
        if (o instanceof Integer)
        {
            return ((Integer) o).intValue() != 0;
        }
        else if (o instanceof BitVector)
        {
            BitVector bv = ((BitVector) o);
            return !bv.containsXZ() && bv.isNotZero();
        }
        else if (o instanceof Bit)
        {
            return o == Bit.ONE;
        }
        else if (o == null)
        {
            return false;
        }
        else if (o instanceof VeraEnumerationElement)
        {
            return ((VeraEnumerationElement) o).getValue() != 0;
        }
        else
        {
            throw new RuntimeException("Cannot convert "
                + o.getClass().getName() + " to boolean type");
        }
    }

    protected static VeraType getCommonIntegralType(
        VeraExpression op1,
        VeraExpression op2)
    {
        VeraType type1 = op1.resultType;
        VeraType type2 = op2.resultType;
        return getCommonIntegralType(type1, type2);
    }

    protected static VeraType getCommonIntegralType(
        VeraType type1,
        VeraType type2)
    {
        if (type1 instanceof VeraMagicType || type2 instanceof VeraMagicType)
        {
            return type1;
        }
        else if ((type1 instanceof VeraIntegerType || type1 instanceof VeraEnumeration)
            && (type2 instanceof VeraIntegerType || type2 instanceof VeraEnumeration))
        {
            return type1.schema.integerType;
        }
        else if (type1 instanceof VeraBitType && type2 instanceof VeraBitType)
        {
            return type1.schema.bitType;
        }
        else
        {
            int typeBits = Math.max(type1.getBitCount(), type2.getBitCount());
            return new VeraFixedBitVectorType(type1.schema, typeBits);
        }
    }

    protected static Object toCommonIntegralType(Object o, VeraType type)
    {
        if (type instanceof VeraIntegerType)
        {
            if (o instanceof Integer || o == null)
            {
                return o;
            }
            else if (o instanceof VeraEnumerationElement)
            {
                VeraEnumerationElement ee = (VeraEnumerationElement) o;
                return new Integer(ee.getValue());
            }
        }
        else if (type instanceof VeraBitVectorType)
        {
            int size = ((VeraBitVectorType) type).getSize();
            if (o instanceof Integer)
            {
                return new BitVector(size, ((Integer) o).intValue());
            }
            else if (o instanceof BitVector)
            {
                BitVector bv = (BitVector) o;
                return bv.length() == size ? bv : bv.setLength(size, Bit.ZERO);
            }
            else if (o instanceof Bit)
            {
                return new BitVector(size, (Bit) o);
            }
            else if (o instanceof VeraEnumerationElement)
            {
                VeraEnumerationElement ee = (VeraEnumerationElement) o;
                return new BitVector(size, ee.getValue());
            }
            else if (o == null)
            {
                return new BitVector(size, Bit.X);
            }
        }
        else if (type instanceof VeraBitType)
        {
            assert (o instanceof Bit);
            return o;
        }
        throw new RuntimeException("Cannot convert "
            + (o != null ? o.getClass().getName() : "null") + " to " + type);
    }

    public static BitVector toBitVector(Object o)
    {
        if (o instanceof BitVector)
        {
            return (BitVector) o;
        }
        else if (o instanceof Integer)
        {
            return new BitVector(32, ((Integer) o).intValue());
        }
        else if (o instanceof Bit)
        {
            return new BitVector(1, (Bit) o);
        }
        else if (o instanceof VeraEnumerationElement)
        {
            VeraEnumerationElement ee = (VeraEnumerationElement) o;
            return new BitVector(32, ee.getValue());
        }
        else if (o == null)
        {
            return new BitVector(32, Bit.X);
        }
        throw new RuntimeException("Cannot convert " + o.getClass().getName()
            + " to BitVector");
    }

    protected static boolean nullEquals(Object o1, Object o2)
    {
        return o1 != null ? o1.equals(o2) : o2 == null;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        accept((VeraExpressionVisitor) visitor);
    }

    public abstract void accept(VeraExpressionVisitor visitor);

    public String toDebugString()
    {
        // for expressions, default to always using the source string
        return toSourceString();
    }
}
