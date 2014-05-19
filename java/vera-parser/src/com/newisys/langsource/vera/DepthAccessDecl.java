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

/**
 * Signal back-reference expression.
 * 
 * @author Trevor Robinson
 */
public final class DepthAccessDecl
    extends ExpressionDecl
{
    private ExpressionDecl signalExpr;
    private int depth;

    public DepthAccessDecl(ExpressionDecl objectExpr)
    {
        this.signalExpr = objectExpr;
    }

    public DepthAccessDecl(ExpressionDecl objectExpr, int depth)
    {
        this.signalExpr = objectExpr;
        this.depth = depth;
    }

    public ExpressionDecl getSignalExpr()
    {
        return signalExpr;
    }

    public void setSignalExpr(ExpressionDecl object)
    {
        this.signalExpr = object;
    }

    public int getDepth()
    {
        return depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public String toString()
    {
        return signalExpr + "." + depth;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
