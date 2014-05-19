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
 * Character sequence tokenizer that uses blanks as delimiters.
 * 
 * @author Trevor Robinson
 */
final class BlankTokenizer
    extends AbstractTokenizer
{
    public BlankTokenizer(CharSequence line)
    {
        super(line);
        findNextToken();
    }

    public BlankTokenizer(CharSequence line, int lineStart, int lineEnd)
    {
        super(line, lineStart, lineEnd);
        findNextToken();
    }

    protected void findNextToken()
    {
        tokenStart = tokenEnd;
        while (tokenStart < lineEnd
            && MakeUtil.isBlank(line.charAt(tokenStart)))
            ++tokenStart;
        if (tokenStart < lineEnd)
        {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < lineEnd
                && !MakeUtil.isBlank(line.charAt(tokenEnd)))
                ++tokenEnd;
        }
    }
}
