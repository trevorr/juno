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

import java.util.*;

import com.newisys.util.system.SystemUtil;

/**
 * Represents all the variables and rules in a makefile.
 * 
 * @author Trevor Robinson
 */
public final class MakeDatabase
{
    private final MakeVariableScope globalScope = new MakeVariableScope();
    private MakeVariableScope currentScope = globalScope;
    private final Stack scopeStack = new Stack();
    private boolean exportAll;
    private final List vpaths = new LinkedList();
    private final Map files = new LinkedHashMap();
    private final List staticRules = new LinkedList();
    private final List patternRules = new LinkedList();

    public MakeDatabase()
    {
        defineAutoVariables();
    }

    private void defineAutoVariables()
    {
        defineVariable("MAKEFILES", MakeVariableOrigin.DEFAULT, false, "");

        String[] shellArgs = SystemUtil.getShellArgs("");
        defineVariable("SHELL", MakeVariableOrigin.DEFAULT, false, shellArgs[0]);

        defineAutoVariable("@D", "$(patsubst %/,%,$(dir $@))");
        defineAutoVariable("%D", "$(patsubst %/,%,$(dir $%))");
        defineAutoVariable("*D", "$(patsubst %/,%,$(dir $*))");
        defineAutoVariable("<D", "$(patsubst %/,%,$(dir $<))");
        defineAutoVariable("?D", "$(patsubst %/,%,$(dir $?))");
        defineAutoVariable("^D", "$(patsubst %/,%,$(dir $^))");
        defineAutoVariable("+D", "$(patsubst %/,%,$(dir $+))");

        defineAutoVariable("@F", "$(notdir $@)");
        defineAutoVariable("%F", "$(notdir $%)");
        defineAutoVariable("*F", "$(notdir $*)");
        defineAutoVariable("<F", "$(notdir $<)");
        defineAutoVariable("?F", "$(notdir $?)");
        defineAutoVariable("^F", "$(notdir $^)");
        defineAutoVariable("+F", "$(notdir $+)");
    }

    private void defineAutoVariable(String name, String value)
    {
        defineVariable(name, MakeVariableOrigin.AUTOMATIC, true, value);
    }

    private void defineVariable(
        String name,
        MakeVariableOrigin origin,
        boolean recursive,
        String value)
    {
        MakeVariable var = new MakeVariable(name, origin, recursive);
        var.setValue(value);
        globalScope.addVariable(var);
    }

    /**
     * Returns all of the variables defined in the global scope.
     *
     * @return Collection of MakeVariable
     */
    public Collection getGlobalVariables()
    {
        return globalScope.getVariables();
    }

    /**
     * Creates a new variable scope and pushes the previous scope on the scope
     * stack.
     */
    public void pushNewVariableScope()
    {
        scopeStack.push(currentScope);
        currentScope = new MakeVariableScope();
    }

    /**
     * Pops the previous scope from the scope stack and makes it the current
     * scope.
     */
    public void popVariableScope()
    {
        currentScope = (MakeVariableScope) scopeStack.pop();
    }

    /**
     * Returns the first variable in the scope stack with the given name, or
     * null if no such variable exists.
     *
     * @param name String
     * @return MakeVariable
     */
    public MakeVariable getVariable(String name)
    {
        MakeVariable var = currentScope.getVariable(name);
        if (var == null)
        {
            ListIterator iter = scopeStack.listIterator(scopeStack.size());
            while (iter.hasPrevious())
            {
                MakeVariableScope scope = (MakeVariableScope) iter.previous();
                var = scope.getVariable(name);
                if (var != null) break;
            }
        }
        return var;
    }

    /**
     * Adds the given variable to the current scope. If a variable with the same
     * name already exists, it is overwritten.
     *
     * @param var MakeVariable
     */
    public void addVariable(MakeVariable var)
    {
        currentScope.addVariable(var);
    }

    /**
     * Returns whether all variables should be exported to the environment of
     * child processes.
     *
     * @return boolean
     */
    public boolean isExportAll()
    {
        return exportAll;
    }

    /**
     * Sets whether all variables should be exported to the environment of child
     * processes.
     *
     * @param exportAll boolean
     */
    public void setExportAll(boolean exportAll)
    {
        this.exportAll = exportAll;
    }

    /**
     * Associates the given search path with files matching the given pattern.
     *
     * @param pattern the pattern to match against filenames (ex. "%.c")
     * @param path the path to search for matching files
     */
    public void addVPath(MakePattern pattern, String path)
    {
        vpaths.add(new VPathEntry(pattern, path));
    }

