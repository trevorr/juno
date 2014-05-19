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

import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.vera.VeraExpression;

/**
 * LHS translator for scalar variable accesses.
 * 
 * @author Trevor Robinson
 */
final class SimpleLHSTranslator
    extends BaseLHSTranslator
{
    private final JavaType resultType;
    private final JavaExpression updateEvent;
    private final JavaExpression lhsOnceExpr;

    public SimpleLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext,
        JavaType desiredResultType,
        VeraExpression obj,
        boolean readAccess,
        boolean writeAccess)
    {
        super(exprXlat, exprContext);

        // translate Vera expression
        assert (obj.isAssignable());
        JavaExpression lhsExpr = translateExpr(obj, "lhs", desiredResultType);
        resultType = lhsExpr.getResultType();

        // check for wait_var update event
        updateEvent = getWaitVarEventRef(lhsExpr);

        // introduce temporary variables as necessary
        boolean multiAccess = (readAccess && writeAccess)
            || (updateEvent != null);
        lhsOnceExpr = EvalOnceExprBuilder.evalLHSExpr(lhsExpr, exprContext,
            "lhs", multiAccess);
    }

    public SimpleLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext,
        JavaExpression lhsExpr,
        boolean readAccess,
        boolean writeAccess)
    {
        super(exprXlat, exprContext);

        // store the expression result type
        resultType = lhsExpr.getResultType();

        // check for wait_var update event
        updateEvent = getWaitVarEventRef(lhsExpr);

        // introduce temporary variables as necessary
        boolean multiAccess = (readAccess && writeAccess)
            || (updateEvent != null);
        lhsOnceExpr = EvalOnceExprBuilder.evalLHSExpr(lhsExpr, exprContext,
            "lhs", multiAccess);
    }

    public JavaType getResultType()
    {
        return resultType;
    }

    public JavaExpression getUpdateEvent()
    {
        return updateEvent;
    }

    public JavaExpression getReadExpression()
    {
        return lhsOnceExpr;
    }

    public ConvertedExpression getWriteExpression(JavaExpression value)
    {
        ConvertedExpression result = new ConvertedExpression(exprContext);
        getAssignWriteExpression(result, resultType, lhsOnceExpr, value,
            updateEvent);
        return result;
    }
}
