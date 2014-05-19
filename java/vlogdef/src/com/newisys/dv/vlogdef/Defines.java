/*
 * Jove Verilog Preprocessor Definition Importer
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
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

package com.newisys.dv.vlogdef;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.newisys.langschema.vera.VeraExpression;
import com.newisys.langschema.vera.VeraRange;
import com.newisys.langsource.vera.ExpressionDecl;
import com.newisys.langsource.vera.RangeDecl;
import com.newisys.parser.util.FunctionMacro;
import com.newisys.parser.util.ParseException;
import com.newisys.parser.vera.VeraParser;
import com.newisys.parser.vera.VeraParserBasePPHandler;
import com.newisys.parser.vera.VeraParserReusableTokenManager;
import com.newisys.parser.verapp.VeraPPBaseHandler;
import com.newisys.parser.verapp.VeraPPMacro;
import com.newisys.parser.verapp.VeraPPParser;
import com.newisys.parser.verapp.VeraPPStaticMacro;
import com.newisys.schemabuilder.vera.VeraSchemaBuilder;
import com.newisys.verilog.util.BitRange;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.ValueConverter;

/**
 * Provides dynamic access to #defines declared in Verilog and Vera source
 * files. Uses facilities from Juno to perform evaluation of constant
 * expressions.
 * 
 * @author Trevor Robinson
 */
public class Defines
{
    final HashSet<File> includedFiles = new HashSet<File>();

    private VeraPPBaseHandler ppHandler = new VeraPPBaseHandler()
    {
        @Override
        protected void doInclude(VeraPPParser parser, String path)
            throws ParseException
        {
            File f = new File(path);
            if (!includedFiles.contains(f))
            {
                includedFiles.add(f);
                super.doInclude(parser, path);
            }
        }
    };

    public Defines()
    {
        String paths = System.getProperty("com.newisys.dv.vlogdef.paths");
        if (paths != null)
        {
            String[] pathArray = paths.split("\\" + File.pathSeparatorChar);
            for (String path : pathArray)
            {
                addUserPath(path);
            }
        }
    }

    public void addSysPath(String path)
    {
        ppHandler.addSysPath(path);
    }

    public void addUserPath(String path)
    {
        ppHandler.addUserPath(path);
    }
    
    public void addDefine(String name, String value)
    {
        ppHandler.addDefine(new VeraPPStaticMacro(name, value));
    }

    public void readFile(String filename)
    {
        String path;
        try
        {
            path = ppHandler.resolvePath(filename, false);
            if (path == null) path = filename;
        }
        catch (IOException e)
        {
            throw new VerilogDefineException("Error resolving path '"
                + filename + "': " + e.getMessage(), e);
        }
        File f = new File(path);
        if (!includedFiles.contains(f))
        {
            includedFiles.add(f);
            VeraPPParser ppParser;
            try
            {
                ppParser = new VeraPPParser(f, ppHandler);
            }
            catch (FileNotFoundException e)
            {
                throw new VerilogDefineException("Error opening file '" + path
                    + "': " + e.getMessage(), e);
            }
            try
            {
                ppParser.file();
            }
            catch (ParseException e)
            {
                throw new VerilogDefineException("Error parsing file '" + path
                    + "': " + e.getMessage(), e);
            }
        }
    }

    private VeraPPMacro getDefine(String name)
    {
        VeraPPMacro macro = ppHandler.getDefine(name);
        if (macro == null)
        {
            throw new VerilogDefineException("Unknown define: " + name);
        }
        return macro;
    }

    private String expand(String name, String... args)
    {
        // look up macro
        VeraPPMacro macro = getDefine(name);
        String expansion;
        if (args != null && args.length > 0)
        {
            // check for function macro
            if (!(macro instanceof FunctionMacro))
            {
                throw new VerilogDefineException("Macro '" + name
                    + "' does not accept arguments");
            }
            FunctionMacro funcMacro = (FunctionMacro) macro;

            // check argument count
            List<String> argNames = funcMacro.getArgumentNames();
            int argCount = argNames.size();
            if (argCount != args.length)
            {
                throw new VerilogDefineException("Macro '" + name
                    + "' expects " + argCount + " arguments; " + args.length
                    + " were provided");
            }

            // build argument name->value map
            int index = 0;
            HashMap<String, String> argValues = new HashMap<String, String>();
            for (String argName : argNames)
            {
                argValues.put(argName, args[index++]);
            }

            expansion = funcMacro.expand(argValues);
        }
        else
        {
            expansion = macro.expand();
        }
        return expansion;
    }

