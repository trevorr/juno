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
 * Represents a Vera global variable.
 * 
 * @author Trevor Robinson
 */
public class VeraGlobalVariable
    extends VeraVariable<VeraVariableModifier>
    implements VeraCompilationUnitMember
{
    private static final long serialVersionUID = 3834876862458507832L;

    private boolean extern;
    private VeraCompilationUnit compUnit;

    public VeraGlobalVariable(VeraName name, VeraType type)
    {
        super(name, type);
    }

    public boolean isExtern()
    {
        return extern;
    }

    public void setExtern(boolean extern)
    {
        this.extern = extern;
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public VeraVisibility getVisibility()
    {
        return VeraVisibility.PUBLIC;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
