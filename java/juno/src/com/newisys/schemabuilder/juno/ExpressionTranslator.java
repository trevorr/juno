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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.juno.runtime.BooleanOp;
import com.newisys.langschema.Scope;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.Type;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.*;
import com.newisys.schemaanalyzer.juno.FunctionAnalysis;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorFormat;

/**
 * Schema translator for (non-constraint) expressions.
 * 
 * @author Trevor Robinson
 */
final class ExpressionTranslator
    extends TranslatorModule
    implements VeraExpressionVisitor
{
    final JavaStructuredType containingType;
    final VarInfoMap varInfoMap;
    final JavaLocalVariable returnVar;
    final JavaType promoteType;
    final JavaType desiredResultType;
    final ConvertedExpression result;

    boolean assignSampleAsync = false;
    boolean sampleAsync = true;

    public ExpressionTranslator(
        TranslatorModule xlatContext,
        JavaStructuredType containingType,
        VarInfoMap varInfoMap,
        JavaLocalVariable returnVar,
        JavaType promoteType,
        JavaType desiredResultType,
        ConvertedExpression result)
    {
        super(xlatContext);
        this.containingType = containingType;
        this.varInfoMap = varInfoMap;
        this.returnVar = returnVar;
        this.promoteType = promoteType;
        this.desiredResultType = desiredResultType;
        this.result = result;
    }

    public void setAssignSampleAsync(boolean assignSampleAsync)
    {
        this.assignSampleAsync = assignSampleAsync;
    }

    ConvertedExpression translateNewExpr(
        VeraExpression veraExpr,
        JavaType promoteType,
        JavaType desiredResultType,
        boolean sampleAsync)
    {
        return translateExpr(veraExpr, result.getScope(), containingType,
            varInfoMap, returnVar, promoteType, desiredResultType, sampleAsync);
    }

    ConvertedExpression translateNewExpr(
        VeraExpression veraExpr,
        JavaType desiredResultType,
        boolean sampleAsync)
    {
        return translateNewExpr(veraExpr, promoteType, desiredResultType,
            sampleAsync);
    }

    ConvertedExpression translateNewExpr(
        VeraExpression veraExpr,
        JavaType desiredResultType)
    {
        return translateNewExpr(veraExpr, promoteType, desiredResultType,
            sampleAsync);
    }

    ConvertedExpression translateNewExpr(VeraExpression veraExpr)
    {
        return translateNewExpr(veraExpr, promoteType, null, sampleAsync);
    }

    JavaExpression translateNestedExpr(
        VeraExpression veraExpr,
        String tempID,
        JavaType promoteType,
        JavaType desiredResultType,
        boolean sampleAsync)
    {
        ConvertedExpression nestedResult = translateNewExpr(veraExpr,
            promoteType, desiredResultType, sampleAsync);
        JavaExpression resultExpr = nestedResult.flatten(tempID);
        result.addInitMembers(nestedResult.getInitMembers());
        return resultExpr;
    }

    JavaExpression translateNestedExpr(
        VeraExpression veraExpr,
        String tempID,
        JavaType desiredResultType,
        boolean sampleAsync)
    {
        return translateNestedExpr(veraExpr, tempID, promoteType,
            desiredResultType, sampleAsync);
    }

    JavaExpression translateNestedExpr(
        VeraExpression veraExpr,
        String tempID,
        JavaType desiredResultType)
    {
        return translateNestedExpr(veraExpr, tempID, promoteType,
            desiredResultType, sampleAsync);
    }

    JavaExpression translateNestedExpr(
        VeraExpression veraExpr,
        JavaType desiredResultType)
    {
        return translateNestedExpr(veraExpr, "temp", promoteType,
            desiredResultType, sampleAsync);
    }

    JavaExpression translateNestedExpr(VeraExpression veraExpr, String tempID)
    {
        return translateNestedExpr(veraExpr, tempID, promoteType, null,
            sampleAsync);
    }

    JavaExpression translateNestedExpr(VeraExpression veraExpr)
    {
        return translateNestedExpr(veraExpr, "temp", promoteType, null,
            sampleAsync);
    }

    JavaExpression translateNestedIntExpr(VeraExpression veraExpr, String tempID)
    {
        return translateNestedExpr(veraExpr, tempID, promoteType,
            schema.intType, sampleAsync);
    }

    JavaExpression translateNestedIntExpr(VeraExpression veraExpr)
    {
        return translateNestedExpr(veraExpr, "temp", promoteType,
            schema.intType, sampleAsync);
    }

    LHSTranslator translateLHS(
        VeraExpression veraExpr,
        JavaType desiredResultType,
        boolean readAccess,
        boolean writeAccess)
    {
        return translateLHS(veraExpr, result, desiredResultType, readAccess,
            writeAccess);
    }

    LHSTranslator translateLHS(
        VeraExpression veraExpr,
        ConvertedExpression result,
        JavaType desiredResultType,
        boolean readAccess,
        boolean writeAccess)
    {
        LHSTranslator xlat;
        if (veraExpr instanceof VeraArrayAccess)
        {
            xlat = new ArrayLHSTranslator(this, result,
                (VeraArrayAccess) veraExpr, readAccess, writeAccess);
        }
        else if (veraExpr instanceof VeraBitSliceAccess)
        {
            xlat = new BitSliceLHSTranslator(this, result,
                (VeraBitSliceAccess) veraExpr, readAccess, writeAccess);
        }
        else if (veraExpr instanceof VeraConcatenation)
        {
            xlat = new ConcatLHSTranslator(this, result,
                (VeraConcatenation) veraExpr, readAccess, writeAccess);
        }
        else
        {
            xlat = new SimpleLHSTranslator(this, result, desiredResultType,
                veraExpr, readAccess, writeAccess);
        }
        return xlat;
    }

    static final int OP_NONE = 0;
    static final int OP_ADD = 1;
    static final int OP_SUBTRACT = 2;
    static final int OP_MULTIPLY = 3;
    static final int OP_DIVIDE = 4;
    static final int OP_MODULO = 5;
    static final int OP_NEGATE = 6;
    static final int OP_AND = 7;
    static final int OP_AND_NOT = 8;
    static final int OP_OR = 9;
    static final int OP_OR_NOT = 10;
    static final int OP_XOR = 11;
    static final int OP_XOR_NOT = 12;
    static final int OP_NOT = 13;
    static final int OP_REVERSE = 14;
    static final int OP_REDUCTIVE_AND = 15;
    static final int OP_REDUCTIVE_AND_NOT = 16;
    static final int OP_REDUCTIVE_OR = 17;
    static final int OP_REDUCTIVE_OR_NOT = 18;
    static final int OP_REDUCTIVE_XOR = 19;
    static final int OP_REDUCTIVE_XOR_NOT = 20;
    static final int OP_EQ = 21;
    static final int OP_NE = 22;
    static final int OP_EXACT_EQ = 23;
    static final int OP_EXACT_NE = 24;
    static final int OP_WILD_EQ = 25;
    static final int OP_WILD_NE = 26;
    static final int OP_GT = 27;
    static final int OP_GE = 28;
    static final int OP_LT = 29;
    static final int OP_LE = 30;
    static final int OP_SHIFT_LEFT = 31;
    static final int OP_SHIFT_RIGHT = 32;
    static final int OP_PRE_INC = 33;
    static final int OP_PRE_DEC = 34;
    static final int OP_POST_INC = 35;
    static final int OP_POST_DEC = 36;

    private int getTypeWidthChecked(JavaType type)
    {
        return (type != null && schema.isDVIntegral(type)) ? schema
            .getTypeWidth(type) : -1;
    }

    private JavaExpression contextPromote(
        JavaExpression expr,
        boolean signExtend)
    {
        JavaType type = expr.getResultType();
        if (schema.isDVIntegral(type))
        {
            int len = schema.getTypeWidth(type);
            int promoteLen = getTypeWidthChecked(promoteType);
            if (len < promoteLen)
            {
                if (schema.isBitVector(type))
                {
                    if (len > 0)
                    {
                        // original length is known (and is definitely less
                        // than the promotion length) so we can unconditionally
                        // call setLength() on the resulting bit vector
                        expr = exprConv.setBitVectorLength(expr, promoteLen);
                    }
                    else
                    {
                        // original length is not known (and may already be
                        // larger than the promotion length) so we must call
                        // BitVectorOp.promote() which checks the length at
                        // run time
                        expr = ExpressionBuilder.staticCall(
                            types.bitVectorOpType, "promote", expr,
                            new JavaIntLiteral(schema, promoteLen));
                    }
                }
                else
                {
                    // bit/integer/enum -> bit vector
                    expr = exprConv.toBitVector(expr, promoteLen, signExtend);
                }
            }
        }
        return expr;
    }

    private void doUnaryArithmetic(VeraUnaryOperation obj, int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 1);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        JavaExpression op1 = translateNestedExpr(veraOp1);
        op1 = contextPromote(op1, false);
        final JavaType type1 = op1.getResultType();
        final ArithmeticOperationBuilder builder;
        if (type1 instanceof JavaNumericType)
        {
            // int/long
            builder = new IntOperationBuilder(this);
        }
        else if (type1 instanceof JavaBooleanType)
        {
            // boolean
            builder = new BooleanOperationBuilder(this);
        }
        else if (type1 == schema.bitType)
        {
            // Bit
            builder = new BitOperationBuilder(this);
        }
        else if (schema.isBitVector(type1))
        {
            // BitVector
            builder = new BitVectorOperationBuilder(this);
        }
        else
        {
            // Integer or enum
            op1 = exprConv.toInteger(op1);
            builder = new IntegerOperationBuilder(this);
        }

        final JavaExpression resultExpr;
        switch (opcode)
        {
        case OP_NEGATE:
            resultExpr = builder.negate(op1);
            break;
        case OP_NOT:
            resultExpr = builder.not(op1);
            break;
        case OP_REVERSE:
            resultExpr = builder.reverse(op1);
            break;
        case OP_REDUCTIVE_AND:
            resultExpr = builder.reductiveAnd(op1);
            break;
        case OP_REDUCTIVE_AND_NOT:
            resultExpr = builder.reductiveAndNot(op1);
            break;
        case OP_REDUCTIVE_OR:
            resultExpr = builder.reductiveOr(op1);
            break;
        case OP_REDUCTIVE_OR_NOT:
            resultExpr = builder.reductiveOrNot(op1);
            break;
        case OP_REDUCTIVE_XOR:
            resultExpr = builder.reductiveXor(op1);
            break;
        case OP_REDUCTIVE_XOR_NOT:
            resultExpr = builder.reductiveXorNot(op1);
            break;
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    private void doBinaryArithmetic(
        VeraBinaryArithmeticOperation obj,
        int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = translateNestedExpr(veraOp1);
        final JavaExpression op2 = translateNestedExpr(veraOp2);
        final JavaExpression resultExpr = doBinaryArithmetic(op1, op2, opcode);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    JavaExpression doBinaryArithmetic(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        op1 = contextPromote(op1, false);
        op2 = contextPromote(op2, opcode == OP_ADD);
        final JavaType type1 = op1.getResultType();
        final JavaType type2 = op2.getResultType();
        final ArithmeticOperationBuilder builder;
        if (type1 instanceof JavaNumericType
            && type2 instanceof JavaNumericType)
        {
            // int/long / int/long
            builder = new IntOperationBuilder(this);
        }
        else if (type1 instanceof JavaBooleanType
            && type2 instanceof JavaBooleanType)
        {
            // boolean / boolean
            builder = new BooleanOperationBuilder(this);
        }
        else if (type1 == schema.bitType && type2 == schema.bitType)
        {
            // Bit / Bit
            builder = new BitOperationBuilder(this);
        }
        else if (schema.isBitVector(type1) || schema.isBitVector(type2))
        {
            // promote to BitVector
            op1 = exprConv.toUnsizedBitVector(op1);
            op2 = exprConv.toUnsizedBitVector(op2);
            builder = new BitVectorOperationBuilder(this);
        }
        else
        {
            // promote to Integer
            op1 = exprConv.toInteger(op1);
            op2 = exprConv.toInteger(op2);
            builder = new IntegerOperationBuilder(this);
        }

        switch (opcode)
        {
        case OP_ADD:
            return builder.add(op1, op2);
        case OP_SUBTRACT:
            return builder.subtract(op1, op2);
        case OP_MULTIPLY:
            return builder.multiply(op1, op2);
        case OP_DIVIDE:
            return builder.divide(op1, op2);
        case OP_MODULO:
            return builder.mod(op1, op2);
        case OP_AND:
            return builder.and(op1, op2);
        case OP_AND_NOT:
            return builder.andNot(op1, op2);
        case OP_OR:
            return builder.or(op1, op2);
        case OP_OR_NOT:
            return builder.orNot(op1, op2);
        case OP_XOR:
            return builder.xor(op1, op2);
        case OP_XOR_NOT:
            return builder.xorNot(op1, op2);
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
    }

    private void doShift(VeraShiftOperation obj, int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = translateNestedExpr(veraOp1);
        final JavaExpression op2 = translateNestedIntExpr(veraOp2, "count");
        final JavaExpression resultExpr = doShift(op1, op2, opcode);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    private JavaExpression doShift(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        if (opcode == OP_SHIFT_LEFT)
        {
            op1 = contextPromote(op1, false);
        }
        final JavaType type1 = op1.getResultType();
        final ShiftOperationBuilder builder;
        if (type1 instanceof JavaNumericType)
        {
            // int/long
            op2 = exprConv.toShiftCount(op2, true);
            builder = new IntOperationBuilder(this);
        }
        else if (schema.isBitVector(type1))
        {
            // BitVector
            op2 = exprConv.toShiftCount(op2, false);
            builder = new BitVectorOperationBuilder(this);
        }
        else
        {
            // promote to Integer
            op1 = exprConv.toInteger(op1);
            op2 = exprConv.toInteger(op2);
            builder = new IntegerOperationBuilder(this);
        }

        switch (opcode)
        {
        case OP_SHIFT_LEFT:
            return builder.shiftLeft(op1, op2);
        case OP_SHIFT_RIGHT:
            return builder.shiftRight(op1, op2);
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
    }

    private void doEquality(VeraBinaryOperation obj, int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = translateNestedExpr(veraOp1);
        final JavaExpression op2 = translateNestedExpr(veraOp2);
        final JavaExpression resultExpr = buildEquality(op1, op2, opcode);
        resultExpr.addAnnotations(obj.getAnnotations());
    }

    JavaExpression buildEquality(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        JavaType type1 = op1.getResultType();
        JavaType type2 = op2.getResultType();
        EqualityOperationBuilder builder = null;
        if (!(type1 instanceof JavaPrimitiveType && type2 instanceof JavaPrimitiveType))
        {
            if (schema.isBitVector(type1) || schema.isBitVector(type2))
            {
                // promote to BitVector
                op1 = exprConv.toUnsizedBitVector(op1);
                op2 = exprConv.toUnsizedBitVector(op2);
                builder = new BitVectorOperationBuilder(this);
            }
            else if (type1 == schema.integerWrapperType
                || type2 == schema.integerWrapperType
                || type1 == schema.intType || type2 == schema.intType)
            {
                // promote to Integer
                op1 = exprConv.toInteger(op1);
                op2 = exprConv.toInteger(op2);
                builder = new IntegerOperationBuilder(this);
            }
            else if (type1 == schema.bitType || type2 == schema.bitType
                || type1 == schema.booleanType || type2 == schema.booleanType)
            {
                // promote to Bit
                op1 = exprConv.toBit(op1);
                op2 = exprConv.toBit(op2);
                builder = new BitOperationBuilder(this);
            }
            else if (type1 == types.stringType && type2 == types.stringType)
            {
                // both String
                builder = new EqualsEqualityOperationBuilder(this, true);
            }
            else if (needsClone(type1))
            {
                // use equals() method for cloned objects
                builder = new EqualsEqualityOperationBuilder(this);
            }
            else if (needsClone(type2))
            {
                if (type2 == types.junoStringType)
                {
                    // swap operands to use JunoString.equals()
                    JavaExpression opTmp = op1;
                    op1 = op2;
                    op2 = opTmp;
                    JavaType typeTmp = type1;
                    type1 = type2;
                    type2 = typeTmp;
                }
                // use equals() method for cloned objects
                builder = new EqualsEqualityOperationBuilder(this);
            }
        }
        if (builder == null)
        {
            // use built-in operators for primitives or non-cloned objects

            // the built-in equality operators require boolean == boolean or
            // numeric == numeric, so we need to convert boolean to int when
            // boolean == numeric; also, if the numeric expression is a
            // constant, we can optimize it away
            if (type1 instanceof JavaBooleanType
                ^ type2 instanceof JavaBooleanType)
            {
                final JavaExpression boolOp, otherOp;
                if (type1 instanceof JavaBooleanType)
                {
                    boolOp = op1;
                    otherOp = op2;
                }
                else
                {
                    boolOp = op2;
                    otherOp = op1;
                }
                if (otherOp instanceof JavaIntLiteral)
                {
                    final JavaIntLiteral lit = (JavaIntLiteral) otherOp;
                    final int litValue = lit.getValue();
                    final boolean isEq = (opcode == OP_EQ
                        || opcode == OP_EXACT_EQ || opcode == OP_WILD_EQ);
                    final JavaExpression resultExpr;
                    if (litValue < 0 || litValue > 1)
                    {
                        // comparing to other than 0/1 is always false
                        resultExpr = new JavaBooleanLiteral(schema, false);
                    }
                    else if (isEq == (litValue == 1))
                    {
                        // == 1, != 0 -> boolOp
                        resultExpr = boolOp;
                    }
                    else
                    {
                        // == 0, != 1 -> !boolOp
                        resultExpr = new JavaLogicalNot(schema, boolOp);
                    }
                    result.setResultExpr(resultExpr);
                    return resultExpr;
                }
                else if (type1 instanceof JavaBooleanType)
                {
                    op1 = exprConv.toInt(op1);
                }
                else
                {
                    op2 = exprConv.toInt(op2);
                }
            }

            builder = new PrimitiveEqualityOperationBuilder(this);
        }

        final JavaExpression resultExpr;
        switch (opcode)
        {
        case OP_EQ:
            resultExpr = builder.equal(op1, op2);
            break;
        case OP_NE:
            resultExpr = builder.notEqual(op1, op2);
            break;
        case OP_EXACT_EQ:
            resultExpr = builder.exactEqual(op1, op2);
            break;
        case OP_EXACT_NE:
            resultExpr = builder.exactNotEqual(op1, op2);
            break;
        case OP_WILD_EQ:
            resultExpr = builder.wildEqual(op1, op2);
            break;
        case OP_WILD_NE:
            resultExpr = builder.wildNotEqual(op1, op2);
            break;
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
        result.setResultExpr(resultExpr);
        return resultExpr;
    }

    private void doRelation(VeraComparisonOperation obj, int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        JavaExpression op1 = translateNestedExpr(veraOp1);
        JavaExpression op2 = translateNestedExpr(veraOp2);
        final JavaType type1 = op1.getResultType();
        final JavaType type2 = op2.getResultType();
        final RelationalOperationBuilder builder;
        if (type1 instanceof JavaNumericType
            && type2 instanceof JavaNumericType)
        {
            // int/long / int/long
            builder = new IntOperationBuilder(this);
        }
        else if (schema.isBitVector(type1) || schema.isBitVector(type2))
        {
            // promote to BitVector
            op1 = exprConv.toUnsizedBitVector(op1);
            op2 = exprConv.toUnsizedBitVector(op2);
            builder = new BitVectorOperationBuilder(this);
        }
        else
        {
            // promote to Integer
            op1 = exprConv.toInteger(op1);
            op2 = exprConv.toInteger(op2);
            builder = new IntegerOperationBuilder(this);
        }

        final JavaExpression resultExpr;
        switch (opcode)
        {
        case OP_GT:
            resultExpr = builder.greater(op1, op2);
            break;
        case OP_GE:
            resultExpr = builder.greaterOrEqual(op1, op2);
            break;
        case OP_LT:
            resultExpr = builder.less(op1, op2);
            break;
        case OP_LE:
            resultExpr = builder.lessOrEqual(op1, op2);
            break;
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    boolean hasJavaLHS(VeraExpression veraExpr)
    {
        if (veraExpr.isAssignable())
        {
            if (veraExpr instanceof VeraVariableReference
                || veraExpr instanceof VeraMemberAccess
                || veraExpr instanceof VeraSignalReference)
            {
                return true;
            }
            else if (veraExpr instanceof VeraArrayAccess)
            {
                // associative array access is not a Java LHS
                Type type = veraExpr.getResultType();
                return type instanceof VeraFixedArrayType
                    || type instanceof VeraDynamicArrayType;
            }
            else
            {
                assert (veraExpr instanceof VeraBitSliceAccess || veraExpr instanceof VeraConcatenation);
            }
        }
        return false;
    }

    private void doAssignOp(VeraAssignment obj, int opcode)
    {
        List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraLHS = (VeraExpression) operands.get(0);
        final VeraExpression veraRHS = (VeraExpression) operands.get(1);
        buildAssignOp(veraLHS, veraRHS, opcode);
        result.getResultExpr().addAnnotations(obj.getAnnotations());
    }

    void buildAssignOp(VeraExpression veraLHS, VeraExpression veraRHS)
    {
        buildAssignOp(veraLHS, veraRHS, OP_NONE);
    }

    void buildAssignOp(
        VeraExpression veraLHS,
        VeraExpression veraRHS,
        int opcode)
    {
        JavaExpression rhs = null;
        JavaType rhsType = null;

        // attempt to use built-in operator if LHS translates to a Java LHS
        if (hasJavaLHS(veraLHS))
        {
            // translate LHS
            JavaExpression lhs = translateNestedExpr(veraLHS,
                types.outputSignalType);
            JavaType lhsType = lhs.getResultType();
            if (exprConv.isOutputSignal(lhsType))
            {
                // Vera does not allow compound assignment on signals
                assert (opcode == OP_NONE);

                // translate RHS using signal width for signalType type
                final JavaType signalType = translateType(veraLHS
                    .getResultType());
                rhs = translateNestedExpr(veraRHS, "rhs", signalType,
                    signalType, assignSampleAsync);
                rhs = exprConv.toObject(rhs);

                // translate drive
                result.setResultExpr(ExpressionBuilder.memberCall(lhs, "drive",
                    rhs));
                return;
            }
            else
            {
                // translate RHS using LHS type for promotion type
                rhs = translateNestedExpr(veraRHS, "rhs", lhsType, lhsType,
                    assignSampleAsync);
                rhsType = rhs.getResultType();

                if (opcode == OP_NONE)
                {
                    // return Java assignment expression
                    LHSTranslator lhsXlat = new SimpleLHSTranslator(this,
                        result, lhs, false, true);
                    if (rhs instanceof JavaArrayInitializer)
                    {
                        // initializers for Vera global variables are changed to
                        // assignments by the Vera schema builder (see
                        // VeraSchemaBuilder.visit(ProgramDecl)); for array
                        // initializers, we need to wrap them in an array
                        // creation expression to produce valid Java
                        JavaArrayInitializer initExpr = (JavaArrayInitializer) rhs;
                        JavaArrayType arrayType = initExpr.getResultType();
                        JavaArrayCreation newArrayExpr = new JavaArrayCreation(
                            arrayType);
                        newArrayExpr.setInitializer(initExpr);
                        rhs = newArrayExpr;
                    }
                    else
                    {
                        rhs = convertRHS(rhs, rhsType, lhsType, false);
                    }
                    lhsXlat.getWriteExpression(rhs).mergeIntoResult(result);
                    return;
                }
                else if (lhsType instanceof JavaNumericType
                    && rhsType instanceof JavaNumericType)
                {
                    // check for wait_var update event
                    JavaExpression updateEvent = getWaitVarEventRef(lhs);
                    if (updateEvent != null)
                    {
                        // LHS must be evaluated twice if update event is
                        // present
                        lhs = EvalOnceExprBuilder.evalLHSExpr(lhs, result,
                            "lhs", true);
                    }

                    // attempt to use Java compound assignment operator
                    JavaExpression resultExpr = getJavaCompoundAssign(lhs, rhs,
                        opcode);
                    if (resultExpr != null)
                    {
                        result.setResultExpr(resultExpr);

                        // generate wait_var notification
                        if (updateEvent != null)
                        {
                            JavaExpression oldValue = result.addTempFor(
                                "old_value", lhs, true);
                            checkUpdate(result, oldValue, lhs, true,
                                updateEvent);
                        }

                        return;
                    }
                }
            }
        }

        // translate LHS
        final LHSTranslator lhsXlat = translateLHS(veraLHS,
            types.outputSignalType, opcode != OP_NONE, true);
        final JavaType lhsType = lhsXlat.getResultType();

        // translate RHS using LHS type for promotion type (if not done already)
        if (rhs == null)
        {
            rhs = translateNestedExpr(veraRHS, "rhs", lhsType, lhsType,
                assignSampleAsync);
            rhsType = rhs.getResultType();
        }

        // perform compound assignment operation if specified
        if (opcode != OP_NONE)
        {
            rhs = expandAssignOp(lhsXlat.getReadExpression(), rhs, opcode);
        }

        // assign result to LHS
        if (!exprConv.isOutputSignal(lhsType))
        {
            rhs = convertRHS(rhs, rhsType, lhsType, false);
        }
        lhsXlat.getWriteExpression(rhs).mergeIntoResult(result);
    }

    private JavaExpression getJavaCompoundAssign(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        switch (opcode)
        {
        case OP_ADD:
            return new JavaAssignAdd(schema, op1, op2);
        case OP_SUBTRACT:
            return new JavaAssignSubtract(schema, op1, op2);
        case OP_MULTIPLY:
            return new JavaAssignMultiply(schema, op1, op2);
        case OP_DIVIDE:
            return new JavaAssignDivide(schema, op1, op2);
        case OP_MODULO:
            return new JavaAssignModulo(schema, op1, op2);
        case OP_AND:
            return new JavaAssignAnd(schema, op1, op2);
        case OP_OR:
            return new JavaAssignOr(schema, op1, op2);
        case OP_XOR:
            return new JavaAssignXor(schema, op1, op2);
        case OP_SHIFT_LEFT:
            return new JavaAssignLeftShift(schema, op1, op2);
        case OP_SHIFT_RIGHT:
            return new JavaAssignSignedRightShift(schema, op1, op2);
        default:
            // simply return null if no operator is available
            return null;
        }
    }

    private JavaExpression expandAssignOp(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        switch (opcode)
        {
        case OP_ADD:
        case OP_SUBTRACT:
            Type type1 = op1.getResultType();
            if (exprConv.isEnum(type1))
            {
                JavaEnum cls = (JavaEnum) type1;
                String methodID = opcode == OP_ADD ? "next" : "previous";
                JavaFunction method = cls.getMethod(methodID,
                    new JavaType[] { schema.intType });
                JavaFunctionInvocation callExpr = new JavaFunctionInvocation(
                    new JavaMemberAccess(op1, method));
                callExpr.addArgument(exprConv.toInt(op2));
                return callExpr;
            }
        case OP_MULTIPLY:
        case OP_DIVIDE:
        case OP_MODULO:
        case OP_AND:
        case OP_AND_NOT:
        case OP_OR:
        case OP_OR_NOT:
        case OP_XOR:
        case OP_XOR_NOT:
            return doBinaryArithmetic(op1, op2, opcode);
        case OP_SHIFT_LEFT:
        case OP_SHIFT_RIGHT:
            return doShift(op1, op2, opcode);
        default:
            throw new RuntimeException("Unknown opcode: " + opcode);
        }
    }

    private void doIncDec(VeraIncDecOperation obj, int opcode)
    {
        // get Vera expression
        List operands = obj.getOperands();
        assert (operands.size() == 1);
        final VeraExpression veraExpr = (VeraExpression) operands.get(0);

        // attempt to use built-in operator if expr translates to a Java LHS
        if (hasJavaLHS(veraExpr))
        {
            JavaExpression lhs = translateNestedExpr(veraExpr);
            final JavaType lhsType = lhs.getResultType();
            if (lhsType instanceof JavaNumericType)
            {
                // check for wait_var update event
                JavaExpression updateEvent = getWaitVarEventRef(lhs);
                if (updateEvent != null)
                {
                    // LHS must be evaluated twice if update event is present
                    lhs = EvalOnceExprBuilder.evalLHSExpr(lhs, result, "lhs",
                        true);
                }

                // build result expression
                final JavaExpression resultExpr;
                switch (opcode)
                {
                case OP_PRE_INC:
                    resultExpr = new JavaPreIncrement(schema, lhs);
                    break;
                case OP_PRE_DEC:
                    resultExpr = new JavaPreDecrement(schema, lhs);
                    break;
                case OP_POST_INC:
                    resultExpr = new JavaPostIncrement(schema, lhs);
                    break;
                case OP_POST_DEC:
                    resultExpr = new JavaPostDecrement(schema, lhs);
                    break;
                default:
                    throw new RuntimeException("Unknown opcode: " + opcode);
                }
                result.setResultExpr(resultExpr);

                // generate wait_var notification
                if (updateEvent != null)
                {
                    JavaExpression oldValue = result.addTempFor("old_value",
                        lhs, true);
                    checkUpdate(result, oldValue, lhs, true, updateEvent);
                }

                return;
            }
        }

        // decode operator
        // optimization: force use of pre-inc/dec in void context
        final boolean isPreOp = (opcode == OP_PRE_INC || opcode == OP_PRE_DEC)
            || desiredResultType == schema.voidType;
        final boolean isIncOp = (opcode == OP_PRE_INC || opcode == OP_POST_INC);

        // translate LHS
        final LHSTranslator lhsXlat = translateLHS(veraExpr, desiredResultType,
            true, true);
        final JavaType lhsType = lhsXlat.getResultType();

        // get read expression
        JavaExpression readExpr = lhsXlat.getReadExpression();
        if (!isPreOp)
        {
            // post-inc/dec evaluates read twice
            readExpr = result.addTempFor("temp", readExpr, true);
        }

        // perform inc/dec operation
        JavaExpression opExpr = expandIncDecOp(readExpr, isIncOp);

        // convert operation result type if necessary
        JavaType opResultType = opExpr.getResultType();
        if (needRHSConversion(lhsType, opResultType))
        {
            opExpr = exprConv.toType(lhsType, opExpr);
        }

        // assign result back
        final ConvertedExpression writeExpr = lhsXlat
            .getWriteExpression(opExpr);

        if (isPreOp)
        {
            // for pre-inc/dec, the result comes from the write
            writeExpr.mergeIntoResult(result);
        }
        else
        {
            // for post-inc/dec, return the read and add statement for write
            result.setResultExpr(readExpr);
            writeExpr.mergeIntoUpdate(result);
        }
    }

    private JavaExpression expandIncDecOp(JavaExpression expr, boolean isIncOp)
    {
        final Type resultType = expr.getResultType();
        if (exprConv.isEnum(resultType))
        {
            final JavaRawAbstractClass cls = (JavaRawAbstractClass) resultType;
            final String methodID = isIncOp ? "next" : "previous";
            return new JavaFunctionInvocation(new JavaMemberAccess(expr, cls
                .getMethod(methodID)));
        }
        else
        {
            final int addSubOpcode = isIncOp ? OP_ADD : OP_SUBTRACT;
            return expandAssignOp(expr, new JavaIntLiteral(schema, 1),
                addSubOpcode);
        }
    }

    public void visit(VeraAdd obj)
    {
        doBinaryArithmetic(obj, OP_ADD);
    }

    public void visit(VeraAndReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_AND);
    }

    public void visit(VeraArithmeticNegative obj)
    {
        doUnaryArithmetic(obj, OP_NEGATE);
    }

    public void visit(VeraArrayAccess obj)
    {
        final LHSTranslator xlat = new ArrayLHSTranslator(this, result, obj,
            true, false);
        // default access is read;
        // write contexts explicitly check for array access
        JavaExpression readExpr = xlat.getReadExpression();
        readExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(readExpr);
    }

    public void visit(VeraArrayCreation obj)
    {
        // translate array tyoe
        final VeraDynamicArrayType veraArrayType = obj.getType();
        final JavaArrayType arrayType = (JavaArrayType) translateType(veraArrayType);

        // translate size expression
        final JavaExpression sizeExpr = exprConv.toInt(translateNestedIntExpr(
            obj.getSizeExpr(), "size"));

        // create Java array creation schema object
        final JavaArrayCreation newExpr = new JavaArrayCreation(arrayType);
        newExpr.addAnnotations(obj.getAnnotations());
        newExpr.addDimension(sizeExpr);

        // determine whether array elements need initialization
        JavaType elemType = arrayType.getElementType();
        JavaExpression elemInitExpr = getInitValue(elemType, false, true);

        // check for source array to copy from
        final JavaExpression resultExpr;
        final VeraExpression veraSrcExpr = obj.getSourceExpr();
        if (veraSrcExpr != null)
        {
            // translate source array expression
            final JavaExpression srcExpr = translateNestedExpr(veraSrcExpr,
                "src");
            assert (srcExpr.getResultType() instanceof JavaArrayType);

            // generate call to Vera.copyArray()
            if (elemInitExpr == null)
            {
                elemInitExpr = new JavaCastExpression(types.objectType,
                    new JavaNullLiteral(schema));
            }
            final JavaExpression[] args = { newExpr, srcExpr, elemInitExpr };
            resultExpr = ExpressionBuilder
                .checkDowncast(ExpressionBuilder.staticCall(types.junoType,
                    "copyArray", args, null), arrayType);
        }
        else if (elemInitExpr != null)
        {
            // no source array, but elements need initialization
            // generate call to Vera.initArray()
            final JavaExpression[] args = { newExpr, elemInitExpr,
                new JavaIntLiteral(schema, 0) };
            resultExpr = ExpressionBuilder
                .checkDowncast(ExpressionBuilder.staticCall(types.junoType,
                    "initArray", args, null), arrayType);
        }
        else
        {
            // no initialization necessary
            resultExpr = newExpr;
        }
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraArrayInitializer obj)
    {
        // translate array tyoe
        final VeraArrayType veraArrayType = (VeraArrayType) obj.getResultType();
        final JavaArrayType arrayType = (JavaArrayType) translateType(veraArrayType);
        final JavaType elemType = arrayType.getAccessType(1);

        // create Java array initializer schema object
        final JavaArrayInitializer initExpr = new JavaArrayInitializer(
            arrayType);
        initExpr.addAnnotations(obj.getAnnotations());

        // translate initializer elements
        final Iterator iter = obj.getElements().iterator();
        while (iter.hasNext())
        {
            final VeraExpression veraElemExpr = (VeraExpression) iter.next();
            final JavaExpression elemExpr = translateNestedExpr(veraElemExpr,
                "elem", elemType);
            initExpr.addElement(exprConv.toType(elemType, elemExpr));
        }
        result.setResultExpr(initExpr);
    }

    public void visit(VeraAssign obj)
    {
        doAssignOp(obj, OP_NONE);
    }

    public void visit(VeraAssignAdd obj)
    {
        doAssignOp(obj, OP_ADD);
    }

    public void visit(VeraAssignAnd obj)
    {
        doAssignOp(obj, OP_AND);
    }

    public void visit(VeraAssignAndNot obj)
    {
        doAssignOp(obj, OP_AND_NOT);
    }

    public void visit(VeraAssignDivide obj)
    {
        doAssignOp(obj, OP_DIVIDE);
    }

    public void visit(VeraAssignLeftShift obj)
    {
        doAssignOp(obj, OP_SHIFT_LEFT);
    }

    public void visit(VeraAssignModulo obj)
    {
        doAssignOp(obj, OP_MODULO);
    }

    public void visit(VeraAssignMultiply obj)
    {
        doAssignOp(obj, OP_MULTIPLY);
    }

    public void visit(VeraAssignOr obj)
    {
        doAssignOp(obj, OP_OR);
    }

    public void visit(VeraAssignOrNot obj)
    {
        doAssignOp(obj, OP_OR_NOT);
    }

    public void visit(VeraAssignRightShift obj)
    {
        doAssignOp(obj, OP_SHIFT_RIGHT);
    }

    public void visit(VeraAssignSubtract obj)
    {
        doAssignOp(obj, OP_SUBTRACT);
    }

    public void visit(VeraAssignXor obj)
    {
        doAssignOp(obj, OP_XOR);
    }

    public void visit(VeraAssignXorNot obj)
    {
        doAssignOp(obj, OP_XOR_NOT);
    }

    public void visit(VeraBitSliceAccess obj)
    {
        final LHSTranslator xlat = new BitSliceLHSTranslator(this, result, obj,
            true, false);
        // default access is read;
        // write contexts explicitly check for bit slice access
        JavaExpression readExpr = xlat.getReadExpression();
        readExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(readExpr);
    }

    public void visit(VeraBitVectorLiteral obj)
    {
        final JavaExpression resultExpr;
        final BitVector bv = obj.getValue();
        int len = bv.length();
        final boolean xz = bv.containsXZ();
        final int radix = obj.getRadix();
        final int promoteLen = getTypeWidthChecked(desiredResultType);
        if (len == 1 && !xz && desiredResultType instanceof JavaBooleanType)
        {
            // boolean result: 1'b0,1 -> false, true
            resultExpr = new JavaBooleanLiteral(schema, BooleanOp.toBoolean(bv));
        }
        else if (len == 1 && promoteLen <= 1)
        {
            if (schema.isBitVector(desiredResultType))
            {
                // BitVector result: 1'b0,1,x,z -> BitVectorOp.BIT_0, BIT_1, ...
                resultExpr = exprConv.getBitVectorExpr(bv.getBit(0));
            }
            else
            {
                // Bit result: 1'b0,1,x,z -> Bit.ZERO, ONE, X, Z
                resultExpr = exprConv.getBitExpr(bv.getBit(0));
            }
        }
        else
        {
            // BitVector result
            // extend length if necessary for context
            int resultLen = (promoteLen > len) ? promoteLen : len;

            // get bit vector type of give length
            JavaClass bitVectorType = schema.getBitVectorType(resultLen);

            if (!xz && (len < 32 || (len == 32 && bv.getBit(31) == Bit.ZERO)))
            {
                // int result
                JavaIntLiteral intLiteral = new JavaIntLiteral(schema, bv
                    .intValue());
                intLiteral.setRadix(getJavaIntRadix(radix));
                resultExpr = ExpressionBuilder.newInstance(bitVectorType,
                    new JavaIntLiteral(schema, resultLen), intLiteral);
            }
            else if (!xz
                && (len < 64 || (len == 64 && bv.getBit(63) == Bit.ZERO)))
            {
                // long result
                JavaLongLiteral longLiteral = new JavaLongLiteral(schema, bv
                    .longValue());
                longLiteral.setRadix(getJavaIntRadix(radix));
                resultExpr = ExpressionBuilder.newInstance(bitVectorType,
                    new JavaIntLiteral(schema, resultLen), longLiteral);
            }
            else if (xz ? bv.getBitCount(Bit.X) == len
                || bv.getBitCount(Bit.Z) == len
                : bv.getBitCount(Bit.ZERO) == len
                    || bv.getBitCount(Bit.ONE) == len)
            {
                // 256'bx -> new BitVector(256, Bit.X)
                resultExpr = ExpressionBuilder.newInstance(bitVectorType,
                    new JavaIntLiteral(schema, resultLen), exprConv
                        .getBitExpr(bv.getBit(0)));
            }
            else
            {
                // 128'hXF -> new BitVector("128'hXF")
                BitVectorFormat bvFormat = new BitVectorFormat();
                if (radix < 8) bvFormat.setFormatWidth(0);
                String bvStr = bvFormat.format(bv.setLength(resultLen), radix);
                resultExpr = ExpressionBuilder.newInstance(bitVectorType,
                    new JavaStringLiteral(schema, bvStr));
            }
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    private int getJavaIntRadix(int bvRadix)
    {
        final int intRadix;
        switch (bvRadix)
        {
        case 16:
        case 8:
            // 16'hFFFF -> 0xFFFF
            // 8'o377 -> 0377
            intRadix = bvRadix;
            break;
        case 2:
            // 16'b1111 -> 0xF
            intRadix = 16;
            break;
        default:
            // 16'd100 -> 100
            intRadix = 10;
        }
        return intRadix;
    }

    public void visit(VeraBitwiseAnd obj)
    {
        doBinaryArithmetic(obj, OP_AND);
    }

    public void visit(VeraBitwiseAndNot obj)
    {
        doBinaryArithmetic(obj, OP_AND_NOT);
    }

    public void visit(VeraBitwiseNegative obj)
    {
        doUnaryArithmetic(obj, OP_NOT);
    }

    public void visit(VeraBitwiseOr obj)
    {
        doBinaryArithmetic(obj, OP_OR);
    }

    public void visit(VeraBitwiseOrNot obj)
    {
        doBinaryArithmetic(obj, OP_OR_NOT);
    }

    public void visit(VeraBitwiseReverse obj)
    {
        doUnaryArithmetic(obj, OP_REVERSE);
    }

    public void visit(VeraBitwiseXor obj)
    {
        doBinaryArithmetic(obj, OP_XOR);
    }

    public void visit(VeraBitwiseXorNot obj)
    {
        doBinaryArithmetic(obj, OP_XOR_NOT);
    }

    public void visit(VeraConcatenation obj)
    {
        final JavaExpression resultExpr;
        final List<VeraExpression> operands = obj.getOperands();
        final List<JavaExpression> javaExprs = new LinkedList<JavaExpression>();
        if (obj.getResultType() instanceof VeraStringType)
        {
            // string concatenation
            for (final VeraExpression veraExpr : operands)
            {
                JavaExpression javaExpr = translateNestedExpr(veraExpr, "str");
                javaExpr = exprConv.toJavaString(javaExpr, false);
                javaExprs.add(javaExpr);
            }
            resultExpr = buildStringConcat(javaExprs);
        }
        else
        {
            // bit concatenation
            for (final VeraExpression veraExpr : operands)
            {
                int veraBits = veraExpr.getResultType().getBitCount();
                JavaType desiredResultType = schema.getBitVectorType(veraBits);
                JavaExpression javaExpr = translateNestedExpr(veraExpr, "bv",
                    null, desiredResultType, sampleAsync);
                javaExpr = exprConv.toBitVector(javaExpr, veraBits, false);
                javaExprs.add(javaExpr);
            }
            resultExpr = buildBitVectorConcat(javaExprs);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    JavaExpression buildStringConcat(List<JavaExpression> javaExprs)
    {
        final JavaArrayType stringArrType = schema.getArrayType(schema
            .getStringType(), 1);
        final JavaFunction concatMethod = types.stringOpType.getMethod(
            "concat", new JavaType[] { stringArrType });
        final JavaFunctionInvocation concatCall = new JavaFunctionInvocation(
            new JavaFunctionReference(concatMethod));
        for (final JavaExpression expr : javaExprs)
        {
            concatCall.addArgument(expr);
        }
        return concatCall;
    }

    JavaExpression buildBitVectorConcat(List<JavaExpression> javaExprs)
    {
        final JavaArrayType bvArrType = schema.getArrayType(
            schema.bitVectorType, 1);
        final JavaFunction concatMethod = types.bitVectorOpType.getMethod(
            "concat", new JavaType[] { bvArrType });
        final JavaFunctionInvocation concatCall = new JavaFunctionInvocation(
            new JavaFunctionReference(concatMethod));
        for (final JavaExpression expr : javaExprs)
        {
            concatCall.addArgument(exprConv.toUnsizedBitVector(expr));
        }
        return concatCall;
    }

    public void visit(VeraConditional obj)
    {
        // translate operands
        final List operands = obj.getOperands();
        assert (operands.size() == 3);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final VeraExpression veraOp3 = (VeraExpression) operands.get(2);
        final JavaExpression op1 = exprConv.toBoolean(translateNestedExpr(
            veraOp1, "cond", schema.booleanType), false, false);
        final ConvertedExpression op2Info = translateNewExpr(veraOp2);
        final ConvertedExpression op3Info = translateNewExpr(veraOp3);

        // convert types of conditional choices
        JavaExpression op2 = op2Info.getResultExpr();
        JavaExpression op3 = op3Info.getResultExpr();
        final JavaType type2 = op2.getResultType();
        final JavaType type3 = op3.getResultType();
        final JavaType resultType;
        if (type2.equals(type3))
        {
            // simple case: types match exactly
            resultType = type2;
        }
        else
        {
            if (schema.isBitVector(type2) || schema.isBitVector(type3))
            {
                // promote to BitVector
                op2 = exprConv.toUnsizedBitVector(op2);
                op3 = exprConv.toUnsizedBitVector(op3);
                JavaType newType2 = op2.getResultType();
                JavaType newType3 = op3.getResultType();
                int size2 = schema.getBitVectorSize(newType2);
                int size3 = schema.getBitVectorSize(newType3);
                resultType = size3 > size2 ? newType3 : newType2;
            }
            else if ((type2 == types.stringType || type2 instanceof JavaNullType)
                && (type3 == types.stringType || type3 instanceof JavaNullType))
            {
                // both String or null
                resultType = schema.getStringType();
            }
            else if ((exprConv.isString(type2) || type2 instanceof JavaNullType)
                && (exprConv.isString(type3) || type3 instanceof JavaNullType))
            {
                // convert to JunoString
                op2 = exprConv.toJunoString(op2);
                op3 = exprConv.toJunoString(op3);
                resultType = types.junoStringType;
            }
            else if (type2 instanceof JavaPrimitiveType
                && type3 instanceof JavaPrimitiveType)
            {
                if (desiredResultType instanceof JavaBooleanType
                    && (type2 instanceof JavaBooleanType || type3 instanceof JavaBooleanType))
                {
                    // convert to boolean
                    op2 = exprConv.toBoolean(op2, false, false);
                    op3 = exprConv.toBoolean(op3, false, false);
                    resultType = schema.booleanType;
                }
                else
                {
                    // promote to common primitive type
                    resultType = schema.promote(type2, type3);
                    op2 = exprConv.toType(resultType, op2);
                    op3 = exprConv.toType(resultType, op3);
                }
            }
            else if (exprConv.hasIntConversion(type2)
                && exprConv.hasIntConversion(type3))
            {
                // convert to Integer
                op2 = exprConv.toInteger(op2);
                op3 = exprConv.toInteger(op3);
                resultType = schema.integerWrapperType;
            }
            else if (type2 instanceof JavaNullType
                && type3 instanceof JavaStructuredType)
            {
                // second operand is null; use type of third
                resultType = type3;
            }
            else if (type3 instanceof JavaNullType
                && type2 instanceof JavaStructuredType)
            {
                // third operand is null; use type of second
                resultType = type3;
            }
            else
            {
                throw new TypeConversionException(
                    "Incompatible types in conditional: "
                        + type2.toDebugString() + ", " + type3.toDebugString());
            }
        }

        // create Java conditional operation
        final JavaExpression resultExpr;
        if (op2Info.hasInitExprs() || op2Info.hasUpdateMembers()
            || op3Info.hasInitExprs() || op3Info.hasUpdateMembers())
        {
            JavaLocalVariable resultVar = result.addTempVar("result",
                resultType);
            JavaVariableReference resultVarRef = new JavaVariableReference(
                resultVar);
            JavaBlock thenBlock = assignCondResult(resultType, resultVarRef,
                op2, op2Info);
            JavaBlock elseBlock = assignCondResult(resultType, resultVarRef,
                op3, op3Info);
            JavaIfStatement ifStmt = new JavaIfStatement(op1, thenBlock,
                elseBlock);
            result.addInitMember(ifStmt);
            resultExpr = resultVarRef;
        }
        else
        {
            resultExpr = new JavaConditional(op1, op2, op3);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    private JavaBlock assignCondResult(
        JavaType resultType,
        JavaVariableReference resultVarRef,
        JavaExpression resultExpr,
        ConvertedExpression exprInfo)
    {
        JavaBlock block = new JavaBlock(schema);
        block.addMembers(exprInfo.getInitMembers());
        if (needsClone(resultType))
        {
            resultExpr = getCloneExpr(resultExpr);
        }
        JavaAssign assignExpr = new JavaAssign(schema, resultVarRef, resultExpr);
        block.addMember(new JavaExpressionStatement(assignExpr));
        block.addMembers(exprInfo.getUpdateMembers());
        return block;
    }

    public void visit(VeraConstraintSet obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraCopyCreation obj)
    {
        // translate class type
        final VeraClass veraType = (VeraClass) obj.getType();
        final JavaRawAbstractClass type = (JavaRawAbstractClass) translateType(veraType);

        // translate source object expression
        final VeraExpression veraSrcExpr = obj.getSource();
        final JavaExpression srcExpr = translateNestedExpr(veraSrcExpr, "src");
        final JavaRawAbstractClass srcCls = (JavaRawAbstractClass) srcExpr
            .getResultType();
        assert (types.junoObjectType.isSuperclassOf(srcCls));

        // Vera: Foo x = new y;
        // Java: Foo x = (Foo) y.clone();
        final JavaExpression resultExpr = ExpressionBuilder.checkDowncast(
            ExpressionBuilder.memberCall(srcExpr, "clone"), type);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraDepthAccess obj)
    {
        final JavaExpression signalExpr = translateNestedExpr(obj.getSignal(),
            types.inputSignalType);
        final JavaExpression resultExpr = checkSample(signalExpr, obj
            .getDepth());
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraDistSet obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraDivide obj)
    {
        doBinaryArithmetic(obj, OP_DIVIDE);
    }

    public void visit(VeraEnumValueReference obj)
    {
        final VeraEnumerationElement veraElem = obj.getElement();
        final JavaMemberVariable var = translateEnumElement(veraElem);
        final JavaExpression resultExpr = new JavaVariableReference(var);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraEqual obj)
    {
        doEquality(obj, OP_EQ);
    }

    public void visit(VeraExactEqual obj)
    {
        doEquality(obj, OP_EXACT_EQ);
    }

    public void visit(VeraExactNotEqual obj)
    {
        doEquality(obj, OP_EXACT_NE);
    }

    public void visit(VeraFunctionInvocation obj)
    {
        if (!obj.getConstraints().isEmpty())
        {
            throw new UnsupportedOperationException(
                "with-constraints are not currently supported");
        }

        // determine Vera function and Java object expression (if any)
        final VeraExpression funcExpr = obj.getFunction();
        final VeraFunction veraFunc;
        final JavaExpression objExpr;
        if (funcExpr instanceof VeraFunctionReference)
        {
            final VeraFunctionReference funcRef = (VeraFunctionReference) funcExpr;
            veraFunc = funcRef.getFunction();
            objExpr = null;
        }
        else
        {
            assert (funcExpr instanceof VeraMemberAccess);
            final VeraMemberAccess memberExpr = (VeraMemberAccess) funcExpr;
            final StructuredTypeMember member = memberExpr.getMember();
            assert (member instanceof VeraMemberFunction);
            veraFunc = (VeraMemberFunction) member;
            final VeraExpression veraObjExpr = memberExpr.getObject();
            objExpr = translateNestedExpr(veraObjExpr, "obj");
        }

        if (builtinFuncMap.isBuiltinFunction(veraFunc))
        {
            BuiltinFunctionTranslator xlat = builtinFuncMap
                .getTranslator(veraFunc);
            if (xlat == null)
            {
                throw new UnsupportedOperationException(veraFunc.toString());
            }
            xlat.translate(this, veraFunc, objExpr, obj.getArguments());
        }
        else
        {
            final JavaFunctor functor = translateFunction(veraFunc);
            final JavaRawAbstractClass cls = (JavaRawAbstractClass) functor
                .getStructuredType();
            if (functor instanceof JavaFunction)
            {
                final JavaFunction func = (JavaFunction) functor;
                final String methodID = func.getName().getIdentifier();
                translateCall(veraFunc, cls, methodID, objExpr, obj
                    .getArguments());
            }
            else
            {
                assert (objExpr instanceof JavaSuperReference);
                translateCtorCall(veraFunc, cls, obj.getArguments());
            }
        }

        JavaExpression resultExpr = result.getResultExpr();
        if (resultExpr != null)
        {
            resultExpr.addAnnotations(obj.getAnnotations());
        }
    }

    void translateCall(
        VeraFunction veraFunc,
        JavaRawAbstractClass cls,
        String methodID,
        JavaExpression obj,
        List<VeraExpression> veraArgExprs)
    {
        VeraFunctionType veraFuncType = veraFunc.getType();

        // look for unique match on method ID and argument count first,
        // partly as an optimization, partly to handle built-in functions
        // that take a signal reference instead of sampling the signal
        JavaFunction method = null;
        if (!veraFuncType.isVarArgs())
        {
            int veraArgCount = veraArgExprs.size();
            method = findUniqueMethod(cls, methodID, veraArgCount);
        }
        if (method == null)
        {
            // determine Java argument types
            List<JavaType> javaArgTypes = new LinkedList<JavaType>();
            getArgTypes(veraFunc, veraArgExprs, javaArgTypes);

            // search for best Java method
            method = findMethod(cls, methodID, javaArgTypes);
        }

        // translate Vera argument expressions to Java expressions
        JavaFunctionType javaFuncType = method.getType();
        List<JavaExpression> javaArgExprs = new LinkedList<JavaExpression>();
        translateArgs(veraFuncType, veraArgExprs, javaFuncType, javaArgExprs);

        // generate Java function invocation expression
        JavaExpression funcExpr;
        if (obj != null)
        {
            funcExpr = new JavaMemberAccess(obj, method);
        }
        else
        {
            funcExpr = new JavaFunctionReference(method);
        }
        JavaFunctionInvocation callExpr = new JavaFunctionInvocation(funcExpr);
        for (JavaExpression argExpr : javaArgExprs)
        {
            callExpr.addArgument(argExpr);
        }
        result.setResultExpr(callExpr);
    }

    void translateCallAsStatic(
        VeraFunction veraFunc,
        JavaRawAbstractClass cls,
        String methodID,
        JavaExpression obj,
        List<VeraExpression> veraArgExprs)
    {
        // determine Java argument types
        List<JavaType> javaArgTypes = new LinkedList<JavaType>();
        javaArgTypes.add(obj.getResultType());
        getArgTypes(veraFunc, veraArgExprs, javaArgTypes);

        // search for best Java method
        JavaFunction method = findMethod(cls, methodID, javaArgTypes);

        // translate Vera argument expressions to Java expressions
        VeraFunctionType veraFuncType = veraFunc.getType();
        JavaFunctionType javaFuncType = method.getType();
        List<JavaExpression> javaArgExprs = new LinkedList<JavaExpression>();
        javaArgExprs.add(obj);
        translateArgs(veraFuncType, veraArgExprs, javaFuncType, javaArgExprs);

        // generate Java function invocation expression
        JavaExpression funcExpr = new JavaFunctionReference(method);
        JavaFunctionInvocation callExpr = new JavaFunctionInvocation(funcExpr);
        for (JavaExpression argExpr : javaArgExprs)
        {
            callExpr.addArgument(argExpr);
        }
        result.setResultExpr(callExpr);
    }

    private JavaFunction findUniqueMethod(
        JavaRawAbstractClass cls,
        String id,
        int argCount)
    {
        // search given class and base classes for a unique method
        // with the given ID and argument count
        JavaFunction result = null;
        JavaAbstractClass curClass = cls;
        while (curClass != null)
        {
            // search the current class for methods with the given ID
            Iterator iter = curClass.lookupObjects(id, JavaNameKind.METHOD);
            while (iter.hasNext())
            {
                JavaFunction method = (JavaFunction) iter.next();
                if (method.getType().getArguments().size() == argCount)
                {
                    // if we find multiple methods, return null
                    if (result != null) return null;

                    result = method;
                }
            }

            // next iteration searches base class
            curClass = curClass.getBaseClass();
        }
        return result;
    }

    void translateCtorCall(
        VeraFunction veraFunc,
        JavaRawAbstractClass cls,
        List<VeraExpression> veraArgExprs)
    {
        // determine Java argument types
        VeraFunctionType veraFuncType = veraFunc.getType();
        List<JavaType> javaArgTypes = new LinkedList<JavaType>();
        getArgTypes(veraFunc, veraArgExprs, javaArgTypes);

        // search for best Java constructor
        JavaConstructor ctor = findCtor(cls, javaArgTypes);

        // translate Vera argument expressions to Java expressions
        JavaFunctionType javaFuncType = ctor.getType();
        List<JavaExpression> javaArgExprs = new LinkedList<JavaExpression>();
        translateArgs(veraFuncType, veraArgExprs, javaFuncType, javaArgExprs);

        // constructor call must be first statement in block
        assert (!result.hasInitMembers());

        // generate Java constructor invocation expression
        JavaConstructorReference ctorRef = new JavaConstructorReference(ctor);
        JavaConstructorInvocation callExpr = new JavaConstructorInvocation(
            ctorRef);
        for (JavaExpression argExpr : javaArgExprs)
        {
            callExpr.addArgument(argExpr);
        }
        result.setResultExpr(callExpr);
    }

    private void getArgTypes(
        VeraFunction veraFunc,
        List<VeraExpression> veraArgExprs,
        List<JavaType> javaArgTypes)
    {
        final VeraFunctionType veraFuncType = veraFunc.getType();
        final Iterator<VeraExpression> veraExprIter = veraArgExprs.iterator();

        // translate types of non-varargs
        {
            final FunctionAnalysis baseFuncAnalysis = analyzer
                .getBaseFunctionAnalysis(veraFunc);
            final List<VeraFunctionArgument> veraArgs = veraFuncType
                .getArguments();
            final Iterator<VeraFunctionArgument> veraArgIter = veraArgs
                .iterator();
            int index = 0;
            while (veraArgIter.hasNext() && veraExprIter.hasNext())
            {
                VeraFunctionArgument veraArg = veraArgIter.next();
                VeraExpression veraExpr = veraExprIter.next();

                // get Java type from translated result type of Vera expression
                // NOTE: the type of the formal argument is not used, since the
                // expression type could yield a more optimal translation in the
                // case of an overloaded Java method
                VeraType veraExprType = veraExpr.getResultType();
                JavaType javaArgType = translateType(veraExprType,
                    isArgumentXZ(veraFunc, index, baseFuncAnalysis),
                    isArgumentStatefulString(veraFunc, index, baseFuncAnalysis));

                // wrap by-reference arguments in a reference pseudo-type object,
                // except for Vera arrays and strings, which are always passed by
                // reference in Java
                if (veraArg.isByRef() && !needsClone(javaArgType)
                    && veraExpr.isAssignable())
                {
                    javaArgType = new JavaRefType(javaArgType);
                }

                javaArgTypes.add(javaArgType);
                ++index;
            }
        }

        if (veraFuncType.isVarArgsByRef())
        {
            // by-ref var-args always translate specifically to final arguments
            // of Object[], Class[]
            javaArgTypes.add(types.objectArrayType);
            javaArgTypes.add(types.classArrayType);
        }
        else if (veraExprIter.hasNext())
        {
            assert (veraFuncType.isVarArgs());

            // translate types of varargs
            while (veraExprIter.hasNext())
            {
                VeraExpression veraExpr = veraExprIter.next();
                VeraType veraExprType = veraExpr.getResultType();
                JavaType javaArgType = translateType(veraExprType, true, false);
                javaArgTypes.add(javaArgType);
            }
        }
    }

    private JavaFunction findMethod(
        JavaRawAbstractClass cls,
        String id,
        List<JavaType> javaArgTypes)
    {
        // search given class and base classes for the best method
        JavaFunction bestMethod = null;
        int lowScore = Integer.MAX_VALUE;
        LinkedList<JavaFunction> rejected = new LinkedList<JavaFunction>();
        JavaAbstractClass curClass = cls;
        while (curClass != schema.getObjectType())
        {
            // search the current class for methods with the given ID
            Iterator iter = curClass.lookupObjects(id, JavaNameKind.METHOD);
            while (iter.hasNext())
            {
                JavaFunction method = (JavaFunction) iter.next();

                // ignore methods from java.lang.Object
                if (method.getStructuredType() == schema.getObjectType())
                    continue;

                int score = getArgMatchScore(method.getType(), javaArgTypes);
                if (score < 0)
                {
                    rejected.add(method);
                }
                else if (score < lowScore)
                {
                    bestMethod = method;
                    lowScore = score;
                }
            }

            // next iteration searches base class
            curClass = curClass.getBaseClass();
        }

        if (bestMethod == null)
        {
            // no matching method found
            JavaType[] argTypes = new JavaType[javaArgTypes.size()];
            javaArgTypes.toArray(argTypes);
            JavaFunction[] considered = new JavaFunction[rejected.size()];
            rejected.toArray(considered);
            throw new MethodNotFoundException(cls, id, argTypes, considered);
        }

        return bestMethod;
    }

    private JavaConstructor findCtor(
        JavaRawAbstractClass cls,
        List<JavaType> javaArgTypes)
    {
        // search given class for the best constructor
        JavaConstructor bestCtor = null;
        int lowScore = Integer.MAX_VALUE;
        LinkedList<JavaConstructor> rejected = new LinkedList<JavaConstructor>();
        Iterator iter = cls.getMembers().iterator();
        while (iter.hasNext())
        {
            Object member = iter.next();
            if (member instanceof JavaConstructor)
            {
                JavaConstructor ctor = (JavaConstructor) member;
                int score = getArgMatchScore(ctor.getType(), javaArgTypes);
                if (score < 0)
                {
                    rejected.add(ctor);
                }
                else if (score < lowScore)
                {
                    bestCtor = ctor;
                    lowScore = score;
                }
            }
        }

        if (bestCtor == null)
        {
            // no matching constructor found
            JavaType[] argTypes = new JavaType[javaArgTypes.size()];
            javaArgTypes.toArray(argTypes);
            JavaConstructor[] considered = new JavaConstructor[rejected.size()];
            rejected.toArray(considered);
            throw new ConstructorNotFoundException(cls, argTypes, considered);
        }

        return bestCtor;
    }

    private int getArgMatchScore(
        JavaFunctionType funcType,
        List<JavaType> actualArgTypes)
    {
        final int argTypeCount = actualArgTypes.size();
        final List<JavaFunctionArgument> formalArgs = funcType.getArguments();
        final int formalArgCount = formalArgs.size();

        // check for compatible argument count
        final boolean varArgs = funcType.isVarArgs();
        if (varArgs ? argTypeCount < formalArgCount - 1
            : argTypeCount != formalArgCount) return -1;

        // attempt to convert each actual argument to the type of
        // the formal argument
        int convCount = 0;
        Iterator<JavaFunctionArgument> formalArgIter = formalArgs.iterator();
        JavaType formalArgType = null;
        Iterator<JavaType> actualArgTypeIter = actualArgTypes.iterator();
        int argIndex = 0;
        while (actualArgTypeIter.hasNext())
        {
            JavaType actualArgType = actualArgTypeIter.next();
            if (formalArgIter.hasNext())
            {
                JavaFunctionArgument formalArg = formalArgIter.next();
                formalArgType = formalArg.getType();
                if (varArgs
                    && argIndex == formalArgCount - 1
                    && (argTypeCount > formalArgCount || getDimensions(actualArgType) == getDimensions(formalArgType) - 1))
                {
                    formalArgType = ((JavaArrayType) formalArgType)
                        .getAccessType(1);
                }
            }

            // handle by-reference arguments
            JavaType formalTargetType = formalArgType;
            JavaType actualTargetType = actualArgType;
            boolean byRef = false;
            if (actualArgType instanceof JavaRefType)
            {
                if (!(formalArgType instanceof JavaArrayType)) return -1;
                JavaArrayType formalArrayType = (JavaArrayType) formalArgType;
                if (formalArrayType.getIndexTypes().length != 1) return -1;
                formalTargetType = formalArrayType.getElementType();
                actualTargetType = ((JavaRefType) actualArgType).targetType;
                byRef = true;
            }

            if (!formalTargetType.isAssignableFrom(actualTargetType))
            {
                if (exprConv.hasConversion(actualTargetType, formalTargetType)
                    && (!byRef || exprConv.hasConversion(formalTargetType,
                        actualTargetType)))
                {
                    ++convCount;
                }
                else
                {
                    // no conversion available, give up
                    return -1;
                }
            }
            else if (formalTargetType instanceof JavaArrayType != actualTargetType instanceof JavaArrayType)
            {
                // do not allow array types to be passed in non-array arguments
                return -1;
            }
            ++argIndex;
        }
        return convCount;
    }

    private int getDimensions(JavaType type)
    {
        if (type instanceof JavaArrayType)
        {
            return ((JavaArrayType) type).getIndexTypes().length;
        }
        return 0;
    }

    private void translateArgs(
        VeraFunctionType veraFuncType,
        List<VeraExpression> veraArgExprs,
        JavaFunctionType javaFuncType,
        List<JavaExpression> javaArgExprs)
    {
        // translate declared arguments
        List<VeraFunctionArgument> veraArgs = veraFuncType.getArguments();
        Iterator<VeraFunctionArgument> veraArgIter = veraArgs.iterator();
        Iterator<VeraExpression> veraExprIter = veraArgExprs.iterator();
        List<JavaFunctionArgument> javaArgs = javaFuncType.getArguments();
        Iterator<JavaFunctionArgument> javaArgIter = javaArgs
            .listIterator(javaArgExprs.size());
        while (veraArgIter.hasNext() && veraExprIter.hasNext())
        {
            VeraFunctionArgument veraArg = veraArgIter.next();
            VeraExpression veraExpr = veraExprIter.next();
            JavaFunctionArgument javaArg = javaArgIter.next();
            JavaType javaArgType = javaArg.getType();

            JavaExpression javaExpr = null;
            if (veraArg.isByRef())
            {
                if (needsClone(javaArgType))
                {
                    // translate expression normally
                    javaExpr = translateNestedExpr(veraExpr, "arg", javaArgType);
                }
                else if (javaArgType instanceof JavaArrayType)
                {
                    // use reference holder to pass by-reference
                    javaExpr = getRefHolderExprs(javaArg, javaArgType, veraExpr);
                }
                else
                {
                    // Vera argument is by-reference but chosen Java method does
                    // not use a reference holder; this case is used by built-in
                    // functions like assoc_index that have optional by-ref
                    // arguments; because translated methods are not overloaded,
                    // and overloaded built-in methods do not explicitly take
                    // array arguments, the check for a Java array argument is
                    // not ambiguous
                }
            }
            if (javaExpr == null)
            {
                // translate expression normally
                javaExpr = translateNestedExpr(veraExpr, "arg", javaArgType,
                    javaArgType, sampleAsync);
                JavaType javaExprType = javaExpr.getResultType();

                // convert/clone result if necessary
                javaExpr = convertRHS(javaExpr, javaExprType, javaArgType, true);
            }
            javaArgExprs.add(javaExpr);
        }

        // translate variable arguments
        if (veraFuncType.isVarArgsByRef())
        {
            // check that Java method expects additional Object[] argument
            assert (javaArgIter.hasNext());
            JavaFunctionArgument javaArg = javaArgIter.next();
            assert (javaArg.getType() == types.objectArrayType);

            // check that Java method expects additional Class[] argument
            assert (javaArgIter.hasNext());
            javaArg = javaArgIter.next();
            assert (javaArg.getType() == types.classArrayType);

            assert (!javaArgIter.hasNext());

            // create Object[] argument which will contain actual arguments
            JavaArrayInitializer argsInitExpr = new JavaArrayInitializer(
                types.objectArrayType);
            JavaLocalVariable argsVar = result.createTempFor("args",
                argsInitExpr, true);
            JavaVariableReference argsRef = new JavaVariableReference(argsVar);
            javaArgExprs.add(argsRef);

            // create Class[] argument which will contain argument types
            JavaArrayInitializer typesInitExpr = new JavaArrayInitializer(
                types.classArrayType);
            JavaLocalVariable typesVar = result.createTempFor("types",
                typesInitExpr, true);
            javaArgExprs.add(new JavaVariableReference(typesVar));

            // translate remaining actual arguments into array initializer
            int argIndex = 0;
            while (veraExprIter.hasNext())
            {
                VeraExpression veraExpr = veraExprIter.next();

                // add argument read expression to arguments initializer
                ConvertedExpression argContext = new ConvertedExpression(result);
                LHSTranslator argXlat = translateLHS(veraExpr, argContext,
                    null, true, true);
                JavaExpression argExpr = argXlat.getReadExpression();
                argsInitExpr.addElement(argExpr);

                // add argument type to types initializer
                JavaType argType = argExpr.getResultType();
                typesInitExpr.addElement(new JavaTypeLiteral(argType));

                // add write-back expression to list
                JavaArrayAccess argsAccess = new JavaArrayAccess(argsRef);
                argsAccess.addIndex(new JavaIntLiteral(schema, argIndex++));
                JavaExpression argValue = ExpressionBuilder.checkDowncast(
                    argsAccess, argType);
                argValue = exprConv.toType(argType, argValue);
                argXlat.getWriteExpression(argValue)
                    .mergeIntoResult(argContext);
                argContext.mergeIntoUpdate(result);
            }

            // add var-args variables as last init members
            result.addInitMember(argsVar);
            result.addInitMember(typesVar);
        }
        else if (veraExprIter.hasNext())
        {
            assert (veraFuncType.isVarArgs());

            if (javaFuncType.isVarArgs())
            {
                // translate remaining actual arguments into result array
                while (veraExprIter.hasNext())
                {
                    VeraExpression veraExpr = veraExprIter.next();
                    // add argument expression to arguments initializer
                    JavaExpression argExpr = translateNestedExpr(veraExpr,
                        "arg");
                    javaArgExprs.add(argExpr);
                }
            }
            else
            {
                // Java method should expect one additional array argument
                assert (javaArgIter.hasNext());
                JavaFunctionArgument javaArg = javaArgIter.next();
                JavaArrayType argArrayType = (JavaArrayType) javaArg.getType();
                JavaType argElemType = argArrayType.getElementType();

                assert (!javaArgIter.hasNext());

                // create array argument which will contain actual arguments
                JavaArrayInitializer argsInitExpr = new JavaArrayInitializer(
                    argArrayType);
                JavaArrayCreation newArrayExpr = new JavaArrayCreation(
                    argArrayType);
                newArrayExpr.setInitializer(argsInitExpr);
                javaArgExprs.add(newArrayExpr);

                // translate remaining actual arguments into array initializer
                while (veraExprIter.hasNext())
                {
                    VeraExpression veraExpr = veraExprIter.next();
                    // add argument expression to arguments initializer
                    JavaExpression argExpr = translateNestedExpr(veraExpr,
                        "arg");
                    argsInitExpr.addElement(exprConv.toType(argElemType,
                        argExpr));
                }
            }
        }
    }

    private JavaExpression getRefHolderExprs(
        JavaFunctionArgument javaArg,
        JavaType javaArgType,
        VeraExpression veraExpr)
    {
        // get type of argument array and its element type
        assert (javaArgType instanceof JavaArrayType);
        JavaArrayType argArrayType = (JavaArrayType) javaArgType;

        JavaExpression holderRef = null;

        // check whether actual argument is a variable reference that was
        // already translated to a holder variable assignable to Java argument
        if (veraExpr instanceof VeraVariableReference && varInfoMap != null)
        {
            final VeraVariableReference veraVarRef = (VeraVariableReference) veraExpr;
            final VeraVariable veraVar = veraVarRef.getVariable();
            final VarInfo info = varInfoMap.getInfo(veraVar);
            if (info != null && info.isHolderVar())
            {
                JavaVariable targetVar = info.getTargetVar();
                if (argArrayType.isAssignableFrom(targetVar.getType()))
                {
                    holderRef = info.getHolderReference();
                }
            }
        }

        if (holderRef == null)
        {
            // translate the actual argument
            JavaType argElemType = argArrayType.getAccessType(1);
            LHSTranslator lhsXlat = translateLHS(veraExpr, argElemType, true,
                true);
            JavaType refType = lhsXlat.getResultType();
            JavaExpression readExpr = lhsXlat.getReadExpression();

            // determine type of holder array to create
            JavaArrayType refArrayType = schema.getArrayType(refType, 1);
            JavaArrayType holderType;
            if (argArrayType.isAssignableFrom(refArrayType))
            {
                // array of actual argument type is assignable to formal
                // argument
                // type: use actual argument type
                // example: actual is Integer[], formal is Object[]
                holderType = refArrayType;
            }
            else
            {
                // array of actual argument type is not assignable to formal
                // argument: use formal argument type and convert expression
                // example: actual is int[], formal is Object[]
                readExpr = exprConv.toType(argElemType, readExpr);
                JavaType convRefType = readExpr.getResultType();
                holderType = schema.getArrayType(convRefType, 1);
            }

            // create expression to initialize reference holder
            JavaArrayInitializer initExpr = new JavaArrayInitializer(holderType);
            initExpr.addElement(readExpr);

            // create reference holder variable
            String argName = javaArg.getName().getIdentifier();
            holderRef = result.addTempFor(argName + "_holder", initExpr, true);

            // create expression to write back value in holder
            JavaArrayAccess holderAccess = new JavaArrayAccess(holderRef);
            holderAccess.addIndex(new JavaIntLiteral(schema, 0));
            JavaExpression valueExpr = exprConv.toType(refType, holderAccess);
            lhsXlat.getWriteExpression(valueExpr).mergeIntoUpdate(result);
        }

        return holderRef;
    }

    public void visit(VeraFunctionReference obj)
    {
        // must be converted in the context of an invocation
        assert false;
    }

    public void visit(VeraGreater obj)
    {
        doRelation(obj, OP_GT);
    }

    public void visit(VeraGreaterOrEqual obj)
    {
        doRelation(obj, OP_GE);
    }

    public void visit(VeraIfElseConstraint obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraImplicationConstraint obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraInSet obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraInstanceCreation obj)
    {
        // translate type
        final VeraComplexType veraType = obj.getType();
        final JavaRawClass cls = (JavaRawClass) translateType(veraType);

        JavaExpression resultExpr = null;
        final List<VeraExpression> veraArgExprs = obj.getArguments();

        if (veraType instanceof VeraClass)
        {
            // find the Vera new() task
            final VeraClass veraCls = (VeraClass) veraType;
            Iterator iter = veraCls.lookupObjects("new", VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                VeraMemberFunction veraFunc = (VeraMemberFunction) iter.next();
                assert (!iter.hasNext());

                // make sure constructor has been translated
                JavaConstructor ctor = translateConstructor(veraFunc, cls);

                // determine Java argument types
                final VeraFunctionType veraFuncType = veraFunc.getType();
                final List<JavaType> javaArgTypes = new LinkedList<JavaType>();
                getArgTypes(veraFunc, veraArgExprs, javaArgTypes);

                // determine whether instantiation should be translated to
                // static factory method call
                final FactoryCallBuilder fcb = factoryXlatMap
                    .getFactory(veraType.getName().getCanonicalName());
                if (fcb != null)
                {
                    // translate Vera argument expressions to Java expressions
                    final JavaFunctionType javaFuncType = ctor.getType();
                    final List<JavaExpression> javaArgExprs = new LinkedList<JavaExpression>();
                    translateArgs(veraFuncType, veraArgExprs, javaFuncType,
                        javaArgExprs);

                    // generate call to Java factory method
                    resultExpr = fcb.callFactory(cls, javaArgExprs,
                        containingType);
                }
                else
                {
                    // search for best Java constructor
                    ctor = findCtor(cls, javaArgTypes);

                    // translate Vera argument expressions to Java expressions
                    final JavaFunctionType javaFuncType = ctor.getType();
                    final List<JavaExpression> javaArgExprs = new LinkedList<JavaExpression>();
                    translateArgs(veraFuncType, veraArgExprs, javaFuncType,
                        javaArgExprs);

                    // create Java instance creation schema object
                    JavaInstanceCreation newExpr = new JavaInstanceCreation(
                        cls, ctor);
                    for (JavaExpression argExpr : javaArgExprs)
                    {
                        newExpr.addArgument(argExpr);
                    }
                    resultExpr = newExpr;
                }
            }
        }
        else
        {
            assert (veraType instanceof VeraPortType);
        }

        if (resultExpr == null)
        {
            // default constructor; no arguments
            assert (veraArgExprs.isEmpty());
            resultExpr = new JavaInstanceCreation(cls, null);
        }

        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraIntegerLiteral obj)
    {
        JavaExpression resultExpr;
        final int i = obj.getValue();
        if (desiredResultType instanceof JavaBooleanType)
        {
            resultExpr = new JavaBooleanLiteral(schema, i != 0);
        }
        else if (desiredResultType == schema.bitType)
        {
            resultExpr = exprConv.getBitExpr((i & 1) != 0 ? Bit.ONE : Bit.ZERO);
        }
        else
        {
            resultExpr = new JavaIntLiteral(schema, i);
            int promoteLen = getTypeWidthChecked(desiredResultType);
            if (promoteLen > 32)
            {
                resultExpr = exprConv.intToBitVector(resultExpr, promoteLen,
                    false);
            }
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraInterfaceReference obj)
    {
        // VeraInterfaceReference should only be used internally by the
        // VeraSchemaBuilder, to produce a VeraSignalReference
        assert false;
    }

    public void visit(VeraLeftShift obj)
    {
        doShift(obj, OP_SHIFT_LEFT);
    }

    public void visit(VeraLess obj)
    {
        doRelation(obj, OP_LT);
    }

    public void visit(VeraLessOrEqual obj)
    {
        doRelation(obj, OP_LE);
    }

    public void visit(VeraLogicalAnd obj)
    {
        // translate operands
        final List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = exprConv.toBoolean(translateNestedExpr(
            veraOp1, "test", schema.booleanType), false, false);
        result.setResultExpr(op1);
        final ConvertedExpression op2Info = translateNewExpr(veraOp2,
            schema.booleanType);
        final JavaExpression op2 = exprConv.toBoolean(op2Info.getResultExpr(),
            false, false);

        // create Java conditional-and operation
        final JavaExpression resultExpr;
        if (op2Info.hasInitExprs())
        {
            JavaVariableReference resultVarRef = result.toVarRef("test",
                schema.booleanType);
            JavaBlock thenBlock = assignCondResult(schema.booleanType,
                resultVarRef, op2, op2Info);
            JavaIfStatement ifStmt = new JavaIfStatement(resultVarRef,
                thenBlock, null);
            result.addInitMember(ifStmt);
            resultExpr = resultVarRef;
        }
        else
        {
            resultExpr = new JavaConditionalAnd(schema, op1, op2);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraLogicalNegative obj)
    {
        final List operands = obj.getOperands();
        assert (operands.size() == 1);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final JavaExpression op1 = translateNestedExpr(veraOp1,
            schema.booleanType);
        final JavaExpression resultExpr;
        if (op1.getResultType() instanceof JavaPrimitiveType)
        {
            JavaExpression booleanOp1 = exprConv.toBoolean(op1, false, false);
            resultExpr = getNotExpr(booleanOp1);

        }
        else
        {
            resultExpr = ExpressionBuilder.memberCall(exprConv
                .toLogicalBit(op1), "not");
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraLogicalOr obj)
    {
        // translate operands
        final List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = exprConv.toBoolean(translateNestedExpr(
            veraOp1, "test", schema.booleanType), false, false);
        result.setResultExpr(op1);
        final ConvertedExpression op2Info = translateNewExpr(veraOp2,
            schema.booleanType);
        final JavaExpression op2 = exprConv.toBoolean(op2Info.getResultExpr(),
            false, false);

        // create Java conditional-or operation
        final JavaExpression resultExpr;
        if (op2Info.hasInitExprs())
        {
            JavaVariableReference resultVarRef = result.toVarRef("test",
                schema.booleanType);
            JavaBlock thenBlock = assignCondResult(schema.booleanType,
                resultVarRef, op2, op2Info);
            JavaIfStatement ifStmt = new JavaIfStatement(
                getNotExpr(resultVarRef), thenBlock, null);
            result.addInitMember(ifStmt);
            resultExpr = resultVarRef;
        }
        else
        {
            resultExpr = new JavaConditionalOr(schema, op1, op2);
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraMemberAccess obj)
    {
        // translate object
        final VeraExpression veraObj = obj.getObject();
        final JavaExpression javaObj = translateNestedExpr(veraObj, "obj");

        // translate member
        final VeraStructuredTypeMember veraMember = obj.getMember();
        final JavaStructuredTypeMember member = translateMember(veraMember);

        // translate member access to static variable or function to a direct
        // variable or function reference to avoid compiler warnings
        JavaExpression resultExpr = null;
        if (member instanceof JavaMemberVariable)
        {
            JavaMemberVariable memberVar = (JavaMemberVariable) member;
            if (memberVar.hasModifier(JavaVariableModifier.STATIC))
            {
                resultExpr = new JavaVariableReference(memberVar);
            }
        }
        else if (member instanceof JavaFunction)
        {
            JavaFunction memberFunc = (JavaFunction) member;
            if (memberFunc.hasModifier(JavaFunctionModifier.STATIC))
            {
                resultExpr = new JavaFunctionReference(memberFunc);
            }
        }

        // create Java member access schema object
        if (resultExpr == null)
        {
            // check for special handling of protected access
            if (member.getVisibility() == JavaVisibility.PROTECTED)
            {
                // check for valid Vera protected access
                JavaRawAbstractClass thisCls = (JavaRawAbstractClass) containingType;
                while (thisCls.getStructuredType() != null)
                {
                    thisCls = (JavaRawAbstractClass) thisCls
                        .getStructuredType();
                }
                JavaRawAbstractClass memberCls = (JavaRawAbstractClass) member
                    .getStructuredType();
                assert (memberCls.isSuperclassOf(thisCls));

                // Vera allows protected access to members of classes not
                // involved in the implementation of this class; Java requires
                // the other class to be this class or a subclass of this class
                JavaRawAbstractClass objCls = (JavaRawAbstractClass) javaObj
                    .getResultType();
                if (!thisCls.isSuperclassOf(objCls))
                {
                    // change member visibility to public to allow access
                    if (member instanceof JavaMemberVariable)
                    {
                        ((JavaMemberVariable) member)
                            .setVisibility(JavaVisibility.PUBLIC);
                    }
                    else if (member instanceof JavaFunction)
                    {
                        ((JavaFunction) member)
                            .setVisibility(JavaVisibility.PUBLIC);
                    }
                }
            }
            resultExpr = new JavaMemberAccess(javaObj, member);
        }

        // translate signal references as sample call
        // unless signal result is desired
        if (!exprConv.isSignal(desiredResultType))
        {
            resultExpr = checkSample(resultExpr, -1);
        }

        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    private JavaExpression checkSample(JavaExpression expr, int depth)
    {
        // does expression refer to input signal?
        Type resultType = expr.getResultType();
        if (exprConv.isInputSignal(resultType))
        {
            // call appropriate sample method
            if (depth >= 0)
            {
                expr = ExpressionBuilder.memberCall(expr, sampleAsync
                    ? "sampleDepthAsync" : "sampleDepth", new JavaIntLiteral(
                    schema, depth));
            }
            else
            {
                expr = ExpressionBuilder.memberCall(expr, sampleAsync
                    ? "sampleAsync" : "sample");
            }
        }
        return expr;
    }

    public void visit(VeraModulo obj)
    {
        doBinaryArithmetic(obj, OP_MODULO);
    }

    public void visit(VeraMultiply obj)
    {
        doBinaryArithmetic(obj, OP_MULTIPLY);
    }

    public void visit(VeraNotAndReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_AND_NOT);
    }

    public void visit(VeraNotEqual obj)
    {
        doEquality(obj, OP_NE);
    }

    public void visit(VeraNotInSet obj)
    {
        // constraints are not handled by this class
        assert false;
    }

    public void visit(VeraNotOrReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_OR_NOT);
    }

    public void visit(VeraNotXorReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_XOR_NOT);
    }

    public void visit(VeraNullLiteral obj)
    {
        final JavaExpression resultExpr = new JavaNullLiteral(schema);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraOrReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_OR);
    }

    public void visit(VeraPostDecrement obj)
    {
        doIncDec(obj, OP_POST_DEC);
    }

    public void visit(VeraPostIncrement obj)
    {
        doIncDec(obj, OP_POST_INC);
    }

    public void visit(VeraPreDecrement obj)
    {
        doIncDec(obj, OP_PRE_DEC);
    }

    public void visit(VeraPreIncrement obj)
    {
        doIncDec(obj, OP_PRE_INC);
    }

    public void visit(VeraReplication obj)
    {
        final List operands = obj.getOperands();
        assert (operands.size() == 2);
        final VeraExpression veraOp1 = (VeraExpression) operands.get(0);
        final VeraExpression veraOp2 = (VeraExpression) operands.get(1);
        final JavaExpression op1 = exprConv.toInt(translateNestedIntExpr(
            veraOp1, "count"));
        final int veraBits = veraOp2.getResultType().getBitCount();
        final JavaType desiredResultType = schema.getBitVectorType(veraBits);
        final JavaExpression op2 = translateNestedExpr(veraOp2, "temp", null,
            desiredResultType, sampleAsync);
        final JavaExpression resultExpr;
        if (obj.getResultType() instanceof VeraStringType)
        {
            resultExpr = ExpressionBuilder.staticCall(types.stringOpType,
                "replicate", op1, exprConv.toJavaString(op2, true));
        }
        else
        {
            resultExpr = ExpressionBuilder.staticCall(types.bitVectorOpType,
                "replicate", op1, exprConv.toBitVector(op2, veraBits, false));
        }
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraRightShift obj)
    {
        doShift(obj, OP_SHIFT_RIGHT);
    }

    public void visit(VeraSignalReference obj)
    {
        // translate signal
        final VeraInterfaceSignal signal = obj.getSignal();
        final JavaMemberVariable signalVar = translateInterfaceSignal(signal);

        // create Java variable reference schema object
        JavaExpression resultExpr = new JavaVariableReference(signalVar);

        // translate signal references as sample call
        // unless signal result is desired
        if (!exprConv.isSignal(desiredResultType))
        {
            resultExpr = checkSample(resultExpr, -1);
        }

        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraStringLiteral obj)
    {
        final JavaExpression resultExpr = new JavaStringLiteral(schema, obj
            .getValue());
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraSubtract obj)
    {
        doBinaryArithmetic(obj, OP_SUBTRACT);
    }

    public void visit(VeraSuperReference obj)
    {
        // translate class type
        final VeraClass veraType = obj.getType();
        final JavaRawAbstractClass type = (JavaRawAbstractClass) translateType(veraType);

        // create Java 'super' reference schema object
        final JavaExpression resultExpr = new JavaSuperReference(type);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraSystemClockReference obj)
    {
        final JavaExpression resultExpr = new JavaVariableReference(
            types.junoType.getField("systemClock"));
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraThisReference obj)
    {
        // translate class type
        final VeraClass veraType = obj.getType();
        final JavaRawAbstractClass type = (JavaRawAbstractClass) translateType(veraType);

        // create Java 'this' reference schema object
        final JavaExpression resultExpr = new JavaThisReference(type);
        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    public void visit(VeraVariableReference obj)
    {
        // translate variable
        final VeraVariable veraVar = obj.getVariable();
        final JavaVariable var = translateVariable(veraVar);

        // build Java variable reference
        JavaExpression resultExpr = translateVarRef(veraVar, var, schema,
            varInfoMap, result.getScope());

        resultExpr.addAnnotations(obj.getAnnotations());
        result.setResultExpr(resultExpr);
    }

    static JavaExpression translateVarRef(
        final VeraVariable veraVar,
        final JavaVariable var,
        final JavaSchema schema,
        final VarInfoMap varInfoMap,
        final Scope localScope)
    {
        JavaExpression resultExpr = null;

        // handle remapped variable references (by-ref holder, fork vars)
        if (varInfoMap != null)
        {
            VarInfo info = varInfoMap.getInfo(veraVar);
            if (info != null)
            {
                resultExpr = info.getReference();
            }
        }

        if (resultExpr == null)
        {
            // check whether field reference is shadowed by local variable
            if (localScope != null
                && var instanceof JavaMemberVariable
                && VarBuilder.containsID(localScope, var.getName()
                    .getIdentifier(), JavaNameKind.EXPRESSION))
            {
                // create 'this.var' member access
                JavaMemberVariable memberVar = (JavaMemberVariable) var;
                JavaStructuredType memberType = memberVar.getStructuredType();
                resultExpr = new JavaMemberAccess(new JavaThisReference(
                    memberType), memberVar);
            }
            else
            {
                // create Java variable reference schema object
                resultExpr = new JavaVariableReference(var);
            }
        }

        return resultExpr;
    }

    public void visit(VeraVoidLiteral obj)
    {
        // 'void' can only appear as the RHS of a drive, and should be
        // checked for explicitly in those contexts
        throw new RuntimeException("Illegal use of 'void'");
    }

    public void visit(VeraWildEqual obj)
    {
        doEquality(obj, OP_WILD_EQ);
    }

    public void visit(VeraWildNotEqual obj)
    {
        doEquality(obj, OP_WILD_NE);
    }

    public void visit(VeraXorReduction obj)
    {
        doUnaryArithmetic(obj, OP_REDUCTIVE_XOR);
    }
}
