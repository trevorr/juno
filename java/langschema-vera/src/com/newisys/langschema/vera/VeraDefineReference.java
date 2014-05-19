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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a reference to a Vera preprocessor #define of the given kind.
 * 
 * @author Trevor Robinson
 */
public final class VeraDefineReference<T extends VeraDefine>
    extends VeraSchemaObjectImpl
{
    private static final long serialVersionUID = 3978422507789367604L;

    private final T define;
    private List<VeraExpression> arguments;

    public VeraDefineReference(T define)
    {
        super(define.schema);
        this.define = define;
    }

    public T getDefine()
    {
        return define;
    }

    public List<VeraExpression> getArguments()
    {
        if (arguments == null) return Collections.emptyList();
        return arguments;
    }

    public void addArgument(VeraExpression arg)
    {
        if (arguments == null) arguments = new LinkedList<VeraExpression>();
        arguments.add(arg);
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return define.getName() + " define ref";
    }
}
