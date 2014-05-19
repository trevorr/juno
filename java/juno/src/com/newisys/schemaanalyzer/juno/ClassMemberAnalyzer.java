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

import com.newisys.langschema.vera.*;

/**
 * Schema analyzer for class member objects. This class expects the following
 * types of objects to be analyzed in the given order: variables, constructor,
 * methods/constraints. Other types of objects may be freely interspersed among
 * those and are ignored.
 * 
 * @author Trevor Robinson
 */
final class ClassMemberAnalyzer
    extends AnalyzerModule
    implements VeraClassMemberVisitor
{
    private final VeraUserClass cls;
    private final ClassAnalysis analysis;
    private final List<VariableAnalysis> localVarAnalyses = new LinkedList<VariableAnalysis>();
    private DADUAnalysis dadu = new DADUAnalysis();
    private boolean gotCtor = false;
    private boolean gotMethod = false;

    public ClassMemberAnalyzer(
        VeraSchemaAnalyzer analyzer,
        VeraUserClass cls,
        ClassAnalysis analysis)
    {
        super(analyzer);
        this.cls = cls;
        this.analysis = analysis;
    }

    public void ctorsComplete()
    {
        // mark fields not definitely assigned as unassigned after ctor
        final int nextIndex = dadu.getNextIndex();
        for (int index = 0; index < nextIndex; ++index)
        {
            if (!dadu.isDA(index))
            {
                final VeraVariable var = dadu.getVariable(index);
                final VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(var);
                varAnalysis.markFieldUnassignedInCtor();
            }
        }
    }

    public void finalizeAnalysis()
    {
        // mark local variables as out of scope
        finalizeLocalVars(localVarAnalyses);
    }

    public void visit(VeraClassConstraint obj)
    {
        ++analyzer.constraintCount;

        // analyze constraint expressions
        final Iterator iter = obj.getExprs().iterator();
        while (iter.hasNext())
        {
            VeraExpression expr = (VeraExpression) iter.next();
            analyzeExpression(expr, AccessType.READ, cls.getCompilationUnit(),
                cls, obj, null, dadu);
        }
    }

    public void visit(VeraEnumeration obj)
    {
        ++analyzer.enumCount;
    }

    public void visit(VeraEnumerationElement obj)
    {
        // ignored
    }

    public void visit(VeraMemberFunction obj)
    {
        ++analyzer.methodCount;

        // determine whether method is a constructor
        final boolean isCtor = obj.isConstructor();
        if (isCtor)
        {
            // check member type ordering
            assert (!gotMethod);
            gotCtor = true;

            // remember whether class has a default constructor
            if (obj.getType().getArguments().size() == 0)
            {
                analysis.hasDefaultCtor = true;
            }
        }
        else
        {
            gotMethod = true;

            // two override analyses:
            // 1) track direct method overrides
            // 2) check for override of a non-virtual method;
            //    requires method renaming in Java
            final FunctionAnalysis funcAnalysis = analyzer
                .getOrCreateFunctionAnalysis(obj);
            final boolean visibleNonVirtual = isVisibleNonVirtual(obj);
            final String id = obj.getName().getIdentifier();
            boolean foundBaseFunc = false;
            VeraClass baseCls = cls.getBaseClass();
            while (baseCls != null)
            {
                Iterator iter = baseCls
                    .lookupObjects(id, VeraNameKind.NON_TYPE);
                while (iter.hasNext())
                {
                    Object baseObj = iter.next();
                    if (baseObj instanceof VeraMemberFunction)
                    {
                        VeraMemberFunction baseFunc = (VeraMemberFunction) baseObj;
                        FunctionAnalysis baseFuncAnalysis = analyzer
                            .getOrCreateFunctionAnalysis(baseFunc);
                        // analysis 1
                        if (!foundBaseFunc)
                        {
                            funcAnalysis.setOverridden(baseFuncAnalysis);
                            baseFuncAnalysis.addDirectOverride(funcAnalysis);
                        }
                        // analysis 2
                        if (visibleNonVirtual && isVisibleNonVirtual(baseFunc))
                        {
                            baseFuncAnalysis.markNonVirtualOverride();
                        }
                        foundBaseFunc = true;
                    }
                }
                baseCls = baseCls.getBaseClass();
            }
            // analysis 2
            if (visibleNonVirtual && foundBaseFunc)
            {
                funcAnalysis.markNonVirtualOverride();
            }
        }

        // analyze method body
        final VeraBlock body = obj.getBody();
        if (body != null)
        {
            final int blockIndex = dadu.beginBlock();
            initFunctionDADU(obj, dadu);
            BlockAnalysis blockAnalysis = analyzeBlock(body, obj, cls
                .getCompilationUnit(), cls, isCtor, dadu);
            dadu = blockAnalysis.dadu;
            finalizeFunctionDADU(obj, dadu);
            dadu.endBlock(blockIndex);
        }
    }

    private boolean isVisibleNonVirtual(VeraMemberFunction func)
    {
        return !func.isVirtual()
            && func.getVisibility() != VeraVisibility.LOCAL;
    }

    public void visit(VeraMemberVariable obj)
    {
        ++analyzer.fieldCount;

        // check member type ordering
        assert (!gotCtor && !gotMethod);

        // create analysis object for this variable (to track context)
        VariableAnalysis varAnalysis = analyzer.getOrCreateVariableAnalysis(
            obj, cls);
        if (obj.getVisibility() == VeraVisibility.LOCAL)
        {
            localVarAnalyses.add(varAnalysis);
        }

        // initialize DA/DU tracking for variable
        int daduIndex = dadu.alloc(obj);

        VeraExpression initExpr = obj.getInitializer();
        if (initExpr != null)
        {
            // analyze initializer
            ExpressionAnalysis exprAnalysis = analyzeExpression(initExpr,
                AccessType.READ, cls.getCompilationUnit(), cls, cls, null, dadu);
            applyAssignAnalysis(varAnalysis, exprAnalysis);

            // mark variable as assigned after initializer
            dadu.markAssigned(daduIndex);

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
                dadu.markAssigned(daduIndex);
            }
        }
    }
}
