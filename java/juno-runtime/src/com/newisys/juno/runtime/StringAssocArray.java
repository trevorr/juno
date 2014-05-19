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
 * String-keyed associative array.
 *
 * @param <V> value type
 * @author Trevor Robinson
 */
public class StringAssocArray<V>
    extends AssocArray<String, V>
{
    /**
     * Creates a StringAssocArray.
     */
    public StringAssocArray()
    {
    }

    /**
     * Creates a StringAssocArray with the specified default value. If a key is
     * requested that does not exist in this array, the default value will be
     * returned.
     *
     * @param nullValue the default value for this array
     */
    public StringAssocArray(V nullValue)
    {
        super(nullValue);
    }

    /**
     * Creates a StringAssocArray with the specified default value factory. If
     * a key is requested that does not exist in this array, the factory will
     * be used to instantiate the default value.
     *
     * @param nullValueFactory the value factory used to create default values
     */
    public StringAssocArray(ValueFactory< ? extends V> nullValueFactory)
    {
        super(nullValueFactory);
    }

    /**
     * Creates a StringAssocArray which is a copy of the specified array.
     *
     * @param other the array to copy
     */
    public StringAssocArray(StringAssocArray< ? extends V> other)
    {
        super(other);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public StringAssocArray<V> clone()
    {
        return new StringAssocArray<V>(this);
    }
}
