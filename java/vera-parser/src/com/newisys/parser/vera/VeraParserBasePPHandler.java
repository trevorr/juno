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

import java.util.Map;

import com.newisys.parser.util.PreprocessedToken;
import com.newisys.parser.verapp.VeraPPBaseHandler;
import com.newisys.parser.verapp.VeraPPMacro;
import com.newisys.parser.verapp.VeraPPParser;
import com.newisys.parser.verapp.VeraPPParserConstants;

/**
 * Vera parser base handler for Vera preprocessor callbacks. Simply extends
 * VeraPPBaseHandler with the ability to push translated tokens from the
 * preprocessor into the VeraParserTokenManager.
 * 
 * @author Trevor Robinson
 */
public class VeraParserBasePPHandler
    extends VeraPPBaseHandler
{
    protected final VeraParserTokenManager tm;

    public VeraParserBasePPHandler(VeraParserTokenManager tm)
    {
        this.tm = tm;
    }

    public VeraParserBasePPHandler(
        VeraParserTokenManager tm,
        Map<String, VeraPPMacro> defines)
    {
        super(defines);
        this.tm = tm;
    }

    public void processToken(VeraPPParser parser, PreprocessedToken t)
    {
        if (!isSkipped() && t.kind != VeraPPParserConstants.WS)
        {
            tm.pushToken(translateToken(parser, t));
        }
    }

    public void processEndOfFile(VeraPPParser parser, PreprocessedToken t)
    {
        // ignore EOF of included files
        if (getIncludeDepth() == 0)
        {
            tm.pushToken(translateToken(parser, t));
        }
    }

    protected VeraToken translateToken(VeraPPParser parser, PreprocessedToken t)
    {
        fillInPreprocessedToken(parser, t);
        VeraToken t2 = new VeraToken();
        t2.assignFrom(t);
        t2.kind = TokenTranslationTable.translate(t.kind);
        return t2;
    }
}
