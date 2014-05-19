/*
 * JavaCC (TM) parser definition for the OpenVera (TM) language
 * Copyright (C) 2003 Trevor A. Robinson
 * JavaCC is a trademark or registered trademark of Sun Microsystems, Inc. in
 * the U.S. or other countries.
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

/**
 * Simple test program for VeraPPParser.
 * 
 * @author Trevor Robinson
 */
public class VeraPPParserTest
{
    public static void main(String[] args)
    {
        try
        {
            VeraPPParser parser;
            VeraPPHandler handler = new VeraPPHandler(System.out);
            if (args.length > 0)
            {
                File inFile = new File(args[0]);
                System.err.println("Input file: " + inFile);
                parser = new VeraPPParser(inFile.getPath(), handler);

                File inFileDir = inFile.getCanonicalFile().getParentFile();
                System.err.println("Adding search path: " + inFileDir);
                handler.addUserPath(inFileDir.getPath());
            }
            else
            {
                System.err.println("Reading from stdin");
                parser = new VeraPPParser(System.in, handler);
                parser.setFilename("<stdin>");
            }
            parser.file();
            handler.flushOutput();
            System.err.println("Preprocessing successful");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
