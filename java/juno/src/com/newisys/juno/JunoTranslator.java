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

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.dv.ifgen.schema.IfgenPackage;
import com.newisys.dv.ifgen.schema.IfgenSchema;
import com.newisys.dv.ifgen.schema.IfgenSchemaMember;
import com.newisys.dv.ifgen.schemaprinter.IfgenSchemaPrinter;
import com.newisys.juno.runtime.TestbenchFactory;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.jove.JoveSchema;
import com.newisys.langschema.vera.VeraClass;
import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraNameKind;
import com.newisys.langschema.vera.VeraProgram;
import com.newisys.langschema.vera.VeraSchema;
import com.newisys.parser.make.MakeParseException;
import com.newisys.parser.make.MakeVariable;
import com.newisys.parser.make.MakeVariableOrigin;
import com.newisys.parser.util.ParseException;
import com.newisys.schemaanalyzer.juno.VeraSchemaAnalyzer;
import com.newisys.schemabuilder.juno.DefaultFactoryCallBuilder;
import com.newisys.schemabuilder.juno.PackageNamer;
import com.newisys.schemabuilder.juno.JunoSchemaBuilder;
import com.newisys.schemaprinter.jove.JoveSchemaPrinter;
import com.newisys.schemaprinter.vera.VeraSchemaPrinter;
import com.newisys.util.cmdline.AbstractArg;
import com.newisys.util.cmdline.AbstractArgDef;
import com.newisys.util.cmdline.CmdLine;
import com.newisys.util.cmdline.CmdLineDef;
import com.newisys.util.cmdline.OptionArg;
import com.newisys.util.cmdline.OptionArgDef;
import com.newisys.util.cmdline.StringArg;
import com.newisys.util.cmdline.StringArgDef;
import com.newisys.util.cmdline.ValidationException;
import com.newisys.util.logging.IndentLogger;

/**
 * Main program for Juno.
 * 
 * @author Trevor Robinson
 */
