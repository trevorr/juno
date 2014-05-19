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

import com.newisys.printf.InvalidFormatSpecException;
import com.newisys.printf.PrintfSpec;

/**
 * Formatter used for conversions that do not consume an argument.
 * 
 * @author Jon Nall
 */
final class VeraNoArgFormatter
    extends VeraBaseFormatter
{
    public final int getMaximumLength(PrintfSpec spec)
    {
        switch (Character.toLowerCase(spec.conversionSpec))
        {
        case '%': // %% always returns "%"
            return 1;
        case '_': // %_ always returns "."
            return 1;
        case 'p': // %p returns "Juno"
            return 4;
        case 'v': // %v always return "<null>"
            return 6;
        case 'm':
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            String[] stackLevels = new String[trace.length];
            int levelsToIgnore = (spec.widthIsValid) ? 0 : spec.width;
            final String stackTraceHeader = "Stack Trace:\n";

            // get the appropriate number of stack levels
            int stackLevel = 0;
            int bufLength = stackTraceHeader.length();

            for (int i = 0; i < trace.length; ++i)
            {
                final String className = trace[i].getClassName();
                if (className.startsWith("com.newisys.juno.runtime")
                    || className.startsWith("com.newisys.printf"))
                {
                    continue;
                }

                // remove "width" levels from the printout, but always print
                // at least 1 level
                if (levelsToIgnore > 0 && i != (trace.length - 1))
                {
                    --levelsToIgnore;
                    continue;
                }

                String curLevel = trace[i].toString();
                bufLength += curLevel.length();
                bufLength += 6; // "\t at " ... "\n"
                stackLevels[stackLevel++] = curLevel;
            }

            StringBuilder buf = new StringBuilder(bufLength);
            buf.append(stackTraceHeader);
            for (final String s : stackLevels)
            {
                if (s == null) continue;
                buf.append("\t at ");
                buf.append(s);
                buf.append("\n");
            }

            spec.cachedString = buf.toString();
            return bufLength;
        default:
            // this Formatter should not be registered with any conversions
            // except those above
            throw new InvalidFormatSpecException(
                "Unsupported no-argument format specifier: "
                    + spec.conversionSpec);
        }
    }

    public final boolean consumesArg(PrintfSpec spec)
    {
        // none of these conversions consume an argument
        return false;
    }

    public void format(PrintfSpec spec, StringBuilder buf)
    {
        switch (Character.toLowerCase(spec.conversionSpec))
        {
        case '%':
            buf.append('%');
            break;
        case '_': // TODO: Do simulators ever use a path separator other than "."?
            buf.append('.');
            break;
        case 'p': // TODO: What should Juno return for %p (currently "Juno")?
            buf.append("Juno");
            break;
        case 'v': // TODO: What should Juno return for %v (currently "<null>")?
            buf.append("<null>");
            break;
        case 'm':
            assert (spec.cachedString != null);
            buf.append(spec.cachedString);
            break;
        default:
            // this Formatter should not be registered with any conversions
            // except those above
            throw new InvalidFormatSpecException(
                "Unsupported no-argument format specifier: "
                    + spec.conversionSpec);
        }
    }
}
