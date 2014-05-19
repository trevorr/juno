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

import com.newisys.langschema.Name;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.Scope;
import com.newisys.langschema.Variable;
import com.newisys.langschema.constraint.*;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaInterface;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaStructuredTypeMember;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaVariableModifier;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.langschema.jove.JoveFixedArrayType;

/**
 * Generates a string representation of random constraint expression.
 * 
 * @author Trevor Robinson
 */
final class ConsExpressionPrinter
    implements ConsConstraintExpressionVisitor
{
    private final JavaStructuredType typeContext;
    private final int parentPrecedence;
    private final StringBuffer buf;

    private ConsExpressionPrinter(
        JavaStructuredType typeContext,
        int parentPrecedence,
        StringBuffer buf)
    {
        this.typeContext = typeContext;
        this.parentPrecedence = parentPrecedence;
        this.buf = buf;
    }

    public static String print(
        ConsExpression expr,
        JavaStructuredType typeContext)
    {
        StringBuffer buf = new StringBuffer();
        ConsExpressionPrinter printer = new ConsExpressionPrinter(typeContext,
            100, buf);
        if (expr instanceof ConsConstraintSet)
        {
            // suppress outer braces for top-level constraint block
            printer.printConstraints((ConsConstraintSet) expr);
        }
        else
        {
            expr.accept(printer);
        }
        return buf.toString();
    }

    private void printExpression(ConsExpression expr)
    {
        printExpression(expr, 100);
    }

    private void printExpression(ConsExpression expr, int parentPrecedence)
    {
        ConsExpressionPrinter printer = new ConsExpressionPrinter(typeContext,
            parentPrecedence, buf);
        expr.accept(printer);
    }

    void printName(Name name)
    {
        String id = name.getIdentifier();
        Namespace namespace = name.getNamespace();
        JavaPackage pkg = typeContext.getPackage();
        if (namespace != null
            && namespace != pkg
            && (!namespace.getName().getCanonicalName().equals("java.lang") || pkg
                .lookupObjects(id, name.getKind()).hasNext()))
        {
            // print qualified name if all of the following are true:
            // a) name is not in default package
            // b) name is not in current package
            // c) name is not from java.lang, or is from java.lang but is hidden
            printNamespace(namespace);
        }
        buf.append(id);
    }

    private void printNamespace(Namespace namespace)
    {
        printNameNoImport(namespace.getName());
        buf.append('.');
    }

    private void printNameNoImport(Name name)
    {
        Namespace namespace = name.getNamespace();
        if (namespace != null)
        {
            printNamespace(namespace);
        }
        buf.append(name.getIdentifier());
    }

    void printType(JavaType type)
    {
        if (type instanceof JavaArrayType)
        {
            JavaArrayType arrayType = (JavaArrayType) type;
            printType(arrayType.getElementType());

            int[] dims;
            int dimCount;
            if (type instanceof JoveFixedArrayType)
            {
                dims = ((JoveFixedArrayType) type).getDimensions();
                dimCount = dims.length;
            }
            else
            {
                dims = null;
                dimCount = arrayType.getIndexTypes().length;
            }

            for (int i = 0; i < dimCount; ++i)
            {
                buf.append('[');
                if (dims != null)
                {
                    buf.append("/*" + dims[i] + "*/");
                }
                buf.append(']');
            }
        }
        else if (type instanceof JoveAssocArrayType)
        {
            JoveAssocArrayType assocType = (JoveAssocArrayType) type;
            printName(assocType.getBaseClass().getName());
            String elemName = assocType.getElementType().toReferenceString();
            buf.append("/*" + elemName + "*/");
        }
        else if (type instanceof JoveBitVectorType)
        {
            JoveBitVectorType bvType = (JoveBitVectorType) type;
            printName(bvType.getBaseClass().getName());
            buf.append("/*" + bvType.getSize() + "*/");
        }
        else if (type instanceof NamedObject)
        {
            printName(((NamedObject) type).getName());
        }
        else
        {
            buf.append(type.toReferenceString());
        }
    }

    private void printUnary(ConsUnaryOperation op, String token, boolean prefix)
    {
        final int precedence = 1;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) buf.append('(');

        if (prefix) buf.append(token);

        ConsExpression op1 = op.getOperand(0);
        printExpression(op1, precedence - (prefix ? 0 : 1));

        if (!prefix) buf.append(token);

        if (needParens) buf.append(')');
    }

    private void printInfix(ConsBinaryOperation op, String token, int precedence)
    {
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) buf.append('(');

        ConsExpression op1 = op.getOperand(0);
        printExpression(op1, precedence);

        buf.append(' ');
        buf.append(token);
        buf.append(' ');

        ConsExpression op2 = op.getOperand(1);
        printExpression(op2, precedence - 1);

        if (needParens) buf.append(')');
    }

    private void checkQualify(JavaStructuredTypeMember member, boolean isStatic)
    {
        JavaStructuredType memberType = member.getStructuredType();
        if (!memberInScope(member, memberType, typeContext))
        {
            printType(memberType);
            buf.append('.');
            if (!isStatic)
            {
                buf.append("this");
                buf.append('.');
            }
        }
    }

    private boolean memberInScope(
        JavaStructuredTypeMember member,
        JavaStructuredType memberType,
        JavaStructuredType fromType)
    {
        if (memberType == fromType)
        {
            // member of same class?
            return true;
        }
        else if (containsNameOf(fromType, (NamedObject) member))
        {
            // hidden by member in base/nested type
            return false;
        }

        if (memberType instanceof JavaInterface)
        {
            // member of implemented interface?
            JavaInterface memberIntf = (JavaInterface) memberType;
            if (fromType.implementsInterface(memberIntf))
            {
                return true;
            }
        }

        if (memberType instanceof JavaRawAbstractClass
            && fromType instanceof JavaRawAbstractClass)
        {
            // member of superclass?
            JavaRawAbstractClass memberClass = (JavaRawAbstractClass) memberType;
            JavaRawAbstractClass classContext = (JavaRawAbstractClass) fromType;
            if (memberClass.isSuperclassOf(classContext))
            {
                return true;
            }
        }

        // in scope for containing class?
        JavaStructuredType outerType = fromType.getStructuredType();
        if (outerType != null)
        {
            return memberInScope(member, memberType, outerType);
        }

        return false;
    }

    private boolean containsNameOf(Scope scope, NamedObject obj)
    {
        Name name = obj.getName();
        Iterator iter = scope.lookupObjects(name.getIdentifier(), name
            .getKind());
        return iter.hasNext();
    }

    public void visit(ConsBitVectorLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsDistSet obj)
    {
        printSetOp(obj, "dist");
    }

    public void visit(ConsInSet obj)
    {
        printSetOp(obj, obj instanceof ConsNotInSet ? "!in" : "in");
    }

    private void printSetOp(ConsSetOperation obj, String keyword)
    {
        final int precedence = 5;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) buf.append("(");

        ConsExpression expr = obj.getExpr();
        printExpression(expr, precedence);

        buf.append(' ');
        buf.append(keyword);
        buf.append(' ');

        buf.append("{ ");
        Iterator iter = obj.getMembers().iterator();
        while (iter.hasNext())
        {
            ConsSetMember member = (ConsSetMember) iter.next();
            member.accept(this);
            if (iter.hasNext())
            {
                buf.append(", ");
            }
        }
        buf.append(" }");

        if (needParens) buf.append(")");
    }

    public void visit(ConsImplication obj)
    {
        printExpression(obj.getOperand(0));
        buf.append(" => ");
        printExpression(obj.getOperand(1));
    }

    public void visit(ConsSetRange obj)
    {
        printExpression(obj.getLow());
        buf.append(':');
        printExpression(obj.getHigh());
        printWeight(obj);
    }

    public void visit(ConsSetValue obj)
    {
        printExpression(obj.getValue());
        printWeight(obj);
    }

    private void printWeight(ConsSetMember obj)
    {
        ConsExpression weight = obj.getWeight();
        if (weight != null)
        {
            buf.append(obj.isWeightPerItem() ? " := " : " :/ ");
            printExpression(weight);
        }
    }

    public void visit(ConsAdd obj)
    {
        printInfix(obj, "+", 3);
    }

    public void visit(ConsAnd obj)
    {
        printInfix(obj, "&", 9);
    }

    public void visit(ConsArrayAccess obj)
    {
        printExpression((ConsExpression) obj.getArray(), 0);
        Iterator iter = obj.getIndices().iterator();
        while (iter.hasNext())
        {
            buf.append('[');
            printExpression((ConsExpression) iter.next());
            buf.append(']');
        }
    }

    public void visit(ConsBitwiseNot obj)
    {
        printUnary(obj, "~", true);
    }

    public void visit(ConsBooleanLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsCharLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsConditional obj)
    {
        final int precedence = 13;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) buf.append('(');

        ConsExpression op1 = obj.getOperand(0);
        printExpression(op1, precedence - 1);

        buf.append(" ? ");

        ConsExpression op2 = obj.getOperand(1);
        printExpression(op2, precedence);

        buf.append(" : ");

        ConsExpression op3 = obj.getOperand(2);
        printExpression(op3, precedence);

        if (needParens) buf.append(')');
    }

    public void visit(ConsConditionalAnd obj)
    {
        printInfix(obj, "&&", 11);
    }

    public void visit(ConsConditionalOr obj)
    {
        printInfix(obj, "||", 12);
    }

    public void visit(ConsConstraintSet obj)
    {
        buf.append("{ ");
        printConstraints(obj);
        buf.append(" }");
    }

    private void printConstraints(ConsConstraintSet obj)
    {
        final Iterator iter = obj.getExprs().iterator();
        while (iter.hasNext())
        {
            ConsExpression expr = (ConsExpression) iter.next();
            printExpression(expr);
            if (!endsWithSet(expr))
            {
                buf.append(';');
            }
            if (iter.hasNext())
            {
                buf.append(' ');
            }
        }
    }

    private boolean endsWithSet(ConsExpression expr)
    {
        if (expr instanceof ConsConstraintSet)
        {
            return true;
        }
        else if (expr instanceof ConsImplication)
        {
            return endsWithSet(((ConsImplication) expr).getOperand(1));
        }
        return false;
    }

    public void visit(ConsDivide obj)
    {
        printInfix(obj, "/", 2);
    }

    public void visit(ConsDoubleLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsEqual obj)
    {
        printInfix(obj, "==", 7);
    }

    public void visit(ConsFloatLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsGreater obj)
    {
        printInfix(obj, ">", 5);
    }

    public void visit(ConsGreaterOrEqual obj)
    {
        printInfix(obj, ">=", 5);
    }

    public void visit(ConsIntLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsLeftShift obj)
    {
        printInfix(obj, "<<", 4);
    }

    public void visit(ConsLess obj)
    {
        printInfix(obj, "<", 5);
    }

    public void visit(ConsLessOrEqual obj)
    {
        printInfix(obj, "<=", 5);
    }

    public void visit(ConsLogicalNot obj)
    {
        printUnary(obj, "!", true);
    }

    public void visit(ConsLongLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsMemberAccess obj)
    {
        printExpression((ConsExpression) obj.getObject(), 0);
        buf.append('.');
        NamedObject member = (NamedObject) obj.getMember();
        buf.append(member.getName().getIdentifier());
    }

    public void visit(ConsModulo obj)
    {
        printInfix(obj, "%", 2);
    }

    public void visit(ConsMultiply obj)
    {
        printInfix(obj, "*", 2);
    }

    public void visit(ConsNotEqual obj)
    {
        printInfix(obj, "!=", 7);
    }

    public void visit(ConsNullLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsOr obj)
    {
        printInfix(obj, "|", 10);
    }

    public void visit(ConsSignedRightShift obj)
    {
        printInfix(obj, ">>", 4);
    }

    public void visit(ConsStringLiteral obj)
    {
        buf.append(obj.toString());
    }

    public void visit(ConsSubtract obj)
    {
        printInfix(obj, "-", 3);
    }

    public void visit(ConsSuperReference obj)
    {
        JavaType type = (JavaType) obj.getType();
        if (type != typeContext)
        {
            printType(type);
            buf.append('.');
        }
        buf.append("super");
    }

    public void visit(ConsThisReference obj)
    {
        JavaType type = obj.getType();
        if (type != typeContext)
        {
            printType(type);
            buf.append('.');
        }
        buf.append("this");
    }

    public void visit(ConsTypeLiteral obj)
    {
        printType(obj.getType());
        buf.append(".class");
    }

    public void visit(ConsUnaryMinus obj)
    {
        printUnary(obj, "-", true);
    }

    public void visit(ConsUnaryPlus obj)
    {
        printUnary(obj, "+", true);
    }

    public void visit(ConsUnsignedRightShift obj)
    {
        printInfix(obj, ">>>", 4);
    }

    public void visit(ConsVariableReference obj)
    {
        Variable var = obj.getVariable();
        if (var instanceof JavaMemberVariable)
        {
            checkQualify((JavaMemberVariable) var, var.getModifiers().contains(
                JavaVariableModifier.STATIC));
        }
        buf.append(var.getName().getIdentifier());
    }

    public void visit(ConsXor obj)
    {
        printInfix(obj, "^", 9);
    }
}
