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

import com.newisys.langschema.Literal;
import com.newisys.langschema.java.JavaArrayAccess;
import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaMemberAccess;
import com.newisys.langschema.java.JavaStructuredTypeMember;
import com.newisys.langschema.java.JavaSuperReference;
import com.newisys.langschema.java.JavaThisReference;
import com.newisys.langschema.java.JavaVariableReference;

/**
 * Provides utility methods for obtaining a single-evaluation, multiple-use
 * expression from an arbitrary expression. For instance, assume that
 * <code>arr[++i]++</code> must be translated with a separate get and put
 * for the array. Obtaining a single-evaluation for <code>arr[++i]</code>
 * might emit a variable declaration <code>int arr_index = ++i;</code> and
 * return the expression <code>arr[arr_index]</code>. The resulting code
 * would be:
 * <pre>
 *     int arr_index = ++i;
 *     arr[arr_index] = arr[arr_index] + 1;
 * </pre>
 * 
 * @author Trevor Robinson
 */
final class EvalOnceExprBuilder
{
    private EvalOnceExprBuilder()
    {
        // do nothing
    }

    public static JavaExpression evalConstExpr(
        JavaExpression expr,
        ConvertedExpression exprContext,
        String tempID,
        boolean multiRef)
    {
        if (multiRef && !isSimpleExpression(expr))
        {
            // introduce temporary variable initialized to expression and
            // return a reference to it
            return exprContext.addTempFor(tempID, expr, true);
        }
        else
        {
            // temporary is not needed; return original expression
            return expr;
        }
    }

    public static JavaExpression evalLHSExpr(
        JavaExpression expr,
        ConvertedExpression exprContext,
        String tempID,
        boolean multiRef)
    {
        if (multiRef && !isSimpleExpression(expr))
        {
            if (expr instanceof JavaMemberAccess)
            {
                // extract information about member access
                JavaMemberAccess ma = (JavaMemberAccess) expr;
                JavaExpression objExpr = ma.getObject();
                JavaStructuredTypeMember member = ma.getMember();

                // introduce temporary variable initialized to member access
                // object expression
                JavaVariableReference objVarRef = exprContext.addTempFor(tempID
                    + "_obj", objExpr, true);

                // return member access that references temporary variable
                return new JavaMemberAccess(objVarRef, member);
            }
            else
            {
                assert (expr instanceof JavaArrayAccess);
                JavaArrayAccess aa = (JavaArrayAccess) expr;

                // introduce temporary for array expression, if necessary
                JavaExpression arrayExpr = aa.getArray();
                arrayExpr = evalConstExpr(arrayExpr, exprContext, tempID
                    + "_array", true);

                // create array access expression that references temporary
                JavaArrayAccess accessExpr = new JavaArrayAccess(arrayExpr);

                // introduce temporaries for index expressions, if necessary
                Iterator iter = aa.getIndices().iterator();
                int indexNo = 0;
                while (iter.hasNext())
                {
                    JavaExpression indexExpr = (JavaExpression) iter.next();
                    indexExpr = evalConstExpr(indexExpr, exprContext, tempID
                        + "_index" + indexNo++, true);
                    accessExpr.addIndex(indexExpr);
                }

                return accessExpr;
            }
        }
        else
        {
            // if temporary is not needed, return original expression
            return expr;
        }
    }

    public static boolean isSimpleExpression(JavaExpression expr)
    {
        return expr instanceof Literal || isSimpleRef(expr);
    }

    public static boolean isSimpleRef(JavaExpression expr)
    {
        // check for simple variable reference
        if (isSimpleVarRef(expr)) return true;

        // check for member access using simple variable reference
        if (expr instanceof JavaMemberAccess)
        {
            JavaMemberAccess ma = (JavaMemberAccess) expr;
            JavaExpression objExpr = ma.getObject();
            if (isSimpleVarRef(objExpr)) return true;
        }

        return false;
    }

    public static boolean isSimpleVarRef(JavaExpression expr)
    {
        return expr instanceof JavaVariableReference
            || expr instanceof JavaThisReference
            || expr instanceof JavaSuperReference;
    }
}
