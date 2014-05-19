/*
 * Juno - OpenVera (TM) to Jove Translator
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

package com.newisys.juno;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.newisys.langsource.SourceObject;
import com.newisys.langsource.vera.MacroDecl;
import com.newisys.parser.util.IncludeLocation;
import com.newisys.parser.util.Macro;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.util.PreprocessedToken;
import com.newisys.parser.util.Token;
import com.newisys.parser.vera.VeraParser;
import com.newisys.parser.vera.VeraParserBasePPHandler;
import com.newisys.parser.vera.VeraParserPPHandler;
import com.newisys.parser.vera.VeraParserReusableTokenManager;
import com.newisys.parser.vera.VeraParserTokenManager;
import com.newisys.parser.verapp.PathResolver;
import com.newisys.parser.verapp.VeraPPMacro;
import com.newisys.parser.verapp.VeraPPParser;
import com.newisys.schemabuilder.vera.VeraPreprocessorInfo;

/**
 * Juno translator parser handler for Vera preprocessor callbacks.
 * Extends VeraParserPPHandler with translator-specific capabilities, like
 * attempting to parse #define macros and resolving included header (.vrh)
 * files to the source (.vr) file.
 * 
 * @author Trevor Robinson
 */
public class JunoPPHandler
    extends VeraParserPPHandler
    implements VeraPreprocessorInfo
{
    private static final Logger logger = Logger
        .getLogger("JunoPPHandler");

    private final PathResolver importPathResolver;
    private final Set<String> headerSourceFiles = new HashSet<String>();
    private boolean inHeaderSource;
    private boolean inLocal;
    private Map<String, VeraPPMacro> localDefines;

    public JunoPPHandler(VeraParserTokenManager tm)
    {
        super(tm);
        importPathResolver = new PathResolver();
    }

    public void addImportPath(String path)
    {
        importPathResolver.addSearchPath(path);
    }

    public Set<String> getImportPaths()
    {
        return importPathResolver.getSearchPaths();
    }

    public boolean isFromHeader(SourceObject obj)
    {
        String filename = obj.getBeginFilename();
        if (headerSourceFiles.contains(filename)) return true;

        IncludeLocation loc = obj.getIncludeLocation();
        while (loc != null)
        {
            filename = loc.filename;
            if (headerSourceFiles.contains(filename)) return true;
            loc = loc.includedFrom;
        }

        return false;
    }

    public String getCompilationUnit(SourceObject obj)
    {
        String filename = obj.getBeginFilename();
        if (!isIncludedSource(filename)) return filename;

        IncludeLocation loc = obj.getIncludeLocation();
        while (loc != null)
        {
            filename = loc.filename;
            if (!isIncludedSource(filename)) return filename;
            loc = loc.includedFrom;
        }

        return filename;
    }

    private boolean isIncludedSource(String filename)
    {
        return filename.endsWith(".vr")
            && !headerSourceFiles.contains(filename);
    }

    public MacroDecl parseMacro(Macro macro)
    {
        MacroDecl expansionDecl = null;
        String expansion = macro.expand();

        VeraParserReusableTokenManager tm = new VeraParserReusableTokenManager();
        VeraParserBasePPHandler handler = new VeraParserBasePPHandler(tm,
            getDefines());
        VeraPPParser macroPPParser = new VeraPPParser(new StringReader(
            expansion), handler);
        try
        {
            // preprocess the macro expansion into the token manager
            macroPPParser.file();

            // attempt to parse the preprocessed macro expansion
            VeraParser macroParser = new VeraParser(tm);
            expansionDecl = macroParser.macro_defn();
        }
        catch (ParseException e)
        {
            // ignored; could be preprocessor encountering a Verilog-style
            // reference to an undefined macro, or the parser not recognizing
            // a supported macro form (statement, expression, range, type)
        }

        return expansionDecl;
    }

    @Override
    public void addDefine(VeraPPMacro macro)
    {
        if (localDefines != null)
        {
            localDefines.put(macro.getName(), macro);
        }
        if (!inLocal)
        {
            super.addDefine(macro);
        }
    }

    @Override
    public void removeDefine(String name)
    {
        if (localDefines != null)
        {
            localDefines.remove(name);
        }
        if (!inLocal)
        {
            super.removeDefine(name);
        }
    }

    @Override
    public VeraPPMacro getDefine(String name)
    {
        if (localDefines != null)
        {
            return localDefines.get(name);
        }
        else
        {
            return super.getDefine(name);
        }
    }

    @Override
    public void processComment(VeraPPParser parser, PreprocessedToken t)
    {
        boolean specialComment = false;
        if (inHeaderSource)
        {
            if (t.image.startsWith("//LOCAL"))
            {
                logger.fine("//LOCAL" + getLocationMsg(parser, t));
                if (!inLocal)
                {
                    inLocal = true;
                    markDefinesLocal = true;
                    if (localDefines == null)
                    {
                        localDefines = copyDefines();
                    }
                }
                else
                {
                    doWarning("Ignoring nested //LOCAL");
                }
                specialComment = true;
            }
            else if (t.image.startsWith("//END_LOCAL"))
            {
                logger.fine("//END_LOCAL" + getLocationMsg(parser, t));
                if (inLocal)
                {
                    inLocal = false;
                    markDefinesLocal = false;
                }
                else
                {
                    doWarning("//END_LOCAL without //LOCAL");
                }
                specialComment = true;
            }
        }
        if (!specialComment)
        {
            super.processComment(parser, t);
        }
    }

    private static String getLocationMsg(VeraPPParser parser, Token t)
    {
        return " at " + parser.getFilename() + ", line " + t.beginLine;
    }

    @Override
    public void processInclude(
        VeraPPParser parser,
        String path,
        boolean sysPath,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        if (!isSkipped())
        {
            // remember whether we were in header source before this include
            boolean wasInHeaderSource = inHeaderSource;
            boolean wasMarkVerilogImport = markVerilogImport;

            boolean found = false;
            String sourcePath = null;
            boolean popLocalDefines = false;
            search: try
            {
                String foundPath;

                // try to resolve using imported include
                if (!sysPath)
                {
                    foundPath = importPathResolver.resolve(path);
                    if (foundPath != null)
                    {
                        path = foundPath;
                        found = true;
                        markVerilogImport = true;
                        break search;
                    }
                }

                // try to resolve path normally
                foundPath = resolvePath(path, sysPath);
                if (foundPath != null)
                {
                    path = foundPath;
                    found = true;
                    markVerilogImport = false;
                    break search;
                }

                // try to resolve user .vrh files using the source .vr file
                if (!sysPath && path.endsWith(".vrh"))
                {
                    // search for .vr file in user search paths
                    sourcePath = path.substring(0, path.length() - 1);
                    foundPath = userPathResolver.resolve(sourcePath);
                    if (foundPath != null)
                    {
                        path = foundPath;
                        found = true;
                        markVerilogImport = false;

                        // do nothing if we have already included this file
                        if (headerSourceFiles.contains(path))
                        {
                            logger.fine("Skipping included source path: "
                                + path);
                            return;
                        }

                        // use .vr file as include file
                        logger.fine("Adding included source path: " + path);
                        headerSourceFiles.add(path);
                        inHeaderSource = true;

                        // if including source for a header, and local defines
                        // are not already active, pop them on completion of
                        // this file
                        popLocalDefines = (localDefines == null);
                    }
                }
            }
            catch (IOException e)
            {
                throw new ParseException("Error resolving include path: "
                    + e.getMessage());
            }

            if (!found)
            {
                System.err.println("Include file not found: " + path);
                if (!sysPath)
                {
                    System.err.println("  Searched imported include paths: "
                        + importPathResolver.getSearchPaths());
                    System.err.println("  Searched user include paths: "
                        + userPathResolver.getSearchPaths());
                }
                System.err.println("  Searched system include paths: "
                    + sysPathResolver.getSearchPaths());
                if (sourcePath != null)
                {
                    System.err.println("  Searched user include paths "
                        + "for source file: " + sourcePath);
                }
            }

            doInclude(parser, path);

            // pop local defines if they were established for this file
            if (popLocalDefines && localDefines != null)
            {
                localDefines = null;
                if (inLocal)
                {
                    doWarning("//LOCAL without //END_LOCAL");
                    inLocal = false;
                }
            }

            // restore file flags
            inHeaderSource = wasInHeaderSource;
            markVerilogImport = wasMarkVerilogImport;
        }
    }
}