    /**
     * Removes all search paths associated with the given pattern.
     *
     * @param pattern the pattern to remove search paths for
     */
    public void removeVPaths(String pattern)
    {
        final Iterator iter = vpaths.iterator();
        while (iter.hasNext())
        {
            final VPathEntry entry = (VPathEntry) iter.next();
            if (entry.getPattern().equals(pattern))
            {
                iter.remove();
            }
        }
    }

    /**
     * Removes all search paths from the database, regardless of their
     * associated pattern.
     */
    public void removeAllVPaths()
    {
        vpaths.clear();
    }

    /**
     * Returns an Iterator over the search paths with patterns matching the
     * given filename. The search paths in the database must not be modified
     * while the returned Iterator is in use.
     *
     * @param name the filename to match patterns against
     * @return an Iterator over the associated search path Strings
     */
    public Iterator searchVPaths(final String name)
    {
        final Iterator iter = vpaths.iterator();
        return new Iterator()
        {
            private String next = findNext();

            private String findNext()
            {
                while (iter.hasNext())
                {
                    final VPathEntry entry = (VPathEntry) iter.next();
                    if (entry.patternMatches(name))
                    {
                        return entry.getPath();
                    }
                }
                return null;
            }

            public boolean hasNext()
            {
                return next != null;
            }

            public Object next()
            {
                if (next == null) throw new NoSuchElementException();
                final Object result = next;
                next = findNext();
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException(
                    "remove() is not supported by this iterator");
            }
        };
    }

    /**
     * Returns all of the make file records defined in the database.
     *
     * @return Collection of MakeFileInfo
     */
    public Collection getFiles()
    {
        return files.values();
    }

    /**
     * Returns the make file record with the given path, or null if no such file
     * exists.
     *
     * @param path String
     * @return MakeFileInfo
     */
    public MakeFileInfo getFile(String path)
    {
        return (MakeFileInfo) files.get(path);
    }

    /**
     * Returns the file with the given path. If no such file exists, one is
     * created.
     *
     * @param path String
     * @return MakeFileInfo
     */
    public MakeFileInfo getOrCreateFile(String path)
    {
        MakeFileInfo fileInfo = getFile(path);
        if (fileInfo == null)
        {
            fileInfo = new MakeFileInfo(path);
            addFile(fileInfo);
        }
        return fileInfo;
    }

    /**
     * Adds the given file to the database. If a file with the same path already
     * exists, it is overwritten.
     *
     * @param fileInfo MakeFileInfo
     */
    public void addFile(MakeFileInfo fileInfo)
    {
        files.put(fileInfo.getPath(), fileInfo);
    }

    /**
     * Returns the rule used to make the given file. If the file does not
     * already have a static rule associated with it, or has a static rule with
     * no commands, this method will attempt to find a matching pattern rule and
     * create a static rule from it.
     *
     * @param fileInfo MakeFileInfo
     * @return MakeStaticRule
     */
    public MakeStaticRule getRuleForFile(MakeFileInfo fileInfo)
    {
        MakeStaticRule rule = fileInfo.getRule();
        if (rule == null || rule.getCommand() == null)
        {
            rule = applyPatternRules(fileInfo.getPath(), rule);
        }
        return rule;
    }

    /**
     * Returns all of the static rules defined in the database.
     *
     * @return List of MakeStaticRule
     */
    public List getStaticRules()
    {
        return staticRules;
    }

    /**
     * Returns the static rule with the given target name, or null if no such
     * rule exists.
     *
     * @param name String
     * @return MakeStaticRule
     */
    public MakeStaticRule getStaticRule(String name)
    {
        MakeFileInfo fileInfo = getFile(name);
        return (fileInfo != null) ? fileInfo.getRule() : null;
    }

    /**
     * Adds the given static rule to the database.
     *
     * @param rule MakeStaticRule
     */
    public void addStaticRule(MakeStaticRule rule)
    {
        staticRules.add(rule);
    }

    /**
     * Returns all of the pattern rules defined in the database.
     *
     * @return List of MakePatternRule
     */
    public List getPatternRules()
    {
        return patternRules;
    }

    /**
     * Adds the given pattern rule to the database.
     *
     * @param rule MakePatternRule
     */
    public void addPatternRule(MakePatternRule rule)
    {
        patternRules.add(rule);
    }

    /**
     * Searches all the pattern rules for one that best matches the given
     * target. If a matching rule is found, a static rule is constructed from it
     * and returned; otherwise, null is returned.
     *
     * @param target the target to find a pattern rule for
     * @param baseRule the base static rule to use (with no commands), or null
     * @return MakePatternRule
     */
    public MakeStaticRule applyPatternRules(
        String target,
        MakeStaticRule baseRule)
    {
        return applyPatternRules(target, baseRule, new HashSet());
    }

