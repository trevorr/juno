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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.parser.make.MakeDatabase;
import com.newisys.parser.make.MakeFileInfo;
import com.newisys.parser.make.MakeParseException;
import com.newisys.parser.make.MakeParser;
import com.newisys.parser.make.MakeStaticRule;
import com.newisys.parser.make.MakeVariable;
import com.newisys.parser.make.MakeVariableOrigin;

/**
 * Populates the given Vera component map with definitions of the given
 * components based on makefiles in the given Vera source tree. The makefiles
 * and components are expected to follow the conventions of the Newisys Vera
 * Make System; they are not arbitrary makefiles. Briefly, the expectations
 * are:
 * <ul><li>
 * $VERA_ROOT/makefile contains make rules describing component dependencies.
 * For example, if component foo depends on component bar, then this makefile
 * should include the rule "foo: bar".
 * </li><li>
 * For each component, $VERA_ROOT/src/$COMP_NAME/$COMP_ROOT.mak should exist.
 * Here, COMP_NAME is the full name of the component, which may contain slashes
 * to indicate subcomponents, and COMP_ROOT is the last segment of the name.
 * For example, if COMP_NAME is tests/test1, then COMP_ROOT is test1.
 * </li><li>
 * If $COMP_ROOT.mak exists, it may contain the following variable assignments:
 * <table>
 *   <tr><td>SRC_FILES</td><td>list of .vr source files</td></tr>
 *   <tr><td>IMPORT_HDR_FILES</td><td>external header files to import (relative to VERA_ROOT)</td></tr>
 *   <tr><td>TESTBENCH_CLASS</td><td>name of the abstract testbench implementation class</td></tr>
 *   <tr><td>VSHELL_SRC_FILE</td><td>source file that generates Verilog shell</td></tr>
 *   <tr><td>TESTCASE_TEMPLATE_FILE</td><td>testcase template file, for multi-testbench testcases</td></tr>
 *   <tr><td>ABSTRACT_TESTBENCH_HEADER</td><td>abstract testbench header file for testcase template</td></tr>
 *   <tr><td>ABSTRACT_TESTBENCH_CLASS</td><td>abstract testbench class name for testcase template</td></tr>
 *   <tr><td>DEFAULT_TESTBENCH</td><td>default testbench component for testcase template</td></tr>
 * <table>
 * </li><li>
 * If $COMP_ROOT.mak does not exist, the component is treated as if it existed
 * and contained "SRC_FILES = $COMP_ROOT.vr".
 * </li></ul>
 * 
 * @author Trevor Robinson
 */
public final class NewisysComponentMapBuilder
{
    private final VeraComponentMap compMap;
    private final File veraRoot;
    private final File veraSrcRoot;
    private final MakeDatabase makeDatabase;
    private final List<MakeVariable> compVars = new LinkedList<MakeVariable>();

    public NewisysComponentMapBuilder(
        VeraComponentMap compMap,
        File veraRoot,
        File veraSrcRoot) throws IOException, MakeParseException
    {
        this.compMap = compMap;

        this.veraRoot = veraRoot;
        this.veraSrcRoot = veraSrcRoot;

        makeDatabase = new MakeDatabase();
        File makeFile = new File(veraRoot, "makefile");
        MakeParser makeParser = new MakeParser(makeDatabase, makeFile.getPath());
        makeParser.parse();
        makeDatabase.applySpecialTargets();
    }

    public VeraComponentMap getComponentMap()
    {
        return compMap;
    }

    public void addComponentVariable(MakeVariable var)
    {
        compVars.add(var);
    }

    public VeraAbsComponent addComponent(String target)
        throws IOException, MakeParseException
    {
        MakeFileInfo targetFileInfo = makeDatabase.getOrCreateFile(target);
        return processComponentRule(targetFileInfo);
    }

    private VeraAbsComponent processComponentRule(MakeFileInfo targetFileInfo)
        throws IOException, MakeParseException
    {
        String targetName = targetFileInfo.getPath();

        // check whether component has already been processed
        VeraAbsComponent resultComp = compMap.getComponent(targetName);
        if (resultComp != null)
        {
            return resultComp;
        }

        System.out.println("Defining component: " + targetName);

        // look up rule for component target
        MakeStaticRule targetRule = makeDatabase.getRuleForFile(targetFileInfo);
        if (targetRule == null)
        {
            throw new RuntimeException("No rule to build component target: "
                + targetName);
        }

        // define new component object, possibly from component makefile
        resultComp = defineComponent(targetName, targetFileInfo.isPhony());

        // process dependency components
        Iterator iter = targetRule.getDependencies().iterator();
        while (iter.hasNext())
        {
            MakeFileInfo depFileInfo = (MakeFileInfo) iter.next();
            VeraAbsComponent depComp = processComponentRule(depFileInfo);
            resultComp.addDependency(depComp);
        }

        return resultComp;
    }

