/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.schemaprinter.vera;

import com.newisys.langschema.vera.*;
import com.newisys.util.text.TokenFormatter;

/**
 * Printer module used to print all first-class schema objects (i.e. those
 * appearing in VeraSchemaObjectVisitor).
 * 
 * @author Trevor Robinson
 */
public class SchemaObjectPrinter
    extends VeraSchemaPrinterModule
    implements VeraSchemaObjectVisitor
{
    public SchemaObjectPrinter(TokenFormatter fmt, VeraSchemaPrinter config)
    {
        super(fmt, config);
    }

    private void print(VeraSchemaMember obj)
    {
        SchemaMemberPrinter printer = new SchemaMemberPrinter(fmt, config);
        obj.accept(printer);
    }

    private void print(VeraCompilationUnitMember obj)
    {
        CompilationUnitMemberPrinter printer = new CompilationUnitMemberPrinter(
            fmt, config);
        obj.accept(printer);
    }

    private void print(VeraClassMember obj)
    {
        ClassMemberPrinter printer = new ClassMemberPrinter(fmt, config);
        obj.accept(printer);
    }

    private void print(VeraBlockMember obj)
    {
        BlockMemberPrinter printer = new BlockMemberPrinter(fmt, config);
        printer.printMember(obj);
    }

    private void print(VeraExpression obj)
    {
        ExpressionPrinter printer = new ExpressionPrinter(fmt, config);
        obj.accept(printer);
    }

    private void print(VeraType obj)
    {
        printType(obj);
    }

    public void visit(VeraAdd obj)
    {
        print(obj);
    }

    public void visit(VeraAndReduction obj)
    {
        print(obj);
    }

    public void visit(VeraArithmeticNegative obj)
    {
        print(obj);
    }

    public void visit(VeraArrayAccess obj)
    {
        print(obj);
    }

    public void visit(VeraArrayCreation obj)
    {
        print(obj);
    }

    public void visit(VeraArrayInitializer obj)
    {
        print(obj);
    }

    public void visit(VeraAssign obj)
    {
        print(obj);
    }

    public void visit(VeraAssignAdd obj)
    {
        print(obj);
    }

    public void visit(VeraAssignAnd obj)
    {
        print(obj);
    }

    public void visit(VeraAssignAndNot obj)
    {
        print(obj);
    }

    public void visit(VeraAssignDivide obj)
    {
        print(obj);
    }

    public void visit(VeraAssignLeftShift obj)
    {
        print(obj);
    }

    public void visit(VeraAssignModulo obj)
    {
        print(obj);
    }

    public void visit(VeraAssignMultiply obj)
    {
        print(obj);
    }

    public void visit(VeraAssignOr obj)
    {
        print(obj);
    }

    public void visit(VeraAssignOrNot obj)
    {
        print(obj);
    }

    public void visit(VeraAssignRightShift obj)
    {
        print(obj);
    }

    public void visit(VeraAssignSubtract obj)
    {
        print(obj);
    }

    public void visit(VeraAssignXor obj)
    {
        print(obj);
    }

    public void visit(VeraAssignXorNot obj)
    {
        print(obj);
    }

    public void visit(VeraAssocArrayType obj)
    {
        print(obj);
    }

    public void visit(VeraBindMember obj)
    {
        printBindMember(obj);
    }

    public void visit(VeraBindVariable obj)
    {
        print(obj);
    }

    public void visit(VeraBitSliceAccess obj)
    {
        print(obj);
    }

    public void visit(VeraBitType obj)
    {
        print(obj);
    }

    public void visit(VeraBitVectorLiteral obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseAnd obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseAndNot obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseNegative obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseOr obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseOrNot obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseReverse obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseXor obj)
    {
        print(obj);
    }

    public void visit(VeraBitwiseXorNot obj)
    {
        print(obj);
    }

    public void visit(VeraBlock obj)
    {
        print(obj);
    }

    public void visit(VeraBreakpointStatement obj)
    {
        print(obj);
    }

    public void visit(VeraBreakStatement obj)
    {
        print(obj);
    }

    public void visit(VeraClassConstraint obj)
    {
        print((VeraClassMember) obj);
    }

    public void visit(VeraCompilationUnit obj)
    {
        print(obj);
    }

    public void visit(VeraConcatenation obj)
    {
        print(obj);
    }

    public void visit(VeraConditional obj)
    {
        print(obj);
    }

    public void visit(VeraConstraintSet obj)
    {
        print(obj);
    }

    public void visit(VeraContinueStatement obj)
    {
        print(obj);
    }

    public void visit(VeraCopyCreation obj)
    {
        print(obj);
    }

    public void visit(VeraDefineArgument obj)
    {
        fmt.printToken(obj.getName().getIdentifier());
    }

    public void visit(VeraDefineReference obj)
    {
        printDefineRef(obj, fmt);
    }

    public void visit(VeraDepthAccess obj)
    {
        print(obj);
    }

    public void visit(VeraDistSet obj)
    {
        print(obj);
    }

    public void visit(VeraDivide obj)
    {
        print(obj);
    }

    public void visit(VeraDriveSampleStatement obj)
    {
        print(obj);
    }

    public void visit(VeraDynamicArrayType obj)
    {
        print(obj);
    }

    public void visit(VeraEnumeration obj)
    {
        printEnum(obj);
    }

    public void visit(VeraEnumerationElement obj)
    {
        printEnumElement(obj);
    }

    public void visit(VeraEnumValueReference obj)
    {
        print(obj);
    }

    public void visit(VeraEqual obj)
    {
        print(obj);
    }

    public void visit(VeraEventType obj)
    {
        print(obj);
    }

    public void visit(VeraExactEqual obj)
    {
        print(obj);
    }

    public void visit(VeraExactNotEqual obj)
    {
        print(obj);
    }

    public void visit(VeraExpectStatement obj)
    {
        print(obj);
    }

    public void visit(VeraExpressionDefine obj)
    {
        print(obj);
    }

    public void visit(VeraExpressionStatement obj)
    {
        print(obj);
    }

    public void visit(VeraFixedArrayType obj)
    {
        print(obj);
    }

    public void visit(VeraFixedBitVectorType obj)
    {
        print(obj);
    }

    public void visit(VeraForkStatement obj)
    {
        print(obj);
    }

    public void visit(VeraForStatement obj)
    {
        print(obj);
    }

    public void visit(VeraFunctionArgument obj)
    {
        printFuncArg(obj);
    }

    public void visit(VeraFunctionInvocation obj)
    {
        print(obj);
    }

    public void visit(VeraFunctionReference obj)
    {
        print(obj);
    }

    public void visit(VeraFunctionType obj)
    {
        printFuncProlog(obj);
        printFuncArgs(obj);
    }

    public void visit(VeraGlobalFunction obj)
    {
        print((VeraSchemaMember) obj);
    }

    public void visit(VeraGlobalVariable obj)
    {
        print(obj);
    }

    public void visit(VeraGreater obj)
    {
        print(obj);
    }

    public void visit(VeraGreaterOrEqual obj)
    {
        print(obj);
    }

    public void visit(VeraHDLFunction obj)
    {
        print((VeraCompilationUnitMember) obj);
    }

    public void visit(VeraIfElseConstraint obj)
    {
        print(obj);
    }

    public void visit(VeraIfStatement obj)
    {
        print(obj);
    }

    public void visit(VeraImplicationConstraint obj)
    {
        print(obj);
    }

    public void visit(VeraInSet obj)
    {
        print(obj);
    }

    public void visit(VeraInstanceCreation obj)
    {
        print(obj);
    }

    public void visit(VeraIntegerLiteral obj)
    {
        print(obj);
    }

    public void visit(VeraIntegerType obj)
    {
        print(obj);
    }

    public void visit(VeraInterfaceReference obj)
    {
        print(obj);
    }

    public void visit(VeraInterfaceSignal obj)
    {
        printInterfaceSignal(obj);
    }

    public void visit(VeraInterfaceType obj)
    {
        printInterface(obj);
    }

    public void visit(VeraLeftShift obj)
    {
        print(obj);
    }

    public void visit(VeraLess obj)
    {
        print(obj);
    }

    public void visit(VeraLessOrEqual obj)
    {
        print(obj);
    }

    public void visit(VeraLocalVariable obj)
    {
        print(obj);
    }

    public void visit(VeraLogicalAnd obj)
    {
        print(obj);
    }

    public void visit(VeraLogicalNegative obj)
    {
        print(obj);
    }

    public void visit(VeraLogicalOr obj)
    {
        print(obj);
    }

    public void visit(VeraMagicType obj)
    {
        print(obj);
    }

    public void visit(VeraMemberAccess obj)
    {
        print(obj);
    }

    public void visit(VeraMemberFunction obj)
    {
        print(obj);
    }

    public void visit(VeraMemberVariable obj)
    {
        print(obj);
    }

    public void visit(VeraModulo obj)
    {
        print(obj);
    }

    public void visit(VeraMultiply obj)
    {
        print(obj);
    }

    public void visit(VeraNotAndReduction obj)
    {
        print(obj);
    }

    public void visit(VeraNotEqual obj)
    {
        print(obj);
    }

    public void visit(VeraNotInSet obj)
    {
        print(obj);
    }

    public void visit(VeraNotOrReduction obj)
    {
        print(obj);
    }

    public void visit(VeraNotXorReduction obj)
    {
        print(obj);
    }

    public void visit(VeraNullLiteral obj)
    {
        print(obj);
    }

    public void visit(VeraNullType obj)
    {
        print(obj);
    }

    public void visit(VeraOrReduction obj)
    {
        print(obj);
    }

    public void visit(VeraPortSignal obj)
    {
        printPortSignal(obj);
    }

    public void visit(VeraPortType obj)
    {
        printPort(obj);
    }

    public void visit(VeraPostDecrement obj)
    {
        print(obj);
    }

    public void visit(VeraPostIncrement obj)
    {
        print(obj);
    }

    public void visit(VeraPreDecrement obj)
    {
        print(obj);
    }

    public void visit(VeraPreIncrement obj)
    {
        print(obj);
    }

    public void visit(VeraProgram obj)
    {
        print(obj);
    }

    public void visit(VeraRandCaseStatement obj)
    {
        print(obj);
    }

    public void visit(VeraRange obj)
    {
        printRange(obj, fmt);
    }

    public void visit(VeraRangeDefine obj)
    {
        print(obj);
    }

    public void visit(VeraRepeatStatement obj)
    {
        print(obj);
    }

    public void visit(VeraReplication obj)
    {
        print(obj);
    }

    public void visit(VeraReturnStatement obj)
    {
        print(obj);
    }

    public void visit(VeraRightShift obj)
    {
        print(obj);
    }

    public void visit(VeraSignalReference obj)
    {
        print(obj);
    }

    public void visit(VeraStatementDefine obj)
    {
        print(obj);
    }

    public void visit(VeraStringLiteral obj)
    {
        print(obj);
    }

    public void visit(VeraStringType obj)
    {
        print(obj);
    }

    public void visit(VeraSubtract obj)
    {
        print(obj);
    }

    public void visit(VeraSuperReference obj)
    {
        print(obj);
    }

    public void visit(VeraSwitchStatement obj)
    {
        print(obj);
    }

    public void visit(VeraSyncStatement obj)
    {
        print(obj);
    }

    public void visit(VeraSystemClass obj)
    {
        print(obj);
    }

    public void visit(VeraSystemClockReference obj)
    {
        print(obj);
    }

    public void visit(VeraTerminateStatement obj)
    {
        print(obj);
    }

    public void visit(VeraThisReference obj)
    {
        print(obj);
    }

    public void visit(VeraTypeDefine obj)
    {
        print(obj);
    }

    public void visit(VeraUDFFunction obj)
    {
        print((VeraCompilationUnitMember) obj);
    }

    public void visit(VeraUnsizedBitVectorType obj)
    {
        print(obj);
    }

    public void visit(VeraUserClass obj)
    {
        printClass(obj);
    }

    public void visit(VeraVariableReference obj)
    {
        print(obj);
    }

    public void visit(VeraVoidLiteral obj)
    {
        print(obj);
    }

    public void visit(VeraVoidType obj)
    {
        print(obj);
    }

    public void visit(VeraWhileStatement obj)
    {
        print(obj);
    }

    public void visit(VeraWildEqual obj)
    {
        print(obj);
    }

    public void visit(VeraWildNotEqual obj)
    {
        print(obj);
    }

    public void visit(VeraXorReduction obj)
    {
        print(obj);
    }
}
