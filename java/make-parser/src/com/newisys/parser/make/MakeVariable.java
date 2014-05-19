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
 * Represents a makefile variable.
 * 
 * @author Trevor Robinson
 */
public class MakeVariable
{
    private final String name;
    private MakeVariableOrigin origin;
    private boolean recursive;
    private boolean exported;
    private String value;

    public MakeVariable(
        String name,
        MakeVariableOrigin origin,
        boolean recursive)
    {
        this.name = name;
        this.origin = origin;
        this.recursive = recursive;
        this.exported = false;
        this.value = "";
    }

    public String getName()
    {
        return name;
    }

    public MakeVariableOrigin getOrigin()
    {
        return origin;
    }

    public void setOrigin(MakeVariableOrigin origin)
    {
        this.origin = origin;
    }

    public boolean isRecursive()
    {
        return recursive;
    }

    public void setRecursive(boolean recursive)
    {
        this.recursive = recursive;
    }

    public boolean isExported()
    {
        return exported;
    }

    public void setExported(boolean exported)
    {
        this.exported = exported;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
