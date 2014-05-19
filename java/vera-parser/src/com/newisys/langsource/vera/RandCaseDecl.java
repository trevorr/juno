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

import java.util.LinkedList;
import java.util.List;

/**
 * Randcase statement.
 * 
 * @author Trevor Robinson
 */
public final class RandCaseDecl
    extends StatementDecl
{
    private List caseMembers; // List<RandCaseMemberDecl>

    public RandCaseDecl()
    {
        this.caseMembers = new LinkedList();
    }

    public void addCaseMember(RandCaseMemberDecl member)
    {
        caseMembers.add(member);
    }

    public List getCaseMembers()
    {
        return caseMembers;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
