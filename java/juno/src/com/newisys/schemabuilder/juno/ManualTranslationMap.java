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

import com.newisys.langschema.java.JavaFunction;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaSchemaObject;
import com.newisys.langschema.java.JavaVariable;

/**
 * Mapping of Vera construct names to manually translated Java schema objects.
 * 
 * @author Trevor Robinson
 */
final class ManualTranslationMap
{
    private final Map<String, JavaSchemaObject> xlatMap = new HashMap<String, JavaSchemaObject>();

    private void addObject(String uri, JavaSchemaObject javaObject)
    {
        xlatMap.put(uri, javaObject);
    }

    public void addClass(String name, JavaRawAbstractClass cls)
    {
        addObject("class:" + name, cls);
    }

    public void addFunction(String name, JavaFunction func)
    {
        addObject("func:" + name, func);
    }

    public void addVariable(String name, JavaVariable var)
    {
        addObject("var:" + name, var);
    }

    private Object getObject(String uri)
    {
        return xlatMap.get(uri);
    }

    public JavaRawAbstractClass getClass(String name)
    {
        return (JavaRawAbstractClass) getObject("class:" + name);
    }

    public JavaFunction getFunction(String name)
    {
        return (JavaFunction) getObject("func:" + name);
    }

    public JavaVariable getVariable(String name)
    {
        return (JavaVariable) getObject("var:" + name);
    }
}