public class JunoTranslator
{
    private static class MyCmdLineDef
        extends CmdLineDef
    {
        public final OptionArgDef helpOpt;

        public final OptionArgDef veraRootOpt;
        public final StringArgDef veraRootArg;

        public final OptionArgDef veraCompOpt;
        public final StringArgDef veraCompArg;

        public final OptionArgDef compVarOpt;
        public final StringArgDef compVarNameArg;
        public final StringArgDef compVarValueArg;

        public final OptionArgDef noDepsOpt;

        public final OptionArgDef veraFileOpt;
        public final StringArgDef veraFileArg;

        public final OptionArgDef sysPathOpt;
        public final StringArgDef sysPathArg;

        public final OptionArgDef veraSchemaDryRunOpt;

        public final OptionArgDef veraSchemaDumpOpt;
        public final StringArgDef veraSchemaDumpArg;

        public final OptionArgDef veraSchemaLoadOpt;
        public final StringArgDef veraSchemaLoadArg;

        public final OptionArgDef javaRootOpt;
        public final StringArgDef javaRootArg;

        public final OptionArgDef javaPkgOpt;
        public final StringArgDef javaPkgArg;

        public final OptionArgDef compClassMapOpt;
        public final StringArgDef compClassMapArg;

        public final OptionArgDef debugOpt;

        public MyCmdLineDef()
        {
            super("juno");

            helpOpt = new OptionArgDef("help",
                "Display usage information and exit", 0, 1);
            addArgDef(helpOpt);

            veraRootOpt = new OptionArgDef("veraroot",
                "Root of Vera source tree", 0, 1);
            veraRootArg = new StringArgDef("path");
            veraRootOpt.addArgDef(veraRootArg);
            addArgDef(veraRootOpt);

            veraCompOpt = new OptionArgDef("veracomp",
                "Vera component(s) to translate", 0, 1);
            veraCompArg = new StringArgDef("name", null, 1,
                AbstractArgDef.UNBOUNDED);
            veraCompOpt.addArgDef(veraCompArg);
            addArgDef(veraCompOpt);

            compVarOpt = new OptionArgDef("compvar",
                "Define make variable for component makefiles", 0,
                AbstractArgDef.UNBOUNDED);
            compVarNameArg = new StringArgDef("name");
            compVarOpt.addArgDef(compVarNameArg);
            compVarValueArg = new StringArgDef("value");
            compVarOpt.addArgDef(compVarValueArg);
            addArgDef(compVarOpt);

            noDepsOpt = new OptionArgDef("nodeps",
                "Do not translate prerequisite components", 0, 1);
            addArgDef(noDepsOpt);

            veraFileOpt = new OptionArgDef("verafile",
                "Vera file(s) to translate", 0, 1);
            veraFileArg = new StringArgDef("file", null, 1,
                AbstractArgDef.UNBOUNDED);
            veraFileOpt.addArgDef(veraFileArg);
            addArgDef(veraFileOpt);

            sysPathOpt = new OptionArgDef("syspath", "Add system include path");
            sysPathArg = new StringArgDef("path");
            sysPathOpt.addArgDef(sysPathArg);
            addArgDef(sysPathOpt);

            veraSchemaDryRunOpt = new OptionArgDef("veraschemadryrun",
                "Process all the Vera components without reading any files", 0,
                1);
            addArgDef(veraSchemaDryRunOpt);

            veraSchemaDumpOpt = new OptionArgDef("veraschemadump",
                "Dump the Vera schema to the given file", 0, 1);
            veraSchemaDumpArg = new StringArgDef("path");
            veraSchemaDumpOpt.addArgDef(veraSchemaDumpArg);
            addArgDef(veraSchemaDumpOpt);

            veraSchemaLoadOpt = new OptionArgDef("veraschemaload",
                "Load the Vera schema from the given file", 0, 1);
            veraSchemaLoadArg = new StringArgDef("path");
            veraSchemaLoadOpt.addArgDef(veraSchemaLoadArg);
            addArgDef(veraSchemaLoadOpt);

            javaRootOpt = new OptionArgDef("javaroot",
                "Root of Java source tree to generate", 1, 1);
            javaRootArg = new StringArgDef("path");
            javaRootOpt.addArgDef(javaRootArg);
            addArgDef(javaRootOpt);

            javaPkgOpt = new OptionArgDef("javapkg",
                "Base package for Java classes", 0, 1);
            javaPkgArg = new StringArgDef("pkg");
            javaPkgOpt.addArgDef(javaPkgArg);
            addArgDef(javaPkgOpt);

            compClassMapOpt = new OptionArgDef("compclassmap",
                "Generate mapping of Vera components to Java classes", 0, 1);
            compClassMapArg = new StringArgDef("path");
            compClassMapOpt.addArgDef(compClassMapArg);
            addArgDef(compClassMapOpt);

            debugOpt = new OptionArgDef("debug", "Output debug information", 0,
                1);
            addArgDef(debugOpt);
        }
    }

    private static final MyCmdLineDef cmdLineDef = new MyCmdLineDef();

