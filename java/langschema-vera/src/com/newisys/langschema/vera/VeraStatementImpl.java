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
 * Base class for Vera statements.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraStatementImpl
    extends VeraSchemaObjectImpl
    implements VeraStatement
{
    private VeraStatement containingStatement;
    private VeraDefineReference<VeraStatementDefine> defineRef;

    public VeraStatementImpl(VeraSchema schema)
    {
        super(schema);
    }

    public final VeraStatement getContainingStatement()
    {
        return containingStatement;
    }

    public final void setContainingStatement(VeraStatement stmt)
    {
        this.containingStatement = stmt;
    }

    public VeraDefineReference<VeraStatementDefine> getDefineRef()
    {
        return defineRef;
    }

    public void setDefineRef(VeraDefineReference<VeraStatementDefine> defineRef)
    {
        this.defineRef = defineRef;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        accept((VeraStatementVisitor) visitor);
    }

    public void accept(VeraBlockMemberVisitor visitor)
    {
        accept((VeraStatementVisitor) visitor);
    }
}
