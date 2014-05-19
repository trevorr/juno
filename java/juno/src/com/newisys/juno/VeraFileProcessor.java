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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.newisys.langschema.vera.VeraSchema;
import com.newisys.langsource.vera.CompilationUnitDecl;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.vera.VeraParser;
import com.newisys.parser.vera.VeraParserBoundedTokenManager;
import com.newisys.parser.vera.VeraParserTokenManager;
import com.newisys.parser.verapp.VeraPPParser;
import com.newisys.parser.verapp.VeraPPStaticMacro;
import com.newisys.schemabuilder.vera.VeraSchemaBuilder;

/**
 * Processes the given Vera files into a schema.
 * 
 * @author Trevor Robinson
 */
public final class VeraFileProcessor
{
    private final VeraSchemaBuilder veraSchemaBuilder = new VeraSchemaBuilder();

    public VeraSchema getSchema()
    {
        return veraSchemaBuilder.getSchema();
    }

    public void processVeraFile(
        String filename,
        VeraComponent component,
        Set<String> sysPaths,
        Set<String> userPaths,
        Set<String> importPaths,
        boolean wantShell)
        throws IOException, InterruptedException, ParseException,
        ParseException
    {
        final VeraParserTokenManager veraTokenMgr = new VeraParserBoundedTokenManager();

        final JunoPPHandler ppCallbacks = new JunoPPHandler(
            veraTokenMgr);
        ppCallbacks.addDefine(new VeraPPStaticMacro("__JUNO__", "1"));

        // support for Newisys multi-testbench testcase templates
        if (component.isTestcaseTemplate())
        {
            String tbHeader = VeraPPParser.toStringLiteral(component
                .getAbstractTestbenchHeader());
            ppCallbacks.addDefine(new VeraPPStaticMacro(
                "ABSTRACT_TESTBENCH_HEADER", tbHeader));
            ppCallbacks.addDefine(new VeraPPStaticMacro("TESTBENCH_HEADER",
                tbHeader));

            String tbClass = component.getAbstractTestbenchClass();
            ppCallbacks.addDefine(new VeraPPStaticMacro(
                "ABSTRACT_TESTBENCH_CLASS", tbClass));
            ppCallbacks.addDefine(new VeraPPStaticMacro("TESTBENCH_CLASS",
                tbClass));
        }

        for (String path : sysPaths)
        {
            ppCallbacks.addSysPath(path);
        }

        File file = new File(filename);
        file = file.getCanonicalFile();
        ppCallbacks.addUserPath(file.getParent());

        for (String path : userPaths)
        {
            ppCallbacks.addUserPath(path);
        }

        for (String path : importPaths)
        {
            ppCallbacks.addImportPath(path);
        }

        final String path = file.getPath();
        final VeraPPParser ppParser = new VeraPPParser(path, ppCallbacks);

        final ParserThreadState threadState = new ParserThreadState();

        final Thread ppThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    ppParser.file();
                }
                catch (ParseException e)
                {
                    threadState.ppThrew(e);
                }
                catch (RuntimeException e)
                {
                    // VeraParserTokenManager wraps InterruptedException
                    // in a RuntimeException
                    if (!(e.getCause() instanceof InterruptedException))
                    {
                        threadState.ppThrew(e);
                    }
                }
                catch (Error e)
                {
                    threadState.ppThrew(e);
                }
                finally
                {
                    synchronized (threadState)
                    {
                        threadState.ppDone();
                    }
                }
            }
        }, "Vera Preprocessor Thread");

        final VeraParser veraParser = new VeraParser(veraTokenMgr);
        final CompilationUnitDecl compUnit = new CompilationUnitDecl(path);

        final Thread veraThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    veraParser.compilation_unit(compUnit);
                }
                catch (ParseException e)
                {
                    threadState.veraThrew(e);
                }
                catch (RuntimeException e)
                {
                    // VeraParserTokenManager wraps InterruptedException
                    // in a RuntimeException
                    if (!(e.getCause() instanceof InterruptedException))
                    {
                        threadState.veraThrew(e);
                    }
                }
                catch (Error e)
                {
                    threadState.ppThrew(e);
                }
                finally
                {
                    synchronized (threadState)
                    {
                        threadState.veraDone();
                    }
                }
            }
        }, "Vera Parser Thread");

        ppThread.start();
        veraThread.start();

        try
        {
            while (true)
            {
                synchronized (threadState)
                {
                    if (threadState.exceptionThrown())
                    {
                        ppThread.interrupt();
                        veraThread.interrupt();
                        break;
                    }
                    if (threadState.done())
                    {
                        break;
                    }
                    threadState.wait();
                }
            }
            ppThread.join();
            veraThread.join();
        }
        catch (InterruptedException e)
        {
            ppThread.interrupt();
            veraThread.interrupt();
            throw e;
        }
        threadState.throwException();

        veraSchemaBuilder.setPreprocInfo(ppCallbacks);
        veraSchemaBuilder.setWantShell(wantShell);
        veraSchemaBuilder.visit(compUnit);
    }
}

final class ParserThreadState
{
    private boolean ppDone;
    private Throwable ppException;
    private boolean veraDone;
    private Throwable veraException;

    public synchronized void ppDone()
    {
        ppDone = true;
        notify();
    }

    public synchronized void ppThrew(Throwable e)
    {
        ppException = e;
        ppDone = true;
        notify();
    }

    public synchronized void veraDone()
    {
        veraDone = true;
        notify();
    }

    public synchronized void veraThrew(Throwable e)
    {
        veraException = e;
        veraDone = true;
        notify();
    }

    public boolean exceptionThrown()
    {
        return ppException != null || veraException != null;
    }

    public boolean done()
    {
        return ppDone && veraDone;
    }

    public void throwException()
        throws ParseException, ParseException
    {
        if (ppException != null)
        {
            if (ppException instanceof ParseException)
            {
                throw (ParseException) ppException;
            }
            else if (ppException instanceof RuntimeException)
            {
                throw (RuntimeException) ppException;
            }
            else
            {
                throw (Error) ppException;
            }
        }
        if (veraException != null)
        {
            if (veraException instanceof ParseException)
            {
                throw (ParseException) veraException;
            }
            else if (veraException instanceof RuntimeException)
            {
                throw (RuntimeException) veraException;
            }
            else
            {
                throw (Error) veraException;
            }
        }
    }
}