    private MakeStaticRule applyPatternRules(
        String target,
        MakeStaticRule baseRule,
        Set activeRules)
    {
        // split target into dir and name
        String dir, name;
        int slashPos = MakeUtil.indexOfLastSlash(target);
        if (slashPos >= 0)
        {
            dir = target.substring(0, slashPos + 1);
            name = target.substring(slashPos + 1);
        }
        else
        {
            dir = null;
            name = target;
        }

        // make a list of matching rules
        Map matchingRules = new LinkedHashMap();
        Map ntmaRules = new LinkedHashMap();
        boolean gotNotMatchAny = false;
        Iterator ruleIter = patternRules.iterator();
        while (ruleIter.hasNext())
        {
            MakePatternRule rule = (MakePatternRule) ruleIter.next();
            boolean hasCommands = rule.getCommand() != null;
            Iterator patIter = rule.getTargetPatterns().iterator();
            while (patIter.hasNext())
            {
                MakePattern pat = (MakePattern) patIter.next();
                boolean hasSlash = pat.toString().indexOf('/') >= 0;
                if (pat.matchesAny())
                {
                    if (hasCommands)
                    {
                        if (!rule.isDoubleColon())
                        {
                            ntmaRules.put(rule, pat);
                        }
                        else
                        {
                            matchingRules.put(rule, pat);
                        }
                    }
                }
                else if (pat.matches(hasSlash ? target : name))
                {
                    gotNotMatchAny = true;
                    if (hasCommands)
                    {
                        matchingRules.put(rule, pat);
                    }
                    break;
                }
            }
        }
        if (!gotNotMatchAny)
        {
            matchingRules.putAll(ntmaRules);
        }

        // find rule with prerequisites that exist or ought to exist (passes 1
        // and 2) or can be made by an implicit rule (pass 2 only)
        for (int pass = 1; pass <= 2; ++pass)
        {
            ruleIter = matchingRules.entrySet().iterator();
            while (ruleIter.hasNext())
            {
                Map.Entry entry = (Map.Entry) ruleIter.next();
                MakePatternRule rule = (MakePatternRule) entry.getKey();
                MakePattern matchingPat = (MakePattern) entry.getValue();
                boolean hasSlash = matchingPat.toString().indexOf('/') >= 0;
                String stub = matchingPat.extractStub(hasSlash ? target : name);

                boolean depsOkay = true;
                List staticDeps = new LinkedList();
                Iterator depIter = rule.getDependencyPatterns().iterator();
                while (depIter.hasNext())
                {
                    MakePattern depPat = (MakePattern) depIter.next();
                    String depName = depPat.replaceStub(stub);
                    if (!hasSlash && dir != null) depName = dir + depName;
                    MakeFileInfo depFileInfo = getFile(depName);
                    if (depFileInfo == null)
                    {
                        depFileInfo = new MakeFileInfo(depName);
                        addFile(depFileInfo);
                        depFileInfo.setIntermediate(true);
                    }
                    if (depFileInfo.exists() || !depFileInfo.isIntermediate())
                    {
                        staticDeps.add(depFileInfo);
                        continue;
                    }
                    else if (pass == 2 && !activeRules.contains(rule))
                    {
                        MakeStaticRule depRule = depFileInfo.getRule();
                        if (depRule == null)
                        {
                            activeRules.add(rule);
                            depRule = applyPatternRules(depName, null,
                                activeRules);
                            activeRules.remove(rule);
                        }
                        if (depRule != null)
                        {
                            staticDeps.add(depFileInfo);
                            continue;
                        }
                    }
                    depsOkay = false;
                    break;
                }

                if (depsOkay)
                {
                    MakeFileInfo targetFileInfo = getOrCreateFile(target);
                    MakeStaticRule staticRule;
                    if (baseRule != null)
                    {
                        staticRule = new MakeStaticRule(baseRule);
                    }
                    else
                    {
                        staticRule = new MakeStaticRule(targetFileInfo);
                    }
                    staticRule.addDependencies(staticDeps);
                    staticRule.setCommand(rule.getCommand());
                    targetFileInfo.setRule(staticRule);
                    return staticRule;
                }
            }
        }

        // no matching pattern rule found
        return baseRule;
    }

    /**
     * Scans the database for special targets and applies their associated
     * attributes to each of their dependencies.
     */
    public void applySpecialTargets()
    {
        MakeStaticRule rule = getStaticRule(".PHONY");
        if (rule != null)
        {
            Iterator iter = rule.getDependencies().iterator();
            while (iter.hasNext())
            {
                MakeFileInfo depFileInfo = (MakeFileInfo) iter.next();
                depFileInfo.setPhony(true);
            }
        }
    }
}
