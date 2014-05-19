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

import com.newisys.eventsim.PulseEvent;

/**
 * A Jove Event with the semantics of Vera's built-in events.
 * 
 * @author Trevor Robinson
 */
public class JunoEvent
    extends PulseEvent
{
    private boolean on = false;
    private long onDuringTime = -1;
    private int handshakeCount = 0;

    /**
     * Creates a new JunoEvent.
     */
    public JunoEvent()
    {
    }

    /**
     * Creates a new JunoEvent with the specified name.
     *
     * @param name the name of this JunoEvent
     */
    public JunoEvent(String name)
    {
        super(name);
    }

    boolean isOn()
    {
        return on;
    }

    void setOn(boolean on)
    {
        this.on = on;
    }

    long getOnDuringTime()
    {
        return onDuringTime;
    }

    void setOnDuringTime(long onDuringTime)
    {
        this.onDuringTime = onDuringTime;
    }

    void giveHandshake()
    {
        ++handshakeCount;
    }

    boolean checkHandshake(boolean acquire)
    {
        if (handshakeCount > 0)
        {
            if (acquire) --handshakeCount;
            return true;
        }
        else
        {
            return false;
        }
    }
}
