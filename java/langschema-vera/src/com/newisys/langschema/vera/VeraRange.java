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

import com.newisys.langschema.Range;

/**
 * Represents a Vera bit slice range. This is represented as a separate object
 * so that it can be linked to a #define.
 * 
 * @author Trevor Robinson
 */
public class VeraRange
    extends VeraSchemaObjectImpl
    implements Range, VeraDefineReferrer<VeraRangeDefine>
{
    private static final long serialVersionUID = 3689069556002076467L;

    private final VeraExpression from;
    private final VeraExpression to;
    private VeraDefineReference<VeraRangeDefine> defineRef;

    public VeraRange(VeraSchema schema, VeraExpression from, VeraExpression to)
    {
        super(schema);
        this.from = from;
        this.to = to;
    }

    public VeraExpression getFrom()
    {
        return from;
    }

    public VeraExpression getTo()
    {
        return to;
    }

    public boolean isConstant()
    {
        return from.isConstant() && to.isConstant();
    }

    public VeraDefineReference<VeraRangeDefine> getDefineRef()
    {
        return defineRef;
    }

    public void setDefineRef(VeraDefineReference<VeraRangeDefine> defineRef)
    {
        this.defineRef = defineRef;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "range " + from + ":" + to;
    }
}
