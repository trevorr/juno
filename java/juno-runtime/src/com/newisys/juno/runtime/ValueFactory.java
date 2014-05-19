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
 * A factory interface that supports creating and copying objects.
 *
 * @param <T> object type produced by factory
 * @author Trevor Robinson
 */
public interface ValueFactory<T>
{
    /**
     * Create a new instance of the underlying object.
     *
     * @return a new instance of the Object
     */
    T newInstance();

    /**
     * Create a copy of the specified object.
     *
     * @param other the object to copy
     * @return a copy of <code>other</code>
     */
    T copyInstance(T other);
}
