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

import java.util.HashSet;
import java.util.Set;

import com.newisys.langschema.vera.VeraFunction;

/**
 * Contains the analysis state for a particular function.
 * 
 * @author Trevor Robinson
 */
public final class FunctionAnalysis
{
    final VeraSchemaAnalyzer analyzer;
    final VeraFunction func;

    private FunctionAnalysis overridden;
    private final Set<FunctionAnalysis> directOverrides = new HashSet<FunctionAnalysis>();
    private boolean returnXZInOverride;
    private final boolean[] argXZInOverride;
    private final boolean[] argStatefulStringInOverride;
    private boolean nonVirtualOverride; // involved in non-virtual overriding
    private boolean varArgsWaitVarAlias; // var args alias wait_var target

    public FunctionAnalysis(VeraSchemaAnalyzer analyzer, VeraFunction func)
    {
        this.analyzer = analyzer;
        this.func = func;
        final int argCount = func.getType().getArguments().size();
        argXZInOverride = new boolean[argCount];
        argStatefulStringInOverride = new boolean[argCount];
    }

    public int getArgumentCount()
    {
        return argXZInOverride.length;
    }

    public FunctionAnalysis getOverridden()
    {
        return overridden;
    }

    void setOverridden(FunctionAnalysis overridden)
    {
        this.overridden = overridden;
    }

    public Set<FunctionAnalysis> getDirectOverrides()
    {
        return directOverrides;
    }

    void addDirectOverride(FunctionAnalysis func)
    {
        directOverrides.add(func);
    }

    public boolean isReturnXZInOverride()
    {
        return returnXZInOverride;
    }

    void setReturnXZInOverride(boolean returnXZInOverride)
    {
        this.returnXZInOverride = returnXZInOverride;
    }

    public boolean isArgumentXZInOverride(int index)
    {
        return argXZInOverride[index];
    }

    void setArgumentXZInOverride(int index, boolean value)
    {
        this.argXZInOverride[index] = value;
    }

    public boolean isArgumentStatefulStringInOverride(int index)
    {
        return argStatefulStringInOverride[index];
    }

    void setArgumentStatefulStringInOverride(int index, boolean value)
    {
        this.argStatefulStringInOverride[index] = value;
    }

    public boolean isNonVirtualOverride()
    {
        return nonVirtualOverride;
    }

    void markNonVirtualOverride()
    {
        if (!nonVirtualOverride)
        {
            analyzer.log.println("Function involved in non-virtual override: "
                + func.getName());

            nonVirtualOverride = true;
        }
    }

    public boolean isVarArgsWaitVarAlias()
    {
        return varArgsWaitVarAlias;
    }

    void markVarArgsWaitVarAlias()
    {
        if (!varArgsWaitVarAlias)
        {
            analyzer.log.println("By-ref var-args alias wait_var target: "
                + func.getName());

            varArgsWaitVarAlias = true;
        }
    }
}
