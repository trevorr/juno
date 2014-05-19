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
 * Represents a Vera user-defined class.
 * 
 * @author Trevor Robinson
 */
public final class VeraUserClass
    extends VeraClass
    implements VeraCompilationUnitMember
{
    private static final long serialVersionUID = 3689069556052276532L;

    private boolean typedefOnly;
    private boolean defined;
    private VeraCompilationUnit compUnit;

    public VeraUserClass(VeraSchema schema, VeraName name, VeraClass baseClass)
    {
        super(schema, name, baseClass);
    }

    public VeraUserClass(VeraSchema schema, VeraName name)
    {
        super(schema, name, schema.rootClass);
    }

    public void setVirtual(boolean virtual)
    {
        this.virtual = virtual;
    }

    public void setBaseClass(VeraClass baseClass)
    {
        this.baseClass = baseClass;
    }

    public void addBaseCtorArg(VeraExpression arg)
    {
        baseCtorArgs.add(arg);
    }

    public boolean isTypedefOnly()
    {
        return typedefOnly;
    }

    public void setTypedefOnly(boolean declared)
    {
        this.typedefOnly = declared;
    }

    public boolean isDefined()
    {
        return defined;
    }

    public void setDefined(boolean defined)
    {
        this.defined = defined;
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
