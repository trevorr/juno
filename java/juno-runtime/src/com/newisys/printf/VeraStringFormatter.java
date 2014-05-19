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

import com.newisys.juno.runtime.JunoString;
import com.newisys.printf.PrintfSpec;

/**
 * Formatter used for string conversions.
 * 
 * @author Jon Nall
 */
final class VeraStringFormatter
    extends VeraBaseFormatter
{
    // for some cases we fall back to the base formatter
    private static final StringFormatter sFormatter = new StringFormatter();

    public final int getMaximumLength(PrintfSpec spec)
    {
        final Object o = spec.obj;
        if (o == null)
        {
            spec.cachedString = NULL_STRING;
            spec.cachedLength = NULL_STRING_LENGTH;
            return spec.cachedLength;
        }
        else if (Character.toLowerCase(spec.conversionSpec) == 'c')
        {
            // Vera doesn't respect width for characters
            spec.cachedLength = 1;
            return 1;
        }
        else if (o instanceof String)
        {
            spec.cachedLength = ((String) o).length();
            spec.cachedString = (String) o;
        }
        else if (o instanceof JunoString)
        {
            String s = ((JunoString) o).toStringOrNull();
            if (s == null)
            {
                spec.cachedString = NULL_STRING;
                spec.cachedLength = NULL_STRING_LENGTH;
                return spec.cachedLength;
            }
            else
            {
                spec.cachedLength = s.length();
                spec.cachedString = s;
            }
        }
        else if (o instanceof Number)
        {
            final int length = PrintfUtils.getWidth(o);
            spec.cachedLength = (length / 8) + ((length % 8 == 0) ? 0 : 1);
        }
        else
        {
            String s = spec.obj.toString();
            spec.cachedLength = s.length();
            spec.cachedString = s;
        }

        assert (spec.cachedLength != -1);
        return Math
            .max(spec.cachedLength, (spec.widthIsValid ? spec.width : 0));
    }

    public final boolean consumesArg(PrintfSpec spec)
    {
        // all string conversions consume an argument
        return true;
    }

    public final void format(PrintfSpec spec, StringBuilder buf)
    {
        final boolean doChar = Character.toLowerCase(spec.conversionSpec) == 'c';
        final boolean doString = Character.toLowerCase(spec.conversionSpec) == 's';
        assert (doChar || doString);

        // length should always be cached in this formatter
        assert (spec.cachedLength != -1);

        if (spec.obj == null)
        {
            formatNullReference(spec, buf);
        }
        else if (doChar)
        {
            // special/optimized cases -- %c with cachedString
            if (spec.obj instanceof Enum< ? >)
            {
                // %c on enumerations prints nothing
            }
            else if (spec.cachedString != null)
            {
                if (spec.cachedString.length() == 0)
                {
                    // %c on the empty string appends nothing
                }
                else
                {
                    buf.append(spec.cachedString.charAt(0));
                }
            }
            else
            {
                // override the PrintfSpec for this value to have a width of
                // zero
                final PrintfSpec charSpec = new PrintfSpec(spec.startIdx,
                    spec.endIdx, Character.toLowerCase(spec.conversionSpec),
                    spec.flags, 0, true, 0, false, spec.obj);
                charSpec.cachedLength = spec.cachedLength;
                charSpec.cachedString = spec.cachedString;
                sFormatter.format(charSpec, buf);
            }
        }
        else
        {
            // otherwise, Vera strings format the same as other strings, but
            // make sure to use a lowercase conversion specifier
            final PrintfSpec veraSpec = new PrintfSpec(spec.startIdx,
                spec.endIdx, Character.toLowerCase(spec.conversionSpec),
                spec.flags, spec.width, spec.widthIsValid, spec.precision,
                spec.precisionIsValid, spec.obj);
            veraSpec.cachedLength = spec.cachedLength;
            veraSpec.cachedString = spec.cachedString;
            sFormatter.format(veraSpec, buf);
        }
    }
}
