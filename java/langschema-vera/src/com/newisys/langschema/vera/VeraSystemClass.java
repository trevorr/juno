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

/**
 * Base class for Vera built-in class types.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraSystemClass
    extends VeraClass
{
    private static final long serialVersionUID = -5215031529477437697L;

    protected VeraSystemClass(
        VeraSchema schema,
        VeraName name,
        VeraClass baseClass)
    {
        super(schema, name, baseClass);
    }

    protected VeraMemberFunction defineMemberFunction(
        String id,
        VeraType returnType)
    {
        VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, this);
        VeraFunctionType funcType = new VeraFunctionType(returnType, false);
        VeraMemberFunction func = new VeraMemberFunction(name, funcType);
        func.setVisibility(VeraVisibility.PUBLIC);
        addMember(func);
        return func;
    }

    protected VeraFunctionArgument addArgument(
        VeraFunction func,
        String id,
        VeraType type)
    {
        final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
        final VeraFunctionArgument arg = new VeraFunctionArgument(name, type,
            func);
        final VeraFunctionType funcType = func.getType();
        funcType.addArgument(arg);
        return arg;
    }

    protected VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        int defValue)
    {
        return addOptArgument(func, id, schema.integerType,
            new VeraIntegerLiteral(schema, defValue));
    }

    protected VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        VeraType type)
    {
        return addOptArgument(func, id, type, null);
    }

    protected VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        VeraType type,
        VeraExpression defValue)
    {
        final VeraFunctionArgument arg = addArgument(func, id, type);
        arg.setInitializer(defValue);
        arg.setOptional(true);
        arg.setOptionalLevel(1);
        return arg;
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
