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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.newisys.io.IndentWriter;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.Type;
import com.newisys.langschema.vera.VeraBitType;
import com.newisys.langschema.vera.VeraBitVectorType;
import com.newisys.langschema.vera.VeraFunction;
import com.newisys.langschema.vera.VeraFunctionArgument;
import com.newisys.langschema.vera.VeraIntegerType;
import com.newisys.langschema.vera.VeraSchemaObject;
import com.newisys.langschema.vera.VeraVariable;

/**
 * Contains the analysis state for a particular variable.
 * 
 * @author Trevor Robinson
 */
public final class VariableAnalysis
{
    final VeraVariable var;
    SchemaObject context;

    // indicates that all possible accesses have been seen
    private boolean scopeComplete;

    // read/write usage analysis:
    private boolean readAccess; // accessed for read
    private boolean writeAccess; // accessed for write (including init)
    private boolean writeSideEffects; // write/init expr has side effects
    private boolean readUnassigned; // read when unassigned (X value observed)
    private boolean writeAssigned; // written when assigned (not final)

    // X/Z analysis:
    private final boolean xzType;
    private boolean assignedXZ; // definitely assigned X/Z
    private boolean notAssignedXZ; // definitely NOT assigned X/Z
    private Set<VariableAnalysis> xzPropagatesFrom; // Set of VariableAnalysis
    private Set<VariableAnalysis> xzPropagatesTo; // Set of VariableAnalysis
    private XZSourceType xzSourceType;
    private VariableAnalysis xzSource;
    private Set<VariableAnalysis> xzDests; // Set of VariableAnalysis

    // string analysis:
    private boolean readStringState; // get_status[_msg], (pre|post|this)match, backref
    private boolean writeStringState; // putc, match

    // field usage analysis:
    private boolean fieldAccessOutsideClass; // cannot be private
    private boolean fieldAccessOutsideSubClass; // cannot be protected
    private boolean fieldAccessOutsideDirectory; // cannot be default access
    private boolean fieldUnassignedInCtor; // cannot be final
    private boolean fieldWriteOutsideCtor; // cannot be final

    // by-ref argument usage:
    private Set<VeraSchemaObject> byRefArgUses; // arguments this variable is passed by-ref to
    private Set<VeraVariable> byRefArgVars; // variables passed by-ref to this argument
    private boolean passedByRefNVA; // passed by-ref to non-var-arg

    // fork usage:
    private boolean forkRead; // read by forked thread
    private boolean forkWrite; // written by forked thread

    // wait_var analysis:
    private boolean waitVar; // target of wait_var
    private boolean waitVarAlias; // by-ref argument alias of wait_var target

