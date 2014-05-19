/*
 * LangSource - Generic Programming Language Source Modeling Tools
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

package com.newisys.parser.util;

/**
 * Indicates the location of the #include directive that caused a particular
 * file to be included.
 * 
 * @author Trevor Robinson
 */
public class IncludeLocation
{
    /**
     * The name of the file that #included the refering file.
     */
    public final String filename;

    /**
     * The line number of the line containing the #include token.
     */
    public final int line;

    /**
     * Location this file was #included from, or null if not included.
     */
    public final IncludeLocation includedFrom;

    public IncludeLocation(
        String filename,
        int line,
        IncludeLocation includedFrom)
    {
        this.filename = filename;
        this.line = line;
        this.includedFrom = includedFrom;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String thisLoc = filename + ":" + line;
        return includedFrom != null ? thisLoc + " included from "
            + includedFrom : thisLoc;
    }
}
