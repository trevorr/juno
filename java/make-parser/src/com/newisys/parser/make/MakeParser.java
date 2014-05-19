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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses the give makefile and updates the given makefile database with the
 * definitions in it.
 * 
 * @author Trevor Robinson
 */
public class MakeParser
{
    private final MakeDatabase database;
    private final LineNumberReader reader;
    private final String filename;
    private int condDepth;
    private int skipDepth;

    private static final Map functions = new LinkedHashMap();

    public static MakeFunction getFunction(String name)
    {
        return (MakeFunction) functions.get(name);
    }

    private static void addFunction(MakeFunction func)
    {
        functions.put(func.getName(), func);
    }

    static
    {
        addFunction(new MakeFunction.Addprefix());
        addFunction(new MakeFunction.Addsuffix());
        addFunction(new MakeFunction.Basename());
        addFunction(new MakeFunction.Call());
        addFunction(new MakeFunction.Dir());
        addFunction(new MakeFunction.Error());
        addFunction(new MakeFunction.Eval());
        addFunction(new MakeFunction.Filter());
        addFunction(new MakeFunction.FilterOut());
        addFunction(new MakeFunction.Findstring());
        addFunction(new MakeFunction.Firstword());
        addFunction(new MakeFunction.Foreach());
        addFunction(new MakeFunction.If());
        addFunction(new MakeFunction.Join());
        addFunction(new MakeFunction.Notdir());
        addFunction(new MakeFunction.Origin());
        addFunction(new MakeFunction.Patsubst());
        addFunction(new MakeFunction.Shell());
        addFunction(new MakeFunction.Sort());
        addFunction(new MakeFunction.Strip());
        addFunction(new MakeFunction.Subst());
        addFunction(new MakeFunction.Suffix());
        addFunction(new MakeFunction.Value());
        addFunction(new MakeFunction.Warning());
        addFunction(new MakeFunction.Wildcard());
        addFunction(new MakeFunction.Word());
        addFunction(new MakeFunction.Wordlist());
        addFunction(new MakeFunction.Words());
    }

    public MakeParser(MakeDatabase database, String filename)
        throws FileNotFoundException
    {
        this(database, new FileReader(filename), filename);
    }

    public MakeParser(MakeDatabase database, Reader reader, String filename)
    {
        this.database = database;
        this.reader = new LineNumberReader(reader);
        this.reader.setLineNumber(1);
        this.filename = filename;
    }

    public MakeDatabase getDatabase()
    {
        return database;
    }

    public String getFilename()
    {
        return filename;
    }

