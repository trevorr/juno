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

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Base implementation of associative arrays.
 *
 * @param <K> key type
 * @param <V> value type
 * @author Trevor Robinson
 */
public class AssocArray<K, V>
    implements Cloneable
{
    private final SortedMap<K, V> map;
    private final ValueFactory< ? extends V> nullValueFactory;

    // cached key iterator
    private transient Iterator<K> keyIter;
    private transient K cachedPrevKey;

    /**
     * Creates an AssocArray with a default value of null.
     */
    public AssocArray()
    {
        this((V) null);
    }

    /**
     * Creates an AssocArray with the specified default value. If a key is
     * requested that does not exist in this array, the default value will be
     * returned.
     *
     * @param nullValue the default value for this array
     */
    public AssocArray(V nullValue)
    {
        this.map = new TreeMap<K, V>();
        this.nullValueFactory = new ImmutableValueFactory<V>(nullValue);
    }

    /**
     * Creates an AssocArray with the specified default value factory. If a key
     * is requested that does not exist in this array, the factory will be used
     * to instantiate the default value.
     *
     * @param nullValueFactory the value factory used to create default values
     */
    public AssocArray(ValueFactory< ? extends V> nullValueFactory)
    {
        this.map = new TreeMap<K, V>();
        this.nullValueFactory = nullValueFactory;
    }

    /**
     * Creates a new AssocArray which is a copy of the specified array.
     *
     * @param other the array to copy
     */
    public AssocArray(AssocArray< ? extends K, ? extends V> other)
    {
        this.map = new TreeMap<K, V>(other.map);
        this.nullValueFactory = other.nullValueFactory;
    }

    /**
     * Gets the value for the specified key from this associative array.
     *
     * @param key the key whose associated value is to be returned
     * @param keepNew indicates whether to add a mapping to the default value
     *      if this associative array contains no mapping for this key
     * @return the value which maps to the specified key, or the default value
     *      if this associative array contains no mapping for this key
     */
    private V getImpl(K key, boolean keepNew)
    {
        V value = map.get(key);
        if (value == null)
        {
            value = nullValueFactory.newInstance();
            if (keepNew)
            {
                map.put(key, value);
                keyIter = null;
                cachedPrevKey = null;
            }
        }
        return value;
    }

    /**
     * Gets the value for the specified key from this associative array.
     *
     * @param key the key whose associated value is to be returned
     * @return the value which maps to the specified key, or the default value
     *      if this associative array contains no mapping for this key
     */
    public V get(K key)
    {
        return getImpl(key, false);
    }

    /**
     * Gets the value for the specified key from this associative array. If the
     * array contains no mapping for the key, a mapping is added to the default
     * value.
     *
     * @param key the key whose associated value is to be returned
     * @return the value which maps to the specified key, or the default value
     *      if this associative array contains no mapping for this key
     */
    public V getOrCreate(K key)
    {
        return getImpl(key, true);
    }

    /**
     * Associates the specified value with the specified key in this
     * associative array.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    public void put(K key, V value)
    {
        map.put(key, value);
        keyIter = null;
        cachedPrevKey = null;
    }

    /**
     * Checks if there are any entries in this associative array.
     *
     * @return the number of entries in this associative array
     */
    public int check()
    {
        return map.size();
    }

    /**
     * Checks if there is a mapping in this associative array for the specified
     * key.
     *
     * @param key the key to check for a mapping
     * @return <code>1</code> if there is an entry in this associative array for
     *      <code>key</code>, <code>0</code> otherwise
     */
    public int check(K key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("AssocArray.check: null key");
        }
        return map.containsKey(key) ? 1 : 0;
    }

    /**
     * Remove all entries from this associative array.
     *
     * @return <code>1</code>
     */
    public int delete()
    {
        map.clear();
        keyIter = null;
        cachedPrevKey = null;
        return 1;
    }

    /**
     * Deletes the entry in this associative array for the specified key (if
     * one exists).
     *
     * @param key the key to delete the mapping of
     * @return <code>1</code> if the key exists in this associative array and
     *      was deleted, <code>0</code> otherwise
     */
    public int delete(K key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("AssocArray.delete: null key");
        }
        if (map.containsKey(key))
        {
            map.remove(key);
            keyIter = null;
            cachedPrevKey = null;
            return 1;
        }
        return 0;
    }

    /**
     * Returns the first key in this associative array. This method can be used
     * with {@link #next} to iterate all keys in the array.
     *
     * @return the first key in this associative array, or null if this array
     *      is empty
     */
    public K first()
    {
        if (!map.isEmpty())
        {
            keyIter = map.keySet().iterator();
            cachedPrevKey = keyIter.next();
            return cachedPrevKey;
        }
        return null;
    }

    /**
     * Returns the key following the given key in this associative array. This
     * method can be used with {@link #first} to iterate all keys in the array.
     *
     * @param prevKey a previously returned key
     * @return the key following <code>prevKey</code> in this associative
     *      array, or null if there are no more keys
     */
    public K next(K prevKey)
    {
        // can we use cached iterator?
        if (keyIter != null && cachedPrevKey.equals(prevKey))
        {
            // does cached iterator contain more keys?
            if (keyIter.hasNext())
            {
                cachedPrevKey = keyIter.next();
                return cachedPrevKey;
            }
            // fall through to no more keys
        }
        else
        {
            // create new key iterator using given key
            keyIter = map.tailMap(prevKey).keySet().iterator();
            if (keyIter.hasNext())
            {
                cachedPrevKey = keyIter.next();
                // done if first key is greater than given key
                if (!cachedPrevKey.equals(prevKey))
                {
                    return cachedPrevKey;
                }
                // skip first key
                if (keyIter.hasNext())
                {
                    cachedPrevKey = keyIter.next();
                    return cachedPrevKey;
                }
                // fall through to no more keys
            }
            // fall through to no more keys
        }

        // clear cached key iterator
        keyIter = null;
        cachedPrevKey = null;

        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public AssocArray<K, V> clone()
    {
        return new AssocArray<K, V>(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return map.toString();
    }
}
