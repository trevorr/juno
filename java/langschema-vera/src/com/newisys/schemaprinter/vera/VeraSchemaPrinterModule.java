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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Name;
import com.newisys.langschema.Visibility;
import com.newisys.langschema.vera.*;
import com.newisys.schemaprinter.SchemaPrinterModule;
import com.newisys.util.text.TokenFormatter;
import com.newisys.verilog.EdgeSet;

/**
 * Convenience base class for Vera schema printer Visitor classes. Provides
 * methods for printing schema information that is common across Visitors.
 */
public abstract class VeraSchemaPrinterModule
    extends SchemaPrinterModule
{
    protected final VeraSchemaPrinter config;

    public VeraSchemaPrinterModule(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        super(fmt);
        this.config = config;
    }

    public void printID(Name name)
    {
        fmt.printToken(name.getIdentifier());
    }

    public void printName(Name name, boolean qualify)
    {
        if (qualify)
        {
            printName(name.getNamespace().getName(), true);
            fmt.printLeadingToken("::");
        }
        printID(name);
    }

    public void printVisibility(
        Visibility visibility,
        VeraVisibility defaultVisibility)
    {
        if (visibility != defaultVisibility)
        {
            fmt.printToken(visibility.toString());
            fmt.printSpace();
        }
    }

    public void printModifiers(Set modifiers)
    {
        int count = modifiers.size();
        if (count > 0)
        {
            String[] strs = new String[count];
            int index = 0;
            Iterator iter = modifiers.iterator();
            while (iter.hasNext())
            {
                strs[index++] = iter.next().toString();
            }

            Arrays.sort(strs);

            for (index = 0; index < count; ++index)
            {
                fmt.printToken(strs[index]);
                fmt.printSpace();
            }
        }
    }

    public void printType(VeraType type)
    {
        printType(type, fmt);
    }

    public void printType(VeraType type, TokenFormatter fmt)
    {
        if (type instanceof VeraArrayType)
        {
            VeraArrayType arrayType = (VeraArrayType) type;
            printType(arrayType.getElementType(), fmt);
        }
        else if (type instanceof VeraComplexType)
        {
            // TODO: qualify names of class enum references outside class
            printName(((VeraComplexType) type).getName(), false);
        }
        else if (type instanceof VeraFixedBitVectorType)
        {
            int size = ((VeraFixedBitVectorType) type).getSize();
            fmt.printToken("bit");
            fmt.printLeadingToken("[");
            fmt.printToken(String.valueOf(size - 1));
            fmt.printTrailingToken(":0]");
        }
        else
        {
            fmt.printToken(type.toReferenceString());
        }
    }

    public void printArrayModifiers(VeraType type)
    {
        printArrayModifiers(type, fmt);
    }

    public void printArrayModifiers(VeraType type, TokenFormatter fmt)
    {
        if (!(type instanceof VeraArrayType)) return;

        if (type instanceof VeraFixedArrayType)
        {
            int[] dims = ((VeraFixedArrayType) type).getDimensions();
            int dimCount = dims.length;
            for (int i = 0; i < dimCount; ++i)
            {
                fmt.printLeadingToken("[");
                fmt.printToken(String.valueOf(dims[i]));
                fmt.printTrailingToken("]");
            }
        }
        else if (type instanceof VeraAssocArrayType)
        {
            fmt.printLeadingToken("[");
            VeraType indexType = ((VeraAssocArrayType) type).getIndexTypes()[0];
            if (!(indexType instanceof VeraBitVectorType))
            {
                printType(indexType, fmt);
            }
            fmt.printTrailingToken("]");
        }
        else
        {
            assert (type instanceof VeraDynamicArrayType);
            fmt.printLeadingToken("[");
            fmt.printToken("*");
            fmt.printTrailingToken("]");
        }
    }

    public void printVarDecl(VeraVariable var)
    {
        printModifiers(var.getModifiers());
        VeraType type = var.getType();
        printType(type);
        fmt.printSpace();
        printID(var.getName());
        printArrayModifiers(type);
    }

    public void printVarInit(VeraVariable var)
    {
        VeraExpression initExpr = var.getInitializer();
        if (initExpr != null)
        {
            fmt.printSpace();
            fmt.printLeadingToken("=");
            fmt.printSpace();
            printExpression(initExpr);
        }
    }

    public void printFuncProlog(VeraFunctionType funcType)
    {
        VeraType returnType = funcType.getReturnType();
        if (returnType != null && !(returnType instanceof VeraVoidType))
        {
            fmt.printToken("function");
            fmt.printSpace();
            printType(returnType);
        }
        else
        {
            fmt.printToken("task");
        }
        fmt.printSpace();
    }

    private int printFuncArg(
        VeraFunctionArgument arg,
        int curLevel,
        boolean first,
        boolean last)
    {
        int argLevel = arg.getOptionalLevel();
        if (!first)
        {
            while (curLevel > argLevel)
            {
                fmt.printTrailingToken(")");
                --curLevel;
            }
            fmt.printTrailingToken(",");
            fmt.printSpace();
        }
        while (curLevel < argLevel)
        {
            fmt.printLeadingToken("(");
            ++curLevel;
        }
        printVarDecl(arg);
        printVarInit(arg);
        if (last)
        {
            while (curLevel > 0)
            {
                fmt.printTrailingToken(")");
                --curLevel;
            }
        }
        return curLevel;
    }

    public void printFuncArg(VeraFunctionArgument arg)
    {
        printFuncArg(arg, 0, true, true);
    }

    public void printFuncArgs(VeraFunctionType funcType)
    {
        fmt.printTrailingToken("(");
        boolean first = true;
        boolean varArgs = funcType.isVarArgs();
        int curLevel = 0;
        Iterator iter = funcType.getArguments().iterator();
        while (iter.hasNext())
        {
            VeraFunctionArgument arg = (VeraFunctionArgument) iter.next();
            curLevel = printFuncArg(arg, curLevel, first, !varArgs
                && !iter.hasNext());
            first = false;
        }
        if (varArgs)
        {
            if (funcType.isVarArgsByRef())
            {
                fmt.printToken("var");
                fmt.printSpace();
            }
            fmt.printToken("...");
        }
        fmt.printTrailingToken(")");
    }

    public void printExpression(VeraExpression expr)
    {
        printExpression(expr, 100);
    }

    public void printExpression(VeraExpression expr, int parentPrecedence)
    {
        printExpression(expr, parentPrecedence, fmt);
    }

    public void printExpression(
        VeraExpression expr,
        int parentPrecedence,
        TokenFormatter fmt)
    {
        ExpressionPrinter printer = new ExpressionPrinter(fmt, config,
            parentPrecedence);
        VeraDefineReference defineRef = expr.getDefineRef();
        if (defineRef != null)
        {
            printDefineRef(defineRef, fmt);
        }
        else
        {
            fmt.beginGroup();
            expr.accept(printer);
            fmt.endGroup();
        }
    }

    public void printRange(VeraRange obj, TokenFormatter fmt)
    {
        VeraDefineReference defineRef = obj.getDefineRef();
        if (defineRef != null)
        {
            printDefineRef(defineRef, fmt);
        }
        else
        {
            printExpression(obj.getFrom(), 100, fmt);
            fmt.printLeadingToken(":");
            printExpression(obj.getTo(), 100, fmt);
        }
    }

    public void printConstraints(List exprs)
    {
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        Iterator iter = exprs.iterator();
        while (iter.hasNext())
        {
            fmt.printSpace();
            VeraExpression expr = (VeraExpression) iter.next();
            printExpression(expr);
            if (!endsWithSet(expr))
            {
                fmt.printTrailingToken(";");
            }
            printNewLine();
        }

        fmt.decIndent();
        fmt.printToken("}");
    }

    public boolean endsWithSet(VeraExpression expr)
    {
        if (expr instanceof VeraConstraintSet)
        {
            return true;
        }
        else if (expr instanceof VeraImplicationConstraint)
        {
            return endsWithSet(((VeraImplicationConstraint) expr).getOperand(1));
        }
        return false;
    }

    public void printBlock(VeraBlock block)
    {
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        if (block != null)
        {
            BlockMemberPrinter bmp = new BlockMemberPrinter(fmt, config);
            bmp.printMembers(block.getMembers());
        }
        else
        {
            // while complete schemas should never have null blocks,
            // we support them here for dumping intermediate schemas
            fmt.printToken("// NULL BLOCK");
            printNewLine();
        }

        fmt.decIndent();
        fmt.printToken("}");
        printNewLine();
    }

    public void printBody(VeraBlock body)
    {
        if (config.isCollapseBodies())
        {
            fmt.printTrailingToken("...");
            printNewLine();
        }
        else
        {
            printNewLine();
            printBlock(body);
        }
    }

    public void printEnum(VeraEnumeration obj)
    {
        fmt.printToken("enum");
        fmt.printSpace();
        printID(obj.getName());
        fmt.printSpace();
        fmt.printToken("=");
        fmt.printSpace();

        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraEnumerationElement member = (VeraEnumerationElement) iter
                .next();
            printEnumElement(member);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }

        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void printEnumElement(VeraEnumerationElement member)
    {
        printID(member.getName());
        fmt.printSpace();
        fmt.printToken("=");
        fmt.printSpace();
        fmt.printToken(String.valueOf(member.getValue()));
    }

    public void printClass(VeraClass obj)
    {
        printVisibility(obj.getVisibility(), VeraVisibility.PUBLIC);

        if (obj.isVirtual())
        {
            fmt.printToken("virtual");
            fmt.printSpace();
        }

        fmt.printToken("class");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        VeraClass baseCls = obj.getBaseClass();
        if (baseCls != null)
        {
            Name name = baseCls.getName();
            if (!name.getCanonicalName().equals("<root>"))
            {
                fmt.incIndent();
                fmt.printToken("extends");
                fmt.printSpace();
                printID(name);
                printNewLine();
                fmt.decIndent();
            }
        }

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        ClassMemberPrinter cmp = new ClassMemberPrinter(fmt, config);
        cmp.printMembers(obj.getMembers());

        fmt.decIndent();
        fmt.printToken("}");
        printNewLine();
    }

    public void printGlobalFunc(VeraGlobalFunction obj)
    {
        if (obj.isExport())
        {
            fmt.printToken("export");
            fmt.printSpace();
        }
        printVisibility(obj.getVisibility(), VeraVisibility.PUBLIC);
        VeraFunctionType funcType = obj.getType();
        printFuncProlog(funcType);
        printID(obj.getName());
        printFuncArgs(funcType);
        printBody(obj.getBody());
    }

    public void printBindMember(VeraBindMember member)
    {
        printID(member.getPortSignal().getName());
        fmt.printSpace();
        printExpression(member.getInterfaceExpr());
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void printInterface(VeraInterfaceType obj)
    {
        fmt.printToken("interface");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraInterfaceSignal member = (VeraInterfaceSignal) iter.next();
            printInterfaceSignal(member);
        }

        fmt.decIndent();
        fmt.printToken("}");
        printNewLine();
    }

    public void printInterfaceSignal(VeraInterfaceSignal member)
    {
        VeraSignalDirection direction = member.getDirection();
        fmt.printToken(direction.toString());

        int width = member.getWidth();
        if (width > 1)
        {
            fmt.printLeadingToken("[");
            fmt.printToken(String.valueOf(width - 1));
            fmt.printTrailingToken(":0]");
        }

        fmt.printSpace();
        printID(member.getName());

        VeraSignalKind kind = member.getKind();
        if (kind == VeraSignalKind.CLOCK)
        {
            fmt.printSpace();
            fmt.printToken("CLOCK");
        }
        else if (kind == VeraSignalKind.NORMAL)
        {
            EdgeSet sampleEdges = member.getSampleEdges();
            if (sampleEdges.contains(EdgeSet.POSEDGE))
            {
                fmt.printSpace();
                fmt.printToken("PSAMPLE");
            }
            if (sampleEdges.contains(EdgeSet.NEGEDGE))
            {
                fmt.printSpace();
                fmt.printToken("NSAMPLE");
            }
            int sampleSkew = member.getSampleSkew();
            if (sampleSkew != 0)
            {
                fmt.printSpace();
                fmt.printLeadingToken("#");
                fmt.printToken(String.valueOf(sampleSkew));
            }

            EdgeSet driveEdges = member.getDriveEdges();
            if (driveEdges.contains(EdgeSet.POSEDGE))
            {
                fmt.printSpace();
                fmt.printToken("PHOLD");
            }
            if (driveEdges.contains(EdgeSet.NEGEDGE))
            {
                fmt.printSpace();
                fmt.printToken("NHOLD");
            }
            int driveSkew = member.getDriveSkew();
            if (driveSkew != 0)
            {
                fmt.printSpace();
                fmt.printLeadingToken("#");
                fmt.printToken(String.valueOf(driveSkew));
            }
        }
        else
        {
            // TODO: return-to-? signals
            assert false;
        }

        int depth = member.getSampleDepth();
        if (depth > 0)
        {
            fmt.printSpace();
            fmt.printToken("depth");
            fmt.printSpace();
            fmt.printToken(String.valueOf(depth));
        }

        VeraVCAKind vcaKind = member.getVCAKind();
        if (vcaKind != VeraVCAKind.NONE)
        {
            // TODO: signal VCA
            fmt.printSpace();
            fmt.printToken("/* VCA */");
        }

        String node = member.getHDLNode();
        if (node != null)
        {
            fmt.printSpace();
            fmt.printToken("hdl_node");
            fmt.printSpace();
            fmt.printToken("\"" + node + "\"");
        }

        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void printPort(VeraPortType obj)
    {
        fmt.printToken("port");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            VeraPortSignal member = (VeraPortSignal) iter.next();
            printPortSignal(member);
        }

        fmt.decIndent();
        fmt.printToken("}");
        printNewLine();
    }

    public void printPortSignal(VeraPortSignal member)
    {
        printID(member.getName());
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void printDefineRef(VeraDefineReference obj, TokenFormatter fmt)
    {
        fmt.printToken(obj.getDefine().getName().getIdentifier());
        List arguments = obj.getArguments();
        if (!arguments.isEmpty())
        {
            fmt.printTrailingToken("(");
            Iterator iter = arguments.iterator();
            while (iter.hasNext())
            {
                printExpression((VeraExpression) iter.next(), 100, fmt);
                if (iter.hasNext())
                {
                    fmt.printTrailingToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printTrailingToken(")");
        }
    }
}
