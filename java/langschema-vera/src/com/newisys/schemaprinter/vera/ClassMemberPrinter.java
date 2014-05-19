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

import com.newisys.langschema.vera.*;
import com.newisys.util.text.TokenFormatter;

/**
 * Printer module used to print Vera class members.
 * 
 * @author Trevor Robinson
 */
public class ClassMemberPrinter
    extends VeraSchemaPrinterModule
    implements VeraClassMemberVisitor
{
    public ClassMemberPrinter(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        super(fmt, config);
    }

    public void printMembers(Collection members)
    {
        boolean first = true;
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            VeraClassMember member = (VeraClassMember) iter.next();
            if (!first && !(member instanceof VeraMemberVariable))
            {
                printNewLine();
            }
            member.accept(this);
            first = false;
        }
    }

    public void visit(VeraClassConstraint obj)
    {
        fmt.printToken("constraint");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();
        printConstraints(obj.getExprs());
        printNewLine();
    }

    public void visit(VeraEnumeration obj)
    {
        printEnum(obj);
    }

    public void visit(VeraEnumerationElement obj)
    {
        // handled in visit(VeraEnumeration)
    }

    public void visit(VeraMemberFunction obj)
    {
        printVisibility(obj.getVisibility(), VeraVisibility.PUBLIC);
        printModifiers(obj.getModifiers());
        VeraFunctionType funcType = obj.getType();
        printFuncProlog(funcType);
        printID(obj.getName());
        printFuncArgs(funcType);
        if (obj.isPureVirtual())
        {
            fmt.printTrailingToken(";");
            printNewLine();
        }
        else
        {
            printBody(obj.getBody());
        }
    }

    public void visit(VeraMemberVariable obj)
    {
        printVisibility(obj.getVisibility(), VeraVisibility.PUBLIC);
        printVarDecl(obj);
        VeraExpression randomSize = obj.getRandomSize();
        if (randomSize != null)
        {
            fmt.printSpace();
            VeraType type = obj.getType();
            if (type instanceof VeraAssocArrayType)
            {
                fmt.printToken("assoc_size");
            }
            else
            {
                assert (type instanceof VeraDynamicArrayType);
                fmt.printToken("dynamic_size");
            }
            fmt.printSpace();
            printExpression(randomSize);
        }
        printVarInit(obj);
        fmt.printTrailingToken(";");
        printNewLine();
    }
}