    public void parse()
        throws IOException, MakeParseException
    {
        Set activeRules = new HashSet();
        StringBuffer commandBuffer = null;

        while (true)
        {
            // read the next line,
            // collapsing any backslash-newline continuations
            int lineNo = reader.getLineNumber();
            String line = readLine();

            // done if we hit EOF
            if (line == null) break;

            // continue with next line if this one is empty
            if (line.length() == 0) continue;

            try
            {
                // process commands (if expected)
                if (line.charAt(0) == '\t' && commandBuffer != null)
                {
                    if (skipDepth == 0)
                    {
                        BlankTokenizer bt = new BlankTokenizer(line);
                        if (bt.hasNextToken())
                        {
                            String command = bt.restOfLine();
                            if (commandBuffer.length() > 0)
                                commandBuffer.append('\n');
                            commandBuffer.append(command);
                        }
                    }
                    continue;
                }

                // ignore line after comments (for now; if line contains a rule
                // with a semicolon, we'll need the whole line)
                StringBuffer lineBuffer = new StringBuffer(line);
                int commentStart = MakeUtil.unescapeIndexOf(lineBuffer, '#');
                if (commentStart >= 0)
                {
                    lineBuffer.setLength(commentStart);
                }
                int lineBufferEnd = lineBuffer.length();

                // get first token
                BlankTokenizer bt = new BlankTokenizer(lineBuffer, 0,
                    lineBufferEnd);
                if (!bt.hasNextToken())
                {
                    // line contains only spaces
                    continue;
                }
                String token1 = bt.nextToken();

                boolean inverse = false;
                if (token1.equals("ifdef")
                    || (token1.equals("ifndef") && (inverse = true)))
                {
                    // get variable name
                    if (!bt.hasNextToken())
                    {
                        throw new MakeParseException(
                            "Variable name expected after " + token1);
                    }
                    String varText = bt.nextToken();

                    // check for extraneous text
                    if (bt.hasNextToken())
                    {
                        throw new MakeParseException("Extraneous text after "
                            + token1);
                    }

                    // handle conditional
                    ++condDepth;
                    if (skipDepth > 0 || isDefined(varText) == inverse)
                    {
                        ++skipDepth;
                    }

                    // done with line
                    continue;
                }
                else if (token1.equals("ifeq")
                    || (token1.equals("ifneq") && (inverse = true)))
                {
                    // find opening delimiter
                    int ts = bt.getNextTokenStart();
                    char openParen = (ts < lineBufferEnd) ? lineBuffer
                        .charAt(ts) : 0;
                    if (openParen != '(' && openParen != '\''
                        && openParen != '"')
                    {
                        throw new MakeParseException(
                            "Delimiter expected after " + token1);
                    }

                    // extract argument 1
                    int argPos1 = ts + 1;
                    int argEndPos1;
                    if (openParen == '(')
                    {
                        // search for comma delimiter
                        argEndPos1 = MakeUtil.indexOfArgumentEnd(lineBuffer,
                            '(', ')', argPos1, lineBufferEnd);
                        if (argEndPos1 < lineBufferEnd)
                        {
                            // trim trailing blanks from argument 1
                            while (MakeUtil.isBlank(lineBuffer
                                .charAt(argEndPos1 - 1)))
                                --argEndPos1;
                        }
                        else
                        {
                            // comma delimiter not found
                            argEndPos1 = -1;
                        }
                    }
                    else
                    {
                        // search for quote delimiter
                        argEndPos1 = MakeUtil.indexOfChar(lineBuffer,
                            openParen, argPos1);
                    }
                    if (argEndPos1 < 0)
                    {
                        throw new MakeParseException(
                            "Delimiter not found after " + token1
                                + " argument 1");
                    }
                    String arg1 = lineBuffer.substring(argPos1, argEndPos1);

                    // extract argument 2
                    int argPos2 = argEndPos1 + 1;
                    while (argPos2 < lineBufferEnd
                        && MakeUtil.isBlank(lineBuffer.charAt(argPos2)))
                        ++argPos2;
                    int argEndPos2;
                    if (openParen == '(')
                    {
                        // search for closing parenthesis delimiter
                        int parenCount = 0;
                        for (argEndPos2 = argPos2; argEndPos2 < lineBufferEnd; ++argEndPos2)
                        {
                            char c = lineBuffer.charAt(argEndPos2);
                            if (c == '(')
                            {
                                ++parenCount;
                            }
                            else if (c == ')' && --parenCount < 0)
                            {
                                break;
                            }
                        }
                        if (argEndPos2 == lineBufferEnd)
                        {
                            // closing parenthesis delimiter not found
                            argEndPos1 = -1;
                        }
                    }
                    else
                    {
                        // find opening delimiter
                        openParen = (argPos2 < lineBufferEnd) ? lineBuffer
                            .charAt(argPos2) : 0;
                        if (openParen != '\'' && openParen != '"')
                        {
                            throw new MakeParseException(
                                "Delimiter not found before " + token1
                                    + " argument 2");
                        }
                        ++argPos2;

                        // search for quote delimiter
                        argEndPos2 = MakeUtil.indexOfChar(lineBuffer,
                            openParen, argPos2);
                    }
                    if (argEndPos2 < 0)
                    {
                        throw new MakeParseException(
                            "Delimiter not found after " + token1
                                + " argument 2");
                    }
                    String arg2 = lineBuffer.substring(argPos2, argEndPos2);

                    // check for extraneous text
                    do
                    {
                        ++argEndPos2;
                    }
                    while (argEndPos2 < lineBufferEnd
                        && MakeUtil.isBlank(lineBuffer.charAt(argEndPos2)));
                    if (argEndPos2 < lineBufferEnd)
                    {
                        throw new MakeParseException("Extraneous text after "
                            + token1);
                    }

                    // handle conditional
                    ++condDepth;
                    if (skipDepth > 0 || isEqual(arg1, arg2) == inverse)
                    {
                        ++skipDepth;
                    }

                    // done with line
                    continue;
                }
                else if (token1.equals("else"))
                {
                    // check for extraneous text
                    if (bt.hasNextToken())
                    {
                        throw new MakeParseException("Extraneous text after "
                            + token1);
                    }

                    // handle conditional
                    if (condDepth == 0)
                    {
                        throw new MakeParseException("else without if");
                    }
                    if (skipDepth <= 1)
                    {
                        skipDepth ^= 1;
                    }

                    // done with line
                    continue;
                }
                else if (token1.equals("endif"))
                {
                    // check for extraneous text
                    if (bt.hasNextToken())
                    {
                        throw new MakeParseException("Extraneous text after "
                            + token1);
                    }

                    // handle conditional
                    if (condDepth == 0)
                    {
                        throw new MakeParseException("endif without if");
                    }
                    --condDepth;
                    if (skipDepth > 0)
                    {
                        --skipDepth;
                    }

                    // done with line
                    continue;
                }
                else if (token1.equals("define"))
                {
                    processDefine(bt, MakeVariableOrigin.FILE);

                    // done with line
                    continue;
                }
                else if (token1.equals("endef"))
                {
                    // valid endefs are handled by processDefine()
                    throw new MakeParseException("Extraneous endef");
                }
                else if (token1.equals("override"))
                {
                    // get next token
                    if (!bt.hasNextToken())
                    {
                        throw new MakeParseException("Empty override");
                    }
                    int ts2 = bt.getNextTokenStart();
                    String token2 = bt.nextToken();

                    // handle define or assignment
                    if (token2.equals("define"))
                    {
                        processDefine(bt, MakeVariableOrigin.OVERRIDE);
                    }
                    else
                    {
                        MakeVariable var = processAssignment(lineBuffer, ts2,
                            lineBufferEnd, MakeVariableOrigin.OVERRIDE);
                        if (var == null)
                        {
                            throw new MakeParseException(
                                "Invalid override syntax");
                        }
                    }

                    // done with line
                    continue;
                }

                // only conditionals and defines are processed inside skipped
                // conditional blocks
                if (skipDepth > 0)
                {
                    continue;
                }

                boolean gotDirective = true;
                if (token1.equals("export"))
                {
                    if (!bt.hasNextToken())
                    {
                        // no more tokens; export all variables
                        database.setExportAll(true);
                    }
                    else
                    {
                        // look for exported assignment
                        MakeVariable var = processAssignment(lineBuffer, bt
                            .getNextTokenStart(), lineBufferEnd,
                            MakeVariableOrigin.FILE);
                        if (var != null)
                        {
                            var.setExported(true);
                        }
                        else
                        {
                            // tokens represent a list of variables to export
                            String varText = bt.restOfLine();
                            processExportList(varText, true);
                        }
                    }
                }
                else if (token1.equals("unexport"))
                {
                    // look for a token
                    if (!bt.hasNextToken())
                    {
                        // no more tokens; unexport all variables
                        database.setExportAll(false);
                    }
                    else
                    {
                        // tokens represent a list of variables to unexport
                        String varText = bt.restOfLine();
                        processExportList(varText, false);
                    }
                }
                else if (token1.equals("vpath"))
                {
                    String argStr = expandVariables(bt.restOfLine());
                    StringBuffer buffer = new StringBuffer(argStr);
                    PathTokenizer pt = new PathTokenizer(buffer);
                    if (pt.hasNextToken())
                    {
                        String patternStr = pt.nextToken();
                        if (pt.hasNextToken())
                        {
                            // 'vpath <pattern> <path...>' means associate given
                            // search paths with given pattern
                            MakePattern pattern = MakePattern.parse(patternStr);
                            do
                            {
                                String path = pt.nextToken();
                                database.addVPath(pattern, path);
                            }
                            while (pt.hasNextToken());
                        }
                        else
                        {
                            // 'vpath <pattern>' (no paths) means remove vpaths
                            // with the given pattern
                            database.removeVPaths(patternStr);
                        }
                    }
                    else
                    {
                        // 'vpath' (no arguments) means remove all vpaths
                        database.removeAllVPaths();
                    }
                }
                else if (token1.equals("include") || token1.equals("-include")
                    || token1.equals("sinclude"))
                {
                    boolean ignoreNotFound = token1.charAt(0) != 'i';

                    // get filename string
                    String filenames = expandVariables(bt.restOfLine());
                    if (filenames.length() == 0)
                    {
                        throw new MakeParseException("Filename expected after "
                            + token1);
                    }

                    // build list of globbed filenames
                    StringBuffer buffer = new StringBuffer(filenames);
                    PathTokenizer pt = new PathTokenizer(buffer);
                    List fileList = globPaths(pt.extractTokens());

                    // process included files
                    Iterator fileIter = fileList.iterator();
                    while (fileIter.hasNext())
                    {
                        String includeFile = (String) fileIter.next();
                        try
                        {
                            MakeParser parser = new MakeParser(database,
                                includeFile);
                            parser.parse();
                        }
                        catch (FileNotFoundException e1)
                        {
                            if (!ignoreNotFound)
                            {
                                throw new MakeParseException(
                                    "Included file not found: " + includeFile);
                            }
                        }
                    }
                }
                else
                {
                    // check whether line is an assignment
                    MakeVariable var = processAssignment(lineBuffer, 0,
                        lineBufferEnd, MakeVariableOrigin.FILE);
                    gotDirective = (var != null);
                }

                // complete last rule, if any
                if (commandBuffer != null)
                {
                    if (commandBuffer.length() > 0)
                    {
                        applyCommand(activeRules, commandBuffer.toString());
                    }
                    commandBuffer = null;
                }

                if (!gotDirective)
                {
                    if (line.charAt(0) == '\t')
                    {
                        // line started with a tab, but we have not seen a rule
                        // and the line was not a directive
                        throw new MakeParseException("Commands without rule");
                    }

                    // line must be a rule
                    commandBuffer = new StringBuffer();
                    activeRules.clear();
                    processRule(line, activeRules, commandBuffer);
                }
            }
            catch (MakeParseException e)
            {
                if (e.getFilename() == null)
                {
                    e.setFilename(filename);
                    e.setLineNumber(lineNo);
                }
                throw e;
            }
        }

        if (commandBuffer != null && commandBuffer.length() > 0)
        {
            applyCommand(activeRules, commandBuffer.toString());
        }

        if (condDepth > 0)
        {
            throw new MakeParseException("Missing endif");
        }
    }

