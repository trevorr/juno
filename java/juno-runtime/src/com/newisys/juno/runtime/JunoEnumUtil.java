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
 * Static utility methods used in the implementation of translated enum types.
 * 
 * @author Trevor Robinson
 */
public final class JunoEnumUtil
{
    public static <E extends Enum<E> & JunoEnum<E>> E advance(E cur, int count)
    {
        final E[] consts = cur.getDeclaringClass().getEnumConstants();
        final boolean haveUndefined = consts[0].toInteger() == null;
        final int undefinedAdj = haveUndefined && consts.length > 1 ? 1 : 0;
        return consts[(cur.ordinal() - undefinedAdj + count)
            % (consts.length - undefinedAdj) + undefinedAdj];
    }

    public static <E extends Enum<E> & JunoEnum<E>> E forValue(
        Class<E> cls,
        Integer value,
        boolean checked)
    {
        final E[] consts = cls.getEnumConstants();
        E undef = null;
        for (final E e : consts)
        {
            final Integer i = e.toInteger();
            if (i == null)
            {
                if (value == null) return e;
                undef = e;
            }
            else if (value != null && i.intValue() == value.intValue())
            {
                return e;
            }
        }
        if (!checked)
        {
            throw new IllegalArgumentException("Value " + value
                + " does not correspond to any member of " + cls.getName());
        }
        return undef;
    }

    public static <E extends Enum<E> & JunoEnum<E>> String toString(E e)
    {
        final Integer i = e.toInteger();
        return i != null ? e.name() : "** UNDEFINED **";
    }
}
