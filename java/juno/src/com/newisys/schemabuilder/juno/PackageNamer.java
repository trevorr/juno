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

package com.newisys.schemabuilder.juno;

import java.io.File;

/**
 * Generates valid Java package names from relative Vera component paths.
 * 
 * @author Trevor Robinson
 */
public final class PackageNamer
{
    private String basePath;
    private String basePackage = "juno";
    private String externalPackage = "juno.external";

    public PackageNamer(String basePath)
    {
        this.basePath = basePath;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getBasePackage()
    {
        return basePackage;
    }

    public void setBasePackage(String basePackage)
    {
        this.basePackage = basePackage;
    }

    public String getExternalPackage()
    {
        return externalPackage;
    }

    public void setExternalPackage(String externalPackage)
    {
        this.externalPackage = externalPackage;
    }

    public String getPackageName(String path)
    {
        if (path.startsWith(basePath))
        {
            // get relative path from base path
            int relStart = basePath.length();
            if (relStart < path.length())
            {
                char c = path.charAt(relStart);
                if (c == File.separatorChar || c == '/') ++relStart;
            }
            String relPath = path.substring(relStart);

            // get directory part of relative path
            File relFile = new File(relPath);
            String dir = relFile.getParent();

            // if file has no directory part, return the base package
            if (dir == null) return basePackage;

            // build package name by replacing path separator with '.',
            // replacing invalid characters with '_', and lowercasing
            int len = dir.length();
            StringBuffer buf = new StringBuffer(basePackage.length() + 1 + len);
            buf.append(basePackage);
            buf.append('.');
            boolean isStart = true;
            for (int i = 0; i < len; ++i)
            {
                char c = dir.charAt(i);
                if (c == File.separatorChar)
                {
                    c = '.';
                    isStart = true;
                }
                else if (isStart)
                {
                    if (!Character.isJavaIdentifierStart(c))
                    {
                        if (!Character.isJavaIdentifierPart(c))
                        {
                            // replace invalid ID char
                            c = '_';
                        }
                        else
                        {
                            // insert underscore before invalid start char
                            buf.append('_');
                        }
                    }
                    c = Character.toLowerCase(c);
                    isStart = false;
                }
                else
                {
                    if (!Character.isJavaIdentifierPart(c))
                    {
                        // replace invalid ID char
                        c = '_';
                    }
                    else
                    {
                        c = Character.toLowerCase(c);
                    }
                }
                buf.append(c);
            }
            return buf.toString();
        }
        else
        {
            // path is outside of designated tree; place in external package
            return externalPackage;
        }
    }
}