    private static final boolean debug = false;
    private static final IndentWriter iw;
    private static final PrintWriter pw;
    static
    {
        if (debug)
        {
            final FileWriter fw;
            try
            {
                fw = new FileWriter("var-analysis.log");
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
            iw = new IndentWriter(fw);
            pw = new PrintWriter(iw, true);
        }
        else
        {
            iw = null;
            pw = null;
        }
    }

    public VariableAnalysis(VeraVariable var, SchemaObject context)
    {
        this.var = var;
        this.context = context;

        // immediately mark non-bit/integer variables as not X/Z
        xzType = isXZType(var.getType());
        notAssignedXZ = !xzType;
    }

    private boolean isXZType(Type type)
    {
        return type instanceof VeraBitType || type instanceof VeraBitVectorType
            || type instanceof VeraIntegerType;
    }

    private void dumpName(StringBuffer msg)
    {
        msg.append(var.getName().getIdentifier());
        if (context != null)
        {
            msg.append(" (in ");
            msg.append(AnalyzerModule.getDescription(context));
            msg.append(")");
        }
    }

    public boolean isScopeComplete()
    {
        return scopeComplete;
    }

    void markScopeComplete()
    {
        if (debug) pw.println("markScopeComplete: " + this);
        if (debug) iw.incIndent();

        scopeComplete = true;

        checkClosure();

        if (debug) iw.decIndent();
    }

    private void checkClosure()
    {
        if (!isXZKnown() && isXZPropagationClosed())
        {
            markNotAssignedXZ();
        }
    }

    private boolean checkingXZClosure;

    private boolean isXZPropagationClosed()
    {
        if (debug) pw.println("isXZPropagationClosed: " + this);
        if (debug) iw.incIndent();

        boolean closed = true;

        if (xzPropagatesFrom != null)
        {
            checkingXZClosure = true;

            Iterator iter = xzPropagatesFrom.iterator();
            while (iter.hasNext())
            {
                VariableAnalysis other = (VariableAnalysis) iter.next();
                if (!other.scopeComplete
                    || (!other.checkingXZClosure && !other
                        .isXZPropagationClosed()))
                {
                    if (debug) pw.println("not closed due to: " + other);
                    closed = false;
                    break;
                }
            }

            checkingXZClosure = false;
        }

        if (debug) iw.decIndent();

        return closed;
    }

    public boolean isReadAccess()
    {
        return readAccess;
    }

    void markReadAccess()
    {
        readAccess = true;
    }

    public boolean isWriteAccess()
    {
        return writeAccess;
    }

    void markWriteAccess()
    {
        writeAccess = true;
    }

    public boolean isWriteSideEffects()
    {
        return writeSideEffects;
    }

    void markWriteSideEffects()
    {
        writeSideEffects = true;
    }

    public boolean isReadUnassigned()
    {
        return readUnassigned;
    }

    void markReadUnassigned()
    {
        readUnassigned = true;
    }

    public boolean isWriteAssigned()
    {
        return writeAssigned;
    }

    void markWriteAssigned()
    {
        writeAssigned = true;
    }

    public boolean isXZType()
    {
        return xzType;
    }

    public boolean isAssignedXZ()
    {
        return assignedXZ;
    }

    void markAssignedXZ(XZSourceType xzSourceType, VariableAnalysis xzSource)
    {
        if (debug) pw.println("markAssignedXZ: " + this);
        if (debug) iw.incIndent();

        assert (!isXZKnown());
        assignedXZ = true;
        this.xzSourceType = xzSourceType;
        this.xzSource = xzSource;
        if (xzSource != null) xzSource.addXZDestination(this);

        // propagate X/Z assignment to linked variables
        if (xzPropagatesTo != null)
        {
            // get iterator for current propagates-to list
            Iterator iter = xzPropagatesTo.iterator();

            // clear propagates-to list before recursion, for two reasons:
            // a) in case circularites exist
            // b) so linked variables will not modify our propagates-to list
            xzPropagatesTo = null;

            // recursively propagate X/Z assignment
            while (iter.hasNext())
            {
                VariableAnalysis other = (VariableAnalysis) iter.next();
                if (!other.isXZKnown())
                {
                    other.markAssignedXZ(XZSourceType.ASSIGNMENT, this);
                }
            }
        }

        clearXZPropagatesFrom();

        if (debug) iw.decIndent();
    }

    public boolean isNotAssignedXZ()
    {
        return notAssignedXZ;
    }

    void markNotAssignedXZ()
    {
        if (debug) pw.println("markNotAssignedXZ: " + this);
        if (debug) iw.incIndent();

        assert (!isXZKnown());
        notAssignedXZ = true;

        // remove self from other propagates-from lists
        if (xzPropagatesTo != null)
        {
            Iterator iter = xzPropagatesTo.iterator();
            xzPropagatesTo = null;
            while (iter.hasNext())
            {
                VariableAnalysis other = (VariableAnalysis) iter.next();
                if (debug)
                    pw.println("removing dependency: " + other + " <- " + this);

                // propagates-from list might not exist for this dependency
                // because it was marked not X/Z in determining the closure
                // of a previous dependency
                if (other.xzPropagatesFrom != null)
                {
                    boolean removed = other.xzPropagatesFrom.remove(this);
                    assert (removed);
                    if (other.scopeComplete) other.checkClosure();
                }
            }
        }

        clearXZPropagatesFrom();

        if (debug) iw.decIndent();
    }

    public boolean isXZKnown()
    {
        return assignedXZ || notAssignedXZ;
    }

    public XZSourceType getXZSourceType()
    {
        return xzSourceType;
    }

    public VariableAnalysis getXZSource()
    {
        return xzSource;
    }

    public Set getXZDestinations()
    {
        return xzDests != null ? xzDests : Collections.EMPTY_SET;
    }

    private void addXZDestination(VariableAnalysis dest)
    {
        if (xzDests == null) xzDests = new LinkedHashSet<VariableAnalysis>();
        xzDests.add(dest);
    }

    void trackXZPropagation(VariableAnalysis from)
    {
        // ignore self-assignment
        if (!isXZKnown() && from != this)
        {
            if (from.assignedXZ)
            {
                markAssignedXZ(XZSourceType.ASSIGNMENT, from);
            }
            else if (!from.notAssignedXZ)
            {
                if (debug)
                    pw.println("adding dependency: " + this + " <- " + from);
                this.addXZPropagatesFrom(from);
                from.addXZPropagatesTo(this);
            }
        }
    }

    private void addXZPropagatesFrom(VariableAnalysis var)
    {
        // X/Z propagation need not be tracked when X/Z condition is known
        assert (!isXZKnown());

        if (xzPropagatesFrom == null)
            xzPropagatesFrom = new HashSet<VariableAnalysis>();
        xzPropagatesFrom.add(var);
    }

    private void clearXZPropagatesFrom()
    {
        // remove self from other propagates-to lists
        if (xzPropagatesFrom != null)
        {
            Iterator iter = xzPropagatesFrom.iterator();
            xzPropagatesFrom = null;
            while (iter.hasNext())
            {
                VariableAnalysis other = (VariableAnalysis) iter.next();
                if (debug)
                    pw.println("removing dependency: " + this + " <- " + other);

                // propagates-to list will not exist for variables currently
                // propagating to this variable
                if (other.xzPropagatesTo != null)
                {
                    // however, if the propagates-to list does exist,
                    // check that it maintains from/to propagation pairing
                    boolean removed = other.xzPropagatesTo.remove(this);
                    assert (removed);
                }
            }
        }
    }

    private void addXZPropagatesTo(VariableAnalysis var)
    {
        // X/Z propagation need not be tracked when X/Z condition is known
        assert (!isXZKnown());

        if (xzPropagatesTo == null)
            xzPropagatesTo = new HashSet<VariableAnalysis>();
        xzPropagatesTo.add(var);
    }

    public boolean isReadStringState()
    {
        return readStringState;
    }

    void markReadStringState(VeraSchemaAnalyzer analyzer)
    {
        if (!readStringState)
        {
            readStringState = true;
            markStringStateVars(analyzer, true, false);
        }
    }

    public boolean isWriteStringState()
    {
        return writeStringState;
    }

    void markWriteStringState(VeraSchemaAnalyzer analyzer)
    {
        if (!writeStringState)
        {
            writeStringState = true;
            markStringStateVars(analyzer, false, true);
        }
    }

    private void markStringStateVars(
        VeraSchemaAnalyzer analyzer,
        boolean read,
        boolean write)
    {
        // mark any variables found so far that are aliased by this argument
        if (byRefArgVars != null)
        {
            for (VeraVariable var : byRefArgVars)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(var);
                // only analyzed variables can be put in byRefArgVars
                assert (varAnalysis != null);
                if (read) varAnalysis.markReadStringState(analyzer);
                if (write) varAnalysis.markWriteStringState(analyzer);
            }
        }

        // mark any by-ref arguments found so far that receive this variable
        if (byRefArgUses != null)
        {
            for (Object obj : byRefArgUses)
            {
                if (obj instanceof VeraFunctionArgument)
                {
                    VeraFunctionArgument arg = (VeraFunctionArgument) obj;
                    VariableAnalysis argAnalysis = analyzer
                        .getOrCreateVariableAnalysis(arg, arg.getFunction());
                    if (read) argAnalysis.markReadStringState(analyzer);
                    if (write) argAnalysis.markWriteStringState(analyzer);
                }
                else
                {
                    // ignore by-ref var-args uses
                }
            }
        }
    }

