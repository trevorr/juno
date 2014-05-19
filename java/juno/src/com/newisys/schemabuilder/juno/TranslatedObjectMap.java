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

import java.util.HashMap;
import java.util.Map;

import com.newisys.dv.ifgen.schema.IfgenSchemaObject;
import com.newisys.langschema.java.JavaSchemaObject;
import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.vera.VeraSchemaObject;

/**
 * Maintains a mapping of Vera schema object to translated Java and Ifgen
 * schema objects. Also maintains wait_var event variables corresponding to
 * Java variables.
 * 
 * @author Trevor Robinson
 */
final class TranslatedObjectMap
{
    private final Map<VeraSchemaObject, JavaSchemaObject> javaObjects = new HashMap<VeraSchemaObject, JavaSchemaObject>();
    private final Map<VeraSchemaObject, IfgenSchemaObject> ifgenObjects = new HashMap<VeraSchemaObject, IfgenSchemaObject>();
    private final Map<JavaVariable, JavaVariable> waitVarEvents = new HashMap<JavaVariable, JavaVariable>();

    public void addJavaObject(
        VeraSchemaObject veraObject,
        JavaSchemaObject javaObject)
    {
        javaObjects.put(veraObject, javaObject);
    }

    public JavaSchemaObject getJavaObject(VeraSchemaObject veraObject)
    {
        return javaObjects.get(veraObject);
    }

    public void addIfgenObject(
        VeraSchemaObject veraObject,
        IfgenSchemaObject ifgenObject)
    {
        ifgenObjects.put(veraObject, ifgenObject);
    }

    public IfgenSchemaObject getIfgenObject(VeraSchemaObject veraObject)
    {
        return ifgenObjects.get(veraObject);
    }

    public void addWaitVarEvent(JavaVariable var, JavaVariable eventVar)
    {
        waitVarEvents.put(var, eventVar);
    }

    public JavaVariable getWaitVarEvent(JavaVariable var)
    {
        return waitVarEvents.get(var);
    }
}
