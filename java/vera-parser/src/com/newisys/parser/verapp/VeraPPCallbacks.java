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

import java.util.List;

import com.newisys.parser.util.IncludeLocation;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.util.PreprocessedToken;

/**
 * Vera preprocessor callbacks.
 * 
 * @author Trevor Robinson
 */
public interface VeraPPCallbacks
{
    void processToken(VeraPPParser parser, PreprocessedToken t);

    void processComment(VeraPPParser parser, PreprocessedToken t);

    void processEndOfLine(VeraPPParser parser, PreprocessedToken t);

    void processEndOfFile(VeraPPParser parser, PreprocessedToken t);

    IncludeLocation getIncludedFrom();

    void processInclude(
        VeraPPParser parser,
        String path,
        boolean sysPath,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException;

    void processDefine(
        VeraPPParser parser,
        VeraPPMacro macro,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processUndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processIfdef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processIfndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processIf(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processElif(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException;

    void processElse(VeraPPParser parser, PreprocessedToken t)
        throws ParseException;

    void processEndif(VeraPPParser parser, PreprocessedToken t)
        throws ParseException;

    void processPragma(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processLine(
        VeraPPParser parser,
        int lineNo,
        String path,
        PreprocessedToken t1,
        PreprocessedToken t2);

    void processError(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException;

    void processWarning(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2);

    VeraPPMacro lookupMacro(VeraPPParser parser, String name);

    void processMacroReference(
        VeraPPParser parser,
        VeraPPMacro macro,
        PreprocessedToken t);

    void processMacroReference(
        VeraPPParser parser,
        VeraPPFunctionMacro macro,
        List argList,
        PreprocessedToken t1,
        PreprocessedToken t2);
}