    private void applyCommand(Collection activeRules, String command)
    {
        Iterator iter = activeRules.iterator();
        while (iter.hasNext())
        {
            MakeRule rule = (MakeRule) iter.next();
            if (rule.getCommand() != null)
            {
                warning("Overriding commands for target '"
                    + rule.getTargetNames() + "'");
            }
            rule.setCommand(command);
        }
    }

    private boolean isDefined(String varText)
        throws MakeParseException
    {
        String varName = expandVariables(varText);
        MakeVariable var = database.getVariable(varName);
        return var != null;
    }

    private boolean isEqual(String arg1, String arg2)
        throws MakeParseException
    {
        String exp1 = expandVariables(arg1);
        String exp2 = expandVariables(arg2);
        return exp1.equals(exp2);
    }

    private MakeVariable processAssignment(
        StringBuffer line,
        int start,
        int end,
        MakeVariableOrigin origin)
        throws MakeParseException
    {
        int len = line.length();

        // look for assignment operator
        boolean found = false;
        boolean cond = false;
        boolean append = false;
        boolean recursive = true;
        int namePos = -1;
        int nameEndPos = -1;
        int pos = start;
        while (pos < len)
        {
            char c1 = line.charAt(pos++);
            char c2 = (pos < len) ? line.charAt(pos) : 0;
            if (c1 == '=')
            {
                // recursive assignment
                found = true;
                break;
            }
            else if (c1 == ':')
            {
                if (c2 == '=')
                {
                    // simple assignment
                    found = true;
                    recursive = false;
                    ++pos;
                    break;
                }
                else
                {
                    // ':' without '=' indicates a rule
                    return null;
                }
            }
            else if (c1 == '+' && c2 == '=')
            {
                // append assignment
                found = true;
                append = true;
                ++pos;
                break;
            }
            else if (c1 == '?' && c2 == '=')
            {
                // conditional assignment
                found = true;
                cond = true;
                ++pos;
                break;
            }
            else if (c1 == '$' && (c2 == '(' || c2 == '{'))
            {
                // variable reference; advance past matching parenthesis
                char closeParen = (c2 == '(') ? ')' : '}';
                int matchPos = MakeUtil.indexOfMatchingDelimiter(line, c2,
                    closeParen, pos + 1, end);
                pos = (matchPos < 0) ? len : matchPos + 1;
            }
            else if (!MakeUtil.isBlank(c1))
            {
                // remember first non-blank as start of variable name
                if (namePos < 0)
                {
                    namePos = pos - 1;
                }
                // remember last non-blank as end of variable name
                nameEndPos = pos;
            }
        }
        if (!found || namePos < 0)
        {
            // reached end of line without finding assignment operator
            // or found assignment operator without variable name
            return null;
        }

        // extract and expand variable name
        String varText = line.substring(namePos, nameEndPos);
        String varName = expandVariables(varText);
        if (varName.length() == 0)
        {
            throw new MakeParseException("Empty variable name");
        }

        // look up existing variable with same name
        MakeVariable existingVar = database.getVariable(varName);
        if (append && existingVar != null)
        {
            // '+=' is immediate if existing variable is non-recursive
            recursive = existingVar.isRecursive();
        }

        // extract variable value
        while (pos < len && MakeUtil.isBlank(line.charAt(pos)))
            ++pos;
        String value = line.substring(pos);

        // expand variable value for simple variables
        if (!recursive && skipDepth == 0)
        {
            value = expandVariables(value);
        }

        // create variable
        MakeVariable var = new MakeVariable(varName, origin, recursive);
        var.setValue(value);

        // add variable to database if not in ignored conditional
        if (skipDepth == 0)
        {
            assignVariable(var, cond, append, existingVar);
        }

        return var;
    }

