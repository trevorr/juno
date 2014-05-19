/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.schemabuilder.juno;

import com.newisys.langschema.Literal;
import com.newisys.langschema.Type;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.verilog.util.Bit;

/**
 * Provides methods for performing type tests and expression type conversion
 * for the built-in types.
 * 
 * @author Trevor Robinson
 */
public final class ExpressionConverter
{
    final JoveSchema schema;
    final SchemaTypes types;

    public ExpressionConverter(JoveSchema schema, SchemaTypes types)
    {
        this.schema = schema;
        this.types = types;
    }

    ////////////////////////////////////////////////////////////
    // Type tests
    ////////////////////////////////////////////////////////////

    public boolean isAssocArray(Type type)
    {
        return type instanceof JavaAbstractClass
            && types.assocArrayType.isSuperclassOf((JavaAbstractClass) type);
    }

    public boolean isEnum(Type type)
    {
        return type instanceof JavaStructuredType
            && ((JavaStructuredType) type)
                .implementsInterface(types.junoEnumType);
    }

    public JavaExpression getEnumUndefined(JavaEnum enumCls)
    {
        return new JavaVariableReference(enumCls.getField("UNDEFINED"));
    }

    public boolean isSignal(Type type)
    {
        return type instanceof JavaStructuredType
            && ((JavaStructuredType) type)
                .implementsInterface(types.signalType);
    }

    public boolean isInputSignal(Type type)
    {
        return type instanceof JavaStructuredType
            && ((JavaStructuredType) type)
                .implementsInterface(types.inputSignalType);
    }

    public boolean isOutputSignal(Type type)
    {
        return type instanceof JavaStructuredType
            && ((JavaStructuredType) type)
                .implementsInterface(types.outputSignalType);
    }

