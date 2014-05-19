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

import java.util.LinkedList;
import java.util.List;

/**
 * Base implementation of a character sequence tokenizer.
 * 
 * @author Trevor Robinson
 */
abstract class AbstractTokenizer
{
    protected final CharSequence line;
    protected int lineEnd;
    protected int tokenStart;
    protected int tokenEnd;

    public AbstractTokenizer(CharSequence line)
    {
        this(line, 0, line.length());
    }

    public AbstractTokenizer(CharSequence line, int lineStart, int lineEnd)
    {
        this.line = line;
        this.lineEnd = lineEnd;
        this.tokenEnd = lineStart;
    }

    public boolean hasNextToken()
    {
        return tokenStart < lineEnd;
    }

    public int getLineEnd()
    {
        return lineEnd;
    }

    public int getNextTokenStart()
    {
        return tokenStart;
    }

    public String nextToken()
    {
        if (hasNextToken())
        {
            String token = line.subSequence(tokenStart, tokenEnd).toString();
            findNextToken();
            return token;
        }
        else
        {
            return "";
        }
    }

    public String restOfLine()
    {
        String token = line.subSequence(tokenStart, lineEnd).toString();
        tokenStart = lineEnd;
        return token;
    }

    public String restOfLineTrimmed()
    {
        int tokenEnd = lineEnd;
        while (tokenEnd > tokenStart
            && MakeUtil.isBlank(line.charAt(tokenEnd - 1)))
            --tokenEnd;
        String token = line.subSequence(tokenStart, tokenEnd).toString();
        tokenStart = lineEnd;
        return token;
    }

    protected abstract void findNextToken();

    public List extractTokens()
    {
        List tokenList = new LinkedList();
        while (hasNextToken())
        {
            tokenList.add(nextToken());
        }
        return tokenList;
    }
}
