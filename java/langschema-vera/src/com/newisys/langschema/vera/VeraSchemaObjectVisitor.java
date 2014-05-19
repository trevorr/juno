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
 * Visitor over all first-class Vera schema objects.
 * 
 * @author Trevor Robinson
 */
public interface VeraSchemaObjectVisitor
    extends VeraSchemaMemberVisitor, VeraCompilationUnitMemberVisitor,
    VeraClassMemberVisitor, VeraBlockMemberVisitor, VeraExpressionVisitor,
    VeraTypeVisitor
{
    void visit(VeraBindMember obj);

    void visit(VeraDefineArgument obj);

    void visit(VeraDefineReference obj);

    void visit(VeraFunctionArgument obj);

    void visit(VeraInterfaceSignal obj);

    void visit(VeraPortSignal obj);

    void visit(VeraRange obj);
}
