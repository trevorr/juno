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
 * Class variable declaration.
 * 
 * @author Trevor Robinson
 */
public final class ClassVarDecl
    extends VarDecl
    implements ClassDeclMember
{
    private VeraVisibility visibility;
    private ExpressionDecl randomSizeExpr;
    private boolean staticVar;
    private RandMode randMode;
    private boolean packed;
    private boolean bigEndian;
    private boolean bitReverse;

    public ClassVarDecl()
    {
        this.staticVar = false;
        this.visibility = VeraVisibility.PUBLIC;
        this.randMode = RandMode.NON_RAND;
        this.packed = false;
        this.bigEndian = false;
        this.bitReverse = false;
    }

    public VeraVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(VeraVisibility visibility)
    {
        this.visibility = visibility;
    }

    public final ExpressionDecl getRandomSizeExpr()
    {
        return randomSizeExpr;
    }

    public final void setRandomSizeExpr(ExpressionDecl randomSizeExpr)
    {
        this.randomSizeExpr = randomSizeExpr;
    }

    public boolean isStaticVar()
    {
        return staticVar;
    }

    public void setStaticVar(boolean staticVar)
    {
        this.staticVar = staticVar;
    }

    public RandMode getRandMode()
    {
        return randMode;
    }

    public void setRandMode(RandMode randMode)
    {
        this.randMode = randMode;
    }

    public boolean isPacked()
    {
        return packed;
    }

    public void setPacked(boolean packed)
    {
        this.packed = packed;
    }

    public boolean isBigEndian()
    {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian)
    {
        this.bigEndian = bigEndian;
    }

    public boolean isBitReverse()
    {
        return bitReverse;
    }

    public void setBitReverse(boolean bitReverse)
    {
        this.bitReverse = bitReverse;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
