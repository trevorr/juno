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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newisys.parser.util.FunctionMacro;
import com.newisys.parser.util.IncludeLocation;
import com.newisys.parser.util.MacroRef;
import com.newisys.parser.util.Token;

/**
 * VeraPPParser implementation of user function macros.
 * 
 * @author Trevor Robinson
 */
public final class VeraPPFunctionMacro
    extends VeraPPUserMacro
    implements FunctionMacro
{
    private final List<String> argumentNames = new LinkedList<String>();

    public VeraPPFunctionMacro(String name, IncludeLocation location)
    {
        super(name, location);
    }

    public void addArgumentName(String name)
    {
        if (argumentNames.contains(name))
        {
            throw new IllegalArgumentException("Duplicate argument name '"
                + name + "' for macro " + getName());
        }

        argumentNames.add(name);
    }

    public List<String> getArgumentNames()
    {
        return argumentNames;
    }

    public String expand(List<String> argValueList)
    {
        return expand(getArgumentValueMap(argValueList));
    }

    public String expand(Map<String, String> argValueMap)
    {
        StringBuffer buf = new StringBuffer();
        for (Token t : tokens)
        {
            String expansion = t.image;
            if (t.kind == VeraPPParserConstants.MACRO_IDENT)
            {
                String value = argValueMap.get(expansion);
                if (value != null) expansion = value;
            }
            else if (t.kind == VeraPPParserConstants.DEFINE_STRING)
            {
                expansion = stringify(expansion, argValueMap);
            }
            buf.append(expansion);
        }
        return buf.toString();
    }

    public void expand(
        List<String> argValueList,
        VeraPPCharStream stream,
        VeraPPFunctionMacroRef thisRef)
    {
        Map<String, String> argValueMap = getArgumentValueMap(argValueList);
        StringBuffer buf = null;
        VeraPPCharStream.Chunk chunk = null;
        for (Token t : tokens)
        {
            String expansion = t.image;
            MacroRef macroArgRef = null;
            if (t.kind == VeraPPParserConstants.MACRO_IDENT)
            {
                String value = argValueMap.get(expansion);
                if (value != null)
                {
                    if (thisRef != null)
                    {
                        macroArgRef = new VeraPPMacroArgRef(this, expansion,
                            thisRef);
                    }
                    expansion = value;
                }
            }
            else if (t.kind == VeraPPParserConstants.DEFINE_STRING)
            {
                expansion = stringify(expansion, argValueMap);
            }

            if (macroArgRef != null)
            {
                if (buf != null)
                {
                    chunk = stream.insertAfter(chunk, buf.toString(), thisRef);
                    buf = null;
                }
                chunk = stream.insertAfter(chunk, expansion, macroArgRef);
            }
            else
            {
                if (buf == null) buf = new StringBuffer();
                buf.append(expansion);
            }
        }
        if (buf != null)
        {
            chunk = stream.insertAfter(chunk, buf.toString(), thisRef);
        }
    }

    private Map<String, String> getArgumentValueMap(List<String> valueList)
    {
        Map<String, String> result = new LinkedHashMap<String, String>();

        Iterator<String> argIter = argumentNames.iterator();
        Iterator<String> valueIter = valueList.iterator();
        while (argIter.hasNext())
        {

            if (!valueIter.hasNext())
            {
                throw new IllegalArgumentException(
                    "Not enough actual arguments for macro " + getName());
            }

            String name = argIter.next();
            String value = valueIter.next();
            result.put(name, value);
        }

        if (valueIter.hasNext())
        {
            throw new IllegalArgumentException(
                "Too many actual arguments for macro " + getName());
        }

        return result;
    }

    private static final Pattern idPattern = Pattern.compile("\\b\\w+");
    private static final Pattern newlinePattern = Pattern
        .compile("[ \t\013\f\r]*\n\\s*");

    private static String stringify(String s, Map<String, String> argValueMap)
    {
        StringBuffer result = new StringBuffer();

        Matcher m = idPattern.matcher(s);
        int prevEnd = 0;
        while (m.find())
        {
            int start = m.start();
            int end = m.end();

            if (prevEnd < start)
            {
                result.append(s.substring(prevEnd, start));
            }

            // extract identifier and look up matching argument
            String id = s.substring(start, end);
            String value = argValueMap.get(id);
            if (value != null)
            {
                // replace newlines (and adjacent spaces) with a single space
                value = newlinePattern.matcher(value).replaceAll(" ");

                // escape double quotes
                value = value.replaceAll("\"", "\\\\\"");
            }
            else
            {
                // argument not found
                value = id;
            }
            result.append(value);

            prevEnd = end;
        }
        if (prevEnd < s.length())
        {
            // optimization
            if (prevEnd == 0) return s;

            result.append(s.substring(prevEnd));
        }

        return result.toString();
    }

    public boolean containsArgumentPasting()
    {
        int prevKind = 0;
        boolean prevArg = false;
        for (Token cur : tokens)
        {
            int curKind = cur.kind;
            boolean curArg = (curKind == VeraPPParserConstants.MACRO_IDENT)
                && argumentNames.contains(cur.image);
            if ((curArg && isPasteable(prevKind))
                || (prevArg && isPasteable(curKind))) return true;
            prevKind = curKind;
            prevArg = curArg;
        }
        return false;
    }

    public boolean containsStringification()
    {
        for (Token t : tokens)
        {
            if (t.kind == VeraPPParserConstants.DEFINE_STRING)
            {
                if (containsStringification(t.image)) return true;
            }
        }
        return false;
    }

    private boolean containsStringification(String s)
    {
        Matcher m = idPattern.matcher(s);
        while (m.find())
        {
            String id = m.group();
            if (argumentNames.contains(id)) return true;
        }
        return false;
    }

    public String toString()
    {
        return getName() + "()";
    }
}