    public boolean isNeedStatefulString()
    {
        return (readStringState && writeStringState) || waitVar || waitVarAlias;
    }

    public boolean isFieldAccessOutsideClass()
    {
        return fieldAccessOutsideClass;
    }

    void markFieldAccessOutsideClass()
    {
        fieldAccessOutsideClass = true;
    }

    public boolean isFieldAccessOutsideSubClass()
    {
        return fieldAccessOutsideSubClass;
    }

    void markFieldAccessOutsideSubClass()
    {
        fieldAccessOutsideClass = true;
        fieldAccessOutsideSubClass = true;
    }

    public boolean isFieldAccessOutsideDirectory()
    {
        return fieldAccessOutsideDirectory;
    }

    void markFieldAccessOutsideDirectory()
    {
        fieldAccessOutsideClass = true;
        fieldAccessOutsideDirectory = true;
    }

    public boolean isFieldUnassignedInCtor()
    {
        return fieldUnassignedInCtor;
    }

    void markFieldUnassignedInCtor()
    {
        fieldUnassignedInCtor = true;
    }

    public boolean isFieldWriteOutsideCtor()
    {
        return fieldWriteOutsideCtor;
    }

    void markFieldWriteOutsideCtor()
    {
        fieldWriteOutsideCtor = true;
    }

    public boolean isPassedByRef()
    {
        return byRefArgUses != null;
    }

    public boolean isPassedByRefNVA()
    {
        return passedByRefNVA;
    }

    public Set getByRefArgUses()
    {
        return (byRefArgUses != null) ? byRefArgUses : Collections.EMPTY_SET;
    }

