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

/**
 * Generic FactoryCallBuilder implementation that generates calls to a static
 * method of the given class called "create". If a given instance creation has
 * arguments, then the create method is expected to have the following
 * arguments: create(Object[], Class[], String). The first argument is an array
 * of argument values, the second is an array of argument declared types, and
 * the third is the default implementation name (passed to the constructor of
 * this class; possibly null). If the instance creation has no arguments, the
 * prototype is: create(String), where the argument is the implementation name
 * described previously.
 * 
 * @author Trevor Robinson
 */
public class DefaultFactoryCallBuilder
    implements FactoryCallBuilder
{
    private final JavaSchema schema;
    private final SchemaTypes types;
    private final ExpressionConverter exprConv;
    private final JavaRawAbstractClass factoryCls;
    private final String defaultImplName;

    public DefaultFactoryCallBuilder(
        JavaSchema schema,
        SchemaTypes types,
        ExpressionConverter exprConv,
        JavaRawClass factoryCls,
        String defaultImplName)
    {
        this.schema = schema;
        this.types = types;
        this.exprConv = exprConv;
        this.factoryCls = factoryCls;
        this.defaultImplName = defaultImplName;
    }

    protected List<JavaExpression> wrapArgs(
        List<JavaExpression> javaArgExprs,
        boolean includeTypes)
    {
        // translate (expr1, expr2...) -> (new Object[] { expr1, expr2... },
        // new Class[] { typeof(expr1).class, typeof(expr2).class... })
        List<JavaExpression> wrappedArgs = new LinkedList<JavaExpression>();

        Iterator iter = javaArgExprs.iterator();
        if (iter.hasNext())
        {
            // create Object[] argument which will contain actual arguments
            JavaArrayInitializer argsInitExpr = new JavaArrayInitializer(
                types.objectArrayType);
            JavaArrayCreation argsNewExpr = new JavaArrayCreation(
                types.objectArrayType);
            argsNewExpr.setInitializer(argsInitExpr);
            wrappedArgs.add(argsNewExpr);

            JavaArrayInitializer typesInitExpr = null;
            if (includeTypes)
            {
                // create Class[] argument which will contain argument types
                typesInitExpr = new JavaArrayInitializer(types.classArrayType);
                JavaArrayCreation typesNewExpr = new JavaArrayCreation(
                    types.classArrayType);
                typesNewExpr.setInitializer(typesInitExpr);
                wrappedArgs.add(typesNewExpr);
            }

            // translate actual arguments into array initializer(s)
            while (iter.hasNext())
            {
                JavaExpression argExpr = (JavaExpression) iter.next();

                // add actual argument to arguments initializer
                argExpr = exprConv.toObject(argExpr);
                argsInitExpr.addElement(argExpr);

                if (includeTypes)
                {
                    // add argument type to types initializer
                    JavaType argType = argExpr.getResultType();
                    typesInitExpr.addElement(new JavaTypeLiteral(argType));
                }
            }
        }

        return wrappedArgs;
    }

    public JavaExpression callFactory(
        JavaRawClass cls,
        List<JavaExpression> args,
        JavaStructuredType containingType)
    {
        // get factory method
        final JavaType[] argTypes;
        if (args.size() > 0)
        {
            argTypes = new JavaType[] { types.objectArrayType,
                types.classArrayType, types.stringType };
        }
        else
        {
            argTypes = new JavaType[] { types.stringType };
        }
        JavaFunction factoryMethod = factoryCls.getMethod("create", argTypes);

        // generate call to factory method
        JavaFunctionInvocation callExpr = new JavaFunctionInvocation(
            new JavaFunctionReference(factoryMethod));
        List<JavaExpression> wrappedArgs = wrapArgs(args, true);
        for (JavaExpression arg : wrappedArgs)
        {
            callExpr.addArgument(arg);
        }

        // add default implementation argument
        JavaExpression defImplArg;
        if (defaultImplName != null)
        {
            defImplArg = new JavaStringLiteral(schema, defaultImplName);
        }
        else
        {
            defImplArg = new JavaNullLiteral(schema);
        }
        callExpr.addArgument(defImplArg);

        return callExpr;
    }
}
