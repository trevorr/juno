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

import com.newisys.langschema.java.JavaExpression;

/**
 * Interface for building various Java arithmetic operation expressions.
 * 
 * @author Trevor Robinson
 */
interface ArithmeticOperationBuilder
{
    JavaExpression add(JavaExpression op1, JavaExpression op2);

    JavaExpression subtract(JavaExpression op1, JavaExpression op2);

    JavaExpression multiply(JavaExpression op1, JavaExpression op2);

    JavaExpression divide(JavaExpression op1, JavaExpression op2);

    JavaExpression mod(JavaExpression op1, JavaExpression op2);

    JavaExpression negate(JavaExpression op1);

    JavaExpression and(JavaExpression op1, JavaExpression op2);

    JavaExpression andNot(JavaExpression op1, JavaExpression op2);

    JavaExpression or(JavaExpression op1, JavaExpression op2);

    JavaExpression orNot(JavaExpression op1, JavaExpression op2);

    JavaExpression xor(JavaExpression op1, JavaExpression op2);

    JavaExpression xorNot(JavaExpression op1, JavaExpression op2);

    JavaExpression not(JavaExpression op1);

    JavaExpression reverse(JavaExpression op1);

    JavaExpression reductiveAnd(JavaExpression op1);

    JavaExpression reductiveAndNot(JavaExpression op1);

    JavaExpression reductiveOr(JavaExpression op1);

    JavaExpression reductiveOrNot(JavaExpression op1);

    JavaExpression reductiveXor(JavaExpression op1);

    JavaExpression reductiveXorNot(JavaExpression op1);
}