    public boolean isString(Type type)
    {
        return type == types.stringType || type == types.junoStringType;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to Bit
    ////////////////////////////////////////////////////////////

    public JavaExpression getBitExpr(Bit b)
    {
        final String[] names = { "ZERO", "ONE", "Z", "X" };
        final String name = names[b.getID()];
        return new JavaVariableReference(schema.bitType.getField(name));
    }

    public JavaExpression commonToBit(JavaExpression expr)
    {
        // narrowing conversion: BitOp.toBit((int) expr)
        // narrowing conversion: BitOp.toBit((long) expr)
        // narrowing conversion: BitOp.toBit((Integer) expr)
        // narrowing conversion: BitOp.toBit((Long) expr)
        // narrowing conversion: BitOp.toBit((String) expr)
        // narrowing conversion: BitOp.toBit((Object) expr)
        return ExpressionBuilder.staticCall(types.bitOpType, "toBit", expr);
    }

    public JavaExpression bitVectorToBit(JavaExpression expr)
    {
        // narrowing conversion
        // expr.getBit(0)
        return ExpressionBuilder.memberCall(expr, "getBit", new JavaIntLiteral(
            schema, 0));
    }

    public JavaExpression booleanToBit(JavaExpression expr)
    {
        // widening conversion
        // expr ? Bit.ONE : Bit.ZERO
        return new JavaConditional(expr, getBitExpr(Bit.ONE),
            getBitExpr(Bit.ZERO));
    }

    public boolean hasBitConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type) || isString(type)
            || type == schema.getObjectType();
    }

    public JavaExpression toBit(JavaExpression expr)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isBit(type))
        {
            // do nothing
        }
        else if (schema.isBitVector(type))
        {
            expr = bitVectorToBit(expr);
        }
        else if (schema.isInt(type) || schema.isLong(type))
        {
            expr = commonToBit(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = booleanToBit(expr);
        }
        else if (isEnum(type))
        {
            expr = commonToBit(enumToIntWrapper(expr));
        }
        else if (isString(type))
        {
            expr = commonToBit(toJavaString(expr, false));
        }
        else if (type == schema.getObjectType())
        {
            expr = commonToBit(expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to Bit");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to logical Bit
    ////////////////////////////////////////////////////////////

    public JavaExpression commonToLogicalBit(JavaExpression expr)
    {
        // narrowing conversion: BitOp.toLogicalBit((int) expr)
        // narrowing conversion: BitOp.toLogicalBit((long) expr)
        // narrowing conversion: BitOp.toLogicalBit((Integer) expr)
        // narrowing conversion: BitOp.toLogicalBit((Long) expr)
        // narrowing conversion: BitOp.toLogicalBit((BitVector) expr)
        return ExpressionBuilder.staticCall(types.bitOpType, "toLogicalBit",
            expr);
    }

    public JavaExpression toLogicalBit(JavaExpression expr)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isBit(type))
        {
            // do nothing
        }
        else if (schema.isInt(type) || schema.isLong(type)
            || schema.isBitVector(type))
        {
            expr = commonToLogicalBit(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = booleanToBit(expr);
        }
        else if (isEnum(type))
        {
            expr = commonToLogicalBit(enumToIntWrapper(expr));
        }
        else if (isString(type))
        {
            expr = commonToBit(toJavaString(expr, false));
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to Bit");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to BitVector
    ////////////////////////////////////////////////////////////

    public JavaExpression getBitVectorExpr(Bit b)
    {
        final String[] names = { "BIT_0", "BIT_1", "BIT_Z", "BIT_X" };
        final String name = names[b.getID()];
        JavaMemberVariable field = types.bitVectorOpType.getField(name);
        field.setType(schema.getBitVectorType(1));
        return new JavaVariableReference(field);
    }

    public JavaExpression getBitVectorExpr(int len)
    {
        // new BitVector(len)
        return ExpressionBuilder.newInstance(schema.getBitVectorType(len),
            new JavaIntLiteral(schema, len));
    }

    public JavaExpression setBitVectorLength(JavaExpression expr, int len)
    {
        // truncate or zero-extend
        // expr.setLength(len, Bit.ZERO)
        JavaFunctionInvocation callExpr = ExpressionBuilder.memberCall(expr,
            "setLength", new JavaIntLiteral(schema, len), getBitExpr(Bit.ZERO));
        callExpr.setResultType(schema.getBitVectorType(len));
        return callExpr;
    }

    public JavaExpression bitToBitVector(JavaExpression expr, int len)
    {
        // widening conversion
        // BitOp.toBitVector(len, (Bit) expr)
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.bitOpType, "toBitVector", new JavaExpression[] {
                new JavaIntLiteral(schema, len), expr }, null);
        callExpr.setResultType(schema.getBitVectorType(len));
        return callExpr;
    }

    public JavaExpression intToBitVector(
        JavaExpression expr,
        int len,
        boolean signExtend)
    {
        // widening conversion
        // new BitVector(len, (int) expr, signExtend)
        return ExpressionBuilder.newInstance(schema.getBitVectorType(len),
            new JavaExpression[] { new JavaIntLiteral(schema, len), expr,
                new JavaBooleanLiteral(schema, signExtend) }, null);
    }

    public JavaExpression longToBitVector(
        JavaExpression expr,
        int len,
        boolean signExtend)
    {
        // widening conversion
        // new BitVector(len, (long) expr, signExtend)
        return ExpressionBuilder.newInstance(schema.getBitVectorType(len),
            new JavaExpression[] { new JavaIntLiteral(schema, len), expr,
                new JavaBooleanLiteral(schema, signExtend) }, null);
    }

    public JavaExpression intWrapperToBitVector(
        JavaExpression expr,
        int len,
        boolean signExtend)
    {
        // widening conversion
        // IntegerOp.toBitVector((Integer) expr, len, signExtend)
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.integerOpType, "toBitVector", new JavaExpression[] { expr,
                new JavaIntLiteral(schema, len),
                new JavaBooleanLiteral(schema, signExtend) }, null);
        callExpr.setResultType(schema.getBitVectorType(len));
        return callExpr;
    }

    public JavaExpression longWrapperToBitVector(
        JavaExpression expr,
        int len,
        boolean signExtend)
    {
        // widening conversion
        // LongOp.toBitVector((Long) expr, len, signExtend)
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.longWrapperOpType, "toBitVector", new JavaExpression[] {
                expr, new JavaIntLiteral(schema, len),
                new JavaBooleanLiteral(schema, signExtend) }, null);
        callExpr.setResultType(schema.getBitVectorType(len));
        return callExpr;
    }

    public JavaExpression intWrapperToUnsizedBitVector(JavaExpression expr)
    {
        // widening conversion
        // IntegerOp.toBitVector((Integer) expr)
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.integerOpType, "toBitVector", expr);
        callExpr.setResultType(schema.getBitVectorType(32));
        return callExpr;
    }

    public JavaExpression longWrapperToUnsizedBitVector(JavaExpression expr)
    {
        // widening conversion
        // LongOp.toBitVector((Long) expr)
        JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
            types.longWrapperOpType, "toBitVector", expr);
        callExpr.setResultType(schema.getBitVectorType(32));
        return callExpr;
    }

    public JavaExpression stringToBitVector(JavaExpression expr)
    {
        // new BitVector(expr.getBytes())
        return ExpressionBuilder.newInstance(schema.bitVectorType,
            ExpressionBuilder.memberCall(expr, "getBytes"));
    }

    public JavaExpression objectToBitVector(JavaExpression expr)
    {
        // BitVectorOp.toBitVector((Object) expr)
        return ExpressionBuilder.staticCall(types.bitVectorOpType,
            "toBitVector", expr);
    }

    public boolean hasBitVectorConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type) || isString(type)
            || type == schema.getObjectType();
    }

    public JavaExpression toBitVector(
        JavaExpression expr,
        int len,
        boolean signExtend)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isBitVector(type))
        {
            if (len > 0 && schema.getBitVectorSize(type) != len)
            {
                expr = setBitVectorLength(expr, len);
            }
        }
        else if (type == schema.integerWrapperType)
        {
            if (len > 0)
            {
                expr = intWrapperToBitVector(expr, len, signExtend);
            }
            else
            {
                expr = intWrapperToUnsizedBitVector(expr);
            }
        }
        else if (schema.isInt(type))
        {
            if (len <= 0) len = 32;
            expr = intToBitVector(expr, len, signExtend);
        }
        else if (type == schema.longWrapperType)
        {
            if (len > 0)
            {
                expr = longWrapperToBitVector(expr, len, signExtend);
            }
            else
            {
                expr = longWrapperToUnsizedBitVector(expr);
            }
        }
        else if (schema.isLong(type))
        {
            if (len <= 0) len = 64;
            expr = longToBitVector(expr, len, signExtend);
        }
        else if (schema.isBoolean(type))
        {
            if (len <= 0) len = 1;
            expr = bitToBitVector(booleanToBit(expr), len);
        }
        else if (type == schema.bitType)
        {
            if (len <= 0) len = 1;
            expr = bitToBitVector(expr, len);
        }
        else if (isEnum(type))
        {
            expr = enumToIntWrapper(expr);
            if (len > 0)
            {
                expr = intWrapperToBitVector(expr, len, signExtend);
            }
            else
            {
                expr = intWrapperToUnsizedBitVector(expr);
            }
        }
        else if (isString(type))
        {
            expr = stringToBitVector(toJavaString(expr, true));
            if (len > 0)
            {
                expr = setBitVectorLength(expr, len);
            }
        }
        else if (type == schema.getObjectType())
        {
            expr = objectToBitVector(expr);
            if (len > 0)
            {
                expr = setBitVectorLength(expr, len);
            }
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to BitVector");
        }
        return expr;
    }

    public JavaExpression toUnsizedBitVector(JavaExpression expr)
        throws TypeConversionException
    {
        return toBitVector(expr, -1, false);
    }

    ////////////////////////////////////////////////////////////
    // Conversions to Boolean
    ////////////////////////////////////////////////////////////

    public JavaExpression bitToBoolean(JavaExpression expr, boolean xzTrue)
    {
        // narrowing conversion: loses X/Z
        if (!xzTrue)
        {
            // expr == Bit.ONE
            return new JavaEqual(schema, expr, new JavaVariableReference(
                schema.bitType.getField("ONE")));
        }
        else
        {
            // expr != Bit.ZERO
            return new JavaNotEqual(schema, expr, new JavaVariableReference(
                schema.bitType.getField("ZERO")));
        }
    }

    public JavaExpression bitVectorToBoolean(
        JavaExpression expr,
        boolean lowBitOnly,
        boolean xzTrue)
    {
        // narrowing conversion
        if (lowBitOnly)
        {
            // !xzTrue: expr.getBit(0) == Bit.ONE
            // xzTrue: expr.getBit(0) != Bit.ZERO
            return bitToBoolean(ExpressionBuilder.memberCall(expr, "getBit",
                new JavaIntLiteral(schema, 0)), xzTrue);
        }
        else
        {
            // BooleanOp.toBoolean((BitVector) expr)
            // BooleanOp.toBooleanXZTrue((BitVector) expr)
            return ExpressionBuilder.staticCall(types.booleanOpType, xzTrue
                ? "toBooleanXZTrue" : "toBoolean", expr);
        }
    }

    public JavaExpression intToBoolean(JavaExpression expr, boolean lowBitOnly)
    {
        // narrowing conversion
        // !lowBitOnly: expr != 0
        // lowBitOnly: (expr & 1) != 0
        if (expr instanceof JavaIntLiteral)
        {
            int value = ((JavaIntLiteral) expr).getValue();
            return new JavaBooleanLiteral(schema, !lowBitOnly ? value != 0
                : (value & 1) != 0);
        }
        if (lowBitOnly)
        {
            expr = new JavaAnd(schema, expr, new JavaIntLiteral(schema, 1));
        }
        return new JavaNotEqual(schema, expr, new JavaIntLiteral(schema, 0));
    }

    public JavaExpression intWrapperToBoolean(
        JavaExpression expr,
        boolean lowBitOnly,
        boolean xzTrue)
    {
        // narrowing conversion
        // IntegerOp.toBoolean((Integer) expr)
        // IntegerOp.lowBitToBoolean((Integer) expr)
        // IntegerOp.toBooleanXZTrue((Integer) expr)
        // IntegerOp.lowBitToBooleanXZTrue((Integer) expr)
        return ExpressionBuilder.staticCall(types.integerOpType, lowBitOnly
            ? (xzTrue ? "lowBitToBooleanXZTrue" : "lowBitToBoolean") : (xzTrue
                ? "toBooleanXZTrue" : "toBoolean"), expr);
    }

    public JavaExpression longWrapperToBoolean(
        JavaExpression expr,
        boolean lowBitOnly,
        boolean xzTrue)
    {
        // narrowing conversion
        // LongWrapperOp.toBoolean((Long) expr)
        // LongWrapperOp.lowBitToBoolean((Integer) expr)
        // LongWrapperOp.toBooleanXZTrue((Long) expr)
        // LongWrapperOp.lowBitToBooleanXZTrue((Integer) expr)
        return ExpressionBuilder.staticCall(types.longWrapperOpType, lowBitOnly
            ? (xzTrue ? "lowBitToBooleanXZTrue" : "lowBitToBoolean") : (xzTrue
                ? "toBooleanXZTrue" : "toBoolean"), expr);
    }

    public JavaExpression objectToBoolean(JavaExpression expr)
    {
        // narrowing conversion
        // BooleanOp.toBoolean((Object) expr)
        // BooleanOp.toBoolean((String) expr)
        return ExpressionBuilder.staticCall(types.booleanOpType, "toBoolean",
            expr);
    }

    public boolean hasBooleanConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type) || isString(type)
            || type == schema.getObjectType();
    }

    public JavaExpression toBoolean(
        JavaExpression expr,
        boolean lowBitOnly,
        boolean xzTrue)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isBoolean(type))
        {
            // do nothing
        }
        else if (type == schema.integerWrapperType)
        {
            expr = intWrapperToBoolean(expr, lowBitOnly, xzTrue);
        }
        else if (type == schema.longWrapperType)
        {
            expr = longWrapperToBoolean(expr, lowBitOnly, xzTrue);
        }
        else if (schema.isInt(type) || schema.isLong(type))
        {
            expr = intToBoolean(expr, lowBitOnly);
        }
        else if (schema.isBitVector(type))
        {
            expr = bitVectorToBoolean(expr, lowBitOnly, xzTrue);
        }
        else if (type == schema.bitType)
        {
            expr = bitToBoolean(expr, xzTrue);
        }
        else if (isEnum(type))
        {
            expr = intWrapperToBoolean(enumToIntWrapper(expr), lowBitOnly,
                xzTrue);
        }
        else if (isString(type))
        {
            expr = objectToBoolean(toJavaString(expr, false));
        }
        else if (type == schema.getObjectType())
        {
            expr = objectToBoolean(expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to boolean");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to int
    ////////////////////////////////////////////////////////////

    public JavaExpression bitToInt(JavaExpression expr)
    {
        // checked conversion: throws IllegalArgumentException if X/Z
        // BitOp.toInt((Bit) expr)
        return ExpressionBuilder.staticCall(types.bitOpType, "toInt", expr);
    }

    public JavaExpression bitVectorToInt(JavaExpression expr)
    {
        // checked conversion: throws IllegalArgumentException if X/Z
        // BitVectorOp.toInt((BitVector) expr)
        return ExpressionBuilder.staticCall(types.bitVectorOpType, "toInt",
            expr);
    }

    public JavaExpression booleanToInt(JavaExpression expr)
    {
        // widening conversion
        // expr ? 1 : 0
        return new JavaConditional(expr, new JavaIntLiteral(schema, 1),
            new JavaIntLiteral(schema, 0));
    }

    public JavaExpression enumToInt(JavaExpression expr)
    {
        // widening conversion
        // expr.toInt()
        return ExpressionBuilder.memberCall(expr, "toInt");
    }

    public JavaExpression longToInt(JavaExpression expr)
    {
        // narrowing conversion
        // (int) expr
        return new JavaCastExpression(schema.intType, expr);
    }

    public JavaExpression objectToInt(JavaExpression expr)
    {
        // narrowing conversion
        // IntOp.toInt((Object) expr)
        // IntOp.toInt((String) expr)
        return ExpressionBuilder.staticCall(types.intOpType, "toInt", expr);
    }

    public boolean hasIntConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type) || isString(type)
            || type == schema.getObjectType();
    }

    public JavaExpression toInt(JavaExpression expr)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isInt(type))
        {
            // do nothing
        }
        else if (schema.isLong(type))
        {
            expr = longToInt(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = booleanToInt(expr);
        }
        else if (schema.isBitVector(type))
        {
            expr = bitVectorToInt(expr);
        }
        else if (type == schema.bitType)
        {
            expr = bitToInt(expr);
        }
        else if (isEnum(type))
        {
            expr = enumToInt(expr);
        }
        else if (isString(type))
        {
            expr = objectToInt(toJavaString(expr, true));
        }
        else if (type == schema.getObjectType())
        {
            expr = objectToInt(expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to int");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to Integer
    ////////////////////////////////////////////////////////////

    public JavaExpression commonToIntWrapper(JavaExpression expr)
    {
        // narrowing conversion: loses Z: IntegerOp.toInteger((Bit) expr)
        // narrowing conversion: loses positional X/Z, truncates to 32 bits:
        //   IntegerOp.toInteger((BitVector) expr)
        // narrowing conversion: IntegerOp.toInteger((String) expr)
        // narrowing conversion: IntegerOp.toInteger((Object) expr)
        return ExpressionBuilder.staticCall(types.integerOpType, "toInteger",
            expr);
    }

    public JavaExpression enumToIntWrapper(JavaExpression expr)
    {
        // widening conversion
        // expr.toInteger()
        return ExpressionBuilder.memberCall(expr, "toInteger");
    }

    public JavaExpression longToIntWrapper(JavaExpression expr)
    {
        // narrowing conversion
        // new Integer((int) expr)
        return ExpressionBuilder.newInstance(schema.integerWrapperType,
            new JavaCastExpression(schema.intType, expr));
    }

    public JavaExpression longWrapperToIntWrapper(JavaExpression expr)
    {
        // narrowing conversion
        // IntegerOp.toInteger((Long) expr)
        return ExpressionBuilder.staticCall(types.integerOpType, "toInteger",
            expr);
    }

    public JavaExpression toInteger(JavaExpression expr)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isInt(type))
        {
            // do nothing
        }
        else if (type == schema.longWrapperType)
        {
            expr = longWrapperToIntWrapper(expr);
        }
        else if (schema.isLong(type))
        {
            expr = longToIntWrapper(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = booleanToInt(expr);
        }
        else if (schema.isBitVector(type) || type == schema.bitType)
        {
            expr = commonToIntWrapper(expr);
        }
        else if (isEnum(type))
        {
            expr = enumToIntWrapper(expr);
        }
        else if (isString(type))
        {
            expr = commonToIntWrapper(toJavaString(expr, true));
        }
        else if (type == schema.getObjectType())
        {
            expr = commonToIntWrapper(expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to Integer");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to Long
    ////////////////////////////////////////////////////////////

    public JavaExpression commonToLongWrapper(JavaExpression expr)
    {
        // narrowing conversion: loses Z: LongWrapperOp.toLongWrapper((Bit) expr)
        // narrowing conversion: loses positional X/Z, truncates to 32 bits:
        //   LongWrapperOp.toLongWrapper((BitVector) expr)
        // narrowing conversion: LongWrapperOp.toLongWrapper((String) expr)
        // narrowing conversion: LongWrapperOp.toLongWrapper((Object) expr)
        return ExpressionBuilder.staticCall(types.longWrapperOpType,
            "toLongWrapper", expr);
    }

    public JavaExpression enumToLongWrapper(JavaExpression expr)
    {
        // widening conversion
        // LongWrapperOp.toLongWrapper(expr.toInteger())
        return intWrapperToLongWrapper(enumToIntWrapper(expr));
    }

    public JavaExpression intToLongWrapper(JavaExpression expr)
    {
        // widening conversion
        // new Long((int) expr)
        return ExpressionBuilder.newInstance(schema.longWrapperType, expr);
    }

    public JavaExpression intWrapperToLongWrapper(JavaExpression expr)
    {
        // narrowing conversion
        // LongWrapperOp.toLongWrapper((Integer) expr)
        return ExpressionBuilder.staticCall(types.longWrapperOpType,
            "toLongWrapper", expr);
    }

    public boolean hasLongWrapperConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type) || isString(type)
            || type == schema.getObjectType();
    }

    public JavaExpression toLongWrapper(JavaExpression expr)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isLong(type))
        {
            // do nothing
        }
        else if (type == schema.integerWrapperType)
        {
            expr = intWrapperToLongWrapper(expr);
        }
        else if (schema.isInt(type))
        {
            expr = intToLongWrapper(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = intToLongWrapper(booleanToInt(expr));
        }
        else if (schema.isBitVector(type) || type == schema.bitType)
        {
            expr = commonToLongWrapper(expr);
        }
        else if (isEnum(type))
        {
            expr = enumToLongWrapper(expr);
        }
        else if (isString(type))
        {
            expr = commonToLongWrapper(toJavaString(expr, true));
        }
        else if (type == schema.getObjectType())
        {
            expr = commonToLongWrapper(expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to Long");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to Object
    ////////////////////////////////////////////////////////////

    public boolean hasObjectConversion(Type type)
    {
        return type instanceof JavaPrimitiveType
            || type instanceof JavaReferenceType;
    }

    public JavaExpression toObject(JavaExpression expr)
    {
        Type type = expr.getResultType();
        assert (type instanceof JavaPrimitiveType || type instanceof JavaReferenceType);
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to shift count (int)
    ////////////////////////////////////////////////////////////

    public JavaExpression bitToShiftCount(JavaExpression expr)
    {
        // checked conversion: throws IllegalArgumentException if X/Z
        // BitOp.toShiftCount((Bit) expr)
        return ExpressionBuilder.staticCall(types.bitOpType, "toShiftCount",
            expr);
    }

    public JavaExpression bitVectorToShiftCount(
        JavaExpression expr,
        boolean limit)
    {
        // checked conversion: throws IllegalArgumentException if X/Z
        // BitVectorOp.toShiftCount((BitVector) expr [, 31])
        if (limit)
        {
            return ExpressionBuilder.staticCall(types.bitVectorOpType,
                "toShiftCount", expr, new JavaIntLiteral(schema, 31));
        }
        return ExpressionBuilder.staticCall(types.bitVectorOpType,
            "toShiftCount", expr);
    }

    public JavaExpression intToShiftCount(JavaExpression expr, boolean limit)
    {
        // narrowing conversion
        // Math.min(expr, 31)
        if (limit)
        {
            if (expr instanceof JavaIntLiteral)
            {
                JavaIntLiteral lit = (JavaIntLiteral) expr;
                if (lit.getValue() > 31) expr = new JavaIntLiteral(schema, 31);
            }
            else
            {
                expr = ExpressionBuilder.staticCall(types.mathType, "min",
                    expr, new JavaIntLiteral(schema, 31));
            }
        }
        return expr;
    }

    public boolean hasShiftCountConversion(JavaType type)
    {
        return schema.isDVIntegral(type) || isEnum(type);
    }

    public JavaExpression toShiftCount(JavaExpression expr, boolean limit)
        throws TypeConversionException
    {
        JavaType type = expr.getResultType();
        if (schema.isInt(type) || schema.isLong(type))
        {
            expr = intToShiftCount(expr, limit);
        }
        else if (schema.isBoolean(type))
        {
            expr = booleanToInt(expr);
        }
        else if (schema.isBitVector(type))
        {
            expr = bitVectorToShiftCount(expr, limit);
        }
        else if (type == schema.bitType)
        {
            expr = bitToShiftCount(expr);
        }
        else if (isEnum(type))
        {
            expr = intToShiftCount(enumToInt(expr), limit);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + type.toDebugString() + " to shift count (int)");
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to String
    ////////////////////////////////////////////////////////////

    public JavaExpression toStringOrBlank(JavaExpression expr)
    {
        return ExpressionBuilder.staticCall(types.stringOpType,
            "toStringOrBlank", expr);
    }

    public JavaExpression junoStringToJavaString(
        JavaExpression expr,
        boolean nullAsBlank)
    {
        // expr.toStringOrBlank()
        // expr.toStringOrNull()
        return ExpressionBuilder.memberCall(expr, nullAsBlank
            ? "toStringOrBlank" : "toStringOrNull");
    }

    public JavaExpression primitiveToJavaString(JavaExpression expr)
    {
        // String.valueOf(expr)
        return ExpressionBuilder.staticCall(schema.getStringType(), "valueOf",
            expr);
    }

    public JavaExpression objectToJavaString(JavaExpression expr)
    {
        // expr.toString()
        return ExpressionBuilder.memberCall(expr, "toString");
    }

    public boolean hasJavaStringConversion(Type type)
    {
        return hasObjectConversion(type);
    }

    public JavaExpression toJavaString(JavaExpression expr, boolean nullAsBlank)
        throws TypeConversionException
    {
        Type type = expr.getResultType();
        if (types.stringType.isAssignableFrom(type))
        {
            if (nullAsBlank && !(expr instanceof Literal))
            {
                expr = toStringOrBlank(expr);
            }
        }
        else if (type == types.junoStringType)
        {
            expr = junoStringToJavaString(expr, nullAsBlank);
        }
        else if (type instanceof JavaPrimitiveType)
        {
            expr = primitiveToJavaString(expr);
        }
        else
        {
            expr = objectToJavaString(expr);
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to JunoString
    ////////////////////////////////////////////////////////////

    public JavaExpression getNullJunoString()
    {
        return new JavaVariableReference(types.junoStringType.getField("NULL"));
    }

    public JavaExpression javaStringToJunoString(JavaExpression expr)
    {
        // new JunoString((String) expr)
        return ExpressionBuilder.newInstance(types.junoStringType, expr);
    }

    public boolean hasJunoStringConversion(Type type)
    {
        return hasObjectConversion(type);
    }

    public JavaExpression toJunoString(JavaExpression expr)
        throws TypeConversionException
    {
        Type type = expr.getResultType();
        if (type instanceof JavaNullType)
        {
            expr = getNullJunoString();
        }
        else if (types.junoStringType.isAssignableFrom(type))
        {
            // do nothing
        }
        else if (type == types.stringType)
        {
            expr = javaStringToJunoString(expr);
        }
        else
        {
            expr = javaStringToJunoString(toJavaString(expr, false));
        }
        return expr;
    }

    ////////////////////////////////////////////////////////////
    // Conversions to arbitrary type
    ////////////////////////////////////////////////////////////

    public boolean hasConversion(JavaType fromType, JavaType toType)
    {
        if (toType.isAssignableFrom(fromType))
        {
            // no conversion necessary
            return true;
        }
        else if (schema.isInt(toType))
        {
            return hasIntConversion(fromType);
        }
        else if (schema.isBoolean(toType))
        {
            return hasBooleanConversion(fromType);
        }
        else if (toType == schema.longWrapperType)
        {
            return hasLongWrapperConversion(fromType);
        }
        else if (schema.isBitVector(toType))
        {
            return hasBitVectorConversion(fromType);
        }
        else if (toType == schema.bitType)
        {
            return hasBitConversion(fromType);
        }
        else if (toType == types.stringType)
        {
            return hasJavaStringConversion(fromType);
        }
        else if (toType == types.junoStringType)
        {
            return hasJunoStringConversion(fromType);
        }
        else if (toType == types.objectType)
        {
            return hasObjectConversion(fromType);
        }
        return false;
    }

    public JavaExpression toType(JavaType type, JavaExpression expr)
        throws TypeConversionException
    {
        Type srcType = expr.getResultType();
        if (schema.isBitVector(type))
        {
            expr = toBitVector(expr, schema.getBitVectorSize(type), false);
        }
        else if (type.isAssignableFrom(srcType))
        {
            // do nothing
        }
        else if (type == schema.integerWrapperType)
        {
            expr = toInteger(expr);
        }
        else if (schema.isInt(type))
        {
            expr = toInt(expr);
        }
        else if (schema.isBoolean(type))
        {
            expr = toBoolean(expr, true, false);
        }
        else if (type == schema.longWrapperType)
        {
            expr = toLongWrapper(expr);
        }
        else if (type == schema.bitType)
        {
            expr = toBit(expr);
        }
        else if (type == types.stringType)
        {
            expr = toJavaString(expr, false);
        }
        else if (type == types.junoStringType)
        {
            expr = toJunoString(expr);
        }
        else if (type == types.objectType)
        {
            expr = toObject(expr);
        }
        else if ((type instanceof JavaAbstractClass
            && srcType instanceof JavaAbstractClass && ((JavaAbstractClass) srcType)
            .isSuperclassOf((JavaAbstractClass) type))
            || (type instanceof JavaStructuredType
                && srcType instanceof JavaInterface && ((JavaStructuredType) type)
                .implementsInterface((JavaInterface) srcType)))
        {
            expr = new JavaCastExpression(type, expr);
        }
        else
        {
            throw new TypeConversionException("Cannot convert "
                + srcType.toDebugString() + " to " + type.toDebugString());
        }
        return expr;
    }
}
