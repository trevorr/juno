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

import com.newisys.langschema.NamedObject;

/**
 * Represents a signal in a Vera port.
 * 
 * @author Trevor Robinson
 */
public final class VeraPortSignal
    extends VeraSchemaObjectImpl
    implements NamedObject, VeraStructuredTypeMember
{
    private static final long serialVersionUID = 3833470621362631993L;

    private final VeraName name;
    private VeraPortType port;

    public VeraPortSignal(VeraSchema schema, VeraName name)
    {
        super(schema);
        this.name = name;
    }

    public VeraName getName()
    {
        return name;
    }

    public VeraType getType()
    {
        return schema.bitVectorType;
    }

    public VeraPortType getStructuredType()
    {
        return port;
    }

    public void setPort(VeraPortType port)
    {
        this.port = port;
    }

    public VeraVisibility getVisibility()
    {
        return VeraVisibility.PUBLIC;
    }

    public void accept(VeraSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "port signal " + name;
    }
}
