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

import com.newisys.langschema.SwitchStatement;

/**
 * Represents a Vera case/casex/casez statement.
 * 
 * @author Trevor Robinson
 */
public final class VeraSwitchStatement
    extends VeraStatementImpl
    implements SwitchStatement
{
    private static final long serialVersionUID = 3256721801307631669L;

    private final VeraCaseKind caseKind;
    private final VeraExpression selector;
    private final List<VeraSwitchCase> cases = new LinkedList<VeraSwitchCase>();
    private boolean gotDefaultCase;

    public VeraSwitchStatement(VeraCaseKind caseKind, VeraExpression selector)
    {
        super(selector.schema);
        this.caseKind = caseKind;
        this.selector = selector;
    }

    public VeraCaseKind getCaseKind()
    {
        return caseKind;
    }

    public VeraExpression getSelector()
    {
        return selector;
    }

    public List<VeraSwitchCase> getCases()
    {
        return cases;
    }

    public VeraSwitchValueCase newValueCase()
    {
        VeraSwitchValueCase _case = new VeraSwitchValueCase(this);
        cases.add(_case);
        return _case;
    }

    public VeraSwitchDefaultCase newDefaultCase()
    {
        assert (!gotDefaultCase);
        VeraSwitchDefaultCase _case = new VeraSwitchDefaultCase(this);
        cases.add(_case);
        gotDefaultCase = true;
        return _case;
    }

    public void accept(VeraStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "case statement";
    }
}
