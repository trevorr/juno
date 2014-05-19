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
 * Character sequence tokenizer for GNU Make rules.
 * 
 * @author Trevor Robinson
 */
final class RuleTokenizer
    extends AbstractTokenizer
{
    private RuleTokenType tokenType;

    public RuleTokenizer(CharSequence line)
    {
        super(line);
        findNextToken();
    }

    public RuleTokenizer(CharSequence line, int lineStart, int lineEnd)
    {
        super(line, lineStart, lineEnd);
        findNextToken();
    }

    public RuleTokenType getNextTokenType()
    {
        return tokenType;
    }

    protected void findNextToken()
    {
        tokenStart = tokenEnd;

        // skip leading blanks
        char c = 0;
        while (tokenStart < lineEnd
            && MakeUtil.isBlank(c = line.charAt(tokenStart)))
            ++tokenStart;

        // look for operator
        tokenEnd = tokenStart + 1;
        switch (c)
        {
        case 0:
            tokenType = RuleTokenType.EOL;
            break;
        case ':':
            char c1 = getChar(tokenEnd);
            switch (c1)
            {
            case ':':
                tokenType = RuleTokenType.DOUBLE_COLON;
                ++tokenEnd;
                break;
            case '=':
                tokenType = RuleTokenType.ASSIGN_OP;
                ++tokenEnd;
                break;
            default:
                tokenType = RuleTokenType.COLON;
            }
            break;
        case ';':
            tokenType = RuleTokenType.SEMICOLON;
            break;
        case '=':
            tokenType = RuleTokenType.ASSIGN_OP;
            break;
        case '+':
        case '?':
            if (getChar(tokenEnd) == '=')
            {
                tokenType = RuleTokenType.ASSIGN_OP;
                ++tokenEnd;
                break;
            }
        default:
            tokenType = null;
        }
        if (tokenType != null) return;

        // look for static text or a variable reference
        tokenType = RuleTokenType.TEXT;
        loop: while (true)
        {
            char c1 = getChar(tokenEnd);
            switch (c)
            {
            case 0:
            case ' ':
            case '\t':
            case '=':
                // end text at EOL, blanks, or assignment
                break loop;
            case '+':
            case '?':
                if (c1 == '=')
                {
                    // end text at assignment
                    ++tokenEnd;
                    break loop;
                }
                break;
            case ':':
                // allow colon in text if it looks like a DOS/Windows drivespec
                if (tokenEnd - tokenStart == 2 && (c1 == '/' || c1 == '\\')
                    && Character.isLetter(line.charAt(tokenStart)))
                {
                    break;
                }
                // end text at (non-drivespec) colon
                break loop;
            case '$':
                ++tokenEnd;
                if (c1 != '$')
                {
                    // token contains a variable reference
                    tokenType = RuleTokenType.VAR_REF;
                    if (c1 == '(' || c1 == '{')
                    {
                        char closeParen = (c1 == '(') ? ')' : '}';
                        int matchPos = MakeUtil.indexOfMatchingDelimiter(line,
                            c1, closeParen, tokenEnd, lineEnd);
                        tokenEnd = (matchPos >= 0) ? matchPos + 1 : lineEnd;
                    }
                }
                break;
            case '\\':
                if (c1 == ':' || c1 == ';' || c1 == '=' || c1 == '\\')
                {
                    // allow escaped colon, semicolon, or assignment
                    ++tokenEnd;
                }
                break;
            }
            c = getChar(tokenEnd++);
        }
        --tokenEnd;
    }

    private char getChar(int pos)
    {
        return (pos < lineEnd) ? line.charAt(pos) : 0;
    }
}
