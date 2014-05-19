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

package com.newisys.juno.runtime;

import java.util.LinkedList;
import java.util.List;

import com.newisys.eventsim.SimulationThread;

/**
 * Used internally by the Juno runtime to track threads forked from different
 * contexts, which is necessary to implement wait_child().
 * 
 * @author Trevor Robinson
 */
final class JunoThreadContext
{
    private final JunoThreadContext parentContext;
    private final List<SimulationThread> forkedThreads = new LinkedList<SimulationThread>();

    public JunoThreadContext(JunoThreadContext parentContext)
    {
        this.parentContext = parentContext;
    }

    public JunoThreadContext getParentContext()
    {
        return parentContext;
    }

    public List<SimulationThread> getForkedThreads()
    {
        return forkedThreads;
    }

    public void addForkedThread(SimulationThread t)
    {
        forkedThreads.add(t);
    }
}
