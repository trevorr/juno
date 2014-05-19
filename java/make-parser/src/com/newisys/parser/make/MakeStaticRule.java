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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.util.text.TextUtil;

/**
 * Represents a makefile static rule.
 * 
 * @author Trevor Robinson
 */
public final class MakeStaticRule
    extends MakeRule
{
    private final MakeFileInfo target;
    private final List dependencies = new LinkedList();
    private MakeStaticRule nextDoubleColonRule;

    public MakeStaticRule(MakeFileInfo target)
    {
        this.target = target;
    }

    public MakeStaticRule(MakeStaticRule other)
    {
        this(other.target);
        dependencies.addAll(other.dependencies);
    }

    public String getTargetNames()
    {
        return target.getPath();
    }

    public MakeFileInfo getTarget()
    {
        return target;
    }

    public String getDependencyNames()
    {
        return TextUtil.toString(dependencies.iterator(), null, null, " ");
    }

    public List getDependencies()
    {
        return dependencies;
    }

    public void addDependency(MakeFileInfo fileInfo)
    {
        dependencies.add(fileInfo);
    }

    public void addDependencies(Collection deps)
    {
        Iterator iter = deps.iterator();
        while (iter.hasNext())
        {
            MakeFileInfo depFileInfo = (MakeFileInfo) iter.next();
            dependencies.add(depFileInfo);
        }
    }

    public MakeStaticRule getNextDoubleColonRule()
    {
        return nextDoubleColonRule;
    }

    public void addDoubleColonRule(MakeStaticRule rule)
    {
        MakeStaticRule lastRule = this;
        while (lastRule.nextDoubleColonRule != null)
        {
            lastRule = lastRule.nextDoubleColonRule;
        }
        lastRule.nextDoubleColonRule = rule;
    }
}
