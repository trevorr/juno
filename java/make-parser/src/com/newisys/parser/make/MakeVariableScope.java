/*
 * Makefile Parser and Model Builder
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.parser.make;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains all the variables defined in a particular scope.
 * 
 * @author Trevor Robinson
 */
final class MakeVariableScope
{
    private final Map variables = new LinkedHashMap();

    /**
     * Returns all of the variables defined in this scope.
     *
     * @return Collection of MakeVariable
     */
    public Collection getVariables()
    {
        return variables.values();
    }

    /**
     * Returns the variable from this scope with the given name, or null if no
     * such variable exists.
     *
     * @param name String
     * @return MakeVariable
     */
    public MakeVariable getVariable(String name)
    {
        return (MakeVariable) variables.get(name);
    }

    /**
     * Adds the given variable to this scope. If a variable with the same name
     * already exists, it is overwritten.
     *
     * @param var MakeVariable
     */
    public void addVariable(MakeVariable var)
    {
        variables.put(var.getName(), var);
    }
}
