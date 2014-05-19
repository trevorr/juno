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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.langschema.Enumeration;
import com.newisys.langschema.EnumerationElement;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Type;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Vera enumeration type.
 * 
 * @author Trevor Robinson
 */
public final class VeraEnumeration
    extends VeraComplexType
    implements Enumeration, VeraClassMember, VeraCompilationUnitMember
{
    private static final long serialVersionUID = 3256438110228134201L;

    private final List<VeraEnumerationElement> members = new LinkedList<VeraEnumerationElement>();
    private final Map<Integer, EnumerationElement> values = new HashMap<Integer, EnumerationElement>();
    private final NameTable nameTable = new NameTable();
    private VeraClass cls;
    private VeraCompilationUnit compUnit;

    public VeraEnumeration(VeraSchema schema, VeraName name)
    {
        super(schema, name);
    }

    public List<VeraEnumerationElement> getMembers()
    {
        return members;
    }

    public void addMember(VeraEnumerationElement element)
    {
        members.add(element);
        values.put(new Integer(element.getValue()), element);
        nameTable.addObject(element);
    }

    public EnumerationElement lookupValue(int value)
    {
        return values.get(value);
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public VeraClass getStructuredType()
    {
        return cls;
    }

    public void setClass(VeraClass cls)
    {
        this.cls = cls;
    }

    public VeraCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

    public void setCompilationUnit(VeraCompilationUnit compUnit)
    {
        this.compUnit = compUnit;
    }

    public boolean isAssignableFrom(Type other)
    {
        return other == this;
    }

    public boolean isIntegralConvertible()
    {
        return true;
    }

    public int getBitCount()
    {
        return 32;
    }

    public void accept(VeraCompilationUnitMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraClassMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(VeraTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "enum " + name;
    }
}
