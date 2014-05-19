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

package com.newisys.schemaanalyzer.juno;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.BlockMember;
import com.newisys.langschema.JoinKind;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.Statement;
import com.newisys.langschema.vera.*;

/**
 * Schema analyzer for block member objects.
 * 
 * @author Trevor Robinson
 */
final class BlockMemberAnalyzer
    extends AnalyzerModule
    implements VeraBlockMemberVisitor
{
    private static class BlockExit
    {
        final VeraStatement stmt;
        final DADUAnalysis dadu;

        public BlockExit(VeraStatement stmt, DADUAnalysis dadu)
        {
            this.stmt = stmt;
            this.dadu = dadu.duplicate();
        }
    }

    private final VeraCompilationUnit compUnit;
    private final VeraUserClass cls;
    private final SchemaObject context;
    private final boolean isCtor;
    private final BlockAnalysis analysis;
    private List<VariableAnalysis> localVarAnalyses;

    private List<BlockExit> curExitList = new LinkedList<BlockExit>();
    private boolean loopPassTwo = false;

    public BlockMemberAnalyzer(
        VeraSchemaAnalyzer analyzer,
        BlockAnalysis analysis,
        SchemaObject context,
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        boolean isCtor)
    {
        super(analyzer);
        this.compUnit = compUnit;
        this.cls = cls;
        this.context = context;
        this.isCtor = isCtor;
        this.analysis = analysis;
    }

    // used to create forked statement analyzers
    private BlockMemberAnalyzer(
        BlockMemberAnalyzer enclosing,
        BlockAnalysis analysis)
    {
        super(enclosing.analyzer);
        this.compUnit = enclosing.compUnit;
        this.cls = enclosing.cls;
        this.context = enclosing.context;
        this.isCtor = enclosing.isCtor;
        this.analysis = analysis;
    }

    private ExpressionAnalysis analyzeExpression(
        VeraExpression expr,
        AccessType accessType)
    {
        ExpressionAnalysis exprAnalysis = analyzeExpression(expr, accessType,
            compUnit, cls, context, analysis, analysis.dadu);
        analysis.dadu = exprAnalysis.daduUncond;
        return exprAnalysis;
    }

    private ExpressionAnalysis analyzeCondition(VeraExpression expr)
    {
        return analyzeCondition(expr, compUnit, cls, context, analysis,
            analysis.dadu);
    }

    private void recordExit(VeraStatement obj)
    {
        curExitList.add(new BlockExit(obj, analysis.dadu));
        analysis.dadu.markDead();
    }

    private void analyzeExits(Class stmtCls, List<BlockExit> enclosingExitList)
    {
        final Iterator<BlockExit> iter = curExitList.iterator();
        curExitList = enclosingExitList;
        while (iter.hasNext())
        {
            BlockExit exit = iter.next();
            if (stmtCls.isInstance(exit.stmt))
            {
                analysis.dadu.merge(exit.dadu);
            }
            else
            {
                curExitList.add(exit);
            }
        }
    }

    public void finalizeAnalysis()
    {
        // merge DA/DU states from any early returns
        analyzeExits(VeraReturnStatement.class, new LinkedList<BlockExit>());
        assert (curExitList.isEmpty());
    }

    public void visit(VeraLocalVariable obj)
    {
        ++analyzer.localVarCount;

        // create analysis object for this variable (to track context)
        VariableAnalysis varAnalysis = analyzer.getOrCreateVariableAnalysis(
            obj, context);
        localVarAnalyses.add(varAnalysis);

        // initialize DA/DU tracking for variable
        int daduIndex = analysis.dadu.alloc(obj);

        VeraExpression initExpr = obj.getInitializer();
        if (initExpr != null)
        {
            // analyze initializer
            ExpressionAnalysis exprAnalysis = analyzeExpression(initExpr,
                AccessType.READ);
            applyAssignAnalysis(varAnalysis, exprAnalysis);

            // mark variable as assigned after initializer
            analysis.dadu.markAssigned(daduIndex);

            // track initialization as write access
            varAnalysis.markWriteAccess();
            if (exprAnalysis.sideEffects) varAnalysis.markWriteSideEffects();
        }
        else
        {
            // treat fixed and associative arrays as always assigned
            VeraType type = obj.getType();
            if (type instanceof VeraFixedArrayType
                || type instanceof VeraAssocArrayType)
            {
                analysis.dadu.markAssigned(daduIndex);
            }
        }
    }

    public void visit(VeraBlock obj)
    {
        // save existing local variable list and create new one
        final List<VariableAnalysis> enclosingVarAnalyses = localVarAnalyses;
        localVarAnalyses = new LinkedList<VariableAnalysis>();

        // remember DA/DU variable index at start of block
        final int blockIndex = analysis.dadu.beginBlock();

        for (final VeraBlockMember member : obj.getMembers())
        {
            member.accept(this);
        }

        // restore DA/DU variable index
        analysis.dadu.endBlock(blockIndex);

        // mark local variables as out of scope
        finalizeLocalVars(localVarAnalyses);
        localVarAnalyses = enclosingVarAnalyses;
    }

    public void visit(VeraBreakpointStatement obj)
    {
        ++analyzer.statementCount;
    }

    public void visit(VeraBreakStatement obj)
    {
        ++analyzer.statementCount;

        recordExit(obj);
    }

    public void visit(VeraContinueStatement obj)
    {
        ++analyzer.statementCount;

        recordExit(obj);
    }

    public void visit(VeraDriveSampleStatement obj)
    {
        ++analyzer.statementCount;

        VeraExpression delayExpr = obj.getDelay();
        if (delayExpr != null)
        {
            analyzeExpression(delayExpr, AccessType.READ);
        }

        VeraExpression destExpr = obj.getDestination();
        analyzeExpression(destExpr, AccessType.WRITE);

        VeraExpression valueExpr = obj.getSource();
        ExpressionAnalysis exprAnalysis = analyzeExpression(valueExpr,
            AccessType.READ);
        applyAssignAnalysis(destExpr, exprAnalysis);
    }

    public void visit(VeraExpectStatement obj)
    {
        ++analyzer.statementCount;

        final VeraExpression delayExpr = obj.getDelay();
        if (delayExpr != null)
        {
            analyzeExpression(delayExpr, AccessType.READ);
        }

        final VeraExpression windowExpr = obj.getWindow();
        if (windowExpr != null)
        {
            analyzeExpression(windowExpr, AccessType.READ);
        }

        for (final VeraExpectTerm term : obj.getExpectTerms())
        {
            analyzeExpression(term.getSignal(), AccessType.READ);
            analyzeExpression(term.getValue(), AccessType.READ);
        }
    }

    public void visit(VeraExpressionStatement obj)
    {
        ++analyzer.statementCount;

        VeraExpression expr = obj.getExpression();
        if (expr instanceof VeraFunctionInvocation)
        {
            VeraFunctionInvocation callExpr = (VeraFunctionInvocation) expr;
            VeraExpression funcExpr = callExpr.getFunction();
            List<VeraExpression> actualArgs = callExpr.getArguments();
            if (isCtor && funcExpr instanceof VeraMemberAccess)
            {
                VeraMemberAccess memberExpr = (VeraMemberAccess) funcExpr;
                VeraExpression objExpr = memberExpr.getObject();
                VeraMemberFunction func = (VeraMemberFunction) memberExpr
                    .getMember();

                // call to super.new?
                if (objExpr instanceof VeraSuperReference
                    && func.isConstructor())
                {
                    // analyze argument expressions
                    ExpressionAnalysis exprAnalysis = new ExpressionAnalysis(
                        compUnit, cls, context, analysis, analysis.dadu);
                    analyzeInvokeArgs(actualArgs, func, exprAnalysis, false,
                        false);

                    // does any argument reference the instance or a local
                    // variable, or contain a construct requiring temporaries?
                    if (exprAnalysis.referencesInstance
                        || exprAnalysis.referencesLocalNonArg
                        || exprAnalysis.byRefArgument
                        || exprAnalysis.postIncDec)
                    {
                        // this class requires transformation of the super call
                        ClassAnalysis thisAnalysis = analyzer
                            .getClassAnalysis(cls);
                        analyzer.log
                            .println("Found complex super ctor call in "
                                + cls.getName());
                        thisAnalysis.transformSuperCall = true;

                        // any user base classes will need a default ctor
                        VeraClass baseCls = cls.getBaseClass();
                        while (baseCls instanceof VeraUserClass)
                        {
                            ClassAnalysis superAnalysis = analyzer
                                .getOrCreateClassAnalysis((VeraUserClass) baseCls);
                            if (superAnalysis.needDefaultCtor) break;
                            analyzer.log.incIndent();
                            analyzer.log
                                .println("Class requires default ctor: "
                                    + baseCls.getName());
                            analyzer.log.decIndent();
                            superAnalysis.needDefaultCtor = true;

                            baseCls = baseCls.getBaseClass();
                        }
                    }

                    // skip processing expression as a whole
                    return;
                }
            }
            else if (funcExpr instanceof VeraFunctionReference)
            {
                VeraFunctionReference funcRef = (VeraFunctionReference) funcExpr;
                VeraFunction func = funcRef.getFunction();
                String id = func.getName().getIdentifier();

                // call to wait_var?
                if (func instanceof VeraGlobalFunction && id.equals("wait_var"))
                {
                    // analyze argument expressions
                    ExpressionAnalysis exprAnalysis = new ExpressionAnalysis(
                        compUnit, cls, context, analysis, analysis.dadu);
                    for (final VeraExpression argExpr : actualArgs)
                    {
                        analyzeNestedExpression(argExpr, exprAnalysis,
                            AccessType.READ, false);

                        // mark variable as a wait_var target
                        VeraVariable var = getReferencedVariable(argExpr);
                        assert (var != null);
                        VariableAnalysis varAnalysis = analyzer
                            .getOrCreateVariableAnalysis(var, null);
                        varAnalysis.markWaitVar(analyzer);
                    }

                    // skip processing expression as a whole
                    return;
                }

                // call to wait_child?
                if (func instanceof VeraGlobalFunction
                    && id.equals("wait_child"))
                {
                    analyzer.log.println("Found wait_child() in "
                        + AnalyzerModule.getDescription(context));

                    // need to create thread context for enclosing block
                    analysis.setNeedThreadContext(true);

                    // skip processing expression as a whole
                    return;
                }
            }
        }
        analyzeExpression(expr, AccessType.READ);
    }

    public void visit(VeraForkStatement obj)
    {
        ++analyzer.statementCount;

        BlockAnalysis forkAnalysis = new BlockAnalysis(analysis);
        Statement searchStmt = obj.getContainingStatement();

        // DA/DU
        final DADUAnalysis daduBeforeFork = analysis.dadu;
        DADUAnalysis daduForks = null;

        final Iterator<VeraStatement> iter = obj.getForkedStatements()
            .iterator();
        assert (iter.hasNext());
        while (iter.hasNext())
        {
            VeraStatement stmt = iter.next();

            // DA/DU: V is [un]assigned before each forked statement of the
            // fork/join statement iff V is [un]assigned before the fork/join
            // statement.
            BlockAnalysis forkStmtAnalysis = new BlockAnalysis(forkAnalysis);
            forkStmtAnalysis.dadu = daduBeforeFork.duplicate();
            BlockMemberAnalyzer bma = new BlockMemberAnalyzer(this,
                forkStmtAnalysis);
            stmt.accept((VeraStatementVisitor) bma);

            filterForkAnalysis(forkStmtAnalysis, searchStmt, false);
            analyzer.setBlockAnalysis(stmt, forkStmtAnalysis);

            // DA/DU
            if (daduForks != null)
            {
                daduForks.merge(forkStmtAnalysis.dadu);
            }
            else
            {
                daduForks = forkStmtAnalysis.dadu;
            }
        }

        filterForkAnalysis(forkAnalysis, searchStmt, true);
        analyzer.setBlockAnalysis(obj, forkAnalysis);

        // DA/DU
        final JoinKind joinKind = obj.getJoinKind();
        if (joinKind == JoinKind.ALL)
        {
            // DA/DU: V is [un]assigned after fork/join all iff V is
            // [un]assigned after each forked statement.
            analysis.dadu = daduForks;
        }
        else if (joinKind == JoinKind.ANY)
        {
            // DA/DU: V is [un]assigned after fork/join any iff V is
            // [un]assigned before the fork and V is [un]assigned after each
            // forked statement.
            daduForks.merge(daduBeforeFork);
            analysis.dadu = daduForks;
        }
        else
        {
            // DA/DU: V is [un]assigned after fork/join none iff V is
            // [un]assigned before the fork.
            assert (joinKind == JoinKind.NONE);
            analysis.dadu = daduBeforeFork;
        }
    }

    private void filterForkAnalysis(
        BlockAnalysis forkAnalysis,
        Statement searchStmt,
        boolean logReferences)
    {
        Set<VeraVariable> localRefs = forkAnalysis.getLocalRefs();
        Iterator<VeraVariable> iter = localRefs.iterator();
        while (iter.hasNext())
        {
            VeraVariable var = iter.next();

            String varKind;
            if (var instanceof VeraLocalVariable)
            {
                // exclude local variables contained within forked statement
                VeraLocalVariable localVar = (VeraLocalVariable) var;
                if (!fromEnclosingStatement(localVar, searchStmt)
                    && !isReturnVar(localVar))
                {
                    iter.remove();
                    continue;
                }
                varKind = "local";
            }
            else if (var instanceof VeraFunctionArgument)
            {
                varKind = "argument";
            }
            else if (var instanceof VeraMemberVariable)
            {
                varKind = "field";
            }
            else
            {
                assert (var instanceof VeraGlobalVariable || var instanceof VeraBindVariable);
                varKind = "global";
            }

            // flag forked reads/writes in variable analysis
            VariableAnalysis varAnalysis = analyzer
                .getOrCreateVariableAnalysis(var, context);
            boolean isRead = forkAnalysis.isLocalRead(var);
            boolean isWrite = forkAnalysis.isLocalWrite(var);
            if (isRead) varAnalysis.markForkRead();
            if (isWrite)
            {
                // TODO: handle shadow variables properly; they should be cloned
                // when referenced, since they are really a separate variable
                // for the purposes of DA/DU and forked access analysis
                if (var.hasModifier(VeraVariableModifier.SHADOW))
                {
                    analyzer.log.println("Analysis of write to shadow variable "
                        + var.getName() + " in fork not fully supported");
                }
                varAnalysis.markForkWrite();
            }

            if (logReferences)
            {
                StringBuffer msg = new StringBuffer(80);
                msg.append("Fork in " + AnalyzerModule.getDescription(context)
                    + " references " + varKind + " ");
                msg.append(var.getName().getIdentifier());
                msg.append(" with ");
                if (isRead && isWrite)
                {
                    msg.append("read/write");
                }
                else if (isWrite)
                {
                    msg.append("write");
                }
                else
                {
                    msg.append("read");
                }
                msg.append(" access");
                analyzer.log.println(msg.toString());
            }
        }
    }

    private boolean isReturnVar(VeraLocalVariable var)
    {
        return context instanceof VeraFunction
            && var == ((VeraFunction) context).getReturnVar();
    }

    private static boolean fromEnclosingStatement(
        BlockMember member,
        Statement searchStmt)
    {
        Statement memberStmt = member.getContainingStatement();
        do
        {
            if (memberStmt == searchStmt) return true;
            searchStmt = searchStmt.getContainingStatement();
        }
        while (searchStmt != null);
        return false;
    }

    public void visit(VeraForStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: V is [un]assigned before the initialization part of the for
        // statement iff V is [un]assigned before the for statement.
        Iterator<VeraBlockMember> initIter = obj.getInitStatements().iterator();
        while (initIter.hasNext())
        {
            VeraStatement stmt = (VeraStatement) initIter.next();
            stmt.accept((VeraStatementVisitor) this);
        }

        List<BlockExit> enclosingExitList = curExitList;
        boolean enclosingLoopPassTwo = loopPassTwo;
        DADUAnalysis daduAfterCond = null;
        while (true)
        {
            curExitList = new LinkedList<BlockExit>();

            // DA/DU: V is definitely assigned before the condition part of the
            // for statement iff V is definitely assigned after the
            // initialization part of the for statement.
            // DA/DU: V is definitely unassigned before the condition part of
            // the for statement iff [a], [b], and [c].
            // [a] V is definitely unassigned after the initialization part of
            // the for statement.
            DADUAnalysis daduBeforeCond = null;
            if (!loopPassTwo)
            {
                daduBeforeCond = analysis.dadu.duplicate();
            }
            VeraExpression condExpr = (VeraExpression) obj.getCondition();
            if (condExpr != null)
            {
                ExpressionAnalysis condAnalysis = analyzeCondition(condExpr);
                if (daduAfterCond == null)
                {
                    daduAfterCond = condAnalysis.daduWhenFalse;
                }

                // DA/DU: [1] A condition expression is present and V is
                // [un]assigned after the condition expression when true.
                analysis.dadu = condAnalysis.daduWhenTrue;
            }
            else
            {
                if (daduAfterCond == null)
                {
                    daduAfterCond = analysis.dadu.duplicate();
                }
                daduAfterCond.markDead();

                // DA/DU: [2] No condition expression is present and V is
                // [un]assigned after the initialization part of the for
                // statement.
            }

            // DA/DU: V is [un]assigned before the contained statement iff
            // either [1] or [2].
            VeraStatement stmt = obj.getStatement();
            stmt.accept((VeraStatementVisitor) this);

            // DA/DU: [b] Assuming V is definitely unassigned before the
            // condition part of the for statement, V is definitely unassigned
            // after the contained statement.
            // [c] Assuming V is definitely unassigned before the contained
            // statement, V is definitely unassigned before every continue
            // statement for which the for statement is the continue target.
            analyzeExits(VeraContinueStatement.class,
                new LinkedList<BlockExit>());

            // DA/DU: V is [un]assigned before the update part of the for
            // statement iff V is [un]assigned after the contained statement
            // and V is [un]assigned before every continue statement for which
            // the for statement is the continue target.
            for (VeraExpressionStatement updateStmt : obj.getUpdateStatements())
            {
                updateStmt.accept((VeraStatementVisitor) this);
            }

            // second pass ends here
            if (loopPassTwo) break;

            // DU before condition - DU after update statements = vars possibly
            // assigned in condition, contained statement, or update statements
            daduBeforeCond.clearDU(analysis.dadu);
            // skip pass two if no vars possibly assigned
            if (!daduBeforeCond.containsDU()) break;

            // DA/DU: V is definitely unassigned after condition when true iff
            // V is definitely unassigned after contained statement and update
            // statements.
            daduAfterCond.clearDU(daduBeforeCond);

            // make a second pass through the loop to detect variables that are
            // possibly assigned multiple times
            loopPassTwo = true;
        }
        loopPassTwo = enclosingLoopPassTwo;

        // DA/DU: V is definitely assigned after a for statement iff both of the
        // following are true:
        // [A] Either a condition expression is not present or V is definitely
        // assigned after the condition expression when false.
        // [B] V is definitely assigned before every break statement for which
        // the for statement is the break target.
        // DA/DU: V is definitely unassigned after a for statement iff both of
        // the following are true:
        // [A] Either a condition expression is not present or V is definitely
        // unassigned after the condition expression when true.
        // [B] V is definitely unassigned after S.
        analysis.dadu = daduAfterCond;
        analyzeExits(VeraBreakStatement.class, enclosingExitList);
    }

    public void visit(VeraIfStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: V is [un]assigned before e iff V is [un]assigned before if (e)
        // S [else T].
        VeraExpression condExpr = obj.getCondition();
        ExpressionAnalysis condAnalysis = analyzeCondition(condExpr);
        DADUAnalysis daduAfterCondWhenFalse = condAnalysis.daduWhenFalse;

        // DA/DU: V is [un]assigned before S iff V is [un]assigned after e when
        // true.
        analysis.dadu = condAnalysis.daduWhenTrue;
        VeraStatement thenStmt = obj.getThenStatement();
        thenStmt.accept((VeraStatementVisitor) this);

        VeraStatement elseStmt = obj.getElseStatement();
        if (elseStmt != null)
        {
            DADUAnalysis daduAfterThen = analysis.dadu.duplicate();

            // DA/DU: V is [un]assigned before T iff V is [un]assigned after e
            // when false.
            analysis.dadu = daduAfterCondWhenFalse;
            elseStmt.accept((VeraStatementVisitor) this);

            // DA/DU: V is [un]assigned after if (e) S else T iff V is
            // [un]assigned after S and V is [un]assigned after T.
            analysis.dadu.merge(daduAfterThen);
        }
        else
        {
            // DA/DU: V is [un]assigned after if (e) S iff V is [un]assigned
            // after S and V is [un]assigned after e when false.
            analysis.dadu.merge(daduAfterCondWhenFalse);
        }
    }

    public void visit(VeraRandCaseStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: V is [un]assigned before the first weight expression of the
        // randcase statement iff V is [un]assigned before the randcase
        // statement.
        final DADUAnalysis daduWeightExprs = analysis.dadu;
        DADUAnalysis daduCases = null;

        for (final VeraRandCase rc : obj.getCaseList())
        {
            // DA/DU: V is [un]assigned before each subsequent weight expression
            // iff V is [un]assigned after the previous weight expression in
            // source order.
            analysis.dadu = daduWeightExprs;

            VeraExpression weightExpr = rc.getWeight();
            analyzeExpression(weightExpr, AccessType.READ);

            // DA/DU: V is [un]assigned before each case statement iff V is
            // [un]assigned after the corresponding weight expression.
            analysis.dadu = analysis.dadu.duplicate();

            VeraStatement stmt = rc.getStatement();
            stmt.accept((VeraStatementVisitor) this);

            // DA/DU
            if (daduCases != null)
            {
                daduCases.merge(analysis.dadu);
            }
            else
            {
                daduCases = analysis.dadu;
            }
        }

        // DA/DU
        if (daduCases == null)
        {
            // DA/DU: V is [un]assigned after a randcase statement with no cases
            // iff V is [un]assigned before the randcase statement.
            analysis.dadu = daduWeightExprs;
        }
        else
        {
            // DA/DU: V is [un]assigned after a randcase statement iff V is
            // [un]assigned after each case.
            analysis.dadu = daduCases;
        }
    }

    public void visit(VeraRepeatStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: repeat (e) S
        List<BlockExit> enclosingExitList = curExitList;
        boolean enclosingLoopPassTwo = loopPassTwo;
        DADUAnalysis daduAfterCond = null;
        while (true)
        {
            curExitList = new LinkedList<BlockExit>();

            // DA/DU: V is definitely assigned before e iff V is definitely
            // assigned before the repeat statement.
            // DA/DU: V is definitely unassigned before e iff [a], [b], and [c].
            // [a] V is definitely unassigned before the repeat statement.
            DADUAnalysis daduBeforeCond = null;
            if (!loopPassTwo)
            {
                daduBeforeCond = analysis.dadu.duplicate();
            }
            VeraExpression condExpr = obj.getCondition();
            analyzeExpression(condExpr, AccessType.READ);
            if (daduAfterCond == null)
            {
                daduAfterCond = analysis.dadu;
            }

            // DA/DU: V is [un]assigned before S iff V is [un]assigned after e.
            VeraStatement stmt = obj.getStatement();
            stmt.accept((VeraStatementVisitor) this);

            // DA/DU: [b] Assuming V is definitely unassigned before e, V is
            // definitely unassigned after S.
            // [c] Assuming V is definitely unassigned before e, V is definitely
            // unassigned before every continue statement for which the repeat
            // statement is the continue target.
            analyzeExits(VeraContinueStatement.class,
                new LinkedList<BlockExit>());

            // second pass ends here
            if (loopPassTwo) break;

            // DU before e - DU after S = vars possibly assigned in e or S
            daduBeforeCond.clearDU(analysis.dadu);
            // skip pass two if no vars assigned in e or S
            if (!daduBeforeCond.containsDU()) break;

            // DA/DU: V is definitely unassigned after e when true iff V is
            // definitely unassigned after S.
            daduAfterCond.clearDU(daduBeforeCond);

            // make a second pass through the loop to detect variables that are
            // possibly assigned multiple times
            loopPassTwo = true;
        }
        loopPassTwo = enclosingLoopPassTwo;

        // DA/DU: V is definitely assigned after repeat (e) S iff V is
        // definitely assigned after e when false and V is definitely assigned
        // before every break statement for which the repeat statement is the
        // break target.
        // DA/DU: V is definitely unassigned after repeat (e) S iff V is
        // definitely unassigned after e when true and V is definitely
        // unassigned after S.
        analysis.dadu = daduAfterCond;
        analyzeExits(VeraBreakStatement.class, enclosingExitList);
    }

    public void visit(VeraReturnStatement obj)
    {
        ++analyzer.statementCount;

        recordExit(obj);
    }

    public void visit(VeraSwitchStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: V is [un]assigned before the switch expression iff V is
        // [un]assigned before the switch statement.
        VeraExpression selExpr = obj.getSelector();
        analyzeExpression(selExpr, AccessType.READ);

        // DA/DU: V is [un]assigned before the first case expression of the
        // switch statement iff V is [un]assigned after the switch expression.
        final DADUAnalysis daduCaseExprs = analysis.dadu;
        DADUAnalysis daduCases = null;

        boolean hasDefault = false;
        final Iterator<VeraSwitchCase> iter = obj.getCases().iterator();
        while (iter.hasNext())
        {
            VeraSwitchCase sc = iter.next();

            // DA/DU: V is [un]assigned before each subsequent case expression
            // iff V is [un]assigned after the previous case expression in
            // source order.
            analysis.dadu = daduCaseExprs;

            if (sc instanceof VeraSwitchValueCase)
            {
                VeraSwitchValueCase svc = (VeraSwitchValueCase) sc;
                for (final VeraExpression valueExpr : svc.getValues())
                {
                    analyzeExpression(valueExpr, AccessType.READ);
                }
            }
            else
            {
                assert (sc instanceof VeraSwitchDefaultCase);
                hasDefault = true;
                assert (!iter.hasNext());
            }

            // DA/DU: V is [un]assigned before each case statement iff V is
            // [un]assigned after the corresponding case expression.
            analysis.dadu = analysis.dadu.duplicate();

            VeraStatement stmt = sc.getStatement();
            stmt.accept((VeraStatementVisitor) this);

            // DA/DU
            if (daduCases != null)
            {
                daduCases.merge(analysis.dadu);
            }
            else
            {
                daduCases = analysis.dadu;
            }
        }

        // DA/DU
        if (daduCases == null)
        {
            // DA/DU: V is [un]assigned after a switch statement with no cases
            // iff V is [un]assigned after the switch expression.
            analysis.dadu = daduCaseExprs;
        }
        else if (!hasDefault)
        {
            // DA/DU: V is [un]assigned after a switch statement without a
            // default case iff V is [un]assigned after the switch and case
            // expressions and V is [un]assigned after each case statement.
            daduCases.merge(daduCaseExprs);
            analysis.dadu = daduCases;
        }
        else
        {
            // DA/DU: V is [un]assigned after a switch statement with a default
            // case iff V is [un]assigned after each case.
            analysis.dadu = daduCases;
        }
    }

    public void visit(VeraSyncStatement obj)
    {
        ++analyzer.statementCount;

        for (final VeraSyncTerm term : obj.getTerms())
        {
            VeraExpression signalExpr = term.getSignal();
            analyzeExpression(signalExpr, AccessType.READ);
        }
    }

    public void visit(VeraTerminateStatement obj)
    {
        ++analyzer.statementCount;
    }

    public void visit(VeraWhileStatement obj)
    {
        ++analyzer.statementCount;

        // DA/DU: while (e) S
        List<BlockExit> enclosingExitList = curExitList;
        boolean enclosingLoopPassTwo = loopPassTwo;
        DADUAnalysis daduAfterCond = null;
        while (true)
        {
            curExitList = new LinkedList<BlockExit>();

            // DA/DU: V is definitely assigned before e iff V is definitely
            // assigned before the while statement.
            // DA/DU: V is definitely unassigned before e iff [a], [b], and [c].
            // [a] V is definitely unassigned before the while statement.
            DADUAnalysis daduBeforeCond = null;
            if (!loopPassTwo)
            {
                daduBeforeCond = analysis.dadu.duplicate();
            }
            VeraExpression condExpr = obj.getCondition();
            ExpressionAnalysis condAnalysis = analyzeCondition(condExpr);
            if (daduAfterCond == null)
            {
                daduAfterCond = condAnalysis.daduWhenFalse;
            }

            // DA/DU: V is [un]assigned before S iff V is [un]assigned after e
            // when true.
            analysis.dadu = condAnalysis.daduWhenTrue;
            VeraStatement stmt = obj.getStatement();
            stmt.accept((VeraStatementVisitor) this);

            // DA/DU: [b] Assuming V is definitely unassigned before e, V is
            // definitely unassigned after S.
            // [c] Assuming V is definitely unassigned before e, V is definitely
            // unassigned before every continue statement for which the while
            // statement is the continue target.
            analyzeExits(VeraContinueStatement.class,
                new LinkedList<BlockExit>());

            // second pass ends here
            if (loopPassTwo) break;

            // DU before e - DU after S = vars possibly assigned in e or S
            daduBeforeCond.clearDU(analysis.dadu);
            // skip pass two if no vars possibly assigned
            if (!daduBeforeCond.containsDU()) break;

            // DA/DU: V is definitely unassigned after e when true iff V is
            // definitely unassigned after S.
            daduAfterCond.clearDU(daduBeforeCond);

            // make a second pass through the loop to detect variables that are
            // possibly assigned multiple times
            loopPassTwo = true;
        }
        loopPassTwo = enclosingLoopPassTwo;

        // DA/DU: V is definitely assigned after while (e) S iff V is definitely
        // assigned after e when false and V is definitely assigned before every
        // break statement for which the while statement is the break target.
        // DA/DU: V is definitely unassigned after while (e) S iff V is
        // definitely unassigned after e when true and V is definitely
        // unassigned after S.
        analysis.dadu = daduAfterCond;
        analyzeExits(VeraBreakStatement.class, enclosingExitList);
    }
}
