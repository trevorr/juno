/*
 * Parser and Source Model for the OpenVera (TM) language
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * VERA and OpenVera are trademarks or registered trademarks of Synopsys, Inc.
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

package com.newisys.parser.verapp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.parser.util.IncludeLocation;
import com.newisys.parser.util.Token;

/**
 * Base VeraPPParser implementation for user macros.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraPPUserMacro
    extends VeraPPMacro
{
    private final IncludeLocation location;
    protected final List<Token> tokens = new LinkedList<Token>();

    public VeraPPUserMacro(String name, IncludeLocation location)
    {
        super(name);
        this.location = location;
    }

    public IncludeLocation getLocation()
    {
        return location;
    }

    public void appendToken(Token t)
    {
        tokens.add(t);
    }

    public List<Token> getTokens()
    {
        return tokens;
    }

    public String expand()
    {
        StringBuffer buf = new StringBuffer();
        for (Token cur : tokens)
        {
            buf.append(cur.image);
        }
        return buf.toString();
    }

    public boolean containsTokenPasting()
    {
        int prevKind = 0;
        for (Token cur : tokens)
        {
            int curKind = cur.kind;
            if (isPasteable(curKind) && isPasteable(prevKind)) return true;
            prevKind = curKind;
        }
        return false;
    }

    protected static boolean isPasteable(int kind)
    {
        return kind == VeraPPParserConstants.MACRO_IDENT
            || kind == VeraPPParserConstants.DEFINE_NUMBER;
    }

    public boolean equals(Object obj)
    {
        if (getClass().equals(obj.getClass()))
        {
            VeraPPUserMacro other = (VeraPPUserMacro) obj;
            if (getName().equals(other.getName()))
            {
                List<Token> otherTokens = other.tokens;
                if (tokens.size() == otherTokens.size())
                {
                    Iterator<Token> thisIter = tokens.iterator();
                    Iterator<Token> otherIter = otherTokens.iterator();
                    while (thisIter.hasNext())
                    {
                        Token thisToken = thisIter.next();
                        Token otherToken = otherIter.next();
                        if (!thisToken.image.equals(otherToken.image))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
