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

import java.io.File;

/**
 * Used to track information about a particular filename referenced in a
 * makefile.
 * 
 * @author Trevor Robinson
 */
public final class MakeFileInfo
{
    private final String path;
    private final File file;
    private boolean exists;
    private boolean phony;
    private boolean intermediate;
    private MakeStaticRule rule;

    public MakeFileInfo(String path)
    {
        this.path = path;
        file = new File(path);
        exists = file.exists();
    }

    public String getPath()
    {
        return path;
    }

    public boolean exists()
    {
        return exists;
    }

    public boolean isPhony()
    {
        return phony;
    }

    public void setPhony(boolean phony)
    {
        this.phony = phony;
    }

    public boolean isIntermediate()
    {
        return intermediate;
    }

    public void setIntermediate(boolean intermediate)
    {
        this.intermediate = intermediate;
    }

    public MakeStaticRule getRule()
    {
        return rule;
    }

    public void setRule(MakeStaticRule rule)
    {
        this.rule = rule;
    }

    public String toString()
    {
        return path;
    }
}
