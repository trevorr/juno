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

import com.newisys.verilog.util.BitVector;

/**
 * BitVector-keyed associative array.
 *
 * @param <V> value type
 * @author Trevor Robinson
 */
public class BitAssocArray<V>
    extends AssocArray<BitVector, V>
{
    public static final BitVector NULL_KEY = new BitVector(64);

    /**
     * Creates a BitAssocArray with a default value of null.
     */
    public BitAssocArray()
    {
    }

    /**
     * Creates a BitAssocArray with the specified default value. If a key is
     * requested that does not exist in this array, the default value will be
     * returned.
     *
     * @param nullValue the default value for this array
     */
    public BitAssocArray(V nullValue)
    {
        super(nullValue);
    }

    /**
     * Creates a BitAssocArray with the specified default value factory. If a
     * key is requested that does not exist in this array, the factory will be
     * used to instantiate the default value.
     *
     * @param nullValueFactory the value factory used to create default values
     */
    public BitAssocArray(ValueFactory< ? extends V> nullValueFactory)
    {
        super(nullValueFactory);
    }

    /**
     * Creates a BitAssocArray which is a copy of the specified array.
     *
     * @param other the array to copy
     */
    public BitAssocArray(BitAssocArray< ? extends V> other)
    {
        super(other);
    }

    /**
     * Validates a key. This method insures the key is a valid numeric value
     * and does not contain X/Z values.
     *
     * @param key the BitVector to validate
     */
    private void validateKey(BitVector key)
    {
        if (key.containsXZ())
        {
            throw new RuntimeException(
                "Undefined index in associative array access");
        }
    }

    /**
     * Gets the value for the specified key from this associative array.
     *
     * @param key the key whose associated value is to be returned
     * @return the value which maps to the specified key, or the default value
     *      if this associative array contains no mapping for this key
     * @throws RuntimeException if key contains X/Z
     */
    public V get(BitVector key)
    {
        validateKey(key);
        return super.get(key);
    }

    /**
     * Gets the value for the specified key from this associative array. If the
     * array contains no mapping for the key, a mapping is added to the default
     * value.
     *
     * @param key the key whose associated value is to be returned
     * @return the value which maps to the specified key, or the default value
     *      if this associative array contains no mapping for this key
     * @throws RuntimeException if key contains X/Z
     */
    public V getOrCreate(BitVector key)
    {
        validateKey(key);
        return super.getOrCreate(key);
    }

    /**
     * Associates the specified value with the specified key in this
     * associative array.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    public void put(BitVector key, V value)
    {
        validateKey(key);
        super.put(key, value);
    }

    /**
     * Returns the first key in this associative array. This method can be used
     * with {@link #next} to iterate all keys in the array.
     *
     * @return the first key in this associative array, or X if this array is
     *      empty
     */
    public BitVector first()
    {
        BitVector first = super.first();
        return first != null ? first : NULL_KEY;
    }

    /**
     * Returns the key following the given key in this associative array. This
     * method can be used with {@link #first} to iterate all keys in the array.
     *
     * @param prevKey a previously returned key
     * @return the key following <code>prevKey</code> in this associative
     *      array, or X if there are no more keys
     */
    public BitVector next(BitVector prevKey)
    {
        BitVector next = super.next(prevKey);
        return next != null ? next : NULL_KEY;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public BitAssocArray<V> clone()
    {
        return new BitAssocArray<V>(this);
    }
}
