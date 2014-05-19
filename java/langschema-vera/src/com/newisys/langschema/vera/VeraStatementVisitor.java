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
 * Visitor over Vera statements.
 * 
 * @author Trevor Robinson
 */
public interface VeraStatementVisitor
{
    void visit(VeraBlock obj);

    void visit(VeraBreakpointStatement obj);

    void visit(VeraBreakStatement obj);

    void visit(VeraContinueStatement obj);

    void visit(VeraDriveSampleStatement obj);

    void visit(VeraExpectStatement obj);

    void visit(VeraExpressionStatement obj);

    void visit(VeraForkStatement obj);

    void visit(VeraForStatement obj);

    void visit(VeraIfStatement obj);

    void visit(VeraRandCaseStatement obj);

    void visit(VeraRepeatStatement obj);

    void visit(VeraReturnStatement obj);

    void visit(VeraSwitchStatement obj);

    void visit(VeraSyncStatement obj);

    void visit(VeraTerminateStatement obj);

    void visit(VeraWhileStatement obj);
}
