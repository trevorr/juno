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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.newisys.dv.DV;
import com.newisys.ova.OVAConfigSwitch;
import com.newisys.ova.OVAEngineAction;

/**
 * An implementation of the OpenVera OVAEngine class.
 * 
 * @author Jon Nall
 */
public final class OVAEngine
    extends JunoObject
{
    // Ova_Bool
    public static final int OVA_FALSE = 0;
    public static final int OVA_TRUE = 1;

    // Bastardized Ova_ConfigSwitch, Ova_EngAction and Ova_AssertEvent
    public static final int OVA_INFO = 0;
    public static final int OVA_QUIET = 1;
    public static final int OVA_REPORT = 2;
    public static final int OVA_MANAGE_ATTEMPTS = 3;
    public static final int OVA_RESET = 4;
    public static final int OVA_FINISH = 5;
    public static final int OVA_TERMINATE = 6;
    public static final int OVA_DISABLE = 7;
    public static final int OVA_ENABLE = 8;
    public static final int OVA_FAILURE = 9;
    public static final int OVA_SUCCESS = 10;
    public static final int OVA_ALL = 11;
    public static final int OVA_NULL = 12;
    public static final int OVA_NO_OP = 14;

    // OVA constants
    public static final int OVA_CLIENTID_NULL = 0;
    public static final int OVA_ASSERTID_NULL = 0;

    // Static members/methods
    private static boolean instanceExists = false;

    // Instance members/methods
    private final com.newisys.ova.OVAEngine ovaEngine;
    private Map<com.newisys.ova.OVAAssert, OVAAssert> assertMap = new HashMap<com.newisys.ova.OVAAssert, OVAAssert>();
    private List<OVAAssert> assertList = null;
    private Iterator<OVAAssert> assertIterator = null;

    public OVAEngine()
    {
        if (instanceExists)
        {
            Juno.error("ERROR: There can be only one instance of OVAEngine");
        }

        assert (DV.simulation.hasOVASupport());
        ovaEngine = DV.simulation.getOVAEngine();
        instanceExists = true;
    }

    /**
     * Returns the OVA API version as returned by the underlying OVA implementation
     * @return a String representing the OVA API Version
     */
    public String getApiVersion()
    {
        return ovaEngine.getApiVersion();
    }

    /**
     * Configures this OVAEngine's options. Valid values for <code>op</code>
     * are {@link #OVA_INFO}, {@link #OVA_QUIET}, and {@link #OVA_REPORT}.
     * Valid values for <code>value</code> are {@link #OVA_TRUE} and
     * {@link #OVA_FALSE}.
     * <P>
     * Defaults for the options are:<br>
     * <code><table border=1>
     * <tr><td><b>Option</b></td><td><b>Default Value</b></td><td><b>Description</b></td></tr>
     * <tr><td>OVA_INFO</td><td>OVA_FALSE</td><td>Line Information in assertion messages</td></tr>
     * <tr><td>OVA_PRINT</td><td>OVA_FALSE</td><td>Assertion messages printed at runtime</td></tr>
     * <tr><td>OVA_REPORT</td><td>OVA_TRUE</td><td>Assertion report at the end of simulation</td></tr>
     * </table></code>
     * @param op one of <code>OVA_INFO</code>, <code>OVA_QUIET</code>, or
     *      <code>OVA_REPORT</code>
     * @param value one of <code>OVA_TRUE</code> or <code>OVA_FALSE</code>
     */
    public void Configure(int op, int value)
    {
        assert (op == OVA_INFO || op == OVA_QUIET || op == OVA_REPORT);
        assert (value == OVA_TRUE || value == OVA_FALSE);

        OVAConfigSwitch configSwitch = null;

        switch (op)
        {
        case OVA_INFO:
            configSwitch = OVAConfigSwitch.ShowLineInfo;
            break;
        case OVA_QUIET:
            configSwitch = OVAConfigSwitch.Quiet;
            break;
        case OVA_REPORT:
            configSwitch = OVAConfigSwitch.PrintReport;
            break;
        default:
            throw new IllegalArgumentException(
                "Unsupported configuration switch: " + op);
        }

        ovaEngine.configure(configSwitch, (value == OVA_TRUE) ? true : false);
    }

    /**
     * Causes this OVAEngine to perform the specified action. Valid values for
     * <code>action</code> are {@link #OVA_RESET} and {@link #OVA_TERMINATE}.
     * <P>
     * <code>OVA_RESET</code> resets all OVA assertions and expressions. All
     * matching attempts are cancelled and new attempts will begin on the
     * following cycle.
     * <P>
     * <code>OVA_TERMINATE</code> terminates all current matching attempts. No
     * new attempts to match will started. Attempts to match cannot be started
     * again once this action is performed.
     *
     * @param action one of <code>OVA_RESET</code> or <code>OVA_TERMINATE</code>
     */
    public void DoAction(int action)
    {
        OVAEngineAction ovaAction = null;

        switch (action)
        {
        case OVA_RESET:
            ovaAction = OVAEngineAction.Reset;
            break;
        case OVA_TERMINATE:
            ovaAction = OVAEngineAction.Terminate;
            break;
        default:
            throw new IllegalArgumentException("Unsupported OVAEngine action: "
                + action);
        }

        ovaEngine.doAction(ovaAction);
    }

    /**
     * Returns the first assertion from the underlying OVA implementation. If
     * there are no OVA assertions in the design, <code>null</code> is returned.
     *
     * @return the first OVAAssert in the design, or <code>null</code> if no
     *      OVAAsserts are present
     */
    public OVAAssert GetFirstAssert()
    {
        if (assertList == null)
        {
            List<com.newisys.ova.OVAAssert> ovaAssertList = ovaEngine
                .getAsserts();
            this.assertList = new ArrayList<OVAAssert>(ovaAssertList.size());
            for (final com.newisys.ova.OVAAssert a : ovaAssertList)
            {
                OVAAssert curAssert = findAssert(a);
                assert (curAssert != null);
                this.assertList.add(curAssert);
            }
        }
        assertIterator = assertList.iterator();

        return (assertIterator.hasNext()) ? assertIterator.next() : null;
    }

    /**
     * Returns the next assertion from the underlying OVA implementation. If
     * there are no more OVA assertions in the design, <code>null</code> is
     * returned. This method may be called repeatedly after
     * {@link #GetFirstAssert()}.
     *
     * @return the next OVAAssert in the design, or <code>null</code> if no more
     *      OVAAsserts are present
     */
    public OVAAssert GetNextAssert()
    {
        if (assertIterator == null)
        {
            return GetFirstAssert();
        }

        return (assertIterator.hasNext()) ? assertIterator.next() : null;
    }

    /**
     * Returns the OVAAssert with the specified name.
     *
     * @param name the name of the desired OVAAssert
     * @return the OVAAssert with the specified name, or <code>null</code> if
     *      no such OVAAssert exists
     */
    public OVAAssert GetAssert(String name)
    {
        com.newisys.ova.OVAAssert ovaAssert = ovaEngine.getAssert(name);
        return findAssert(ovaAssert);
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
        assert (event.eventType == OVA_RESET || event.eventType == OVA_TERMINATE);

        throw new UnsupportedOperationException(
            "OVAEngine.EnableTrigger is not yet supported");
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
        assert (event.eventType == OVA_RESET || event.eventType == OVA_TERMINATE);

        throw new UnsupportedOperationException(
            "OVAEngine.DisableTrigger is not yet supported");
    }

    private OVAAssert findAssert(com.newisys.ova.OVAAssert ovaAssert)
    {
        if (ovaAssert == null)
        {
            return null;
        }

        if (assertMap.containsKey(ovaAssert))
        {
            OVAAssert veraAssert = assertMap.get(ovaAssert);
            assert (veraAssert != null);
            return veraAssert;
        }
        else
        {
            OVAAssert veraAssert = new OVAAssert(ovaAssert);
            assertMap.put(ovaAssert, veraAssert);
            return veraAssert;
        }
    }
}
