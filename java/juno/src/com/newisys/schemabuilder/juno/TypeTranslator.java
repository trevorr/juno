/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.schemabuilder.juno;

import com.newisys.langschema.Type;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.jove.JoveFixedArrayType;
import com.newisys.langschema.vera.*;

/**
 * Schema translator for types.
 * 
 * @author Trevor Robinson
 */
final class TypeTranslator
    extends TranslatorModule
    implements VeraTypeVisitor
{
    private final boolean allowXZ;
    private final boolean statefulString;
    private JavaType type;

    public TypeTranslator(
        TranslatorModule xlatContext,
        boolean allowXZ,
        boolean statefulString)
    {
        super(xlatContext);
        this.allowXZ = allowXZ;
        this.statefulString = statefulString;
    }

    public JavaType getType()
    {
        assert (type != null);
        return type;
    }

    static JavaType translateArrayType(
        TranslatorModule module,
        VeraArrayType arrayType,
        JavaType elemType)
    {
        if (arrayType instanceof VeraAssocArrayType)
        {
            return TypeTranslator.translateAssocArrayType(module,
                (VeraAssocArrayType) arrayType, elemType);
        }
        else if (arrayType instanceof VeraDynamicArrayType)
        {
            return TypeTranslator.translateDynamicArrayType(module,
                (VeraDynamicArrayType) arrayType, elemType);
        }
        else
        {
            assert (arrayType instanceof VeraFixedArrayType);
            return TypeTranslator.translateFixedArrayType(
                (VeraFixedArrayType) arrayType, elemType);
        }
    }

    public void visit(VeraAssocArrayType obj)
    {
        JavaType elemType = translateType(obj.getElementType());
        type = translateAssocArrayType(this, obj, elemType);
    }

    static JavaType translateAssocArrayType(
        TranslatorModule module,
        VeraAssocArrayType arrayType,
        JavaType elemType)
    {
        Type[] indexTypes = arrayType.getIndexTypes();
        assert (indexTypes.length == 1);
        JavaRawClass baseClass;
        if (indexTypes[0] instanceof VeraStringType)
        {
            baseClass = module.types.stringObjectAssocArrayType;
        }
        else
        {
            assert (indexTypes[0] instanceof VeraBitVectorType);
            baseClass = module.types.bitObjectAssocArrayType;
        }
        return new JoveAssocArrayType(baseClass, elemType);
    }

    public void visit(VeraBitType obj)
    {
        if (allowXZ)
        {
            type = schema.bitType;
        }
        else
        {
            type = schema.booleanType;
        }
    }

    public void visit(VeraDynamicArrayType obj)
    {
        JavaType elemType = translateType(obj.getElementType());
        type = translateDynamicArrayType(this, obj, elemType);
    }

    static JavaType translateDynamicArrayType(
        TranslatorModule module,
        VeraDynamicArrayType arrayType,
        JavaType elemType)
    {
        Type[] indexTypes = arrayType.getIndexTypes();
        assert (indexTypes.length == 1);
        return module.schema.getArrayType(elemType, 1);
    }

    public void visit(VeraEnumeration obj)
    {
        type = translateEnum(obj);
    }

    public void visit(VeraEventType obj)
    {
        type = types.junoEventType;
    }

    public void visit(VeraFixedArrayType obj)
    {
        JavaType elemType = translateType(obj.getElementType());
        type = translateFixedArrayType(obj, elemType);
    }

    static JavaType translateFixedArrayType(
        VeraFixedArrayType arrayType,
        JavaType elemType)
    {
        int[] dimensions = arrayType.getDimensions();
        return new JoveFixedArrayType(elemType, dimensions);
    }

    public void visit(VeraFixedBitVectorType obj)
    {
        VeraDefineReference<VeraTypeDefine> defineRef = obj.getDefineRef();
        String typeDefID = null;
        if (defineRef != null)
        {
            typeDefID = defineRef.getDefine().getName().getIdentifier();
        }
        type = schema.getBitVectorType(obj.getSize(), typeDefID);
    }

    public void visit(VeraFunctionType obj)
    {
        // function types are not directly translated
        assert false;
    }

    public void visit(VeraIntegerType obj)
    {
        if (allowXZ)
        {
            type = schema.integerWrapperType;
        }
        else
        {
            type = schema.intType;
        }
    }

    public void visit(VeraInterfaceType obj)
    {
        type = translateInterface(obj);
    }

    public void visit(VeraMagicType obj)
    {
        type = schema.getObjectType();
    }

    public void visit(VeraNullType obj)
    {
        type = schema.nullType;
    }

    public void visit(VeraPortType obj)
    {
        type = translatePort(obj);
    }

    public void visit(VeraStringType obj)
    {
        type = statefulString ? types.junoStringType : schema.getStringType();
    }

    public void visit(VeraSystemClass obj)
    {
        assert (obj == obj.getVeraSchema().rootClass);
        type = types.junoObjectType;
    }

    public void visit(VeraUnsizedBitVectorType obj)
    {
        type = schema.bitVectorType;
    }

    public void visit(VeraUserClass obj)
    {
        type = translateClass(obj, false);
    }

    public void visit(VeraVoidType obj)
    {
        type = schema.voidType;
    }
}
