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

package com.newisys.juno.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorFormat;

/**
 * String conversion and operation methods referenced by translated code.
 * 
 * @author Trevor Robinson
 */
public final class StringOp
{
    private StringOp()
    {
    }

    public static int len(String s)
    {
        return s != null ? s.length() : 0;
    }

    public static int getc(String s, int index)
    {
        int len = len(s);
        return index >= 0 && index < len ? s.charAt(index) : 0;
    }

    public static String putc(String s, int index, char c)
    {
        // ignore status
        return putc(s, index, c, null);
    }

    public static String putc(String s, int index, char c, int[] status_ref)
    {
        String result = s;
        int status;
        int len = len(s);
        if (index >= 0 && index < len)
        {
            if (c != 0)
            {
                StringBuffer buffer = new StringBuffer(s);
                buffer.setCharAt(index, c);
                result = buffer.toString();
            }
            else
            {
                // putting 0 truncates strings
                result = s.substring(0, index);
            }
            status = JunoString.OK;
        }
        else
        {
            status = JunoString.STR_ERR_OUT_OF_RANGE;
        }
        if (status_ref != null)
        {
            status_ref[0] = status;
        }
        return result;
    }

    public static String putc(String s, int index, int c)
    {
        // ignore status
        return putc(s, index, c, null);
    }

    public static String putc(String s, int index, int c, int[] status_ref)
    {
        return putc(s, index, (char) c, status_ref);
    }

    public static String putc(String s, int index, String c)
    {
        // ignore status
        return putc(s, index, c, null);
    }

    public static String putc(String s, int index, String c, int[] status_ref)
    {
        return putc(s, index, c != null && c.length() > 0 ? c.charAt(0) : 0,
            status_ref);
    }

    public static String get_status_msg(int status)
    {
        String[] msgs = { "No error", "Error in regular expression",
            "Error: location past end of string" };
        return msgs[status];
    }

    public static int compare(String s1, String s2)
    {
        if (s1 != null && s2 != null)
        {
            return s1.compareTo(s2);
        }
        return (s1 != null ? 1 : 0) - (s2 != null ? 1 : 0);
    }

    public static int icompare(String s1, String s2)
    {
        if (s1 != null && s2 != null)
        {
            return s1.compareToIgnoreCase(s2);
        }
        return (s1 != null ? 1 : 0) - (s2 != null ? 1 : 0);
    }

    public static int hash(String s, int size)
    {
        return s != null ? s.hashCode() % size : -1;
    }

    public static String substr(String s, int start)
    {
        int len = len(s);
        return (start >= 0 && start < len) ? s.substring(start) : "";
    }

    public static String substr(String s, int start, int end)
    {
        int len = len(s);
        if (end >= len) end = len - 1;
        return (start >= 0 && start <= end) ? s.substring(start, end + 1) : "";
    }

    public static int search(String s, String pattern)
    {
        if (s != null && pattern != null && pattern.length() > 0)
        {
            return s.indexOf(pattern.toString());
        }
        return -1;
    }

    public static int match(String s, String pattern)
    {
        // ignore returned matcher and status
        return match(s, pattern, null, null);
    }

    public static int match(
        String s,
        String pattern,
        Matcher[] matcher_ref,
        int[] status_ref)
    {
        Matcher matcher = null;
        boolean matched = false;
        int status = JunoString.OK;
        if (s != null && pattern != null)
        {
            try
            {
                Pattern p = Pattern.compile(pattern);
                matcher = p.matcher(s);
                matched = matcher.find();
            }
            catch (PatternSyntaxException e)
            {
                status = JunoString.STR_ERR_REGEXP_SYNTAX;
            }
        }
        if (matcher_ref != null)
        {
            matcher_ref[0] = matcher;
        }
        if (status_ref != null)
        {
            status_ref[0] = status;
        }
        return matched ? 1 : 0;
    }

