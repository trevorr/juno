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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.FunctionType;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Type;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Vera function type.
 * 
 * @author Trevor Robinson
 */
public final class VeraFunctionType
    extends VeraType
    implements FunctionType
{
    private static final long serialVersionUID = 3690756198181974584L;

    private final List<VeraFunctionArgument> arguments = new LinkedList<VeraFunctionArgument>();
    private final NameTable nameTable = new NameTable();
    private boolean varArgs;
    private boolean varArgsByRef;
    private VeraType returnType;
    private boolean returnsXZ;

    public VeraFunctionType(VeraSchema schema)
    {
        super(schema);
    }

    public VeraFunctionType(VeraType returnType, boolean returnsXZ)
    {
        super(returnType.schema);
        this.returnType = returnType;
        this.returnsXZ = returnsXZ;
    }

    public List<VeraFunctionArgument> getArguments()
    {
        return arguments;
    }

    public void addArgument(VeraFunctionArgument arg)
    {
        arguments.add(arg);
        nameTable.addObject(arg);
    }

    public void renameArgument(VeraFunctionArgument arg, VeraName newName)
    {
        assert (arguments.contains(arg));
        nameTable.removeObject(arg);
        arg.setName(newName);
        nameTable.addObject(arg);
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean isVarArgs()
    {
        return varArgs;
    }

    public void setVarArgs(boolean varArgs)
    {
        this.varArgs = varArgs;
    }

    public boolean isVarArgsByRef()
    {
        return varArgsByRef;
    }

    public void setVarArgsByRef(boolean varArgsByRef)
    {
        this.varArgsByRef = varArgsByRef;
    }

    public VeraType getReturnType()
    {
        return returnType;
    }

    public void setReturnType(VeraType returnType)
    {
        this.returnType = returnType;
    }

    public boolean isReturnsXZ()
    {
        return returnsXZ;
    }

    public void setReturnsXZ(boolean returnsXZ)
    {
        this.returnsXZ = returnsXZ;
    }

    public Set< ? extends VeraType> getExceptionTypes()
    {
        return Collections.emptySet();
    }

    public boolean isAssignableFrom(Type other)
    {
        // function types are used internally and never assigned
        return false;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof VeraFunctionType)
        {
            VeraFunctionType other = (VeraFunctionType) obj;
            return returnType.equals(other.returnType);
        }
        return false;
    }

    public int hashCode()
    {
        return VeraFunctionType.class.hashCode() ^ returnType.hashCode();
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toReferenceString()
    {
        // since there is no way to "reference" a function type (which are
        // really just introduced to simplify the schema), just return the
        // source string
        return toSourceString();
    }
}