    private void processDefine(BlankTokenizer bt, MakeVariableOrigin origin)
        throws MakeParseException, IOException
    {
        // get variable name
        if (!bt.hasNextToken())
        {
            throw new MakeParseException("Variable name expected after define");
        }
        String varText = bt.restOfLineTrimmed();

        // expand variables in variable name; an empty variable name after
        // expansion is allowed for defines
        String varName = expandVariables(varText);

        // process the define up to the matching endef
        MakeVariable var = processDefineBody(varName, origin);

        // add variable to database if not in ignored conditional
        if (skipDepth == 0)
        {
            assignVariable(var, false, false);
        }
    }

    private MakeVariable processDefineBody(
        String varName,
        MakeVariableOrigin origin)
        throws IOException, MakeParseException
    {
        StringBuffer buffer = new StringBuffer();
        int defineLevel = 1;
        while (true)
        {
            // read the next line,
            // collapsing any backslash-newline continuations
            String line = readLine();
            if (line == null) break;

            // look for endef or nested define
            if (line.charAt(0) != '\t')
            {
                // find first token
                BlankTokenizer bt = new BlankTokenizer(line);
                String token1 = bt.hasNextToken() ? bt.nextToken() : "";
                if (token1.equals("define"))
                {
                    ++defineLevel;
                }
                else if (token1.equals("endef"))
                {
                    // check for extraneous text (not including comments)
                    if (bt.hasNextToken()
                        && line.charAt(bt.getNextTokenStart()) != '#')
                    {
                        throw new MakeParseException("Extraneous text after "
                            + token1);
                    }

                    // handle endef
                    if (--defineLevel == 0)
                    {
                        MakeVariable var = new MakeVariable(varName, origin,
                            true);
                        var.setValue(buffer.toString());
                        return var;
                    }
                }
            }

            // separate lines with newline characters
            if (buffer.length() > 0)
            {
                buffer.append('\n');
            }

            // append line to value
            buffer.append(line);
        }

        throw new MakeParseException("Missing endef after define");
    }

