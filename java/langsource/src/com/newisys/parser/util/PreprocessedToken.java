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
 * Represents a parser token that was the result of preprocessing.
 * It includes information about the #include and macro expansion chains.
 * 
 * @author Trevor Robinson
 */
public class PreprocessedToken
    extends Token
    implements Cloneable
{
    /**
     * The name of the file this token is from.
     */
    public String filename;

    /**
     * Location this file was #included from, or null if not included.
     */
    public IncludeLocation includedFrom;

    /**
     * Macro this token was expanded from, or null if not from a macro.
     */
    public MacroRef expandedFrom;

    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // should never happen
            throw new Error(e);
        }
    }

    public void assignFrom(PreprocessedToken other)
    {
        kind = other.kind;
        beginLine = other.beginLine;
        beginColumn = other.beginColumn;
        endLine = other.endLine;
        endColumn = other.endColumn;
        image = other.image;
        filename = other.filename;
        includedFrom = other.includedFrom;
        expandedFrom = other.expandedFrom;
    }
}
