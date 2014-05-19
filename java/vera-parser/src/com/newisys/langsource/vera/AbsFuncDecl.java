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
 * Abstract function declaration.
 * 
 * @author Trevor Robinson
 */
public abstract class AbsFuncDecl
    extends VeraSourceObjectImpl
{
    private String identifier;
    private TypeRef returnType;
    private List params; // List<ParamDecl>
    private boolean export;
    private boolean extern;

    public AbsFuncDecl()
    {
        this.params = new LinkedList();
        this.export = false;
        this.extern = false;
    }

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public final TypeRef getReturnType()
    {
        return returnType;
    }

    public final void setReturnType(TypeRef returnType)
    {
        this.returnType = returnType;
    }

    public final List getParams()
    {
        return params;
    }

    public final boolean isExport()
    {
        return export;
    }

    public final void setExport(boolean export)
    {
        this.export = export;
    }

    public final boolean isExtern()
    {
        return extern;
    }

    public final void setExtern(boolean extern)
    {
        this.extern = extern;
    }
}
