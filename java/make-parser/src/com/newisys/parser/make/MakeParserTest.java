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

/**
 * A simple debugging/test program for the makefile parser.
 * 
 * @author Trevor Robinson
 */
public final class MakeParserTest
{
    public static void main(String[] args)
    {
        MakeDatabase database = new MakeDatabase();
        try
        {
            MakeParser parser = new MakeParser(database, args[0]);
            parser.parse();

            database.applySpecialTargets();
            dumpVariables(database.getGlobalVariables());
            dumpRules("Static", database.getStaticRules());
            dumpRules("Pattern", database.getPatternRules());
        }
        catch (MakeParseException e)
        {
            System.err.println(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void defineCmdLineVar(
        MakeDatabase database,
        String name,
        String value)
    {
        MakeVariable var = new MakeVariable(name,
            MakeVariableOrigin.COMMAND_LINE, false);
        var.setValue(value);
        database.addVariable(var);
    }

    private static void dumpVariables(Collection vars)
    {
        System.out.println("Variables:");
        Iterator iter = vars.iterator();
        while (iter.hasNext())
        {
            MakeVariable var = (MakeVariable) iter.next();
            System.out.println("  " + var.getName() + " [" + var.getOrigin()
                + "] " + (var.isRecursive() ? "= " : ":= ") + var.getValue());
        }
    }

    private static void dumpRules(String type, Collection rules)
    {
        System.out.println(type + " rules:");
        Iterator iter = rules.iterator();
        while (iter.hasNext())
        {
            MakeRule rule = (MakeRule) iter.next();
            dumpRule(rule);
            if (rule.isDoubleColon() && rule instanceof MakeStaticRule)
            {
                MakeStaticRule cur = (MakeStaticRule) rule, next;
                while ((next = cur.getNextDoubleColonRule()) != null)
                {
                    dumpRule(next);
                    cur = next;
                }
            }
        }
    }

    private static void dumpRule(MakeRule rule)
    {
        System.out.println("  " + rule);
        String command = rule.getCommand();
        if (command != null)
        {
            String[] commands = command.split("\n");
            for (int i = 0; i < commands.length; ++i)
            {
                System.out.println("    " + commands[i]);
            }
        }
    }
}
