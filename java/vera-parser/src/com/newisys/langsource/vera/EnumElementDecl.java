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
 * Enum element declaration.
 * 
 * @author Trevor Robinson
 */
public final class EnumElementDecl
    extends VeraSourceObjectImpl
{
    private String identifier;
    private ExpressionDecl value;
    private ExpressionDecl firstSuffix;
    private ExpressionDecl lastSuffix;

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public ExpressionDecl getValue()
    {
        return value;
    }

    public void setValue(ExpressionDecl value)
    {
        this.value = value;
    }

    public ExpressionDecl getFirstSuffix()
    {
        return firstSuffix;
    }

    public void setFirstSuffix(ExpressionDecl firstSuffix)
    {
        this.firstSuffix = firstSuffix;
    }

    public ExpressionDecl getLastSuffix()
    {
        return lastSuffix;
    }

    public void setLastSuffix(ExpressionDecl lastSuffix)
    {
        this.lastSuffix = lastSuffix;
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
