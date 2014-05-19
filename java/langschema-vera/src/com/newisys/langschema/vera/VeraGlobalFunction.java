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
 * Represents a Vera global function.
 * 
 * @author Trevor Robinson
 */
public class VeraGlobalFunction
    extends VeraFunction
    implements VeraCompilationUnitMember, VeraSchemaMember
{
    private static final long serialVersionUID = 3688785869184120885L;

    private boolean export;
    private VeraCompilationUnit compUnit;

    public VeraGlobalFunction(VeraName name, VeraFunctionType funcType)
    {
        super(name, funcType);
    }

    public boolean isExport()
    {
        return export;
    }

    public void setExport(boolean export)
    {
        this.export = export;
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
