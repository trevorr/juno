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
import java.util.List;

import com.newisys.langschema.vera.*;
import com.newisys.util.text.StringTokenFormatter;
import com.newisys.util.text.TokenFormatter;

/**
 * Printer module used to print Vera compilation unit members.
 * 
 * @author Trevor Robinson
 */
public class CompilationUnitMemberPrinter
    extends VeraSchemaPrinterModule
    implements VeraCompilationUnitMemberVisitor
{
    public CompilationUnitMemberPrinter(
        TokenFormatter fmt,
        VeraSchemaPrinter config)
    {
        super(fmt, config);
    }

    public void printMembers(Collection members)
    {
        boolean first = true;
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            VeraCompilationUnitMember member = (VeraCompilationUnitMember) iter
                .next();
            if (!first) printNewLine();
            member.accept(this);
            first = false;
        }
    }

    public void visit(VeraBindVariable obj)
    {
        fmt.printToken("bind");
        fmt.printSpace();
        printName(obj.getPort().getName(), false);
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraBindMember member = (VeraBindMember) iter.next();
            printBindMember(member);
        }

        fmt.decIndent();
        fmt.printToken("}");
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

    public void visit(VeraExpressionDefine obj)
    {
        printDefine(obj, formatDefineExpr(obj.getExpression()));
    }

    private void printDefine(VeraDefine obj, String expansion)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("#define ");
        buf.append(obj.getName());
        List arguments = obj.getArguments();
        if (!arguments.isEmpty())
        {
            buf.append('(');
            Iterator iter = arguments.iterator();
            while (iter.hasNext())
            {
                VeraDefineArgument var = (VeraDefineArgument) iter.next();
                buf.append(var.getName());
                if (iter.hasNext()) buf.append(',');
            }
            buf.append(')');
        }
        buf.append(' ');
        buf.append(expansion);
        fmt.printToken(buf.toString());
        printNewLine();
    }

    private String formatDefineExpr(VeraExpression expr)
    {
        if (expr != null)
        {
            StringTokenFormatter strFmt = new StringTokenFormatter();
            printExpression(expr, 100, strFmt);
            return strFmt.toString();
        }
        else
        {
            return "/* NULL */";
        }
    }

    public void visit(VeraGlobalFunction obj)
    {
        printGlobalFunc(obj);
    }

    public void visit(VeraGlobalVariable obj)
    {
        // TODO: print global variables in program block
        printVarDecl(obj);
        printVarInit(obj);
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraHDLFunction obj)
    {
        fmt.printToken("hdl_task");
        fmt.printSpace();
        printID(obj.getName());
        VeraFunctionType funcType = obj.getType();
        printFuncArgs(funcType);
        fmt.printToken("\"" + obj.getInstPath() + "\"");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraInterfaceType obj)
    {
        printInterface(obj);
    }

    public void visit(VeraPortType obj)
    {
        printPort(obj);
    }

    public void visit(VeraProgram obj)
    {
        fmt.printToken("program");
        fmt.printSpace();
        printID(obj.getName());
        printBody(obj.getBlock());
    }

    public void visit(VeraRangeDefine obj)
    {
        printDefine(obj, formatDefineRange(obj.getRange()));
    }

    private String formatDefineRange(VeraRange range)
    {
        if (range != null)
        {
            StringTokenFormatter strFmt = new StringTokenFormatter();
            printRange(range, strFmt);
            return strFmt.toString();
        }
        else
        {
            return "/* NULL */";
        }
    }

    public void visit(VeraStatementDefine obj)
    {
        printDefine(obj, formatDefineStmt(obj.getStatement()));
    }

    private String formatDefineStmt(VeraStatement stmt)
    {
        if (stmt != null)
        {
            StringTokenFormatter strFmt = new StringTokenFormatter();
            BlockMemberPrinter bmp = new BlockMemberPrinter(strFmt, config);
            bmp.printStatement(stmt);
            return strFmt.toString();
        }
        else
        {
            return "/* NULL */";
        }
    }

    public void visit(VeraTypeDefine obj)
    {
        printDefine(obj, formatDefineType(obj.getType()));
    }

    private String formatDefineType(VeraType type)
    {
        if (type != null)
        {
            StringTokenFormatter strFmt = new StringTokenFormatter();
            printType(type, strFmt);
            printArrayModifiers(type, strFmt);
            return strFmt.toString();
        }
        else
        {
            return "/* NULL */";
        }
    }

    public void visit(VeraUDFFunction obj)
    {
        fmt.printToken("extern");
        fmt.printSpace();
        fmt.printToken("\"" + obj.getLanguage() + "\"");
        fmt.printSpace();
        VeraFunctionType funcType = obj.getType();
        printFuncProlog(funcType);
        printID(obj.getName());
        printFuncArgs(funcType);
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraUserClass obj)
    {
        printClass(obj);
    }
}
