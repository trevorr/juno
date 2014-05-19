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

import java.util.LinkedList;
import java.util.List;

import com.newisys.util.text.TextUtil;

/**
 * Represents a makefile pattern rule.
 * 
 * @author Trevor Robinson
 */
public final class MakePatternRule
    extends MakeRule
{
    private final List targetPatterns = new LinkedList();
    private final List dependencyPatterns = new LinkedList();

    public String getTargetNames()
    {
        return TextUtil.toString(targetPatterns.iterator(), null, null, " ");
    }

    public List getTargetPatterns()
    {
        return targetPatterns;
    }

    public void addTargetPattern(MakePattern pattern)
    {
        targetPatterns.add(pattern);
    }

    public String getDependencyNames()
    {
        return TextUtil
            .toString(dependencyPatterns.iterator(), null, null, " ");
    }

    public List getDependencyPatterns()
    {
        return dependencyPatterns;
    }

    public void addDependencyPattern(MakePattern pattern)
    {
        dependencyPatterns.add(pattern);
    }
}
