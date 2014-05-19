/*
 * Makefile Parser and Model Builder
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.parser.make;

/**
 * Enumeration of makefile variable origins.
 * 
 * @author Trevor Robinson
 */
public final class MakeVariableOrigin
{
    private final String str;
    private final int level;

    private MakeVariableOrigin(String str, int level)
    {
        this.str = str;
        this.level = level;
    }

    public boolean overrides(MakeVariableOrigin other)
    {
        return level >= other.level;
    }

    public String toString()
    {
        return str;
    }

    // IMPORTANT: These strings are dictated by the 'origin' function!

    public static final MakeVariableOrigin DEFAULT = new MakeVariableOrigin(
        "default", 0);
    public static final MakeVariableOrigin ENVIRONMENT = new MakeVariableOrigin(
        "environment", 1);
    public static final MakeVariableOrigin FILE = new MakeVariableOrigin(
        "file", 2);
    public static final MakeVariableOrigin ENVIRONMENT_OVERRIDE = new MakeVariableOrigin(
        "environment override", 3);
    public static final MakeVariableOrigin COMMAND_LINE = new MakeVariableOrigin(
        "command line", 4);
    public static final MakeVariableOrigin OVERRIDE = new MakeVariableOrigin(
        "override", 5);
    public static final MakeVariableOrigin AUTOMATIC = new MakeVariableOrigin(
        "automatic", 6);
}
