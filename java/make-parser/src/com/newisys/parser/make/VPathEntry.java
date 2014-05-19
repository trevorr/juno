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
 * Represents a vpath declaration.
 * 
 * @author Trevor Robinson
 */
final class VPathEntry
{
    private final MakePattern pattern;
    private final String path;

    public VPathEntry(MakePattern pattern, String path)
    {
        this.pattern = pattern;
        this.path = path;
    }

    public String getPattern()
    {
        return pattern.getPattern();
    }

    public boolean patternMatches(String s)
    {
        return pattern.matches(s);
    }

    public String getPath()
    {
        return path;
    }

    public String toString()
    {
        return pattern + " " + path;
    }
}
