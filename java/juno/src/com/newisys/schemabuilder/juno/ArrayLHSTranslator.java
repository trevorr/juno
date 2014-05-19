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
import java.util.List;

import com.newisys.langschema.java.JavaAbstractClass;
import com.newisys.langschema.java.JavaArrayAccess;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaAssign;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.vera.VeraArrayAccess;
import com.newisys.langschema.vera.VeraExpression;

/**
 * LHS translator for array elements, including individual bit vector bits.
 * 
 * @author Trevor Robinson
 */
final class ArrayLHSTranslator
    extends BaseLHSTranslator
{
    private final JavaType arrayType;
    private final boolean isDrive;
    private final JavaType elementType;
    private final JavaExpression updateEvent;
    private final JavaExpression arrayOnceExpr;
    private final JavaExpression signalOnceExpr;
    private final JavaExpression objectOnceExpr;
    private final JavaExpression indexOnceExpr;

    public ArrayLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext,
        VeraArrayAccess obj,
        boolean readAccess,
        boolean writeAccess)
    {
        super(exprXlat, exprContext);

        // translate the array expression
        final VeraExpression veraArrayExpr = obj.getArray();
        final JavaExpression arrayExpr = translateExpr(veraArrayExpr,
            "lhs_obj", writeAccess ? types.outputSignalType : null);
        arrayType = arrayExpr.getResultType();
        isDrive = exprConv.isOutputSignal(arrayType);
        final boolean bitVector = isDrive || schema.isBitVector(arrayType);

        // translate the indices
        final List veraIndices = obj.getIndices();
        final int indexCount = veraIndices.size();
        final JavaExpression[] indexExprs = new JavaExpression[indexCount];
        JavaExpression updateEvent;
        if (arrayType instanceof JavaArrayType)
        {
            JavaArrayType realArrayType = (JavaArrayType) arrayType;
            elementType = realArrayType.getElementType();

            // native Java array: translate multiple int indexes
            int curIndex = 0;
            final Iterator iter = veraIndices.iterator();
            while (iter.hasNext())
            {
                VeraExpression veraIndexExpr = (VeraExpression) iter.next();
                JavaExpression indexExpr = exprConv.toInt(translateExpr(
                    veraIndexExpr, "lhs_index" + curIndex, schema.intType));
                indexExprs[curIndex++] = indexExpr;
            }
        }
        else
        {
            // BitVector bit access or associative array access: single
            // index
            assert (indexCount == 1);
            final VeraExpression veraIndexExpr = (VeraExpression) veraIndices
                .get(0);
            if (bitVector)
            {
                elementType = schema.bitType;

                // BitVector bit access: int index
                indexExprs[0] = exprConv.toInt(translateExpr(veraIndexExpr,
                    "lhs_index", schema.intType));
            }
            else
            {
                // associative array access: BitVector or String index
                assert (arrayType instanceof JoveAssocArrayType);
                final JoveAssocArrayType assocType = (JoveAssocArrayType) arrayType;
                elementType = assocType.getElementType();

                JavaExpression indexExpr = translateExpr(veraIndexExpr,
                    "lhs_index", null);
                final JavaAbstractClass baseClass = assocType.getBaseClass();
                if (types.bitAssocArrayType.isSuperclassOf(baseClass))
                {
                    // BitVector-indexed associative array
                    indexExpr = exprConv.toUnsizedBitVector(indexExpr);
                }
                else
                {
                    // String-indexed associative array
                    assert (types.stringAssocArrayType
                        .isSuperclassOf(baseClass));
                    indexExpr = exprConv.toJavaString(indexExpr, false);
                }
                indexExprs[0] = indexExpr;
            }
        }

        // check for wait_var update event
        updateEvent = getWaitVarEventRef(arrayExpr);

        // check the Java type of the array
        boolean multiAccess = (readAccess && writeAccess)
            || (updateEvent != null);
        if (arrayType instanceof JavaArrayType)
        {
            // native Java array
            JavaExpression lhs = buildNativeArrayAccess(arrayExpr, indexExprs);
            arrayOnceExpr = EvalOnceExprBuilder.evalLHSExpr(lhs, exprContext,
                "lhs", multiAccess);
            signalOnceExpr = null;
            objectOnceExpr = null;
            indexOnceExpr = null;
            if (updateEvent != null)
            {
                updateEvent = buildNativeArrayAccess(updateEvent, indexExprs);
            }
        }
        else
        {
            // associative array or bit vector
            arrayOnceExpr = null;
            if (isDrive)
            {
                signalOnceExpr = EvalOnceExprBuilder.evalLHSExpr(arrayExpr,
                    exprContext, "lhs_signal", multiAccess);
                if (readAccess)
                {
                    objectOnceExpr = EvalOnceExprBuilder.evalLHSExpr(
                        ExpressionBuilder.memberCall(signalOnceExpr,
                            "sampleAsync"), exprContext, "lhs_bv", true);
                }
                else
                {
                    objectOnceExpr = null;
                }
            }
            else
            {
                signalOnceExpr = null;
                objectOnceExpr = EvalOnceExprBuilder.evalLHSExpr(arrayExpr,
                    exprContext, "lhs_obj", multiAccess
                        || (writeAccess && bitVector));
            }
            indexOnceExpr = EvalOnceExprBuilder.evalConstExpr(indexExprs[0],
                exprContext, "lhs_index", multiAccess);
            if (updateEvent != null && !bitVector)
            {
                updateEvent = buildAssocArrayGet(updateEvent,
                    types.junoEventType, indexOnceExpr, true);
            }
        }
        this.updateEvent = updateEvent;
    }

    private JavaExpression buildNativeArrayAccess(
        JavaExpression array,
        JavaExpression[] indices)
    {
        // normal Java array
        final JavaArrayAccess access = new JavaArrayAccess(array);
        for (int i = 0; i < indices.length; ++i)
        {
            access.addIndex(indices[i]);
        }
        return access;
    }

    private JavaExpression buildAssocArrayGet(
        JavaExpression array,
        JavaType elementType,
        JavaExpression index,
        boolean forWrite)
    {
        // associative array get
        // Vera: x = foo[2];
        // Java: x = (Integer) foo.get(new BitVector(32, 2));
        return ExpressionBuilder.checkDowncast(ExpressionBuilder.memberCall(
            array, forWrite ? "getOrCreate" : "get", index), elementType);
    }

    private JavaExpression buildAssocArraySet(
        JavaExpression array,
        JavaType elementType,
        JavaExpression index,
        JavaExpression value)
    {
        // associative array set
        // Vera: foo[2] = 42;
        // Java: foo.put(new BitVector(32, 2), new Integer(42));
        return ExpressionBuilder.memberCall(array, "put", index, exprConv
            .toObject(value));
    }

    private JavaExpression buildBitGet(
        JavaExpression array,
        JavaExpression index)
    {
        // BitVector bit get
        // Vera: x = foo[2];
        // Java: x = foo.getBit(2);
        return ExpressionBuilder.memberCall(array, "getBit", index);
    }

    private JavaExpression buildBitSet(
        JavaExpression array,
        JavaExpression index,
        JavaExpression value)
    {
        // BitVector bit set
        // Vera: foo[2] = 1'b1;
        // Java: foo = foo.setBit(2, Bit.ONE);
        JavaExpression setExpr = ExpressionBuilder.memberCall(array, "setBit",
            index, exprConv.toBit(value));
        return new JavaAssign(schema, array, setExpr);
    }

    private JavaExpression buildBitDrive(
        JavaExpression signal,
        JavaExpression index,
        JavaExpression value)
    {
        // BitVector bit slice set
        // Vera: foo[2] = 1'b1;
        // Java: foo.driveRange(2, 2, new BitVector("1'b1"));
        return ExpressionBuilder.memberCall(signal, "driveRange",
            new JavaExpression[] { index, index, exprConv.toObject(value) },
            null);
    }

    public JavaType getResultType()
    {
        return elementType;
    }

    public JavaExpression getUpdateEvent()
    {
        return updateEvent;
    }

    public JavaExpression getReadExpression()
    {
        if (arrayOnceExpr != null)
        {
            return arrayOnceExpr;
        }
        else
        {
            if (schema.isBitVector(arrayType))
            {
                return buildBitGet(objectOnceExpr, indexOnceExpr);
            }
            else
            {
                return buildAssocArrayGet(objectOnceExpr, elementType,
                    indexOnceExpr, false);
            }
        }
    }

    public ConvertedExpression getWriteExpression(JavaExpression value)
    {
        ConvertedExpression result = new ConvertedExpression(exprContext);
        if (arrayOnceExpr != null)
        {
            getAssignWriteExpression(result, elementType, arrayOnceExpr, value,
                updateEvent);
        }
        else if (isDrive)
        {
            result.setResultExpr(buildBitDrive(signalOnceExpr, indexOnceExpr,
                value));
        }
        else if (schema.isBitVector(arrayType))
        {
            if (updateEvent != null)
            {
                value = EvalOnceExprBuilder.evalConstExpr(value, result,
                    "new_value", true);
            }
            result.setResultExpr(buildBitSet(objectOnceExpr, indexOnceExpr,
                value));
            if (updateEvent != null)
            {
                JavaExpression oldValue = result.addTempFor("old_value",
                    buildBitGet(objectOnceExpr, indexOnceExpr), true);
                checkUpdate(result, oldValue, value, false, updateEvent);
            }
        }
        else if (hasValueSemantics(elementType))
        {
            JavaExpression destExpr = buildAssocArrayGet(objectOnceExpr,
                elementType, indexOnceExpr, true);
            result.setResultExpr(getValueWriteExpression(elementType, destExpr,
                value));
        }
        else
        {
            if (updateEvent != null)
            {
                value = EvalOnceExprBuilder.evalConstExpr(value, result,
                    "new_value", true);
            }
            result.setResultExpr(buildAssocArraySet(objectOnceExpr,
                elementType, indexOnceExpr, value));
            if (updateEvent != null)
            {
                JavaExpression oldValue = result.addTempFor("old_value",
                    buildAssocArrayGet(objectOnceExpr, elementType,
                        indexOnceExpr, false), true);
                checkUpdate(result, oldValue, value, false, updateEvent);
            }
        }
        return result;
    }
}
