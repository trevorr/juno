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

package com.newisys.langsource.vera;

import com.newisys.langschema.BlankLine;
import com.newisys.langschema.BlockComment;
import com.newisys.langschema.InlineComment;
import com.newisys.langsource.SourceObjectImpl;
import com.newisys.parser.util.PreprocessedToken;
import com.newisys.parser.util.Token;
import com.newisys.parser.vera.VeraToken;

/**
 * Base implementation for Vera source objects.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraSourceObjectImpl
    extends SourceObjectImpl
    implements VeraSourceObject
{
    public void setBeginLocation(Token beginToken)
    {
        // only accept the first call
        if (getBeginLine() == 0)
        {
            setBeginLocation(((PreprocessedToken) beginToken).filename,
                beginToken.beginLine, beginToken.beginColumn);
            setIncludeLocation(((PreprocessedToken) beginToken).includedFrom);
        }
    }

    public void setBeginLocation(VeraSourceObject beginObject)
    {
        // only accept the first call
        if (getBeginLine() == 0)
        {
            setBeginLocation(beginObject.getBeginFilename(), beginObject
                .getBeginLine(), beginObject.getBeginColumn());
            setIncludeLocation(beginObject.getIncludeLocation());
        }
    }

    public void setEndLocation(Token endToken)
    {
        // subsequent calls override previous location
        setEndLocation(((PreprocessedToken) endToken).filename,
            endToken.endLine, endToken.endColumn);
    }

    public void setEndLocation(VeraSourceObject endObject)
    {
        // subsequent calls override previous location
        setEndLocation(endObject.getEndFilename(), endObject.getEndLine(),
            endObject.getEndColumn());
    }

    public void applyCommentsFrom(Token realToken, boolean blanks)
    {
        VeraToken t = (VeraToken) realToken;
        applyCommentChain(t.leadingComments, true, blanks);
        applyCommentChain(t.trailingComments, false, blanks);
    }

    public void applyCommentsFrom(
        Token firstToken,
        Token lastToken,
        boolean blanks)
    {
        VeraToken t = (VeraToken) firstToken;
        while (t != lastToken && t != null)
        {
            applyCommentsFrom(t, blanks);
            firstToken = (VeraToken) firstToken.next;
        }
    }

    private void applyCommentChain(
        Token specialToken,
        boolean leading,
        boolean blanks)
    {
        while (specialToken != null)
        {
            applyComment(specialToken, leading, blanks);
            specialToken = specialToken.next;
        }
    }

    public void applyComment(Token specialToken, boolean leading, boolean blanks)
    {
        String s = specialToken.image;
        char c;
        if (s.startsWith("//"))
        {
            int eol = s.length();
            while ((c = s.charAt(eol - 1)) == '\r' || c == '\n')
                --eol;
            addAnnotation(new BlockComment(s.substring(2, eol), leading));
        }
        else if (s.startsWith("/*"))
        {
            assert (s.endsWith("*/"));
            InlineComment comment = new InlineComment(leading);
            int start = 2;
            while (true)
            {
                int eol = start;
                char c0 = s.charAt(eol), c1 = s.charAt(eol + 1);
                while (c0 != '\r' && c0 != '\n' && (c0 != '*' || c1 != '/'))
                {
                    ++eol;
                    c0 = c1;
                    c1 = s.charAt(eol + 1);
                }
                comment.addLine(s.substring(start, eol));
                if (c0 == '*') break;
                if (c0 == '\r' && c1 == '\n') ++eol;
                start = eol + 1;
            }
            addAnnotation(comment);
        }
        else if (s.length() > 0 && ((c = s.charAt(0)) == '\r' || c == '\n'))
        {
            if (blanks)
            {
                addAnnotation(leading ? BlankLine.LEADING : BlankLine.TRAILING);
            }
        }
    }
}
