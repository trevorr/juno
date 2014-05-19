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

/**
 * An implementation of the OpenVera OVAEvent class.
 * 
 * @author Jon Nall
 */
public class OVAEvent
    extends JunoObject
{
    /**
     * Publicly usable JunoEvent
     */
    public JunoEvent Event = null;

    int eventType;

    /**
     * Create an OVA Event associated with the type of event described by the
     * event parameter.
     * <P>
     * When used in the context of an OVAEngine object, event can take on the
     * values of {@link OVAEngine#OVA_RESET} or {@link OVAEngine#OVA_TERMINATE}.
     * <P>
     * When used in the context of an OVAAssert object, event can take on the
     * values of {@link OVAEngine#OVA_RESET}, {@link OVAEngine#OVA_SUCCESS},
     * {@link OVAEngine#OVA_FAILURE}, {@link OVAEngine#OVA_ENABLE},
     * {@link OVAEngine#OVA_DISABLE}, or {@link OVAEngine#OVA_ALL}.
     *
     * @param eventType the event type to associate with this OVAEvent
     */
    public OVAEvent(int eventType)
    {
        assert (eventType == OVAEngine.OVA_RESET
            || eventType == OVAEngine.OVA_TERMINATE
            || eventType == OVAEngine.OVA_SUCCESS
            || eventType == OVAEngine.OVA_FAILURE
            || eventType == OVAEngine.OVA_ENABLE
            || eventType == OVAEngine.OVA_DISABLE || eventType == OVAEngine.OVA_ALL);
        this.eventType = eventType;

        throw new UnsupportedOperationException("OVAEvent is not yet supported");
    }

    /**
     * Wait on the event, blocking the current thread.
     *
     * @see #GetNextEvent
     */
    public void Wait()
    {
        throw new UnsupportedOperationException(
            "OVAEvent.Wait() is not yet supported");
    }

    /**
     * Returns the event type that unblocked this thread. If more than one event
     * caused the thread to unblock, this method may be called multiple times,
     * until {@link OVAEngine#OVA_NULL} is returned.
     * <P>
     * If there are multiple calls to {@link #Wait} without intervening calls to
     * getNextEvent, getNextEvent will return the set of event types corresponding
     * to the last Wait().
     *
     * @return the event type that unblocked this thread or
     *      {@link OVAEngine#OVA_NULL} if there are no more events.
     */
    public int GetNextEvent()
    {
        throw new UnsupportedOperationException(
            "OVAEvent.GetNextEvent() is not yet supported");
    }
}
