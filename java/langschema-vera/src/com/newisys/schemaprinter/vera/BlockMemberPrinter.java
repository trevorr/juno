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
import com.newisys.verilog.EdgeSet;

/**
 * Printer module used to print Vera block members.
 * 
 * @author Trevor Robinson
 */
public class BlockMemberPrinter
    extends VeraSchemaPrinterModule
    implements VeraBlockMemberVisitor
{
    public BlockMemberPrinter(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        super(fmt, config);
    }

    public void printStatement(VeraStatement stmt)
    {
        VeraDefineReference defineRef = stmt.getDefineRef();
        if (defineRef != null)
        {
            printDefineRef(defineRef, fmt);
            printNewLine();
        }
        else
        {
            stmt.accept(this);
        }
    }

    public void printMember(VeraBlockMember member)
    {
        if (member instanceof VeraStatement)
        {
            printStatement((VeraStatement) member);
        }
        else
        {
            member.accept(this);
        }
    }

    public void printMembers(Collection members)
    {
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            VeraBlockMember member = (VeraBlockMember) iter.next();
            printMember(member);
        }
    }

    private void printGuard(String token, VeraExpression expr)
    {
        fmt.printToken(token);
        fmt.printSpace();
        fmt.printLeadingToken("(");
        printExpression(expr);
        fmt.printTrailingToken(")");
    }

    private void printNestedStatement(VeraStatement stmt)
    {
        if (stmt instanceof VeraBlock)
        {
            printBlock((VeraBlock) stmt);
        }
        else
        {
            fmt.incIndent();
            printStatement(stmt);
            fmt.decIndent();
        }
    }

    private void printPredicate(VeraStatement stmt, boolean simple)
    {
        if (simple)
        {
            fmt.printSpace();
            printStatement(stmt);
        }
        else
        {
            printNewLine();
            printNestedStatement(stmt);
        }
    }

    private boolean isSimpleStmt(VeraStatement stmt)
    {
        return stmt instanceof VeraExpressionStatement
            || stmt instanceof VeraBreakpointStatement
            || stmt instanceof VeraBreakStatement
            || stmt instanceof VeraContinueStatement
            || stmt instanceof VeraDriveSampleStatement
            || stmt instanceof VeraExpectStatement
            || stmt instanceof VeraReturnStatement
            || stmt instanceof VeraSyncStatement
            || stmt instanceof VeraTerminateStatement;
    }

    public void visit(VeraLocalVariable obj)
    {
        printVarDecl(obj);
        printVarInit(obj);
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraBlock obj)
    {
        printBlock(obj);
    }

    public void visit(VeraBreakpointStatement obj)
    {
        fmt.printToken("breakpoint");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraBreakStatement obj)
    {
        fmt.printToken("break");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraContinueStatement obj)
    {
        fmt.printToken("continue");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraDriveSampleStatement obj)
    {
        final VeraExpression delay = obj.getDelay();
        if (delay != null)
        {
            fmt.printLeadingToken("@");
            printExpression(delay);
            fmt.printSpace();
        }

        printExpression(obj.getDestination());

        fmt.printSpace();
        fmt.printLeadingToken(obj.isNonBlocking() ? "<=" : "=");
        fmt.printSpace();

        printExpression(obj.getSource());

        if (obj.isSoft())
        {
            fmt.printSpace();
            fmt.printToken("soft");
        }

        if (obj.isAsync())
        {
            fmt.printSpace();
            fmt.printToken("async");
        }

        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraExpectStatement obj)
    {
        final String token;
        final VeraExpectKind expectKind = obj.getExpectKind();
        if (expectKind == VeraExpectKind.SIMPLE)
        {
            token = "@";
        }
        else if (expectKind == VeraExpectKind.FULL)
        {
            token = "@@";
        }
        else
        {
            assert (expectKind == VeraExpectKind.RESTRICTED);
            token = "@@@";
        }
        fmt.printLeadingToken(token);

        final VeraExpression delay = obj.getDelay();
        final VeraExpression window = obj.getWindow();
        if (delay != null)
        {
            printExpression(delay);
            if (window != null)
            {
                fmt.printTrailingToken(",");
                printExpression(window);
            }
        }
        else if (window != null)
        {
            fmt.printLeadingToken(",");
            printExpression(window);
        }
        else
        {
            // delay or window is required; if none specified, use delay 0
            fmt.printToken("0");
        }
        fmt.printSpace();

        final Iterator iter = obj.getExpectTerms().iterator();
        final boolean expectAll = obj.isExpectAll();
        while (iter.hasNext())
        {
            VeraExpectTerm term = (VeraExpectTerm) iter.next();
            printExpression(term.getSignal());
            fmt.printSpace();
            fmt.printLeadingToken(term.isEqual() ? "==" : "!=");
            fmt.printSpace();
            printExpression(term.getValue());
            if (iter.hasNext())
            {
                if (expectAll)
                {
                    fmt.printTrailingToken(",");
                }
                else
                {
                    fmt.printSpace();
                    fmt.printLeadingToken("or");
                }
                fmt.printSpace();
            }
        }

        if (obj.isSoft())
        {
            fmt.printSpace();
            fmt.printToken("soft");
        }

        if (obj.isAsync())
        {
            fmt.printSpace();
            fmt.printToken("async");
        }

        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraExpressionStatement obj)
    {
        printExpression(obj.getExpression());
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraForkStatement obj)
    {
        fmt.printToken("fork");
        printNewLine();

        Iterator iter = obj.getForkedStatements().iterator();
        while (iter.hasNext())
        {
            printNestedStatement((VeraStatement) iter.next());
        }

        fmt.printToken("join");
        fmt.printToken(obj.getJoinKind().toString());
        printNewLine();
    }

    public void visit(VeraForStatement obj)
    {
        fmt.printToken("for");
        fmt.printSpace();
        fmt.printLeadingToken("(");

        Iterator iter = obj.getInitStatements().iterator();
        while (iter.hasNext())
        {
            VeraExpressionStatement exprStmt = (VeraExpressionStatement) iter
                .next();
            printExpression(exprStmt.getExpression());
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
        fmt.printTrailingToken(";");
        fmt.printSpace();

        VeraExpression condExpr = (VeraExpression) obj.getCondition();
        if (condExpr != null)
        {
            printExpression(condExpr);
        }
        fmt.printTrailingToken(";");
        fmt.printSpace();

        iter = obj.getUpdateStatements().iterator();
        while (iter.hasNext())
        {
            VeraExpressionStatement exprStmt = (VeraExpressionStatement) iter
                .next();
            printExpression(exprStmt.getExpression());
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }

        fmt.printTrailingToken(")");
        printNewLine();

        printNestedStatement(obj.getStatement());
    }

    public void visit(VeraIfStatement obj)
    {
        VeraIfStatement cur = obj;
        while (cur != null)
        {
            VeraStatement thenStmt = cur.getThenStatement();
            VeraStatement elseStmt = cur.getElseStatement();

            printGuard("if", cur.getCondition());

            printPredicate(thenStmt, elseStmt == null && isSimpleStmt(thenStmt));

            cur = null;
            if (elseStmt != null)
            {
                fmt.printToken("else");
                if (elseStmt instanceof VeraIfStatement)
                {
                    fmt.printSpace();
                    cur = (VeraIfStatement) elseStmt;
                }
                else
                {
                    printNewLine();
                    printNestedStatement(elseStmt);
                }
            }
        }
    }

    public void visit(VeraRandCaseStatement obj)
    {
        fmt.printToken("randcase");
        fmt.printToken("{");
        printNewLine();

        final Iterator iter = obj.getCaseList().iterator();
        while (iter.hasNext())
        {
            final VeraRandCase c = (VeraRandCase) iter.next();
            printExpression(c.getWeight());
            fmt.printTrailingToken(":");

            final VeraStatement stmt = c.getStatement();
            printPredicate(stmt, isSimpleStmt(stmt));
        }

        fmt.printToken("}");
        printNewLine();
    }

    public void visit(VeraRepeatStatement obj)
    {
        printGuard("repeat", obj.getCondition());
        printNewLine();

        printNestedStatement(obj.getStatement());
    }

    public void visit(VeraReturnStatement obj)
    {
        fmt.printToken("return");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraSwitchStatement obj)
    {
        final VeraCaseKind caseKind = obj.getCaseKind();
        final String caseKeyword;
        if (caseKind == VeraCaseKind.X)
        {
            caseKeyword = "casex";
        }
        else if (caseKind == VeraCaseKind.Z)
        {
            caseKeyword = "casez";
        }
        else
        {
            assert (caseKind == VeraCaseKind.NORMAL);
            caseKeyword = "case";
        }
        printGuard(caseKeyword, obj.getSelector());
        printNewLine();

        fmt.printToken("{");
        printNewLine();

        final Iterator iter = obj.getCases().iterator();
        while (iter.hasNext())
        {
            final VeraSwitchCase c = (VeraSwitchCase) iter.next();
            if (c instanceof VeraSwitchValueCase)
            {
                final VeraSwitchValueCase vc = (VeraSwitchValueCase) c;
                final Iterator valueIter = vc.getValues().iterator();
                while (valueIter.hasNext())
                {
                    VeraExpression expr = (VeraExpression) valueIter.next();
                    printExpression(expr);
                    if (valueIter.hasNext())
                    {
                        fmt.printToken(",");
                        fmt.printSpace();
                    }
                }
                fmt.printTrailingToken(":");
            }
            else
            {
                assert (c instanceof VeraSwitchDefaultCase);
                fmt.printToken("default");
                fmt.printTrailingToken(":");
            }

            final VeraStatement stmt = c.getStatement();
            printPredicate(stmt, isSimpleStmt(stmt));
        }

        fmt.printToken("}");
        printNewLine();
    }

    public void visit(VeraSyncStatement obj)
    {
        fmt.printLeadingToken("@");
        fmt.printLeadingToken("(");

        Iterator iter = obj.getTerms().iterator();
        while (iter.hasNext())
        {
            VeraSyncTerm term = (VeraSyncTerm) iter.next();

            EdgeSet edges = term.getEdgeSet();
            if (EdgeSet.POSEDGE.contains(edges))
            {
                fmt.printToken("posedge");
                fmt.printSpace();
            }
            else if (EdgeSet.NEGEDGE.contains(edges))
            {
                fmt.printToken("negedge");
                fmt.printSpace();
            }

            printExpression(term.getSignal());

            if (term.isAsync())
            {
                fmt.printSpace();
                fmt.printToken("async");
            }

            if (iter.hasNext())
            {
                fmt.printSpace();
                fmt.printToken("or");
                fmt.printSpace();
            }
        }

        fmt.printTrailingToken(")");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraTerminateStatement obj)
    {
        fmt.printToken("terminate");
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(VeraWhileStatement obj)
    {
        printGuard("while", obj.getCondition());
        printNewLine();

        printNestedStatement(obj.getStatement());
    }
}
