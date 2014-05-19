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

package com.newisys.juno;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an abstract Vera component, which may be an actual component or a
 * grouping component.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraAbsComponent
    implements Serializable
{
    private final String name;
    private final Set<VeraAbsComponent> dependencies = new LinkedHashSet<VeraAbsComponent>();

    /**
     * Constructs a new abstract component.
     *
     * @param name the name of this component
     */
    public VeraAbsComponent(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this component.
     *
     * @return String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the set of components that this component is dependent upon.
     *
     * @return Set of VeraAbsComponent
     */
    public Set<VeraAbsComponent> getDependencies()
    {
        return dependencies;
    }

    /**
     * Adds the given component to this list of components that this component
     * depends upon.
     *
     * @param target VeraAbsComponent
     */
    public void addDependency(VeraAbsComponent target)
    {
        dependencies.add(target);
    }

    /**
     * Returns all the dependencies for the this component, including transitive
     * dependencies. The returned components are unique and in the order of a
     * pre-order traversal of the dependency graph. Circular dependencies are
     * ignored.
     *
     * @return Set of VeraAbsComponent
     */
    public Set<VeraAbsComponent> getFullDependencies()
    {
        Set<VeraAbsComponent> fullDeps = new LinkedHashSet<VeraAbsComponent>();
        Set<VeraAbsComponent> activeDeps = new HashSet<VeraAbsComponent>();
        addDependencies(fullDeps, activeDeps);
        return fullDeps;
    }

    /**
     * Recursive helper function for getFullDependencies().
     *
     * @param fullDeps the full dependency set being built
     * @param activeDeps the dependencies currently recursed into
     */
    private void addDependencies(
        Set<VeraAbsComponent> fullDeps,
        Set<VeraAbsComponent> activeDeps)
    {
        Set<VeraAbsComponent> targetDeps = getDependencies();
        for (VeraAbsComponent dep : targetDeps)
        {
            if (!activeDeps.contains(dep))
            {
                activeDeps.add(dep);
                dep.addDependencies(fullDeps, activeDeps);
                activeDeps.remove(dep);
            }
            fullDeps.add(dep);
        }
    }

    /**
     * Returns the set of include paths for the this component.
     *
     * @return Set of String
     */
    public Set<String> getIncludePaths()
    {
        Set<String> pathList = new LinkedHashSet<String>();
        addIncludePaths(pathList);

        Set<VeraAbsComponent> fullDeps = getFullDependencies();
        for (VeraAbsComponent dep : fullDeps)
        {
            dep.addIncludePaths(pathList);
        }

        return pathList;
    }

    /**
     * Helper function for getIncludePaths(). Adds the include paths for this
     * component to the given set.
     *
     * @param paths the set of paths to add to
     */
    protected void addIncludePaths(Set<String> paths)
    {
        // do nothing by default
    }

    /**
     * Returns the set of include paths for the this component.
     *
     * @return Set of String
     */
    public Set<String> getAllImportedIncludes()
    {
        Set<String> pathList = new LinkedHashSet<String>();
        addImportPaths(pathList);

        Set<VeraAbsComponent> fullDeps = getFullDependencies();
        for (VeraAbsComponent dep : fullDeps)
        {
            dep.addImportPaths(pathList);
        }

        return pathList;
    }

    /**
     * Helper function for getAllImportedIncludes(). Adds the include paths for
     * this component to the given set.
     *
     * @param paths the set of paths to add to
     */
    protected void addImportPaths(Set<String> paths)
    {
        // do nothing by default
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }
}
