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

package com.newisys.schemaprinter.vera;

import java.util.Collection;
import java.util.Iterator;

import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraGlobalFunction;
import com.newisys.langschema.vera.VeraSchemaMember;
import com.newisys.langschema.vera.VeraSchemaMemberVisitor;
import com.newisys.util.text.TokenFormatter;

/**
 * Printer module used to print Vera schema members.
 * 
 * @author Trevor Robinson
 */
public class SchemaMemberPrinter
    extends VeraSchemaPrinterModule
    implements VeraSchemaMemberVisitor
{
    public SchemaMemberPrinter(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        super(fmt, config);
    }

    public void printMembers(Collection members)
    {
        boolean first = true;
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            VeraSchemaMember member = (VeraSchemaMember) iter.next();
            if (!first) printNewLine();
            member.accept(this);
            first = false;
        }
    }

    public void visit(VeraCompilationUnit obj)
    {
        CompilationUnitMemberPrinter cump = new CompilationUnitMemberPrinter(
            fmt, config);
        cump.printMembers(obj.getMembers());
    }

    public void visit(VeraGlobalFunction obj)
    {
        printGlobalFunc(obj);
    }
}
