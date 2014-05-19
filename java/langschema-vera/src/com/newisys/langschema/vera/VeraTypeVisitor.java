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

package com.newisys.langschema.vera;

/**
 * Visitor over Vera types.
 * 
 * @author Trevor Robinson
 */
public interface VeraTypeVisitor
{
    void visit(VeraAssocArrayType obj);

    void visit(VeraBitType obj);

    void visit(VeraDynamicArrayType obj);

    void visit(VeraEnumeration obj);

    void visit(VeraEventType obj);

    void visit(VeraFixedArrayType obj);

    void visit(VeraFixedBitVectorType obj);

    void visit(VeraFunctionType obj);

    void visit(VeraIntegerType obj);

    void visit(VeraInterfaceType obj);

    void visit(VeraMagicType obj);

    void visit(VeraNullType obj);

    void visit(VeraPortType obj);

    void visit(VeraStringType obj);

    void visit(VeraSystemClass obj);

    void visit(VeraUnsizedBitVectorType obj);

    void visit(VeraUserClass obj);

    void visit(VeraVoidType obj);
}
