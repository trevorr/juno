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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.juno.runtime.Juno;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Schema;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.util.SemanticException;
import com.newisys.langschema.vera.*;

/**
 * Map of translators for built-in task/function calls.
 * 
 * @author Trevor Robinson
 */
final class BuiltinFunctionTranslatorMap
{
    private final Map<String, BuiltinFunctionTranslator> builtinFuncMap = new HashMap<String, BuiltinFunctionTranslator>();

    public BuiltinFunctionTranslatorMap(SchemaTypes types)
    {
        SimpleStaticTranslator veraStaticXlat = new SimpleStaticTranslator(
            types.junoType);

        builtinFuncMap.put("alloc", veraStaticXlat);
        builtinFuncMap.put("assert", new AssertTranslator());
        builtinFuncMap.put("assoc_index", new AssocIndexTranslator());
        builtinFuncMap.put("cast_assign", new CastAssignTranslator());
        builtinFuncMap.put("delay", veraStaticXlat);
        builtinFuncMap.put("error", veraStaticXlat);
        builtinFuncMap.put("error_mode", veraStaticXlat);
        builtinFuncMap.put("exit", veraStaticXlat);
        builtinFuncMap.put("fclose", veraStaticXlat);
        builtinFuncMap.put("feof", veraStaticXlat);
        builtinFuncMap.put("ferror", veraStaticXlat);
        builtinFuncMap.put("fflush", veraStaticXlat);
        builtinFuncMap.put("flag", veraStaticXlat);
        builtinFuncMap.put("fopen", veraStaticXlat);
        builtinFuncMap.put("fprintf", veraStaticXlat);
        builtinFuncMap.put("freadb", veraStaticXlat);
        builtinFuncMap.put("freadh", veraStaticXlat);
        builtinFuncMap.put("freadstr", veraStaticXlat);
        builtinFuncMap.put("get_coverage", null);
        builtinFuncMap.put("get_cycle", new GetCycleTranslator());
        builtinFuncMap.put("get_inst_coverage", null);
        builtinFuncMap.put("get_plus_arg", veraStaticXlat);
        builtinFuncMap.put("get_systime", veraStaticXlat);
        builtinFuncMap.put("get_time", veraStaticXlat);
        builtinFuncMap.put("get_time_unit", null);
        builtinFuncMap.put("getstate", new GetStateTranslator());
        builtinFuncMap.put("initstate", null);
        builtinFuncMap.put("lock_file", veraStaticXlat);
        builtinFuncMap.put("mailbox_get", veraStaticXlat);
        builtinFuncMap.put("mailbox_put", veraStaticXlat);
        builtinFuncMap.put("os_command", veraStaticXlat);
        builtinFuncMap.put("printf", veraStaticXlat);
        builtinFuncMap.put("prodget", null);
        builtinFuncMap.put("prodset", null);
        builtinFuncMap.put("psprintf", veraStaticXlat);
        builtinFuncMap.put("rand48", veraStaticXlat);
        builtinFuncMap.put("random", veraStaticXlat);
        builtinFuncMap.put("region_enter", veraStaticXlat);
        builtinFuncMap.put("region_exit", veraStaticXlat);
        builtinFuncMap.put("rewind", veraStaticXlat);
        builtinFuncMap.put("semaphore_get", veraStaticXlat);
        builtinFuncMap.put("semaphore_put", veraStaticXlat);
        builtinFuncMap.put("setstate", new SetStateTranslator());
        builtinFuncMap.put("signal_connect", null);
        builtinFuncMap.put("sprintf", new SprintfTranslator());
        builtinFuncMap.put("srandom", veraStaticXlat);
        builtinFuncMap.put("sscanf", veraStaticXlat);
        builtinFuncMap.put("stop", null);
        builtinFuncMap.put("suspend_thread", new SuspendThreadTranslator());
        builtinFuncMap.put("sync", veraStaticXlat);
        builtinFuncMap.put("timeout", null);
        builtinFuncMap.put("trace", null);
        builtinFuncMap.put("trigger", veraStaticXlat);
        builtinFuncMap.put("unlock_file", veraStaticXlat);
        builtinFuncMap.put("urand48", veraStaticXlat);
        builtinFuncMap.put("urandom", veraStaticXlat);
        builtinFuncMap.put("urandom_range", veraStaticXlat);
        builtinFuncMap.put("vca", null);
        builtinFuncMap.put("vera_bit_reverse", null);
        builtinFuncMap.put("vera_crc", null);
        builtinFuncMap.put("vera_get_clk_name", veraStaticXlat);
        builtinFuncMap.put("vera_get_dir", veraStaticXlat);
        builtinFuncMap.put("vera_get_ifc_name", veraStaticXlat);
        builtinFuncMap.put("vera_get_in_depth", veraStaticXlat);
        builtinFuncMap.put("vera_get_in_skew", veraStaticXlat);
        builtinFuncMap.put("vera_get_in_type", veraStaticXlat);
        builtinFuncMap.put("vera_get_name", veraStaticXlat);
        builtinFuncMap.put("vera_get_out_skew", veraStaticXlat);
        builtinFuncMap.put("vera_get_out_type", veraStaticXlat);
        builtinFuncMap.put("vera_get_surrx_D", null);
        builtinFuncMap.put("vera_get_surrx_F", null);
        builtinFuncMap.put("vera_get_surrx_R", null);
        builtinFuncMap.put("vera_get_surrx_Z", null);
        builtinFuncMap.put("vera_get_width", veraStaticXlat);
        builtinFuncMap.put("vera_has_surrx", null);
        builtinFuncMap.put("vera_is_bound", veraStaticXlat);
        builtinFuncMap.put("vera_pack", null);
        builtinFuncMap.put("vera_pack_big_endian", null);
        builtinFuncMap.put("vera_plot", null);
        builtinFuncMap.put("vera_report_profile", veraStaticXlat);
        builtinFuncMap.put("vera_unpack", null);
        builtinFuncMap.put("vera_unpack_big_endian", null);
        builtinFuncMap.put("vsv_call_func", null);
        builtinFuncMap.put("vsv_call_task", null);
        builtinFuncMap.put("vsv_close_conn", null);
        builtinFuncMap.put("vsv_get_conn_err", null);
        builtinFuncMap.put("vsv_make_client", null);
        builtinFuncMap.put("vsv_make_server", null);
        builtinFuncMap.put("vsv_up_connections", null);
        builtinFuncMap.put("vsv_wait_for_done", null);
        builtinFuncMap.put("vsv_wait_for_input", null);
        builtinFuncMap.put("wait_child", veraStaticXlat);
        builtinFuncMap.put("wait_var", new WaitVarTranslator());

        SimpleMemberTranslator memberXlat = new SimpleMemberTranslator();

        builtinFuncMap.put("<root>::constraint_mode", memberXlat);
        builtinFuncMap.put("<root>::finalize", memberXlat);
        builtinFuncMap.put("<root>::object_compare", memberXlat);
        builtinFuncMap.put("<root>::object_copy", memberXlat);
        builtinFuncMap.put("<root>::object_print", memberXlat);
        builtinFuncMap.put("<root>::pack", memberXlat);
        builtinFuncMap.put("<root>::post_pack", memberXlat);
        builtinFuncMap.put("<root>::post_randomize", memberXlat);
        builtinFuncMap.put("<root>::post_unpack", memberXlat);
        builtinFuncMap.put("<root>::pre_pack", memberXlat);
        builtinFuncMap.put("<root>::pre_randomize", memberXlat);
        builtinFuncMap.put("<root>::pre_unpack", memberXlat);
        builtinFuncMap.put("<root>::rand_mode", memberXlat);
        builtinFuncMap.put("<root>::randomize", memberXlat);
        builtinFuncMap.put("<root>::unpack", memberXlat);

        StringMemberTranslator stringXlat = new StringMemberTranslator();

        builtinFuncMap.put("string::atobin", stringXlat);
        builtinFuncMap.put("string::atohex", stringXlat);
        builtinFuncMap.put("string::atoi", stringXlat);
        builtinFuncMap.put("string::atooct", stringXlat);
        builtinFuncMap.put("string::backref", stringXlat);
        builtinFuncMap.put("string::bittostr", stringXlat);
        builtinFuncMap.put("string::compare", stringXlat);
        builtinFuncMap.put("string::get_status", stringXlat);
        builtinFuncMap.put("string::get_status_msg", stringXlat);
        builtinFuncMap.put("string::getc", stringXlat);
        builtinFuncMap.put("string::hash", stringXlat);
        builtinFuncMap.put("string::icompare", stringXlat);
        builtinFuncMap.put("string::itoa", stringXlat);
        builtinFuncMap.put("string::len", stringXlat);
        builtinFuncMap.put("string::match", stringXlat);
        builtinFuncMap.put("string::postmatch", stringXlat);
        builtinFuncMap.put("string::prematch", stringXlat);
        builtinFuncMap.put("string::putc", stringXlat);
        builtinFuncMap.put("string::search", stringXlat);
        builtinFuncMap.put("string::substr", stringXlat);
        builtinFuncMap.put("string::thismatch", stringXlat);
        builtinFuncMap.put("string::tolower", stringXlat);
        builtinFuncMap.put("string::toupper", stringXlat);
    }

