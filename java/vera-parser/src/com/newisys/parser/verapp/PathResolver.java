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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Resolves filenames against a set of search paths, and caches the result to
 * speed up future lookups of the same filename.
 * 
 * @author Trevor Robinson
 */
public final class PathResolver
{
    private final Set<String> searchPaths = new LinkedHashSet<String>();

    public void addSearchPath(String path)
    {
        searchPaths.add(path);
        invalidateResolutions();
    }

    public Set<String> getSearchPaths()
    {
        return Collections.unmodifiableSet(searchPaths);
    }

    private final Map<String, String> resolvedFileMap = new HashMap<String, String>();

    private void addResolution(String filename, String path)
    {
        resolvedFileMap.put(filename, path);
    }

    private String getResolution(String filename)
    {
        return resolvedFileMap.get(filename);
    }

    private void invalidateResolutions()
    {
        resolvedFileMap.clear();
    }

    public String resolve(String filename)
        throws IOException
    {
        String result = getResolution(filename);
        if (result == null)
        {
            final File f = new File(filename);
            if (f.exists())
            {
                result = f.getCanonicalPath();
            }
            else if (!f.isAbsolute())
            {
                final Iterator<String> i = searchPaths.iterator();
                while (i.hasNext())
                {
                    final String path = i.next();
                    File searchFile = new File(path);
                    if (f.getName().equals(searchFile.getName()))
                    {
                        searchFile = new File(searchFile.getParent(), filename);
                        if (searchFile.exists())
                        {
                            result = searchFile.getCanonicalPath();
                            break;
                        }
                    }
                    searchFile = new File(searchFile, filename);
                    if (searchFile.exists())
                    {
                        result = searchFile.getCanonicalPath();
                        break;
                    }
                }
            }
            addResolution(filename, result);
        }
        return result;
    }
}
