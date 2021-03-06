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

/**
 * Base class for Vera-compatible conversion formatters.
 * 
 * @author Jon Nall
 */
abstract class VeraBaseFormatter
    implements ConversionFormatter
{
    protected static final String NULL_STRING = "(NULL)";
    protected static final int NULL_STRING_LENGTH = NULL_STRING.length();

    protected final void formatNullReference(PrintfSpec spec, StringBuilder buf)
    {
        buf.append(NULL_STRING);
    }
}
