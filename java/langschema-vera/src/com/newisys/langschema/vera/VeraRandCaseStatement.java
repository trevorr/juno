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

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Vera randcase statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraRandCaseStatement
    extends VeraStatementImpl
{
    private static final long serialVersionUID = 3258128050893107250L;

    private final List<VeraRandCase> caseList = new LinkedList<VeraRandCase>();

    public VeraRandCaseStatement(VeraSchema schema)
    {
        super(schema);
    }

    public List<VeraRandCase> getCaseList()
    {
        return caseList;
    }

    public void addCase(VeraRandCase randCase)
    {
        randCase.statement.setContainingStatement(this);
        caseList.add(randCase);
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "randcase statement";
    }
}
