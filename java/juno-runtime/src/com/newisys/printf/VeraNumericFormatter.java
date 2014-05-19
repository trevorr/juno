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

import com.newisys.juno.runtime.JunoEnum;
import com.newisys.juno.runtime.JunoString;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorFormat;

/**
 * Formatter used for numeric conversions.
 * 
 * @author Jon Nall
 */
final class VeraNumericFormatter
    extends VeraBaseFormatter
{
    private static BitVectorFormat bvFormatter = new BitVectorFormat();
    static
    {
        // don't print length/radix information on BitVectors
        bvFormatter.setPrintRadix(false);
        bvFormatter.setPrintLength(false);
        bvFormatter.setXzCompression(false);
    }

    public int getMaximumLength(PrintfSpec spec)
    {
        int baseLength = 0;

        if (spec.obj instanceof Number || spec.obj instanceof Boolean)
        {
            spec.cachedLength = getNaturalWidth(spec);
            baseLength += spec.cachedLength;
        }
        else if (spec.obj instanceof Enum< ? >)
        {
            spec.cachedLength = getNaturalWidth(spec);
            baseLength += spec.cachedLength;
            baseLength += 11; // "(ENUM_VAR:" ... ")"
        }
        else if (spec.obj instanceof String || spec.obj instanceof JunoString)
        {
            final String s;
            if (spec.obj instanceof JunoString)
            {
                s = ((JunoString) spec.obj).toStringOrBlank();
            }
            else
            {
                s = (String) spec.obj;
            }
            final int length = s.length();

            baseLength += length
                * getNaturalWidth(getRadix(spec.conversionSpec), 8);
        }
        else if (spec.obj == null)
        {
            baseLength += NULL_STRING_LENGTH;
        }

        return Math.max(baseLength, spec.widthIsValid ? spec.width : 0);
    }

    public boolean consumesArg(PrintfSpec spec)
    {
        // all numeric conversions consume an argument
        return true;
    }

    public void format(PrintfSpec spec, StringBuilder buf)
    {
        if (spec.obj == null)
        {
            formatNullReference(spec, buf);
            return;
        }

        Number[] values = { null };
        boolean isEnum = spec.obj instanceof Enum< ? >;
        if (spec.obj instanceof String || spec.obj instanceof JunoString)
        {
            final String s;
            if (spec.obj instanceof JunoString)
            {
                s = ((JunoString) spec.obj).toStringOrBlank();
            }
            else
            {
                s = (String) spec.obj;
            }

            final int length = s.length();
            if (length == 0)
            {
                return;
            }

            final int radix = getRadix(spec.conversionSpec);
            if (radix == 16)
            {
                // the empty string is returned for hexadecimal conversions
                return;
            }

            values = new Integer[length];
            final int naturalLength = getNaturalWidth(radix, 8);

            for (int i = 0; i < length; ++i)
            {
                char c = s.charAt(i);
                if (getRadix(spec.conversionSpec) == 2)
                {
                    // for binary:
                    // the first character in the string occupies the MSB bits in
                    // the vector, but each byte of the string is reversed
                    char revC = 0;
                    for (int j = 0; j < 8; ++j)
                    {
                        revC |= ((c >> j) & 1) << (8 - j - 1);
                    }
                    values[i] = (int) revC;
                }
                else
                {
                    values[i] = (int) c;
                }
            }

            spec = new PrintfSpec(spec.startIdx, spec.endIdx,
                spec.conversionSpec, spec.flags, 0, false, 0, false, values[0]);
            spec.cachedLength = (radix == 2) ? naturalLength : 0;
        }
        else if (spec.obj instanceof Enum< ? >)
        {
            final int ordinal;
            if (spec.obj instanceof JunoEnum< ? >)
            {
                if (!((JunoEnum< ? >) spec.obj).isDefined())
                {
                    buf.append("(ENUM_VAR:X)");
                    return;
                }
                else
                {
                    ordinal = ((JunoEnum< ? >) spec.obj).toInt();
                }
            }
            else
            {
                ordinal = ((Enum< ? >) spec.obj).ordinal();
            }
            values[0] = ordinal;

            // force a width of 0 for these values
            final PrintfSpec tmpSpec = new PrintfSpec(spec.startIdx,
                spec.endIdx, spec.conversionSpec, spec.flags,
                spec.cachedLength, true, 0, false, spec.obj);
            tmpSpec.cachedLength = spec.cachedLength;
            tmpSpec.cachedString = spec.cachedString;
            spec = tmpSpec;
        }
        else if (spec.obj instanceof Boolean)
        {
            values[0] = ((Boolean) spec.obj).booleanValue() ? Bit.ONE
                : Bit.ZERO;
        }
        else if (spec.obj instanceof Number)
        {
            values[0] = (Number) spec.obj;
        }
        else
        {
            throw new InvalidFormatSpecException(
                "Unsupported numeric conversion from type: "
                    + spec.obj.getClass());
        }

        if (isEnum)
        {
            assert (values.length == 1);
            buf.append("(ENUM_VAR:");
        }

        for (int i = 0; i < values.length; ++i)
        {
            formatNumber(values[i], spec, buf, i > 0);
        }

        if (isEnum)
        {
            buf.append(")");
        }
    }

    private void formatNumber(
        Number number,
        PrintfSpec spec,
        StringBuilder buf,
        boolean forceNaturalWidth)
    {
        final int naturalWidth;
        if (spec.cachedLength == -1)
        {
            naturalWidth = getNaturalWidth(spec);
        }
        else
        {
            naturalWidth = spec.cachedLength;
        }

        String valueString = null;
        final int numBits = PrintfUtils.getWidth(number);
        final int radix = getRadix(spec.conversionSpec);
        if (number instanceof BitVector)
        {
            valueString = bvFormatter.format((BitVector) number, radix);
        }
        else if (number instanceof Bit)
        {
            Bit b = (Bit) number;
            if (radix == 2)
            {
                valueString = b.toString().toLowerCase();
            }
            else if (radix == 8 || radix == 10)
            {
                if (b.isXZ())
                {
                    valueString = "?";
                }
                else
                {
                    valueString = b.toString();
                }
            }
            else
            {
                assert (radix == 16);
                valueString = b.toString();
            }
        }
        else
        {
            long value = number.longValue();
            // conversions to binary, octal, or hex get printed as unsigned
            // values
            boolean printUnsigned = (!isSignedConversion(spec) || !isSignedType(spec.obj));
            if (printUnsigned && numBits < 64)
            {
                value &= ((1L << numBits) - 1);
            }

            if (radix == 2)
            {
                valueString = Long.toBinaryString(value);
            }
            else if (radix == 8)
            {
                valueString = Long.toOctalString(value);
            }
            else if (radix == 10)
            {
                valueString = Long.toString(value);
            }
            else if (radix == 16)
            {
                valueString = Long.toHexString(value);
            }
        }

        final char leadingChar = valueString.charAt(0);
        final boolean unknownDecimal = leadingChar == '?'
            && isDecimal(spec.conversionSpec);
        assert (!unknownDecimal || valueString.length() == 1);

        // use the maximum of the natural width and the specified width unless
        // the specified width is zero, in which case use the minimum width
        final int width;
        final int length = valueString.length();
        if (unknownDecimal)
        {
            width = 0;
        }
        else if (forceNaturalWidth || !spec.widthIsValid)
        {
            width = naturalWidth;
        }
        else if (spec.width > length)
        {
            width = spec.width;
        }
        else
        {
            width = length;
        }

        final int numPaddingChars = Math.max(width - length, 0);
        final boolean leftJustify = spec.flags
            .contains(PrintfFlag.LEFT_JUSTIFY);
        final char paddingChar = getPaddingChar(spec, leadingChar);
        if (!leftJustify)
        {
            for (int i = 0; i < numPaddingChars; ++i)
            {
                buf.append(paddingChar);
            }
        }
        buf.append(valueString);
        if (leftJustify)
        {
            for (int i = 0; i < numPaddingChars; ++i)
            {
                buf.append(' ');
            }
        }
    }

    private int getRadix(char conversionSpec)
    {
        switch (Character.toLowerCase(conversionSpec))
        {
        case 'b':
            return 2;
        case 'o':
            return 8;
        case 'd':
        case 'i':
        case 'u':
            return 10;
        case 'h':
        case 'x':
            return 16;
        default:
            throw new InvalidFormatSpecException(
                "Unsupported numeric conversion: " + conversionSpec);
        }
    }

    private boolean isSignedConversion(PrintfSpec spec)
    {
        switch (Character.toLowerCase(spec.conversionSpec))
        {
        case 'd':
        case 'i':
            return true;
        default:
            return false;
        }
    }

    private boolean isSignedType(Object obj)
    {
        // BitVector and Bit are unsigned types. All other types are signed
        if (obj instanceof BitVector)
        {
            return false;
        }
        else if (obj instanceof Bit)
        {
            return false;
        }

        return true;
    }

    private boolean isDecimal(char conversionSpec)
    {
        switch (Character.toLowerCase(conversionSpec))
        {
        case 'd':
        case 'i':
        case 'u':
            return true;
        default:
            return false;
        }
    }

    private int getNaturalWidth(PrintfSpec spec)
    {
        int radix = getRadix(spec.conversionSpec);
        int numBits = 0;

        final Object obj = spec.obj;
        int extraWidth = 0;
        if (obj instanceof Enum< ? >)
        {
            numBits = 32;
        }
        else if (obj instanceof String)
        {
            numBits = ((String) obj).length() * 8;
        }
        else if (obj instanceof JunoString)
        {
            numBits = ((JunoString) obj).toStringOrBlank().length() * 8;
        }
        else
        {
            numBits = PrintfUtils.getWidth(obj);
            if (isDecimal(spec.conversionSpec) && isSignedType(spec.obj))
            {
                // decimal conversions from numeric types have a natural width
                // of 11 although it calculates as 10. possibly for the sign
                // character? although that doesn't make sense for %u as it also
                // has a natural width of 11.
                extraWidth = 1;
            }
        }

        return getNaturalWidth(radix, numBits) + extraWidth;
    }

    private int getNaturalWidth(final int radix, final int numBits)
    {
        double ln2radix = Math.log(radix) / Math.log(2);
        return (int) (Math.ceil(numBits / ln2radix));
    }

    protected final char getPaddingChar(PrintfSpec spec, char leadingChar)
    {
        char paddingChar = ' ';

        if (spec.obj instanceof Enum< ? >)
        {
            // enums converted to numeric values are padded with zero
            // unless they're converted to decimal, in which case spaces are
            // used.
            paddingChar = (isDecimal(spec.conversionSpec)) ? ' ' : '0';
        }
        else if (spec.obj instanceof Number)
        {
            if (!Character.isDigit(leadingChar))
            {
                final char lowerLeadingChar = Character
                    .toLowerCase(leadingChar);

                if (lowerLeadingChar == 'x' || lowerLeadingChar == 'z')
                {
                    paddingChar = leadingChar;
                }
                else if (Character.isLetter(leadingChar))
                {
                    paddingChar = '0';
                }
                else
                {
                    paddingChar = ' ';
                }
            }
            else if (isDecimal(spec.conversionSpec))
            {
                paddingChar = ' ';
            }
            else
            {
                paddingChar = '0';
            }
        }

        return paddingChar;
    }
}
