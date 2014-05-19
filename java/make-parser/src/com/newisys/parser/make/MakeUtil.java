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
 * Utilities methods for parsing makefiles.
 * 
 * @author Trevor Robinson
 */
final class MakeUtil
{
    private MakeUtil()
    {
    }

    public static int indexOfChar(CharSequence s, char c, int start)
    {
        int end = s.length();
        while (start < end)
        {
            if (s.charAt(start) == c) return start;
            ++start;
        }
        return -1;
    }

    public static int indexOfLastSlash(String text)
    {
        int slashPos1 = text.lastIndexOf('/');
        int slashPos2 = text.lastIndexOf('\\');
        int slashPos = Math.max(slashPos1, slashPos2);
        return slashPos;
    }

    public static int indexOfMatchingDelimiter(
        CharSequence s,
        char openChar,
        char closeChar,
        int start,
        int end)
    {
        int count = 0;
        for (int i = start; i < end; ++i)
        {
            char c = s.charAt(i);
            if (c == closeChar && --count < 0)
            {
                return i;
            }
            else if (c == openChar)
            {
                ++count;
            }
        }
        return -1;
    }

    public static int indexOfArgumentEnd(
        CharSequence s,
        char openParen,
        char closeParen,
        int start,
        int end)
    {
        int parenCount = 0;
        for (int i = start; i < end; ++i)
        {
            char c = s.charAt(i);
            if (c == openParen)
            {
                ++parenCount;
            }
            else if (c == closeParen)
            {
                --parenCount;
            }
            else if (c == ',' && parenCount <= 0)
            {
                return i;
            }
        }
        return end;
    }

    public static int indexOfUnescaped(CharSequence s, char c)
    {
        int start = 0;
        while (true)
        {
            int pos = indexOfChar(s, c, start);
            if (!isEscaped(s, pos))
            {
                return pos;
            }
            start = pos + 1;
        }
    }

    public static boolean isEscaped(CharSequence s, int pos)
    {
        boolean escaped = false;
        for (int i = pos - 1; i >= 0 && s.charAt(i) == '\\'; --i)
        {
            escaped = !escaped;
        }
        return escaped;
    }

    public static boolean isDriveColon(CharSequence s, int pos)
    {
        int len = s.length();
        if (pos > 0 && pos < len - 1)
        {
            char before = s.charAt(pos - 1);
            char after = s.charAt(pos + 1);
            return (after == '/' || after == '\\')
                && Character.isLetter(before);
        }
        return false;
    }

    public static int unescapeIndexOf(StringBuffer s, char c)
    {
        return unescapeIndexOf(s, c, (char) 0, false);
    }

    public static int unescapeIndexOf(StringBuffer s, char c1, char c2)
    {
        return unescapeIndexOf(s, c1, c2, false);
    }

    public static int unescapeIndexOf(
        StringBuffer s,
        char c1,
        char c2,
        boolean blank)
    {
        return unescapeIndexOf(s, 0, s.length(), c1, c2, blank);
    }

    public static int unescapeIndexOf(
        final StringBuffer s,
        final int start,
        int end,
        final char c1,
        final char c2,
        final boolean blank)
    {
        int pos = start;
        while (true)
        {
            // remember where this iteration started
            int iterStart = pos;

            // scan to position of first occurrence of end of line, c1, c2, or
            // a blank (if blank is specified as a delimiter)
            char c;
            while (pos < end && (c = s.charAt(pos)) != c1 && c != c2
                && !(blank && isBlank(c)))
                ++pos;

            // done if found end of line
            if (pos == end)
            {
                pos = -1;
                break;
            }

            // check for escaped delimiter
            if (pos > iterStart && s.charAt(pos - 1) == '\\')
            {
                // find first leading backslash
                int slashPos = pos - 2;
                while (slashPos >= iterStart && s.charAt(slashPos) == '\\')
                    --slashPos;
                ++slashPos;

                // remove any backslashes used to escape backslashes
                int slashes = pos - slashPos;
                int keepSlashes = slashes / 2;
                pos -= keepSlashes;
                s.delete(slashPos, pos);
                end -= slashes - keepSlashes;

                // an even number of backslashes does not escape the delimiter
                if ((slashes & 1) == 0) break;
            }
            else
            {
                // delimiter was not escaped; return its position
                break;
            }
        }
        return pos;
    }

    public static boolean isBlank(char c)
    {
        return c == ' ' || c == '\t';
    }

    public static boolean isFuncChar(char c)
    {
        return Character.isLowerCase(c) || c == '-';
    }
}
