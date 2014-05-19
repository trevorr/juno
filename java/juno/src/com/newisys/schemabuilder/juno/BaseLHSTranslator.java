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
import com.newisys.langschema.java.JavaCastExpression;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaNullType;
import com.newisys.langschema.java.JavaPrimitiveType;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.VeraExpression;

/**
 * Base class for LHS translators. Provides common state and utility methods.
 * 
 * @author Trevor Robinson
 */
abstract class BaseLHSTranslator
    extends TranslatorModule
    implements LHSTranslator
{
    protected final ExpressionTranslator exprXlat;
    protected final ConvertedExpression exprContext;

    public BaseLHSTranslator(
        ExpressionTranslator exprXlat,
        ConvertedExpression exprContext)
    {
        super(exprXlat);
        this.exprXlat = exprXlat;
        this.exprContext = exprContext;
    }

    protected final JavaExpression translateExpr(
        VeraExpression veraExpr,
        String tempID,
        JavaType typeContext)
    {
        ConvertedExpression convExpr = exprXlat.translateNewExpr(veraExpr,
            typeContext);
        convExpr.flatten(tempID);
        return convExpr.mergeIntoResult(exprContext);
    }

    protected final boolean hasValueSemantics(JavaType lhsType)
    {
        // JunoString has value semantics
        return lhsType == types.junoStringType;
    }

    protected final JavaExpression getValueWriteExpression(
        JavaType lhsType,
        JavaExpression destExpr,
        JavaExpression value)
    {
        // JunoString is the only assignable object type with value semantics
        assert (lhsType == types.junoStringType);

        // JunoString: str.assign(value)
        if (value.getResultType() instanceof JavaNullType)
        {
            // avoid ambiguous method invocation: str.assign((String) null)
            value = new JavaCastExpression(types.stringType, value);
        }
        return ExpressionBuilder.memberCall(destExpr, "assign", value);
    }

    protected final void getAssignWriteExpression(
        ConvertedExpression result,
        JavaType lhsType,
        JavaExpression lhs,
        JavaExpression value,
        JavaExpression updateEvent)
    {
        if (hasValueSemantics(lhsType))
        {
            result.setResultExpr(getValueWriteExpression(lhsType, lhs, value));
        }
        else
        {
            result.setResultExpr(new JavaAssign(schema, lhs, value));
            if (updateEvent != null)
            {
                JavaExpression oldValue = result.addTempFor("old_value", lhs,
                    true);
                checkUpdate(result, oldValue, lhs,
                    lhsType instanceof JavaPrimitiveType, updateEvent);
            }
        }
    }
}
