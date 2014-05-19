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

import com.newisys.langschema.NamedObject;

/**
 * Base class for Vera preprocessor #defines.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraDefine
    extends VeraSchemaObjectImpl
    implements NamedObject, VeraCompilationUnitMember
{
    private final VeraName name;
    private VeraVisibility visibility;
    private VeraCompilationUnit compUnit;
    private final List<VeraDefineArgument> arguments = new LinkedList<VeraDefineArgument>();
    private boolean verilogImport;

    public VeraDefine(VeraSchema schema, VeraName name)
    {
        super(schema);
        this.name = name;
        visibility = VeraVisibility.PUBLIC;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public boolean hasArguments()
    {
        return !arguments.isEmpty();
    }

    public List<VeraDefineArgument> getArguments()
    {
        return arguments;
    }

    public void addArgument(VeraDefineArgument arg)
    {
        arguments.add(arg);
    }

    public void addArguments(List<VeraDefineArgument> args)
    {
        arguments.addAll(args);
    }

    public boolean isVerilogImport()
    {
        return verilogImport;
    }

    public void setVerilogImport(boolean verilogImport)
    {
        this.verilogImport = verilogImport;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        accept((VeraCompilationUnitMemberVisitor) visitor);
    }

    public String toDebugString()
    {
        return "define " + name;
    }
}
