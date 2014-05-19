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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.newisys.parser.util.PreprocessedToken;

/**
 * Simple handler for Vera preprocessor callbacks. Writes preprocessed output
 * to the given output stream with options to control passthrough of comments
 * and pragmas.
 * 
 * @author Trevor Robinson
 */
public class VeraPPHandler
    extends VeraPPBaseHandler
{
    private PrintWriter out;
    private boolean passPragmas = false;
    private boolean passLineComments = true;
    private boolean passBlockComments = false;

    public VeraPPHandler(OutputStream _out)
    {
        this(new PrintWriter(_out, true));
    }

    public VeraPPHandler(Writer _out)
    {
        this(new PrintWriter(_out, true));
    }

    private VeraPPHandler(PrintWriter _out)
    {
        this.out = _out;
    }

    public void flushOutput()
    {
        out.flush();
    }

    public boolean isPassPragmas()
    {
        return passPragmas;
    }

    public void setPassPragmas(boolean passPragmas)
    {
        this.passPragmas = passPragmas;
    }

    public boolean isPassLineComments()
    {
        return passLineComments;
    }

    public void setPassLineComments(boolean passLineComments)
    {
        this.passLineComments = passLineComments;
    }

    public boolean isPassBlockComments()
    {
        return passBlockComments;
    }

    public void setPassBlockComments(boolean passBlockComments)
    {
        this.passBlockComments = passBlockComments;
    }

    public void processToken(VeraPPParser parser, PreprocessedToken t)
    {
        if (!isSkipped())
        {
            out.print(t.image);
        }
    }

    public void processComment(VeraPPParser parser, PreprocessedToken t)
    {
        boolean lineComment = t.kind == VeraPPParserConstants.SINGLE_LINE_COMMENT;
        boolean blockComment = t.kind == VeraPPParserConstants.MULTI_LINE_COMMENT;
        if (!isSkipped()
            && ((lineComment && passLineComments) || (blockComment && passBlockComments)))
        {
            out.print(t.image);
        }
    }

    public void processEndOfLine(VeraPPParser parser, PreprocessedToken t)
    {
        if (!isSkipped())
        {
            out.println();
        }
    }

    public void processEndOfFile(VeraPPParser parser, PreprocessedToken t)
    {
        out.flush();
    }

    public void processPragma(VeraPPParser parser, String text)
    {
        if (!isSkipped() && passPragmas)
        {
            out.print("#pragma");
            out.println(text);
        }
    }
}
