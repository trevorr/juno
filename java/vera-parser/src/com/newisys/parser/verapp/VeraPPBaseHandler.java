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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.newisys.parser.util.IncludeLocation;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.util.PreprocessedToken;
import com.newisys.parser.util.Token;

/**
 * Base handler for Vera preprocessor callbacks. Performs basic functions like
 * maintaining the #define table, resolving #include directives, and #if/#ifdef
 * handling.
 * 
 * @author Trevor Robinson
 */
public class VeraPPBaseHandler
    implements VeraPPCallbacks
{
    private static final Logger logger = Logger.getLogger("VeraPPBaseHandler");

    private static class CondInfo
    {
        boolean matched = false;
        boolean gotElse = false;
    }

    protected final PathResolver sysPathResolver;
    protected final PathResolver userPathResolver;
    private final Map<String, VeraPPMacro> defines;
    private final LinkedList<CondInfo> condStack;
    private int skipDepth;
    private int includeDepth;
    private IncludeLocation includeLocation;

    public VeraPPBaseHandler()
    {
        this(new LinkedHashMap<String, VeraPPMacro>());
    }

    protected VeraPPBaseHandler(Map<String, VeraPPMacro> defines)
    {
        sysPathResolver = new PathResolver();
        userPathResolver = new PathResolver();
        this.defines = defines;
        condStack = new LinkedList<CondInfo>();
        skipDepth = 0;
        includeDepth = 0;
    }

    public void addSysPath(String path)
    {
        sysPathResolver.addSearchPath(path);
    }

    public Set<String> getSysPaths()
    {
        return sysPathResolver.getSearchPaths();
    }

    public void addUserPath(String path)
    {
        userPathResolver.addSearchPath(path);
    }

    public Set<String> getUserPaths()
    {
        return userPathResolver.getSearchPaths();
    }

    public String resolvePath(String path, boolean sysPath)
        throws IOException
    {
        String foundPath = null;
        if (!sysPath)
        {
            foundPath = userPathResolver.resolve(path);
        }
        if (foundPath == null)
        {
            foundPath = sysPathResolver.resolve(path);
        }
        return foundPath;
    }

    public void addDefine(VeraPPMacro macro)
    {
        defines.put(macro.getName(), macro);
    }

    public void removeDefine(String name)
    {
        defines.remove(name);
    }

    public VeraPPMacro getDefine(String name)
    {
        return defines.get(name);
    }

    public Map<String, VeraPPMacro> getDefines()
    {
        return defines;
    }

    protected Map<String, VeraPPMacro> copyDefines()
    {
        return new LinkedHashMap<String, VeraPPMacro>(defines);
    }

    public void processToken(VeraPPParser parser, PreprocessedToken t)
    {
        // do nothing
    }

    public void processComment(VeraPPParser parser, PreprocessedToken t)
    {
        // do nothing
    }

    public void processEndOfLine(VeraPPParser parser, PreprocessedToken t)
    {
        // do nothing
    }

    public void processEndOfFile(VeraPPParser parser, PreprocessedToken t)
    {
        // do nothing
    }

    protected void fillInPreprocessedToken(VeraPPParser parser, Token t)
    {
        PreprocessedToken ft = (PreprocessedToken) t;
        ft.filename = parser.getFilename();
        ft.includedFrom = includeLocation;
    }

    public IncludeLocation getIncludedFrom()
    {
        return includeLocation;
    }

    public void processInclude(
        VeraPPParser parser,
        String path,
        boolean sysPath,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        logDirective(parser, "#include " + path);
        if (skipDepth == 0)
        {
            String foundPath;
            try
            {
                foundPath = resolvePath(path, sysPath);
                if (foundPath == null) foundPath = path;
            }
            catch (IOException e)
            {
                throw new ParseException("Error resolving include path: "
                    + e.getMessage());
            }
            doInclude(parser, foundPath);
        }
    }

    protected void doInclude(VeraPPParser parser, String path)
        throws ParseException
    {
        ++includeDepth;

        IncludeLocation saveLocation = includeLocation;
        includeLocation = new IncludeLocation(parser.getFilename(), parser
            .getCurrentLine(), saveLocation);

        try
        {
            VeraPPParser includeParser = new VeraPPParser(path, this);
            includeParser.file();
        }
        catch (FileNotFoundException e)
        {
            throw new ParseException("Include file not found: " + path);
        }
        finally
        {
            --includeDepth;

            includeLocation = saveLocation;
        }
    }

    public void processDefine(
        VeraPPParser parser,
        VeraPPMacro macro,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#define " + macro);
        if (skipDepth == 0)
        {
            String name = macro.getName();
            VeraPPMacro existingMacro = lookupMacro(parser, name);
            if (existingMacro != null && !macro.equals(existingMacro))
            {
                StringBuffer buf = new StringBuffer();
                buf.append("Macro '");
                buf.append(name);
                buf.append("' redefined as '");
                buf.append(macro.expand());
                buf.append('\'');
                if (macro instanceof VeraPPUserMacro)
                {
                    buf.append(" at ");
                    buf.append(((VeraPPUserMacro) macro).getLocation());
                }
                buf.append("; was defined as '");
                buf.append(existingMacro.expand());
                buf.append('\'');
                if (existingMacro instanceof VeraPPUserMacro)
                {
                    buf.append(" at ");
                    buf.append(((VeraPPUserMacro) existingMacro).getLocation());
                }
                doWarning(buf.toString());
            }
            addDefine(macro);
        }
    }

    public void processUndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#undef " + name);
        if (skipDepth == 0)
        {
            removeDefine(name);
        }
    }

    public void processIfdef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#ifdef " + name);
        CondInfo condInfo = new CondInfo();
        condStack.addLast(condInfo);
        if (skipDepth == 0 && lookupMacro(parser, name) != null)
        {
            condInfo.matched = true;
        }
        else
        {
            ++skipDepth;
        }
    }

    public void processIfndef(
        VeraPPParser parser,
        String name,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#ifndef " + name);
        CondInfo condInfo = new CondInfo();
        condStack.addLast(condInfo);
        if (skipDepth == 0 && lookupMacro(parser, name) == null)
        {
            condInfo.matched = true;
        }
        else
        {
            ++skipDepth;
        }
    }

    public void processIf(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#if " + condition);
        CondInfo condInfo = new CondInfo();
        condStack.addLast(condInfo);
        if (skipDepth == 0 && condition)
        {
            condInfo.matched = true;
        }
        else
        {
            ++skipDepth;
        }
    }

    public void processElif(
        VeraPPParser parser,
        boolean condition,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        logDirective(parser, "#elif " + condition);
        if (condStack.isEmpty())
        {
            throw new ParseException("#elif without #if");
        }
        CondInfo condInfo = condStack.getLast();
        if (condInfo.gotElse)
        {
            throw new ParseException("#elif after #else");
        }
        if (skipDepth == 1 && condition && !condInfo.matched)
        {
            condInfo.matched = true;
            skipDepth = 0;
        }
        else if (skipDepth == 0)
        {
            skipDepth = 1;
        }
    }

    public void processElse(VeraPPParser parser, PreprocessedToken t)
        throws ParseException
    {
        logDirective(parser, "#else");
        if (condStack.isEmpty())
        {
            throw new ParseException("#else without #if");
        }
        CondInfo condInfo = condStack.getLast();
        if (condInfo.gotElse)
        {
            throw new ParseException("#else after #else");
        }
        condInfo.gotElse = true;
        if (skipDepth == 1 && !condInfo.matched)
        {
            condInfo.matched = true;
            skipDepth = 0;
        }
        else if (skipDepth == 0)
        {
            skipDepth = 1;
        }
    }

    public void processEndif(VeraPPParser parser, PreprocessedToken t)
        throws ParseException
    {
        logDirective(parser, "#endif");
        if (condStack.isEmpty())
        {
            throw new ParseException("#endif without #if");
        }
        condStack.removeLast();
        if (skipDepth > 0)
        {
            --skipDepth;
        }
    }

    public void processPragma(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#pragma " + text);
        // do nothing
    }

    public void processLine(
        VeraPPParser parser,
        int lineNo,
        String path,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        logDirective(parser, "#line " + lineNo + " " + path);
        // do nothing
    }

    public void processError(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
        throws ParseException
    {
        throw new ParseException(text);
    }

    public void processWarning(
        VeraPPParser parser,
        String text,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        doWarning(text);
    }

    protected void doWarning(String text)
    {
        System.err.println("Warning: " + text);
    }

    public VeraPPMacro lookupMacro(VeraPPParser parser, String name)
    {
        return getDefine(name);
    }

    public void processMacroReference(
        VeraPPParser parser,
        VeraPPMacro macro,
        PreprocessedToken t)
    {
        if (skipDepth == 0)
        {
            parser.doExpansion(macro, t);
        }
    }

    public void processMacroReference(
        VeraPPParser parser,
        VeraPPFunctionMacro macro,
        List argList,
        PreprocessedToken t1,
        PreprocessedToken t2)
    {
        if (skipDepth == 0)
        {
            parser.doExpansion(macro, argList, t1, t2);
        }
    }

    protected final boolean isSkipped()
    {
        return skipDepth != 0;
    }

    protected final int getIncludeDepth()
    {
        return includeDepth;
    }

    private static void logDirective(VeraPPParser parser, String msg)
    {
        logger.fine(msg + getLocationMsg(parser));
    }

    private static String getLocationMsg(VeraPPParser parser)
    {
        return " at " + parser.getFilename() + ", line "
            + parser.getCurrentLine();
    }
}