    public static String prematch(String s, Matcher matcher)
    {
        if (s != null && matcher != null)
        {
            try
            {
                return s.substring(0, matcher.start());
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return null;
    }

    public static String postmatch(String s, Matcher matcher)
    {
        if (s != null && matcher != null)
        {
            try
            {
                return s.substring(matcher.end());
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return null;
    }

    public static String thismatch(String s, Matcher matcher)
    {
        if (s != null && matcher != null)
        {
            try
            {
                // returns substring currently at matched location, which is
                // NOT necessarily the original string that was matched
                return s.substring(matcher.start(), matcher.end());
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return null;
    }

    public static String backref(String s, Matcher matcher, int index)
    {
        if (index < 0)
        {
            throw new IllegalArgumentException("backref() index must be >= 0");
        }
        if (s != null && matcher != null && index < matcher.groupCount())
        {
            try
            {
                // returns substring currently at matched location, which is
                // NOT necessarily the original string that was matched
                ++index;
                return s.substring(matcher.start(index), matcher.end(index));
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return null;
    }

    public static int atoi(String s)
    {
        if (s != null)
        {
            String num = scanNumber(s, decPattern);
            num = num.replaceAll("_", "");
            return Integer.parseInt(num);
        }
        return 0;
    }

    public static String itoa(int i)
    {
        return String.valueOf(i);
    }

    public static BitVector atohex(String s)
    {
        if (s != null)
        {
            String num = scanNumber(s, hexPattern);
            BitVectorFormat fmt = new BitVectorFormat();
            fmt.setRadix(16);
            return fmt.parse(num);
        }
        return new BitVector(31, 0);
    }

    public static BitVector atooct(String s)
    {
        if (s != null)
        {
            String num = scanNumber(s, octPattern);
            BitVectorFormat fmt = new BitVectorFormat();
            fmt.setRadix(8);
            return fmt.parse(num);
        }
        return new BitVector(31, 0);
    }

    public static BitVector atobin(String s)
    {
        if (s != null)
        {
            String num = scanNumber(s, binPattern);
            BitVectorFormat fmt = new BitVectorFormat();
            fmt.setRadix(2);
            return fmt.parse(num);
        }
        return new BitVector(31, 0);
    }

    private static final Pattern hexPattern = Pattern
        .compile("([0-9_]*'h)?[0-9A-Fa-f_]+");
    private static final Pattern decPattern = Pattern
        .compile("([0-9_]*'d)?[0-9_]+");
    private static final Pattern octPattern = Pattern
        .compile("([0-9_]*'o)?[0-7_]+");
    private static final Pattern binPattern = Pattern
        .compile("([0-9_]*'b)?[01_]+");

    private static String scanNumber(String s, Pattern p)
    {
        Matcher m = p.matcher(s);
        return m.lookingAt() ? m.group() : "0";
    }

    public static String bittostr(BitVector bv)
    {
        try
        {
            // Vera ignores \000 bytes so we have to cut them out or else
            // they appear as the end of a string to the
            // StringBuffer
            int numChars = 0;
            byte[] bytes = bv.getBytes();
            for (int i = 0; i < bytes.length; ++i)
            {
                if (bytes[i] != 0)
                {
                    ++numChars;
                }
            }

            byte[] revBytes = new byte[numChars];
            for (int src = 0, dst = revBytes.length - 1; src < bytes.length; ++src)
            {
                if (bytes[src] == 0) continue;
                revBytes[dst--] = bytes[src];
            }
            return new String(revBytes);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    public static String concat(String... strs)
    {
        int len = 0;
        for (final String str : strs)
        {
            if (str != null) len += str.length();
        }
        if (len > 0)
        {
            final StringBuffer result = new StringBuffer(len);
            for (final String str : strs)
            {
                if (str != null) result.append(str);
            }
            return result.toString();
        }
        return null;
    }

    public static String replicate(int count, String str)
    {
        if (str != null)
        {
            final StringBuffer result = new StringBuffer(str.length() * count);
            for (int i = 0; i < count; ++i)
            {
                result.append(str);
            }
            return result.toString();
        }
        return null;
    }

    public static boolean equals(String s, Object obj)
    {
        if (obj instanceof JunoString)
        {
            return obj.equals(s);
        }
        else if (obj instanceof String)
        {
            return s != null && s.equals(obj);
        }
        return obj == null && s == null;
    }

    public static int hashCode(String s)
    {
        return s != null ? s.hashCode() : -1;
    }

    public static String toDisplayString(String s)
    {
        return s != null ? s : "(NULL)";
    }

    public static String toStringOrBlank(String s)
    {
        return s != null ? s : "";
    }
}