    private void processExportList(String varText, boolean export)
        throws MakeParseException
    {
        String varNames = expandVariables(varText);
        BlankTokenizer bt = new BlankTokenizer(varNames);
        while (bt.hasNextToken())
        {
            // get next variable name
            String varName = bt.nextToken();

            // look up variable name in database
            MakeVariable var = database.getVariable(varName);

            // if variable does not exist, create an empty simple variable
            if (var == null)
            {
                var = new MakeVariable(varName, MakeVariableOrigin.FILE, false);
                database.addVariable(var);
            }

            // flag variable to be exported
            var.setExported(export);
        }
    }

    private void processRule(
        String line,
        Set activeRules,
        StringBuffer commandBuffer)
        throws MakeParseException
    {
        String semiCmd = null;

        // rule ends at first semicolon or comment, if present
        StringBuffer lineBuffer = new StringBuffer(line);
        int ruleEndPos = MakeUtil.unescapeIndexOf(lineBuffer, ';', '#');
        if (ruleEndPos >= 0)
        {
            if (lineBuffer.charAt(ruleEndPos) == ';')
            {
                semiCmd = lineBuffer.substring(ruleEndPos + 1);
            }
            lineBuffer.setLength(ruleEndPos);
        }
        int lineBufferEnd = lineBuffer.length();

        // expand target tokens until we find a (double) colon
        StringBuffer targetBuffer = new StringBuffer();
        StringBuffer depBuffer = new StringBuffer();
        boolean gotColon = false;
        boolean doubleColon = false;
        RuleTokenizer rt = new RuleTokenizer(lineBuffer, 0, lineBufferEnd);
        while (rt.hasNextToken() && !gotColon)
        {
            RuleTokenType tokenType = rt.getNextTokenType();
            String token = rt.nextToken();

            // done if we found a literal (double) colon
            doubleColon = tokenType == RuleTokenType.DOUBLE_COLON;
            if (tokenType == RuleTokenType.COLON || doubleColon)
            {
                gotColon = true;
                break;
            }

            // expand any variables in the token
            StringBuffer expansion = new StringBuffer(expandVariables(token));

            // check for a semicolon command in the expansion
            boolean gotSemiCmd = false;
            if (semiCmd == null)
            {
                ruleEndPos = MakeUtil.unescapeIndexOf(expansion, ';');
                if (ruleEndPos >= 0)
                {
                    // split expansion around semicolon and include
                    // rest of line (expanded) in semicolon command
                    semiCmd = expansion.substring(ruleEndPos + 1)
                        + expandVariables(rt.restOfLine());
                    expansion.setLength(ruleEndPos);
                    gotSemiCmd = true;
                }
            }

            // check for a (double) colon in the expansion
            int colonPos = MakeUtil.unescapeIndexOf(expansion, ':');
            if (colonPos >= 0)
            {
                // check for double colon
                int prereqPos = colonPos + 1;
                if (expansion.charAt(colonPos + 1) == ':')
                {
                    doubleColon = true;
                    ++prereqPos;
                }

                // split expansion around (double) colon
                depBuffer.append(expansion.substring(prereqPos));
                expansion.setLength(colonPos);
                gotColon = true;
            }
            else if (gotSemiCmd)
            {
                // expansion contained a semicolon without a preceding colon
                break;
            }

            targetBuffer.append(expansion);
            targetBuffer.append(' ');
        }
        PathTokenizer pt = new PathTokenizer(targetBuffer);

        // we did not find a rule; make sure line was empty
        if (!gotColon)
        {
            if (pt.hasNextToken())
            {
                throw new MakeParseException("Colon not found in rule");
            }
            if (semiCmd != null)
            {
                throw new MakeParseException(
                    "Missing rule before semicolon command");
            }
            return;
        }

        // ignore rules with empty target list
        if (!pt.hasNextToken()) return;

        // build target list
        List targetNameList = globPaths(pt.extractTokens());

        // check for target-specific variable
        StringBuffer targetVarBuffer = new StringBuffer();
        targetVarBuffer.append(depBuffer);
        targetVarBuffer.append(lineBuffer.substring(rt.getNextTokenStart()));
        if (semiCmd != null) targetVarBuffer.append(semiCmd);
        if (processTargetVariable(targetNameList, targetVarBuffer))
        {
            return;
        }

        // expand dependencies
        depBuffer.append(rt.restOfLine());
        MakeUtil.unescapeIndexOf(depBuffer, '=');
        depBuffer = new StringBuffer(expandVariables(depBuffer.toString()));

        // check for a semicolon command in the expansion
        if (semiCmd == null)
        {
            ruleEndPos = MakeUtil.unescapeIndexOf(depBuffer, ';');
            if (ruleEndPos >= 0)
            {
                // split expansion around semicolon
                semiCmd = depBuffer.substring(ruleEndPos + 1);
                depBuffer.setLength(ruleEndPos);
            }
        }

        // extract paths for target pattern or dependencies
        pt = new PathTokenizer(depBuffer, ':');
        List tempList = pt.extractTokens();

        // check for static pattern rule
        MakePattern staticPattern = null;
        if (pt.isHitStopChar())
        {
            // get target pattern
            int size = tempList.size();
            if (size == 0)
            {
                throw new MakeParseException("Missing target pattern");
            }
            else if (size > 1)
            {
                throw new MakeParseException("Multiple target patterns");
            }
            String patternString = (String) tempList.get(0);
            staticPattern = MakePattern.parse(patternString);

            // extract paths for dependencies
            pt = new PathTokenizer(depBuffer, pt.getLineEnd() + 1, depBuffer
                .length());
            tempList = pt.extractTokens();
        }

        // build dependency list
        List depNameList = globPaths(tempList);

        boolean firstTarget = true;
        MakePatternRule patternRule = null;
        List depList = null;
        Iterator targetNameIter = targetNameList.iterator();
        while (targetNameIter.hasNext())
        {
            String targetName = (String) targetNameIter.next();
            MakePattern targetPattern = MakePattern.parse(targetName);
            boolean isPatternRule = !targetPattern.isStatic();

            // check for invalid combination of target specifiers
            if (firstTarget)
            {
                if (isPatternRule && staticPattern != null)
                {
                    throw new MakeParseException(
                        "Mixed implicit and static pattern rules");
                }
            }
            else
            {
                if (isPatternRule != (patternRule != null))
                {
                    throw new MakeParseException(
                        "Mixed implicit and normal rules");
                }
            }

            if (isPatternRule)
            {
                if (patternRule == null)
                {
                    patternRule = new MakePatternRule();
                    patternRule.setDoubleColon(doubleColon);
                }
                patternRule.addTargetPattern(targetPattern);
            }
            else
            {
                if (staticPattern != null)
                {
                    if (!staticPattern.matches(targetName))
                    {
                        throw new MakeParseException("Target '" + targetName
                            + "' does not match pattern '" + staticPattern
                            + "'");
                    }
                    String stub = staticPattern.extractStub(targetName);
                    depList = buildDepList(depNameList, stub);
                }
                else if (depList == null)
                {
                    depList = buildDepList(depNameList, null);
                }

                MakeFileInfo target = database.getOrCreateFile(targetName);
                MakeStaticRule existingRule = target.getRule();
                if (existingRule != null
                    && existingRule.isDoubleColon() != doubleColon)
                {
                    throw new MakeParseException("Target '" + targetName
                        + "' has both : and :: rules");
                }
                MakeStaticRule rule = existingRule;
                if (existingRule == null || doubleColon)
                {
                    rule = new MakeStaticRule(target);
                    rule.setDoubleColon(doubleColon);
                    if (existingRule == null)
                    {
                        target.setRule(rule);
                        database.addStaticRule(rule);
                    }
                    else
                    {
                        existingRule.addDoubleColonRule(rule);
                    }
                }
                rule.addDependencies(depList);
                activeRules.add(rule);
            }

            firstTarget = false;
        }
        if (patternRule != null)
        {
            Iterator depNameIter = depNameList.iterator();
            while (depNameIter.hasNext())
            {
                String depName = (String) depNameIter.next();
                MakePattern depPattern = MakePattern.parse(depName);
                patternRule.addDependencyPattern(depPattern);
            }
            database.addPatternRule(patternRule);
            activeRules.add(patternRule);
        }
        if (semiCmd != null)
        {
            commandBuffer.append(semiCmd);
        }
    }

