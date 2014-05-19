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

import com.newisys.langschema.java.JavaAssign;
import com.newisys.langschema.java.JavaClassMember;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaFunction;
import com.newisys.langschema.java.JavaFunctionInvocation;
import com.newisys.langschema.java.JavaFunctionReference;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaVariableReference;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.VeraBitSliceAccess;
import com.newisys.langschema.vera.VeraDefineReference;
import com.newisys.langschema.vera.VeraExpression;
import com.newisys.langschema.vera.VeraRange;
import com.newisys.langschema.vera.VeraRangeDefine;

/**
 * LHS translator for bit vector slices.
 * 
 * @author Trevor Robinson
 */
final class BitSliceLHSTranslator
    extends BaseLHSTranslator
{
    private final JavaType resultType;
    private final boolean isDrive;
    private final JavaExpression updateEvent;
    private final JavaExpression signalOnceExpr;
    private final JavaExpression bvOnceExpr;
    private final JavaExpression rangeExpr;
    private final JavaExpression highOnceExpr;
    private final JavaExpression lowOnceExpr;

    public BitSliceLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext,
        VeraBitSliceAccess obj,
        boolean readAccess,
        boolean writeAccess)
    {
        super(exprXlat, exprContext);

        // convert the array expression
        final VeraExpression veraArrayExpr = obj.getArray();
        final JavaExpression arrayExpr = translateExpr(veraArrayExpr,
            "lhs_obj", writeAccess ? types.outputSignalType : null);
        final JavaType arrayType = arrayExpr.getResultType();
        isDrive = exprConv.isOutputSignal(arrayType);
        if (isDrive)
        {
            resultType = schema.bitVectorType;
        }
        else
        {
            assert (schema.isBitVector(arrayType));
            resultType = arrayType;
        }

        final VeraRange range = obj.getRange();

        // check for range define reference
        JavaExpression rangeExpr = null;
        final VeraDefineReference<VeraRangeDefine> defineRef = range
            .getDefineRef();
        if (defineRef != null)
        {
            final VeraRangeDefine define = defineRef.getDefine();
            final JavaClassMember member = translateRangeDefine(define);
            if (member != null)
            {
                if (member instanceof JavaMemberVariable)
                {
                    rangeExpr = new JavaVariableReference(
                        (JavaMemberVariable) member);
                }
                else
                {
                    rangeExpr = new JavaFunctionInvocation(
                        new JavaFunctionReference((JavaFunction) member));
                }
            }
        }
        this.rangeExpr = rangeExpr;

        // convert the high index expression
        final VeraExpression veraHighExpr = range.getFrom();
        final JavaExpression highExpr = exprConv.toInt(translateExpr(
            veraHighExpr, "lhs_high", schema.intType));

        // convert the low index expression
        final VeraExpression veraLowExpr = range.getTo();
        final JavaExpression lowExpr = exprConv.toInt(translateExpr(
            veraLowExpr, "lhs_low", schema.intType));

        // check for wait_var update event
        updateEvent = getWaitVarEventRef(arrayExpr);

        // create single-evaluation expressions for sub-expressions
        boolean multiAccess = (readAccess && writeAccess)
            || (updateEvent != null);
        if (isDrive)
        {
            signalOnceExpr = EvalOnceExprBuilder.evalLHSExpr(arrayExpr,
                exprContext, "lhs_signal", multiAccess);
            if (readAccess)
            {
                bvOnceExpr = EvalOnceExprBuilder.evalLHSExpr(ExpressionBuilder
                    .memberCall(signalOnceExpr, "sampleAsync"), exprContext,
                    "lhs_bv", true);
            }
            else
            {
                bvOnceExpr = null;
            }
        }
        else
        {
            signalOnceExpr = null;
            bvOnceExpr = EvalOnceExprBuilder.evalLHSExpr(arrayExpr,
                exprContext, "lhs_bv", multiAccess || writeAccess);
        }
        highOnceExpr = EvalOnceExprBuilder.evalConstExpr(highExpr, exprContext,
            "lhs_high", multiAccess);
        lowOnceExpr = EvalOnceExprBuilder.evalConstExpr(lowExpr, exprContext,
            "lhs_low", multiAccess);
    }

    private JavaExpression buildBitSliceGet(
        JavaExpression array,
        JavaExpression high,
        JavaExpression low)
    {
        // BitVector bit slice get
        // Vera: x = foo[4:2];
        // Java: x = BitVectorOp.getBits(foo, 4, 2);
        return ExpressionBuilder.staticCall(types.bitVectorOpType, "getBits",
            new JavaExpression[] { array, high, low }, null);
    }

    private JavaExpression buildBitSliceGet(
        JavaExpression array,
        JavaExpression range)
    {
        // BitVector bit slice get
        // Vera: x = foo[RANGE];
        // Java: x = foo.getBits(RANGE);
        return ExpressionBuilder.memberCall(array, "getBits", range);
    }

    private JavaExpression buildBitSliceSet(
        JavaExpression array,
        JavaExpression high,
        JavaExpression low,
        JavaExpression value)
    {
        // BitVector bit slice set
        // Vera: foo[4:2] = 3'b101;
        // Java: foo = BitVectorOp.setBits(foo, 4, 2, new BitVector("3'b101"));
        int size = schema.getBitVectorSize(array.getResultType());
        JavaExpression setExpr = ExpressionBuilder.staticCall(
            types.bitVectorOpType, "setBits", new JavaExpression[] { array,
                high, low, exprConv.toBitVector(value, size, false) }, null);
        return new JavaAssign(schema, array, setExpr);
    }

    private JavaExpression buildBitSliceSet(
        JavaExpression array,
        JavaExpression range,
        JavaExpression value)
    {
        // BitVector bit slice set
        // Vera: foo[RANGE] = 3'b101;
        // Java: foo = foo.setBits(RANGE, new BitVector("3'b101"));
        int size = schema.getBitVectorSize(array.getResultType());
        JavaExpression setExpr = ExpressionBuilder.memberCall(array, "setBits",
            range, exprConv.toBitVector(value, size, false));
        return new JavaAssign(schema, array, setExpr);
    }

    private JavaExpression buildBitSliceDrive(
        JavaExpression signal,
        JavaExpression high,
        JavaExpression low,
        JavaExpression value)
    {
        // BitVector bit slice set
        // Vera: foo[4:2] = 3'b101;
        // Java: foo.driveRange(4, 2, new BitVector("3'b101"));
        return ExpressionBuilder.memberCall(signal, "driveRange",
            new JavaExpression[] { high, low, exprConv.toObject(value) }, null);
    }

    private JavaExpression buildBitSliceDrive(
        JavaExpression signal,
        JavaExpression range,
        JavaExpression value)
    {
        // BitVector bit slice set
        // Vera: foo[RANGE] = 3'b101;
        // Java: foo.driveRange(RANGE, new BitVector("3'b101"));
        return ExpressionBuilder.memberCall(signal, "driveRange", range,
            exprConv.toObject(value));
    }

    public JavaType getResultType()
    {
        return resultType;
    }

    public JavaExpression getUpdateEvent()
    {
        return updateEvent;
    }

    public JavaExpression getRangeExpression()
    {
        return rangeExpr;
    }

    public JavaExpression getHighExpression()
    {
        return highOnceExpr;
    }

    public JavaExpression getLowExpression()
    {
        return lowOnceExpr;
    }

    public JavaExpression getReadExpression()
    {
        JavaExpression getExpr;
        if (rangeExpr != null)
        {
            getExpr = buildBitSliceGet(bvOnceExpr, rangeExpr);
        }
        else
        {
            getExpr = buildBitSliceGet(bvOnceExpr, highOnceExpr, lowOnceExpr);
        }
        return getExpr;
    }

    public ConvertedExpression getWriteExpression(JavaExpression value)
    {
        ConvertedExpression result = new ConvertedExpression(exprContext);
        if (isDrive)
        {
            JavaExpression driveExpr;
            if (rangeExpr != null)
            {
                driveExpr = buildBitSliceDrive(signalOnceExpr, rangeExpr, value);
            }
            else
            {
                driveExpr = buildBitSliceDrive(signalOnceExpr, highOnceExpr,
                    lowOnceExpr, value);
            }
            result.setResultExpr(driveExpr);
        }
        else
        {
            JavaExpression setExpr;
            if (rangeExpr != null)
            {
                setExpr = buildBitSliceSet(bvOnceExpr, rangeExpr, value);
            }
            else
            {
                setExpr = buildBitSliceSet(bvOnceExpr, highOnceExpr,
                    lowOnceExpr, value);
            }
            result.setResultExpr(setExpr);
            if (updateEvent != null)
            {
                JavaExpression oldValue = result.addTempFor("old_value",
                    bvOnceExpr, true);
                checkUpdate(result, oldValue, bvOnceExpr, false, updateEvent);
            }
        }
        return result;
    }
}
