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
 * Member access expression.
 * 
 * @author Trevor Robinson
 */
public final class MemberAccessDecl
    extends ExpressionDecl
{
    private ExpressionDecl objectExpr;
    private String identifier;

    public MemberAccessDecl(ExpressionDecl objectExpr)
    {
        this.objectExpr = objectExpr;
    }

    public MemberAccessDecl(ExpressionDecl objectExpr, String identifier)
    {
        this.objectExpr = objectExpr;
        this.identifier = identifier;
    }

    public ExpressionDecl getObjectExpr()
    {
        return objectExpr;
    }

    public void setObjectExpr(ExpressionDecl object)
    {
        this.objectExpr = object;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String toString()
    {
        return objectExpr + "." + identifier;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