    private List buildDepList(List depNameList, String patternStub)
    {
        List depList = new LinkedList();
        Iterator iter = depNameList.iterator();
        while (iter.hasNext())
        {
            String depName = (String) iter.next();
            if (patternStub != null)
            {
                MakePattern depPattern = MakePattern.parse(depName);
                depName = depPattern.replaceStub(patternStub);
            }
            MakeFileInfo depFileInfo = database.getOrCreateFile(depName);
            depList.add(depFileInfo);
        }
        return depList;
    }

    private List globPaths(List origList)
    {
        List newList = new LinkedList();
        Iterator iter = origList.iterator();
        while (iter.hasNext())
        {
            String path = (String) iter.next();
            String[] paths = FileGlobber.glob(path);
            if (paths.length > 0)
            {
                for (int i = 0; i < paths.length; ++i)
                {
                    newList.add(paths[i]);
                }
            }
            else
            {
                newList.add(path);
            }
        }
        return newList;
    }

    private boolean processTargetVariable(
        List targetList,
        StringBuffer lineBuffer)
    {
        RuleTokenizer rt = new RuleTokenizer(lineBuffer);

        MakeVariableOrigin origin = MakeVariableOrigin.FILE;
        RuleTokenType tokenType = rt.getNextTokenType();
        String token = rt.nextToken();

        if (tokenType == RuleTokenType.TEXT && token.equals("override"))
        {
            origin = MakeVariableOrigin.OVERRIDE;
            tokenType = rt.getNextTokenType();
            token = rt.nextToken();
        }

        tokenType = rt.getNextTokenType();
        token = rt.nextToken();
        if (tokenType == RuleTokenType.ASSIGN_OP)
        {
            String value = rt.restOfLine();

            // TODO: handle target variable
            warning("target variables not currently supported");

            return true;
        }
        return false;
    }

