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

/**
 * Base class for representing makefile patterns.
 * 
 * @author Trevor Robinson
 */
public abstract class MakePattern
{
    public static MakePattern parse(String pattern)
    {
        StringBuffer buffer = new StringBuffer(pattern);
        int pctPos = MakeUtil.unescapeIndexOf(buffer, '%');
        pattern = buffer.toString();
        if (pctPos >= 0)
        {
            String prefix = pattern.substring(0, pctPos);
            String suffix = pattern.substring(pctPos + 1);
            return new RealPattern(pattern, prefix, suffix);
        }
        else
        {
            return new StaticPattern(pattern);
        }
    }

    protected final String pattern;

    protected MakePattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return pattern;
    }

    public String toString()
    {
        return pattern;
    }

    public abstract boolean isStatic();

    public abstract boolean matchesAny();

    public abstract boolean matches(String s);

    public abstract String extractStub(String s);

    public abstract String replaceStub(String s);

    /**
     * Represents a makefile pattern that actually contains a wildcard.
     * 
     * @author Trevor Robinson
     */
    private static final class RealPattern
        extends MakePattern
    {
        private final String prefix;
        private final int prefixLen;
        private final String suffix;
        private final int suffixLen;

        RealPattern(String pattern, String prefix, String suffix)
        {
            super(pattern);
            this.prefix = prefix;
            prefixLen = prefix.length();
            this.suffix = suffix;
            suffixLen = suffix.length();
        }

        public boolean isStatic()
        {
            return false;
        }

        public boolean matchesAny()
        {
            return prefixLen == 0 && suffixLen == 0;
        }

        public boolean matches(String s)
        {
            return s.startsWith(prefix) && s.endsWith(suffix);
        }

        public String extractStub(String s)
        {
            return s.substring(prefixLen, s.length() - suffixLen);
        }

        public String replaceStub(String s)
        {
            return prefix + s + suffix;
        }
    }

    /**
     * Represents a makefile pattern that contains no wildcard.
     * 
     * @author Trevor Robinson
     */
    private static final class StaticPattern
        extends MakePattern
    {
        StaticPattern(String pattern)
        {
            super(pattern);
        }

        public boolean isStatic()
        {
            return true;
        }

        public boolean matchesAny()
        {
            return false;
        }

        public boolean matches(String s)
        {
            return s.equals(pattern);
        }

        public String extractStub(String s)
        {
            return "";
        }

        public String replaceStub(String s)
        {
            return pattern;
        }
    }
}
