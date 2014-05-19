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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of Vera class names to Java factory call builders used to translate
 * instance creation expressions for those classes.
 * 
 * @author Trevor Robinson
 */
final class FactoryTranslationMap
    implements Serializable
{
    private static final long serialVersionUID = 3257849883108389939L;

    private final Map<String, FactoryCallBuilder> xlatMap = new HashMap<String, FactoryCallBuilder>();

    public void addFactory(String veraCls, FactoryCallBuilder fcb)
    {
        xlatMap.put(veraCls, fcb);
    }

    public FactoryCallBuilder getFactory(String veraCls)
    {
        return xlatMap.get(veraCls);
    }
}