    private void assignVariable(MakeVariable var, boolean cond, boolean append)
    {
        MakeVariable existingVar = database.getVariable(var.getName());
        assignVariable(var, cond, append, existingVar);
    }

    private void assignVariable(
        MakeVariable var,
        boolean cond,
        boolean append,
        MakeVariable existingVar)
    {
        if (existingVar == null
            || (!cond && var.getOrigin().overrides(existingVar.getOrigin())))
        {
            if (append)
            {
                existingVar.setValue(existingVar.getValue() + var.getValue());
            }
            else
            {
                database.addVariable(var);
            }
        }
    }

    public String expandVariables(String s)
        throws MakeParseException
    {
        return expandVariables(s, null);
    }

    private String expandVariables(String s, Set expandingVars)
        throws MakeParseException
    {
        int start = 0;
        int len = s.length();
        StringBuffer result = new StringBuffer(len);
        while (start < len)
        {
            // look for next variable reference
            int pos = s.indexOf('$', start);

            // substring between starting position and '$'?
            if (pos > start)
            {
                // append substring before '$' to result
                result.append(s.substring(start, pos));
            }
            // '$' not found?
            else if (pos < 0)
            {
                // append rest of input string to result and exit
                result.append(s.substring(start));
                break;
            }

            // advance position past '$'; break if end of string
            if (++pos >= len)
            {
                break;
            }

            // look at first character of reference
            char c = s.charAt(pos++);
            switch (c)
            {
            case '$':
                result.append(c);
                break;
            case '(':
            case '{':
                // find first terminating character
                char closeParen = (c == '(') ? ')' : '}';
                int closePos = s.indexOf(closeParen, pos);
                if (closePos < 0)
                {
                    throw new MakeParseException(
                        "Unterminated variable reference");
                }

                // look for function name
                int funcEndPos = pos;
                while (MakeUtil.isFuncChar(s.charAt(funcEndPos)))
                    ++funcEndPos;
                if (MakeUtil.isBlank(s.charAt(funcEndPos)))
                {
                    String funcName = s.substring(pos, funcEndPos);
                    MakeFunction func = getFunction(funcName);
                    if (func != null)
                    {
                        // find matching delimiter
                        int matchPos = MakeUtil.indexOfMatchingDelimiter(s, c,
                            closeParen, pos, len);
                        if (matchPos < 0)
                        {
                            throw new MakeParseException(
                                "Unterminated call to function '" + funcName
                                    + "'");
                        }
                        pos = matchPos + 1;

                        // parse arguments
                        int argPos = funcEndPos + 1;
                        while (MakeUtil.isBlank(s.charAt(argPos)))
                            ++argPos;
                        List argList = new LinkedList();
                        int argCount = 0;
                        int maxArgs = func.getMaximumArgs();
                        while (argPos < matchPos)
                        {
                            int argEnd;
                            if (++argCount < maxArgs || maxArgs < 0)
                            {
                                argEnd = MakeUtil.indexOfArgumentEnd(s, c,
                                    closeParen, argPos, matchPos);
                            }
                            else
                            {
                                // if this function has a maximum argument count
                                // and it is reached, treat all remaining text
                                // as the final argument
                                argEnd = matchPos;
                            }
                            String arg = s.substring(argPos, argEnd);
                            if (func.isExpandArgs())
                            {
                                arg = expandVariables(arg, expandingVars);
                            }
                            argList.add(arg);
                            argPos = argEnd + 1;
                        }

                        // add blank argument if none found
                        if (argCount == 0)
                        {
                            argList.add("");
                            ++argCount;
                        }

                        // invoke the function
                        String[] args = new String[argCount];
                        argList.toArray(args);
                        result.append(func.expand(this, args));
                        break;
                    }
                }

                // look for nested variable reference
                String varName = null;
                int refPos = s.indexOf('$', pos);
                if (refPos >= 0)
                {
                    // find matching delimiter and expand contents
                    int matchPos = MakeUtil.indexOfMatchingDelimiter(s, c,
                        closeParen, pos, len);
                    if (matchPos >= 0)
                    {
                        varName = expandVariables(s.substring(pos, matchPos));
                        pos = matchPos + 1;
                    }
                }

                // no nested references or unmatched opening parentheses;
                // use string up to first closing parenthesis as variable name
                if (varName == null)
                {
                    varName = s.substring(pos, closePos);
                    pos = closePos + 1;
                }

                // check for substitution reference: $(xy:x=y) -> yy
                int colonPos = varName.indexOf(':');
                if (colonPos >= 0)
                {
                    int equalPos = varName.indexOf('=', colonPos + 1);
                    if (equalPos >= 0)
                    {
                        // extract parts: varName:pattern=replace
                        String patternText = varName.substring(colonPos + 1,
                            equalPos);
                        String replaceText = varName.substring(equalPos + 1);
                        varName = varName.substring(0, colonPos);

                        // look up variable
                        MakeVariable var = database.getVariable(varName);
                        if (var != null)
                        {
                            String value = expandVariable(var, expandingVars);
                            MakePattern pattern = MakePattern
                                .parse(patternText);
                            if (!pattern.isStatic())
                            {
                                MakePattern replace = MakePattern
                                    .parse(replaceText);
                                value = MakeFunction.Patsubst.expand(pattern,
                                    replace, value);
                            }
                            else
                            {
                                value = MakeFunction.Subst.expand(patternText,
                                    replaceText, value);
                            }
                            result.append(value);
                        }
                        else
                        {
                            // undefined variable
                        }
                        break;
                    }
                }

                // not a substitution reference; simply expand
                MakeVariable var = database.getVariable(varName);
                if (var != null)
                {
                    String value = expandVariable(var, expandingVars);
                    result.append(value);
                }
                else
                {
                    // undefined variable
                }
                break;
            default:
            }

            // current position becomes starting position of next iteration
            start = pos;
        }
        return result.toString();
    }

