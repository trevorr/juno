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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a name-indexed map of all the components in a Vera source tree.
 * 
 * @author Trevor Robinson
 */
public class VeraComponentMap
    implements Serializable
{
    private static final long serialVersionUID = 3690193248312440368L;

    private final Map<String, VeraAbsComponent> components = new LinkedHashMap<String, VeraAbsComponent>();

    /**
     * Constructs a new component map.
     */
    public VeraComponentMap()
    {
        // do nothing
    }

    /**
     * Returns the component with the given name, or null if no such component
     * exists.
     *
     * @param name the name of the component
     * @return VeraAbsComponent
     */
    public VeraAbsComponent getComponent(String name)
    {
        return components.get(name);
    }

    /**
     * Returns a collection of all components in this map.
     *
     * @return Collection of VeraAbsComponent
     */
    public Collection<VeraAbsComponent> getComponents()
    {
        return components.values();
    }

    /**
     * Adds the given component to this component manager. If a component with
     * the same name already exists, it will be replaced.
     *
     * @param component a component
     */
    public void addComponent(VeraAbsComponent component)
    {
        components.put(component.getName(), component);
    }
}