    void addByRefArgUse(VeraFunctionArgument arg, VeraSchemaAnalyzer analyzer)
    {
        // add the given argument to the set of by-ref arguments that alias
        // this variable
        if (byRefArgUses == null)
        {
            byRefArgUses = new HashSet<VeraSchemaObject>();
        }
        byRefArgUses.add(arg);
        passedByRefNVA = true;

        // add this variable to the set of variables the given argument aliases
        VariableAnalysis argAnalysis = analyzer.getOrCreateVariableAnalysis(
            arg, arg.getFunction());
        argAnalysis.addByRefArgVar(var);

        // if this variable is a wait_var target or alias,
        // mark the argument as a wait_var alias
        if (waitVarAlias)
        {
            argAnalysis.markWaitVarAlias(analyzer);
        }

        // if the argument is a wait_var target,
        // mark this variable as a wait_var target
        if (argAnalysis.isWaitVar())
        {
            markWaitVar(analyzer);
        }

        // propagate read/write string state analysis to argument
        if (readStringState) argAnalysis.markReadStringState(analyzer);
        if (writeStringState) argAnalysis.markWriteStringState(analyzer);
    }

    void addByRefVarArgUse(VeraFunction func, VeraSchemaAnalyzer analyzer)
    {
        if (byRefArgUses == null)
        {
            byRefArgUses = new HashSet<VeraSchemaObject>();
        }
        byRefArgUses.add(func);

        // if this variable is a wait_var target or alias,
        // mark the function as having wait_var alias var-args
        FunctionAnalysis funcAnalysis = analyzer
            .getOrCreateFunctionAnalysis(func);
        if (waitVarAlias)
        {
            funcAnalysis.markVarArgsWaitVarAlias();
        }
    }

    public Set getByRefArgVars()
    {
        return (byRefArgVars != null) ? byRefArgVars : Collections.EMPTY_SET;
    }

    private void addByRefArgVar(VeraVariable var)
    {
        if (byRefArgVars == null)
        {
            byRefArgVars = new HashSet<VeraVariable>();
        }
        byRefArgVars.add(var);
    }

    public boolean isForkRead()
    {
        return forkRead;
    }

    void markForkRead()
    {
        forkRead = true;
    }

    public boolean isForkWrite()
    {
        return forkWrite;
    }

    void markForkWrite()
    {
        forkWrite = true;
    }

    public boolean isWaitVar()
    {
        return waitVar;
    }

    void markWaitVar(VeraSchemaAnalyzer analyzer)
    {
        if (!waitVar)
        {
            StringBuffer msg = new StringBuffer(80);
            msg.append("Found wait_var target: ");
            dumpName(msg);
            analyzer.log.println(msg.toString());

            waitVar = true;
            waitVarAlias = true;

            markWaitVars(analyzer);
            markWaitVarAliases(analyzer);
        }
    }

    private void markWaitVars(VeraSchemaAnalyzer analyzer)
    {
        // mark any variables found so far that are aliased by this
        // wait_var target argument
        if (byRefArgVars != null)
        {
            for (VeraVariable var : byRefArgVars)
            {
                VariableAnalysis varAnalysis = analyzer
                    .getVariableAnalysis(var);
                // only analyzed variables can be put in byRefArgVars
                assert (varAnalysis != null);
                varAnalysis.markWaitVar(analyzer);
            }
        }
    }

    public boolean isWaitVarAlias()
    {
        return waitVarAlias;
    }

    private void markWaitVarAlias(VeraSchemaAnalyzer analyzer)
    {
        if (!waitVarAlias)
        {
            StringBuffer msg = new StringBuffer(80);
            msg.append("Found alias for wait_var target: ");
            dumpName(msg);
            analyzer.log.println(msg.toString());

            waitVarAlias = true;

            markWaitVarAliases(analyzer);
        }
    }

    private void markWaitVarAliases(VeraSchemaAnalyzer analyzer)
    {
        // mark any by-ref arguments/functions found so far that receive
        // this variable as taking wait_var aliases
        if (byRefArgUses != null)
        {
            for (Object obj : byRefArgUses)
            {
                if (obj instanceof VeraFunctionArgument)
                {
                    VeraFunctionArgument arg = (VeraFunctionArgument) obj;
                    VariableAnalysis argAnalysis = analyzer
                        .getOrCreateVariableAnalysis(arg, arg.getFunction());
                    argAnalysis.markWaitVarAlias(analyzer);
                }
                else
                {
                    VeraFunction func = (VeraFunction) obj;
                    FunctionAnalysis funcAnalysis = analyzer
                        .getOrCreateFunctionAnalysis(func);
                    funcAnalysis.markVarArgsWaitVarAlias();
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if (context != null)
        {
            return var.getName().getIdentifier() + " in "
                + AnalyzerModule.getDescription(context);
        }
        else
        {
            return var.getName().getCanonicalName();
        }
    }
}
