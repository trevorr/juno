/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.langsource.vera;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Operation expression.
 * 
 * @author Trevor Robinson
 */
public final class OperationDecl
    extends ExpressionDecl
{
    private Operator operator;
    private final List operands = new LinkedList(); // List<ExpressionDecl>

    public OperationDecl()
    {
    }

    public OperationDecl(Operator operator)
    {
        this.operator = operator;
    }

    public OperationDecl(Operator operator, ExpressionDecl op1)
    {
        this(operator);
        operands.add(op1);
    }

    public OperationDecl(
        Operator operator,
        ExpressionDecl op1,
        ExpressionDecl op2)
    {
        this(operator);
        operands.add(op1);
        operands.add(op2);
    }

    public OperationDecl(
        Operator operator,
        ExpressionDecl op1,
        ExpressionDecl op2,
        ExpressionDecl op3)
    {
        this(operator);
        operands.add(op1);
        operands.add(op2);
        operands.add(op3);
    }

    public Operator getOperator()
    {
        return operator;
    }

    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    public void addOperand(ExpressionDecl op)
    {
        operands.add(op);
    }

    public List getOperands()
    {
        return operands;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        if (operator == Operator.CONDITIONAL)
        {
            assert (operands.size() == 3);
            buf.append(operands.get(0));
            buf.append('?');
            buf.append(operands.get(1));
            buf.append(':');
            buf.append(operands.get(2));
        }
        else if (operator == Operator.CONCATENATION)
        {
            buf.append('{');
            Iterator iter = operands.iterator();
            while (iter.hasNext())
            {
                buf.append(iter.next());
                if (iter.hasNext())
                {
                    buf.append(',');
                }
            }
            buf.append('}');
        }
        else if (operator == Operator.REPLICATION)
        {
            assert (operands.size() == 2);
            buf.append('{');
            buf.append(operands.get(0));
            buf.append(operands.get(1));
            buf.append('}');
        }
        else
        {
            OperatorPosition pos = operator.getPosition();
            if (pos == OperatorPosition.PREFIX)
            {
                buf.append(operator);
            }
            Iterator iter = operands.iterator();
            while (iter.hasNext())
            {
                buf.append(iter.next());
                if (pos == OperatorPosition.INFIX && iter.hasNext())
                {
                    buf.append(operator);
                }
            }
            if (pos == OperatorPosition.POSTFIX)
            {
                buf.append(operator);
            }
        }
        return buf.toString();
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
