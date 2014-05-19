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
 * String-to-Object associative array.
 * 
 * @author Trevor Robinson
 */
public final class StringObjectAssocArray
    extends StringAssocArray<Object>
{
    /**
     * Creates a StringObjectAssocArray.
     */
    public StringObjectAssocArray()
    {
    }

    /**
     * Creates a StringObjectAssocArray with the specified default value. If a key is
     * requested that does not exist in this StringObjectAssocArray, the default
     * value will be returned.
     *
     * @param nullValue the default value for this StringObjectAssocArray
     */
    public StringObjectAssocArray(Object nullValue)
    {
        super(nullValue);
    }

    /**
     * Creates a StringObjectAssocArray with the specified ValueFactory. If a key is
     * requested that does not exist in this StringObjectAssocArray, <code>nullValueFactory</code>
     * will be used to instantiate the default value.
     *
     * @param nullValueFactory the ValueFactory for this StringObjectAssocArray
     */
    public StringObjectAssocArray(ValueFactory< ? > nullValueFactory)
    {
        super(nullValueFactory);
    }

    /**
     * Creates a StringObjectAssocArray which is a copy of the specified AssocArray.
     *
     * @param other the AssocArray to copy
     */
    public StringObjectAssocArray(AssocArray other)
    {
        super(other);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public StringObjectAssocArray clone()
    {
        return new StringObjectAssocArray(this);
    }
}
