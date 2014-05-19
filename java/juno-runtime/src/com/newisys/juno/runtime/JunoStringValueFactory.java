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

package com.newisys.juno.runtime;

/**
 * Implementation of ValueFactory for JunoString objects.
 * 
 * @author Trevor Robinson
 */
public final class JunoStringValueFactory
    implements ValueFactory<JunoString>
{
    /**
     * A singleton instance of JunoStringValueFactory.
     */
    public static final JunoStringValueFactory INSTANCE = new JunoStringValueFactory();

    /**
     * Instantiates a new JunoString and returns it.
     *
     * @return a new JunoString
     */
    public JunoString newInstance()
    {
        return new JunoString();
    }

    /**
     * Returns a new JunoString, which is a copy of <code>other</code>.
     *
     * @param other the JunoString to copy
     * @return a new JunoString, which is a copy of <code>other</code>
     */
    public JunoString copyInstance(JunoString other)
    {
        return new JunoString(other);
    }
}
