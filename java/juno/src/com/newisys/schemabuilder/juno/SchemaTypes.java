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

import com.newisys.dv.ClockSignal;
import com.newisys.dv.DV;
import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.dv.InOutSignal;
import com.newisys.dv.InputSignal;
import com.newisys.dv.OutputSignal;
import com.newisys.dv.PortSignalWrapper;
import com.newisys.dv.Signal;
import com.newisys.dv.vlogdef.Defines;
import com.newisys.eventsim.Event;
import com.newisys.juno.runtime.*;
import com.newisys.langschema.java.JavaAnnotationType;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaRawInterface;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.RandExclude;
import com.newisys.randsolver.annotation.Randc;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitRange;
import com.newisys.verilog.util.Length;

/**
 * Cache of the Java schema types used by the translator.
 * 
 * @author Trevor Robinson
 */
public final class SchemaTypes
{
    final JoveSchema schema;

    final JavaRawClass absCallErrorType;
    final JavaRawClass assocArrayType;
    final JavaRawClass bitAssocArrayType;
    final JavaRawClass bitObjectAssocArrayType;
    final JavaRawClass bitOpType;
    final JavaRawClass bitRangeType;
    final JavaRawClass bitVectorOpType;
    final JavaRawClass booleanOpType;
    final JavaRawClass classType;
    final JavaArrayType classArrayType;
    final JavaRawInterface clockSignalType;
    final JavaAnnotationType constraintType;
    final JavaRawClass definesType;
    final JavaRawClass dvAppType;
    final JavaRawClass dvSimType;
    final JavaRawClass dvType;
    final JavaRawClass edgeSetType;
    final JavaRawClass eventType;
    final JavaArrayType eventArrayType;
    final JavaRawInterface inOutSignalType;
    final JavaArrayType inOutSignalArrayType;
    final JavaRawInterface inputSignalType;
    final JavaRawClass intOpType;
    final JavaRawClass integerOpType;
    final JavaAnnotationType lengthType;
    final JavaRawClass longWrapperOpType;
    final JavaRawClass mathType;
    final JavaRawClass objectType;
    final JavaArrayType objectArrayType;
    final JavaRawInterface outputSignalType;
    final JavaRawClass portSignalWrapperType;
    final JavaAnnotationType randType;
    final JavaAnnotationType randcType;
    final JavaAnnotationType randExcludeType;
    final JavaAnnotationType randomizableType;
    final JavaRawInterface runnableType;
    final JavaRawInterface signalType;
    final JavaRawClass stringType;
    final JavaRawClass stringAssocArrayType;
    final JavaRawClass stringObjectAssocArrayType;
    final JavaRawClass stringOpType;
    final JavaRawClass unsuppOpType;
    final JavaRawClass junoType;
    final JavaRawInterface junoEnumType;
    final JavaRawClass junoEnumUtilType;
    final JavaRawClass junoEventType;
    final JavaRawClass junoEventValueFactoryType;
    final JavaRawClass junoObjectType;
    final JavaRawClass junoStringType;
    final JavaRawClass junoStringValueFactoryType;

