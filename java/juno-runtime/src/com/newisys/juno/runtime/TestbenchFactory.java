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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory used to instantiate a testbench in a polymorphic fashion.
 * 
 * @author Trevor Robinson
 */
public final class TestbenchFactory
{
    private TestbenchFactory()
    {
    }

    /**
     * Instantiates a testbench as specified by +testbenchClass on the command
     * line.
     * <P>
     * This is equivalent to <code>create(null, null, null)</code>.
     *
     * @return the new TestBench
     */
    public static Object create()
    {
        return create(null, null, null);
    }

    /**
     * Instantiates a testbench, using <code>defaultClsName</code> if
     * +testbenchClass is not specified on the command line.
     * <P>
     * This is equivalent to <code>create(null, null, defaultClsName)</code>.
     *
     * @param defaultClsName the classname to use if +testbenchClass is not
     *      specified on the command line
     * @return the new TestBench
     */
    public static Object create(String defaultClsName)
    {
        return create(null, null, defaultClsName);
    }

    /**
     * Instantiates a testbench, using <code>defaultClsName</code> if
     * +testbenchClass is not specified on the command line.
     *
     * @param args an array of arguments to pass to the testbench's constructor
     * @param defaultClsName the classname to use if +testbenchClass is not
     *      specified on the command line
     * @return the new TestBench
     */
    public static Object create(Object[] args, String defaultClsName)
    {
        Class[] argTypes = null;
        if (args != null)
        {
            argTypes = new Class[args.length];
            for (int i = 0; i < args.length; ++i)
            {
                Object arg = args[i];
                if (arg == null)
                {
                    throw new RuntimeException(
                        "Unable to determine type for null testbench "
                            + " constructor argument at position " + i);
                }
                argTypes[i] = arg.getClass();
            }
        }
        return create(args, argTypes, defaultClsName);
    }

    /**
     * Instantiates a testbench, using <code>defaultClsName</code> if
     * +testbenchClass is not specified on the command line.
     *
     * @param args an array of arguments to pass to the testbench's constructor
     * @param argTypes an array of Class objects that describes the types in
     *      <code>args</code>
     * @param defaultClsName the classname to use if +testbenchClass is not
     *      specified on the command line
     * @return the new TestBench
     */
    public static Object create(
        Object[] args,
        Class[] argTypes,
        String defaultClsName)
    {
        String clsName = Juno.getProperty("testbenchClass=", defaultClsName);
        if (clsName == null)
        {
            throw new RuntimeException("+testbenchClass not specified");
        }
        Class< ? > cls;
        try
        {
            cls = Class.forName(clsName);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Testbench class '" + clsName
                + "' not found", e);
        }
        Constructor< ? > ctor;
        try
        {
            ctor = cls.getConstructor(argTypes);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Constructor '"
                + describeConstructor(cls, argTypes)
                + "' not found in testbench class", e);
        }
        Object tb;
        try
        {
            tb = ctor.newInstance(args);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Error instantiating testbench class '"
                + clsName + "'", e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Error accessing constructor '"
                + describeConstructor(cls, argTypes) + "' of testbench class",
                e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Error executing constructor '"
                + describeConstructor(cls, argTypes) + "' of testbench class",
                e);
        }
        return tb;
    }

    private static String describeConstructor(Class cls, Class[] argTypes)
    {
        StringBuffer buf = new StringBuffer(80);
        buf.append(cls.getName());
        buf.append('(');
        if (argTypes != null)
        {
            for (int i = 0; i < argTypes.length; ++i)
            {
                if (i > 0) buf.append(", ");
                buf.append(argTypes[i].getName());
            }
        }
        buf.append(')');
        return buf.toString();
    }
}
