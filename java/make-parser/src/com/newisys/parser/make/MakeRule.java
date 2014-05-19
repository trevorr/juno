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
 * Base class for representing makefile rules.
 * 
 * @author Trevor Robinson
 */
public abstract class MakeRule
{
    protected String command;
    protected boolean doubleColon;

    public abstract String getTargetNames();

    public abstract String getDependencyNames();

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public boolean isDoubleColon()
    {
        return doubleColon;
    }

    public void setDoubleColon(boolean doubleColon)
    {
        this.doubleColon = doubleColon;
    }

    public String toString()
    {
        return getTargetNames() + (doubleColon ? ":: " : ": ")
            + getDependencyNames();
    }
}
