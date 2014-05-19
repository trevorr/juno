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

import java.util.Iterator;
import java.util.List;

import com.newisys.langschema.NamedObject;
import com.newisys.langschema.StructuredType;
import com.newisys.langschema.vera.*;
import com.newisys.util.text.TokenFormatter;

/**
 * Printer module used to print Vera expressions.
 * 
 * @author Trevor Robinson
 */
public class ExpressionPrinter
    extends VeraSchemaPrinterModule
    implements VeraExpressionVisitor
{
    private final int parentPrecedence;

    public ExpressionPrinter(
        TokenFormatter fmt,
        VeraSchemaPrinter config,
        int parentPrecedence)
    {
        super(fmt, config);
        this.parentPrecedence = parentPrecedence;
    }

    public ExpressionPrinter(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        this(fmt, config, 100);
    }

    // Precedence:
    // 0: ()
    // 1: ++ --
    // 2: & ~& | ~| ^ ~^ ~ >< - ! (unary)
    // 3: * / %
    // 4: + -
    // 5: << >>
    // 6: < <= > >= in !in dist
    // 7: =?= !?= == != === !==
    // 8: & &~
    // 9: ^ ^~
    // 10: | |~
    // 11: &&
    // 12: ||
    // 13: ?:
    // 14: =>
    // 15: = += -= *= /= %=
    // 16: <<= >>= &= |= ^= ~&= ~|= ~^=

    private void printUnary(
        VeraUnaryOperation op,
        String token,
        int precedence,
        boolean prefix)
    {
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        if (prefix) fmt.printLeadingToken(token);

        VeraExpression op1 = op.getOperand(0);
        printExpression(op1, precedence - (prefix ? 0 : 1));

        if (!prefix) fmt.printTrailingToken(token);

        if (needParens) fmt.printTrailingToken(")");
    }

    private void printInfix(VeraBinaryOperation op, String token, int precedence)
    {
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        VeraExpression op1 = op.getOperand(0);
        printExpression(op1, precedence);

        fmt.printSpace();
        fmt.printLeadingToken(token);
        fmt.printSpace();

        VeraExpression op2 = op.getOperand(1);
        printExpression(op2, precedence - 1);

        if (needParens) fmt.printTrailingToken(")");
    }

    private void printSet(VeraSetOperation op, String token)
    {
        final int precedence = 6;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        printExpression(op.getExpr(), precedence);

        fmt.printSpace();
        fmt.printLeadingToken(token);
        fmt.printSpace();

        fmt.printLeadingToken("{");
        Iterator iter = op.getMembers().iterator();
        while (iter.hasNext())
        {
            fmt.printSpace();
            VeraSetMember member = (VeraSetMember) iter.next();

            VeraExpression weight = member.getWeight();
            if (weight != null)
            {
                printExpression(weight);
                fmt.printSpace();
                fmt.printLeadingToken(member.isWeightPerItem() ? ":=" : ":/");
                fmt.printSpace();
            }

            if (member instanceof VeraSetRange)
            {
                VeraSetRange rangeMember = (VeraSetRange) member;
                printExpression(rangeMember.getLow());
                fmt.printLeadingToken(":");
                printExpression(rangeMember.getHigh());
            }
            else
            {
                VeraSetValue valueMember = (VeraSetValue) member;
                printExpression(valueMember.getValue());
            }

            if (iter.hasNext()) fmt.printTrailingToken(",");
        }
        fmt.printSpace();
        fmt.printTrailingToken("}");

        if (needParens) fmt.printTrailingToken(")");
    }

    private void printArgs(List args, boolean parensOpt)
    {
        if (!parensOpt || !args.isEmpty())
        {
            fmt.printTrailingToken("(");
            Iterator iter = args.iterator();
            while (iter.hasNext())
            {
                printExpression((VeraExpression) iter.next());
                if (iter.hasNext())
                {
                    fmt.printTrailingToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printTrailingToken(")");
        }
    }

    public void visit(VeraAdd obj)
    {
        printInfix(obj, "+", 4);
    }

    public void visit(VeraAndReduction obj)
    {
        printUnary(obj, "&", 2, true);
    }

    public void visit(VeraArithmeticNegative obj)
    {
        printUnary(obj, "-", 2, true);
    }

    public void visit(VeraArrayAccess obj)
    {
        printExpression(obj.getArray(), 0);
        Iterator iter = obj.getIndices().iterator();
        while (iter.hasNext())
        {
            fmt.printLeadingToken("[");
            printExpression((VeraExpression) iter.next());
            fmt.printTrailingToken("]");
        }
    }

    public void visit(VeraArrayCreation obj)
    {
        fmt.printToken("new");
        Iterator iter = obj.getDimensions().iterator();
        while (iter.hasNext())
        {
            fmt.printLeadingToken("[");
            printExpression((VeraExpression) iter.next());
            fmt.printTrailingToken("]");
        }
        VeraExpression sourceExpr = obj.getSourceExpr();
        if (sourceExpr != null)
        {
            fmt.printSpace();
            fmt.printLeadingToken("(");
            printExpression(sourceExpr);
            fmt.printTrailingToken(")");
        }
    }

    public void visit(VeraArrayInitializer obj)
    {
        fmt.printLeadingToken("{");
        Iterator iter = obj.getElements().iterator();
        while (iter.hasNext())
        {
            fmt.printSpace();
            printExpression((VeraExpression) iter.next());
            if (iter.hasNext()) fmt.printTrailingToken(",");
        }
        fmt.printSpace();
        fmt.printTrailingToken("}");
    }

    public void visit(VeraAssign obj)
    {
        printInfix(obj, "=", 15);
    }

    public void visit(VeraAssignAdd obj)
    {
        printInfix(obj, "+=", 15);
    }

    public void visit(VeraAssignAnd obj)
    {
        printInfix(obj, "&=", 16);
    }

    public void visit(VeraAssignAndNot obj)
    {
        printInfix(obj, "~&=", 16);
    }

    public void visit(VeraAssignDivide obj)
    {
        printInfix(obj, "/=", 15);
    }

    public void visit(VeraAssignLeftShift obj)
    {
        printInfix(obj, "<<=", 16);
    }

    public void visit(VeraAssignModulo obj)
    {
        printInfix(obj, "%=", 15);
    }

    public void visit(VeraAssignMultiply obj)
    {
        printInfix(obj, "*=", 15);
    }

    public void visit(VeraAssignOr obj)
    {
        printInfix(obj, "|=", 16);
    }

    public void visit(VeraAssignOrNot obj)
    {
        printInfix(obj, "~|=", 16);
    }

    public void visit(VeraAssignRightShift obj)
    {
        printInfix(obj, ">>=", 16);
    }

    public void visit(VeraAssignSubtract obj)
    {
        printInfix(obj, "-=", 15);
    }

    public void visit(VeraAssignXor obj)
    {
        printInfix(obj, "^=", 16);
    }

    public void visit(VeraAssignXorNot obj)
    {
        printInfix(obj, "~^=", 16);
    }

    public void visit(VeraBitSliceAccess obj)
    {
        printExpression(obj.getArray(), 0);
        fmt.printLeadingToken("[");
        printRange(obj.getRange(), fmt);
        fmt.printTrailingToken("]");
    }

    public void visit(VeraBitVectorLiteral obj)
    {
        fmt.printToken(obj.getValue().toString(obj.getRadix()));
    }

    public void visit(VeraBitwiseAnd obj)
    {
        printInfix(obj, "&", 8);
    }

    public void visit(VeraBitwiseAndNot obj)
    {
        printInfix(obj, "&~", 8);
    }

    public void visit(VeraBitwiseNegative obj)
    {
        printUnary(obj, "~", 2, true);
    }

    public void visit(VeraBitwiseOr obj)
    {
        printInfix(obj, "|", 10);
    }

    public void visit(VeraBitwiseOrNot obj)
    {
        printInfix(obj, "|~", 10);
    }

    public void visit(VeraBitwiseReverse obj)
    {
        printUnary(obj, "><", 2, true);
    }

    public void visit(VeraBitwiseXor obj)
    {
        printInfix(obj, "^", 9);
    }

    public void visit(VeraBitwiseXorNot obj)
    {
        printInfix(obj, "^~", 9);
    }

    public void visit(VeraConcatenation obj)
    {
        fmt.printLeadingToken("{");
        Iterator iter = obj.getOperands().iterator();
        while (iter.hasNext())
        {
            fmt.printSpace();
            printExpression((VeraExpression) iter.next());
            if (iter.hasNext()) fmt.printTrailingToken(",");
        }
        fmt.printSpace();
        fmt.printTrailingToken("}");
    }

    public void visit(VeraConditional obj)
    {
        final int precedence = 13;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        VeraExpression op1 = obj.getOperand(0);
        printExpression(op1, precedence - 1);

        fmt.printSpace();
        fmt.printLeadingToken("?");
        fmt.printSpace();

        VeraExpression op2 = obj.getOperand(1);
        printExpression(op2, precedence);

        fmt.printSpace();
        fmt.printLeadingToken(":");
        fmt.printSpace();

        VeraExpression op3 = obj.getOperand(2);
        printExpression(op3, precedence);

        if (needParens) fmt.printTrailingToken(")");
    }

    public void visit(VeraConstraintSet obj)
    {
        List exprs = obj.getExprs();
        printConstraints(exprs);
    }

    public void visit(VeraCopyCreation obj)
    {
        fmt.printToken("new");
        fmt.printSpace();
        printExpression(obj.getSource());
    }

    public void visit(VeraDepthAccess obj)
    {
        fmt.printLeadingToken(".");
        fmt.printToken(String.valueOf(obj.getDepth()));
    }

    public void visit(VeraDistSet obj)
    {
        printSet(obj, "dist");
    }

    public void visit(VeraDivide obj)
    {
        printInfix(obj, "/", 3);
    }

    public void visit(VeraEnumValueReference obj)
    {
        VeraEnumerationElement elem = obj.getElement();
        // TODO: qualify names of class enum value references outside class
        printName(elem.getName(), false);
    }

    public void visit(VeraEqual obj)
    {
        printInfix(obj, "==", 7);
    }

    public void visit(VeraExactEqual obj)
    {
        printInfix(obj, "===", 7);
    }

    public void visit(VeraExactNotEqual obj)
    {
        printInfix(obj, "!==", 7);
    }

    public void visit(VeraFunctionInvocation obj)
    {
        printExpression(obj.getFunction(), 0);
        printArgs(obj.getArguments(), false);
        final List constraints = obj.getConstraints();
        if (!constraints.isEmpty())
        {
            fmt.printSpace();
            fmt.printToken("with");
            fmt.printSpace();
            printConstraints(constraints);
        }
    }

    public void visit(VeraFunctionReference obj)
    {
        printName(obj.getFunction().getName(), false);
    }

    public void visit(VeraGreater obj)
    {
        printInfix(obj, ">", 6);
    }

    public void visit(VeraGreaterOrEqual obj)
    {
        printInfix(obj, ">=", 6);
    }

    public void visit(VeraIfElseConstraint obj)
    {
        fmt.printToken("if");
        fmt.printSpace();
        fmt.printLeadingToken("(");
        printExpression(obj.getIfExpression());
        fmt.printTrailingToken(")");

        final VeraExpression thenExpr = obj.getThenExpression();
        printExpression(thenExpr);
        if (!endsWithSet(thenExpr))
        {
            fmt.printTrailingToken(";");
        }

        final VeraExpression elseExpr = obj.getElseExpression();
        if (elseExpr != null)
        {
            fmt.printToken("else");
            fmt.printSpace();
            printExpression(elseExpr);
            if (!endsWithSet(elseExpr))
            {
                fmt.printTrailingToken(";");
            }
        }
    }

    public void visit(VeraImplicationConstraint obj)
    {
        printInfix(obj, "=>", 14);
    }

    public void visit(VeraInSet obj)
    {
        printSet(obj, "in");
    }

    public void visit(VeraInstanceCreation obj)
    {
        fmt.printToken("new");
        printArgs(obj.getArguments(), true);
    }

    public void visit(VeraIntegerLiteral obj)
    {
        fmt.printToken(Integer.toString(obj.getValue()));
    }

    public void visit(VeraInterfaceReference obj)
    {
        printName(obj.getInterface().getName(), false);
    }

    public void visit(VeraLeftShift obj)
    {
        printInfix(obj, "<<", 5);
    }

    public void visit(VeraLess obj)
    {
        printInfix(obj, "<", 6);
    }

    public void visit(VeraLessOrEqual obj)
    {
        printInfix(obj, "<=", 6);
    }

    public void visit(VeraLogicalAnd obj)
    {
        printInfix(obj, "&&", 11);
    }

    public void visit(VeraLogicalNegative obj)
    {
        printUnary(obj, "!", 2, true);
    }

    public void visit(VeraLogicalOr obj)
    {
        printInfix(obj, "||", 12);
    }

    public void visit(VeraMemberAccess obj)
    {
        printExpression(obj.getObject(), 0);
        fmt.printLeadingToken(".");
        NamedObject member = (NamedObject) obj.getMember();
        printID(member.getName());
    }

    public void visit(VeraModulo obj)
    {
        printInfix(obj, "%", 3);
    }

    public void visit(VeraMultiply obj)
    {
        printInfix(obj, "*", 3);
    }

    public void visit(VeraNotAndReduction obj)
    {
        printUnary(obj, "~&", 2, true);
    }

    public void visit(VeraNotEqual obj)
    {
        printInfix(obj, "!=", 7);
    }

    public void visit(VeraNotInSet obj)
    {
        printSet(obj, "!in");
    }

    public void visit(VeraNotOrReduction obj)
    {
        printUnary(obj, "~|", 2, true);
    }

    public void visit(VeraNotXorReduction obj)
    {
        printUnary(obj, "~^", 2, true);
    }

    public void visit(VeraNullLiteral obj)
    {
        fmt.printToken("null");
    }

    public void visit(VeraOrReduction obj)
    {
        printUnary(obj, "|", 2, true);
    }

    public void visit(VeraPostDecrement obj)
    {
        printUnary(obj, "--", 1, false);
    }

    public void visit(VeraPostIncrement obj)
    {
        printUnary(obj, "++", 1, false);
    }

    public void visit(VeraPreDecrement obj)
    {
        printUnary(obj, "--", 1, true);
    }

    public void visit(VeraPreIncrement obj)
    {
        printUnary(obj, "++", 1, true);
    }

    public void visit(VeraReplication obj)
    {
        fmt.printLeadingToken("{");
        fmt.printSpace();
        printExpression(obj.getOperand(0));
        fmt.printSpace();

        VeraExpression op2 = obj.getOperand(1);
        boolean op2concat = op2 instanceof VeraConcatenation;
        if (!op2concat)
        {
            fmt.printLeadingToken("{");
            fmt.printSpace();
        }
        printExpression(op2);
        if (!op2concat)
        {
            fmt.printSpace();
            fmt.printTrailingToken("}");
        }

        fmt.printSpace();
        fmt.printTrailingToken("}");
    }

    public void visit(VeraRightShift obj)
    {
        printInfix(obj, ">>", 5);
    }

    public void visit(VeraSignalReference obj)
    {
        VeraInterfaceSignal signal = obj.getSignal();
        StructuredType intf = signal.getStructuredType();
        printName(intf.getName(), false);
        fmt.printLeadingToken(".");
        printID(signal.getName());
    }

    public void visit(VeraStringLiteral obj)
    {
        fmt.printToken('"' + VeraStringLiteral.escape(obj.getValue()) + '"');
    }

    public void visit(VeraSubtract obj)
    {
        printInfix(obj, "-", 4);
    }

    public void visit(VeraSuperReference obj)
    {
        fmt.printToken("super");
    }

    public void visit(VeraSystemClockReference obj)
    {
        fmt.printToken("CLOCK");
    }

    public void visit(VeraThisReference obj)
    {
        fmt.printToken("this");
    }

    public void visit(VeraVariableReference obj)
    {
        printName(obj.getVariable().getName(), false);
    }

    public void visit(VeraVoidLiteral obj)
    {
        fmt.printToken("void");
    }

    public void visit(VeraWildEqual obj)
    {
        printInfix(obj, "=?=", 7);
    }

    public void visit(VeraWildNotEqual obj)
    {
        printInfix(obj, "!?=", 7);
    }

    public void visit(VeraXorReduction obj)
    {
        printUnary(obj, "^", 2, true);
    }
}
