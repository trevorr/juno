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

import com.newisys.dv.ifgen.schema.IfgenSchema;
import com.newisys.langschema.java.JavaFunction;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaSchemaObject;
import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.langschema.vera.VeraSchema;
import com.newisys.langschema.vera.VeraSchemaMember;
import com.newisys.langschema.vera.VeraSchemaObject;
import com.newisys.schemaanalyzer.juno.VeraSchemaAnalyzer;
import com.newisys.util.logging.IndentLogger;

/**
 * Primary facade for using the Juno translator.
 * 
 * @author Trevor Robinson
 */
public final class JunoSchemaBuilder
{
    private final VeraSchema veraSchema;
    private final JoveSchema javaSchema;
    private final IfgenSchema ifSchema;
    private final VeraSchemaAnalyzer analyzer;
    private final PackageNamer packageNamer;
    private final IndentLogger log;
    private final SchemaTypes types;
    private final ExpressionConverter exprConv;
    private final ManualTranslationMap manXlatMap;
    private final FactoryTranslationMap factoryXlatMap;
    private TranslatedObjectMap xlatObjMap;

    public JunoSchemaBuilder(
        VeraSchema veraSchema,
        JoveSchema javaSchema,
        IfgenSchema ifSchema,
        VeraSchemaAnalyzer analyzer,
        PackageNamer packageNamer,
        IndentLogger log)
    {
        this.veraSchema = veraSchema;
        this.javaSchema = javaSchema;
        this.ifSchema = ifSchema;
        this.analyzer = analyzer;
        this.packageNamer = packageNamer;
        this.log = log;
        this.types = new SchemaTypes(javaSchema);
        this.exprConv = new ExpressionConverter(javaSchema, types);
        this.manXlatMap = new ManualTranslationMap();
        this.factoryXlatMap = new FactoryTranslationMap();
    }

    public SchemaTypes getTypes()
    {
        return types;
    }

    public ExpressionConverter getExprConv()
    {
        return exprConv;
    }

    public void addTranslatedClass(String name, JavaRawAbstractClass cls)
    {
        manXlatMap.addClass(name, cls);
    }

    public void addTranslatedFunction(String name, JavaFunction func)
    {
        manXlatMap.addFunction(name, func);
    }

    public void addTranslatedVariable(String name, JavaVariable var)
    {
        manXlatMap.addVariable(name, var);
    }

    public void addFactory(String veraCls, FactoryCallBuilder fcb)
    {
        factoryXlatMap.addFactory(veraCls, fcb);
    }

    public void addFactory(
        String veraCls,
        JavaRawClass factoryCls,
        String defaultImplName)
    {
        addFactory(veraCls, new DefaultFactoryCallBuilder(javaSchema, types,
            exprConv, factoryCls, defaultImplName));
    }

    public JoveSchema build()
    {
        SchemaMemberTranslator xlat = new SchemaMemberTranslator(javaSchema,
            ifSchema, analyzer, packageNamer, log, types, exprConv, manXlatMap,
            factoryXlatMap);
        Iterator iter = veraSchema.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraSchemaMember obj = (VeraSchemaMember) iter.next();
            obj.accept(xlat);
        }

        xlatObjMap = xlat.xlatObjMap;
        return javaSchema;
    }

    public JavaSchemaObject getTranslatedObject(VeraSchemaObject veraObject)
    {
        return xlatObjMap.getJavaObject(veraObject);
    }
}
