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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NamedObject;
import com.newisys.langschema.java.*;

/**
 * Represents a complete translation of a Vera expression, which may result in
 * multiple Java statements.
 * 
 * @author Trevor Robinson
 */
final class ConvertedExpression
{
    final JavaSchema schema;
    private final TempBlockScope tempScope;

    private List<JavaBlockMember> initMembers;
    private boolean initExprs;
    private JavaExpression resultExpr;
    private boolean optionalResult;
    private List<JavaBlockMember> updateMembers;

    public ConvertedExpression(JavaSchema schema, TempBlockScope tempScope)
    {
        this.schema = schema;
        this.tempScope = tempScope;
    }

    public ConvertedExpression(ConvertedExpression other)
    {
        this.schema = other.schema;
        this.tempScope = other.tempScope;
    }

    public TempBlockScope getScope()
    {
        return tempScope;
    }

    public boolean isSimpleExpr()
    {
        return !hasInitExprs() && !hasUpdateMembers();
    }

    public boolean hasInitMembers()
    {
        return initMembers != null && !initMembers.isEmpty();
    }

    public List<JavaBlockMember> getInitMembers()
    {
        return initMembers != null ? initMembers : Collections.EMPTY_LIST;
    }

    public void addInitMember(JavaBlockMember member)
    {
        if (initMembers == null)
        {
            initMembers = new LinkedList<JavaBlockMember>();
        }
        initMembers.add(member);
        if (member instanceof NamedObject)
        {
            tempScope.addObject((NamedObject) member);
        }
        if (!isBlankVar(member)) initExprs = true;
    }

    private boolean isBlankVar(JavaBlockMember member)
    {
        return member instanceof JavaLocalVariable
            && ((JavaLocalVariable) member).getInitializer() == null;
    }

    public void addInitMembers(List<JavaBlockMember> members)
    {
        for (JavaBlockMember member : members)
        {
            addInitMember(member);
        }
    }

    public void addInitExpr(JavaExpression expr)
    {
        addInitMember(new JavaExpressionStatement(expr));
    }

    public JavaLocalVariable createTempVar(String id, JavaType type)
    {
        return VarBuilder.createLocalVar(tempScope, id, type);
    }

    public JavaLocalVariable addTempVar(String id, JavaType type)
    {
        JavaLocalVariable var = createTempVar(id, type);
        addInitMember(var);
        return var;
    }

    public JavaLocalVariable createTempFor(
        String id,
        JavaExpression expr,
        boolean makeFinal)
    {
        JavaType type = expr.getResultType();
        JavaLocalVariable var = createTempVar(id, type);
        var.setInitializer(expr);
        if (makeFinal)
        {
            var.addModifier(JavaVariableModifier.FINAL);
        }
        return var;
    }

    public JavaVariableReference addTempFor(
        String id,
        JavaExpression expr,
        boolean makeFinal)
    {
        JavaLocalVariable var = createTempFor(id, expr, makeFinal);
        addInitMember(var);
        return new JavaVariableReference(var);
    }

    public boolean hasInitExprs()
    {
        return initExprs;
    }

    public JavaExpression getResultExpr()
    {
        return resultExpr;
    }

    public void setResultExpr(JavaExpression expression)
    {
        this.resultExpr = expression;
    }

    public void convertResultExpr(JavaType type, ExpressionConverter exprConv)
    {
        resultExpr = exprConv.toType(type, resultExpr);
    }

    public boolean hasOptionalResult()
    {
        return optionalResult;
    }

    public void setOptionalResult(boolean optionalResult)
    {
        this.optionalResult = optionalResult;
    }

    public boolean hasUpdateMembers()
    {
        return updateMembers != null && !updateMembers.isEmpty();
    }

    public List<JavaBlockMember> getUpdateMembers()
    {
        return updateMembers != null ? updateMembers : Collections.EMPTY_LIST;
    }

    public void addUpdateMember(JavaBlockMember member)
    {
        if (updateMembers == null)
        {
            updateMembers = new LinkedList<JavaBlockMember>();
        }
        updateMembers.add(member);
    }

    public void addUpdateMembers(List<JavaBlockMember> members)
    {
        for (JavaBlockMember member : members)
        {
            addUpdateMember(member);
        }
    }

    public void addUpdateExpr(JavaExpression expr)
    {
        addUpdateMember(new JavaExpressionStatement(expr));
    }

    public JavaExpression mergeIntoResult(ConvertedExpression other)
    {
        if (initMembers != null)
        {
            other.addInitMembers(initMembers);
        }
        if (resultExpr != null)
        {
            other.setResultExpr(resultExpr);
        }
        if (updateMembers != null)
        {
            other.addUpdateMembers(updateMembers);
        }
        return resultExpr;
    }

    public void mergeIntoInit(ConvertedExpression other)
    {
        if (initMembers != null)
        {
            other.addInitMembers(initMembers);
        }
        if (resultExpr != null && !optionalResult)
        {
            other.addInitExpr(resultExpr);
        }
        if (updateMembers != null)
        {
            other.addUpdateMembers(updateMembers);
        }
    }

    public void mergeIntoUpdate(ConvertedExpression other)
    {
        if (initMembers != null)
        {
            other.addInitMembers(initMembers);
        }
        if (resultExpr != null && !optionalResult)
        {
            other.addUpdateExpr(resultExpr);
        }
        if (updateMembers != null)
        {
            other.addUpdateMembers(updateMembers);
        }
    }

