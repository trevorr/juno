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

import com.newisys.dv.ifgen.schema.IfgenSchema;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraGlobalFunction;
import com.newisys.langschema.vera.VeraSchemaMemberVisitor;
import com.newisys.schemaanalyzer.juno.VeraSchemaAnalyzer;
import com.newisys.util.logging.IndentLogger;

/**
 * Schema translator for top-level schema members (which includes only
 * compilation units).
 * 
 * @author Trevor Robinson
 */
final class SchemaMemberTranslator
    extends TranslatorModule
    implements VeraSchemaMemberVisitor
{
    public SchemaMemberTranslator(
        JoveSchema schema,
        IfgenSchema ifSchema,
        VeraSchemaAnalyzer analyzer,
        PackageNamer packageNamer,
        IndentLogger log,
        SchemaTypes types,
        ExpressionConverter exprConv,
        ManualTranslationMap manXlatMap,
        FactoryTranslationMap factoryXlatMap)
    {
        super(schema, ifSchema, analyzer, packageNamer, log, types, exprConv,
            manXlatMap, factoryXlatMap);
    }

    public void visit(VeraCompilationUnit obj)
    {
        translateCompUnit(obj);
    }

    public void visit(VeraGlobalFunction obj)
    {
        // ignore built-in functions
    }
}
