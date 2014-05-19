/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.parser.verapp;

import java.util.Iterator;
import java.util.List;

import com.newisys.parser.util.Macro;
import com.newisys.parser.util.MacroRef;

/**
 * VeraPPParser implementation of function macros references.
 * 
 * @author Trevor Robinson
 */
public class VeraPPFunctionMacroRef
    implements MacroRef
{
    private final VeraPPFunctionMacro macro;
    private final List argList;
    private final MacroRef expandedFrom;

    public VeraPPFunctionMacroRef(
        VeraPPFunctionMacro macro,
        List argList,
        MacroRef expandedFrom)
    {
        this.macro = macro;
        this.argList = argList;
        this.expandedFrom = expandedFrom;
    }

    public Macro getMacro()
    {
        return macro;
    }

    public List getArgList()
    {
        return argList;
    }

    public MacroRef getExpandedFrom()
    {
        return expandedFrom;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(macro.getName());
        buf.append("(");
        Iterator iter = argList.iterator();
        while (iter.hasNext())
        {
            buf.append(iter.next());
            if (iter.hasNext()) buf.append(',');
        }
        buf.append(")");
        return buf.toString();
    }
}
