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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.JoinKind;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.langschema.vera.*;
import com.newisys.schemaanalyzer.juno.BlockAnalysis;
import com.newisys.verilog.EdgeSet;

/**
 * Schema translator for block members.
 * 
 * @author Trevor Robinson
 */
final class BlockMemberTranslator
    extends TranslatorModule
    implements VeraBlockMemberVisitor
{
    private final JavaBlock block;
    private final TempBlockScope tempScope;
    private VarInfoMap varInfoMap;
    private final JavaLocalVariable returnVar;
    private final JavaRawAbstractClass containingClass;
    private final JavaClassMember assocMember;
    private final BlockMemberTranslator outerXlat;

    private boolean transformSuperNewCall;
    private boolean translatingLoop;
    private boolean translatingSwitch;
    private JavaLabeledStatement loopLabelStmt;

    public BlockMemberTranslator(
        TranslatorModule xlatContext,
        JavaBlock block,
        TempBlockScope tempScope,
        VarInfoMap varInfoMap,
        JavaLocalVariable returnVar,
        JavaRawAbstractClass containingClass,
        JavaClassMember assocMember)
    {
        super(xlatContext);
        this.block = block;
        this.tempScope = tempScope;
        this.varInfoMap = varInfoMap;
        this.returnVar = returnVar;
        this.containingClass = containingClass;
        this.assocMember = assocMember;
        this.outerXlat = null;
    }

    private BlockMemberTranslator(
        BlockMemberTranslator outerXlat,
        JavaBlock block,
        TempBlockScope tempScope,
        VarInfoMap varInfoMap,
        JavaLocalVariable returnVar,
        JavaRawAbstractClass containingClass,
        JavaClassMember assocMember)
    {
        super(outerXlat);
        this.block = block;
        this.tempScope = tempScope;
        this.varInfoMap = varInfoMap;
        this.returnVar = returnVar;
        this.containingClass = containingClass;
        this.assocMember = assocMember;
        this.outerXlat = outerXlat;
    }

    public void setTransformSuperNewCall(boolean transformSuperNewCall)
    {
        this.transformSuperNewCall = transformSuperNewCall;
    }

    public void translateBlockMembers(VeraBlock veraBlock)
    {
        final List members = veraBlock.getMembers();
        final Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            VeraBlockMember member = (VeraBlockMember) iter.next();
            member.accept(this);
        }
    }

    private JavaBlock translateBlock(VeraBlock veraBlock)
    {
        final JavaBlock subBlock = new JavaBlock(schema);
        subBlock.addAnnotations(veraBlock.getAnnotations());

        final BlockMemberTranslator subXlat = new BlockMemberTranslator(this,
            subBlock, new TempBlockScope(tempScope), varInfoMap, returnVar,
            containingClass, assocMember);
        subXlat.translateBlockMembers(veraBlock);

        return subBlock;
    }

    private JavaStatement translateStatement(VeraStatement veraStmt)
    {
        final JavaBlock subBlock = new JavaBlock(schema);

        translateStatementInto(veraStmt, subBlock, varInfoMap, containingClass);

        if (!(veraStmt instanceof VeraBlock))
        {
            List members = subBlock.getMembers();
            if (members.size() == 1)
            {
                Object member = members.get(0);
                if (member instanceof JavaStatement)
                {
                    return (JavaStatement) member;
                }
            }
        }
        return subBlock;
    }

    private void translateStatementInto(
        VeraStatement veraStmt,
        JavaBlock block,
        VarInfoMap varInfoMap,
        JavaRawAbstractClass containingClass)
    {
        final BlockMemberTranslator subXlat = new BlockMemberTranslator(this,
            block, new TempBlockScope(tempScope), varInfoMap, returnVar,
            containingClass, assocMember);
        if (veraStmt instanceof VeraBlock)
        {
            subXlat.translateBlockMembers((VeraBlock) veraStmt);
        }
        else
        {
            veraStmt.accept((VeraStatementVisitor) subXlat);
        }
    }

    private ConvertedExpression translateExpr(
        VeraExpression veraExpr,
        JavaType desiredResultType)
    {
        return translateExpr(veraExpr, tempScope, containingClass, varInfoMap,
            returnVar, null, desiredResultType);
    }

    private JavaExpression translateIntExpr(
        VeraExpression veraExpr,
        String tempID)
    {
        ConvertedExpression convExpr = translateExpr(veraExpr, schema.intType);
        convExpr.convertResultExpr(schema.intType, exprConv);
        return convExpr.toBlockExpr(block, tempID);
    }

    private ConvertedExpression translateCondition(VeraExpression veraCond)
    {
        ConvertedExpression condInfo = translateExpr(veraCond,
            schema.booleanType);
        condInfo.setResultExpr(exprConv.toBoolean(condInfo.getResultExpr(),
            false, false));
        return condInfo;
    }

    public void visit(VeraLocalVariable obj)
    {
        translateLocalVariable(obj, false, containingClass, assocMember, block,
            tempScope, returnVar, varInfoMap);
    }

    public void visit(VeraBlock obj)
    {
        JavaBlock subBlock = translateBlock(obj);

        // remove unnecessary sub-blocks containing only statements
        // (i.e. no local variable declarations or classes)
        boolean containsNonStmt = false;
        List members = subBlock.getMembers();
        for (Iterator iter = members.iterator(); iter.hasNext();)
        {
            if (!(iter.next() instanceof JavaStatement))
            {
                containsNonStmt = true;
                break;
            }
        }
        if (!containsNonStmt)
        {
            // transfer any annotations from block to first member
            List<Annotation> annotations = subBlock.getAnnotations();
            if (!annotations.isEmpty() && !members.isEmpty())
            {
                JavaBlockMember member0 = (JavaBlockMember) members.get(0);
                member0.addAnnotations(0, annotations);
            }

            // add all sub-block members directly to containing block
            block.addMembers(members);
        }
        else
        {
            block.addMember(subBlock);
        }
    }

    public void visit(VeraBreakpointStatement obj)
    {
        // TODO: translate VeraBreakpointStatement
    }

    public void visit(VeraBreakStatement obj)
    {
        JavaLabeledStatement target = null;
        BlockMemberTranslator cur = outerXlat;
        boolean inSwitch = false;
        while (cur != null)
        {
            if (cur.translatingLoop)
            {
                if (inSwitch)
                {
                    // intervening switch statement; generate labeled break
                    target = cur.loopLabelStmt;
                    if (target == null)
                    {
                        target = new JavaLabeledStatement(schema, VarBuilder
                            .uniquifyID(tempScope, "loop", JavaNameKind.LABEL));
                        cur.loopLabelStmt = target;
                    }
                }
                else
                {
                    // no intervening switch statement; generate simple break
                }
                break;
            }
            else if (cur.translatingSwitch)
            {
                inSwitch = true;
            }
            cur = cur.outerXlat;
        }
        JavaBreakStatement breakStmt = new JavaBreakStatement(schema, target);
        breakStmt.addAnnotations(obj.getAnnotations());
        block.addMember(breakStmt);
    }

    public void visit(VeraContinueStatement obj)
    {
        JavaContinueStatement contStmt = new JavaContinueStatement(schema);
        contStmt.addAnnotations(obj.getAnnotations());
        block.addMember(contStmt);
    }

    public void visit(VeraDriveSampleStatement obj)
    {
        // translate destination
        VeraExpression veraDest = obj.getDestination();
        final JavaExpression highExpr, lowExpr;
        if (veraDest instanceof VeraArrayAccess)
        {
            VeraArrayAccess aa = (VeraArrayAccess) veraDest;
            veraDest = aa.getArray();
            List indices = aa.getIndices();
            assert (indices.size() == 1);
            VeraExpression veraIndex = (VeraExpression) indices.get(0);
            JavaExpression indexExpr = translateIntExpr(veraIndex, "index");
            // FIXME: translate index as eval-once
            highExpr = indexExpr;
            lowExpr = highExpr;
        }
        else if (veraDest instanceof VeraBitSliceAccess)
        {
            VeraBitSliceAccess bsa = (VeraBitSliceAccess) veraDest;
            veraDest = bsa.getArray();
            VeraRange range = bsa.getRange();
            VeraExpression veraHigh = range.getFrom();
            VeraExpression veraLow = range.getTo();
            highExpr = translateIntExpr(veraHigh, "high");
            lowExpr = translateIntExpr(veraLow, "low");
        }
        else
        {
            highExpr = null;
            lowExpr = null;
        }
        JavaExpression destExpr = translateExpr(veraDest,
            types.outputSignalType).toBlockExpr(block, "dest");
        boolean isDrive = exprConv.isOutputSignal(destExpr.getResultType());

        VeraExpression veraValue = obj.getSource();
        final JavaExpression stmtExpr;
        if (veraValue instanceof VeraVoidLiteral)
        {
            if (isDrive)
            {
                // void drive
                VeraExpression veraDelay = obj.getDelay();
                if (veraDelay != null)
                {
                    stmtExpr = ExpressionBuilder.memberCall(destExpr,
                        "syncDriveDelay", translateIntExpr(veraDelay, "delay"));
                }
                else
                {
                    stmtExpr = ExpressionBuilder.memberCall(destExpr,
                        "syncDrive");
                }
            }
            else
            {
                // void assignment; ignored
                stmtExpr = null;
            }
        }
        else
        {
            if (isDrive)
            {
                // build drive method name and argument list
                StringBuffer methodID = new StringBuffer(30);
                LinkedList<JavaExpression> argList = new LinkedList<JavaExpression>();
                methodID.append("drive");
                if (highExpr != null)
                {
                    methodID.append("Range");
                    argList.add(highExpr);
                    argList.add(lowExpr);
                }
                if (obj.isAsync())
                {
                    methodID.append("Async");
                }
                else
                {
                    VeraExpression veraDelay = obj.getDelay();
                    if (veraDelay != null)
                    {
                        methodID.append("Delay");
                        argList.addFirst(translateIntExpr(veraDelay, "delay"));
                    }
                }
                if (obj.isSoft())
                {
                    methodID.append("Soft");
                }
                if (obj.isNonBlocking() && !obj.isAsync())
                {
                    methodID.append("NB");
                }

                // translate value
                JavaExpression valueExpr = translateExpr(veraValue, null)
                    .toBlockExpr(block, "value");
                argList.add(exprConv.toObject(valueExpr));

                // build drive call
                JavaExpression[] argArray = new JavaExpression[argList.size()];
                argList.toArray(argArray);
                stmtExpr = ExpressionBuilder.memberCall(destExpr, methodID
                    .toString(), argArray, null);
            }
            else
            {
                // build sample assignment
                ConvertedExpression result = new ConvertedExpression(schema,
                    tempScope);
                ExpressionTranslator exprXlat = new ExpressionTranslator(this,
                    containingClass, varInfoMap, returnVar, null,
                    schema.voidType, result);
                exprXlat.setAssignSampleAsync(obj.isAsync());
                exprXlat.buildAssignOp(veraDest, veraValue);
                stmtExpr = result.toBlockExpr(block, null);
            }
        }

        if (stmtExpr != null)
        {
            JavaExpressionStatement stmt = new JavaExpressionStatement(stmtExpr);
            stmt.addAnnotations(obj.getAnnotations());
            block.addMember(stmt);
        }
    }

    public void visit(VeraExpectStatement obj)
    {
        // TODO: only simple expects are supported
        assert (obj.getExpectKind() == VeraExpectKind.SIMPLE);

        // TODO: expect window is not supported
        assert (obj.getWindow() == null);

        // TODO: async expect is not supported
        assert (!obj.isAsync());

        // TODO: only single-term expects are supported
        final List terms = obj.getExpectTerms();
        assert (terms.size() == 1);
        final VeraExpectTerm term = (VeraExpectTerm) terms.get(0);

        // TODO: only void expects are supported
        assert (term.getValue() instanceof VeraVoidLiteral);
        assert (term.isEqual());

        // translate signal expression of expect term
        final JavaExpression signalExpr = translateExpr(term.getSignal(),
            types.inputSignalType).toBlockExpr(block, "signal");
        // TODO: signal required on LHS of expect term?
        assert (exprConv.isInputSignal(signalExpr.getResultType()));

        // void expect
        final JavaExpression stmtExpr;
        final VeraExpression veraDelay = obj.getDelay();
        if (veraDelay != null)
        {
            stmtExpr = ExpressionBuilder.memberCall(signalExpr,
                "syncSampleDelay", translateIntExpr(veraDelay, "delay"));
        }
        else
        {
            stmtExpr = ExpressionBuilder.memberCall(signalExpr, "syncSample");
        }
        JavaExpressionStatement stmt = new JavaExpressionStatement(stmtExpr);
        stmt.addAnnotations(obj.getAnnotations());
        block.addMember(stmt);
    }

    public void visit(VeraExpressionStatement obj)
    {
        VeraExpression veraExpr = obj.getExpression();
        ConvertedExpression convExpr = translateExpr(veraExpr, schema.voidType);
        JavaExpression expr = convExpr.getResultExpr();

        // is super() call?
        JavaBlockMember stmt;
        if (expr instanceof JavaConstructorInvocation)
        {
            // need to transform super() call?
            if (transformSuperNewCall)
            {
                JavaConstructorInvocation ctorCallExpr = (JavaConstructorInvocation) expr;
                List<JavaExpression> callArgs = ctorCallExpr.getArguments();

                // ignore no-arg super() call
                if (callArgs.size() == 0) return;

                // call super.init(...)
                JavaSuperReference superRef = new JavaSuperReference(
                    containingClass);
                expr = ExpressionBuilder.memberCall(superRef, "init",
                    getArgArray(callArgs), containingClass);
                stmt = new JavaExpressionStatement(expr);
                block.addMember(stmt);
            }
            else
            {
                // make sure super() call goes at top of block
                assert (!convExpr.hasInitMembers());
                stmt = new JavaExpressionStatement(expr);
                block.addMemberAt(stmt, 0);
            }
        }
        else
        {
            stmt = convExpr.toBlockStmt(block);
        }
        if (stmt != null)
        {
            stmt.addAnnotations(obj.getAnnotations());
        }
    }

    public void visit(VeraForkStatement obj)
    {
        JavaVariableReference simRef = new JavaVariableReference(types.dvType
            .getField("simulation"));

        // create final copies of shadow variables
        VarInfoMap outerVarInfoMap = new VarInfoMap(varInfoMap);
        BlockAnalysis forkAnalysis = analyzer.getBlockAnalysis(obj);
        if (forkAnalysis != null)
        {
            // iterate local variables referenced in fork
            Iterator varIter = forkAnalysis.getLocalRefs().iterator();
            while (varIter.hasNext())
            {
                VeraVariable veraVar = (VeraVariable) varIter.next();
                JavaVariable var = translateVariable(veraVar);
                VarInfo varInfo = varInfoMap != null ? varInfoMap.getInfo(var)
                    : null;
                boolean varIsHolder = varInfo != null && varInfo.isHolderVar();
                JavaType varType = var.getType();
                boolean needsClone = false;
                if (veraVar.hasModifier(VeraVariableModifier.SHADOW)
                    && (!var.hasModifier(JavaVariableModifier.FINAL)
                        || (needsClone = needsClone(varType)) || varIsHolder))
                {
                    // for shadow variable "Foo foo", create temporary:
                    // final Foo forked_foo = foo;
                    // use reference to determine type, in case of holder vars
                    JavaExpression varRef = ExpressionTranslator
                        .translateVarRef(veraVar, var, schema, outerVarInfoMap,
                            null);
                    JavaType varRefType = varRef.getResultType();
                    String id = var.getName().getIdentifier();
                    id = id.replace("_holder", "");
                    JavaLocalVariable forkVar = VarBuilder.createLocalVar(
                        tempScope, "forked_" + id, varRefType);
                    tempScope.addObject(forkVar);
                    forkVar.addModifier(JavaVariableModifier.FINAL);
                    if (needsClone) varRef = getCloneExpr(varRef);
                    forkVar.setInitializer(varRef);
                    block.addMember(forkVar);

                    // translate variable references to forked variable
                    outerVarInfoMap.addInfo(new VarInfo(schema, veraVar,
                        forkVar, false));
                }
            }
        }

        // translate Vera fork statements into DVSimulation.fork() calls
        List<JavaExpression> forkCalls = new LinkedList<JavaExpression>();
        JavaType threadType = null;
        List veraForkStmts = obj.getForkedStatements();
        Iterator iter = veraForkStmts.iterator();
        while (iter.hasNext())
        {
            VeraStatement veraStmt = (VeraStatement) iter.next();

            // create anonymous Runnable class
            JavaRawClass anonClass = new JavaRawClass(containingClass);
            anonClass.setBaseClass(types.objectType);
            anonClass.addBaseInterface(types.runnableType);

            // implement Runnable.run()
            JavaFunction runMethod = anonClass
                .newMethod("run", schema.voidType);
            runMethod.setVisibility(JavaVisibility.PUBLIC);
            JavaBlock body = new JavaBlock(schema);

            // create non-final copies of written shadow variables
            VarInfoMap innerVarInfoMap = new VarInfoMap(outerVarInfoMap);
            BlockAnalysis forkStmtAnalysis = analyzer
                .getBlockAnalysis(veraStmt);
            if (forkStmtAnalysis != null)
            {
                // iterate local variables referenced in forked statement
                Iterator varIter = forkStmtAnalysis.getLocalRefs().iterator();
                while (varIter.hasNext())
                {
                    VeraVariable veraVar = (VeraVariable) varIter.next();
                    JavaVariable var = translateVariable(veraVar);
                    JavaType varType = var.getType();
                    // duplicate shadow variables that are written but have not
                    // been cloned for the forked statement
                    if (veraVar.hasModifier(VeraVariableModifier.SHADOW)
                        && forkStmtAnalysis.isLocalWrite(veraVar)
                        && !needsClone(varType))
                    {
                        // for shadow variable "forked_foo", create temporary:
                        // Foo foo = forked_foo;
                        // use reference to determine type, in case of holder
                        // vars
                        JavaExpression forkVarRef = ExpressionTranslator
                            .translateVarRef(veraVar, var, schema,
                                outerVarInfoMap, null);
                        JavaType forkVarRefType = forkVarRef.getResultType();
                        String id = var.getName().getIdentifier();
                        id = id.replace("_holder", "");
                        JavaLocalVariable shadowVar = VarBuilder
                            .createLocalVar(tempScope, id, forkVarRefType);
                        tempScope.addObject(shadowVar);
                        shadowVar.setInitializer(forkVarRef);
                        body.addMember(shadowVar);

                        // translate variable references to forked variable
                        innerVarInfoMap.addInfo(new VarInfo(schema, veraVar,
                            shadowVar, false));
                    }
                }
            }

            // translated Vera statement into run() body
            translateStatementInto(veraStmt, body, innerVarInfoMap, anonClass);
            body = checkThreadContext(body, forkStmtAnalysis);
            runMethod.setBody(body);

            // instantiate the anonymous class
            JavaInstanceCreation newExpr = new JavaInstanceCreation(
                types.runnableType, null);
            newExpr.setAnonymousClass(anonClass);

            // generate fork call: Vera.fork("func", new Runnable() {})
            JavaExpression forkCall = ExpressionBuilder.staticCall(
                types.junoType, "fork", new JavaStringLiteral(schema,
                    describeClassMember(assocMember)), newExpr);
            forkCalls.add(forkCall);

            // get returned thread type on first fork
            if (threadType == null)
            {
                threadType = forkCall.getResultType();
            }
        }

        JavaStatement stmt = null;
        JoinKind joinKind = obj.getJoinKind();
        if (joinKind == JoinKind.NONE)
        {
            // not joined; add fork calls directly to block
            iter = forkCalls.iterator();
            while (iter.hasNext())
            {
                JavaExpression forkCall = (JavaExpression) iter.next();
                JavaStatement forkStmt = new JavaExpressionStatement(forkCall);
                block.addMember(forkStmt);
                if (stmt == null) stmt = forkStmt;
            }
        }
        else
        {
            // build array initializer with fork calls
            JavaArrayType threadArrayType = schema.getArrayType(threadType, 1);
            JavaArrayInitializer arrayInit = new JavaArrayInitializer(
                threadArrayType);
            iter = forkCalls.iterator();
            while (iter.hasNext())
            {
                JavaExpression forkCall = (JavaExpression) iter.next();
                arrayInit.addElement(forkCall);
            }

            // create new array of threads
            JavaArrayCreation arrayNew = new JavaArrayCreation(threadArrayType);
            arrayNew.setInitializer(arrayInit);

            // generate join call
            String joinMethodID = (joinKind == JoinKind.ANY) ? "joinAny"
                : "joinAll";
            JavaExpression joinCall = ExpressionBuilder.memberCall(simRef,
                joinMethodID, arrayNew);
            stmt = new JavaExpressionStatement(joinCall);
            block.addMember(stmt);
        }
        stmt.addAnnotations(obj.getAnnotations());
    }

    public void visit(VeraForStatement obj)
    {
        translatingLoop = true;

        // translate init statements
        List veraInitStmts = obj.getInitStatements();
        JavaBlock initBlock = new JavaBlock(schema);
        boolean gotInitOrUpdate = processForStmts(veraInitStmts, initBlock);

        // translate condition expression
        VeraExpression veraCond = (VeraExpression) obj.getCondition();
        ConvertedExpression condInfo = null;
        if (veraCond != null)
        {
            condInfo = translateCondition(veraCond);
            gotInitOrUpdate |= condInfo.hasInitExprs()
                || condInfo.hasUpdateMembers();
        }

        // translate update statements
        List veraUpdateStmts = obj.getUpdateStatements();
        JavaBlock updateBlock = new JavaBlock(schema);
        gotInitOrUpdate |= processForStmts(veraUpdateStmts, updateBlock);

        VeraStatement veraStmt = obj.getStatement();

        // if any statements or expressions have initialization or update
        // statements, for-statement must be transformed to a
        // while(true)-break-statement
        JavaStatement loopStmt;
        if (gotInitOrUpdate)
        {
            // append init members to containing block
            block.addMembers(initBlock.getMembers());

            // create statement block for while-loop
            JavaBlock stmtBlock = new JavaBlock(schema);

            // append guard statement to while-block
            if (condInfo != null)
            {
                JavaExpression guardExpr = condInfo.toBlockExpr(stmtBlock,
                    "guard");
                stmtBlock.addMember(new JavaIfStatement(getNotExpr(guardExpr),
                    new JavaBreakStatement(schema)));
            }

            // translate Vera statement into while-block
            translateStatementInto(veraStmt, stmtBlock, varInfoMap,
                containingClass);

            // append update members to while-block
            stmtBlock.addMembers(updateBlock.getMembers());

            // append while-statement to containing block
            JavaExpression condExpr = new JavaBooleanLiteral(schema, true);
            loopStmt = new JavaWhileStatement(condExpr, stmtBlock);
        }
        else
        {
            // append for-statement to containing block
            JavaForStatement forStmt = new JavaForStatement(schema);
            forStmt.addInitStatements(initBlock.getMembers());
            if (condInfo != null)
            {
                forStmt.setCondition(condInfo.toBlockExpr(block, "guard"));
            }
            for (JavaBlockMember updateMember : updateBlock.getMembers())
            {
                forStmt
                    .addUpdateStatement((JavaExpressionStatement) updateMember);
            }
            forStmt.setStatement(translateStatement(veraStmt));
            loopStmt = forStmt;
        }

        // put loop statement in labeled statement if necessary
        if (loopLabelStmt != null)
        {
            loopLabelStmt.setStatement(loopStmt);
            loopStmt = loopLabelStmt;
        }

        loopStmt.addAnnotations(obj.getAnnotations());

        // append loop to containing block
        block.addMember(loopStmt);

        translatingLoop = false;
    }

    private boolean processForStmts(List veraStmts, JavaBlock tempBlock)
    {
        boolean gotInitOrUpdate = false;
        Iterator iter = veraStmts.iterator();
        while (iter.hasNext())
        {
            VeraExpressionStatement veraStmt = (VeraExpressionStatement) iter
                .next();
            VeraExpression veraExpr = veraStmt.getExpression();
            ConvertedExpression exprInfo = translateExpr(veraExpr, tempScope,
                containingClass, varInfoMap, returnVar, null, schema.voidType);
            gotInitOrUpdate |= exprInfo.hasInitExprs()
                || exprInfo.hasUpdateMembers();
            tempBlock.addMembers(exprInfo.getInitMembers());
            tempBlock.addMember(new JavaExpressionStatement(exprInfo
                .getResultExpr()));
            tempBlock.addMembers(exprInfo.getUpdateMembers());
        }
        return gotInitOrUpdate;
    }

    public void visit(VeraIfStatement obj)
    {
        // translate condition
        VeraExpression veraCond = obj.getCondition();
        ConvertedExpression condInfo = translateCondition(veraCond);
        JavaExpression condExpr = condInfo.toBlockExpr(block, "guard");

        // translate then-statement
        VeraStatement veraThen = obj.getThenStatement();
        JavaStatement thenStmt = translateStatement(veraThen);

        // translate else-statement (if present)
        VeraStatement veraElse = obj.getElseStatement();
        JavaStatement elseStmt = (veraElse != null)
            ? translateStatement(veraElse) : null;

        // append if-statement to containing block
        JavaIfStatement stmt = new JavaIfStatement(condExpr, thenStmt, elseStmt);
        stmt.addAnnotations(obj.getAnnotations());
        block.addMember(stmt);
    }

    public void visit(VeraRandCaseStatement obj)
    {
        translatingSwitch = true;

        // create temp variable containing randcase weights
        JavaArrayType intArrayType = schema.getArrayType(schema.intType, 1);
        JavaLocalVariable weightsVar = VarBuilder.createLocalVar(tempScope,
            "weights", intArrayType);
        tempScope.addObject(weightsVar);
        weightsVar.addModifier(JavaVariableModifier.FINAL);
        JavaArrayInitializer initExpr = new JavaArrayInitializer(intArrayType);
        weightsVar.setInitializer(initExpr);

        // create call to Vera.randcase()
        JavaFunctionReference funcRef = new JavaFunctionReference(
            types.junoType.getMethod("randcase",
                new JavaType[] { intArrayType }));
        JavaFunctionInvocation funcCall = new JavaFunctionInvocation(funcRef);
        funcCall.addArgument(new JavaVariableReference(weightsVar));

        // create switch statement to decode randcase index
        JavaSwitchStatement switchStmt = new JavaSwitchStatement(funcCall);
        switchStmt.addAnnotations(obj.getAnnotations());

        // process cases into array initializer and switch statement
        List cases = obj.getCaseList();
        Iterator iter = cases.iterator();
        int index = 0;
        while (iter.hasNext())
        {
            VeraRandCase randCase = (VeraRandCase) iter.next();

            // translate weight
            ConvertedExpression weightInfo = translateExpr(
                randCase.getWeight(), schema.intType);
            weightInfo.convertResultExpr(schema.intType, exprConv);
            JavaExpression weightExpr = weightInfo.toBlockExpr(block, "weight");
            initExpr.addElement(weightExpr);

            // translate statement
            JavaStatement stmt = translateStatement(randCase.getStatement());

            // create switch case
            if (iter.hasNext())
            {
                JavaSwitchValueCase _case = switchStmt.newValueCase();
                _case.addValue(new JavaIntLiteral(schema, index++));
                _case.addMember(stmt);
                _case.addMember(new JavaBreakStatement(schema));
            }
            else
            {
                // make last case default for DA/DU analysis
                JavaSwitchCase _case = switchStmt.newDefaultCase();
                _case.addMember(stmt);
            }
        }

        // add variable and switch to block (after any weight inits/updates)
        block.addMember(weightsVar);
        block.addMember(switchStmt);

        translatingSwitch = false;
    }

    public void visit(VeraRepeatStatement obj)
    {
        translatingLoop = true;

        // translate condition
        VeraExpression veraCond = obj.getCondition();
        ConvertedExpression condInfo = translateExpr(veraCond, schema.intType);
        condInfo.convertResultExpr(schema.intType, exprConv);
        JavaExpression condExpr = condInfo.toEvalOnceExpr(block, "count");

        // create loop variable
        JavaLocalVariable loopVar = VarBuilder.createLocalVar(tempScope, "i",
            schema.intType);
        tempScope.addObject(loopVar);
        loopVar.setInitializer(new JavaIntLiteral(schema, 0));
        JavaVariableReference loopVarRef = new JavaVariableReference(loopVar);

        // translate statement
        VeraStatement veraStmt = obj.getStatement();
        JavaStatement stmt = translateStatement(veraStmt);

        // build for-loop
        JavaForStatement forStmt = new JavaForStatement(schema);
        forStmt.addInitStatement(loopVar);
        forStmt.setCondition(new JavaLess(schema, loopVarRef, condExpr));
        forStmt.addUpdateStatement(new JavaExpressionStatement(
            new JavaPreIncrement(schema, loopVarRef)));
        forStmt.setStatement(stmt);

        // put loop statement in labeled statement if necessary
        JavaStatement loopStmt = forStmt;
        if (loopLabelStmt != null)
        {
            loopLabelStmt.setStatement(loopStmt);
            loopStmt = loopLabelStmt;
        }

        loopStmt.addAnnotations(obj.getAnnotations());

        // append for-loop to containing block
        block.addMember(loopStmt);

        translatingLoop = false;
    }

    public void visit(VeraReturnStatement obj)
    {
        JavaType returnType = null;
        if (returnVar != null)
        {
            JavaFunction func = (JavaFunction) assocMember;
            returnType = func.getType().getReturnType();
        }
        JavaReturnStatement stmt = addReturnStatement(block, returnVar,
            returnType, varInfoMap);
        stmt.addAnnotations(obj.getAnnotations());
    }

    public void visit(VeraSwitchStatement obj)
    {
        // translate selector expression
        VeraExpression veraSel = obj.getSelector();
        ConvertedExpression selInfo = translateExpr(veraSel, null);

        // translate cases into temporary CaseInfo list
        List<CaseInfo> caseInfos = new LinkedList<CaseInfo>();
        VeraCaseKind kind = obj.getCaseKind();
        boolean useJavaSwitch = (kind == VeraCaseKind.NORMAL);
        List veraCases = obj.getCases();
        Iterator caseIter = veraCases.iterator();
        while (caseIter.hasNext())
        {
            CaseInfo caseInfo = new CaseInfo();
            caseInfos.add(caseInfo);

            VeraSwitchCase _case = (VeraSwitchCase) caseIter.next();
            if (_case instanceof VeraSwitchValueCase)
            {
                // translate case value expressions
                VeraSwitchValueCase valueCase = (VeraSwitchValueCase) _case;
                List veraValues = valueCase.getValues();
                Iterator valueIter = veraValues.iterator();
                while (valueIter.hasNext())
                {
                    // translate value expression
                    VeraExpression veraValue = (VeraExpression) valueIter
                        .next();
                    ConvertedExpression valueInfo = translateExpr(veraValue,
                        null);
                    caseInfo.valueInfos.add(valueInfo);

                    // cannot use Java switch if value has initialization or
                    // update statements
                    useJavaSwitch &= !valueInfo.hasInitExprs()
                        && !valueInfo.hasUpdateMembers();
                    if (useJavaSwitch)
                    {
                        // cannot use Java switch if value is not a constant
                        // int/char/short/byte expression
                        JavaExpression value = valueInfo.getResultExpr();
                        useJavaSwitch &= JavaSwitchValueCase
                            .isValidValue(value);
                    }
                }
            }
            else
            {
                assert (_case instanceof VeraSwitchDefaultCase);
            }

            // get case statement (but wait to translate it)
            caseInfo.veraStmt = _case.getStatement();
        }

        // determine whether to use switch-statement or if-else chain
        if (useJavaSwitch)
        {
            translatingSwitch = true;

            JavaExpression selExpr = selInfo.toBlockExpr(block, "selector");

            // selector must be int, since translator does not generate
            // char/short/byte
            // FIXME: should select default case if selector is undefined;
            // toInt() generates code that will throw a RuntimeException
            selExpr = exprConv.toInt(selExpr);

            JavaSwitchStatement switchStmt = new JavaSwitchStatement(selExpr);
            switchStmt.addAnnotations(obj.getAnnotations());

            // generate switch cases for each CaseInfo
            caseIter = caseInfos.iterator();
            while (caseIter.hasNext())
            {
                CaseInfo caseInfo = (CaseInfo) caseIter.next();
                JavaSwitchCase _case;
                if (!caseInfo.valueInfos.isEmpty())
                {
                    // generate value cases
                    JavaSwitchValueCase valueCase = switchStmt.newValueCase();
                    Iterator valueIter = caseInfo.valueInfos.iterator();
                    while (valueIter.hasNext())
                    {
                        ConvertedExpression valueInfo = (ConvertedExpression) valueIter
                            .next();
                        JavaExpression valueExpr = valueInfo.toBlockExpr(block,
                            "value");
                        valueCase.addValue(valueExpr);
                    }
                    _case = valueCase;
                }
                else
                {
                    // default case; must be last case
                    assert (!caseIter.hasNext());
                    _case = switchStmt.newDefaultCase();
                }

                // add statement to case, followed by break-statement
                JavaStatement stmt = translateStatement(caseInfo.veraStmt);
                _case.addMember(stmt);
                if (canCompleteNormally(stmt))
                {
                    _case.addMember(new JavaBreakStatement(schema));
                }
            }

            block.addMember(switchStmt);

            translatingSwitch = false;
        }
        else
        {
            // get simple selector expression
            // FIXME: if the case values are primarily of a different type,
            // it might be best to convert the selector type
            JavaExpression selExpr = selInfo.toEvalOnceExpr(block, "selector");

            // determine operation to use in comparisons
            // FIXME: casex/casez both use =?=
            int opcode = (kind == VeraCaseKind.NORMAL)
                ? ExpressionTranslator.OP_EXACT_EQ
                : ExpressionTranslator.OP_WILD_EQ;

            // generate if-else statements for each CaseInfo
            JavaStatement switchStmt = null;
            JavaIfStatement prevIfStmt = null;
            caseIter = caseInfos.iterator();
            while (caseIter.hasNext())
            {
                JavaIfStatement ifStmt = null;
                JavaStatement caseStmt;
                CaseInfo caseInfo = (CaseInfo) caseIter.next();
                if (!caseInfo.valueInfos.isEmpty())
                {
                    // build equality test expression for this case:
                    // selector == value1 || selector == value2 || ...
                    JavaExpression testExpr = null;
                    Iterator valueIter = caseInfo.valueInfos.iterator();
                    while (valueIter.hasNext())
                    {
                        ConvertedExpression valueInfo = (ConvertedExpression) valueIter
                            .next();
                        // FIXME: init/update statements should probably not
                        // be added directly to the top-level block
                        JavaExpression valueExpr = valueInfo.toBlockExpr(block,
                            "value");
                        JavaExpression eqExpr = buildCaseEqualityExpr(selExpr,
                            valueExpr, opcode);
                        if (testExpr != null)
                        {
                            testExpr = new JavaConditionalOr(schema, testExpr,
                                eqExpr);
                        }
                        else
                        {
                            testExpr = eqExpr;
                        }
                    }

                    // build if-statement for this case
                    JavaStatement stmt = translateStatement(caseInfo.veraStmt);
                    ifStmt = new JavaIfStatement(testExpr, stmt);
                    caseStmt = ifStmt;
                }
                else
                {
                    // default case; must be last case
                    assert (!caseIter.hasNext());
                    caseStmt = translateStatement(caseInfo.veraStmt);
                }

                if (prevIfStmt != null)
                {
                    // chain this statement onto the previous statement
                    prevIfStmt.setElseStatement(caseStmt);
                }
                else
                {
                    // add first statement to block when done
                    switchStmt = caseStmt;
                }
                prevIfStmt = ifStmt;
            }

            assert (switchStmt != null);
            switchStmt.addAnnotations(obj.getAnnotations());
            block.addMember(switchStmt);
        }
    }

    private boolean canCompleteNormally(JavaStatement stmt)
    {
        // poor man's liveness checking
        if (stmt instanceof JavaBlock)
        {
            // does every statement in block complete normally?
            JavaBlock block = (JavaBlock) stmt;
            List members = block.getMembers();
            Iterator iter = members.iterator();
            while (iter.hasNext())
            {
                Object member = iter.next();
                if (member instanceof JavaStatement)
                {
                    JavaStatement memberStmt = (JavaStatement) member;
                    if (!canCompleteNormally(memberStmt)) return false;
                }
            }
            return true;
        }
        else if (stmt instanceof JavaBreakStatement
            || stmt instanceof JavaContinueStatement
            || stmt instanceof JavaReturnStatement)
        {
            // break, continue, return never complete normally
            return false;
        }
        else
        {
            return true;
        }
    }

    private JavaExpression buildCaseEqualityExpr(
        JavaExpression op1,
        JavaExpression op2,
        int opcode)
    {
        ConvertedExpression result = new ConvertedExpression(schema, tempScope);
        ExpressionTranslator exprXlat = new ExpressionTranslator(this,
            containingClass, varInfoMap, returnVar, null, schema.booleanType,
            result);
        exprXlat.buildEquality(op1, op2, opcode);
        result.convertResultExpr(schema.booleanType, exprConv);
        return result.toBlockExpr(block, "temp");
    }

    private static class CaseInfo
    {
        final List<ConvertedExpression> valueInfos = new LinkedList<ConvertedExpression>();
        VeraStatement veraStmt;
    }

    public void visit(VeraSyncStatement obj)
    {
        final JavaExpression syncExpr;
        final List terms = obj.getTerms();
        if (terms.size() == 1)
        {
            // build syncXXX() call for single signal
            final VeraSyncTerm term = (VeraSyncTerm) terms.get(0);
            syncExpr = getSyncExpr(term, false);
        }
        else
        {
            // build array expression of getXXXEvent() calls
            final JavaArrayInitializer arrayInit = new JavaArrayInitializer(
                types.eventArrayType);
            final Iterator iter = terms.iterator();
            while (iter.hasNext())
            {
                VeraSyncTerm term = (VeraSyncTerm) iter.next();
                arrayInit.addElement(getSyncExpr(term, true));
            }
            final JavaArrayCreation arrayNew = new JavaArrayCreation(
                types.eventArrayType);
            arrayNew.setInitializer(arrayInit);

            // wait on sync events
            final JavaVariableReference simRef = new JavaVariableReference(
                types.dvType.getField("simulation"));
            syncExpr = ExpressionBuilder.memberCall(simRef, "waitForAny",
                arrayNew);
        }
        JavaExpressionStatement stmt = new JavaExpressionStatement(syncExpr);
        stmt.addAnnotations(obj.getAnnotations());
        block.addMember(stmt);
    }

    private JavaExpression getSyncExpr(VeraSyncTerm term, boolean wantEvent)
    {
        // extract sync signal and bit range
        final VeraExpression veraSyncExpr = term.getSignal();
        final VeraExpression veraSignal;
        final VeraExpression veraHighBit, veraLowBit;
        if (veraSyncExpr instanceof VeraArrayAccess)
        {
            VeraArrayAccess aa = (VeraArrayAccess) veraSyncExpr;
            veraSignal = aa.getArray();
            List indices = aa.getIndices();
            assert (indices.size() == 1);
            veraHighBit = (VeraExpression) indices.get(0);
            veraLowBit = null;
        }
        else if (veraSyncExpr instanceof VeraBitSliceAccess)
        {
            VeraBitSliceAccess bsa = (VeraBitSliceAccess) veraSyncExpr;
            veraSignal = bsa.getArray();
            VeraRange range = bsa.getRange();
            veraHighBit = range.getFrom();
            veraLowBit = range.getTo();
        }
        else
        {
            veraSignal = veraSyncExpr;
            veraHighBit = null;
            veraLowBit = null;
        }

        // translate signal
        assert (veraSignal instanceof VeraMemberAccess
            || veraSignal instanceof VeraSignalReference || veraSignal instanceof VeraSystemClockReference);
        final JavaExpression signalExpr = translateExpr(veraSignal,
            types.inputSignalType).toBlockExpr(block, "signal");
        assert (exprConv.isInputSignal(signalExpr.getResultType()));

        // generate call to appropriate sync method
        final JavaExpression syncExpr;
        final EdgeSet edgeSet = term.getEdgeSet();
        boolean async = term.isAsync();
        if (edgeSet.equals(EdgeSet.ANYEDGE))
        {
            // generate value change mask, if any
            JavaExpression maskExpr = null;
            if (veraHighBit != null)
            {
                final JavaExpression highBitExpr = translateIntExpr(
                    veraHighBit, "high");
                if (veraLowBit != null)
                {
                    final JavaExpression lowBitExpr = translateIntExpr(
                        veraLowBit, "low");
                    maskExpr = ExpressionBuilder.staticCall(
                        types.bitVectorOpType, "getMask", highBitExpr,
                        lowBitExpr);
                }
                else
                {
                    maskExpr = ExpressionBuilder.staticCall(
                        types.bitVectorOpType, "getMask", highBitExpr);
                }
            }

            if (wantEvent)
            {
                if (maskExpr == null)
                {
                    maskExpr = new JavaNullLiteral(schema);
                }
                syncExpr = ExpressionBuilder.memberCall(signalExpr,
                    "getChangeEvent", maskExpr, new JavaBooleanLiteral(schema,
                        async));
            }
            else if (maskExpr != null)
            {
                syncExpr = ExpressionBuilder.memberCall(signalExpr, async
                    ? "syncChangeAsync" : "syncChange", maskExpr);
            }
            else
            {
                syncExpr = ExpressionBuilder.memberCall(signalExpr, async
                    ? "syncChangeAsync" : "syncChange");
            }
        }
        else
        {
            final JavaExpression edgeExpr = getEdgeExpr(edgeSet);

            // generate edge bit index, if any
            JavaExpression bitExpr = null;
            if (veraHighBit != null)
            {
                final JavaExpression highBitExpr = translateIntExpr(
                    veraHighBit, "high");
                if (veraLowBit != null)
                {
                    final JavaExpression lowBitExpr = translateIntExpr(
                        veraLowBit, "low");
                    bitExpr = ExpressionBuilder.staticCall(types.mathType,
                        "min", highBitExpr, lowBitExpr);
                }
                else
                {
                    bitExpr = highBitExpr;
                }
            }

            if (wantEvent)
            {
                if (bitExpr == null)
                {
                    bitExpr = new JavaIntLiteral(schema, 0);
                }
                syncExpr = ExpressionBuilder.memberCall(signalExpr,
                    "getEdgeEvent", new JavaExpression[] { edgeExpr, bitExpr,
                        new JavaBooleanLiteral(schema, async) }, null);
            }
            else if (bitExpr != null)
            {
                syncExpr = ExpressionBuilder.memberCall(signalExpr, async
                    ? "syncEdgeAsync" : "syncEdge", edgeExpr, bitExpr);
            }
            else
            {
                syncExpr = ExpressionBuilder.memberCall(signalExpr, async
                    ? "syncEdgeAsync" : "syncEdge", edgeExpr);
            }
        }
        return syncExpr;
    }

    public void visit(VeraTerminateStatement obj)
    {
        JavaVariableReference simRef = new JavaVariableReference(types.dvType
            .getField("simulation"));
        JavaExpression terminateCall = ExpressionBuilder.memberCall(simRef,
            "terminate");
        JavaExpressionStatement stmt = new JavaExpressionStatement(
            terminateCall);
        stmt.addAnnotations(obj.getAnnotations());
        block.addMember(stmt);
    }

    public void visit(VeraWhileStatement obj)
    {
        translatingLoop = true;

        // translate condition
        VeraExpression veraCond = obj.getCondition();
        ConvertedExpression condInfo = translateCondition(veraCond);

        VeraStatement veraStmt = obj.getStatement();

        // if condition has initialization or update statements, while(cond)
        // must be transformed to while(true) { if (!cond) break; }
        JavaExpression condExpr;
        JavaStatement stmt;
        if (condInfo.hasInitExprs() || condInfo.hasUpdateMembers())
        {
            // create statement block for while-loop
            JavaBlock stmtBlock = new JavaBlock(schema);

            // append guard statement to while-block
            JavaExpression guardExpr = condInfo.toBlockExpr(stmtBlock, "guard");
            stmtBlock.addMember(new JavaIfStatement(getNotExpr(guardExpr),
                new JavaBreakStatement(schema)));

            // translate Vera statement into while-block
            translateStatementInto(veraStmt, stmtBlock, varInfoMap,
                containingClass);

            // use true-literal for while-condition
            condExpr = new JavaBooleanLiteral(schema, true);
            stmt = stmtBlock;
        }
        else
        {
            // translate condition and statement literally
            condExpr = condInfo.toBlockExpr(block, "guard");
            stmt = translateStatement(veraStmt);
        }

        // put loop statement in labeled statement if necessary
        JavaStatement loopStmt = new JavaWhileStatement(condExpr, stmt);
        if (loopLabelStmt != null)
        {
            loopLabelStmt.setStatement(loopStmt);
            loopStmt = loopLabelStmt;
        }

        loopStmt.addAnnotations(obj.getAnnotations());

        // append while-loop to containing block
        block.addMember(loopStmt);

        translatingLoop = false;
    }
}
