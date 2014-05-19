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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;

import com.newisys.util.system.SystemUtil;

/**
 * Base class for built-in makefile functions.
 * 
 * @author Trevor Robinson
 */
public abstract class MakeFunction
{
    private final boolean expandArgs;
    private final int maximumArgs;
    private final int minimumArgs;
    private final String name;

    public MakeFunction(
        String name,
        int minimumArgs,
        int maximumArgs,
        boolean expandArgs)
    {
        this.name = name;
        this.minimumArgs = minimumArgs;
        this.maximumArgs = maximumArgs;
        this.expandArgs = expandArgs;
    }

    public String getName()
    {
        return name;
    }

    public int getMinimumArgs()
    {
        return minimumArgs;
    }

    public int getMaximumArgs()
    {
        return maximumArgs;
    }

    public boolean isExpandArgs()
    {
        return expandArgs;
    }

    protected void checkArgCount(String[] args)
        throws MakeParseException
    {
        if (args.length < minimumArgs)
        {
            throw new MakeParseException("Not enough arguments to function '"
                + name + "'; " + minimumArgs + " expected");
        }
    }

    public abstract String expand(MakeParser parser, String[] args)
        throws MakeParseException;

    public static class Addprefix
        extends MakeFunction
    {
        public Addprefix()
        {
            super("addprefix", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public static String expand(final String prefix, String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    return prefix + s;
                }
            });
        }
    }

    public static class Addsuffix
        extends MakeFunction
    {
        public Addsuffix()
        {
            super("addsuffix", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public static String expand(final String suffix, String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    return s + suffix;
                }
            });
        }
    }

    public static class Basename
        extends MakeFunction
    {
        public Basename()
        {
            super("basename", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    int periodPos = s.lastIndexOf('.');
                    return (periodPos >= 0) ? s.substring(0, periodPos) : s;
                }
            });
        }
    }

    public static class Call
        extends MakeFunction
    {
        public Call()
        {
            super("call", 1, -1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            String funcName = args[0].trim();

            // check for built-in function
            MakeFunction func = MakeParser.getFunction(funcName);
            if (func != null)
            {
                String[] funcArgs = new String[args.length - 1];
                System.arraycopy(args, 1, funcArgs, 0, args.length - 1);
                return func.expand(parser, funcArgs);
            }

            MakeDatabase database = parser.getDatabase();
            database.pushNewVariableScope();
            try
            {
                // create variables for arguments
                for (int i = 0; i < args.length; ++i)
                {
                    MakeVariable var = new MakeVariable(String.valueOf(i),
                        MakeVariableOrigin.AUTOMATIC, false);
                    var.setValue(args[i]);
                    database.addVariable(var);
                }

                // expand function
                return parser.expandVariable(funcName);
            }
            finally
            {
                database.popVariableScope();
            }
        }
    }

    public static class Dir
        extends MakeFunction
    {
        public Dir()
        {
            super("dir", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    int slashPos = MakeUtil.indexOfLastSlash(s);
                    return (slashPos >= 0) ? s.substring(0, slashPos + 1)
                        : "./";
                }
            });
        }
    }

    public static class Error
        extends MakeFunction
    {
        public Error()
        {
            super("error", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            parser.error(args[0]);
            return "";
        }
    }

    public static class Eval
        extends MakeFunction
    {
        public Eval()
        {
            super("eval", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            MakeParser evalParser = new MakeParser(parser.getDatabase(),
                new StringReader(args[0]), "eval");
            try
            {
                evalParser.parse();
            }
            catch (IOException e)
            {
                throw new MakeParseException("I/O error processing 'eval'", e);
            }
            return "";
        }
    }

    public static class Filter
        extends MakeFunction
    {
        public Filter()
        {
            super("filter", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public static String expand(String patternsText, String text)
            throws MakeParseException
        {
            return filter(text, patternsText, false);
        }
    }

    public static class FilterOut
        extends MakeFunction
    {
        public FilterOut()
        {
            super("filter-out", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public static String expand(String patternsText, String text)
            throws MakeParseException
        {
            return filter(text, patternsText, true);
        }
    }

    public static class Findstring
        extends MakeFunction
    {
        public Findstring()
        {
            super("findstring", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public static String expand(String pattern, String text)
        {
            return text.indexOf(pattern) >= 0 ? pattern : "";
        }
    }

    public static class Firstword
        extends MakeFunction
    {
        public Firstword()
        {
            super("firstword", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return Wordlist.expand(0, 0, args[0]);
        }
    }

    public static class Foreach
        extends MakeFunction
    {
        public Foreach()
        {
            super("foreach", 3, 3, false);
        }

        public String expand(final MakeParser parser, final String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            final String varName = parser.expandVariables(args[0]);
            return filter(parser.expandVariables(args[1]), new WordCallback()
            {
                public String expand(String s)
                    throws MakeParseException
                {
                    MakeDatabase database = parser.getDatabase();
                    database.pushNewVariableScope();
                    try
                    {
                        MakeVariable var = new MakeVariable(varName,
                            MakeVariableOrigin.AUTOMATIC, false);
                        var.setValue(s);
                        database.addVariable(var);

                        return parser.expandVariables(args[2]);
                    }
                    finally
                    {
                        database.popVariableScope();
                    }
                }
            });
        }
    }

    public static class If
        extends MakeFunction
    {
        public If()
        {
            super("if", 2, 3, false);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            boolean cond = parser.expandVariables(args[0].trim()).length() > 0;
            String text = cond ? args[1] : (args.length > 2 ? args[2] : null);
            return (text != null) ? parser.expandVariables(text) : "";
        }
    }

    public static class Join
        extends MakeFunction
    {
        public Join()
        {
            super("join", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1]);
        }

        public String expand(String list1, String list2)
        {
            String[] words1 = split(list1);
            String[] words2 = split(list2);
            int count = Math.max(words1.length, words2.length);
            StringBuffer result = new StringBuffer(list1.length()
                + list2.length());
            for (int i = 0; i < count; ++i)
            {
                String word1 = (i < words1.length) ? words1[i] : "";
                String word2 = (i < words2.length) ? words2[i] : "";
                if (i > 0) result.append(' ');
                result.append(word1 + word2);
            }
            return result.toString();
        }
    }

    public static class Notdir
        extends MakeFunction
    {
        public Notdir()
        {
            super("notdir", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    int slashPos = MakeUtil.indexOfLastSlash(s);
                    return (slashPos >= 0) ? s.substring(slashPos + 1) : s;
                }
            });
        }
    }

    public static class Origin
        extends MakeFunction
    {
        public Origin()
        {
            super("origin", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            String varName = args[0];
            MakeVariable var = parser.getDatabase().getVariable(varName);
            return (var != null) ? var.getOrigin().toString() : "undefined";
        }
    }

    public static class Patsubst
        extends MakeFunction
    {
        public Patsubst()
        {
            super("patsubst", 3, 3, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1], args[2]);
        }

        public static String expand(String pattern, String replace, String text)
        {
            return expand(MakePattern.parse(pattern), MakePattern
                .parse(replace), text);
        }

        public static String expand(
            MakePattern pattern,
            MakePattern replace,
            String text)
        {
            String[] words = split(text);
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < words.length; ++i)
            {
                if (pattern.matches(words[i]))
                {
                    if (result.length() > 0) result.append(' ');
                    String stub = pattern.extractStub(words[i]);
                    result.append(replace.replaceStub(stub));
                }
            }
            return result.toString();
        }
    }

    public static class Shell
        extends MakeFunction
    {
        public Shell()
        {
            super("shell", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            String value;
            try
            {
                Process process = SystemUtil.execShell(args[0]);
                StringBuffer buffer = new StringBuffer(1024);
                InputStreamReader isr = new InputStreamReader(process
                    .getInputStream());
                BufferedReader br = new BufferedReader(isr);
                while (true)
                {
                    String line = br.readLine();
                    if (line == null) break;
                    if (buffer.length() > 0) buffer.append(' ');
                    buffer.append(line);
                }
                value = buffer.toString();
            }
            catch (Exception e)
            {
                parser.warning(e.getMessage());
                value = "";
            }
            return value;
        }
    }

    public static class Sort
        extends MakeFunction
    {
        public Sort()
        {
            super("sort", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
        {
            String[] words = split(text);
            Arrays.sort(words);
            return join(words);
        }
    }

    public static class Strip
        extends MakeFunction
    {
        public Strip()
        {
            super("strip", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
        {
            int len = text.length();
            StringBuffer result = new StringBuffer(len);
            boolean gotBlank = false;
            boolean gotNonBlank = false;
            int pos = 0;
            while (pos < len)
            {
                char c = text.charAt(pos);
                if (MakeUtil.isBlank(c))
                {
                    gotBlank = gotNonBlank;
                }
                else
                {
                    if (gotBlank)
                    {
                        result.append(' ');
                        gotBlank = false;
                    }
                    result.append(c);
                    gotNonBlank = true;
                }
            }
            return result.toString();
        }
    }

    public static class Subst
        extends MakeFunction
    {
        public Subst()
        {
            super("subst", 3, 3, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0], args[1], args[2]);
        }

        public static String expand(String pattern, String replace, String text)
        {
            int len = text.length();
            int patLen = pattern.length();
            StringBuffer result = new StringBuffer(len);
            int start = 0;
            while (start < len)
            {
                // look for next instance of pattern
                int pos = text.indexOf(pattern, start);
                if (pos < 0)
                {
                    // pattern not found; copy rest of text to result and exit
                    result.append(text.substring(start));
                    break;
                }
                else if (pos > start)
                {
                    // pattern found; copy preceding text to result
                    result.append(text.substring(start, pos));
                }

                // copy replacement to result
                result.append(replace);

                // advance search position past pattern
                start = pos + patLen;
            }
            return result.toString();
        }
    }

    public static class Suffix
        extends MakeFunction
    {
        public Suffix()
        {
            super("suffix", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    int periodPos = s.lastIndexOf('.');
                    return (periodPos >= 0) ? s.substring(periodPos) : null;
                }
            });
        }
    }

    public static class Value
        extends MakeFunction
    {
        public Value()
        {
            super("value", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            String varName = args[0];
            MakeVariable var = parser.getDatabase().getVariable(varName);
            return (var != null) ? var.getValue() : "";
        }
    }

    public static class Warning
        extends MakeFunction
    {
        public Warning()
        {
            super("warning", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            parser.warning(args[0]);
            return "";
        }
    }

    public static class Wildcard
        extends MakeFunction
    {
        public Wildcard()
        {
            super("wildcard", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
            throws MakeParseException
        {
            return filter(text, new WordCallback()
            {
                public String expand(String s)
                {
                    return join(FileGlobber.glob(s));
                }
            });
        }
    }

    public static class Word
        extends MakeFunction
    {
        public Word()
        {
            super("word", 2, 2, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            try
            {
                int index = Integer.parseInt(args[0]) - 1;
                return Wordlist.expand(index, index, args[1]);
            }
            catch (NumberFormatException e)
            {
                throw new MakeParseException(
                    "Non-numeric argument 1 in 'word' function");
            }
        }
    }

    public static class Wordlist
        extends MakeFunction
    {
        public Wordlist()
        {
            super("wordlist", 3, 3, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            int curArg = 1;
            try
            {
                int start = Integer.parseInt(args[0]) - 1;
                curArg = 2;
                int end = Integer.parseInt(args[1]) - 1;
                return expand(start, end, args[2]);
            }
            catch (NumberFormatException e)
            {
                throw new MakeParseException("Non-numeric argument " + curArg
                    + " in 'wordlist' function");
            }
        }

        public static String expand(int start, int end, String text)
        {
            String[] words = split(text);
            end = Math.min(end, words.length - 1);
            return (start >= 0 && end >= start) ? join(words, start, end) : "";
        }
    }

    public static class Words
        extends MakeFunction
    {
        public Words()
        {
            super("words", 1, 1, true);
        }

        public String expand(MakeParser parser, String[] args)
            throws MakeParseException
        {
            checkArgCount(args);
            return expand(args[0]);
        }

        public static String expand(String text)
        {
            String[] words = split(text);
            return String.valueOf(words.length);
        }
    }

    static String[] split(String s)
    {
        return s.split("\\s+");
    }

    static String join(String[] words)
    {
        return join(words, 0, words.length - 1);
    }

    static String join(String[] words, int start, int end)
    {
        int len = 0;
        for (int i = start; i <= end; ++i)
        {
            if (i > start) ++len;
            len += words[i].length();
        }

        StringBuffer result = new StringBuffer(len);
        for (int i = start; i <= end; ++i)
        {
            if (i > start) result.append(' ');
            result.append(words[i]);
        }
        return result.toString();
    }

    private interface WordCallback
    {
        String expand(String s)
            throws MakeParseException;
    }

    static String filter(String text, WordCallback cb)
        throws MakeParseException
    {
        String[] words = split(text);
        StringBuffer result = new StringBuffer(text.length());
        for (int i = 0; i < words.length; ++i)
        {
            String word = cb.expand(words[i]);
            if (word != null && word.length() > 0)
            {
                if (result.length() > 0) result.append(' ');
                result.append(word);
            }
        }
        return result.toString();
    }

    static String filter(
        String text,
        String patternsText,
        final boolean filterOut)
        throws MakeParseException
    {
        final MakePattern[] patterns = parsePatterns(patternsText);
        return filter(text, new WordCallback()
        {
            public String expand(String s)
            {
                return matchesPatterns(s, patterns) != filterOut ? s : null;
            }
        });
    }

    private static MakePattern[] parsePatterns(String text)
    {
        String[] words = split(text);
        MakePattern[] patterns = new MakePattern[words.length];
        for (int i = 0; i < words.length; ++i)
        {
            patterns[i] = MakePattern.parse(words[i]);
        }
        return patterns;
    }

    static boolean matchesPatterns(String text, MakePattern[] patterns)
    {
        for (int i = 0; i < patterns.length; ++i)
        {
            if (patterns[i].matches(text)) return true;
        }
        return false;
    }
}
