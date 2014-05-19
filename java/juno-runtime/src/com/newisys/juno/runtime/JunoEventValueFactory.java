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
 * Implements a ValueFactory for JunoEvent objects.
 * 
 * @author Trevor Robinson
 */
public final class JunoEventValueFactory
    implements ValueFactory<JunoEvent>
{
    /**
     * A singleton instance of JunoEventValueFactory.
     */
    public static final JunoEventValueFactory INSTANCE = new JunoEventValueFactory();

    /**
     * Instantiates a new JunoEvent and returns it.
     *
     * @return a new JunoEvent
     */
    public JunoEvent newInstance()
    {
        return new JunoEvent();
    }

    /**
     * Instantiates a new JunoEvent and returns it.
     *
     * @param other the JunoEvent to copy. This argument is ignored in this
     *      implementation.
     * @return a new JunoEvent
     */
    public JunoEvent copyInstance(JunoEvent other)
    {
        return new JunoEvent();
    }
}
