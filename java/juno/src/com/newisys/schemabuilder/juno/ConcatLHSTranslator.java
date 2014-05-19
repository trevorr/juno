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

import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.VeraBitSliceAccess;
import com.newisys.langschema.vera.VeraBitVectorType;
import com.newisys.langschema.vera.VeraConcatenation;
import com.newisys.langschema.vera.VeraEnumeration;
import com.newisys.langschema.vera.VeraExpression;
import com.newisys.langschema.vera.VeraRange;
import com.newisys.langschema.vera.VeraType;

/**
 * LHS translator for the LHS concatenation operator.
 * 
 * @author Trevor Robinson
 */
final class ConcatLHSTranslator
    extends BaseLHSTranslator
{
    private final LHSTranslator[] fieldXlats;
    private final int[] lhsSizes;
    private final boolean dynamicSize;
    private final JavaType maxResultType;

    public ConcatLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext,
        VeraConcatenation obj,
        boolean readAccess,
        boolean writeAccess)
    {
        super(exprXlat, exprContext);

        final int operandCount = obj.getOperands().size();
        fieldXlats = new LHSTranslator[operandCount];
        lhsSizes = new int[operandCount];
        boolean foundDynamicSize = false;
        int maxLHSSize = 0;
        int curIndex = 0;
        final Iterator iter = obj.getOperands().iterator();
        while (iter.hasNext())
        {
            VeraExpression veraExpr = (VeraExpression) iter.next();

            // determine bit count of field on LHS
            int lhsSize;
            VeraType veraType = veraExpr.getResultType();
            if (veraType instanceof VeraEnumeration)
            {
                // enums are 0-bit on LHS and 32-bit on RHS
                lhsSize = 0;
            }
            else
            {
                assert (veraType.isStrictIntegral());

                // bit-slice accesses with non-constant indices yield a
                // dynamically-sized bit vector result
                if (veraExpr instanceof VeraBitSliceAccess)
                {
                    lhsSize = -1;
                    VeraBitSliceAccess bitSliceExpr = (VeraBitSliceAccess) veraExpr;
                    VeraRange range = bitSliceExpr.getRange();
                    VeraExpression highExpr = range.getFrom();
                    VeraExpression lowExpr = range.getTo();
                    if (highExpr.isConstant() && lowExpr.isConstant())
                    {
                        Integer high = VeraExpression.toInteger(highExpr
                            .evaluateConstant());
                        Integer low = VeraExpression.toInteger(lowExpr
                            .evaluateConstant());
                        if (high != null && low != null)
                        {
                            lhsSize = Math
                                .abs(high.intValue() - low.intValue()) + 1;
                        }
                    }
                }
                else
                {
                    lhsSize = veraType.getBitCount();
                }
            }
            lhsSizes[curIndex] = lhsSize;
            foundDynamicSize = (lhsSize < 0);
            maxLHSSize += (lhsSize >= 0) ? lhsSize : veraType.getBitCount();

            LHSTranslator fieldXlat = exprXlat.translateLHS(veraExpr, null,
                readAccess || (writeAccess && (lhsSize < 0)), writeAccess);
            fieldXlats[curIndex++] = fieldXlat;
        }
        dynamicSize = foundDynamicSize;
        // okay to have LHS (not RHS!) greater than max bit vector size;
        // we just want to limit RHS promotion
        maxLHSSize = Math.min(maxLHSSize, VeraBitVectorType.MAX_SIZE);
        maxResultType = schema.getBitVectorType(maxLHSSize);
    }

    public JavaType getResultType()
    {
        return maxResultType;
    }

    public JavaExpression getUpdateEvent()
    {
        return null;
    }

    public JavaExpression getReadExpression()
    {
        List<JavaExpression> javaExprs = new LinkedList<JavaExpression>();
        for (int i = 0; i < fieldXlats.length; ++i)
        {
            javaExprs.add(fieldXlats[i].getReadExpression());
        }
        return exprXlat.buildBitVectorConcat(javaExprs);
    }

    public ConvertedExpression getWriteExpression(JavaExpression value)
    {
        // ExpressionTranslator should have converted RHS to BitVector
        assert (schema.isBitVector(value.getResultType()));

        ConvertedExpression result = new ConvertedExpression(exprContext);

        // make sure RHS expression appears to be evaluated only once
        value = EvalOnceExprBuilder.evalConstExpr(value, result, "bv", true);
        result.setResultExpr(value);
        result.setOptionalResult(true);

        // if result is dynamically sized/indexed,
        // we need position and width variables
        JavaVariableReference posRef = null, widthRef = null;
        if (dynamicSize)
        {
            posRef = result.addTempFor("pos", new JavaIntLiteral(schema, 0),
                false);
            widthRef = new JavaVariableReference(result.addTempVar("width",
                schema.intType));
        }

        // generate field assignment statements
        int pos = 0;
        for (int i = fieldXlats.length - 1; i >= 0; --i)
        {
            // build RHS expression
            JavaExpression rhsExpr;
            boolean updatePos = false;
            LHSTranslator fieldXlat = fieldXlats[i];
            int lhsSize = lhsSizes[i];
            if (lhsSize == 0)
            {
                // enumeration variables become undefined and do not affect
                // the RHS position
                JavaEnum enumCls = (JavaEnum) fieldXlat.getResultType();
                rhsExpr = exprConv.getEnumUndefined(enumCls);
            }
            else
            {
                // build RHS high/low index expressions
                JavaExpression rhsHighExpr = null, rhsLowExpr = null;
                JavaExpression widthExpr = null;
                JavaType getBitsResultType = null;
                if (lhsSize > 0)
                {
                    if (dynamicSize)
                    {
                        widthExpr = new JavaIntLiteral(schema, lhsSize);
                    }
                    else
                    {
                        rhsLowExpr = new JavaIntLiteral(schema, pos);
                        pos += lhsSize;
                        rhsHighExpr = new JavaIntLiteral(schema, pos - 1);
                    }
                    getBitsResultType = schema.getBitVectorType(lhsSize);
                }
                else
                {
                    assert (dynamicSize);
                    assert (fieldXlat instanceof BitSliceLHSTranslator);
                    BitSliceLHSTranslator bitSliceXlat = (BitSliceLHSTranslator) fieldXlat;
                    JavaExpression lhsRangeExpr = bitSliceXlat
                        .getRangeExpression();
                    if (lhsRangeExpr != null)
                    {
                        widthExpr = ExpressionBuilder.memberCall(lhsRangeExpr,
                            "length");
                    }
                    else
                    {
                        JavaExpression lhsHighExpr = exprConv
                            .toInt(bitSliceXlat.getHighExpression());
                        JavaExpression lhsLowExpr = exprConv.toInt(bitSliceXlat
                            .getLowExpression());
                        widthExpr = new JavaAdd(schema, ExpressionBuilder
                            .staticCall(types.mathType, "abs",
                                new JavaSubtract(schema, lhsHighExpr,
                                    lhsLowExpr)), new JavaIntLiteral(schema, 1));
                    }
                }
                if (dynamicSize)
                {
                    rhsLowExpr = posRef;
                    rhsHighExpr = new JavaSubtract(schema, new JavaAdd(schema,
                        posRef, new JavaAssign(schema, widthRef, widthExpr)),
                        new JavaIntLiteral(schema, 1));
                    updatePos = true;
                }

                // build RHS bit slice expression
                JavaFunctionInvocation callExpr = ExpressionBuilder.staticCall(
                    types.bitVectorOpType, "getBitsChecked",
                    new JavaExpression[] { value, rhsHighExpr, rhsLowExpr },
                    null);
                if (getBitsResultType != null)
                {
                    callExpr.setResultType(getBitsResultType);
                }
                rhsExpr = callExpr;

                // convert RHS if necessary
                JavaType lhsType = fieldXlat.getResultType();
                JavaType rhsType = rhsExpr.getResultType();
                if (needRHSConversion(lhsType, rhsType))
                {
                    rhsExpr = exprConv.toType(lhsType, rhsExpr);
                }
            }

            // generate statement to write to field
            fieldXlat.getWriteExpression(rhsExpr).mergeIntoInit(result);

            // update RHS position (if dynamically indexed)
            if (updatePos)
            {
                result.addInitExpr(new JavaAssignAdd(schema, posRef, widthRef));
            }
        }

        return result;
    }
}
