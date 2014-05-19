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

import com.newisys.langschema.vera.VeraPrimitiveKind;

/**
 * Bit vector type reference.
 * 
 * @author Trevor Robinson
 */
public final class BitVectorTypeRef
    extends PrimitiveTypeRef
{
    private ExpressionDecl highBitExpr;

    public BitVectorTypeRef(ExpressionDecl highBitExpr)
    {
        super(VeraPrimitiveKind.BIT_VECTOR);
        this.highBitExpr = highBitExpr;
    }

    public ExpressionDecl getHighBitExpr()
    {
        return highBitExpr;
    }

    public String toString()
    {
        return "bit[" + highBitExpr + ":0]";
    }

    public void accept(VeraSourceVisitor visitor)
    {
        visitor.visit(this);
    }
}
