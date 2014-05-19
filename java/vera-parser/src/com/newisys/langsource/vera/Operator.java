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

/**
 * Enumeration of operators.
 * 
 * @author Trevor Robinson
 */
public final class Operator
{
    private final String str;
    private final OperatorPosition position;

    private Operator(String str)
    {
        this.str = str;
        this.position = OperatorPosition.INFIX;
    }

    public Operator(String str, boolean prefix)
    {
        this.str = str;
        this.position = prefix ? OperatorPosition.PREFIX
            : OperatorPosition.POSTFIX;
    }

    public OperatorPosition getPosition()
    {
        return position;
    }

    public String toString()
    {
        return str;
    }

    // n-ary operators
    public static final Operator CONCATENATION = new Operator("{X,X}");

    // tertiary operators
    public static final Operator CONDITIONAL = new Operator("?:");

    // binary operators
    public static final Operator LOGICAL_OR = new Operator("||");
    public static final Operator LOGICAL_AND = new Operator("&&");
    public static final Operator BITWISE_OR = new Operator("|");
    public static final Operator BITWISE_NOR = new Operator("|~");
    public static final Operator BITWISE_XOR = new Operator("^");
    public static final Operator BITWISE_XNOR = new Operator("^~");
    public static final Operator BITWISE_AND = new Operator("&");
    public static final Operator BITWISE_NAND = new Operator("&~");
    public static final Operator EQUAL = new Operator("==");
    public static final Operator NOT_EQUAL = new Operator("!=");
    public static final Operator EXACT_EQUAL = new Operator("===");
    public static final Operator EXACT_NOT_EQUAL = new Operator("!==");
    public static final Operator WILD_EQUAL = new Operator("=?=");
    public static final Operator WILD_NOT_EQUAL = new Operator("!?=");
    public static final Operator LESS = new Operator("<");
    public static final Operator LESS_OR_EQUAL = new Operator("<=");
    public static final Operator GREATER = new Operator(">");
    public static final Operator GREATER_OR_EQUAL = new Operator(">=");
    public static final Operator IN = new Operator("in");
    public static final Operator NOT_IN = new Operator("!in");
    public static final Operator DIST = new Operator("dist");
    public static final Operator LEFT_SHIFT = new Operator("<<");
    public static final Operator RIGHT_SHIFT = new Operator(">>");
    public static final Operator ADD = new Operator("+");
    public static final Operator SUBTRACT = new Operator("-");
    public static final Operator MULTIPLY = new Operator("*");
    public static final Operator DIVIDE = new Operator("/");
    public static final Operator MODULO = new Operator("%");
    public static final Operator REPLICATION = new Operator("{N{X}}");

    // (binary) assignment operators
    public static final Operator ASSIGN = new Operator("=");
    public static final Operator NONBLOCKING_ASSIGN = new Operator("<=");
    public static final Operator ADD_ASSIGN = new Operator("+=");
    public static final Operator SUBTRACT_ASSIGN = new Operator("-=");
    public static final Operator MULTIPLY_ASSIGN = new Operator("*=");
    public static final Operator DIVIDE_ASSIGN = new Operator("/=");
    public static final Operator MODULO_ASSIGN = new Operator("%=");
    public static final Operator LEFT_SHIFT_ASSIGN = new Operator("<<=");
    public static final Operator RIGHT_SHIFT_ASSIGN = new Operator(">>=");
    public static final Operator AND_ASSIGN = new Operator("&=");
    public static final Operator OR_ASSIGN = new Operator("|=");
    public static final Operator XOR_ASSIGN = new Operator("^=");
    public static final Operator NAND_ASSIGN = new Operator("~&=");
    public static final Operator NOR_ASSIGN = new Operator("~|=");
    public static final Operator XNOR_ASSIGN = new Operator("~^=");

    // unary operators
    public static final Operator NEGATIVE = new Operator("-", true);
    public static final Operator LOGICAL_NEGATIVE = new Operator("!", true);
    public static final Operator BITWISE_NEGATIVE = new Operator("~", true);
    public static final Operator AND_REDUCTION = new Operator("&", true);
    public static final Operator NAND_REDUCTION = new Operator("~&", true);
    public static final Operator OR_REDUCTION = new Operator("|", true);
    public static final Operator NOR_REDUCTION = new Operator("~|", true);
    public static final Operator XOR_REDUCTION = new Operator("^", true);
    public static final Operator XNOR_REDUCTION = new Operator("~^", true);
    public static final Operator BITWISE_REVERSE = new Operator("><", true);
    public static final Operator PRE_INCREMENT = new Operator("++", true);
    public static final Operator PRE_DECREMENT = new Operator("--", true);
    public static final Operator POST_INCREMENT = new Operator("++", false);
    public static final Operator POST_DECREMENT = new Operator("--", false);
}