    public boolean isBuiltinFunction(VeraFunction func)
    {
        String name = func.getName().getCanonicalName();
        return builtinFuncMap.containsKey(name);
    }

    public BuiltinFunctionTranslator getTranslator(VeraFunction func)
    {
        String name = func.getName().getCanonicalName();
        return builtinFuncMap.get(name);
    }

    /**
     * Translates a built-in task/function call to a Java static method call
     * of the same name in the given class.
     */
    private class SimpleStaticTranslator
        implements BuiltinFunctionTranslator
    {
        private final JavaRawAbstractClass cls;

        public SimpleStaticTranslator(JavaRawAbstractClass cls)
        {
            this.cls = cls;
        }

        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            String methodID = func.getName().getIdentifier();
            exprXlat.translateCall(func, cls, methodID, obj, veraArgExprs);
        }
    }

    /**
     * Translates a built-in task/function call to a Java instance method call
     * of the same name on the given object (if specified) or 'this'.
     */
    private class SimpleMemberTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            JavaRawAbstractClass cls = (obj != null)
                ? (JavaRawAbstractClass) obj.getResultType()
                : (JavaRawAbstractClass) exprXlat.containingType;
            String methodID = func.getName().getIdentifier();
            exprXlat.translateCall(func, cls, methodID, obj, veraArgExprs);
        }
    }

    /**
     * Translates usage of the (user-defined) assert macro to a Java assertion
     * statement.
     */
    private class AssertTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            // Vera: assert(foo);
            // Java: assert(foo);
            int argCount = veraArgExprs.size();
            assert (argCount == 1);
            VeraExpression veraTestExpr = veraArgExprs.get(0);
            JavaExpression testExpr = exprXlat.translateNestedExpr(
                veraTestExpr, exprXlat.schema.booleanType);
            // NOTE: our Vera assert() macro is unfortunately defined as
            // 'if (!(foo)) assertFailed()', which means that if foo is X/Z,
            // the assert does not fail
            testExpr = exprXlat.exprConv.toBoolean(testExpr, false, true);
            exprXlat.result.addInitMember(new JavaAssertStatement(testExpr));
        }
    }

    /**
     * Translates calls to assoc_index.
     */
    private class AssocIndexTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            int argCount = veraArgExprs.size();
            assert (argCount >= 2 && argCount <= 3);

            VeraExpression arg0 = veraArgExprs.get(0);
            int op = ((VeraIntegerLiteral) arg0).getValue();

            VeraExpression arg1 = veraArgExprs.get(1);
            JavaExpression arrayExpr = exprXlat.translateNestedExpr(arg1,
                "array");

            JavaExpression result = null;
            if (argCount == 2)
            {
                if (op == Juno.CHECK || op == Juno.DELETE)
                {
                    // assoc_index(CHECK, array) -> array.check()
                    // assoc_index(DELETE, array) -> array.delete()
                    result = ExpressionBuilder.memberCall(arrayExpr,
                        op == Juno.CHECK ? "check" : "delete");
                }
                else
                {
                    throw new SemanticException("Invalid assoc_index opcode");
                }
            }
            else
            {
                JavaType arrayType = arrayExpr.getResultType();
                boolean isBitArray = exprXlat.types.bitAssocArrayType
                    .isAssignableFrom(arrayType);
                JavaType formalIndexType = isBitArray
                    ? exprXlat.schema.bitVectorType : exprXlat.schema
                        .getStringType();
                VeraExpression arg2 = veraArgExprs.get(2);
                if (op == Juno.CHECK || op == Juno.DELETE)
                {
                    // assoc_index(CHECK, array, index) -> array.check(index)
                    // assoc_index(DELETE, array, index) -> array.delete(index)
                    JavaExpression indexExpr = exprXlat.translateNestedExpr(
                        arg2, "index", formalIndexType);
                    indexExpr = exprXlat.exprConv.toType(formalIndexType,
                        indexExpr);
                    result = ExpressionBuilder.memberCall(arrayExpr,
                        op == Juno.CHECK ? "check" : "delete", indexExpr);
                }
                else if (op == Juno.FIRST || op == Juno.NEXT)
                {
                    // assoc_index(FIRST, array, index)
                    // (BitVector) -> !(index = array.first()).containsXZ()
                    // (Integer/String) -> (index = array.first()) != null
                    // (JunoString) -> !(index.assign(array.first())).equals(JunoString.NULL)
                    // assoc_index(NEXT, array, index)
                    // (BitVector) -> !(index = array.next(index)).containsXZ()
                    // (Integer/String) -> (index = array.next(index)) != null
                    // (JunoString) -> !(index.assign(array.next(index))).equals(JunoString.NULL)
                    //
                    // NOTE: These translations have a subtle difference from
                    // the most literal translation to Vera.assoc_index: the
                    // index expression is assigned to null/X when there is no
                    // first/next index instead of being left unassigned.
                    JavaExpression indexExpr = null;
                    JavaType actualIndexType = null;
                    int indexKind = 0;
                    if (exprXlat.hasJavaLHS(arg2))
                    {
                        indexExpr = exprXlat.translateNestedExpr(arg2);
                        actualIndexType = indexExpr.getResultType();
                        if (exprXlat.schema.isBitVector(actualIndexType))
                        {
                            indexKind = 1;
                        }
                        else if (actualIndexType == exprXlat.schema.integerWrapperType
                            || actualIndexType == exprXlat.schema
                                .getStringType())
                        {
                            indexKind = 2;
                        }
                        else if (actualIndexType == exprXlat.types.junoStringType)
                        {
                            indexKind = 3;
                        }
                    }
                    if (indexKind > 0)
                    {
                        LHSTranslator lhsXlat = new SimpleLHSTranslator(
                            exprXlat, exprXlat.result, indexExpr,
                            op == Juno.NEXT, true);
                        JavaExpression callExpr;
                        if (op == Juno.FIRST)
                        {
                            callExpr = ExpressionBuilder.memberCall(arrayExpr,
                                "first");
                        }
                        else
                        {
                            JavaExpression prevIndexExpr = exprXlat.exprConv
                                .toType(formalIndexType, lhsXlat
                                    .getReadExpression());
                            callExpr = ExpressionBuilder.memberCall(arrayExpr,
                                "next", prevIndexExpr);
                        }
                        callExpr = exprXlat.convertRHS(callExpr,
                            formalIndexType, actualIndexType, false);
                        JavaExpression assignExpr = lhsXlat.getWriteExpression(
                            callExpr).getResultExpr();
                        switch (indexKind)
                        {
                        case 1:
                            result = new JavaLogicalNot(exprXlat.schema,
                                ExpressionBuilder.memberCall(assignExpr,
                                    "containsXZ"));
                            break;
                        case 2:
                            result = new JavaNotEqual(exprXlat.schema,
                                assignExpr,
                                new JavaNullLiteral(exprXlat.schema));
                            break;
                        case 3:
                            result = new JavaLogicalNot(exprXlat.schema,
                                ExpressionBuilder.memberCall(assignExpr,
                                    "equals", new JavaVariableReference(
                                        exprXlat.types.junoStringType
                                            .getField("NULL"))));
                            break;
                        default:
                            assert false;
                        }
                    }
                    else
                    {
                        // assoc_index(FIRST, array, index)
                        // -> Vera.assoc_index(Vera.FIRST, array, index_holder)
                        // assoc_index(NEXT, array, index)
                        // -> Vera.assoc_index(Vera.NEXT, array, index_holder)
                        exprXlat.translateCall(func, exprXlat.types.junoType,
                            "assoc_index", obj, veraArgExprs);
                    }
                }
                else
                {
                    throw new SemanticException("Invalid assoc_index opcode");
                }
            }
            if (result != null)
            {
                exprXlat.result.setResultExpr(result);
            }
        }
    }

    /**
     * Translates calls to cast_assign.
     */
    private class CastAssignTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            int argCount = veraArgExprs.size();
            assert (argCount >= 2 && argCount <= 3);
            boolean checked = (argCount == 3);
            VeraExpression veraDstExpr = veraArgExprs.get(0);
            VeraExpression veraSrcExpr = veraArgExprs.get(1);
            VeraType veraDstType = veraDstExpr.getResultType();
            VeraType veraSrcType = veraSrcExpr.getResultType();
            boolean voidContext = exprXlat.desiredResultType instanceof JavaVoidType;

            JavaExpression result = null;
            boolean alwaysFail = false;
            if (veraDstType.isAssignableFrom(veraSrcType))
            {
                // identical types,
                // string/enum/integer/bit to integer/bit,
                // class upcast

                // handle as normal assignment: dst = src
                exprXlat.buildAssignOp(veraDstExpr, veraSrcExpr);

                // flatten assignment expression into init statement
                exprXlat.result.flatten(null);
            }
            else if (veraDstType instanceof VeraUserClass
                && veraSrcType instanceof VeraUserClass)
            {
                VeraUserClass veraDstClass = (VeraUserClass) veraDstType;
                VeraUserClass veraSrcClass = (VeraUserClass) veraSrcType;
                if (veraSrcClass.isSuperclassOf(veraDstClass))
                {
                    // downcast

                    // translate result class
                    JavaRawClass dstClass = exprXlat.translateClass(
                        veraDstClass, false);

                    // translate source expression
                    JavaExpression srcExpr = exprXlat.translateNestedExpr(
                        veraSrcExpr, "src");
                    if (checked)
                    {
                        // source will be evaluated twice if checked
                        srcExpr = EvalOnceExprBuilder.evalConstExpr(srcExpr,
                            exprXlat.result, "src", true);
                    }

                    // build cast expression: (DstClass) src
                    JavaExpression castExpr = new JavaCastExpression(dstClass,
                        srcExpr);
                    JavaNullLiteral nullLiteral = null;
                    if (checked)
                    {
                        // conditional cast:
                        // src instanceof DstClass ? (DstClass) src : null
                        nullLiteral = new JavaNullLiteral(exprXlat.schema);
                        castExpr = new JavaConditional(new JavaTypeTest(
                            srcExpr, dstClass), castExpr, nullLiteral);
                    }

                    // build assignment expression: dst = <castExpr>
                    LHSTranslator lhsXlat = exprXlat.translateLHS(veraDstExpr,
                        null, checked && !voidContext, true);
                    lhsXlat.getWriteExpression(castExpr).mergeIntoInit(
                        exprXlat.result);

                    // build result expression: dst != null || src == null
                    if (checked && !voidContext)
                    {
                        result = new JavaConditionalOr(
                            exprXlat.schema,
                            new JavaNotEqual(exprXlat.schema, lhsXlat
                                .getReadExpression(), nullLiteral),
                            new JavaEqual(exprXlat.schema, srcExpr, nullLiteral));
                    }
                }
                else
                {
                    // fail (unrelated declared types)
                    alwaysFail = true;
                }
            }
            else if (veraDstType.isIntegralConvertible()
                && (veraSrcType.isIntegralConvertible() || veraSrcType instanceof VeraStringType))
            {
                // translate source expression
                JavaExpression srcExpr = exprXlat.translateNestedExpr(
                    veraSrcExpr, "src");
                if (checked && !voidContext
                    && veraDstType instanceof VeraEnumeration)
                {
                    // source will be evaluated twice if checked, not in void
                    // context, and assigning to an enum
                    srcExpr = EvalOnceExprBuilder.evalConstExpr(srcExpr,
                        exprXlat.result, "src", true);
                }

                JavaExpression intExpr;
                if (veraSrcType instanceof VeraStringType)
                {
                    // string to integer/bit/enum
                    srcExpr = exprXlat.exprConv.toJavaString(srcExpr, true);
                    if (veraDstType instanceof VeraBitVectorType)
                    {
                        // new BitVector(src.getBytes())
                        JavaExpression bytesExpr = ExpressionBuilder
                            .memberCall(srcExpr, "getBytes");
                        intExpr = ExpressionBuilder.newInstance(
                            exprXlat.schema.bitVectorType, bytesExpr);
                    }
                    else
                    {
                        // IntOp.toInt(src)
                        assert (veraDstType instanceof VeraIntegerType);
                        intExpr = ExpressionBuilder.staticCall(
                            exprXlat.types.intOpType, "toInt", srcExpr);
                    }
                }
                else
                {
                    // enum/integer/bit to enum
                    assert (veraSrcType.isIntegralConvertible());
                    intExpr = srcExpr;
                }

                LHSTranslator lhsXlat = exprXlat.translateLHS(veraDstExpr,
                    null, checked && !voidContext, true);

                JavaExpression finalExpr;
                if (veraDstType instanceof VeraEnumeration)
                {
                    // convert source expression to Integer
                    JavaExpression integerExpr = exprXlat.exprConv
                        .toInteger(intExpr);
                    if (checked && !voidContext)
                    {
                        // source will be evaluated twice if checked
                        integerExpr = EvalOnceExprBuilder.evalConstExpr(
                            integerExpr, exprXlat.result, "src", true);
                    }

                    // build enum conversion expression
                    VeraEnumeration veraEnum = (VeraEnumeration) veraDstType;
                    JavaEnum enumClass = exprXlat.translateEnum(veraEnum);
                    JavaBooleanLiteral checkedLiteral = new JavaBooleanLiteral(
                        exprXlat.schema, checked);
                    finalExpr = ExpressionBuilder.staticCall(enumClass,
                        "forValue", integerExpr, checkedLiteral);

                    // build result expression: dst.isDefined() || src == null
                    if (checked && !voidContext)
                    {
                        result = new JavaConditionalOr(exprXlat.schema,
                            ExpressionBuilder.memberCall(lhsXlat
                                .getReadExpression(), "isDefined"),
                            new JavaEqual(exprXlat.schema, integerExpr,
                                new JavaNullLiteral(exprXlat.schema)));
                    }
                }
                else
                {
                    // string to integer/bit
                    finalExpr = intExpr;
                }

                // build assignment expression: dst = <finalExpr>
                lhsXlat.getWriteExpression(finalExpr).mergeIntoInit(
                    exprXlat.result);
            }
            else
            {
                // fail
                alwaysFail = true;
            }

            if (alwaysFail)
            {
                if (checked)
                {
                    // assign null/X to dst
                    LHSTranslator lhsXlat = exprXlat.translateLHS(veraDstExpr,
                        null, false, true);
                    JavaExpression undefValue = exprXlat.getInitValue(lhsXlat
                        .getResultType(), true, false);
                    lhsXlat.getWriteExpression(undefValue).mergeIntoInit(
                        exprXlat.result);
                }
                else
                {
                    // throw exception for bad cast
                    exprXlat.result.addInitExpr(ExpressionBuilder.staticCall(
                        exprXlat.types.junoType, "badCast"));
                }

                // return false
                result = new JavaBooleanLiteral(exprXlat.schema, false);
            }
            else if (result == null && !voidContext)
            {
                // if result expression is not set, always return true
                result = new JavaBooleanLiteral(exprXlat.schema, true);
            }

            exprXlat.result.setOptionalResult(true);
            exprXlat.result.setResultExpr(result);
        }
    }

    /**
     * Translates calls to get_cycle.
     */
    private class GetCycleTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            // Vera: foo = get_cycle();
            // Java: foo = Vera.systemClock.getCycleCount();
            // Vera: foo = get_cycle(signal);
            // Java: foo = signal.getClock().getCycleCount();
            int argCount = veraArgExprs.size();
            assert (argCount >= 0 && argCount <= 1);
            JavaExpression clockExpr;
            if (argCount == 0)
            {
                clockExpr = new JavaVariableReference(exprXlat.types.junoType
                    .getField("systemClock"));
            }
            else
            {
                VeraExpression veraSignalExpr = veraArgExprs.get(0);
                JavaExpression signalExpr = exprXlat.translateNestedExpr(
                    veraSignalExpr, "signal", exprXlat.types.signalType);
                clockExpr = ExpressionBuilder
                    .memberCall(signalExpr, "getClock");
            }
            JavaExpression getCycleCountCall = ExpressionBuilder.memberCall(
                clockExpr, "getCycleCount");
            exprXlat.result.setResultExpr(getCycleCountCall);
        }
    }

    /**
     * Translates calls to get_state.
     * Not implemented, but not generally necessary with Java.
     */
    private class GetStateTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            int argCount = veraArgExprs.size();
            assert (argCount >= 1 && argCount <= 2);
            // TODO: translate type of VeraRandomState
            if (true) return;
            VeraExpression veraStateExpr = veraArgExprs.get(0);
            LHSTranslator lhsXlat = exprXlat.translateLHS(veraStateExpr, null,
                false, true);
            if (argCount == 1)
            {
                // thread getstate()
                // Vera: getstate(foo);
                // Java: foo = DV.simulation.getRandom().clonePRNG();
                JavaVariableReference simRef = new JavaVariableReference(
                    exprXlat.types.dvType.getField("simulation"));
                JavaExpression getRandomCall = ExpressionBuilder.memberCall(
                    simRef, "getRandom");
                JavaExpression clonePRNGCall = ExpressionBuilder.memberCall(
                    getRandomCall, "clonePRNG");
                lhsXlat.getWriteExpression(clonePRNGCall).mergeIntoResult(
                    exprXlat.result);
            }
            else
            {
                // TODO: object getstate()
                assert false;
            }
        }
    }

    /**
     * Translates calls to set_state.
     * Not implemented, but not generally necessary with Java.
     */
    private class SetStateTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            int argCount = veraArgExprs.size();
            assert (argCount >= 1 && argCount <= 2);
            // TODO: translate type of VeraRandomState
            if (true) return;
            VeraExpression veraStateExpr = veraArgExprs.get(0);
            JavaExpression stateExpr = exprXlat.translateNestedExpr(
                veraStateExpr, "state");
            if (argCount == 1)
            {
                // thread setstate()
                // Vera: setstate(foo);
                // Java: DV.simulation.setRandom(foo.clonePRNG());
                JavaVariableReference simRef = new JavaVariableReference(
                    exprXlat.types.dvType.getField("simulation"));
                JavaExpression clonePRNGCall = ExpressionBuilder.memberCall(
                    stateExpr, "clonePRNG");
                JavaExpression setRandomCall = ExpressionBuilder.memberCall(
                    simRef, "setRandom", clonePRNGCall);
                exprXlat.result.setResultExpr(setRandomCall);
            }
            else
            {
                // TODO: object setstate()
                assert false;
            }
        }
    }

    /**
     * Translates calls to sprintf.
     */
    private class SprintfTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            assert (veraArgExprs.size() > 0);
            final VeraExpression veraResultArgExpr = veraArgExprs.get(0);
            final JavaExpression resultArgExpr = exprXlat.translateNestedExpr(
                veraResultArgExpr, "result");
            final JavaType resultType = resultArgExpr.getResultType();
            if (resultType == exprXlat.schema.getStringType())
            {
                // result argument is String; translate as sprintf(foo, ...) as
                // foo = psprintf(...)
                final Schema veraSchema = func.getSchema();
                final Iterator< ? extends NamedObject> iter = veraSchema
                    .lookupObjects("psprintf", VeraNameKind.NON_TYPE);
                assert (iter.hasNext());
                final VeraFunction psprintfFunc = (VeraFunction) iter.next();
                final List<VeraExpression> psprintfArgs = new LinkedList<VeraExpression>(
                    veraArgExprs);
                psprintfArgs.remove(0);
                exprXlat.translateCall(psprintfFunc, exprXlat.types.junoType,
                    "psprintf", null, psprintfArgs);
                JavaExpression expr = exprXlat.result.getResultExpr();
                expr = new JavaAssign(exprXlat.schema, resultArgExpr, expr);
                exprXlat.result.setResultExpr(expr);
            }
            else
            {
                // result argument should be JunoString
                exprXlat.translateCall(func, exprXlat.types.junoType,
                    "sprintf", null, veraArgExprs);
            }
        }
    }

    /**
     * Translates calls to suspend_thread.
     */
    private class SuspendThreadTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            // Vera: suspend_thread();
            // Java: DV.simulation.yield();
            assert (veraArgExprs.size() == 0);
            JavaVariableReference simRef = new JavaVariableReference(
                exprXlat.types.dvType.getField("simulation"));
            JavaExpression yieldCall = ExpressionBuilder.memberCall(simRef,
                "yield");
            exprXlat.result.setResultExpr(yieldCall);
        }
    }

    /**
     * Translates calls to wait_var.
     */
    private class WaitVarTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            // Vera: wait_var(var, ...);
            // Java: Vera.sync(Vera.ANY, varevent, ...);
            final int argCount = veraArgExprs.size();
            assert (argCount > 0);
            JavaExpression[] javaArgExprs = new JavaExpression[argCount + 1];
            int argIndex = 0;
            javaArgExprs[argIndex++] = new JavaVariableReference(
                exprXlat.types.junoType.getField("ANY"));
            for (VeraExpression veraExpr : veraArgExprs)
            {
                LHSTranslator lhsXlat = exprXlat.translateLHS(veraExpr, null,
                    true, false);
                JavaExpression eventRef = lhsXlat.getUpdateEvent();
                if (eventRef == null)
                {
                    exprXlat
                        .log("Warning: wait_var event not defined for variable: "
                            + veraExpr);
                    eventRef = new JavaNullLiteral(exprXlat.schema);
                }
                javaArgExprs[argIndex++] = eventRef;
            }
            JavaExpression syncCall = ExpressionBuilder.staticCall(
                exprXlat.types.junoType, "sync", javaArgExprs, null);
            exprXlat.result.setResultExpr(syncCall);
        }
    }

    /**
     * Translates calls to string member functions. Primarily used when a
     * Vera string is translated to a Java String; the implementation for
     * translations to JunoString is trivial.
     */
    private class StringMemberTranslator
        implements BuiltinFunctionTranslator
    {
        public void translate(
            ExpressionTranslator exprXlat,
            VeraFunction func,
            JavaExpression obj,
            List<VeraExpression> veraArgExprs)
        {
            assert (obj != null);
            JavaRawAbstractClass cls = (JavaRawAbstractClass) obj
                .getResultType();
            String methodID = func.getName().getIdentifier();
            if (cls == exprXlat.types.junoStringType)
            {
                exprXlat.translateCall(func, cls, methodID, obj, veraArgExprs);
            }
            else
            {
                assert (cls == exprXlat.schema.getStringType());
                boolean writeBack = false;
                boolean msg = false;
                if (methodID.equals("tolower"))
                {
                    // translate directly to String.toLowerCase
                    exprXlat.translateCall(func, cls, "toLowerCase", obj,
                        veraArgExprs);
                }
                else if (methodID.equals("toupper"))
                {
                    // translate directly to String.toUpperCase
                    exprXlat.translateCall(func, cls, "toUpperCase", obj,
                        veraArgExprs);
                }
                else if (methodID.equals("itoa") || methodID.equals("bittostr"))
                {
                    // translate to static StringOp call with assign to 'this'
                    // expression instead of passing it
                    writeBack = true;
                    exprXlat.translateCall(func, exprXlat.types.stringOpType,
                        methodID, null, veraArgExprs);
                }
                else if (methodID.equals("get_status")
                    || (msg = methodID.equals("get_status_msg")))
                {
                    // translate to JunoString.OK / StringOp.get_status_msg(OK)
                    // (since the analysis phase guarantees the status is OK)
                    JavaVariableReference okRef = new JavaVariableReference(
                        exprXlat.types.junoStringType.getField("OK"));
                    JavaExpression result = msg ? ExpressionBuilder.staticCall(
                        exprXlat.types.stringOpType, methodID, okRef) : okRef;
                    exprXlat.result.setResultExpr(result);
                }
                else if (methodID.equals("prematch")
                    || methodID.equals("postmatch")
                    || methodID.equals("thismatch")
                    || methodID.equals("backref"))
                {
                    // these functions return the null string if no match
                    JavaVariableReference nullRef = new JavaVariableReference(
                        exprXlat.types.junoStringType.getField("NULL"));
                    exprXlat.result.setResultExpr(nullRef);
                }
                else
                {
                    // only putc() requires write-back
                    writeBack = methodID.equals("putc");
                    exprXlat.translateCallAsStatic(func,
                        exprXlat.types.stringOpType, methodID, obj,
                        veraArgExprs);
                }
                if (writeBack)
                {
                    // potential multiple references to 'this' expression
                    // should be okay, since Vera requires it to be a variable
                    // reference or member variable access
                    JavaExpression expr = exprXlat.result.getResultExpr();
                    expr = new JavaAssign(exprXlat.schema, obj, expr);
                    exprXlat.result.setResultExpr(expr);
                }
            }
        }
    }
}
