/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.langschema.vera;

import java.io.Serializable;

/**
 * Represents Vera signal surround-by-X parameters.
 * 
 * @author Trevor Robinson
 */
public final class VeraSurrXParams
    implements Serializable
{
    private static final long serialVersionUID = 4050477915689267761L;

    private final VeraSurrXTransition transition;
    private final int timeToX;
    private final int timeToValue;

    public VeraSurrXParams(
        VeraSurrXTransition transition,
        int timeToX,
        int timeToValue)
    {
        this.transition = transition;
        this.timeToX = timeToX;
        this.timeToValue = timeToValue;
    }

    public VeraSurrXTransition getTransition()
    {
        return transition;
    }

    public int getTimeToX()
    {
        return timeToX;
    }

    public int getTimeToValue()
    {
        return timeToValue;
    }

    public String toString()
    {
        return "#" + transition.getCode() + "(" + timeToX + ", " + timeToValue
            + ")";
    }
}
