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
 * BitVector-to-Object associative array.
 * 
 * @author Trevor Robinson
 */
public final class BitObjectAssocArray
    extends BitAssocArray<Object>
{
    /**
     * Creates a BitObjectAssocArray.
     */
    public BitObjectAssocArray()
    {
    }

    /**
     * Creates a BitObjectAssocArray with the specified default value. If a key
     * is requested that does not exist in this array, the default value will
     * be returned.
     *
     * @param nullValue the default value for this array
     */
    public BitObjectAssocArray(Object nullValue)
    {
        super(nullValue);
    }

    /**
     * Creates a BitObjectAssocArray with the specified default value factory.
     * If a key is requested that does not exist in this array, the factory
     * will be used to instantiate the default value.
     *
     * @param nullValueFactory the value factory used to create default values
     */
    public BitObjectAssocArray(ValueFactory< ? > nullValueFactory)
    {
        super(nullValueFactory);
    }

    /**
     * Creates a BitObjectAssocArray which is a copy of the specified array.
     *
     * @param other the array to copy
     */
    public BitObjectAssocArray(BitObjectAssocArray other)
    {
        super(other);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public BitObjectAssocArray clone()
    {
        return new BitObjectAssocArray(this);
    }
}
