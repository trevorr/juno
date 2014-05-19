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

package com.newisys.langsource.vera;

/**
 * Visitor over Vera source objects.
 * 
 * @author Trevor Robinson
 */
public interface VeraSourceVisitor
{
    void visit(ArrayAccessDecl obj);

    void visit(ArrayInitDecl obj);

    void visit(ArrayTypeRef obj);

    void visit(BindDecl obj);

    void visit(BindSignalDecl obj);

    void visit(BitSliceAccessDecl obj);

    void visit(BitVectorLiteralDecl obj);

    void visit(BitVectorTypeRef obj);

    void visit(BlockDecl obj);

    void visit(BreakDecl obj);

    void visit(BreakpointDecl obj);

    void visit(CaseDecl obj);

    void visit(CaseMemberDecl obj);

    void visit(ClassConstraintDecl obj);

    void visit(ClassDecl obj);

    void visit(ClassFuncDecl obj);

    void visit(ClassVarDecl obj);

    void visit(CompilationUnitDecl obj);

    void visit(ConstraintDecl obj);

    void visit(ContinueDecl obj);

    void visit(DefaultValueDecl obj);

    void visit(DefineDecl obj);

    void visit(DepthAccessDecl obj);

    void visit(DriveSampleDecl obj);

    void visit(EnumDecl obj);

    void visit(EnumElementDecl obj);

    void visit(ExpectDecl obj);

    void visit(ExpectExprDecl obj);

    void visit(ExpressionStatementDecl obj);

    void visit(ExtConstraintDecl obj);

    void visit(ExternVarDecl obj);

    void visit(FixedArrayTypeRef obj);

    void visit(ForDecl obj);

    void visit(ForkJoinDecl obj);

    void visit(FuncCallDecl obj);

    void visit(FuncDecl obj);

    void visit(GlobalVarDecl decl);

    void visit(HDLTaskDecl obj);

    void visit(IfElseConstraintDecl obj);

    void visit(IfElseDecl obj);

    void visit(ImplicationConstraintDecl obj);

    void visit(IntegerLiteralDecl obj);

    void visit(InterfaceDecl obj);

    void visit(LocalVarDecl obj);

    void visit(MemberAccessDecl obj);

    void visit(NewArrayDecl obj);

    void visit(NewCopyDecl obj);

    void visit(NewDecl obj);

    void visit(NullLiteralDecl obj);

    void visit(OperationDecl obj);

    void visit(ParamDecl obj);

    void visit(PortDecl obj);

    void visit(PragmaDecl obj);

    void visit(PrimitiveTypeRef obj);

    void visit(ProgramDecl obj);

    void visit(RandCaseDecl obj);

    void visit(RandCaseMemberDecl obj);

    void visit(RepeatDecl obj);

    void visit(ReturnDecl obj);

    void visit(SetOperationDecl obj);

    void visit(SignalDecl obj);

    void visit(SignalRangeDecl obj);

    void visit(SignalSkewDecl obj);

    void visit(StringLiteralDecl obj);

    void visit(SuperRefDecl obj);

    void visit(SyncDecl obj);

    void visit(TerminateDecl obj);

    void visit(ThisRefDecl obj);

    void visit(TypedefDecl obj);

    void visit(UDFFuncDecl obj);

    void visit(UserTypeRef obj);

    void visit(VarRefDecl obj);

    void visit(VoidLiteralDecl obj);

    void visit(WhileDecl obj);
}
