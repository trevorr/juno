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

import java.io.File;
import java.io.FilenameFilter;

/**
 * Utility method for expanding makefile file globs into actual paths.
 * 
 * @author Trevor Robinson
 */
final class FileGlobber
{
    private FileGlobber()
    {
    }

    public static String[] glob(String path)
    {
        final File file = new File(path);

        // split path into directory and filename glob
        final String dir = file.getParent();
        final String glob = file.getName();

        // determine whether glob contains wildcards
        final int globLen = glob.length();
        boolean wild = false;
        for (int i = 0; i < globLen; ++i)
        {
            if (isGlobChar(glob.charAt(i)))
            {
                wild = true;
                break;
            }
        }

        if (!wild)
        {
            // no wildcards; simply check if path describes an existing file
            return file.exists() ? new String[] { path } : new String[0];
        }
        else
        {
            // search directory for files matching glob
            File parent = file.getParentFile();
            if (parent == null)
            {
                String cwd = System.getProperty("user.dir");
                parent = new File(cwd);
            }
            File[] files = parent.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return matchesGlob(name, glob);
                }
            });

            // convert File[] into String[]
            int count = (files != null) ? files.length : 0;
            String[] paths = new String[count];
            for (int i = 0; i < count; ++i)
            {
                String name = files[i].getName();
                paths[i] = dir != null ? dir + File.separatorChar + name : name;
            }
            return paths;
        }
    }

    // default access for efficient access by inner class
    static boolean matchesGlob(String name, String glob)
    {
        return matchesGlob(name, 0, glob, 0);
    }

    private static boolean matchesGlob(
        String name,
        int namePos,
        String glob,
        int globPos)
    {
        int nameLen = name.length();
        int globLen = glob.length();
        while (namePos < nameLen || globPos < globLen)
        {
            int staticPos = globPos;
            globPos = nextGlob(glob, globPos);
            if (globPos > staticPos)
            {
                int staticLen = globPos - staticPos;
                if (!name.regionMatches(namePos, glob, staticPos, staticLen))
                    return false;
                namePos += staticLen;
            }
            if (globPos == globLen) break;

            switch (glob.charAt(globPos++))
            {
            case '*':
                while (globPos < globLen)
                {
                    char globChar = glob.charAt(globPos);
                    if (globChar == '?')
                    {
                        if (++namePos == nameLen) return false;
                    }
                    else if (globChar != '*')
                    {
                        break;
                    }
                    ++globPos;
                }
                while (namePos < nameLen)
                {
                    if (matchesGlob(name, namePos, glob, globPos)) return true;
                    ++namePos;
                }
                return false;
            case '?':
                if (namePos == nameLen) return false;
                ++namePos;
                break;
            case '[':
                if (namePos == nameLen) return false;
                int charsPos = globPos;
                while (globPos < globLen && glob.charAt(globPos) != ']')
                    ++globPos;
                if (globPos == globLen) return false;
                String chars = glob.substring(charsPos, globPos++);
                if (chars.indexOf(name.charAt(namePos)) < 0) return false;
                ++namePos;
                break;
            }
        }
        return namePos == nameLen && globPos == globLen;
    }

    private static int nextGlob(String glob, int pos)
    {
        int len = glob.length();
        while (pos < len && !isGlobChar(glob.charAt(pos)))
            ++pos;
        return pos;
    }

    private static boolean isGlobChar(char c)
    {
        return c == '*' || c == '?' || c == '[';
    }
}
