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
 * Character sequence tokenizer that tokenizes a path list with the given
 * delimiter. It implements special GNU Make handling of escaped delimiters,
 * colon delimiters with DOS/Windows paths, and leading ".///".
 * 
 * @author Trevor Robinson
 */
final class PathTokenizer
    extends AbstractTokenizer
{
    private final StringBuffer buffer;
    private final char stopChar;
    private boolean hitStopChar;

    public PathTokenizer(StringBuffer buffer)
    {
        this(buffer, 0, buffer.length(), (char) 0);
    }

    public PathTokenizer(StringBuffer buffer, char stopChar)
    {
        this(buffer, 0, buffer.length(), stopChar);
    }

    public PathTokenizer(StringBuffer buffer, int lineStart, int lineEnd)
    {
        this(buffer, lineStart, lineEnd, (char) 0);
    }

    public PathTokenizer(
        StringBuffer buffer,
        int lineStart,
        int lineEnd,
        char stopChar)
    {
        super(buffer, lineStart, lineEnd);
        this.buffer = buffer;
        this.stopChar = stopChar;
        findNextToken();
    }

    public boolean isHitStopChar()
    {
        return hitStopChar;
    }

    protected void findNextToken()
    {
        tokenStart = tokenEnd;

        // skip leading blanks
        char c = 0;
        while (tokenStart < lineEnd
            && MakeUtil.isBlank(c = line.charAt(tokenStart)))
            ++tokenStart;
        if (tokenStart == lineEnd) return;
        if (c == stopChar)
        {
            lineEnd = tokenStart;
            hitStopChar = true;
            return;
        }

        // find end of next path
        do
        {
            tokenEnd = MakeUtil.unescapeIndexOf(buffer, tokenStart, lineEnd,
                stopChar, (char) 0, true);
            lineEnd = buffer.length();
            if (tokenEnd < 0) tokenEnd = lineEnd;
        }
        while (stopChar == ':' && tokenEnd < lineEnd
            && buffer.charAt(tokenEnd) == ':'
            && MakeUtil.isDriveColon(buffer, tokenEnd));

        // skip leading ".///"
        if (tokenEnd - tokenStart > 2 && buffer.charAt(tokenStart) == '.'
            && buffer.charAt(tokenStart + 1) == '/')
        {
            tokenStart += 2;
            while (tokenStart < tokenEnd && buffer.charAt(tokenStart) == '/')
                ++tokenStart;
        }

        // if ".///" became "", replace with "./"
        if (tokenStart == tokenEnd)
        {
            tokenStart -= 2;
            buffer.replace(tokenStart, tokenEnd, "./");
        }
    }
}
