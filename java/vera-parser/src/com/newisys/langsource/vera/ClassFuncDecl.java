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

import com.newisys.langschema.vera.VeraVisibility;

/**
 * Class function declaration.
 * 
 * @author Trevor Robinson
 */
public final class ClassFuncDecl
    extends VeraFuncDecl
    implements ClassDeclMember
{
    private boolean virtual;
    private VeraVisibility visibility;

    public ClassFuncDecl()
    {
        this.virtual = false;
        this.visibility = VeraVisibility.PUBLIC;
    }

    public boolean isVirtual()
    {
        return virtual;
    }

    public void setVirtual(boolean virtual)
    {
        this.virtual = virtual;
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