    public SchemaTypes(JoveSchema schema)
    {
        this.schema = schema;

        absCallErrorType = (JavaRawClass) schema
            .getTypeForSystemClass(AbstractMethodCallError.class.getName());
        assocArrayType = (JavaRawClass) schema
            .getTypeForSystemClass(AssocArray.class.getName());
        bitAssocArrayType = (JavaRawClass) schema
            .getTypeForSystemClass(BitAssocArray.class.getName());
        bitObjectAssocArrayType = (JavaRawClass) schema
            .getTypeForSystemClass(BitObjectAssocArray.class.getName());
        bitOpType = (JavaRawClass) schema.getTypeForSystemClass(BitOp.class
            .getName());
        bitRangeType = (JavaRawClass) schema
            .getTypeForSystemClass(BitRange.class.getName());
        bitVectorOpType = (JavaRawClass) schema
            .getTypeForSystemClass(BitVectorOp.class.getName());
        booleanOpType = (JavaRawClass) schema
            .getTypeForSystemClass(BooleanOp.class.getName());
        classType = (JavaRawClass) schema.getTypeForSystemClass(Class.class
            .getName());
        classArrayType = schema.getArrayType(classType, 1);
        clockSignalType = (JavaRawInterface) schema
            .getTypeForSystemClass(ClockSignal.class.getName());
        constraintType = (JavaAnnotationType) schema
            .getTypeForSystemClass(Constraint.class.getName());
        definesType = (JavaRawClass) schema.getTypeForSystemClass(Defines.class
            .getName());
        dvAppType = (JavaRawClass) schema
            .getTypeForSystemClass(DVApplication.class.getName());
        dvSimType = (JavaRawClass) schema
            .getTypeForSystemClass(DVSimulation.class.getName());
        dvType = (JavaRawClass) schema
            .getTypeForSystemClass(DV.class.getName());
        edgeSetType = (JavaRawClass) schema.getTypeForSystemClass(EdgeSet.class
            .getName());
        eventType = (JavaRawClass) schema.getTypeForSystemClass(Event.class
            .getName());
        eventArrayType = schema.getArrayType(eventType, 1);
        inOutSignalType = (JavaRawInterface) schema
            .getTypeForSystemClass(InOutSignal.class.getName());
        inOutSignalArrayType = schema.getArrayType(inOutSignalType, 1);
        inputSignalType = (JavaRawInterface) schema
            .getTypeForSystemClass(InputSignal.class.getName());
        intOpType = (JavaRawClass) schema.getTypeForSystemClass(IntOp.class
            .getName());
        integerOpType = (JavaRawClass) schema
            .getTypeForSystemClass(IntegerOp.class.getName());
        lengthType = (JavaAnnotationType) schema
            .getTypeForSystemClass(Length.class.getName());
        longWrapperOpType = (JavaRawClass) schema
            .getTypeForSystemClass(LongWrapperOp.class.getName());
        mathType = (JavaRawClass) schema.getTypeForSystemClass(Math.class
            .getName());
        objectType = schema.getObjectType();
        objectArrayType = schema.getArrayType(objectType, 1);
        outputSignalType = (JavaRawInterface) schema
            .getTypeForSystemClass(OutputSignal.class.getName());
        portSignalWrapperType = (JavaRawClass) schema
            .getTypeForSystemClass(PortSignalWrapper.class.getName());
        randType = (JavaAnnotationType) schema.getTypeForSystemClass(Rand.class
            .getName());
        randcType = (JavaAnnotationType) schema
            .getTypeForSystemClass(Randc.class.getName());
        randExcludeType = (JavaAnnotationType) schema
            .getTypeForSystemClass(RandExclude.class.getName());
        randomizableType = (JavaAnnotationType) schema
            .getTypeForSystemClass(Randomizable.class.getName());
        runnableType = (JavaRawInterface) schema
            .getTypeForSystemClass(Runnable.class.getName());
        signalType = (JavaRawInterface) schema
            .getTypeForSystemClass(Signal.class.getName());
        stringType = schema.getStringType();
        stringAssocArrayType = (JavaRawClass) schema
            .getTypeForSystemClass(StringAssocArray.class.getName());
        stringObjectAssocArrayType = (JavaRawClass) schema
            .getTypeForSystemClass(StringObjectAssocArray.class.getName());
        stringOpType = (JavaRawClass) schema
            .getTypeForSystemClass(StringOp.class.getName());
        unsuppOpType = (JavaRawClass) schema
            .getTypeForSystemClass(UnsupportedOperationException.class
                .getName());
        junoType = (JavaRawClass) schema.getTypeForSystemClass(Juno.class
            .getName());
        junoEnumType = (JavaRawInterface) schema
            .getTypeForSystemClass(JunoEnum.class.getName());
        junoEnumUtilType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoEnumUtil.class.getName());
        junoEventType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoEvent.class.getName());
        junoEventValueFactoryType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoEventValueFactory.class.getName());
        junoObjectType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoObject.class.getName());
        junoStringType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoString.class.getName());
        junoStringValueFactoryType = (JavaRawClass) schema
            .getTypeForSystemClass(JunoStringValueFactory.class.getName());
    }
}
