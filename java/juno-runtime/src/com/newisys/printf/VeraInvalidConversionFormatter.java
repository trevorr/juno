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

package com.newisys.printf;

import com.newisys.printf.PrintfSpec;

/**
 * Formatter used for invalid/unknown conversions.
 * 
 * @author Jon Nall
 */
final class VeraInvalidConversionFormatter
    extends VeraBaseFormatter
{
    public int getMaximumLength(PrintfSpec spec)
    {
        // invalid arguments don't print anything
        return 0;
    }

    public boolean consumesArg(PrintfSpec spec)
    {
        // invalid arguments don't consume an argument
        return false;
    }

    public void format(PrintfSpec spec, StringBuilder buf)
    {
        // invalid arguments don't print anything
        return;
    }
}