    private VeraParserReusableTokenManager preprocess(
        String name,
        String expansion)
    {
        VeraParserReusableTokenManager tm = new VeraParserReusableTokenManager();
        VeraParserBasePPHandler handler = new VeraParserBasePPHandler(tm,
            ppHandler.getDefines());
        VeraPPParser macroPPParser = new VeraPPParser(new StringReader(
            expansion), handler);
        try
        {
            macroPPParser.file();
        }
        catch (ParseException e)
        {
            throw new VerilogDefineException("Error preprocessing expansion '"
                + expansion + "' of macro '" + name + "': " + e.getMessage(), e);
        }
        return tm;
    }

    private Object evaluateExpression(String name, String expansion)
    {
        VeraParserReusableTokenManager tm = preprocess(name, expansion);
        VeraParser macroParser = new VeraParser(tm);
        ExpressionDecl exprDecl;
        try
        {
            exprDecl = macroParser.cond_expr();
        }
        catch (ParseException e)
        {
            throw new VerilogDefineException("Error parsing expression '"
                + tm.getContents() + "' for macro '" + name + "': "
                + e.getMessage(), e);
        }
        VeraSchemaBuilder builder = new VeraSchemaBuilder();
        VeraExpression expr;
        try
        {
            expr = builder.processExpression(exprDecl);
        }
        catch (RuntimeException e)
        {
            throw new VerilogDefineException("Error resolving expression '"
                + exprDecl + "' for macro '" + name + "': " + e.getMessage(), e);
        }
        if (!expr.isConstant())
        {
            throw new VerilogDefineException(
                "Unable to evaluate non-constant expression '"
                    + expr.toSourceString() + "' for macro '" + name + "'");
        }
        return expr.evaluateConstant();
    }

    private BitRange evaluateRange(String name, String expansion)
    {
        VeraParserReusableTokenManager tm = preprocess(name, expansion);
        VeraParser macroParser = new VeraParser(tm);
        RangeDecl rangeDecl;
        try
        {
            rangeDecl = macroParser.range();
        }
        catch (ParseException e)
        {
            throw new VerilogDefineException("Error parsing range '"
                + tm.getContents() + "' for macro '" + name + "': "
                + e.getMessage(), e);
        }
        VeraSchemaBuilder builder = new VeraSchemaBuilder();
        VeraRange range;
        try
        {
            range = builder.processRange(rangeDecl);
        }
        catch (RuntimeException e)
        {
            throw new VerilogDefineException("Error resolving range '"
                + rangeDecl + "' for macro '" + name + "': " + e.getMessage(),
                e);
        }
        if (!range.isConstant())
        {
            throw new VerilogDefineException(
                "Unable to evaluate non-constant range '"
                    + range.toSourceString() + "' for macro '" + name + "'");
        }
        Object fromObj = range.getFrom().evaluateConstant();
        if (!(fromObj instanceof Number || fromObj instanceof BitVector))
        {
            throw new VerilogDefineException("Unexpected from-index type '"
                + fromObj.getClass().getName() + "' in range macro '" + name
                + "'");
        }
        Object toObj = range.getTo().evaluateConstant();
        if (!(toObj instanceof Number || toObj instanceof BitVector))
        {
            throw new VerilogDefineException("Unexpected to-index type '"
                + toObj.getClass().getName() + "' in range macro '" + name
                + "'");
        }
        int from = ValueConverter.toInt(fromObj);
        int to = ValueConverter.toInt(toObj);
        return new BitRange(from, to);
    }

    public Object getObject(String name, String... args)
    {
        String expansion = expand(name, args);
        return evaluateExpression(name, expansion);
    }

    public int getInt(String name, String... args)
    {
        Object value = getObject(name, args);
        return ValueConverter.toInt(value);
    }

    public long getLong(String name, String... args)
    {
        Object value = getObject(name, args);
        return ValueConverter.toLong(value);
    }

    public BitVector getBitVector(String name, String... args)
    {
        Object value = getObject(name, args);
        return ValueConverter.toBitVector(value);
    }

    public BitRange getBitRange(String name, String... args)
    {
        String expansion = expand(name, args);
        return evaluateRange(name, expansion);
    }
}