    private VeraAbsComponent defineComponent(String targetName, boolean isPhony)
        throws IOException, MakeParseException
    {
        // check whether component has already been processed
        VeraAbsComponent resultComp = compMap.getComponent(targetName);
        if (resultComp != null)
        {
            return resultComp;
        }

        // groups are represented by phony targets
        // (also treat generated testcases as groups)
        if (isPhony || targetName.startsWith("gen/testcases/"))
        {
            VeraComponentGroup group = new VeraComponentGroup(targetName);
            resultComp = group;
        }
        else
        {
            File compRoot = new File(veraSrcRoot, targetName);

            VeraComponent comp = new VeraComponent(targetName, compRoot
                .getPath());
            resultComp = comp;

            // process the component makefile, if present
            boolean gotSrcFiles = false;
            File compMakeFile = new File(compRoot, compRoot.getName() + ".mak");
            if (compMakeFile.exists())
            {
                MakeDatabase compMakeDatabase = new MakeDatabase();

                // define VERA_SRC_DIR (normally provided by top-level makefile)
                MakeVariable veraSrc = new MakeVariable("VERA_SRC_DIR",
                    MakeVariableOrigin.DEFAULT, false);
                veraSrc.setValue(veraSrcRoot.getCanonicalPath() + "/");
                compMakeDatabase.addVariable(veraSrc);

                // add user-defined variables to component makefile database
                Iterator iter = compVars.iterator();
                while (iter.hasNext())
                {
                    MakeVariable var = (MakeVariable) iter.next();
                    compMakeDatabase.addVariable(var);
                }

                // parse the component makefile
                MakeParser compMakeParser = new MakeParser(compMakeDatabase,
                    compMakeFile.getPath());
                compMakeParser.parse();

                String srcFiles = getVar(compMakeDatabase, "SRC_FILES");
                if (srcFiles != null)
                {
                    addFilesToList(srcFiles, comp.getSourceFiles(), compRoot,
                        compMakeDatabase);
                    gotSrcFiles = true;
                }

                String testcaseTemplate = getVar(compMakeDatabase,
                    "TESTCASE_TEMPLATE_FILE");
                templateLabel: if (testcaseTemplate != null)
                {
                    final String absTBHeaderVarName = "ABSTRACT_TESTBENCH_HEADER";
                    String absTBHeader = getVar(compMakeDatabase,
                        absTBHeaderVarName);
                    if (absTBHeader == null)
                    {
                        System.err.println("Warning: " + absTBHeaderVarName
                            + " not specified for testcase template component");
                        break templateLabel;
                    }

                    final String absTBClassVarName = "ABSTRACT_TESTBENCH_CLASS";
                    String absTBClass = getVar(compMakeDatabase,
                        absTBClassVarName);
                    if (absTBClass == null)
                    {
                        System.err.println("Warning: " + absTBClassVarName
                            + " not specified for testcase template component");
                        break templateLabel;
                    }

                    addFilesToList(testcaseTemplate, comp.getSourceFiles(),
                        compRoot, compMakeDatabase);
                    comp.setTestcaseTemplate(true);
                    comp.setAbstractTestbenchHeader(absTBHeader);
                    comp.setAbstractTestbenchClass(absTBClass);

                    final String defTBVarName = "DEFAULT_TESTBENCH";
                    String defTB = getVar(compMakeDatabase, defTBVarName);
                    if (defTB != null)
                    {
                        comp.setDefaultTestbenchComponent(defTB);
                    }
                    else
                    {
                        System.err.println("Warning: " + defTBVarName
                            + " not specified for testcase template component");
                    }
                }

                String tbClass = getVar(compMakeDatabase, "TESTBENCH_CLASS");
                if (tbClass != null)
                {
                    comp.setTestbenchClass(tbClass);
                }

                String vshellSrcFile = getVar(compMakeDatabase,
                    "VSHELL_SRC_FILE");
                if (vshellSrcFile != null)
                {
                    addFilesToList(vshellSrcFile, comp.getVshellSourceFiles(),
                        compRoot, compMakeDatabase);
                }

                String importHdrFiles = getVar(compMakeDatabase,
                    "IMPORT_HDR_FILES");
                if (importHdrFiles != null)
                {
                    addFilesToList(importHdrFiles, comp.getImportedIncludes(),
                        veraRoot, compMakeDatabase);
                }
            }

            // if there is no component makefile, or SRC_FILES is undefined,
            // add the default source file: <component name>.vr
            if (!gotSrcFiles)
            {
                File srcFile = new File(compRoot, compRoot.getName() + ".vr");
                comp.addSourceFile(srcFile.getCanonicalPath());
            }
        }
        compMap.addComponent(resultComp);

        return resultComp;
    }

    private static String getVar(MakeDatabase db, String name)
    {
        MakeVariable var = db.getVariable(name);
        if (var != null)
        {
            return var.getValue().trim();
        }
        return null;
    }

    private void addFilesToList(
        String str,
        Collection<String> list,
        File root,
        MakeDatabase mdb)
        throws IOException
    {
        String[] names = str.split("\\s+");
        for (int i = 0; i < names.length; ++i)
        {
            String name = names[i];
            if (name.length() > 0)
            {
                File f = new File(root, name);
                vpathSearch: if (!f.exists())
                {
                    final List<String> searchedPaths = new LinkedList<String>();
                    searchedPaths.add(root.getPath());
                    final Iterator iter = mdb.searchVPaths(name);
                    while (iter.hasNext())
                    {
                        String path = (String) iter.next();
                        f = new File(path, name);
                        if (f.exists()) break vpathSearch;
                        searchedPaths.add(path);
                    }
                    throw new FileNotFoundException(name + " not found in "
                        + searchedPaths);
                }
                list.add(f.getCanonicalPath());
            }
        }
    }
}
