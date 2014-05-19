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

import com.newisys.langschema.Name;
import com.newisys.langschema.Namespace;

/**
 * Represents a Vera name.
 * 
 * @author Trevor Robinson
 */
public final class VeraName
    implements Name
{
    private static final long serialVersionUID = 3258135743162431024L;

    private final String identifier;
    private final VeraNameKind kind;
    private final Namespace namespace;

    public VeraName(String identifier, VeraNameKind kind, Namespace namespace)
    {
        this.identifier = identifier;
        this.kind = kind;
        this.namespace = namespace;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public VeraNameKind getKind()
    {
        return kind;
    }

    public Namespace getNamespace()
    {
        return namespace;
    }

    public String getCanonicalName()
    {
        return (namespace != null) ? namespace.getName().getCanonicalName()
            + "::" + identifier : identifier;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof VeraName)
        {
            VeraName other = (VeraName) obj;
            return identifier.equals(other.identifier)
                && kind == other.kind
                && (namespace != null && other.namespace != null ? namespace
                    .getName().equals(other.namespace.getName())
                    : namespace == other.namespace);
        }
        return false;
    }

    public int hashCode()
    {
        return identifier.hashCode() ^ kind.hashCode()
            ^ (namespace != null ? namespace.hashCode() : 0);
    }

    public String toString()
    {
        return getCanonicalName();
    }
}
