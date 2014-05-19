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
 * Utility methods for handling macro references.
 * 
 * @author Trevor Robinson
 */
public final class MacroRefUtil
{
    private MacroRefUtil()
    {
    }

    public static MacroRef getCommonMacroRef(MacroRef begin, MacroRef end)
    {
        MacroRef result;
        if (begin == end)
        {
            result = begin;
        }
        else if (begin != null && end != null)
        {
            int beginDepth = getExpansionDepth(begin);
            int endDepth = getExpansionDepth(end);
            if (endDepth > beginDepth)
            {
                end = getExpansionAtDepth(end, endDepth - beginDepth);
            }
            else if (beginDepth > endDepth)
            {
                begin = getExpansionAtDepth(begin, beginDepth - endDepth);
            }
            while (begin != end && begin != null && end != null)
            {
                begin = begin.getExpandedFrom();
                end = end.getExpandedFrom();
            }
            result = (begin == end) ? begin : null;
        }
        else
        {
            result = null;
        }
        return result;
    }

    public static int getExpansionDepth(MacroRef ref)
    {
        return getExpansionDepth(ref, null);
    }

    public static int getExpansionDepth(MacroRef from, MacroRef to)
    {
        int depth = 0;
        while (from != to && from != null)
        {
            from = from.getExpandedFrom();
            ++depth;
        }
        return depth;
    }

    public static MacroRef getExpansionAtDepth(MacroRef ref, int depth)
    {
        while (depth > 0)
        {
            if (ref != null) ref = ref.getExpandedFrom();
            --depth;
        }
        return ref;
    }

    public static boolean expansionContains(MacroRef ref, MacroRef test)
    {
        while (ref != null)
        {
            if (ref == test) return true;
            ref = ref.getExpandedFrom();
        }
        return false;
    }
}
