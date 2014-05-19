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
 * Class declaration.
 * 
 * @author Trevor Robinson
 */
public final class ClassDecl
    extends VeraSourceObjectImpl
    implements CompilationUnitDeclMember
{
    private String identifier;
    private boolean extern;
    private boolean virtual;
    private boolean local;
    private String baseClassIdentifer;
    private List baseCtorArgs; // List<ExpressionDecl>
    private List classEnums; // List<EnumDecl>
    private List classVars; // List<ClassVarDecl>
    private List classConstraints; // List<ClassConstraintDecl>
    private List classFuncs; // List<ClassFuncDecl>

    public ClassDecl()
    {
        super();
        this.extern = false;
        this.virtual = false;
        this.local = false;
        this.baseCtorArgs = new LinkedList();
        this.classEnums = new LinkedList();
        this.classVars = new LinkedList();
        this.classConstraints = new LinkedList();
        this.classFuncs = new LinkedList();
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public boolean isExtern()
    {
        return extern;
    }

    public void setExtern(boolean extern)
    {
        this.extern = extern;
    }

    public boolean isVirtual()
    {
        return virtual;
    }

    public void setVirtual(boolean virtual)
    {
        this.virtual = virtual;
    }

    public String getBaseClassIdentifer()
    {
        return baseClassIdentifer;
    }

    public boolean isLocal()
    {
        return local;
    }

    public void setLocal(boolean local)
    {
        this.local = local;
    }

    public void setBaseClassIdentifer(String baseClassIdentifer)
    {
        this.baseClassIdentifer = baseClassIdentifer;
    }

    public void addBaseCtorArg(ExpressionDecl arg)
    {
        baseCtorArgs.add(arg);
    }

    public List getBaseCtorArgs()
    {
        return baseCtorArgs;
    }

    public void addClassEnum(EnumDecl enumeration)
    {
        classEnums.add(enumeration);
    }

    public List getClassEnums()
    {
        return classEnums;
    }

    public void addClassVar(ClassVarDecl var)
    {
        classVars.add(var);
    }

    public List getClassVars()
    {
        return classVars;
    }

    public void addClassConstraint(ClassConstraintDecl cons)
    {
        classConstraints.add(cons);
    }

    public List getClassConstraints()
    {
        return classConstraints;
    }

    public void addClassFunc(ClassFuncDecl func)
    {
        classFuncs.add(func);
    }

    public List getClassFuncs()
    {
        return classFuncs;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
