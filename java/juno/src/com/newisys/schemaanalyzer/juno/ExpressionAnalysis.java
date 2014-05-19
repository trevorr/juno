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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.vera.VeraCompilationUnit;
import com.newisys.langschema.vera.VeraUserClass;
import com.newisys.langschema.vera.VeraVariable;

/**
 * Contains the analysis state for a particular expression.
 * 
 * @author Trevor Robinson
 */
final class ExpressionAnalysis
{
    final VeraCompilationUnit compUnit;
    final VeraUserClass cls;
    final SchemaObject context;
    final BlockAnalysis blockAnalysis;

    boolean referencesInstance;
    boolean referencesLocalNonArg;
    boolean byRefArgument;
    boolean postIncDec;
    boolean sideEffects;
    DADUAnalysis daduUncond;
    DADUAnalysis daduWhenTrue;
    DADUAnalysis daduWhenFalse;

    XZSourceType uncondXZType;
    Set<VariableAnalysis> xzPropagatesFrom;

    public ExpressionAnalysis(
        VeraCompilationUnit compUnit,
        VeraUserClass cls,
        SchemaObject context,
        BlockAnalysis blockAnalysis,
        DADUAnalysis daduAnalysis)
    {
        this.compUnit = compUnit;
        this.cls = cls;
        this.context = context;
        this.blockAnalysis = blockAnalysis;
        this.daduUncond = daduAnalysis;
    }

    public void addLocalAccess(VeraVariable var, boolean read, boolean write)
    {
        if (blockAnalysis != null)
        {
            blockAnalysis.addLocalAccess(var, read, write);
        }
    }

    public void splitDADU()
    {
        if (daduUncond != null)
        {
            daduWhenTrue = daduUncond;
            daduWhenFalse = daduUncond.duplicate();
            daduUncond = null;
        }
    }

    public void mergeDADU()
    {
        if (daduWhenTrue != null)
        {
            daduUncond = daduWhenTrue;
            daduUncond.merge(daduWhenFalse);
            daduWhenTrue = daduWhenFalse = null;
        }
    }

    public boolean isUncondXZ()
    {
        return uncondXZType != null;
    }

    public void markUncondXZ(XZSourceType xzSourceType)
    {
        uncondXZType = xzSourceType;
        xzPropagatesFrom = null;
    }

    public void addXZPropagatesFrom(VariableAnalysis var)
    {
        if (xzPropagatesFrom == null)
            xzPropagatesFrom = new HashSet<VariableAnalysis>();
        xzPropagatesFrom.add(var);
    }

    public Set<VariableAnalysis> getXZPropagatesFrom()
    {
        if (xzPropagatesFrom == null) return Collections.emptySet();
        return xzPropagatesFrom;
    }
}
