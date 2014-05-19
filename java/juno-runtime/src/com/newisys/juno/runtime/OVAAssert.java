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

import com.newisys.ova.OVAAssertAction;
import com.newisys.ova.OVAAssertEventType;

/**
 * An implementation of the OpenVera OVAAssert class.
 * 
 * @author Jon Nall
 */
public class OVAAssert
    extends JunoObject
{
    private com.newisys.ova.OVAAssert ovaAssert;

    /**
     * Only the OVAEngine class is allowed to create new OVAAssert objects.
     *
     * @param ovaAssert the underlying Jove OVAAssert
     */
    OVAAssert(com.newisys.ova.OVAAssert ovaAssert)
    {
        this.ovaAssert = ovaAssert;
    }

    /**
     * Returns the name of this OVAAssert.
     *
     * @return the name of this OVAAssert
     */
    public String GetName()
    {
        return ovaAssert.getName();
    }

    /**
     * Enables the specified OVAEvent to be triggered when the event type
     * associated with it occurs.
     * <P>
     * This is currently unimplemented.
     *
     * @param event the OVAEvent to trigger
     * @throws UnsupportedOperationException unconditionally
     */
    public void EnableTrigger(OVAEvent event)
    {
        throw new UnsupportedOperationException(
            "OVAAssert.EnableTrigger is not yet supported");
    }

    /**
     * Disables the specified OVAEvent from being triggered when the event type
     * associated with it occurs.
     * <P>
     * This is currently unimplemented.
     *
     * @param event the OVAEvent to disable from triggering
     * @throws UnsupportedOperationException unconditionally
     */
    public void DisableTrigger(OVAEvent event)
    {
        throw new UnsupportedOperationException(
            "OVAAssert.DisableTrigger is not yet supported");
    }

    /**
     * Causes this OVAAssert to perform the specified action. Valid values for
     * <code>action</code> are {@link OVAEngine#OVA_RESET},
     * {@link OVAEngine#OVA_ENABLE}, and {@link OVAEngine#OVA_DISABLE}.
     * <P>
     * <code>OVA_RESET</code> resets this OVAAssert. All matching attempts are
     * cancelled and new attempts will begin on the following cycle.
     * <P>
     * <code>OVA_ENABLE</code> enables this OVAAssert. Matching attempts will
     * begin on the following cycle and will stay enabled unless a subsequent
     * <code>OVA_DISABLE</code> action is performed.
     * <P>
     * <code>OVA_DISABLE</code> disables this OVAAssert. Matching attempts will
     * end immediately and will stay disabled unless a subsequent
     * <code>OVA_ENABLE</code> action is performed.
     *
     * @param action one of <code>OVA_RESET</code>, <code>OVA_ENABLE</code>,
     *      or <code>OVA_DISABLE</code>
     */
    public void DoAction(int action)
    {
        OVAAssertAction ovaAction = null;

        switch (action)
        {
        case OVAEngine.OVA_RESET:
            ovaAction = OVAAssertAction.Reset;
            break;
        case OVAEngine.OVA_ENABLE:
            ovaAction = OVAAssertAction.EnableNewAttempts;
            break;
        case OVAEngine.OVA_DISABLE:
            ovaAction = OVAAssertAction.DisableNewAttempts;
            break;
        default:
            throw new IllegalArgumentException("Unsupported OVAAssert action: "
                + action);
        }

        final int attemptID = 0; // attemptID doesn't matter for these actions
        ovaAssert.doAction(ovaAction, attemptID);
    }

    /**
     * Enables counting of successful or failed match attmpts, depending on
     * the specified operation. Valid values for <code>op</code> are
     * {@link OVAEngine#OVA_SUCCESS} and {@link OVAEngine#OVA_FAILURE}.
     * <P>
     * Note that once counting has started, it cannot be stopped.
     *
     * @param op one of <code>OVA_SUCCESS</code> or <code>OVA_FAILURE</code>
     */
    public void EnableCount(int op)
    {
        assert (op == OVAEngine.OVA_SUCCESS || op == OVAEngine.OVA_FAILURE);

        OVAAssertEventType eventType = (op == OVAEngine.OVA_SUCCESS)
            ? OVAAssertEventType.AttemptSuccess
            : OVAAssertEventType.AttemptFailure;

        ovaAssert.enableCount(eventType);
    }

    /**
     * Retrieves the current success or failure count of this OVAAssert,
     * depending on the specified operation. Valid values for <code>op</code>
     * are {@link OVAEngine#OVA_SUCCESS} and {@link OVAEngine#OVA_FAILURE}.
     *
     * @param op one of <code>OVA_SUCCESS</code> or <code>OVA_FAILURE</code>
     * @return the number of times this OVAAssert has matched successfully or
     *      failed to match, depending on <code>op</code>
     */
    public int GetCount(int op)
    {
        assert (op == OVAEngine.OVA_SUCCESS || op == OVAEngine.OVA_FAILURE);
        OVAAssertEventType eventType = (op == OVAEngine.OVA_SUCCESS)
            ? OVAAssertEventType.AttemptSuccess
            : OVAAssertEventType.AttemptFailure;

        // Vera returns an int here
        final long count = ovaAssert.getCount(eventType);
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return ovaAssert.toString();
    }
}
