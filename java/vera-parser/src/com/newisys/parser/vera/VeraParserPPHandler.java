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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langsource.vera.CompilationUnitDeclMember;
import com.newisys.langsource.vera.DefineDecl;
import com.newisys.langsource.vera.PragmaDecl;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.util.PreprocessedToken;
import com.newisys.parser.util.Token;
import com.newisys.parser.verapp.VeraPPMacro;
import com.newisys.parser.verapp.VeraPPParser;
import com.newisys.parser.verapp.VeraPPParserConstants;

/**
 * Vera parser handler for Vera preprocessor callbacks. Adds comment and
 * preprocessor #define tracking to VeraParserBasePPHandler.
 * 
 * @author Trevor Robinson
 */
public class VeraParserPPHandler
    extends VeraParserBasePPHandler
{
    private final List<CompilationUnitDeclMember> preprocDecls;
    private VeraToken nextToken;
    private PreprocessedToken firstCommentToken;
    private PreprocessedToken prevCommentToken;
    private CompilationUnitDeclMember preprocDeclOnLine;
    private boolean tokenOrCommentOnLine;

    protected boolean markDefinesLocal;
    protected boolean markVerilogImport;

    public VeraParserPPHandler(VeraParserTokenManager tm)
    {
        super(tm);
        preprocDecls = new LinkedList<CompilationUnitDeclMember>();
    }

    public void processToken(VeraPPParser parser, PreprocessedToken t)
    {
        // preprocessor directives should be separated from tokens by newline
        assert (preprocDeclOnLine == null);

        if (!isSkipped() && t.kind != VeraPPParserConstants.WS)
        {
            if (nextToken != null)
            {
                tm.pushToken(nextToken);
            }
            nextToken = translateToken(parser, t);
            tokenOrCommentOnLine = true;
        }
    }

    public void processComment(VeraPPParser parser, PreprocessedToken t)
    {
        if (!isSkipped())
        {
            addCommentToken(parser, t);
        }
    }

    public void processEndOfLine(VeraPPParser parser, PreprocessedToken t)
    {
        if (preprocDeclOnLine != null)
        {
            applyPreprocComments(false);
        }
        preprocDeclOnLine = null;

        if (nextToken != null)
        {
            nextToken.trailingComments = firstCommentToken;
            clearCommentTokens();
            tm.pushToken(nextToken);
            nextToken = null;
        }
        else if (!tokenOrCommentOnLine
            && (prevCommentToken == null || prevCommentToken.kind != t.kind))
        {
            addCommentToken(parser, t);
        }
        tokenOrCommentOnLine = false;
    }

    public void processEndOfFile(VeraPPParser parser, PreprocessedToken t)
    {
        // treat EOF like EOL for purposes of comment/token handling
        processEndOfLine(parser, t);

        // don't want to apply comments across files
        clearCommentTokens();

        super.processEndOfFile(parser, t);
    }

    public void processInclude(
        VeraPPParser parser,
        String path,
        boolean sysPath,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        super.processInclude(parser, path, sysPath, t1, t2);
        clearCommentTokens();
    }

    public void processDefine(
        VeraPPParser parser,
        VeraPPMacro macro,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        // preprocessor directives should be separated from tokens by newline
        assert (nextToken == null);

        super.processDefine(parser, macro, t1, t2);
        if (!isSkipped())
        {
            DefineDecl defineDecl = new DefineDecl(macro);
            defineDecl.setLocal(markDefinesLocal);
            defineDecl.setVerilogImport(markVerilogImport);
            fillInPreprocessedToken(parser, t1);
            defineDecl.setBeginLocation(t1);
            fillInPreprocessedToken(parser, t2);
            defineDecl.setEndLocation(t2);
            preprocDeclOnLine = defineDecl;
            applyPreprocComments(true);
            preprocDecls.add(preprocDeclOnLine);
        }
        clearCommentTokens();
    }

    public void processUndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processUndef(parser, name, t1, t2);
        clearCommentTokens();
    }

    public void processIfdef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processIfdef(parser, name, t1, t2);
        clearCommentTokens();
    }

    public void processIfndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processIfndef(parser, name, t1, t2);
        clearCommentTokens();
    }

    public void processIf(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processIf(parser, condition, t1, t2);
        clearCommentTokens();
    }

    public void processElif(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        super.processElif(parser, condition, t1, t2);
        clearCommentTokens();
    }

    public void processElse(VeraPPParser parser, PreprocessedToken t)
        throws ParseException
    {
        super.processElse(parser, t);
        clearCommentTokens();
    }

    public void processEndif(VeraPPParser parser, PreprocessedToken t)
        throws ParseException
    {
        super.processEndif(parser, t);
        clearCommentTokens();
    }

    public void processPragma(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        // preprocessor directives should be separated from tokens by newline
        assert (nextToken == null);

        super.processPragma(parser, text, t1, t2);
        if (!isSkipped())
        {
            PragmaDecl pragmaDecl = new PragmaDecl(text);
            fillInPreprocessedToken(parser, t1);
            pragmaDecl.setBeginLocation(t1);
            fillInPreprocessedToken(parser, t2);
            pragmaDecl.setEndLocation(t2);
            preprocDeclOnLine = pragmaDecl;
            applyPreprocComments(true);
            preprocDecls.add(preprocDeclOnLine);
        }
        clearCommentTokens();
    }

    public void processLine(
        VeraPPParser parser,
        int lineNo,
        String path,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processLine(parser, lineNo, path, t1, t2);
        clearCommentTokens();
    }

    public void processError(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        super.processError(parser, text, t1, t2);
        clearCommentTokens();
    }

    public void processWarning(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        super.processWarning(parser, text, t1, t2);
        clearCommentTokens();
    }

    protected VeraToken translateToken(VeraPPParser parser, PreprocessedToken t)
    {
        VeraToken t2 = super.translateToken(parser, t);
        t2.leadingComments = firstCommentToken;
        if (!preprocDecls.isEmpty())
        {
            t2.addPreprocDecls(preprocDecls);
            preprocDecls.clear();
        }
        clearCommentTokens();
        return t2;
    }

    private void applyPreprocComments(boolean leading)
    {
        Token ct = firstCommentToken;
        while (ct != null)
        {
            preprocDeclOnLine.applyComment(ct, leading, true);
            ct = ct.next;
        }
        clearCommentTokens();
    }

    private void addCommentToken(VeraPPParser parser, PreprocessedToken t)
    {
        fillInPreprocessedToken(parser, t);
        PreprocessedToken t2 = (PreprocessedToken) t.clone();
        t2.next = null;
        t2.specialToken = prevCommentToken;
        if (prevCommentToken != null) prevCommentToken.next = t2;
        prevCommentToken = t2;
        if (firstCommentToken == null) firstCommentToken = t2;
        tokenOrCommentOnLine = true;
    }

    private void clearCommentTokens()
    {
        firstCommentToken = prevCommentToken = null;
    }
}