    public JavaExpression flatten(String tempID)
    {
        flatten(tempID, false, null);
        return resultExpr;
    }

    public JavaExpression flattenEvalOnce(String tempID)
    {
        flatten(tempID, !EvalOnceExprBuilder.isSimpleExpression(resultExpr),
            null);
        return resultExpr;
    }

    private JavaBlockMember flatten(
        String tempID,
        boolean forceTemp,
        JavaType varType)
    {
        JavaBlockMember member = null;

        if (resultExpr != null)
        {
            JavaType resultType = resultExpr.getResultType();
            if (varType == null) varType = resultType;
            if (resultType == schema.voidType || tempID == null)
            {
                // if the expression is void (or no temporary is requested),
                // add the expression to the init members as a statement and
                // set the result expression to null
                if (!optionalResult && !(varType instanceof JavaNullType))
                {
                    // if the expression is not a valid statement expression,
                    // create a dummy variable assignment
                    if (!JavaExpressionStatement.isValidExpression(resultExpr))
                    {
                        JavaLocalVariable tempVar = VarBuilder.createLocalVar(
                            tempScope, "dummy", varType);
                        tempVar.setInitializer(resultExpr);
                        member = tempVar;
                    }
                    else
                    {
                        member = new JavaExpressionStatement(resultExpr);
                    }
                    addInitMember(member);
                }
                resultExpr = null;
                optionalResult = false;
            }
            else if (forceTemp || hasUpdateMembers())
            {
                // if we have update members, introduce a temporary variable
                // for the result expression; the result expression becomes a
                // reference to the temporary
                JavaLocalVariable tempVar = createTempVar(tempID, varType);
                tempVar.setInitializer(resultExpr);
                member = tempVar;
                addInitMember(member);
                resultExpr = new JavaVariableReference(tempVar);
            }
        }

        // move update members to init members
        if (hasUpdateMembers())
        {
            addInitMembers(updateMembers);
            updateMembers.clear();
        }

        return member;
    }

    public JavaVariableReference toVarRef(String tempID, JavaType varType)
    {
        if (!(resultExpr instanceof JavaVariableReference))
        {
            flatten(tempID, true, varType);
        }
        return (JavaVariableReference) resultExpr;
    }

    public JavaExpression toBlockExpr(JavaBlock block, String tempID)
    {
        // flatten result expression and update members into init members
        flatten(tempID, false, null);

        // inject init members into block
        injectInitMembers(block);

        return resultExpr;
    }

    public JavaExpression toEvalOnceExpr(JavaBlock block, String tempID)
    {
        // flatten result expression and update members into init members
        flatten(tempID, !EvalOnceExprBuilder.isSimpleExpression(resultExpr),
            null);

        // inject init members into block
        injectInitMembers(block);

        return resultExpr;
    }

    public JavaBlockMember toBlockStmt(JavaBlock block)
    {
        // flatten result expression and update members into init members
        JavaBlockMember member = flatten(null, false, null);
        assert (resultExpr == null);

        // inject init members into block
        if (member == null && hasInitMembers())
        {
            member = initMembers.get(0);
        }
        injectInitMembers(block);

        return member;
    }

    public void toLocalVar(JavaBlock block, JavaLocalVariable var)
    {
        // use result expression as variable initializer
        // and add variable to init members
        var.setInitializer(resultExpr);
        addInitMember(var);
        resultExpr = null;

        // move update members to init members
        if (hasUpdateMembers())
        {
            addInitMembers(updateMembers);
            updateMembers.clear();
        }

        // inject init members into block
        injectInitMembers(block);
    }

    private void injectInitMembers(JavaBlock block)
    {
        if (initMembers != null)
        {
            block.addMembers(initMembers);
            initMembers.clear();
        }
    }

    public JavaExpression toMemberInitExpr(
        JavaRawAbstractClass cls,
        JavaMemberVariable var)
    {
        // need to create initializer function if init/update members present
        if (hasInitMembers() || hasUpdateMembers())
        {
            // create private initializer function
            String memberID = var.getName().getIdentifier();
            String initID = buildID("getInit", memberID);
            JavaType memberType = var.getType();
            JavaFunctionType funcType = new JavaFunctionType(memberType);
            JavaFunction initFunc = new JavaFunction(initID, funcType);
            cls.addMember(initFunc);

            // make static if necessary
            if (var.hasModifier(JavaVariableModifier.STATIC))
            {
                initFunc.addModifier(JavaFunctionModifier.STATIC);
            }

            // create body for function
            JavaBlock body = new JavaBlock(schema);
            initFunc.setBody(body);

            // inject statements from converted expression into body
            toBlockExpr(body, "result");
            assert (resultExpr != null);

            // add return statement at end of body
            body.addMember(new JavaReturnStatement(schema, resultExpr));

            // return expression that invokes initializer function
            resultExpr = new JavaFunctionInvocation(new JavaFunctionReference(
                initFunc));
        }

        return resultExpr;
    }

    private static String buildID(String part1, String part2)
    {
        return part1 + Character.toUpperCase(part2.charAt(0))
            + part2.substring(1);
    }
}
