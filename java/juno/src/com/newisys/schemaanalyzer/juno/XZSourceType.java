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

package com.newisys.schemaanalyzer.juno;

/**
 * Enumeration of X/Z sources for a variable.
 * 
 * @author Trevor Robinson
 */
public final class XZSourceType
{
    // assignment from X/Z variable
    public static final XZSourceType ASSIGNMENT = new XZSourceType(0,
        "assignment");

    // read of uninitialized variable
    public static final XZSourceType UNINIT = new XZSourceType(1,
        "uninitialized read");

    // bit vector literal containing X/Z
    public static final XZSourceType LITERAL = new XZSourceType(2, "literal");

    // read of array element
    public static final XZSourceType ARRAY = new XZSourceType(3, "array access");

    // read of Verilog signal
    public static final XZSourceType SIGNAL = new XZSourceType(4, "signal read");

    // built-in, HDL, or UDF function
    public static final XZSourceType EXTERNAL = new XZSourceType(5,
        "external function");

    public static final int NUM_CODES = 6;

    private final int code;
    private final String str;

    private XZSourceType(int code, String str)
    {
        this.code = code;
        this.str = str;
    }

    public static XZSourceType getInstance(int code)
    {
        switch (code)
        {
        case 0:
            return ASSIGNMENT;
        case 1:
            return UNINIT;
        case 2:
            return LITERAL;
        case 3:
            return ARRAY;
        case 4:
            return SIGNAL;
        case 5:
            return EXTERNAL;
        default:
            throw new IllegalArgumentException("Illegal code: " + code);
        }
    }

    public int getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
