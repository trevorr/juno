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

package com.newisys.printf;

/**
 * Provides a static method for unescaping character sequences.
 * 
 * @author Jon Nall
 */
final class StringUnescaper
{
    final static class StringUnscapeException
        extends RuntimeException
    {
        private static final long serialVersionUID = 3256438105882899509L;
        private String partialString;

        public StringUnscapeException(String s, String partial)
        {
            super(s);
            partialString = partial;
        }

        public String getPartialString()
        {
            return partialString;
        }
    }

    public static CharSequence unescape(CharSequence s)
    {
        // look through regular text to find escape sequences
        StringBuilder buf = null;
        final int length = s.length();
        for (int i = 0; i < length; ++i)
        {
            if (s.charAt(i) == '\\')
            {
                // only create a new StringBuilder if there are actual escapes
                if (buf == null)
                {
                    buf = new StringBuilder(s.length());
                    buf.append(s.subSequence(0, Math.max(0, i)));
                }

                // handle escape sequence
                if (i == length - 1)
                {
                    // Vera quits processing at this point
                    throw new StringUnscapeException(
                        "Incomplete escape sequence", buf.toString());
                }
                else
                {
                    char escapeChar = s.charAt(++i);

                    switch (escapeChar)
                    {
                    case '\\':
                        buf.append('\\');
                        break;
                    case 'a':
                        buf.append((char) 0x07);
                        break;
                    case 'b':
                        buf.append('\b');
                        break;
                    case 'f':
                        buf.append('\f');
                        break;
                    case 'n':
                        buf.append('\n');
                        break;
                    case 'r':
                        buf.append('\r');
                        break;
                    case 't':
                        buf.append('\t');
                        break;
                    case 'v':
                        buf.append((char) 0x0B);
                        break;
                    case '\n':
                        break; // line continuation
                    default:
                        buf.append(escapeChar);
                    }
                }
            }
            else if (buf != null)
            {
                buf.append(s.charAt(i));
            }
        }

        if (buf != null)
        {
            return buf;
        }
        else
        {
            return s;
        }
    }
}