    private String expandVariables(
        String s,
        Set expandingVars,
        MakeVariable addVar)
        throws MakeParseException
    {
        if (expandingVars == null)
        {
            expandingVars = new HashSet();
        }
        expandingVars.add(addVar);
        return expandVariables(s, expandingVars);
    }

    public String expandVariable(String varName)
        throws MakeParseException
    {
        MakeVariable var = database.getVariable(varName);
        return (var != null) ? expandVariable(var, null) : "";
    }

    private String expandVariable(MakeVariable var, Set expandingVars)
        throws MakeParseException
    {
        String value = var.getValue();
        if (var.isRecursive())
        {
            if (expandingVars != null && expandingVars.contains(var))
            {
                throw new MakeParseException("Recursive variable '"
                    + var.getName() + "' references itself");
            }
            value = expandVariables(value, expandingVars, var);
        }
        return value;
    }

    public void warning(String message)
    {
        System.err.println(MakeParseException.formatMessage(filename, reader
            .getLineNumber(), message));
    }

    public void error(String message)
        throws MakeParseException
    {
        throw new MakeParseException(message);
    }

    private String readLine()
        throws IOException
    {
        String result = null;

        // loop in case line is continued with backslash-newline
        while (true)
        {
            // read a line from the file
            final String line = reader.readLine();

            // done if EOF
            if (line == null) break;

            final int len = line.length();

            // append line onto current result
            if (result == null)
            {
                // first iteration; not handling continuation
                result = line;
            }
            else
            {
                // handling continuation; join lines with a single space
                int end = result.length();
                while (--end > 0 && MakeUtil.isBlank(result.charAt(end - 1)));
                int start = 0;
                while (start < len && MakeUtil.isBlank(line.charAt(start)))
                    ++start;
                result = result.substring(0, end) + ' ' + line.substring(start);
            }

            // done if end of line is not escaped by a backslash
            if (!MakeUtil.isEscaped(line, len)) break;

            // read another line
        }

        return result;
    }
}
