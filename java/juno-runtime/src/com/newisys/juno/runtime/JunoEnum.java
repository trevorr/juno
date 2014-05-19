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
 * Interface implemented by enum types translated from Vera. Provides
 * Vera-like facilities for conversion, increment/decrement, packing, etc. 
 * 
 * @author Trevor Robinson
 */
public interface JunoEnum<E extends Enum<E> & JunoEnum<E>>
{
    /**
     * Returns the enum constant following this one, in declaration order.
     * If this is the last enum constant declared, this method returns the
     * first enum constant.
     *
     * @return the next enum constant
     */
    E next();

    /**
     * Returns the n'th enum constant following this one, in declaration order,
     * wrapping from the last constant to the first as necessary. Calling
     * next(1) is equivalent to calling next().
     *
     * @param count the number of constants to advance
     * @return the n'th enum constant following this one
     */
    E next(int count);

    /**
     * Returns the enum constant preceding this one, in declaration order.
     * If this is the first enum constant declared, this method returns the
     * last enum constant.
     *
     * @return the previous enum constant
     */
    E previous();

    /**
     * Returns the n'th enum constant preceding this one, in declaration order,
     * wrapping from the first constant to the last as necessary. Calling
     * previous(1) is equivalent to calling previous().
     *
     * @param count the number of constants to advance
     * @return the n'th enum constant preceding this one
     */
    E previous(int count);

    /**
     * Returns the enum constant associated with the specified value. If no
     * enumeration value corresponds the given <code>value</code> and
     * <code>checked</code> is <code>false</code>, this method throws an
     * IllegalArgumentException. If there is no corresponding value and
     * <code>checked</code> is true, this method returns the UNDEFINED value.
     *
     * @param value the value corresponding to the desired enum constant
     * @param checked <code>true</code> if the caller will check/handle an
     *      undefined enumeration value, <code>false</code> if this method
     *      should throw an exception
     * @return the enum constant corresponding to <code>value</code>, or the
     *      UNDEFINED value if there is none
     */
    E getForValue(Integer value, boolean checked);

    /**
     * Returns the number of bits needed to encode the values of this
     * enumeration.
     *
     * @return the number of bits needed to encode this enumeration
     */
    int getPackedSize();

    /**
     * Returns whether or not this is a signed enumeration.
     *
     * @return <code>true</code> if this enumeration is signed,
     *      <code>false</code> otherwise
     */
    boolean isSigned();

    /**
     * Returns whether this value is a defined enumeration value, as opposed to
     * the special UNDEFINED enumeration value.
     *
     * @return <code>true</code> if this enumeration value is defined,
     *      <code>false</code> if it is UNDEFINED
     */
    boolean isDefined();

    /**
     * Returns the <code>int</code> value corresponding to this enumeration
     * value.
     *
     * @return the <code>int</code> value corresponding to this enumeration
     *      value
     */
    int toInt();

    /**
     * Returns the Integer value corresponding to this enumeration. For the
     * UNDEFINED enumeration value, this method returns <code>null</code>.
     *
     * @return the Integer value corresponding to this enumeration value
     */
    Integer toInteger();

    /**
     * Returns the String representation of this enumeration value. For the
     * UNDEFINED enumeration value, this method returns "** UNDEFINED **".
     *
     * @return the String representation of this enumeration value
     */
    String toString();
}
