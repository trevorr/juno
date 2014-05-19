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
 * Implementation of ValueFactory for immutable objects.
 *
 * @param <T> object type produced by factory
 * @author Trevor Robinson
 */
public final class ImmutableValueFactory<T>
    implements ValueFactory<T>
{
    private final T value;

    /**
     * Create an ImmutableValueFactory with the specified immutable object.
     *
     * @param value the immutable Object to use in this factory
     */
    public ImmutableValueFactory(T value)
    {
        this.value = value;
    }

    /**
     * Returns the immutable object for this factory.
     *
     * @return this factory's immutable object
     */
    public T newInstance()
    {
        return value;
    }

    /**
     * Returns the immutable object passed as an argument.
     *
     * @param other the Object to copy
     * @return <code>other</code>
     */
    public T copyInstance(T other)
    {
        return other;
    }
}
