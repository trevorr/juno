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

import com.newisys.dv.DV;
import com.newisys.verilog.util.BitVector;

/**
 * An implementation of Vera's built-in string type.
 * 
 * @author Trevor Robinson
 */
public final class JunoString
    implements Cloneable
{
    /**
     * A constant null string.
     */
    public static final JunoString NULL = new JunoString();

    /**
     * Vera's OK define.
     */
    public static final int OK = 0;

    /**
     * Vera's STR_ERR_OUT_OF_RANGE define.
     */
    public static final int STR_ERR_OUT_OF_RANGE = 1;

    /**
     * Vera's STR_ERR_REGEXP_SYNTAX define.
     */
    public static final int STR_ERR_REGEXP_SYNTAX = 2;

    private StringBuffer buffer;
    private boolean bufferShared;
    private int status;
    private Matcher matcher;
    private JunoEvent changeEvent;

    /**
     * Creates a new, empty JunoString.
     */
    public JunoString()
    {
    }

    /**
     * Creates a new JunoString, which is a copy of <code>str</code>.
     *
     * @param str the String to copy
     */
    public JunoString(String str)
    {
        assignWithoutNotify(str);
    }

    /**
     * Creates a new JunoString, which is a copy of <code>str</code> and has
     * the specified <code>status</code> and <code>matcher</code> state.
     *
     * @param str the String to copy
     * @param status one of {@link #OK}, {@link #STR_ERR_OUT_OF_RANGE}, or
     *      {@link #STR_ERR_REGEXP_SYNTAX}
     * @param matcher a Matcher to be used in JunoString's various match methods
     */
    public JunoString(String str, int status, Matcher matcher)
    {
        assignWithoutNotify(str);
        this.status = status;
        this.matcher = matcher;
    }

    /**
     * Creates a new JunoString, which is a copy of <code>other</code>.
     *
     * @param other the JunoString to copy
     */
    public JunoString(JunoString other)
    {
        assign(other);
    }

    /**
     * Returns a JunoEvent that is notified whenever this JunoString changes.
     *
     * @return the change event for this JunoString
     */
    public JunoEvent getChangeEvent()
    {
        synchronized (this)
        {
            if (changeEvent == null)
            {
                changeEvent = new JunoEvent();
            }
        }
        return changeEvent;
    }

    private void notifyChange()
    {
        if (changeEvent != null)
        {
            DV.simulation.notifyOf(changeEvent);
        }
    }

    private void copyOnWrite()
    {
        if (bufferShared)
        {
            StringBuffer newBuffer = new StringBuffer(buffer.length());
            newBuffer.append(buffer);
            buffer = newBuffer;
            bufferShared = false;
        }
    }

    private static boolean isSameSequence(CharSequence s1, CharSequence s2)
    {
        int len;
        if (s1 != null && s2 != null && s1 != s2
            && (len = s1.length()) == s2.length())
        {
            for (int i = 0; i < len; ++i)
            {
                if (s1.charAt(i) != s2.charAt(i)) return false;
            }
            return true;
        }
        return s1 == s2;
    }

    private void assignWithoutNotify(String str)
    {
        status = OK;
        buffer = str != null ? new StringBuffer(str) : null;
        bufferShared = false;
    }

    /**
     * Assigns <code>str</code> to this JunoString.
     *
     * @param str the String to assign
     * @return this JunoString after the assignment
     */
    public JunoString assign(String str)
    {
        boolean changed = !isSameSequence(str, buffer);
        assignWithoutNotify(str);
        if (changed) notifyChange();
        return this;
    }

    /**
     * Assigns <code>other</code> to this JunoString.
     *
     * @param other the JunoString to assign
     * @return this JunoString after the assignment
     */
    public JunoString assign(JunoString other)
    {
        status = OK;
        boolean changed = !isSameSequence(buffer, other.buffer);
        buffer = other.buffer;
        boolean shared = (buffer != null);
        bufferShared = shared;
        other.bufferShared = shared;
        if (changed) notifyChange();
        return this;
    }

    /**
     * Returns the length of this JunoString.
     *
     * @return the number of characters in this JunoString
     */
    public int len()
    {
        status = OK;
        return buffer != null ? buffer.length() : 0;
    }

    /**
     * Returns the character at the specified index in this JunoString.
     *
     * @param index the index of the character to return
     * @return the character at <code>index</code>, or 0 if <code>index</code>
     *      is outside the bounds of this JunoString.
     */
    public int getc(int index)
    {
        status = OK;
        int len = len();
        return index >= 0 && index < len ? buffer.charAt(index) : 0;
    }

    /**
     * Returns a new JunoString which is a copy of this JunoString will all
     * characters converted to lower case.
     *
     * @return a new JunoString with the contents of this JunoString converted
     *      to lower case.
     */
    public JunoString tolower()
    {
        if (buffer == null)
        {
            throw new JunoRuntimeException(
                "JunoString.tolower failed due to null string");
        }
        status = OK;
        return new JunoString(buffer.toString().toLowerCase());
    }

    /**
     * Returns a new JunoString which is a copy of this JunoString will all
     * characters converted to upper case.
     *
     * @return a new JunoString with the contents of this JunoString converted
     *      to upper case.
     */
    public JunoString toupper()
    {
        if (buffer == null)
        {
            throw new JunoRuntimeException(
                "JunoString.toupper failed due to null string");
        }
        status = OK;
        return new JunoString(buffer.toString().toUpperCase());
    }

    /**
     * Inserts the specified character into this JunoString at the specified
     * index. If index is outside the bounds of this JunoString, the status of
     * this JunoString is changed to {@link #STR_ERR_OUT_OF_RANGE}.
     *
     * @param index the index at which to insert <code>c</code>
     * @param c the character to insert at <code>index</code>
     */
    public void putc(int index, char c)
    {
        copyOnWrite();
        int len = len();
        if (index >= 0 && index < len)
        {
            boolean changed;
            if (c != 0)
            {
                changed = buffer.charAt(index) != c;
                buffer.setCharAt(index, c);
            }
            else
            {
                changed = true;
                buffer.setLength(index);
            }
            status = OK;
            if (changed) notifyChange();
        }
        else
        {
            status = STR_ERR_OUT_OF_RANGE;
        }
    }

    /**
     * Inserts the specified character into this JunoString at the specified
     * index.
     * <P>
     * This is equivalent to <code>putc(index, (char)c)</code>.
     *
     * @param index the index at which to insert <code>c</code>
     * @param c the character to insert at <code>index</code>
     */
    public void putc(int index, int c)
    {
        putc(index, (char) c);
    }

    /**
     * Inserts the first character of the specified String into this JunoString
     * at the specified index. If <code>s</code> is <code>null</code> or
     * <code>s.length()</code> is 0, 0 is inserted.
     *
     * @param index the index at which to insert the character
     * @param s a String, the first character of which will be inserted into
     *      this JunoString at <code>index</code>
     */
    public void putc(int index, String s)
    {
        putc(index, s != null && s.length() > 0 ? s.charAt(0) : 0);
    }

    /**
     * Returns the status of this JunoString.
     *
     * @return the current status of this JunoString.
     */
    public int get_status()
    {
        return status;
    }

    /**
     * Returns a String representation of the current status of this JunoString.
     *
     * @return a String representing the status of this JunoString
     */
    public String get_status_msg()
    {
        return StringOp.get_status_msg(status);
    }

    /**
     * Compares this JunoString to the specified string and returns the results
     * of the comparison with semantics identical to {@link String#compareTo}.
     *
     * @param other the string to compare
     * @return a negative value, 0, or a positive value as specified in
     *      String.compareTo
     * @see String#compareTo(java.lang.String)
     */
    public int compare(String other)
    {
        status = OK;
        if (buffer != null && other != null)
        {
            return buffer.toString().compareTo(other);
        }
        return (buffer != null ? 1 : 0) - (other != null ? 1 : 0);
    }

    /**
     * Compares this JunoString to the specified string and returns the results
     * of the comparison with semantics identical to {@link String#compareTo}.
     *
     * @param other the string to compare
     * @return a negative value, 0, or a positive value as specified in
     *      String.compareTo
     * @see String#compareTo(java.lang.String)
     */
    public int compare(JunoString other)
    {
        return compare(other.toStringOrNull());
    }

    /**
     * Compares this JunoString to the specified string case-insensitively and
     * returns the results of the comparison with semantics identical to
     * {@link String#compareToIgnoreCase}.
     *
     * @param other the JunoString to compare
     * @return a negative value, 0, or a positive value as specified in
     *      String.compareToIgnoreCase
     * @see String#compareToIgnoreCase
     */
    public int icompare(String other)
    {
        status = OK;
        if (buffer != null && other != null)
        {
            return buffer.toString().compareToIgnoreCase(other);
        }
        return (buffer != null ? 1 : 0) - (other != null ? 1 : 0);
    }

    /**
     * Compares this JunoString to the specified string case-insensitively and
     * returns the results of the comparison with semantics identical to
     * {@link String#compareToIgnoreCase}.
     *
     * @param other the JunoString to compare
     * @return a negative value, 0, or a positive value as specified in
     *      String.compareToIgnoreCase
     * @see String#compareToIgnoreCase
     */
    public int icompare(JunoString other)
    {
        return icompare(other.toStringOrNull());
    }

    /**
     * Returns a hash code for this JunoString. The returns value will be
     * a value in the interval [0, size).
     *
     * @param size the maximum value the hash code should take on, exclusive
     * @return the hash code, or -1 if this JunoString's buffer is null.
     */
    public int hash(int size)
    {
        status = OK;
        return buffer != null ? buffer.toString().hashCode() % size : -1;
    }

    /**
     * Returns a substring of this JunoString. The substring will contain all
     * characters in this JunoString from <code>start</code> through the end
     * of the JunoString.
     *
     * @param start the beginning index, inclusive
     * @return a new JunoString containing the specified substring, or the
     *      empty string if <code>start</code> is out of the bounds of this
     *      JunoString
     */
    public JunoString substr(int start)
    {
        status = OK;
        int len = len();
        String s = (start >= 0 && start < len) ? buffer.substring(start) : "";
        return new JunoString(s);
    }

    /**
     * Returns a substring of this JunoString. The substring will contain all
     * characters in this JunoString from <code>start</code> (inclusive) through
     * <code>end</code> (exclusive).
     *
     * @param start the beginning index, inclusive
     * @param end the ending index, exclusive
     * @return a new JunoString containing the specified substring, or the
     *      empty string if <code>start</code> or <code>end</code> is out of the
     *      bounds of this JunoString
     */
    public JunoString substr(int start, int end)
    {
        status = OK;
        int len = len();
        if (end >= len) end = len - 1;
        String s = (start >= 0 && start <= end) ? buffer.substring(start,
            end + 1) : "";
        return new JunoString(s);
    }

    /**
     * Searches for the specified pattern in this JunoString. It will find the
     * first occurrence of <code>pattern</code> in this JunoString and return
     * the starting index of that occurrence or -1 if the pattern cannot be
     * found.
     *
     * @param pattern the JunoString containing the pattern for which to search
     * @return the index of the first character of <code>pattern</code> in this
     *      JunoString, or -1 if the pattern cannot be found.
     */
    public int search(JunoString pattern)
    {
        status = OK;
        if (!isEmpty() && !pattern.isEmpty())
        {
            return buffer.indexOf(pattern.toString());
        }
        return -1;
    }

    private boolean isEmpty()
    {
        return buffer == null || buffer.length() == 0;
    }

    /**
     * Finds the specified regular expression in this JunoString.
     * <P>
     * If the pattern is found in this JunoString, 1 is returned.
     * Otherwise, 0 is returned.
     * <P>
     * If <code>pattern</code> is not a legal regular expression, 0 is returned
     * and the status of this JunoString is set to {@link #STR_ERR_REGEXP_SYNTAX}.
     * <P>
     * It should be noted that this method creates matcher state in this JunoString
     * that is used by {@link #prematch}, {@link #postmatch}, {@link #thismatch},
     * and {@link #backref}. This matcher state is reset each time <code>match</code>
     * is called.
     *
     * @param pattern the regular expression to match
     * @return 1 if a match was found, 0 otherwise
     */
    public int match(String pattern)
    {
        status = OK;
        matcher = null;
        boolean matched = false;
        if (buffer != null && pattern != null)
        {
            try
            {
                Pattern p = Pattern.compile(pattern);
                matcher = p.matcher(buffer);
                matched = matcher.find();
            }
            catch (PatternSyntaxException e)
            {
                status = STR_ERR_REGEXP_SYNTAX;
            }
        }
        return matched ? 1 : 0;
    }

    /**
     * Finds the specified regular expression in this JunoString.
     * <P>
     * If the pattern is found in this JunoString, 1 is returned.
     * Otherwise, 0 is returned.
     * <P>
     * If <code>pattern</code> is not a legal regular expression, 0 is returned
     * and the status of this JunoString is set to {@link #STR_ERR_REGEXP_SYNTAX}.
     * <P>
     * It should be noted that this method creates matcher state in this JunoString
     * that is used by {@link #prematch}, {@link #postmatch}, {@link #thismatch},
     * and {@link #backref}. This matcher state is reset each time <code>match</code>
     * is called.
     *
     * @param pattern the regular expression to match
     * @return 1 if a match was found, 0 otherwise
     */
    public int match(JunoString pattern)
    {
        return match(pattern.toStringOrNull());
    }

    /**
     * Returns the substring of this JunoString including all characters up to
     * the beginning of the previous match. This method expects a valid
     * matcher state as generated by {@link #match(String)} or
     * {@link #JunoString(String, int, Matcher)}.
     * <P>
     * It returns a substring equivalent to <code>substr(0, matcher.start())</code>.
     *
     * @return a new JunoString containing the substring of all characters before
     *      the start of the previous match, or the NULL string if there is not
     *      a valid match state
     */
    public JunoString prematch()
    {
        status = OK;
        if (buffer != null && matcher != null)
        {
            try
            {
                return new JunoString(buffer.substring(0, matcher.start()));
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return NULL;
    }

    /**
     * Returns the substring of this JunoString including all characters after
     * the end of the previous match. This method expects a valid
     * matcher state as generated by {@link #match(String)} or
     * {@link #JunoString(String, int, Matcher)}.
     * <P>
     * It returns a substring equivalent to <code>substr(matcher.end())</code>.
     *
     * @return a new JunoString containing the substring of all characters after
     *      the end of the previous match, or the NULL string if there is not
     *      a valid match state
     */
    public JunoString postmatch()
    {
        status = OK;
        if (buffer != null && matcher != null)
        {
            try
            {
                return new JunoString(buffer.substring(matcher.end()));
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return NULL;
    }

    /**
     * Returns the substring corresponding to the previous match. This method
     * expects a valid matcher state as generated by {@link #match(String)} or
     * {@link #JunoString(String, int, Matcher)}.
     * <P>
     * It returns a substring equivalent to
     * <code>substr(matcher.start(), matcher.end())</code>.
     *
     * @return a new JunoString containing the substring of all characters
     *      contained in the previous match, or the NULL string if there is not
     *      a valid match state
     */
    public JunoString thismatch()
    {
        status = OK;
        if (buffer != null && matcher != null)
        {
            try
            {
                // returns substring currently at matched location, which is
                // NOT necessarily the original string that was matched
                return new JunoString(buffer.substring(matcher.start(), matcher
                    .end()));
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return NULL;
    }

    /**
     * Returns the substring corresponding to the specified capture group of the
     * previous match. This method expects a valid matcher state as generated by
     * {@link #match(String)} or {@link #JunoString(String, int, Matcher)}.
     *
     * @param index the index of the capture group
     * @return a substring containing the characters matched by the specified
     *      capture group in the previous match, or the NULL string if the
     *      match state is invalid
     */
    public JunoString backref(int index)
    {
        status = OK;
        if (index < 0)
        {
            throw new IllegalArgumentException(
                "JunoString.backref index must be >= 0");
        }

        ++index;
        if (buffer != null && matcher != null && index <= matcher.groupCount()
            && matcher.start(index) != -1 && matcher.end(index) != -1)
        {
            try
            {
                // returns substring currently at matched location, which is
                // NOT necessarily the original string that was matched
                return new JunoString(buffer.substring(matcher.start(index),
                    matcher.end(index)));
            }
            catch (IllegalStateException e)
            {
                // ignored
            }
        }
        return NULL;
    }

    /**
     * Parses this JunoString as a decimal value and returns the resulting int.
     * Underscores are allowed in this JunoString.
     *
     * @return an int corresponding to the decimal value of this JunoString
     */
    public int atoi()
    {
        status = OK;
        return StringOp.atoi(buffer.toString());
    }

    /**
     * Assigns the specified int to this JunoString. The string assigned is
     * equivalent to <code>String.valueOf(i)</code>.
     *
     * @param i the int to assign to this JunoString
     */
    public void itoa(int i)
    {
        // Vera always notifies on itoa, even if the result is the same
        assignWithoutNotify(String.valueOf(i));
        notifyChange();
    }

    /**
     * Parses this JunoString as a hexadecimal value and returns the equivalent
     * BitVector.
     *
     * @return a BitVector having the value of this JunoString when treated as
     *      a hexadecimal value
     */
    public BitVector atohex()
    {
        status = OK;
        return StringOp.atohex(buffer.toString());
    }

    /**
     * Parses this JunoString as an octal value and returns the equivalent
     * BitVector.
     *
     * @return a BitVector having the value of this JunoString when treated as
     *      an octal value
     */
    public BitVector atooct()
    {
        status = OK;
        return StringOp.atooct(buffer.toString());
    }

    /**
     * Parses this JunoString as a binary value and returns the equivalent
     * BitVector.
     *
     * @return a BitVector having the value of this JunoString when treated as
     *      a binary value
     */
    public BitVector atobin()
    {
        status = OK;
        return StringOp.atobin(buffer.toString());
    }

    /**
     * Assigns the specified BitVector to this JunoString. Each 8 bits of
     * <code>bv</code> are treated as a character to assign to this JunoString.
     * Furthermore, the most significant byte of <code>bv</code> is placed in
     * the first character of this JunoString.
     * <P>
     * For example if <code>bv</code> is the BitVector 32'h41424344, the resulting
     * JunoString will be "ABCD", where A = 0x41, B = 0x42, etc. in ASCII.
     *
     * @param bv the BitVector to assign to this JunoString
     */
    public void bittostr(BitVector bv)
    {
        status = OK;
        String str = StringOp.bittostr(bv);
        boolean changed = !isSameSequence(str, buffer);
        buffer = (str != null) ? new StringBuffer(str) : null;
        bufferShared = false;
        if (changed) notifyChange();
    }

    protected Object clone()
    {
        return new JunoString(this);
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof JunoString)
        {
            return compare((JunoString) obj) == 0;
        }
        else if (obj instanceof String)
        {
            return buffer != null && buffer.toString().equals(obj);
        }
        return obj == null && buffer == null;
    }

    public int hashCode()
    {
        return buffer != null ? buffer.toString().hashCode() : -1;
    }

    /**
     * Returns a String containing the contents of this JunoString, or "(NULL)"
     * if this JunoString contains the null string.
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return buffer != null ? buffer.toString() : "(NULL)";
    }

    /**
     * Returns a String containing the contents of this JunoString, or an empty
     * string if this JunoString contains the null string.
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toStringOrBlank()
    {
        return buffer != null ? buffer.toString() : "";
    }

    /**
     * Returns a String containing the contents of this JunoString, or null if
     * this JunoString contains the null string.
     *
     * @return String or null
     * @see java.lang.Object#toString()
     */
    public String toStringOrNull()
    {
        return buffer != null ? buffer.toString() : null;
    }
}
