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

package com.newisys.schemaanalyzer.juno;

import java.util.Iterator;

import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraCompilationUnitMember;
import com.newisys.langschema.vera.VeraGlobalFunction;
import com.newisys.langschema.vera.VeraSchemaMemberVisitor;

/**
 * Schema analyzer for top-level schema member objects (which includes only
 * compilation units).
 * 
 * @author Trevor Robinson
 */
final class SchemaMemberAnalyzer
    extends AnalyzerModule
    implements VeraSchemaMemberVisitor
{
    public SchemaMemberAnalyzer(VeraSchemaAnalyzer analyzer)
    {
        super(analyzer);
    }

    public void visit(VeraCompilationUnit obj)
    {
        CompUnitMemberAnalyzer cuma = new CompUnitMemberAnalyzer(analyzer, obj);
        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraCompilationUnitMember member = (VeraCompilationUnitMember) iter
                .next();
            member.accept(cuma);
        }
    }

    public void visit(VeraGlobalFunction obj)
    {
        // ignore built-in functions
    }
}