    public static void main(String[] args)
    {
        try
        {
            CmdLine cmdLine = cmdLineDef.processCmdLine(args);
            JunoTranslator v2j = new JunoTranslator(cmdLine);
            v2j.run();

            // exit with success
            System.exit(0);
        }
        catch (ValidationException e)
        {
            System.err.println(e.getMessage());
            cmdLineDef.dumpUsage(System.err);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // exit with error
        System.exit(1);
    }

    private final CmdLine cmdLine;
    private final IndentLogger log;

    public JunoTranslator(CmdLine cmdLine)
    {
        this.cmdLine = cmdLine;
        log = new IndentLogger(System.out);
    }

    public void run()
        throws ClassNotFoundException, IOException, MakeParseException,
        InterruptedException, ParseException, ValidationException
    {
        final int SCHEMA_DUMP_VERSION = 1;

        final String veraRootPath;
        final String veraSrcPath;
        final VeraComponentMap compMap;

        // replace final modifier for veraSchema when the following javac
        // bug is fixed (it is present as of 1.5.0_05)
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6328007
        VeraSchema veraSchema;

        final String loadPath = cmdLine.getOptionArgValue(
            cmdLineDef.veraSchemaLoadOpt, cmdLineDef.veraSchemaLoadArg);
        final String veraRootArg = cmdLine.getOptionArgValue(
            cmdLineDef.veraRootOpt, cmdLineDef.veraRootArg);
        if ((loadPath != null) == (veraRootArg != null))
        {
            throw new ValidationException(
                "Must specify -veraroot or -veraschemaload");
        }

        if (loadPath != null)
        {
            log.println("Loading Vera schema from " + loadPath);
            FileInputStream fis = new FileInputStream(loadPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            int version = ois.readInt();
            assert (version == SCHEMA_DUMP_VERSION);
            veraRootPath = ois.readUTF();
            veraSrcPath = ois.readUTF();
            compMap = (VeraComponentMap) ois.readObject();
            veraSchema = (VeraSchema) ois.readObject();
            ois.close();
            log.println("Vera schema loaded");
        }
        else
        {
            compMap = new VeraComponentMap();

            // determine Vera tree root and src directories;
            // if src directory does not exist in root, use root as src
            final File veraRootFile = new File(veraRootArg).getCanonicalFile();
            veraRootPath = veraRootFile.getPath();
            File veraSrcFile = new File(veraRootFile, "src");
            if (veraSrcFile.isDirectory())
            {
                veraSrcPath = veraSrcFile.getPath();
            }
            else
            {
                veraSrcFile = veraRootFile;
                veraSrcPath = veraRootPath;
            }

            // build ordered list of components to process
            final Set<VeraAbsComponent> compList = new LinkedHashSet<VeraAbsComponent>();
            final List<AbstractArg> veraCompOpts = cmdLine
                .getArgsForDef(cmdLineDef.veraCompOpt);
            if (veraCompOpts != null)
            {
                buildVeraComponentMap(compMap, veraRootFile, veraSrcFile);
                buildCompList(compMap, veraCompOpts, compList);
            }
            final List<AbstractArg> veraFileOpts = cmdLine
                .getArgsForDef(cmdLineDef.veraFileOpt);
            if (veraFileOpts != null)
            {
                final VeraComponent comp = new VeraComponent("$default$",
                    veraSrcPath);
                for (AbstractArg veraFileOpt : veraFileOpts)
                {
                    final List<StringArg> veraFileArgs = ((OptionArg) veraFileOpt)
                        .getArgsForDef(cmdLineDef.veraFileArg);
                    for (StringArg veraFileArg : veraFileArgs)
                    {
                        final String argPath = veraFileArg.getValue();
                        final File argFile = new File(argPath);
                        final File veraFile;
                        if (argFile.isAbsolute())
                            veraFile = argFile;
                        else
                            veraFile = new File(veraSrcFile, argPath);
                        comp.addSourceFile(veraFile.getPath());
                    }
                }
                compMap.addComponent(comp);
                compList.add(comp);
            }
            if (compList.isEmpty())
            {
                throw new ValidationException(
                    "No components specified; please specify -veracomp or -verafile");
            }

            // build Vera schema according to component list
            final VeraFileProcessor fp = new VeraFileProcessor();
            final boolean veraSchemaDryRun = cmdLine
                .getArgsForDef(cmdLineDef.veraSchemaDryRunOpt) != null;
            buildVeraSchema(compList, fp, veraSchemaDryRun);
            if (veraSchemaDryRun) return;
            veraSchema = fp.getSchema();

            final String dumpPath = cmdLine.getOptionArgValue(
                cmdLineDef.veraSchemaDumpOpt, cmdLineDef.veraSchemaDumpArg);
            if (dumpPath != null)
            {
                log.println("Dumping Vera schema to " + dumpPath);
                File dumpFile = new File(dumpPath);
                File dumpDir = dumpFile.getParentFile();
                if (dumpDir != null && !dumpDir.exists())
                {
                    log.println("  (Creating directory " + dumpDir.getPath()
                        + ")");
                    dumpDir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(dumpFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeInt(SCHEMA_DUMP_VERSION);
                oos.writeUTF(veraRootPath);
                oos.writeUTF(veraSrcPath);
                oos.writeObject(compMap);
                oos.writeObject(veraSchema);
                oos.close();
                log.println("Vera schema written");
            }
        }

        if (false)
        {
            VeraSchemaPrinter vsp = new VeraSchemaPrinter();
            vsp.print(veraSchema, new OutputStreamWriter(System.out));
        }

        log.println("Performing global analysis of Vera schema");
        log.incIndent();
        VeraSchemaAnalyzer analyzer = new VeraSchemaAnalyzer(log);
        analyzer.analyze(veraSchema);
        log.decIndent();
        log.println("Global analysis complete");
        analyzer.dumpCounts();

        JoveSchema javaSchema = new JoveSchema();
        IfgenSchema ifSchema = new IfgenSchema();
        PackageNamer packageNamer = new PackageNamer(veraSrcPath);
        String javaPkgArg = cmdLine.getOptionArgValue(cmdLineDef.javaPkgOpt,
            cmdLineDef.javaPkgArg);
        if (javaPkgArg != null)
        {
            packageNamer.setBasePackage(javaPkgArg);
            packageNamer.setExternalPackage(javaPkgArg + ".external");
        }
        JunoSchemaBuilder jsb = new JunoSchemaBuilder(veraSchema, javaSchema,
            ifSchema, analyzer, packageNamer, log);
        JavaRawClass tbFactoryClass = (JavaRawClass) javaSchema
            .getTypeForClass(TestbenchFactory.class.getName());
        DefaultFactoryCallBuilder tbFactoryBuilder = new DefaultFactoryCallBuilder(
            javaSchema, jsb.getTypes(), jsb.getExprConv(), tbFactoryClass, null);
        Iterator iter = compMap.getComponents().iterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof VeraComponent)
            {
                VeraComponent comp = (VeraComponent) obj;
                if (comp.isTestcaseTemplate())
                {
                    jsb.addFactory(comp.getAbstractTestbenchClass(),
                        tbFactoryBuilder);
                }
            }
        }
        jsb.build();

        // write Java source
        JoveSchemaPrinter printer = new JoveSchemaPrinter();
        JavaPackage basePkg = javaSchema.getPackage(packageNamer
            .getBasePackage(), true);
        String javaRootPath = cmdLine.getOptionArgValue(cmdLineDef.javaRootOpt,
            cmdLineDef.javaRootArg);
        File javaRoot = new File(javaRootPath);
        printer.print(basePkg, javaRoot);

        // write Ifgen source
        IfgenSchemaPrinter ifPrinter = new IfgenSchemaPrinter();
        printIfgenPackage(ifPrinter, null, ifSchema.getMembers(), javaRoot);

        // write component->class map
        final String ccmPath = cmdLine.getOptionArgValue(
            cmdLineDef.compClassMapOpt, cmdLineDef.compClassMapArg);
        if (ccmPath != null)
        {
            writeComponentClassMap(ccmPath, veraSrcPath, compMap, veraSchema,
                jsb);
        }
    }

    private void printIfgenPackage(
        IfgenSchemaPrinter printer,
        IfgenPackage pkg,
        List< ? extends IfgenSchemaMember> members,
        File srcRoot)
        throws IOException
    {
        final List<IfgenSchemaMember> nonPackageMembers = new LinkedList<IfgenSchemaMember>();
        for (IfgenSchemaMember member : members)
        {
            if (member instanceof IfgenPackage)
            {
                IfgenPackage pkgMember = (IfgenPackage) member;
                printIfgenPackage(printer, pkgMember, pkgMember.getMembers(),
                    srcRoot);
            }
            else
            {
                nonPackageMembers.add(member);
            }
        }
        if (!nonPackageMembers.isEmpty())
        {
            File ifPkgRoot = IfgenSchemaPrinter.getPackageRoot(pkg, srcRoot);
            String pkgName = (pkg != null) ? pkg.getName().getIdentifier()
                : "default";
            File file = new File(ifPkgRoot, pkgName + ".if");
            printer.printFile(nonPackageMembers, pkg, file);
        }
    }

    private void buildVeraComponentMap(
        final VeraComponentMap cm,
        final File veraRootFile,
        final File veraSrcFile)
        throws IOException, MakeParseException
    {
        final NewisysComponentMapBuilder cmb = new NewisysComponentMapBuilder(
            cm, veraRootFile, veraSrcFile);

        // add user-defined make variables to component map builder
        final List compVarOpts = cmdLine.getArgsForDef(cmdLineDef.compVarOpt);
        if (compVarOpts != null)
        {
            Iterator compVarOptIter = compVarOpts.iterator();
            while (compVarOptIter.hasNext())
            {
                OptionArg opt = (OptionArg) compVarOptIter.next();
                String name = opt.getArgValue(cmdLineDef.compVarNameArg);
                String value = opt.getArgValue(cmdLineDef.compVarValueArg);
                MakeVariable var = new MakeVariable(name,
                    MakeVariableOrigin.DEFAULT, false);
                var.setValue(value);
                cmb.addComponentVariable(var);
            }
        }

        // define all components
        final List veraCompOpts = cmdLine.getArgsForDef(cmdLineDef.veraCompOpt);
        if (veraCompOpts != null)
        {
            Iterator veraCompOptIter = veraCompOpts.iterator();
            while (veraCompOptIter.hasNext())
            {
                OptionArg opt = (OptionArg) veraCompOptIter.next();
                List veraCompArgs = opt.getArgsForDef(cmdLineDef.veraCompArg);
                Iterator veraCompArgIter = veraCompArgs.iterator();
                while (veraCompArgIter.hasNext())
                {
                    StringArg arg = (StringArg) veraCompArgIter.next();
                    String compName = arg.getValue();
                    cmb.addComponent(compName);
                }
            }
        }
    }

    private void buildVeraSchema(
        final Set<VeraAbsComponent> compSet,
        final VeraFileProcessor fp,
        final boolean dryRun)
        throws IOException, InterruptedException, ParseException,
        ValidationException
    {
        final VeraComponentProcessor cp = new VeraComponentProcessor(fp);
        cp.setDryRun(dryRun);

        // build list of system paths from command line arguments
        final List sysPathOpts = cmdLine.getArgsForDef(cmdLineDef.sysPathOpt);
        if (sysPathOpts != null)
        {
            Iterator sysPathOptIter = sysPathOpts.iterator();
            while (sysPathOptIter.hasNext())
            {
                OptionArg opt = (OptionArg) sysPathOptIter.next();
                String path = opt.getArgValue(cmdLineDef.sysPathArg);
                cp.addSysPath(path);
            }
        }

        // process all components
        Iterator compSetIter = compSet.iterator();
        while (compSetIter.hasNext())
        {
            VeraComponent comp = (VeraComponent) compSetIter.next();
            cp.processVeraComponent(comp);
        }
    }

    private Set<VeraAbsComponent> buildCompList(
        final VeraComponentMap compMap,
        final List veraCompOpts,
        final Set<VeraAbsComponent> compList)
    {
        log.println("Building ordered component list:");
        boolean deps = cmdLine.getArgsForDef(cmdLineDef.noDepsOpt) == null;
        Iterator veraCompOptIter = veraCompOpts.iterator();
        while (veraCompOptIter.hasNext())
        {
            OptionArg opt = (OptionArg) veraCompOptIter.next();
            List veraCompArgs = opt.getArgsForDef(cmdLineDef.veraCompArg);
            Iterator veraCompArgIter = veraCompArgs.iterator();
            while (veraCompArgIter.hasNext())
            {
                StringArg arg = (StringArg) veraCompArgIter.next();
                String compName = arg.getValue();
                VeraAbsComponent comp = compMap.getComponent(compName);

                if (deps)
                {
                    Set depComps = comp.getFullDependencies();
                    Iterator depIter = depComps.iterator();
                    while (depIter.hasNext())
                    {
                        VeraAbsComponent depComp = (VeraAbsComponent) depIter
                            .next();
                        addComponent(depComp, compList);
                    }
                }

                addComponent(comp, compList);
            }
        }
        return compList;
    }

    private void addComponent(
        VeraAbsComponent depComp,
        Set<VeraAbsComponent> compSet)
    {
        if (depComp instanceof VeraComponent)
        {
            if (compSet.add(depComp))
            {
                log.println("  " + depComp.getName());
            }
        }
    }

    private void writeComponentClassMap(
        String outputPath,
        String veraSrcPath,
        VeraComponentMap compMap,
        VeraSchema veraSchema,
        JunoSchemaBuilder javaSchemaBuilder)
        throws FileNotFoundException
    {
        log.println("Writing component->class mapping: " + outputPath);
        final PrintWriter out = new PrintWriter(
            new FileOutputStream(outputPath));

        final Iterator schemaMemberIter = veraSchema.getMembers().iterator();
        while (schemaMemberIter.hasNext())
        {
            Object schemaMember = schemaMemberIter.next();
            if (schemaMember instanceof VeraCompilationUnit)
            {
                final VeraCompilationUnit compUnit = (VeraCompilationUnit) schemaMember;

                // determine name of containing component
                final String srcPath = compUnit.getSourcePath();
                // out.println("# file: " + srcPath);
                if (srcPath.startsWith(veraSrcPath))
                {
                    final String relPath = srcPath.substring(veraSrcPath
                        .length() + 1);
                    int lastSepPos = relPath.lastIndexOf(File.separatorChar);
                    if (lastSepPos < 0)
                    {
                        lastSepPos = relPath.lastIndexOf('/');
                    }
                    final String compName = relPath.substring(0, lastSepPos);

                    VeraAbsComponent absComp = compMap.getComponent(compName);
                    if (absComp instanceof VeraComponent)
                    {
                        // out.println("# comp: " + compName);
                        VeraComponent comp = (VeraComponent) absComp;

                        // look up translated testbench class, if present
                        String tbClass = comp.getTestbenchClass();
                        if (tbClass != null)
                        {
                            Iterator tbClassIter = compUnit.lookupObjects(
                                tbClass, VeraNameKind.TYPE);
                            if (tbClassIter.hasNext())
                            {
                                VeraClass veraTBClass = (VeraClass) tbClassIter
                                    .next();
                                JavaRawClass javaTBClass = (JavaRawClass) javaSchemaBuilder
                                    .getTranslatedObject(veraTBClass);
                                out.println(compName + " := "
                                    + javaTBClass.getName());
                            }
                        }

                        // look for programs in compilation unit
                        // (ignore non-source files)
                        else if (comp.getSourceFiles().contains(srcPath))
                        {
                            final Iterator cuMemberIter = compUnit.getMembers()
                                .iterator();
                            while (cuMemberIter.hasNext())
                            {
                                Object cuMember = cuMemberIter.next();
                                if (cuMember instanceof VeraProgram)
                                {
                                    VeraProgram veraProgram = (VeraProgram) cuMember;
                                    JavaRawClass javaProgramClass = (JavaRawClass) javaSchemaBuilder
                                        .getTranslatedObject(veraProgram);
                                    out.println(compName + " := "
                                        + javaProgramClass.getName());
                                }
                            }
                        }
                        else
                        {
                            // out.println("# not a source file");
                        }
                    }
                }
                // out.println();
            }
        }

        out.close();
    }
}
