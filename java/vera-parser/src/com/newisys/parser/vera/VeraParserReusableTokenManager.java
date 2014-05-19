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

package com.newisys.parser.vera;

import java.util.Iterator;
import java.util.LinkedList;

import com.newisys.parser.util.Token;

/**
 * Linked list implementation of VeraParserTokenManager that can be reset to
 * the beginning and reread.
 * 
 * @author Trevor Robinson
 */
public class VeraParserReusableTokenManager
    implements VeraParserTokenManager
{
    private final LinkedList<VeraToken> tokens = new LinkedList<VeraToken>();
    private Iterator<VeraToken> tokenIter;

    public void pushToken(VeraToken t)
    {
        tokens.addLast(t);
    }

    public Token getNextToken()
    {
        if (tokenIter == null) tokenIter = tokens.iterator();
        VeraToken t = tokenIter.next();
        // break token chain from previous parse so parser is not confused
        t.next = null;
        return t;
    }

    public void reset()
    {
        tokenIter = null;
    }

    public String getContents()
    {
        int len = 0;
        for (VeraToken t : tokens)
        {
            len += t.image.length();
        }
        StringBuffer buf = new StringBuffer(len);
        for (VeraToken t : tokens)
        {
            buf.append(t.image);
        }
        return buf.toString();
    }
}
