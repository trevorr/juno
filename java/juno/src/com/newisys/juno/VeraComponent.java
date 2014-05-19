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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an actual Vera component (not a grouping component).
 * 
 * @author Trevor Robinson
 */
public final class VeraComponent
    extends VeraAbsComponent
{
    private static final long serialVersionUID = 3546080276355627058L;

    private final String sourcePath;
    private final Set<String> sourceFiles = new LinkedHashSet<String>();
    private final Set<String> vshellSourceFiles = new LinkedHashSet<String>();
    private final Set<String> importedIncludes = new LinkedHashSet<String>();

    private String testbenchClass;

    private boolean testcaseTemplate;
    private String abstractTestbenchHeader;
    private String abstractTestbenchClass;
    private String defaultTestbenchComponent;

    /**
     * Constructs a new Vera component.
     *
     * @param name the name of this component
     * @param sourcePath the source path for this component
     */
    public VeraComponent(String name, String sourcePath)
    {
        super(name);
        this.sourcePath = sourcePath;
    }

    /**
     * Returns the source path for this component.
     *
     * @return String
     */
    public String getSourcePath()
    {
        return sourcePath;
    }

    /**
     * Returns the list of source files for this component.
     *
     * @return Set of String
     */
    public Set<String> getSourceFiles()
    {
        return sourceFiles;
    }

    /**
     * Adds the given filename to the list of source files.
     *
     * @param name the filename of the source file
     */
    public void addSourceFile(String name)
    {
        sourceFiles.add(name);
    }

    /**
     * Returns the list of Verilog shell source files for this component.
     *
     * @return Set of String
     */
    public Set<String> getVshellSourceFiles()
    {
        return vshellSourceFiles;
    }

    /**
     * Adds the given filename to the list of Verilog shell source files.
     *
     * @param name the filename of the source file
     */
    public void addVshellSourceFile(String name)
    {
        vshellSourceFiles.add(name);
    }

    /**
     * Returns the list of include files imported by this component.
     *
     * @return Set of String
     */
    public Set<String> getImportedIncludes()
    {
        return importedIncludes;
    }

    /**
     * Adds the given path to the list of imported include files.
     *
     * @param path the path to the include file to import
     */
    public void addImportedInclude(String path)
    {
        importedIncludes.add(path);
    }

    /**
     * Returns the name of the abstract testbench implementation class in this
     * component, or null if this component does not contain an abstract
     * testbench implementation.
     *
     * @return the name of the testbench class
     */
    public String getTestbenchClass()
    {
        return testbenchClass;
    }

    /**
     * Sets the name of the abstract testbench implementation class in this
     * component.
     *
     * @param cls the name of the testbench class
     */
    public void setTestbenchClass(String cls)
    {
        this.testbenchClass = cls;
    }

    /**
     * Returns whether this component represents a testcase template (i.e. has
     * TESTCASE_TEMPLATE_FILE defined in its makefile).
     *
     * @return true iff this component is a testcase template
     */
    public boolean isTestcaseTemplate()
    {
        return testcaseTemplate;
    }

    /**
     * Sets whether this component represents a testcase template.
     *
     * @param testcaseTemplate true iff this component is a testcase template
     */
    public void setTestcaseTemplate(boolean testcaseTemplate)
    {
        this.testcaseTemplate = testcaseTemplate;
    }

    /**
     * Returns the header file containing the abstract testbench class for this
     * testcase template.
     *
     * @return the abstract testbench header file
     */
    public String getAbstractTestbenchHeader()
    {
        return abstractTestbenchHeader;
    }

    /**
     * Sets the header file containing the abstract testbench class for this
     * testcase template.
     *
     * @param hdr the abstract testbench header file
     */
    public void setAbstractTestbenchHeader(String hdr)
    {
        this.abstractTestbenchHeader = hdr;
    }

    /**
     * Returns the name of the abstract testbench class for this testcase
     * template.
     *
     * @return the name of the abstract testbench class
     */
    public String getAbstractTestbenchClass()
    {
        return abstractTestbenchClass;
    }

    /**
     * Sets the name of the abstract testbench class for this testcase template.
     *
     * @param cls the name of the abstract testbench class
     */
    public void setAbstractTestbenchClass(String cls)
    {
        this.abstractTestbenchClass = cls;
    }

    /**
     * Returns the name of the default testbench component.
     *
     * @return the name of the default testbench component
     */
    public String getDefaultTestbenchComponent()
    {
        return defaultTestbenchComponent;
    }

    /**
     * Sets the name of the default testbench component.
     *
     * @param comp the name of the default testbench component
     */
    public void setDefaultTestbenchComponent(String comp)
    {
        this.defaultTestbenchComponent = comp;
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.juno.VeraAbsComponent#addIncludePaths(java.util.Set)
     */
    @Override
    protected void addIncludePaths(Set<String> paths)
    {
        paths.add(sourcePath);
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.juno.VeraAbsComponent#addImportPaths(java.util.Set)
     */
    @Override
    protected void addImportPaths(Set<String> paths)
    {
        paths.addAll(importedIncludes);
    }
}
