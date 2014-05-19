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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Schema;
import com.newisys.langschema.util.NameTable;
import com.newisys.schemaprinter.vera.VeraSchemaPrinter;

/**
 * Represents a complete Vera schema.
 * 
 * @author Trevor Robinson
 */
public final class VeraSchema
    implements Schema
{
    private static final long serialVersionUID = 3256719580792632881L;

    private final List<VeraSchemaMember> members = new LinkedList<VeraSchemaMember>();
    private final NameTable nameTable = new NameTable();

    private boolean useSourceString = true;
    private transient VeraSchemaPrinter defaultPrinter;

    // define schema-singleton primitive type objects
    public final VeraBitType bitType = new VeraBitType(this);
    public final VeraFixedBitVectorType bitVector32Type = new VeraFixedBitVectorType(
        this, 32);
    public final VeraFixedBitVectorType bitVector64Type = new VeraFixedBitVectorType(
        this, 64);
    public final VeraUnsizedBitVectorType bitVectorType = new VeraUnsizedBitVectorType(
        this);
    public final VeraEventType eventType = new VeraEventType(this);
    public final VeraIntegerType integerType = new VeraIntegerType(this);
    public final VeraMagicType magicType = new VeraMagicType(this);
    public final VeraNullType nullType = new VeraNullType(this);
    public final VeraVoidType voidType = new VeraVoidType(this);

    // define schema-singleton primitive class objects
    public final VeraRootClass rootClass = new VeraRootClass(this);
    public final VeraStringType stringType = new VeraStringType(this, rootClass);
    {
        rootClass.defineRootClass();
        stringType.defineStringClass();
    }

    public List<VeraSchemaMember> getMembers()
    {
        return members;
    }

    public void addMember(VeraSchemaMember member)
    {
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean isUseSourceString()
    {
        return useSourceString;
    }

    public void setUseSourceString(boolean useSourceString)
    {
        this.useSourceString = useSourceString;
    }

    public VeraSchemaPrinter getDefaultPrinter()
    {
        if (defaultPrinter == null)
        {
            defaultPrinter = new VeraSchemaPrinter();
            defaultPrinter.setCollapseBodies(true);
        }
        return defaultPrinter;
    }

    public void setDefaultPrinter(VeraSchemaPrinter printer)
    {
        defaultPrinter = printer;
    }
}
