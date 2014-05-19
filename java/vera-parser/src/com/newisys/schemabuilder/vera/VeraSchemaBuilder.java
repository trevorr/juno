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

package com.newisys.schemabuilder.vera;

import java.util.*;

import com.newisys.langschema.Expression;
import com.newisys.langschema.FunctionType;
import com.newisys.langschema.Name;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Scope;
import com.newisys.langschema.Type;
import com.newisys.langschema.util.SemanticException;
import com.newisys.langschema.vera.*;
import com.newisys.langsource.SourceObject;
import com.newisys.langsource.vera.*;
import com.newisys.parser.util.FunctionMacro;
import com.newisys.parser.util.Macro;
import com.newisys.parser.util.MacroRef;
import com.newisys.parser.verapp.VeraPPFunctionMacro;
import com.newisys.parser.verapp.VeraPPFunctionMacroRef;
import com.newisys.parser.verapp.VeraPPMacroArgRef;
import com.newisys.parser.verapp.VeraPPUserMacro;
import com.newisys.verilog.EdgeSet;

/**
 * VeraSourceVisitor implementation that builds a VeraSchema from the given
 * source objects.
 * 
 * @author Trevor Robinson
 */
public final class VeraSchemaBuilder
    implements VeraSourceVisitor
{
    private final VeraSchema schema;
    private final VeraAnonymousScope globalScope;
    private final List<Runnable> deferredProcesses;
    private final Map<MacroRef, Map<String, VeraExpression>> macroArgMap;

    private VeraPreprocessorInfo preprocInfo;
    private boolean wantShell;

    private VeraCompilationUnitScope compUnitScope;
    VeraScopeDelegate currentScope;
    private VeraCompilationUnit primaryCompUnit;
    private Set<VeraCompilationUnitMember> shellMembers;

    private VeraUserClass currentClass;
    private boolean externClass;

    private VeraType currentType;
    private Type lhsType;

    private VeraEnumeration currentEnum;
    private boolean newEnum;
    private int nextEnumValue;

    private VeraFunction currentFunc;
    private boolean newFunc;
    private int currentArgPos;

    private VeraVariable currentVar;

    private VeraStatement currentStmt;
    private VeraRandCase currentRandCase;

    private VeraExpression currentExpr;

    private VeraInterfaceType currentIntf;
    private VeraInterfaceSignal currentIntfSignal;

    private VeraBindVariable currentBind;
    private VeraBindMember currentBindMember;

    private VeraProgram currentProgram;

    public VeraSchemaBuilder()
    {
        schema = new VeraSchema();
        globalScope = new VeraAnonymousScope();
        deferredProcesses = new LinkedList<Runnable>();
        macroArgMap = new HashMap<MacroRef, Map<String, VeraExpression>>();
        currentScope = globalScope;

        defineBuiltinFunctions();
    }

    private void defineBuiltinFunctions()
    {
        VeraFunction f;
        VeraFunctionArgument a;

        f = defineBuiltinFunction("alloc", schema.integerType);
        addArgument(f, "type", schema.integerType);
        addArgument(f, "id", schema.integerType);
        addArgument(f, "count", schema.integerType);
        addOptArgument(f, "key_count", schema.integerType);

        // Vera extension: treat assert as a built-in function
        f = defineBuiltinFunction("assert", schema.voidType);
        addArgument(f, "expr", schema.integerType);

        f = defineBuiltinFunction("assoc_index", schema.integerType);
        addArgument(f, "op", schema.integerType);
        a = addArgument(f, "array", schema.magicType);
        a.setByRef(true);
        a = addOptArgument(f, "index", schema.magicType);
        a.setByRef(true);

        f = defineBuiltinFunction("cast_assign", schema.integerType);
        a = addArgument(f, "dst", schema.magicType);
        a.setByRef(true);
        a.setReturnsXZ(true);
        addArgument(f, "src", schema.magicType);
        addOptArgument(f, "fatal", 1); // CHECK = 0

        f = defineBuiltinFunction("delay", schema.voidType);
        addArgument(f, "time", schema.integerType);

        f = defineBuiltinFunction("error", schema.voidType);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("error_mode", schema.voidType);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "cls", schema.integerType);

        f = defineBuiltinFunction("exit", schema.voidType);
        addArgument(f, "status", schema.integerType);

        f = defineBuiltinFunction("fclose", schema.voidType);
        addArgument(f, "fd", schema.integerType);

        f = defineBuiltinFunction("feof", schema.integerType);
        addArgument(f, "fd", schema.integerType);

        f = defineBuiltinFunction("ferror", schema.integerType);
        addArgument(f, "fd", schema.integerType);

        f = defineBuiltinFunction("fflush", schema.voidType);
        addArgument(f, "fd", schema.integerType);

        f = defineBuiltinFunction("flag", schema.integerType);
        addOptArgument(f, "op", -1); // OFF = 0, ON = 1

        f = defineBuiltinFunction("fopen", schema.integerType);
        addArgument(f, "filename", schema.stringType);
        addArgument(f, "mode", schema.stringType);
        addOptArgument(f, "verbose", 4); // VERBOSE = 4, SILENT = 0

        f = defineBuiltinFunction("fprintf", schema.voidType);
        addArgument(f, "fd", schema.integerType);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("freadb", schema.bitVectorType, true);
        addArgument(f, "fd", schema.integerType);
        addOptArgument(f, "verbose", 4); // VERBOSE = 4, SILENT = 0

        f = defineBuiltinFunction("freadh", schema.bitVectorType, true);
        addArgument(f, "fd", schema.integerType);
        addOptArgument(f, "verbose", 4); // VERBOSE = 4, SILENT = 0

        f = defineBuiltinFunction("freadstr", schema.stringType);
        addArgument(f, "fd", schema.integerType);
        addOptArgument(f, "verbose", 4); // VERBOSE = 4, SILENT = 0, RAWIN = 1

        f = defineBuiltinFunction("getstate", schema.voidType);
        a = addArgument(f, "state", schema.bitVector64Type);
        a.setByRef(true);
        addOptArgument(f, "obj", schema.magicType);

        f = defineBuiltinFunction("get_coverage", schema.integerType);
        // no arguments

        f = defineBuiltinFunction("get_cycle", schema.bitVector32Type);
        addOptArgument(f, "signal", schema.magicType);

        f = defineBuiltinFunction("get_inst_coverage", schema.integerType);
        // no arguments

        f = defineBuiltinFunction("get_plus_arg", schema.bitVectorType, true);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "name", schema.stringType);

        f = defineBuiltinFunction("get_systime", schema.bitVector32Type);
        // no arguments

        f = defineBuiltinFunction("get_time", schema.bitVector32Type);
        addArgument(f, "part", schema.integerType);

        f = defineBuiltinFunction("get_time_unit", schema.integerType);
        // no arguments

        f = defineBuiltinFunction("initstate", schema.voidType);
        addArgument(f, "seed", schema.integerType);
        a = addArgument(f, "state", schema.bitVector64Type);
        a.setByRef(true);
        addOptArgument(f, "obj", schema.magicType);

        f = defineBuiltinFunction("lock_file", schema.integerType);
        addArgument(f, "filename", schema.stringType);
        addArgument(f, "timeout", schema.integerType);

        f = defineBuiltinFunction("mailbox_get", schema.integerType);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "id", schema.integerType);
        a = addOptArgument(f, "data", schema.magicType);
        a.setByRef(true);
        a.setReturnsXZ(true);
        addOptArgument(f, "fatal", 1); // CHECK = 0

        f = defineBuiltinFunction("mailbox_put", schema.voidType);
        addArgument(f, "id", schema.integerType);
        addArgument(f, "data", schema.magicType);

        f = defineBuiltinFunction("os_command", schema.integerType);
        addArgument(f, "command", schema.stringType);

        f = defineBuiltinFunction("printf", schema.voidType);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("prodget", schema.magicType, true);
        addOptArgument(f, "name", schema.stringType);
        addOptArgument(f, "occurrence", 1);

        f = defineBuiltinFunction("prodset", schema.voidType);
        addArgument(f, "value", schema.magicType);
        addOptArgument(f, "name", schema.stringType);
        addOptArgument(f, "occurrence", 1);

        f = defineBuiltinFunction("psprintf", schema.stringType);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("rand48", schema.integerType);
        addOptArgument(f, "seed", schema.integerType);

        f = defineBuiltinFunction("random", schema.integerType);
        addOptArgument(f, "seed", schema.integerType);

        f = defineBuiltinFunction("region_enter", schema.integerType);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "id", schema.integerType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("region_exit", schema.voidType);
        addArgument(f, "id", schema.integerType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("rewind", schema.voidType);
        addArgument(f, "fd", schema.integerType);

        f = defineBuiltinFunction("semaphore_get", schema.integerType);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "id", schema.integerType);
        addArgument(f, "key_count", schema.integerType);

        f = defineBuiltinFunction("semaphore_put", schema.voidType);
        addArgument(f, "id", schema.integerType);
        addArgument(f, "key_count", schema.integerType);

        f = defineBuiltinFunction("setstate", schema.voidType);
        a = addArgument(f, "state", schema.bitVector64Type);
        a.setByRef(true);
        addOptArgument(f, "obj", schema.magicType);

        f = defineBuiltinFunction("signal_connect", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);
        addArgument(f, "target_signal", schema.magicType);
        addOptArgument(f, "attributes", schema.stringType);
        addOptArgument(f, "clock", schema.magicType);

        f = defineBuiltinFunction("sprintf", schema.voidType);
        a = addArgument(f, "str", schema.stringType);
        a.setByRef(true);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("srandom", schema.voidType);
        addArgument(f, "seed", schema.integerType);
        addOptArgument(f, "obj", schema.magicType);

        f = defineBuiltinFunction("sscanf", schema.voidType);
        addArgument(f, "str", schema.stringType);
        addArgument(f, "format", schema.stringType);
        setVarArgs(f, true);

        f = defineBuiltinFunction("stop", schema.voidType);
        // no arguments

        f = defineBuiltinFunction("suspend_thread", schema.voidType);
        // no arguments

        f = defineBuiltinFunction("sync", schema.integerType);
        addArgument(f, "op", schema.integerType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("timeout", schema.voidType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("trace", schema.voidType);
        addArgument(f, "op", schema.integerType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("trigger", schema.voidType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("unlock_file", schema.voidType);
        addArgument(f, "filename", schema.stringType);

        f = defineBuiltinFunction("urand48", schema.bitVector32Type);
        addOptArgument(f, "seed", schema.integerType);

        f = defineBuiltinFunction("urandom", schema.bitVector32Type);
        addOptArgument(f, "seed", schema.integerType);

        f = defineBuiltinFunction("urandom_range", schema.bitVector32Type);
        addArgument(f, "maxval", schema.bitVector32Type);
        addOptArgument(f, "minval", schema.bitVector32Type,
            new VeraIntegerLiteral(schema, 0));

        f = defineBuiltinFunction("vca", schema.voidType);
        addArgument(f, "op", schema.integerType);
        addArgument(f, "intf_signal", schema.magicType);

        f = defineBuiltinFunction("vera_bit_reverse", schema.voidType);
        a = addArgument(f, "dst", schema.bitVectorType);
        a.setByRef(true);
        addArgument(f, "src", schema.bitVectorType);

        // X/Z inputs are documented to cause a fatal error,
        // so we assume vera_crc does not return X/Z
        f = defineBuiltinFunction("vera_crc", schema.bitVector64Type);
        addArgument(f, "order", schema.integerType);
        addArgument(f, "stream", schema.magicType);
        addArgument(f, "index1", schema.bitVector64Type);
        addArgument(f, "index2", schema.bitVector64Type);
        addOptArgument(f, "init_crc", schema.bitVectorType);

        f = defineBuiltinFunction("vera_get_clk_name", schema.stringType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_dir", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_ifc_name", schema.stringType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_in_depth", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_in_skew", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_in_type", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_name", schema.stringType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_out_skew", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_out_type", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_get_surrx_D", schema.voidType);
        addArgument(f, "port_signal", schema.magicType);
        a = addArgument(f, "time_to_x", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "time_to_value", schema.integerType);
        a.setByRef(true);

        f = defineBuiltinFunction("vera_get_surrx_F", schema.voidType);
        addArgument(f, "port_signal", schema.magicType);
        a = addArgument(f, "time_to_x", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "time_to_value", schema.integerType);
        a.setByRef(true);

        f = defineBuiltinFunction("vera_get_surrx_R", schema.voidType);
        addArgument(f, "port_signal", schema.magicType);
        a = addArgument(f, "time_to_x", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "time_to_value", schema.integerType);
        a.setByRef(true);

        f = defineBuiltinFunction("vera_get_surrx_Z", schema.voidType);
        addArgument(f, "port_signal", schema.magicType);
        a = addArgument(f, "time_to_x", schema.integerType);
        a.setByRef(true);
        a = addArgument(f, "time_to_value", schema.integerType);
        a.setByRef(true);

        f = defineBuiltinFunction("vera_get_width", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_has_surrx", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_is_bound", schema.integerType);
        addArgument(f, "port_signal", schema.magicType);

        f = defineBuiltinFunction("vera_pack", schema.integerType);
        addArgument(f, "storage", schema.magicType);
        a = addArgument(f, "bit_offset", schema.integerType);
        a.setByRef(true);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vera_pack_big_endian", schema.integerType);
        addArgument(f, "storage", schema.magicType);
        a = addArgument(f, "bit_offset", schema.integerType);
        a.setByRef(true);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vera_plot", schema.voidType);
        addArgument(f, "filename", schema.stringType);
        addArgument(f, "format", schema.integerType);
        addArgument(f, "variable", schema.stringType);
        addArgument(f, "mode", schema.integerType);
        addOptArgument(f, "dump", schema.stringType);

        f = defineBuiltinFunction("vera_report_profile", schema.voidType);
        addArgument(f, "type", schema.integerType);
        addArgument(f, "filename", schema.stringType);

        f = defineBuiltinFunction("vera_unpack", schema.integerType);
        addArgument(f, "storage", schema.magicType);
        a = addArgument(f, "bit_offset", schema.integerType);
        a.setByRef(true);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vera_unpack_big_endian", schema.integerType);
        addArgument(f, "storage", schema.magicType);
        a = addArgument(f, "bit_offset", schema.integerType);
        a.setByRef(true);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vsv_call_func", schema.integerType);
        addArgument(f, "connection", schema.integerType);
        addArgument(f, "time_mode", schema.integerType);
        addArgument(f, "func_name", schema.stringType);
        addArgument(f, "return_value", schema.magicType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vsv_call_task", schema.integerType);
        addArgument(f, "connection", schema.integerType);
        addArgument(f, "time_mode", schema.integerType);
        addArgument(f, "task_name", schema.stringType);
        setVarArgs(f, false);

        f = defineBuiltinFunction("vsv_close_conn", schema.integerType);
        addArgument(f, "connection", schema.integerType);

        f = defineBuiltinFunction("vsv_get_conn_err", schema.stringType);
        // no arguments

        f = defineBuiltinFunction("vsv_make_client", schema.integerType);
        addArgument(f, "host", schema.stringType);
        addArgument(f, "port", schema.integerType);
        addArgument(f, "authentication", schema.integerType);

        f = defineBuiltinFunction("vsv_make_server", schema.integerType);
        addArgument(f, "port", schema.integerType);
        addArgument(f, "authentication", schema.integerType);
        addOptArgument(f, "verbose", 0); // VERBOSE = 4

        f = defineBuiltinFunction("vsv_up_connections", schema.integerType);
        addArgument(f, "timeout", schema.integerType);

        f = defineBuiltinFunction("vsv_wait_for_done", schema.voidType);
        // no arguments

        f = defineBuiltinFunction("vsv_wait_for_input", schema.voidType);
        addArgument(f, "time_mode", schema.integerType);

        f = defineBuiltinFunction("wait_child", schema.voidType);
        // no arguments

        f = defineBuiltinFunction("wait_var", schema.voidType);
        setVarArgs(f, false);
    }

    private VeraFunction defineBuiltinFunction(String id, VeraType returnType)
    {
        return defineBuiltinFunction(id, returnType, false);
    }

    private VeraFunction defineBuiltinFunction(
        String id,
        VeraType returnType,
        boolean returnsXZ)
    {
        final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
        final VeraFunctionType funcType = new VeraFunctionType(returnType,
            returnsXZ);
        final VeraGlobalFunction func = new VeraGlobalFunction(name, funcType);
        schema.addMember(func);
        globalScope.addObject(func);
        return func;
    }

    private VeraFunctionArgument addArgument(
        VeraFunction func,
        String id,
        VeraType type)
    {
        final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
        final VeraFunctionArgument arg = new VeraFunctionArgument(name, type,
            func);
        VeraFunctionType funcType = func.getType();
        funcType.addArgument(arg);
        return arg;
    }

    private VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        int defValue)
    {
        return addOptArgument(func, id, schema.integerType,
            new VeraIntegerLiteral(schema, defValue));
    }

    private VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        VeraType type)
    {
        return addOptArgument(func, id, type, null);
    }

    private VeraFunctionArgument addOptArgument(
        VeraFunction func,
        String id,
        VeraType type,
        VeraExpression defValue)
    {
        final VeraFunctionArgument arg = addArgument(func, id, type);
        arg.setInitializer(defValue);
        arg.setOptional(true);
        arg.setOptionalLevel(1);
        return arg;
    }

    private void setVarArgs(VeraFunction func, boolean byRef)
    {
        VeraFunctionType funcType = func.getType();
        funcType.setVarArgs(true);
        funcType.setVarArgsByRef(byRef);
    }

    public VeraSchema getSchema()
    {
        return schema;
    }

    public VeraPreprocessorInfo getPreprocInfo()
    {
        return preprocInfo;
    }

    public void setPreprocInfo(VeraPreprocessorInfo preprocInfo)
    {
        this.preprocInfo = preprocInfo;
    }

    public boolean isWantShell()
    {
        return wantShell;
    }

    public void setWantShell(boolean wantShell)
    {
        this.wantShell = wantShell;
    }

    public VeraProgram getProgram()
    {
        return currentProgram;
    }

    private void copyAnnotations(SourceObject from, VeraSchemaObject to)
    {
        if (from.hasAnnotations())
        {
            to.addAnnotations(from.getAnnotations());
        }
    }

    private void addDeferredProcess(Runnable process)
    {
        deferredProcesses.add(process);
    }

    private void runDeferredProcesses()
    {
        final Iterator iter = deferredProcesses.iterator();
        while (iter.hasNext())
        {
            Runnable process = (Runnable) iter.next();
            process.run();
            iter.remove();
        }
    }

    private void checkDefineRef(MacroDecl srcObj, VeraDefineReferrer schemaObj)
    {
        MacroRef macroRef = null;
        final MacroRef firstRef = srcObj.getFirstExpandedFrom();
        if (firstRef instanceof VeraPPMacroArgRef)
        {
            if (schemaObj instanceof VeraExpression)
            {
                // first expansion is function macro argument
                addMacroArg((VeraPPMacroArgRef) firstRef,
                    (VeraExpression) schemaObj);
            }

            // use next lower expansion, if any
            MacroRef nextRef = srcObj.getLastExpandedFrom();
            while (nextRef != null && nextRef != firstRef)
            {
                macroRef = nextRef;
                nextRef = nextRef.getExpandedFrom();
            }
        }
        else
        {
            // use first expansion
            macroRef = firstRef;
        }

        if (macroRef != null)
        {
            Map<String, VeraExpression> argRefMap = null;
            if (macroRef instanceof VeraPPFunctionMacroRef)
            {
                argRefMap = getMacroArgs(macroRef);
            }

            final Macro macro = macroRef.getMacro();
            final String id = macro.getName();
            final Iterator iter = currentScope.lookupObjects(id,
                VeraNameKind.PREPROC);
            badRef: if (iter.hasNext())
            {
                VeraDefine define = (VeraDefine) iter.next();
                checkMultipleDefinition(iter);

                VeraDefineReference defineRef = new VeraDefineReference(define);
                List<VeraDefineArgument> defineArgs = define.getArguments();
                if (!defineArgs.isEmpty())
                {
                    if (argRefMap == null) break badRef;

                    for (VeraDefineArgument defineArg : defineArgs)
                    {
                        String argID = defineArg.getName().getIdentifier();
                        VeraExpression argExpr = argRefMap.get(argID);
                        if (argExpr == null) break badRef;
                        defineRef.addArgument(argExpr);
                    }
                }
                schemaObj.setDefineRef(defineRef);
            }

            if (argRefMap != null)
            {
                clearMacroArgs(macroRef);
            }
        }
    }

    private void addMacroArg(VeraPPMacroArgRef argRef, VeraExpression expr)
    {
        MacroRef funcRef = argRef.getExpandedFrom();
        assert (funcRef instanceof VeraPPFunctionMacroRef);
        Map<String, VeraExpression> argRefMap = macroArgMap.get(funcRef);
        if (argRefMap == null)
        {
            argRefMap = new HashMap<String, VeraExpression>();
            macroArgMap.put(funcRef, argRefMap);
        }
        argRefMap.put(argRef.getArg(), expr);
    }

    private Map<String, VeraExpression> getMacroArgs(MacroRef funcRef)
    {
        return macroArgMap.get(funcRef);
    }

    private void clearMacroArgs(MacroRef funcRef)
    {
        macroArgMap.remove(funcRef);
    }

    public void visit(ArrayAccessDecl obj)
    {
        // process array expression
        final ExpressionDecl arrayExprDecl = obj.getArrayExpr();
        final VeraExpression arrayExpr = processExpression(arrayExprDecl);

        // check array expression type
        final Type arrayExprType = arrayExpr.getResultType();
        final Type[] indexTypes;
        if (arrayExprType instanceof VeraArrayType)
        {
            final VeraArrayType arrayType = (VeraArrayType) arrayExprType;
            indexTypes = arrayType.getIndexTypes();
        }
        else if (arrayExprType instanceof VeraBitVectorType)
        {
            indexTypes = new Type[] { schema.integerType };
        }
        else
        {
            throw new SourceSemanticException(
                "Array or bit vector reference expected", arrayExprDecl);
        }

        // check array dimensions
        final List indexExprDecls = obj.getIndexExprs();
        if (indexExprDecls.size() != indexTypes.length)
        {
            throw new SourceSemanticException(
                "Incorrect number of array indices; " + indexTypes.length
                    + " expected", obj);
        }

        // create schema object
        final VeraArrayAccess arrayAccess = new VeraArrayAccess(arrayExpr);
        copyAnnotations(obj, arrayAccess);

        // process indices
        final Iterator iter = indexExprDecls.iterator();
        int dimension = 0;
        while (iter.hasNext())
        {
            final ExpressionDecl indexExprDecl = (ExpressionDecl) iter.next();
            final VeraExpression indexExpr = processExpression(indexExprDecl);
            final Type indexType = indexTypes[dimension];
            if (!indexType.isAssignableFrom(indexExpr.getResultType()))
            {
                throw new SourceSemanticException(
                    "Array index expression must be assignable to " + indexType,
                    indexExprDecl);
            }
            arrayAccess.addIndex(indexExpr);
            ++dimension;
        }

        checkDefineRef(obj, arrayAccess);
        currentExpr = arrayAccess;
    }

    public void visit(ArrayInitDecl obj)
    {
        // get array type
        assert (lhsType != null);
        if (!(lhsType instanceof VeraFixedArrayType))
        {
            throw new SourceSemanticException(
                "Fixed array type expected for left-hand expression", obj);
        }
        final VeraFixedArrayType type = (VeraFixedArrayType) lhsType;

        // create initializer expression
        final VeraArrayInitializer initExpr = new VeraArrayInitializer(type);
        copyAnnotations(obj, initExpr);

        // save LHS type
        final Type prevLHSType = lhsType;

        // update LHS type for nested initializers
        final Type[] indexTypes = type.getIndexTypes();
        final int[] highBounds = type.getHighBounds();
        final Type elementType = type.getElementType();
        int dimCount = indexTypes.length;
        if (dimCount > 1)
        {
            // multi-dimensional array: drop the first dimension
            --dimCount;
            final int[] nestedDimensions = new int[dimCount];
            for (int i = 0; i < dimCount; ++i)
            {
                nestedDimensions[i] = highBounds[i + 1] + 1;
            }
            lhsType = new VeraFixedArrayType((VeraType) elementType,
                nestedDimensions);
        }
        else
        {
            // no more array dimensions: use element type
            lhsType = elementType;
        }

        // process elements
        final List elements = obj.getElements();
        if (elements.size() > highBounds[0] + 1)
        {
            throw new SourceSemanticException(
                "Too many elements in array initializer", obj);
        }
        final Iterator elemIter = elements.iterator();
        while (elemIter.hasNext())
        {
            final ExpressionDecl argExprDecl = (ExpressionDecl) elemIter.next();
            final VeraExpression argExpr = processExpression(argExprDecl);
            if (!lhsType.isAssignableFrom(argExpr.getResultType()))
            {
                throw new SourceSemanticException(
                    "Type mismatch for array initializer element", argExprDecl);
            }
            initExpr.addElement(argExpr);
        }

        // restore LHS type
        lhsType = prevLHSType;

        checkDefineRef(obj, initExpr);
        currentExpr = initExpr;
    }

    public void visit(ArrayTypeRef obj)
    {
        // process the element type
        final VeraType elementType = processType(obj.getElementTypeRef());

        // return the array type in currentType
        final ArrayKind arrayKind = obj.getArrayKind();
        final VeraArrayType arrayType;
        if (arrayKind == ArrayKind.BIT_ASSOCIATIVE)
        {
            arrayType = new VeraAssocArrayType(elementType,
                schema.bitVector64Type);
        }
        else if (arrayKind == ArrayKind.STRING_ASSOCIATIVE)
        {
            arrayType = new VeraAssocArrayType(elementType, schema.stringType);
        }
        else
        {
            assert (arrayKind == ArrayKind.DYNAMIC);
            arrayType = new VeraDynamicArrayType(elementType);
        }
        checkDefineRef(obj, arrayType);
        currentType = arrayType;
    }

    public void visit(BindDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // look up bind by identifier in current scope
        final String id = obj.getIdentifier();
        VeraBindVariable bind = lookupBind(id, false);
        if (bind == null)
        {
            // create new bind type
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
            final VeraPortType port;
            try
            {
                port = lookupPort(obj.getPortIdentifier(), true);
            }
            catch (SourceSemanticException e)
            {
                e.setSourceObject(obj);
                throw e;
            }
            bind = new VeraBindVariable(name, port);
            copyAnnotations(obj, bind);

            // add bind signals
            HashSet<VeraPortSignal> portMembers = new HashSet<VeraPortSignal>(
                port.getMembers());
            currentBind = bind;
            final Iterator iter = obj.getSignals().iterator();
            while (iter.hasNext())
            {
                final BindSignalDecl signalDecl = (BindSignalDecl) iter.next();
                signalDecl.accept(this);
                assert (currentBindMember != null);
                final VeraBindMember member = currentBindMember;
                currentBindMember = null;
                bind.addMember(member);
                portMembers.remove(member.getPortSignal());
            }
            currentBind = null;
            if (!portMembers.isEmpty())
            {
                final VeraPortSignal firstSignal = portMembers.iterator()
                    .next();
                throw new SourceSemanticException(
                    "Missing bind definition for port signal '"
                        + firstSignal.getName() + "'", obj);
            }

            // add bind to compilation unit
            compUnit.addMember(bind);
        }
    }

    public void visit(BindSignalDecl obj)
    {
        // get port signal
        final VeraPortType port = currentBind.getPort();
        final VeraPortSignal portSignal;
        {
            final String portSignalID = obj.getPortMember();
            final Iterator nameIter = port.lookupObjects(portSignalID,
                VeraNameKind.NON_TYPE);
            if (!nameIter.hasNext())
            {
                throw new SourceSemanticException("Unknown port signal '"
                    + portSignalID + "'", obj);
            }
            portSignal = (VeraPortSignal) nameIter.next();
        }

        // process signal ranges
        VeraExpression firstRange = null;
        VeraConcatenation concat = null;
        Iterator iter = obj.getSignalRanges().iterator();
        while (iter.hasNext())
        {
            final SignalRangeDecl rangeDecl = (SignalRangeDecl) iter.next();

            // get interface
            final VeraInterfaceType intf;
            try
            {
                String intfID = rangeDecl.getInterfaceIdentifier();
                intf = lookupInterface(intfID, true);
            }
            catch (SourceSemanticException e)
            {
                e.setSourceObject(obj);
                throw e;
            }

            // get signal
            final String intfSignalID = rangeDecl.getSignalIdentifier();
            final VeraInterfaceSignal intfSignal;
            {
                final Iterator nameIter = intf.lookupObjects(intfSignalID,
                    VeraNameKind.NON_TYPE);
                if (!nameIter.hasNext())
                {
                    throw new SourceSemanticException(
                        "Unknown interface signal '" + intfSignalID + "'", obj);
                }
                intfSignal = (VeraInterfaceSignal) nameIter.next();
            }
            VeraExpression signalRef = new VeraSignalReference(intfSignal);

            // get bitfield (if any)
            RangeDecl bitfield = rangeDecl.getBitfield();
            if (bitfield != null)
            {
                final int width = intfSignal.getWidth();
                final ExpressionDecl highExprDecl = bitfield.getTo();
                final ExpressionDecl lowExprDecl = bitfield.getFrom();

                // check whether field is slice or single bit
                if (lowExprDecl != highExprDecl)
                {
                    VeraRange range = processRange(bitfield);

                    // evaluate index expressions
                    int high = evalIntExpr(range.getFrom(), highExprDecl);
                    int low = evalIntExpr(range.getTo(), lowExprDecl);

                    // check slice indices
                    if (low > high || low < 0 || high >= width)
                    {
                        throw new SourceSemanticException("Invalid bit slice",
                            bitfield);
                    }

                    // create new range containing integer literals
                    VeraExpression highExpr = new VeraIntegerLiteral(schema,
                        high);
                    VeraExpression lowExpr = new VeraIntegerLiteral(schema, low);
                    VeraRange constRange = new VeraRange(schema, highExpr,
                        lowExpr);
                    constRange.setDefineRef(range.getDefineRef());

                    // change result expression to bit slice
                    signalRef = new VeraBitSliceAccess(signalRef, constRange);
                }
                else
                {
                    // evaluate high index and create expression for it
                    int high = evalIntExpr(highExprDecl);
                    VeraExpression highExpr = new VeraIntegerLiteral(schema,
                        high);

                    // check bit index
                    if (high < 0 || high >= width)
                    {
                        throw new SourceSemanticException("Invalid bit access",
                            bitfield);
                    }

                    // change result expression to bit access
                    VeraArrayAccess arrayAccess = new VeraArrayAccess(signalRef);
                    arrayAccess.addIndex(highExpr);
                    signalRef = arrayAccess;
                }
            }

            // add expression to concatenation if necessary
            if (firstRange != null)
            {
                if (concat == null)
                {
                    concat = new VeraConcatenation(schema);
                    concat.addOperand(firstRange);
                }
                concat.addOperand(signalRef);
            }
            else
            {
                firstRange = signalRef;
            }
        }

        // determine final signal expression
        final VeraExpression intfExpr;
        if (concat != null)
        {
            // concatenation bind
            intfExpr = concat;
        }
        else if (firstRange != null)
        {
            // simple bind
            intfExpr = firstRange;
        }
        else
        {
            // void bind
            intfExpr = new VeraVoidLiteral(schema);
        }

        currentBindMember = new VeraBindMember(portSignal, intfExpr);
        copyAnnotations(obj, currentBindMember);
    }

    public void visit(BitSliceAccessDecl obj)
    {
        // process bit vector expression
        final ExpressionDecl bitExprDecl = obj.getBitExpr();
        final VeraExpression bitExpr = processExpression(bitExprDecl);
        final Type bitExprType = bitExpr.getResultType();
        if (!(bitExprType instanceof VeraBitVectorType))
        {
            throw new SourceSemanticException("Bit vector reference expected",
                bitExprDecl);
        }

        // process range
        final RangeDecl rangeDecl = obj.getRange();
        final VeraRange range = processRange(rangeDecl);
        if (!range.getFrom().getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Bit slice index expression must be integral", rangeDecl
                    .getFrom());
        }
        if (!range.getTo().getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Bit slice index expression must be integral", rangeDecl
                    .getTo());
        }

        currentExpr = new VeraBitSliceAccess(bitExpr, range);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(BitVectorLiteralDecl obj)
    {
        VeraBitVectorLiteral bvLiteral = new VeraBitVectorLiteral(schema, obj
            .getValue());
        bvLiteral.setRadix(obj.getRadix());
        copyAnnotations(obj, bvLiteral);
        checkDefineRef(obj, bvLiteral);
        currentExpr = bvLiteral;
    }

    public void visit(BitVectorTypeRef obj)
    {
        // determine size by evaluating high bit expression
        final ExpressionDecl highExprDecl = obj.getHighBitExpr();
        final int size = evalIntExpr(highExprDecl) + 1;
        if (size <= 0)
        {
            throw new SourceSemanticException(
                "Invalid size for bit vector type", highExprDecl);
        }

        final VeraFixedBitVectorType bvType = new VeraFixedBitVectorType(
            schema, size);
        checkDefineRef(obj, bvType);
        currentType = bvType;
    }

    public void visit(BlockDecl obj)
    {
        final VeraBlock block = new VeraBlock(schema);
        copyAnnotations(obj, block);

        // block scope becomes current scope
        final VeraScopeDelegate prevScope = currentScope;
        currentScope = new VeraSimpleScope(block, prevScope);

        // process local variable declarations
        Iterator iter = obj.getLocalVars().iterator();
        while (iter.hasNext())
        {
            final LocalVarDecl varDecl = (LocalVarDecl) iter.next();
            varDecl.accept(this);
            assert (currentVar != null);
            final VeraLocalVariable var = (VeraLocalVariable) currentVar;
            currentVar = null;

            checkDuplicateName(block, var, obj);
            block.addMember(var);
        }

        // process statements
        iter = obj.getStatements().iterator();
        while (iter.hasNext())
        {
            final StatementDecl stmt = (StatementDecl) iter.next();
            stmt.accept(this);
            assert (currentStmt != null);
            block.addMember(currentStmt);
            currentStmt = null;
        }

        // restore previous scope
        currentScope = prevScope;

        checkDefineRef(obj, block);
        currentStmt = block;
    }

    public void visit(BreakDecl obj)
    {
        currentStmt = new VeraBreakStatement(schema);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(BreakpointDecl obj)
    {
        currentStmt = new VeraBreakpointStatement(schema);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(CaseDecl obj)
    {
        // process selector expression
        final VeraExpression selectorExpr = processExpression(obj.getCaseExpr());

        // create switch schema object
        final VeraSwitchStatement switchStmt = new VeraSwitchStatement(obj
            .getCaseKind(), selectorExpr);
        copyAnnotations(obj, switchStmt);

        // process case members
        final Iterator iter = obj.getCaseMembers().iterator();
        while (iter.hasNext())
        {
            final CaseMemberDecl memberDecl = (CaseMemberDecl) iter.next();
            final VeraSwitchValueCase valueCase = switchStmt.newValueCase();

            // process case value expressions
            final Iterator valueIter = memberDecl.getExprs().iterator();
            while (valueIter.hasNext())
            {
                ExpressionDecl valueExprDecl = (ExpressionDecl) valueIter
                    .next();
                VeraExpression valueExpr = processExpression(valueExprDecl);
                if (!selectorExpr.getResultType().isAssignableFrom(
                    valueExpr.getResultType()))
                {
                    throw new SourceSemanticException(
                        "Case expression must be assignable to type of selector",
                        memberDecl);
                }
                valueCase.addValue(valueExpr);
            }

            // process case statement
            final StatementDecl stmtDecl = memberDecl.getStatement();
            stmtDecl.accept(this);
            assert (currentStmt != null);
            final VeraStatement stmt = currentStmt;
            currentStmt = null;
            valueCase.setStatement(stmt);
        }

        // process default statement
        final StatementDecl defStmtDecl = obj.getDefaultStatement();
        if (defStmtDecl != null)
        {
            defStmtDecl.accept(this);
            assert (currentStmt != null);
            final VeraStatement defStmt = currentStmt;
            currentStmt = null;

            // create default switch case for default statement
            final VeraSwitchDefaultCase defCase = switchStmt.newDefaultCase();
            defCase.setStatement(defStmt);
        }

        checkDefineRef(obj, switchStmt);
        currentStmt = switchStmt;
    }

    public void visit(CaseMemberDecl obj)
    {
        // handled in visit(CaseDecl)
        assert false;
    }

    public void visit(ClassConstraintDecl obj)
    {
        // must be within the context of a class
        assert (currentClass != null);

        // look up constraint in class
        final VeraClassConstraint cons;
        final String id = obj.getIdentifier();
        final Iterator iter = currentClass.lookupObjects(id,
            VeraNameKind.NON_TYPE);
        if (iter.hasNext())
        {
            final NamedObject foundObj = (NamedObject) iter.next();
            checkMultipleDefinition(iter);
            if (!(foundObj instanceof VeraClassConstraint))
            {
                throw new SourceSemanticException("Identifier '" + id
                    + "' already used for non-constraint", obj);
            }
            cons = (VeraClassConstraint) foundObj;
        }
        else
        {
            // create new class constraint
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE,
                currentClass);
            cons = new VeraClassConstraint(schema, name);
            copyAnnotations(obj, cons);
            currentClass.addMember(cons);
        }

        // process expressions in constraint body (if present)
        if (!obj.isExtern() && !externClass)
        {
            if (cons.isDefined())
            {
                throw new SourceSemanticException("Class constraint '" + id
                    + "' is already defined", obj);
            }

            processConstraints(obj, cons);

            cons.setDefined(true);
        }
    }

    public void visit(ClassDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // determine base class
        final VeraClass baseClass;
        final String baseID = obj.getBaseClassIdentifer();
        if (baseID != null)
        {
            try
            {
                // look up base class by identifier
                baseClass = lookupClass(baseID, true);

                if (((VeraUserClass) baseClass).isTypedefOnly())
                {
                    throw new SourceSemanticException(
                        "Cannot derived from typedef-only class '" + baseID
                            + "'", obj);
                }
            }
            catch (SourceSemanticException e)
            {
                e.setSourceObject(obj);
                throw e;
            }
        }
        else
        {
            // use the implicit root class as the base class
            baseClass = schema.rootClass;
        }

        // look up class by identifier in current scope
        final String id = obj.getIdentifier();
        final VeraClass cls = lookupClass(id, false);
        final boolean local = obj.isLocal();
        final boolean newClass, likeNewClass;
        if (cls != null)
        {
            currentClass = (VeraUserClass) cls;
            newClass = false;
            likeNewClass = currentClass.isTypedefOnly();

            // skip consistency checks if we've only seen a typedef
            if (!likeNewClass)
            {
                // check for consistent base class with previous declaration
                if (baseClass != currentClass.getBaseClass())
                {
                    throw new SourceSemanticException(
                        "Base class type mismatch", obj);
                }

                // check for consistent modifiers with previous declaration
                if (currentClass.getVisibility() != localToVisibility(local))
                {
                    throw new SourceSemanticException(
                        "Class visibility mismatch", obj);
                }
                if (currentClass.isVirtual() != obj.isVirtual())
                {
                    throw new SourceSemanticException(
                        "Virtual class type mismatch", obj);
                }
            }
        }
        else
        {
            // create new class object
            final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
            currentClass = new VeraUserClass(schema, name, baseClass);
            copyAnnotations(obj, currentClass);
            newClass = true;
            likeNewClass = true;
        }

        // put class in appropriate compilation unit
        trackExternObject(currentClass, compUnit, newClass, local, obj
            .isExtern());

        if (likeNewClass)
        {
            // we have now seen a declaration for this class
            currentClass.setTypedefOnly(false);

            // set base class
            currentClass.setBaseClass(baseClass);

            // set class modifiers
            currentClass.setVisibility(localToVisibility(local));
            currentClass.setVirtual(obj.isVirtual());
        }

        // member declarations need to know whether this is an extern definition
        externClass = obj.isExtern() || isFromHeader(obj);

        // class may not be redeclared after its definition
        if (!externClass && currentClass.isDefined())
        {
            throw new SourceSemanticException("Class '" + id
                + "' is already defined", obj);
        }

        // process base ctor arguments (ignored for extern classes)
        if (baseID != null && !externClass)
        {
            final Iterator argIter = obj.getBaseCtorArgs().iterator();
            while (argIter.hasNext())
            {
                final ExpressionDecl argExprDecl = (ExpressionDecl) argIter
                    .next();
                final VeraExpression argExpr = processExpression(argExprDecl);
                currentClass.addBaseCtorArg(argExpr);
            }
        }

        // class scope becomes current scope
        final VeraScopeDelegate prevScope = currentScope;
        currentScope = new VeraClassScope(currentClass, prevScope);

        // process class members
        visitList(obj.getClassEnums());
        visitList(obj.getClassVars());
        visitList(obj.getClassConstraints());
        visitList(obj.getClassFuncs());

        // generate a constructor if none present
        final Iterator iter = currentClass.lookupObjects("new",
            VeraNameKind.NON_TYPE);
        if (!iter.hasNext())
        {
            // create implicit no-argument constructor
            final VeraName name = new VeraName("new", VeraNameKind.NON_TYPE,
                currentClass);
            final VeraFunctionType funcType = new VeraFunctionType(schema);
            final VeraMemberFunction func = new VeraMemberFunction(name,
                funcType);
            func.setVisibility(VeraVisibility.PUBLIC);
            func.setImplicit(true);
            currentClass.addMember(func);
        }
        else
        {
            assert (iter.next() instanceof VeraMemberFunction);
        }

        // restore previous scope
        currentScope = prevScope;

        if (!externClass)
        {
            currentClass.setDefined(true);
        }

        currentClass = null;
    }

    private static VeraVisibility localToVisibility(boolean local)
    {
        return local ? VeraVisibility.LOCAL : VeraVisibility.PUBLIC;
    }

    public void visit(ClassFuncDecl obj)
    {
        // must be within the context of a class
        assert (currentClass != null);

        // look up method by identifier in current class
        final String id = obj.getIdentifier();
        final Iterator iter = currentClass.lookupObjects(id,
            VeraNameKind.NON_TYPE);
        final VeraMemberFunction func;
        final VeraFunctionType funcType;
        if (iter.hasNext())
        {
            // check that found object is a method
            final NamedObject foundObj = (NamedObject) iter.next();
            if (!(foundObj instanceof VeraMemberFunction))
            {
                throw new SourceSemanticException("Identifier '" + id
                    + "' already used for non-method", obj);
            }
            func = (VeraMemberFunction) foundObj;
            funcType = func.getType();
            newFunc = false;

            // identifier should be unique
            checkMultipleDefinition(iter);

            // check for consistent modifiers with previous declaration
            if (func.getVisibility() != obj.getVisibility())
            {
                throw new SourceSemanticException("Method visibility mismatch",
                    obj);
            }
        }
        else
        {
            // create new method object
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE,
                currentClass);
            funcType = new VeraFunctionType(schema);
            func = new VeraMemberFunction(name, funcType);
            copyAnnotations(obj, func);
            func.setVisibility(obj.getVisibility());
            newFunc = true;

            // add method to class
            currentClass.addMember(func);
        }
        currentFunc = func;

        // process method prototype
        processFuncProto(obj);

        // set method modifiers
        if (newFunc)
        {
            VeraMemberFunction baseFunc = findOverriddenVirtual(func);
            if (baseFunc != null && !baseFunc.getType().equals(func.getType()))
            {
                throw new SourceSemanticException("Virtual method '" + id
                    + "' does not match super class definition", obj);
            }
            if (baseFunc != null || obj.isVirtual())
            {
                func.setVirtual(true);
                func.setPureVirtual(true);
            }
        }
        else if (obj.isVirtual() && !func.isVirtual())
        {
            // NOTE: Vera does not consider it an error to have a virtual
            // mismatch between the extern definition and the body, but
            // silently uses only the virtual-ness of the extern definition.
            throw new SourceSemanticException("Virtual method type mismatch",
                obj);
        }

        // queue method body for second pass processing (if present)
        if (obj.getBlock() != null)
        {
            // remember that method has a body, even if we don't process it
            func.setPureVirtual(false);

            if (!externClass)
            {
                // method may not be redeclared after its definition
                if (func.isDefined())
                {
                    throw new SourceSemanticException("Method '" + id
                        + "' is already defined", obj);
                }

                queueFuncBody(currentFunc, obj, currentScope);

                func.setDefined(true);
            }
        }

        currentFunc = null;
    }

    private void processFuncProto(AbsFuncDecl obj)
    {
        assert (currentFunc != null);
        final VeraFunctionType funcType = currentFunc.getType();

        // process return type
        final VeraType returnType;
        final TypeRef returnTypeRef = obj.getReturnType();
        if (returnTypeRef != null)
        {
            returnType = processType(returnTypeRef);
        }
        else
        {
            returnType = schema.voidType;
        }
        if (newFunc)
        {
            funcType.setReturnType(returnType);
        }
        else if (!returnType.equals(funcType.getReturnType()))
        {
            throw new SourceSemanticException("Return type '" + returnType
                + "' does not match previous definition '"
                + funcType.getReturnType() + "'", obj);
        }

        // process arguments
        final Iterator paramDeclIter = obj.getParams().iterator();
        currentArgPos = 0;
        while (paramDeclIter.hasNext())
        {
            ParamDecl paramDecl = (ParamDecl) paramDeclIter.next();
            paramDecl.accept(this);
        }

        // check argument count
        final List argList = funcType.getArguments();
        final int argCount = argList.size();
        if (!newFunc && currentArgPos != argCount)
        {
            throw new SourceSemanticException("Argument count mismatch; "
                + argCount + " expected", obj);
        }
    }

    private VeraMemberFunction findOverriddenVirtual(VeraMemberFunction func)
    {
        String id = func.getName().getIdentifier();
        VeraClass cls = func.getStructuredType();
        while (true)
        {
            cls = cls.getBaseClass();
            if (cls == null) break;

            Iterator iter = cls.lookupObjects(id, VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof VeraMemberFunction)
                {
                    VeraMemberFunction baseFunc = (VeraMemberFunction) obj;
                    if (baseFunc.isVirtual())
                    {
                        return baseFunc;
                    }
                }
                assert (!iter.hasNext());
                // we can have two situations here:
                // 1: an overridden function was found, but was not virtual;
                //    since this search was done for it, we can stop here
                // 2: another type of object with the same identifier was found;
                //    in this case, Vera stops searching, so we stop too
                return null;
            }
        }
        return null;
    }

    private void queueFuncBody(
        final VeraFunction func,
        final VeraFuncDecl funcDecl,
        final VeraScopeDelegate scope)
    {
        addDeferredProcess(new Runnable()
        {
            public void run()
            {
                currentScope = scope;
                processFuncBody(func, funcDecl);
                currentScope = null;
            }
        });
    }

    void processFuncBody(VeraFunction func, VeraFuncDecl funcDecl)
    {
        final String funcID = func.getName().getIdentifier();
        final BlockDecl block = funcDecl.getBlock();

        // non-local functions may not have multiple body definitions;
        //
        if (func.getBody() != null)
        {
            if (func.getVisibility() != VeraVisibility.LOCAL)
            {
                throw new SourceSemanticException("Non-local function '"
                    + funcID + "' has multiple body definitions", block);
            }
            else
            {
                System.err.println("Warning: Ignoring redefinition of local "
                    + "function '" + funcID + "'");
                return;
            }
        }

        // function argument scope becomes current scope
        final FunctionType funcType = func.getType();
        final VeraScopeDelegate prevScope = currentScope;
        currentScope = new VeraSimpleScope(funcType, prevScope);

        // create return variable for function
        final VeraType returnType = (VeraType) funcType.getReturnType();
        if (!(returnType instanceof VeraVoidType))
        {
            VeraName returnVarName = new VeraName(funcID,
                VeraNameKind.NON_TYPE, null);
            VeraLocalVariable returnVar = new VeraLocalVariable(returnVarName,
                returnType);
            func.setReturnVar(returnVar);
        }

        // process the block statement
        currentFunc = func;
        if (func instanceof VeraMemberFunction)
        {
            currentClass = (VeraUserClass) ((VeraMemberFunction) func)
                .getStructuredType();
        }
        func.setBody((VeraBlock) processStatement(block));
        currentFunc = null;
        currentClass = null;

        // restore previous scope
        currentScope = prevScope;
    }

    private void checkDuplicateName(
        Scope scope,
        NamedObject obj,
        SourceObject srcObj)
    {
        final Name name = obj.getName();
        final String id = name.getIdentifier();
        final Iterator iter = scope.lookupObjects(id, VeraNameKind.NON_TYPE);
        if (iter.hasNext())
        {
            throw new SourceSemanticException("Duplicate identifier: " + id,
                srcObj);
        }
    }

    public void visit(ClassVarDecl obj)
    {
        // must be within the context of a class
        assert (currentClass != null);

        // process field type
        final VeraType type = processType(obj.getTypeRef());

        // look up field by identifier in current class
        final String id = obj.getIdentifier();
        final Iterator iter = currentClass.lookupObjects(id,
            VeraNameKind.NON_TYPE);
        final VeraMemberVariable var;
        if (iter.hasNext())
        {
            // check that found object is a field
            final NamedObject foundObj = (NamedObject) iter.next();
            if (!(foundObj instanceof VeraMemberVariable))
            {
                throw new SourceSemanticException("Identifier '" + id
                    + "' already used for non-field", obj);
            }
            var = (VeraMemberVariable) foundObj;

            // identifier should be unique
            checkMultipleDefinition(iter);

            // check for consistent modifiers with previous declaration
            if (!var.getType().equals(type))
            {
                throw new SourceSemanticException("Field type mismatch", obj);
            }
            if (var.getVisibility() != obj.getVisibility())
            {
                throw new SourceSemanticException("Field visibility mismatch",
                    obj);
            }
            if (var.hasModifier(VeraVariableModifier.STATIC) != obj
                .isStaticVar())
            {
                throw new SourceSemanticException("Field static type mismatch",
                    obj);
            }
            if (var.hasModifier(VeraVariableModifier.RAND) != (obj
                .getRandMode() == RandMode.RAND)
                || var.hasModifier(VeraVariableModifier.RANDC) != (obj
                    .getRandMode() == RandMode.RANDC))
            {
                throw new SourceSemanticException(
                    "Field randomization type mismatch", obj);
            }
            if (var.hasModifier(VeraVariableModifier.PACKED) != obj.isPacked())
            {
                throw new SourceSemanticException(
                    "Field packing type mismatch", obj);
            }
            if (var.hasModifier(VeraVariableModifier.BIG_ENDIAN) != obj
                .isBigEndian())
            {
                throw new SourceSemanticException("Field endian type mismatch",
                    obj);
            }
            if (var.hasModifier(VeraVariableModifier.BIT_REVERSE) != obj
                .isBitReverse())
            {
                throw new SourceSemanticException("Field bit order mismatch",
                    obj);
            }
        }
        else
        {
            // create new field object
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE,
                currentClass);
            var = new VeraMemberVariable(name, type);
            copyAnnotations(obj, var);

            // add field to class
            currentClass.addMember(var);

            // set field modifiers
            var.setVisibility(obj.getVisibility());
            if (obj.isStaticVar())
            {
                var.addModifier(VeraVariableModifier.STATIC);
            }
            if (obj.getRandMode() == RandMode.RAND)
            {
                var.addModifier(VeraVariableModifier.RAND);
            }
            else if (obj.getRandMode() == RandMode.RANDC)
            {
                var.addModifier(VeraVariableModifier.RANDC);
            }
            if (obj.isPacked())
            {
                var.addModifier(VeraVariableModifier.PACKED);
            }
            if (obj.isBigEndian())
            {
                var.addModifier(VeraVariableModifier.BIG_ENDIAN);
            }
            if (obj.isBitReverse())
            {
                var.addModifier(VeraVariableModifier.BIT_REVERSE);
            }
        }

        // random array size and initializer expression are ignored for extern
        // classes
        if (!externClass)
        {
            // field may not be redeclared after its definition
            if (var.isDefined())
            {
                throw new SourceSemanticException("Field '" + id
                    + "' is already defined", obj);
            }

            // process random array size expression
            final ExpressionDecl sizeExprDecl = obj.getRandomSizeExpr();
            if (sizeExprDecl != null)
            {
                final VeraExpression sizeExpr = processExpression(sizeExprDecl);
                if (!sizeExpr.getResultType().isIntegralConvertible())
                {
                    throw new SourceSemanticException(
                        "Random array size expression must be integral",
                        sizeExprDecl);
                }
                var.setRandomSize(sizeExpr);
            }

            // process initializer expression
            final ExpressionDecl initExprDecl = obj.getInitExpr();
            if (initExprDecl != null)
            {
                lhsType = var.getType();

                final VeraExpression initExpr = processExpression(initExprDecl);
                if (!var.getType().isAssignableFrom(initExpr.getResultType()))
                {
                    throw new SourceSemanticException(
                        "Initializer type is not assignable to field type",
                        initExprDecl);
                }
                var.setInitializer(initExpr);

                lhsType = null;
            }

            var.setDefined(true);
        }
    }

    public void visit(CompilationUnitDecl obj)
    {
        deferredProcesses.clear();
        macroArgMap.clear();

        compUnitScope = new VeraCompilationUnitScope(globalScope);
        currentScope = compUnitScope;
        primaryCompUnit = getCompUnit(obj.getPath(), false);
        if (wantShell)
        {
            shellMembers = new LinkedHashSet<VeraCompilationUnitMember>();
        }

        currentClass = null;
        currentType = null;
        lhsType = null;
        currentEnum = null;
        currentFunc = null;
        currentVar = null;
        currentStmt = null;
        currentExpr = null;
        currentIntf = null;
        currentIntfSignal = null;
        currentBind = null;
        currentBindMember = null;
        currentProgram = null;

        visitList(obj.getMembers());

        // function/program bodies and defines must be processed last, since
        // they may refer to variables and other functions declared later
        runDeferredProcesses();

        // if program block was found, associate Verilog shell members with it
        if (wantShell && currentProgram != null)
        {
            currentProgram.getShellMembers().addAll(shellMembers);
        }
        shellMembers = null;

        primaryCompUnit.setComplete(true);
    }

    public void visit(ConstraintDecl obj)
    {
        final VeraConstraintSet cons = new VeraConstraintSet(schema);
        copyAnnotations(obj, cons);
        processConstraints(obj, cons);
        checkDefineRef(obj, cons);
        currentExpr = cons;
    }

    private void processConstraints(ConstraintDecl obj, VeraConstraintSet cons)
    {
        final Iterator exprIter = obj.getConstraintExprs().iterator();
        while (exprIter.hasNext())
        {
            ExpressionDecl exprDecl = (ExpressionDecl) exprIter.next();
            VeraExpression expr = processExpression(exprDecl);
            cons.addExpr(expr);
        }
    }

    public void visit(ContinueDecl obj)
    {
        currentStmt = new VeraContinueStatement(schema);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(DefaultValueDecl obj)
    {
        // visit(FuncCallDecl) handles DefaultValueDecl
        assert false;
    }

    public void visit(DefineDecl obj)
    {
        final Macro macro = obj.getMacro();

        // compilation unit should not contain built-in macros
        assert (macro instanceof VeraPPUserMacro);

        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // function macro argument pasting or stringification not supported
        if (macro instanceof VeraPPFunctionMacro)
        {
            VeraPPFunctionMacro funcMacro = (VeraPPFunctionMacro) macro;
            if (funcMacro.containsArgumentPasting()
                || funcMacro.containsStringification()) return;
        }

        // search for a macro with the given identifier
        final String id = macro.getName();
        final Iterator iter = compUnitScope.lookupObjects(id,
            VeraNameKind.PREPROC);

        // ignore redefinitions
        if (iter.hasNext()) return;

        // attempt to parse macro
        MacroDecl macroDecl = preprocInfo.parseMacro(macro);
        if (macroDecl != null)
        {
            //System.out.println("Parsed " + id + ": " + macroDecl);

            VeraName name = new VeraName(id, VeraNameKind.PREPROC, null);

            final VeraScopeDelegate macroScope;
            final List<VeraDefineArgument> args;
            if (macro instanceof FunctionMacro)
            {
                // begin new scope for define arguments
                VeraAnonymousScope anonScope = new VeraAnonymousScope(
                    compUnitScope);
                macroScope = anonScope;

                // create schema objects for define arguments
                FunctionMacro funcMacro = (FunctionMacro) macro;
                List argIDs = funcMacro.getArgumentNames();
                args = new LinkedList<VeraDefineArgument>();
                Iterator argIter = argIDs.iterator();
                while (argIter.hasNext())
                {
                    String argID = (String) argIter.next();
                    VeraName argName = new VeraName(argID,
                        VeraNameKind.NON_TYPE, null);
                    VeraDefineArgument var = new VeraDefineArgument(schema,
                        argName);
                    anonScope.addObject(var);
                    args.add(var);
                }
            }
            else
            {
                macroScope = compUnitScope;
                args = null;
            }

            VeraDefine define = null;
            if (macroDecl instanceof StatementDecl)
            {
                final StatementDecl stmtDecl = (StatementDecl) macroDecl;
                final VeraStatementDefine stmtDefine = new VeraStatementDefine(
                    schema, name);
                addDeferredProcess(new Runnable()
                {
                    public void run()
                    {
                        currentScope = macroScope;
                        try
                        {
                            VeraStatement stmt = processStatement(stmtDecl);
                            stmtDefine.setStatement(stmt);
                        }
                        catch (RuntimeException e)
                        {
                            compUnit.removeMember(stmtDefine);
                        }
                        currentScope = null;
                    }
                });
                define = stmtDefine;
            }
            else if (macroDecl instanceof ExpressionDecl)
            {
                final ExpressionDecl exprDecl = (ExpressionDecl) macroDecl;
                final VeraExpressionDefine exprDefine = new VeraExpressionDefine(
                    schema, name);
                addDeferredProcess(new Runnable()
                {
                    public void run()
                    {
                        currentScope = macroScope;
                        try
                        {
                            VeraExpression expr = processExpression(exprDecl);
                            exprDefine.setExpression(expr);
                        }
                        catch (RuntimeException e)
                        {
                            compUnit.removeMember(exprDefine);
                        }
                        currentScope = null;
                    }
                });
                define = exprDefine;
            }
            else if (macroDecl instanceof RangeDecl)
            {
                final RangeDecl r = (RangeDecl) macroDecl;
                final VeraRangeDefine rangeDefine = new VeraRangeDefine(schema,
                    name);
                addDeferredProcess(new Runnable()
                {
                    public void run()
                    {
                        currentScope = macroScope;
                        try
                        {
                            VeraRange range = processRange(r);
                            rangeDefine.setRange(range);
                        }
                        catch (RuntimeException e)
                        {
                            compUnit.removeMember(rangeDefine);
                        }
                        currentScope = null;
                    }
                });
                define = rangeDefine;
            }
            else if (macroDecl instanceof TypeRef)
            {
                final TypeRef typeRef = (TypeRef) macroDecl;
                final VeraTypeDefine typeDefine = new VeraTypeDefine(schema,
                    name);
                addDeferredProcess(new Runnable()
                {
                    public void run()
                    {
                        currentScope = macroScope;
                        try
                        {
                            VeraType type = processType(typeRef);
                            typeDefine.setType(type);
                        }
                        catch (RuntimeException e)
                        {
                            compUnit.removeMember(typeDefine);
                        }
                        currentScope = null;
                    }
                });
                define = typeDefine;
            }

            if (define != null)
            {
                if (obj.isLocal())
                {
                    define.setVisibility(VeraVisibility.LOCAL);
                }
                if (obj.isVerilogImport())
                {
                    define.setVerilogImport(true);
                }
                if (args != null)
                {
                    define.addArguments(args);
                }
                copyAnnotations(obj, define);
                compUnit.addMember(define);
            }

            // restore previous scope
            currentScope = macroScope;
        }
    }

    public void visit(DepthAccessDecl obj)
    {
        final ExpressionDecl signalExprDecl = obj.getSignalExpr();
        final VeraExpression signalExpr = processExpression(signalExprDecl);
        currentExpr = new VeraDepthAccess(signalExpr, obj.getDepth());
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(DriveSampleDecl obj)
    {
        // process assignment destination expression
        final ExpressionDecl destExprDecl = obj.getDestExpr();
        final VeraExpression destExpr = processExpression(destExprDecl);
        final Type destType = destExpr.getResultType();

        // LHS for non-blocking drive must be port/interface member
        final boolean nb = obj.getOperator() == Operator.NONBLOCKING_ASSIGN;
        if (nb)
        {
            boolean valid = false;
            if (destExpr instanceof VeraSignalReference)
            {
                valid = true;
            }
            else if (destExpr instanceof VeraMemberAccess)
            {
                VeraMemberAccess memberExpr = (VeraMemberAccess) destExpr;
                Expression objectExpr = memberExpr.getObject();
                Type objectType = objectExpr.getResultType();
                if (objectType instanceof VeraPortType)
                {
                    valid = true;
                }
            }
            if (!valid)
            {
                throw new SourceSemanticException(
                    "Port/interface signal reference expected for drive", obj);
            }
        }

        final ExpressionDecl valueExprDecl = obj.getValueExpr();
        final VeraExpression valueExpr = processExpression(valueExprDecl);
        final Type valueType = valueExpr.getResultType();
        if (!destType.isAssignableFrom(valueType)
            && !(valueType instanceof VeraVoidType))
        {
            throw new SourceSemanticException("Value [" + valueType
                + "] is not assignable to signal [" + destType + "]", obj);
        }

        final VeraDriveSampleStatement driveStmt = new VeraDriveSampleStatement(
            destExpr, valueExpr);
        copyAnnotations(obj, driveStmt);

        final ExpressionDecl delayExprDecl = obj.getDelayExpr();
        if (delayExprDecl != null)
        {
            final VeraExpression delayExpr = processExpression(delayExprDecl);
            driveStmt.setDelay(delayExpr);
        }

        driveStmt.setNonBlocking(nb);
        driveStmt.setSoft(obj.isSoft());
        driveStmt.setAsync(obj.isAsync());

        checkDefineRef(obj, driveStmt);
        currentStmt = driveStmt;
    }

    public void visit(EnumDecl obj)
    {
        final String id = obj.getIdentifier();
        if (currentClass == null)
        {
            // determine compilation unit
            final VeraCompilationUnit compUnit = getCompUnit(obj);

            // look up enumeration by identifier in current scope
            currentEnum = lookupEnumeration(id, false);
            if (currentEnum != null)
            {
                // found previously defined enumeration
                newEnum = false;
            }
            else
            {
                // create new enumeration object
                final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
                currentEnum = new VeraEnumeration(schema, name);
                copyAnnotations(obj, currentEnum);
                newEnum = true;

                // add enumeration to compilation unit
                compUnit.addMember(currentEnum);
            }
        }
        else
        {
            final Iterator iter = currentClass.lookupObjects(id,
                VeraNameKind.TYPE);
            if (iter.hasNext())
            {
                // check that found object is an enumeration
                final NamedObject foundObj = (NamedObject) iter.next();
                if (!(foundObj instanceof VeraEnumeration))
                {
                    throw new SourceSemanticException("Identifier '" + id
                        + "' already used for non-enumeration", obj);
                }
                currentEnum = (VeraEnumeration) foundObj;
                newEnum = false;

                // identifier should be unique
                checkMultipleDefinition(iter);
            }
            else
            {
                // create new enumeration object
                final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
                currentEnum = new VeraEnumeration(schema, name);
                copyAnnotations(obj, currentEnum);
                newEnum = true;

                // add enumeration to class
                currentClass.addMember(currentEnum);
            }
        }

        nextEnumValue = 0;
        visitList(obj.getElements());

        currentEnum = null;
    }

    public void visit(EnumElementDecl obj)
    {
        // determine value of next element
        int value;
        final ExpressionDecl valueDecl = obj.getValue();
        if (valueDecl != null)
        {
            // use specified value
            value = evalIntExpr(valueDecl);
        }
        else
        {
            // use default/consecutive value
            value = nextEnumValue;
        }

        final String id = obj.getIdentifier();
        final ExpressionDecl firstSuffixDecl = obj.getFirstSuffix();
        if (firstSuffixDecl != null)
        {
            // determine suffix range for range enumeration
            final int firstSuffix, lastSuffix;
            final int n = evalIntExpr(firstSuffixDecl);
            final ExpressionDecl lastSuffixDecl = obj.getLastSuffix();
            if (lastSuffixDecl != null)
            {
                int m = evalIntExpr(lastSuffixDecl);
                if (n <= m)
                {
                    firstSuffix = n;
                    lastSuffix = m;
                }
                else
                {
                    firstSuffix = m;
                    lastSuffix = n;
                }
            }
            else
            {
                firstSuffix = 0;
                lastSuffix = n - 1;
            }
            if (firstSuffix < 0 || lastSuffix < 0)
            {
                throw new SourceSemanticException(
                    "Invalid enumeration range value", obj);
            }

            // define range enumeration
            for (int i = firstSuffix; i <= lastSuffix; ++i)
            {
                addEnumElement(id + i, value++, obj);
            }
        }
        else
        {
            // define single enumeration
            addEnumElement(id, value++, obj);
        }

        // store default value for next element
        nextEnumValue = value;
    }

    private void addEnumElement(String id, int value, EnumElementDecl obj)
    {
        if (newEnum)
        {
            // check that enumeration element values are unique
            if (currentEnum.lookupValue(value) != null)
            {
                throw new SourceSemanticException(
                    "Duplicate enumeration element value: " + value, obj);
            }

            // create new enumeration element object
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE,
                currentEnum);
            final VeraEnumerationElement element = new VeraEnumerationElement(
                currentEnum, name, value);
            copyAnnotations(obj, element);

            // add element to enumeration and current scope
            currentEnum.addMember(element);
            final VeraCompilationUnit compUnit = currentEnum
                .getCompilationUnit();
            if (compUnit != null)
            {
                compUnit.addMember(element);
            }
            else
            {
                final VeraUserClass cls = (VeraUserClass) currentEnum
                    .getStructuredType();
                assert (cls != null);
                cls.addMember(element);
            }
        }
        else
        {
            // look up element identifier in enumeration
            final Iterator iter = currentEnum.lookupObjects(id,
                VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                final VeraEnumerationElement element = (VeraEnumerationElement) iter
                    .next();

                // check that element value is consistent
                if (element.getValue() != value)
                {
                    throw new SourceSemanticException(
                        "Enumeration element value (" + value
                            + ") does not match previous definition ("
                            + element.getValue() + ")", obj);
                }

                // identifier should be unique
                checkMultipleDefinition(iter);
            }
            else
            {
                throw new SourceSemanticException("Enumeration element '" + id
                    + "' not found in previous definition", obj);
            }
        }
    }

    public void visit(ExpectDecl obj)
    {
        final VeraExpectStatement stmt = new VeraExpectStatement(schema, obj
            .getExpectKind());
        copyAnnotations(obj, stmt);

        // process delay cycles, if present
        final ExpressionDecl delayDecl = obj.getDelayExpr();
        if (delayDecl != null)
        {
            stmt.setDelay(processExpression(delayDecl));
        }

        // process window cycles, if present
        final ExpressionDecl windowDecl = obj.getWindowExpr();
        if (windowDecl != null)
        {
            stmt.setWindow(processExpression(windowDecl));
        }

        // process terms
        final Iterator iter = obj.getExpectExprs().iterator();
        while (iter.hasNext())
        {
            final ExpectExprDecl exprDecl = (ExpectExprDecl) iter.next();
            final VeraExpression signalExpr = processExpression(exprDecl
                .getSignalExpr());
            final VeraExpression valueExpr = processExpression(exprDecl
                .getValueExpr());
            stmt.addExpectTerm(new VeraExpectTerm(signalExpr, valueExpr,
                exprDecl.getOperator() == Operator.EQUAL));
        }

        // set options
        stmt.setExpectAll(obj.isExpectAll());
        stmt.setSoft(obj.isSoft());
        stmt.setAsync(obj.isAsync());

        checkDefineRef(obj, stmt);
        currentStmt = stmt;
    }

    public void visit(ExpectExprDecl obj)
    {
        // TODO: ExpectExprDecl
        throw new UnsupportedOperationException();
    }

    public void visit(ExpressionStatementDecl obj)
    {
        final VeraExpression expr = processExpression(obj.getExpr());
        currentStmt = new VeraExpressionStatement(expr);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(ExtConstraintDecl obj)
    {
        // look up class by identifier
        final VeraClass cls;
        final String classID = obj.getClassIdentifier();
        try
        {
            cls = lookupClass(classID, true);
        }
        catch (SourceSemanticException e)
        {
            e.setSourceObject(obj);
            throw e;
        }

        // look up constraint by identifier in class
        final VeraClassConstraint cons;
        final String id = obj.getIdentifier();
        final Iterator iter = cls.lookupObjects(id, VeraNameKind.NON_TYPE);
        if (iter.hasNext())
        {
            // check that found object is a method
            final NamedObject foundObj = (NamedObject) iter.next();
            if (!(foundObj instanceof VeraClassConstraint))
            {
                throw new SourceSemanticException("Identifier '" + id
                    + "' does not represent a constraint", obj);
            }
            cons = (VeraClassConstraint) foundObj;

            // identifier should be unique
            checkMultipleDefinition(iter);
        }
        else
        {
            throw new SourceSemanticException("Unknown constraint: " + id, obj);
        }

        // check that constraint body is not already defined
        if (cons.isDefined())
        {
            throw new SourceSemanticException("Constraint '" + id
                + "' is already defined", obj);
        }

        copyAnnotations(obj, cons);

        // class scope becomes current scope
        final VeraScopeDelegate prevScope = currentScope;
        currentScope = new VeraClassScope(cls, currentScope);

        // process expressions in constraint body
        final Iterator exprIter = obj.getConstraintExprs().iterator();
        while (exprIter.hasNext())
        {
            final ExpressionDecl exprDecl = (ExpressionDecl) exprIter.next();
            final VeraExpression expr = processExpression(exprDecl);
            cons.addExpr(expr);
        }
        cons.setDefined(true);

        // restore previous scope
        currentScope = prevScope;
    }

    public void visit(ExternVarDecl obj)
    {
        // TODO: ExternVarDecl
        throw new UnsupportedOperationException();
    }

    public void visit(FixedArrayTypeRef obj)
    {
        // process the element type
        final VeraType elementType = processType(obj.getElementTypeRef());

        // evaluate dimension expressions
        final List dimExprDecls = obj.getDimensions();
        final int[] dimensions = new int[dimExprDecls.size()];
        final Iterator iter = dimExprDecls.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            ExpressionDecl dimExprDecl = (ExpressionDecl) iter.next();
            int dim = evalIntExpr(dimExprDecl);
            if (dim <= 0)
            {
                throw new SourceSemanticException("Invalid array dimension",
                    dimExprDecl);
            }
            dimensions[i++] = dim;
        }

        final VeraFixedArrayType arrayType = new VeraFixedArrayType(
            elementType, dimensions);
        checkDefineRef(obj, arrayType);
        currentType = arrayType;
    }

    public void visit(ForDecl obj)
    {
        final VeraForStatement forStmt = new VeraForStatement(schema);
        copyAnnotations(obj, forStmt);

        Iterator iter = obj.getInitExprs().iterator();
        while (iter.hasNext())
        {
            final ExpressionDecl exprDecl = (ExpressionDecl) iter.next();
            final VeraExpression expr = processExpression(exprDecl);
            final VeraExpressionStatement stmt = new VeraExpressionStatement(
                expr);
            forStmt.addInitStatement(stmt);
        }

        final ExpressionDecl condExprDecl = obj.getCondition();
        if (condExprDecl != null)
        {
            final VeraExpression condExpr = processExpression(condExprDecl);
            if (!condExpr.getResultType().isIntegralConvertible())
            {
                throw new SourceSemanticException(
                    "Integral expression expected in 'for' condition",
                    condExprDecl);
            }
            forStmt.setCondition(condExpr);
        }

        iter = obj.getUpdateExprs().iterator();
        while (iter.hasNext())
        {
            final ExpressionDecl exprDecl = (ExpressionDecl) iter.next();
            final VeraExpression expr = processExpression(exprDecl);
            final VeraExpressionStatement stmt = new VeraExpressionStatement(
                expr);
            forStmt.addUpdateStatement(stmt);
        }

        final VeraStatement stmt = processStatement(obj.getStatement());
        forStmt.setStatement(stmt);

        checkDefineRef(obj, forStmt);
        currentStmt = forStmt;
    }

    public void visit(ForkJoinDecl obj)
    {
        final VeraForkStatement forkStmt = new VeraForkStatement(schema, obj
            .getJoinKind());
        copyAnnotations(obj, forkStmt);

        final Iterator iter = obj.getStatements().iterator();
        while (iter.hasNext())
        {
            final StatementDecl stmtDecl = (StatementDecl) iter.next();
            forkStmt.addForkedStatements(processStatement(stmtDecl));
        }

        checkDefineRef(obj, forkStmt);
        currentStmt = forkStmt;
    }

    public void visit(FuncCallDecl obj)
    {
        // process function expression
        final ExpressionDecl funcExprDecl = obj.getFunction();
        final VeraExpression funcExpr = processExpression(funcExprDecl);
        final VeraFunction func;
        if (funcExpr instanceof VeraFunctionReference)
        {
            VeraFunctionReference funcRef = (VeraFunctionReference) funcExpr;
            func = funcRef.getFunction();
        }
        else if (funcExpr instanceof VeraMemberAccess)
        {
            VeraMemberAccess memberRef = (VeraMemberAccess) funcExpr;
            VeraStructuredTypeMember member = memberRef.getMember();
            if (!(member instanceof VeraMemberFunction))
            {
                throw new SourceSemanticException(
                    "Function reference expected", funcExprDecl);
            }
            func = (VeraFunction) member;
        }
        else
        {
            throw new SourceSemanticException("Function reference expected",
                funcExprDecl);
        }

        // expand arguments and create function invocation expression
        final VeraFunctionInvocation funcCall = new VeraFunctionInvocation(
            funcExpr);
        copyAnnotations(obj, funcCall);
        final FunctionType funcType = func.getType();
        final List argExprs = expandArgs(funcType.getArguments(), funcType
            .isVarArgs(), obj.getArguments(), obj);
        final Iterator iter = argExprs.iterator();
        while (iter.hasNext())
        {
            funcCall.addArgument((VeraExpression) iter.next());
        }

        // process randomize-with constraints
        // FIXME: randomize-with constraints should be evaluated with the
        // object's class brought into scope at the innermost nesting level
        final ConstraintDecl cons = obj.getConstraints();
        if (cons != null)
        {
            if (!isRandomizeRef(func))
            {
                throw new SourceSemanticException(
                    "'with' constraints only allowed for randomize() call",
                    cons);
            }

            final Iterator exprIter = cons.getConstraintExprs().iterator();
            while (exprIter.hasNext())
            {
                final ExpressionDecl exprDecl = (ExpressionDecl) exprIter
                    .next();
                final VeraExpression expr = processExpression(exprDecl);
                funcCall.addConstraint(expr);
            }
        }

        checkDefineRef(obj, funcCall);
        currentExpr = funcCall;
    }

    private List<VeraExpression> expandArgs(
        List formalArgs,
        boolean isVarArgs,
        List callArgs,
        SourceObject obj)
    {
        // get default values for arguments and where to use them
        final int formalArgCount = formalArgs.size();
        final boolean[] useDefault = new boolean[formalArgCount];
        final int callArgCount = callArgs.size();
        getDefaultArgs(formalArgs, isVarArgs, callArgCount, useDefault, obj);

        // process arguments
        List<VeraExpression> result = new ArrayList<VeraExpression>(Math.max(
            formalArgCount, callArgCount));
        int argIndex = 0;
        final Iterator formalArgIter = formalArgs.iterator();
        final Iterator callArgIter = callArgs.iterator();
        while (formalArgIter.hasNext() || callArgIter.hasNext())
        {
            VeraFunctionArgument formalArg = null;
            VeraExpression defaultExpr = null;
            if (formalArgIter.hasNext())
            {
                formalArg = (VeraFunctionArgument) formalArgIter.next();
                defaultExpr = formalArg.getInitializer();
            }

            if (defaultExpr != null && useDefault[argIndex])
            {
                // argument not specified; use default value
                result.add(defaultExpr);
            }
            else if (callArgIter.hasNext())
            {
                final ExpressionDecl argExprDecl = (ExpressionDecl) callArgIter
                    .next();
                final VeraExpression argExpr;
                if (argExprDecl instanceof DefaultValueDecl)
                {
                    // default value placeholder; use default value
                    if (defaultExpr == null)
                    {
                        throw new SourceSemanticException(
                            "Argument does not have a default value",
                            argExprDecl);
                    }
                    argExpr = defaultExpr;
                }
                else
                {
                    // use given argument expression
                    argExpr = processExpression(argExprDecl);

                    // check argument type
                    if (formalArg != null)
                    {
                        Type argType = formalArg.getType();
                        Type exprType = argExpr.getResultType();
                        if (argType != schema.magicType
                            && !argType.isAssignableFrom(exprType))
                        {
                            throw new SourceSemanticException("'" + exprType
                                + "' expression is not assignable to '"
                                + argType + "' argument", argExprDecl);
                        }
                    }
                }
                result.add(argExpr);
            }
            else
            {
                // argument count should have been checked by getDefaultArgs()
                assert (formalArg.isOptional());

                // stop adding arguments at first optional, no-default argument
                break;
            }
            ++argIndex;
        }
        return result;
    }

    private void getDefaultArgs(
        List formalArgs,
        boolean isVarArgs,
        int actualArgCount,
        boolean[] useDefault,
        SourceObject obj)
    {
        final int formalArgCount = formalArgs.size();

        // determine last level to fill and how many arguments it gets
        final int lastFillLevel;
        int lastFillCount;
        if (actualArgCount == 0)
        {
            // fill no arguments
            lastFillLevel = Integer.MIN_VALUE;
            lastFillCount = 0;
        }
        else if (actualArgCount < formalArgCount)
        {
            // build array of optional argument levels
            final int[] argLevels = new int[formalArgCount];
            int argIndex = 0;
            final Iterator iter = formalArgs.iterator();
            while (iter.hasNext())
            {
                VeraFunctionArgument arg = (VeraFunctionArgument) iter.next();
                argLevels[argIndex++] = arg.getOptionalLevel();
            }

            // sort argument levels
            Arrays.sort(argLevels);

            // last level to fill is level at last actual argument position
            int searchPos = actualArgCount - 1;
            lastFillLevel = argLevels[searchPos];

            // get count of arguments to fill at last level
            lastFillCount = 1;
            while (searchPos > 0 && argLevels[searchPos - 1] == lastFillLevel)
            {
                ++lastFillCount;
                --searchPos;
            }
        }
        else
        {
            if (actualArgCount > formalArgCount && !isVarArgs)
            {
                throw new SourceSemanticException(
                    "Too many arguments for function", obj);
            }

            // fill all arguments
            lastFillLevel = Integer.MAX_VALUE;
            lastFillCount = Integer.MAX_VALUE;
        }

        // fill output arrays
        int argIndex = 0;
        final Iterator iter = formalArgs.iterator();
        while (iter.hasNext())
        {
            final VeraFunctionArgument arg = (VeraFunctionArgument) iter.next();
            final int level = arg.getOptionalLevel();
            if (level < lastFillLevel)
            {
                // use actual argument
                useDefault[argIndex] = false;
            }
            else if (level == lastFillLevel && lastFillCount > 0)
            {
                // use actual argument and decrement last fill count
                useDefault[argIndex] = false;
                --lastFillCount;
            }
            else
            {
                // use default argument
                if (!arg.isOptional())
                {
                    throw new SourceSemanticException(
                        "Value not specified for required argument "
                            + arg.getName(), obj);
                }
                useDefault[argIndex] = true;
            }
            ++argIndex;
        }
    }

    private boolean isRandomizeRef(VeraFunction func)
    {
        String funcName = func.getName().getCanonicalName();
        return funcName.equals("<root>::randomize");
    }

    private interface GlobalFuncFactory
    {
        VeraGlobalFunction create(VeraName name, VeraFunctionType funcType);
    }

    private VeraFunction processGlobalFunc(
        AbsFuncDecl obj,
        GlobalFuncFactory factory)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // get class identifier and local modifier (if present)
        final String classID;
        final boolean local;
        if (obj instanceof FuncDecl)
        {
            final FuncDecl funcObj = (FuncDecl) obj;
            classID = funcObj.getClassIdentifier();
            local = funcObj.isLocal();
        }
        else
        {
            classID = null;
            local = false;
        }

        // look up or create function
        final String id = obj.getIdentifier();
        final VeraScopeDelegate funcScope;
        if (classID != null)
        {
            if (local)
            {
                throw new SourceSemanticException(
                    "Class functions cannot be local", obj);
            }

            // look up class by identifier
            final VeraClass cls;
            try
            {
                cls = lookupClass(classID, true);
            }
            catch (SourceSemanticException e)
            {
                e.setSourceObject(obj);
                throw e;
            }
            currentClass = (VeraUserClass) cls;

            // look up method by identifier in current class
            final Iterator iter = cls.lookupObjects(id, VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                // check that found object is a method
                final NamedObject foundObj = (NamedObject) iter.next();
                if (!(foundObj instanceof VeraMemberFunction))
                {
                    throw new SourceSemanticException("Identifier '" + id
                        + "' does not represent a method", obj);
                }
                final VeraMemberFunction func = (VeraMemberFunction) foundObj;
                newFunc = false;

                // remember whether method has a body
                if (((VeraFuncDecl) obj).getBlock() != null)
                {
                    func.setPureVirtual(false);
                }

                // identifier should be unique
                checkMultipleDefinition(iter);

                currentFunc = func;
                funcScope = new VeraClassScope(cls, currentScope);
            }
            else
            {
                throw new SourceSemanticException("Unknown method: " + id, obj);
            }
        }
        else
        {
            VeraGlobalFunction func;
            try
            {
                func = lookupFunction(id, false);
            }
            catch (SourceSemanticException e)
            {
                e.setSourceObject(obj);
                throw e;
            }
            if (func != null)
            {
                // found function with existing prototype
                newFunc = false;
            }
            else
            {
                // create new function object
                final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE,
                    null);
                final VeraFunctionType funcType = new VeraFunctionType(schema);
                func = factory.create(name, funcType);
                copyAnnotations(obj, func);
                newFunc = true;

                func.setVisibility(localToVisibility(local));
                func.setExport(obj.isExport());
            }

            // put function in appropriate compilation unit
            trackExternObject(func, compUnit, newFunc, local, obj.isExtern());

            currentFunc = func;
            funcScope = currentScope;
        }
        VeraFunction resultFunc = currentFunc;

        // process function prototype
        processFuncProto(obj);

        // queue function body for second pass processing (if present)
        if (obj instanceof VeraFuncDecl && !isFromHeader(obj))
        {
            final VeraFuncDecl veraObj = (VeraFuncDecl) obj;
            final BlockDecl block = veraObj.getBlock();
            if (block != null)
            {
                queueFuncBody(currentFunc, veraObj, funcScope);
            }
        }

        currentFunc = null;
        currentClass = null;

        return resultFunc;
    }

    public void visit(final FuncDecl obj)
    {
        class FunctionFactory
            implements GlobalFuncFactory
        {
            public VeraGlobalFunction create(
                VeraName name,
                VeraFunctionType funcType)
            {
                return new VeraGlobalFunction(name, funcType);
            }
        }
        VeraFunction func = processGlobalFunc(obj, new FunctionFactory());

        if (func instanceof VeraGlobalFunction)
        {
            VeraGlobalFunction globalFunc = (VeraGlobalFunction) func;
            if (globalFunc.isExport())
            {
                // add exported function to Verilog shell
                if (shellMembers != null) shellMembers.add(globalFunc);
            }
        }
    }

    public void visit(GlobalVarDecl obj)
    {
        // handled in visit(ProgramDecl)
        assert false;
    }

    private VeraExpression processInitExpr(
        ExpressionDecl initExprDecl,
        VeraType type)
    {
        lhsType = type;

        final VeraExpression initExpr = processExpression(initExprDecl);
        if (!type.isAssignableFrom(initExpr.getResultType()))
        {
            throw new SourceSemanticException(
                "Initializer type is not assignable to variable type",
                initExprDecl);
        }

        lhsType = null;

        return initExpr;
    }

    public void visit(final HDLTaskDecl obj)
    {
        class FunctionFactory
            implements GlobalFuncFactory
        {
            public VeraGlobalFunction create(
                VeraName name,
                VeraFunctionType funcType)
            {
                final VeraHDLFunction func = new VeraHDLFunction(name, funcType);
                return func;
            }
        }
        VeraHDLFunction func = (VeraHDLFunction) processGlobalFunc(obj,
            new FunctionFactory());

        // set instance path if present
        if (!obj.isExtern())
        {
            String objInstPath = obj.getInstPath();
            String curInstPath = func.getInstPath();
            if (curInstPath == null)
            {
                // got the instance path for the first time
                func.setInstPath(objInstPath);
            }
            else
            {
                // if instance path appears in multiple declarations,
                // at least make sure they are the same
                assert (curInstPath.equals(objInstPath));
            }

            // add HDL function to Verilog shell
            if (shellMembers != null) shellMembers.add(func);
        }
    }

    public void visit(IfElseConstraintDecl obj)
    {
        VeraExpression ifExpr = processExpression(obj.getIfExpr());
        VeraExpression thenExpr = processExpression(obj.getThenExpr());
        VeraExpression elseExpr = null;
        ExpressionDecl elseExprDecl = obj.getElseExpr();
        if (elseExprDecl != null)
        {
            elseExpr = processExpression(elseExprDecl);
        }
        currentExpr = new VeraIfElseConstraint(ifExpr, thenExpr, elseExpr);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(IfElseDecl obj)
    {
        final ExpressionDecl ifExprDecl = obj.getIfExpr();
        final VeraExpression ifExpr = processExpression(ifExprDecl);
        if (!ifExpr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Integral expression expected in 'if' condition", ifExprDecl);
        }
        final VeraStatement thenStmt = processStatement(obj.getThenStatement());
        final StatementDecl elseStmtDecl = obj.getElseStatement();
        final VeraStatement elseStmt = (elseStmtDecl != null)
            ? processStatement(elseStmtDecl) : null;
        currentStmt = new VeraIfStatement(ifExpr, thenStmt, elseStmt);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(ImplicationConstraintDecl obj)
    {
        VeraExpression predExpr = processExpression(obj.getPredicateExpr());
        VeraExpression consExpr = processExpression(obj.getConstraintExpr());
        currentExpr = new VeraImplicationConstraint(predExpr, consExpr);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(IntegerLiteralDecl obj)
    {
        currentExpr = new VeraIntegerLiteral(schema, obj.getValue());
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(InterfaceDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // look up interface by identifier in current scope
        final String id = obj.getIdentifier();
        VeraInterfaceType intf = lookupInterface(id, false);
        if (intf == null)
        {
            // create new interface type
            final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
            intf = new VeraInterfaceType(schema, name);
            copyAnnotations(obj, intf);

            // add interface signals
            currentIntf = intf;
            final Iterator iter = obj.getSignals().iterator();
            while (iter.hasNext())
            {
                final SignalDecl signalDecl = (SignalDecl) iter.next();
                signalDecl.accept(this);
                assert (currentIntfSignal != null);
                final VeraInterfaceSignal signal = currentIntfSignal;
                currentIntfSignal = null;
                checkDuplicateName(intf, signal, obj);
                intf.addMember(signal);
            }
            currentIntf = null;

            // add interface to compilation unit
            compUnit.addMember(intf);
        }

        // add interface to Verilog shell
        if (shellMembers != null) shellMembers.add(intf);
    }

    public void visit(LocalVarDecl obj)
    {
        // create new local variable
        final String id = obj.getIdentifier();
        final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
        final VeraType type = processType(obj.getTypeRef());
        final VeraLocalVariable var = new VeraLocalVariable(name, type);
        copyAnnotations(obj, var);

        // process modifiers
        final Collection lvdModifiers = obj.getModifiers();
        if (lvdModifiers.contains(LocalVarModifier.STATIC))
        {
            var.addModifier(VeraVariableModifier.STATIC);
        }
        if (lvdModifiers.contains(LocalVarModifier.SHADOW))
        {
            var.addModifier(VeraVariableModifier.SHADOW);
        }

        // process initializer expression
        final ExpressionDecl initExprDecl = obj.getInitExpr();
        if (initExprDecl != null)
        {
            var.setInitializer(processInitExpr(initExprDecl, type));
        }

        currentVar = var;
    }

    public void visit(MemberAccessDecl obj)
    {
        // evaluate object expression
        final VeraExpression objExpr = processExpression(obj.getObjectExpr());
        final Type objType = objExpr.getResultType();

        // look up member, depending on type of object
        final VeraStructuredTypeMember member;
        final String id = obj.getIdentifier();
        try
        {
            if (objType instanceof VeraClass)
            {
                final VeraClass cls = (VeraClass) objType;
                member = lookupMember(cls, id, VeraNameKind.NON_TYPE, true);
                currentExpr = new VeraMemberAccess(objExpr, member);
            }
            else if (objType instanceof VeraInterfaceType)
            {
                final VeraInterfaceType intf = (VeraInterfaceType) objType;
                member = lookupMember(intf, id, VeraNameKind.NON_TYPE, true);
                currentExpr = new VeraSignalReference(
                    (VeraInterfaceSignal) member);
            }
            else if (objType instanceof VeraPortType)
            {
                if (!id.startsWith("$"))
                {
                    throw new SourceSemanticException(
                        "Port member name should start with '$'", obj);
                }
                final VeraPortType port = (VeraPortType) objType;
                member = lookupMember(port, id.substring(1),
                    VeraNameKind.NON_TYPE, true);
                currentExpr = new VeraMemberAccess(objExpr, member);
            }
            else
            {
                throw new SourceSemanticException(
                    "Class, interface, or port reference expected for member access",
                    obj);
            }
        }
        catch (SourceSemanticException e)
        {
            e.setSourceObject(obj);
            throw e;
        }
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(NewArrayDecl obj)
    {
        assert (lhsType != null);
        if (!(lhsType instanceof VeraDynamicArrayType))
        {
            throw new SourceSemanticException("L-value must be dynamic array",
                obj);
        }
        final VeraDynamicArrayType arrayType = (VeraDynamicArrayType) lhsType;

        final ExpressionDecl sizeExprDecl = obj.getSizeExpr();
        final VeraExpression sizeExpr = processExpression(sizeExprDecl);
        if (!sizeExpr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Size expression must be integral", sizeExprDecl);
        }

        final VeraArrayCreation createExpr = new VeraArrayCreation(arrayType,
            sizeExpr);
        copyAnnotations(obj, createExpr);

        final ExpressionDecl sourceExprDecl = obj.getSourceExpr();
        if (sourceExprDecl != null)
        {
            final VeraExpression sourceExpr = processExpression(sourceExprDecl);
            if (!sourceExpr.getResultType().equals(arrayType))
            {
                throw new SourceSemanticException(
                    "Source array must have same type as L-value",
                    sourceExprDecl);
            }
        }

        checkDefineRef(obj, createExpr);
        currentExpr = createExpr;
    }

    public void visit(NewCopyDecl obj)
    {
        // get class or port type
        assert (lhsType != null);
        if (!(lhsType instanceof VeraClass || lhsType instanceof VeraPortType))
        {
            throw new SourceSemanticException(
                "Class or port type expected for left-hand expression", obj);
        }
        final VeraComplexType type = (VeraComplexType) lhsType;

        // process source expression
        final ExpressionDecl sourceExprDecl = obj.getSourceExpr();
        final VeraExpression sourceExpr = processExpression(sourceExprDecl);
        if (!type.isAssignableFrom(sourceExpr.getResultType()))
        {
            throw new SourceSemanticException(
                "Source object type not is not assignable to new object type",
                sourceExprDecl);
        }

        // create new expression
        currentExpr = new VeraCopyCreation(type, sourceExpr);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(NewDecl obj)
    {
        assert (lhsType != null);

        final VeraInstanceCreation newExpr;
        if (lhsType instanceof VeraClass)
        {
            // get class type
            final VeraClass cls = (VeraClass) lhsType;

            // get formal args for constructor, if any
            final List formalArgs;
            Iterator iter = cls.lookupObjects("new", VeraNameKind.NON_TYPE);
            if (iter.hasNext())
            {
                final VeraMemberFunction newFunc = (VeraMemberFunction) iter
                    .next();
                final FunctionType funcType = newFunc.getType();
                formalArgs = funcType.getArguments();
            }
            else
            {
                formalArgs = Collections.EMPTY_LIST;
            }

            // expand arguments and create new expression
            newExpr = new VeraInstanceCreation(cls);
            final List argExprs = expandArgs(formalArgs, false, obj
                .getArguments(), obj);
            iter = argExprs.iterator();
            while (iter.hasNext())
            {
                newExpr.addArgument((VeraExpression) iter.next());
            }
        }
        else if (lhsType instanceof VeraPortType)
        {
            if (obj.getArguments().size() > 0)
            {
                throw new SourceSemanticException(
                    "Arguments not allowed for port creation", obj);
            }

            newExpr = new VeraInstanceCreation((VeraPortType) lhsType);
        }
        else
        {
            throw new SourceSemanticException(
                "Class or port type expected for left-hand expression", obj);
        }
        copyAnnotations(obj, newExpr);

        checkDefineRef(obj, newExpr);
        currentExpr = newExpr;
    }

    public void visit(NullLiteralDecl obj)
    {
        currentExpr = new VeraNullLiteral(schema);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(OperationDecl obj)
    {
        // save LHS type (necessary for operations in array init elements)
        final Type prevLHSType = lhsType;

        final Operator operator = obj.getOperator();

        // process operands
        final List operandDecls = obj.getOperands();
        final VeraExpression operands[] = new VeraExpression[operandDecls
            .size()];
        int operandCount = 0;
        final Iterator iter = operandDecls.iterator();
        while (iter.hasNext())
        {
            ExpressionDecl operandDecl = (ExpressionDecl) iter.next();
            VeraExpression operand = processExpression(operandDecl);
            operands[operandCount++] = operand;

            // 'new' expressions need to know type of variable on left-hand side
            if (operandCount == 1 && operator == Operator.ASSIGN)
            {
                lhsType = operand.getResultType();
            }
        }

        try
        {
            // process operator
            if (operator == Operator.CONCATENATION)
            {
                final VeraConcatenation concat = new VeraConcatenation(schema);
                for (int i = 0; i < operandCount; ++i)
                {
                    concat.addOperand(operands[i]);
                }
                currentExpr = concat;
            }
            else if (operator == Operator.CONDITIONAL)
            {
                assert (operandCount == 3);
                currentExpr = new VeraConditional(operands[0], operands[1],
                    operands[2]);
            }
            else if (operator == Operator.LOGICAL_OR)
            {
                assert (operandCount == 2);
                currentExpr = new VeraLogicalOr(operands[0], operands[1]);
            }
            else if (operator == Operator.LOGICAL_AND)
            {
                assert (operandCount == 2);
                currentExpr = new VeraLogicalAnd(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_OR)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseOr(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_NOR)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseOrNot(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_XOR)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseXor(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_XNOR)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseXorNot(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_AND)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseAnd(operands[0], operands[1]);
            }
            else if (operator == Operator.BITWISE_NAND)
            {
                assert (operandCount == 2);
                currentExpr = new VeraBitwiseAndNot(operands[0], operands[1]);
            }
            else if (operator == Operator.EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.NOT_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraNotEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.EXACT_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraExactEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.EXACT_NOT_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraExactNotEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.WILD_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraWildEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.WILD_NOT_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraWildNotEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.LESS)
            {
                assert (operandCount == 2);
                currentExpr = new VeraLess(operands[0], operands[1]);
            }
            else if (operator == Operator.LESS_OR_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraLessOrEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.GREATER)
            {
                assert (operandCount == 2);
                currentExpr = new VeraGreater(operands[0], operands[1]);
            }
            else if (operator == Operator.GREATER_OR_EQUAL)
            {
                assert (operandCount == 2);
                currentExpr = new VeraGreaterOrEqual(operands[0], operands[1]);
            }
            else if (operator == Operator.LEFT_SHIFT)
            {
                assert (operandCount == 2);
                currentExpr = new VeraLeftShift(operands[0], operands[1]);
            }
            else if (operator == Operator.RIGHT_SHIFT)
            {
                assert (operandCount == 2);
                currentExpr = new VeraRightShift(operands[0], operands[1]);
            }
            else if (operator == Operator.ADD)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAdd(operands[0], operands[1]);
            }
            else if (operator == Operator.SUBTRACT)
            {
                assert (operandCount == 2);
                currentExpr = new VeraSubtract(operands[0], operands[1]);
            }
            else if (operator == Operator.MULTIPLY)
            {
                assert (operandCount == 2);
                currentExpr = new VeraMultiply(operands[0], operands[1]);
            }
            else if (operator == Operator.DIVIDE)
            {
                assert (operandCount == 2);
                currentExpr = new VeraDivide(operands[0], operands[1]);
            }
            else if (operator == Operator.MODULO)
            {
                assert (operandCount == 2);
                currentExpr = new VeraModulo(operands[0], operands[1]);
            }
            else if (operator == Operator.REPLICATION)
            {
                assert (operandCount == 2);
                currentExpr = new VeraReplication(operands[0], operands[1]);
            }
            else if (operator == Operator.ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssign(operands[0], operands[1]);
            }
            else if (operator == Operator.ADD_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignAdd(operands[0], operands[1]);
            }
            else if (operator == Operator.SUBTRACT_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignSubtract(operands[0], operands[1]);
            }
            else if (operator == Operator.MULTIPLY_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignMultiply(operands[0], operands[1]);
            }
            else if (operator == Operator.DIVIDE_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignDivide(operands[0], operands[1]);
            }
            else if (operator == Operator.MODULO_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignModulo(operands[0], operands[1]);
            }
            else if (operator == Operator.LEFT_SHIFT_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignLeftShift(operands[0], operands[1]);
            }
            else if (operator == Operator.RIGHT_SHIFT_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignRightShift(operands[0], operands[1]);
            }
            else if (operator == Operator.AND_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignAnd(operands[0], operands[1]);
            }
            else if (operator == Operator.OR_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignOr(operands[0], operands[1]);
            }
            else if (operator == Operator.XOR_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignXor(operands[0], operands[1]);
            }
            else if (operator == Operator.NAND_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignAndNot(operands[0], operands[1]);
            }
            else if (operator == Operator.NOR_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignOrNot(operands[0], operands[1]);
            }
            else if (operator == Operator.XNOR_ASSIGN)
            {
                assert (operandCount == 2);
                currentExpr = new VeraAssignXorNot(operands[0], operands[1]);
            }
            else if (operator == Operator.NEGATIVE)
            {
                assert (operandCount == 1);
                currentExpr = new VeraArithmeticNegative(operands[0]);
            }
            else if (operator == Operator.LOGICAL_NEGATIVE)
            {
                assert (operandCount == 1);
                currentExpr = new VeraLogicalNegative(operands[0]);
            }
            else if (operator == Operator.BITWISE_NEGATIVE)
            {
                assert (operandCount == 1);
                currentExpr = new VeraBitwiseNegative(operands[0]);
            }
            else if (operator == Operator.AND_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraAndReduction(operands[0]);
            }
            else if (operator == Operator.NAND_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraNotAndReduction(operands[0]);
            }
            else if (operator == Operator.OR_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraOrReduction(operands[0]);
            }
            else if (operator == Operator.NOR_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraNotOrReduction(operands[0]);
            }
            else if (operator == Operator.XOR_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraXorReduction(operands[0]);
            }
            else if (operator == Operator.XNOR_REDUCTION)
            {
                assert (operandCount == 1);
                currentExpr = new VeraNotXorReduction(operands[0]);
            }
            else if (operator == Operator.BITWISE_REVERSE)
            {
                assert (operandCount == 1);
                currentExpr = new VeraBitwiseReverse(operands[0]);
            }
            else if (operator == Operator.PRE_INCREMENT)
            {
                assert (operandCount == 1);
                currentExpr = new VeraPreIncrement(operands[0]);
            }
            else if (operator == Operator.PRE_DECREMENT)
            {
                assert (operandCount == 1);
                currentExpr = new VeraPreDecrement(operands[0]);
            }
            else if (operator == Operator.POST_INCREMENT)
            {
                assert (operandCount == 1);
                currentExpr = new VeraPostIncrement(operands[0]);
            }
            else if (operator == Operator.POST_DECREMENT)
            {
                assert (operandCount == 1);
                currentExpr = new VeraPostDecrement(operands[0]);
            }
            else
            {
                throw new RuntimeException("Unknown operator: " + operator);
            }
        }
        catch (SourceSemanticException e)
        {
            e.setSourceObject(obj);
            throw e;
        }
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);

        lhsType = prevLHSType;
    }

    public void visit(ParamDecl obj)
    {
        assert (currentFunc != null);
        final VeraFunctionType funcType = currentFunc.getType();

        // get identifier
        final String argID = obj.getIdentifier();
        final VeraName argName = new VeraName(argID, VeraNameKind.NON_TYPE,
            null);

        // get type
        final VeraType argType = processType(obj.getTypeRef());

        // get by-reference flag
        final boolean byRef = obj.isByRef();

        // get optional argument information
        final ExpressionDecl defExprDecl = obj.getDefaultExpr();
        final boolean optional = defExprDecl != null;
        final int optLevel = obj.getOptLevel();
        if (optLevel > 0 && !optional)
        {
            throw new SourceSemanticException(
                "Optional argument must have default value", obj);
        }

        final VeraFunctionArgument arg;
        if (newFunc)
        {
            // create argument object
            arg = new VeraFunctionArgument(argName, argType, currentFunc);
            copyAnnotations(obj, arg);
            arg.setByRef(byRef);
            arg.setOptional(optional);
            arg.setOptionalLevel(optLevel);
            funcType.addArgument(arg);
        }
        else
        {
            // get argument object from previous definition
            final List argList = funcType.getArguments();
            if (currentArgPos >= argList.size())
            {
                throw new SourceSemanticException("Argument count mismatch; "
                    + argList.size() + " expected", obj);
            }
            arg = (VeraFunctionArgument) argList.get(currentArgPos);

            // check that argument definition is consistent
            if (!argType.equals(arg.getType()) || byRef != arg.isByRef())
            {
                throw new SourceSemanticException(
                    "Argument definition mismatch at position " + currentArgPos,
                    obj);
            }

            // update name
            // NOTE: argument names are checked for uniqueness when the
            // function body is processed
            if (!argName.equals(arg.getName()))
            {
                funcType.renameArgument(arg, argName);
            }
        }

        // process default value expression
        if (defExprDecl != null)
        {
            final VeraExpression defExpr = processExpression(defExprDecl);
            arg.setInitializer(defExpr);
        }

        ++currentArgPos;
    }

    public void visit(PortDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // look up port by identifier in current scope
        final String id = obj.getIdentifier();
        VeraPortType port = lookupPort(id, false);
        if (port == null)
        {
            // create new port type
            final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
            port = new VeraPortType(schema, name);
            copyAnnotations(obj, port);

            // add port members
            final Iterator iter = obj.getMembers().iterator();
            while (iter.hasNext())
            {
                final String memberID = (String) iter.next();
                final VeraName memberName = new VeraName(memberID,
                    VeraNameKind.NON_TYPE, port);
                final VeraPortSignal member = new VeraPortSignal(schema,
                    memberName);
                checkDuplicateName(port, member, obj);
                port.addMember(member);
            }

            // add port to compilation unit
            compUnit.addMember(port);
        }
    }

    public void visit(PragmaDecl obj)
    {
        // TODO: visit PragmaDecl
        //System.out.println("#pragma " + obj.getText());
    }

    public void visit(PrimitiveTypeRef obj)
    {
        final VeraPrimitiveKind kind = obj.getPrimitiveKind();
        final VeraType type;
        if (kind == VeraPrimitiveKind.INTEGER)
        {
            type = schema.integerType;
        }
        else if (kind == VeraPrimitiveKind.STRING)
        {
            type = schema.stringType;
        }
        else if (kind == VeraPrimitiveKind.BIT)
        {
            type = schema.bitType;
        }
        else if (kind == VeraPrimitiveKind.EVENT)
        {
            type = schema.eventType;
        }
        else
        {
            throw new AssertionError("Unexpected primitive kind: " + kind);
        }
        currentType = type;
    }

    public void visit(final ProgramDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // program blocks do not appear in generated headers
        if (isFromHeader(obj)) return;

        // at most one program block per compilation unit
        assert (currentProgram == null);

        // process global variable declarations
        Iterator iter = obj.getGlobalVars().iterator();
        while (iter.hasNext())
        {
            final GlobalVarDecl varDecl = (GlobalVarDecl) iter.next();

            // create new global variable
            final String id = varDecl.getIdentifier();
            final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
            final VeraType type = processType(varDecl.getTypeRef());
            final VeraGlobalVariable var = new VeraGlobalVariable(name, type);
            copyAnnotations(varDecl, var);
            compUnit.addMember(var);
        }

        // create new program block
        final String id = obj.getIdentifier();
        final VeraName name = new VeraName(id, VeraNameKind.NON_TYPE, null);
        final VeraBlock block = new VeraBlock(schema);
        final VeraProgram program = new VeraProgram(name, block);
        copyAnnotations(obj, program);

        // program blocks are not accessible by name, but we still check for
        // uniqueness, since the Vera loader requires it
        checkDuplicateName(compUnit, program, obj);

        // add program block to compilation unit
        compUnit.addMember(program);

        // process program body later
        addDeferredProcess(new Runnable()
        {
            public void run()
            {
                processProgramBody(program, obj);
            }
        });

        currentProgram = program;
    }

    void processProgramBody(VeraProgram program, ProgramDecl programDecl)
    {
        currentScope = compUnitScope;

        final VeraBlock block = program.getBlock();

        // process global variable initializers
        Iterator iter = programDecl.getGlobalVars().iterator();
        while (iter.hasNext())
        {
            final GlobalVarDecl varDecl = (GlobalVarDecl) iter.next();
            final VeraGlobalVariable var = lookupGlobalVar(varDecl
                .getIdentifier(), true);

            // if variable declaration has an initializer, translate it into an
            // assignment at the beginning of the program block
            // NOTE: for array initializers, this violates the syntactic rule
            // that array initializers can only occur in variable declarations;
            // still, we model global variables this way because it is the only
            // way to allow separate programs to share global variables
            final ExpressionDecl initExprDecl = varDecl.getInitExpr();
            if (initExprDecl != null)
            {
                VeraExpression initExpr = processInitExpr(initExprDecl, var
                    .getType());
                block.addMember(new VeraExpressionStatement(new VeraAssign(
                    new VeraVariableReference(var), initExpr)));
            }
        }

        // process statements
        iter = programDecl.getStatements().iterator();
        while (iter.hasNext())
        {
            final StatementDecl stmt = (StatementDecl) iter.next();
            stmt.accept(this);
            assert (currentStmt != null);
            block.addMember(currentStmt);
            currentStmt = null;
        }

        currentScope = null;
    }

    public void visit(RandCaseDecl obj)
    {
        final VeraRandCaseStatement randCaseStmt = new VeraRandCaseStatement(
            schema);
        copyAnnotations(obj, randCaseStmt);

        final Iterator iter = obj.getCaseMembers().iterator();
        while (iter.hasNext())
        {
            final RandCaseMemberDecl member = (RandCaseMemberDecl) iter.next();
            member.accept(this);
            assert (currentRandCase != null);
            randCaseStmt.addCase(currentRandCase);
            currentRandCase = null;
        }

        checkDefineRef(obj, randCaseStmt);
        currentStmt = randCaseStmt;
    }

    public void visit(RandCaseMemberDecl obj)
    {
        final ExpressionDecl weightExprDecl = obj.getWeightExpr();
        final VeraExpression weightExpr = processExpression(weightExprDecl);
        if (!weightExpr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "'randcase' weight expression must be integral", weightExprDecl);
        }

        final StatementDecl stmtDecl = obj.getStatementDecl();
        final VeraStatement stmt = processStatement(stmtDecl);

        currentRandCase = new VeraRandCase(weightExpr, stmt);
    }

    public void visit(RepeatDecl obj)
    {
        final ExpressionDecl countExprDecl = obj.getCondition();
        final VeraExpression countExpr = processExpression(countExprDecl);
        if (!countExpr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Integral expression expected in 'repeat' count", countExprDecl);
        }
        final VeraStatement stmt = processStatement(obj.getStatement());
        currentStmt = new VeraRepeatStatement(countExpr, stmt);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(ReturnDecl obj)
    {
        currentStmt = new VeraReturnStatement(schema);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(SetOperationDecl obj)
    {
        // process expression
        final ExpressionDecl exprDecl = obj.getExpr();
        final VeraExpression expr = processExpression(exprDecl);

        // create expression object based on operator type
        final VeraSetOperation setOp;
        final Operator operator = obj.getOperator();
        if (operator == Operator.IN)
        {
            setOp = new VeraInSet(expr);
        }
        else if (operator == Operator.NOT_IN)
        {
            setOp = new VeraNotInSet(expr);
        }
        else
        {
            assert (operator == Operator.DIST);
            setOp = new VeraDistSet(expr);
        }
        copyAnnotations(obj, setOp);

        // process set members
        final List ranges = obj.getRanges().getRanges();
        final Iterator rangeIter = ranges.iterator();
        while (rangeIter.hasNext())
        {
            final RangeDecl range = (RangeDecl) rangeIter.next();

            final VeraSetMember setMember;
            final ExpressionDecl lowDecl = range.getFrom();
            final VeraExpression low = processExpression(lowDecl);
            final ExpressionDecl highDecl = range.getTo();
            if (lowDecl == highDecl)
            {
                // if low and high expressions reference same object,
                // there is really only a single value
                setMember = new VeraSetValue(low);
            }
            else
            {
                final VeraExpression high = processExpression(highDecl);
                setMember = new VeraSetRange(low, high);
            }

            // process weighting (if present)
            if (range instanceof WeightedRangeDecl)
            {
                final WeightedRangeDecl weightedRange = (WeightedRangeDecl) range;
                final ExpressionDecl weightDecl = weightedRange.getWeight();
                if (weightDecl != null)
                {
                    final VeraExpression weight = processExpression(weightDecl);
                    setMember
                        .setWeight(weight, weightedRange.isWeightPerItem());
                }
            }

            setOp.addMember(setMember);
        }

        checkDefineRef(obj, setOp);
        currentExpr = setOp;
    }

    public void visit(SignalDecl obj)
    {
        // create interface signal
        final VeraName name = new VeraName(obj.getIdentifier(),
            VeraNameKind.NON_TYPE, currentIntf);
        final ExpressionDecl highBitExpr = obj.getHighBitExpr();
        final int highBit = highBitExpr != null ? evalIntExpr(highBitExpr) : 0;
        final VeraInterfaceSignal signal = new VeraInterfaceSignal(schema,
            name, obj.getKind(), obj.getDirection(), highBit + 1);
        copyAnnotations(obj, signal);

        // set sample/drive edges
        final EdgeSet sampleEdges = obj.getSampleEdges();
        signal.setSampleEdges(sampleEdges);
        signal.setDriveEdges(obj.getDriveEdges());

        // process skews
        final Iterator iter = obj.getSkews().iterator();
        while (iter.hasNext())
        {
            final SignalSkewDecl skewDecl = (SignalSkewDecl) iter.next();
            final ExpressionDecl timeToXExpr = skewDecl.getTimeToXExpr();
            final int timeToValue = evalIntExpr(skewDecl.getTimeToValueExpr());
            if (timeToXExpr == null)
            {
                // simple skew
                final int skew = timeToValue;
                if (skew < 0)
                {
                    // negative skew is sample skew
                    signal.setSampleSkew(skew);
                }
                else if (skew > 0)
                {
                    // positive skew is drive skew
                    signal.setDriveSkew(skew);
                }
                else
                {
                    // zero skew is ignored
                }
            }
            else
            {
                // surround-by-X timing
                final int timeToX = evalIntExpr(timeToXExpr);
                signal.addSurrXParams(new VeraSurrXParams(skewDecl
                    .getTransitionKind(), timeToX, timeToValue));
            }
        }

        // set sample depth
        final ExpressionDecl depthExpr = obj.getDepthExpr();
        final int depth = depthExpr != null ? evalIntExpr(depthExpr) : 0;
        signal.setSampleDepth(depth);

        // set VCA checking
        signal.setVCAKind(obj.getVCAKind());
        signal.setVCAQValue(obj.getVCAQValue());

        // set HDL node
        signal.setHDLNode(obj.getHDLNode());

        currentIntfSignal = signal;
    }

    public void visit(SignalRangeDecl obj)
    {
        // handled in visit(BindSignalDecl)
        assert false;
    }

    public void visit(SignalSkewDecl obj)
    {
        // handled in visit(SignalDecl)
        assert false;
    }

    public void visit(StringLiteralDecl obj)
    {
        currentExpr = new VeraStringLiteral(schema, obj.getValue());
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(SuperRefDecl obj)
    {
        if (currentClass == null)
        {
            throw new SourceSemanticException(
                "'super' outside of class context", obj);
        }
        final VeraClass baseClass = currentClass.getBaseClass();
        if (baseClass == null)
        {
            throw new SourceSemanticException("Class '"
                + currentClass.getName() + "' has no superclass", obj);
        }
        currentExpr = new VeraSuperReference(currentClass);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(SyncDecl obj)
    {
        final VeraSyncStatement syncStmt = new VeraSyncStatement(schema);
        copyAnnotations(obj, syncStmt);

        final Iterator iter = obj.getTerms().iterator();
        while (iter.hasNext())
        {
            final SyncTerm term = (SyncTerm) iter.next();
            final ExpressionDecl signalDecl = term.getSignal();
            final VeraExpression signal = (signalDecl != null)
                ? processExpression(signalDecl) : new VeraSystemClockReference(
                    schema);

            final SyncEdge edge = term.getEdge();
            final EdgeSet edgeSet;
            if (edge == SyncEdge.POSEDGE)
            {
                edgeSet = EdgeSet.POSEDGE;
            }
            else if (edge == SyncEdge.NEGEDGE)
            {
                edgeSet = EdgeSet.NEGEDGE;
            }
            else
            {
                assert (edge == SyncEdge.ANYEDGE);
                edgeSet = EdgeSet.ANYEDGE;
            }
            syncStmt.addTerm(new VeraSyncTerm(signal, edgeSet, term.isAsync()));
        }

        checkDefineRef(obj, syncStmt);
        currentStmt = syncStmt;
    }

    public void visit(TerminateDecl obj)
    {
        currentStmt = new VeraTerminateStatement(schema);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    public void visit(ThisRefDecl obj)
    {
        if (currentClass == null)
        {
            throw new SourceSemanticException(
                "'this' outside of class context", obj);
        }
        currentExpr = new VeraThisReference(currentClass);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(TypedefDecl obj)
    {
        // determine compilation unit
        final VeraCompilationUnit compUnit = getCompUnit(obj);

        // search for a class with the given identifier
        final String id = obj.getIdentifier();
        final VeraClass foundClass = lookupClass(id, false);
        if (foundClass == null)
        {
            // create new typedef-only class
            final VeraName name = new VeraName(id, VeraNameKind.TYPE, null);
            final VeraUserClass cls = new VeraUserClass(schema, name);
            copyAnnotations(obj, cls);
            cls.setTypedefOnly(true);

            // put class in appropriate compilation unit
            trackExternObject(cls, compUnit, true, true, true);
        }
    }

    public void visit(final UDFFuncDecl obj)
    {
        class FunctionFactory
            implements GlobalFuncFactory
        {
            public VeraGlobalFunction create(
                VeraName name,
                VeraFunctionType funcType)
            {
                funcType.setVarArgs(obj.isVarArgs());
                final VeraUDFFunction func = new VeraUDFFunction(name, funcType);
                func.setLanguage(obj.getLanguage());
                return func;
            }
        }
        processGlobalFunc(obj, new FunctionFactory());
    }

    public void visit(UserTypeRef obj)
    {
        final VeraType type;
        try
        {
            type = lookupType(obj.getIdentifier(), true);
        }
        catch (SourceSemanticException e)
        {
            e.setSourceObject(obj);
            throw e;
        }
        currentType = type;
    }

    public void visit(VarRefDecl obj)
    {
        final String id = obj.getIdentifier();
        final NamedObject foundObj;
        try
        {
            final String classID = obj.getClassIdentifier();
            if (classID == null)
            {
                foundObj = lookupObject(id, VeraNameKind.ANY, true);
            }
            else
            {
                final VeraClass cls = lookupClass(classID, true);
                foundObj = (NamedObject) lookupMember(cls, id,
                    VeraNameKind.ANY, true);
            }
        }
        catch (SourceSemanticException e)
        {
            e.setSourceObject(obj);
            throw e;
        }
        if (foundObj instanceof VeraVariable)
        {
            currentExpr = new VeraVariableReference((VeraVariable) foundObj);
        }
        else if (foundObj instanceof VeraFunction)
        {
            final VeraFunction func = (VeraFunction) foundObj;
            if (obj.isFunctionCall())
            {
                currentExpr = new VeraFunctionReference(func);
            }
            else
            {
                if (func != currentFunc)
                {
                    throw new SourceSemanticException(
                        "Illegal function reference", obj);
                }
                VeraVariable returnVar = func.getReturnVar();
                if (returnVar == null)
                {
                    throw new SourceSemanticException(
                        "Function has no return value", obj);
                }
                currentExpr = new VeraVariableReference(returnVar);
            }
        }
        else if (foundObj instanceof VeraEnumerationElement)
        {
            final VeraEnumerationElement elem = (VeraEnumerationElement) foundObj;
            currentExpr = new VeraEnumValueReference(elem);
        }
        else if (foundObj instanceof VeraInterfaceType)
        {
            final VeraInterfaceType intf = (VeraInterfaceType) foundObj;
            currentExpr = new VeraInterfaceReference(intf);
        }
        else
        {
            throw new SourceSemanticException(
                "Variable, function, enumeration element, or interface "
                    + "reference expected", obj);
        }
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(VoidLiteralDecl obj)
    {
        currentExpr = new VeraVoidLiteral(schema);
        copyAnnotations(obj, currentExpr);
        checkDefineRef(obj, currentExpr);
    }

    public void visit(WhileDecl obj)
    {
        final ExpressionDecl condExprDecl = obj.getCondition();
        final VeraExpression condExpr = processExpression(condExprDecl);
        if (!condExpr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Integral expression expected in 'while' condition",
                condExprDecl);
        }
        final VeraStatement stmt = processStatement(obj.getStatement());
        currentStmt = new VeraWhileStatement(condExpr, stmt);
        copyAnnotations(obj, currentStmt);
        checkDefineRef(obj, currentStmt);
    }

    private void visitList(List l)
    {
        final Iterator iter = l.iterator();
        while (iter.hasNext())
        {
            final VeraSourceObject obj = (VeraSourceObject) iter.next();
            obj.accept(this);
        }
    }

    private VeraCompilationUnit getCompUnit(VeraSourceObject obj)
    {
        final String path = preprocInfo.getCompilationUnit(obj);
        final boolean fromHeader = preprocInfo.isFromHeader(obj);
        return getCompUnit(path, fromHeader);
    }

    private VeraCompilationUnit getCompUnit(String path, boolean fromHeader)
    {
        // look up or create compilation unit
        final VeraCompilationUnit compUnit;
        final Iterator iter = schema
            .lookupObjects(path, VeraNameKind.COMP_UNIT);
        if (iter.hasNext())
        {
            compUnit = (VeraCompilationUnit) iter.next();
            checkMultipleDefinition(iter);
        }
        else
        {
            compUnit = new VeraCompilationUnit(schema, path);
            schema.addMember(compUnit);
        }
        compUnitScope.addCompilationUnit(compUnit, fromHeader);
        compUnitScope.setCurrentUnit(compUnit);
        return compUnit;
    }

    private boolean isFromHeader(SourceObject obj)
    {
        return preprocInfo.isFromHeader(obj);
    }

    private void trackExternObject(
        VeraCompilationUnitMember member,
        VeraCompilationUnit compUnit,
        boolean isNew,
        boolean isLocal,
        boolean isExtern)
    {
        boolean add = isNew;

        if (!isNew && !isLocal && !isExtern)
        {
            VeraCompilationUnit externCompUnit = member.getCompilationUnit();
            if (compUnit != externCompUnit)
            {
                externCompUnit.removeMember(member);
                add = true;
            }
        }

        if (add)
        {
            compUnit.addMember(member);
        }
    }

    public VeraExpression processExpression(ExpressionDecl exprDecl)
    {
        exprDecl.accept(this);
        assert (currentExpr != null);
        final VeraExpression expr = currentExpr;
        currentExpr = null;
        return expr;
    }

    public VeraRange processRange(RangeDecl rangeDecl)
    {
        VeraExpression from = processExpression(rangeDecl.getFrom());
        VeraExpression to = processExpression(rangeDecl.getTo());
        VeraRange range = new VeraRange(schema, from, to);
        checkDefineRef(rangeDecl, range);
        return range;
    }

    public VeraStatement processStatement(StatementDecl stmtDecl)
    {
        stmtDecl.accept(this);
        assert (currentStmt != null);
        final VeraStatement stmt = currentStmt;
        currentStmt = null;
        return stmt;
    }

    public VeraType processType(TypeRef typeRef)
    {
        typeRef.accept(this);
        assert (currentType != null);
        final VeraType type = currentType;
        currentType = null;
        return type;
    }

    private int evalIntExpr(ExpressionDecl exprDecl)
    {
        final VeraExpression expr = processExpression(exprDecl);
        return evalIntExpr(expr, exprDecl);
    }

    private int evalIntExpr(VeraExpression expr, ExpressionDecl exprDecl)
    {
        if (!expr.getResultType().isIntegralConvertible())
        {
            throw new SourceSemanticException(
                "Integral constant expression expected", exprDecl);
        }
        final Integer result = VeraExpression
            .toInteger(expr.evaluateConstant());
        if (result == null)
        {
            throw new SourceSemanticException("Expression may not contain X/Z",
                exprDecl);
        }
        return result.intValue();
    }

    private VeraClass lookupClass(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.TYPE, mustExist);
        if (obj != null && !(obj instanceof VeraClass))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a class");
        }
        return (VeraClass) obj;
    }

    private VeraPortType lookupPort(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.TYPE, mustExist);
        if (obj != null && !(obj instanceof VeraPortType))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a port");
        }
        return (VeraPortType) obj;
    }

    private VeraInterfaceType lookupInterface(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.ANY, mustExist);
        if (obj != null && !(obj instanceof VeraInterfaceType))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent an interface");
        }
        return (VeraInterfaceType) obj;
    }

    private VeraBindVariable lookupBind(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.NON_TYPE,
            mustExist);
        if (obj != null && !(obj instanceof VeraBindVariable))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a bind");
        }
        return (VeraBindVariable) obj;
    }

    private VeraEnumeration lookupEnumeration(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.TYPE, mustExist);
        if (obj != null && !(obj instanceof VeraEnumeration))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent an enumeration");
        }
        return (VeraEnumeration) obj;
    }

    private VeraType lookupType(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.TYPE, mustExist);
        if (obj != null && !(obj instanceof VeraType))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a type");
        }
        return (VeraType) obj;
    }

    private VeraGlobalFunction lookupFunction(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.NON_TYPE,
            mustExist);
        if (obj != null && !(obj instanceof VeraGlobalFunction))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a global function");
        }
        return (VeraGlobalFunction) obj;
    }

    private VeraGlobalVariable lookupGlobalVar(String id, boolean mustExist)
    {
        final NamedObject obj = lookupObject(id, VeraNameKind.NON_TYPE,
            mustExist);
        if (obj != null && !(obj instanceof VeraGlobalVariable))
        {
            throw new SemanticException("Identifier '" + id
                + "' does not represent a global variable");
        }
        return (VeraGlobalVariable) obj;
    }

    private NamedObject lookupObject(
        String id,
        VeraNameKind kind,
        boolean mustExist)
    {
        NamedObject obj = null;
        final Iterator iter = currentScope.lookupObjects(id, kind);
        if (iter.hasNext())
        {
            obj = (NamedObject) iter.next();
            checkMultipleDefinition(iter);
        }
        else if (mustExist)
        {
            throw new SemanticException("Unknown identifier: " + id);
        }
        return obj;
    }

    private void checkMultipleDefinition(Iterator iter)
    {
        if (iter.hasNext())
        {
            NamedObject obj = (NamedObject) iter.next();
            String id = obj.getName().getIdentifier();
            throw new SemanticException("Multiple occurrences of identifier: "
                + id);
        }
    }

    private VeraStructuredTypeMember lookupMember(
        VeraClass cls,
        String id,
        VeraNameKind kind,
        boolean mustExist)
    {
        VeraStructuredTypeMember member = null;
        while (cls != null)
        {
            Iterator iter = cls.lookupObjects(id, kind);
            if (iter.hasNext())
            {
                member = (VeraStructuredTypeMember) iter.next();
                checkMultipleDefinition(iter);
                break;
            }
            cls = cls.getBaseClass();
        }
        if (member == null && mustExist)
        {
            throw new SemanticException("Unknown member identifier: " + id);
        }
        return member;
    }

    private VeraStructuredTypeMember lookupMember(
        VeraStructuredType type,
        String id,
        VeraNameKind kind,
        boolean mustExist)
    {
        VeraStructuredTypeMember member = null;
        Iterator iter = type.lookupObjects(id, kind);
        if (iter.hasNext())
        {
            member = (VeraStructuredTypeMember) iter.next();
            checkMultipleDefinition(iter);
        }
        if (member == null && mustExist)
        {
            throw new SemanticException("Unknown member identifier: " + id);
        }
        return member;
    }
}
