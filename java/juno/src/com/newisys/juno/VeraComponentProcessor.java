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

package com.newisys.juno;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.newisys.parser.util.ParseException;

/**
 * Processes the Vera files in the given Vera components using the given file
 * processor.
 * 
 * @author Trevor Robinson
 */
public final class VeraComponentProcessor
{
    private final VeraFileProcessor fp;
    private boolean dryRun = false;
    private final Set<String> sysPaths = new LinkedHashSet<String>();

    public VeraComponentProcessor(VeraFileProcessor fp)
    {
        this.fp = fp;
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }

    public void addSysPath(String path)
    {
        sysPaths.add(path);
    }

    public void processVeraComponent(VeraComponent component)
        throws IOException, InterruptedException, ParseException,
        ParseException
    {
        showSystemState();
        System.out.println("Analyzing component: " + component.getName());

        Set<String> includePaths = component.getIncludePaths();
        Set<String> importPaths = component.getAllImportedIncludes();

        Set<String> sourceFiles = component.getSourceFiles();
        for (String filename : sourceFiles)
        {
            System.out.println("  Analyzing file: " + filename);
            if (!dryRun)
            {
                fp.processVeraFile(filename, component, sysPaths, includePaths,
                    importPaths, false);
            }
        }

        Set<String> vshellSourceFiles = component.getVshellSourceFiles();
        for (String filename : vshellSourceFiles)
        {
            if (sourceFiles.contains(filename)) continue;
            System.out.println("  Analyzing file: " + filename);
            if (!dryRun)
            {
                fp.processVeraFile(filename, component, sysPaths, includePaths,
                    importPaths, true);
            }
        }
    }

    private static DateFormat timeFormat = DateFormat.getTimeInstance();

    private void showSystemState()
    {
        System.out.print("Time: " + timeFormat.format(new Date()) + ", ");

        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long memTotal = runtime.totalMemory();
        long memUsed = memTotal - runtime.freeMemory();
        System.out.println("Memory: " + formatSize(memUsed) + " used / "
            + formatSize(memTotal) + " heap / "
            + formatSize(runtime.maxMemory()) + " max");
    }

    private String formatSize(long size)
    {
        int shifts = 0;
        while (size > 10240)
        {
            size >>>= 10;
            ++shifts;
        }
        double scaledSize;
        if (size >= 1000)
        {
            scaledSize = (double) size / 1024;
            ++shifts;
        }
        else
        {
            scaledSize = size;
        }
        String[] units = { "B", "KB", "MB", "GB", "TB", "PB", "XB" };
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(1);
        return format.format(scaledSize) + units[shifts];
    }
}
