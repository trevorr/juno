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

import com.newisys.printf.ConversionFormatter;
import com.newisys.printf.PrintfFlag;
import com.newisys.printf.PrintfFormatter;

//Vera's printf function is a bit inconsistent, with quite a few corner cases.
//What follows is a list of cases I've found where Vera's printf is either
//not consistent or differs from what I expected. I've tried to be thorough,
//but some cases may be missing.
//
//Conversion specifiers. Vera adds the following:
//  %m prints a stack track. it takes a width specifier which specifies how
//     many levels of stack to print. if the stack has N levels where the 0th
//     level is the entry to the thread and the Nth level is the printf call
//     an unspecified width or width of 0 will print stack levels [0,N). a
//     width of 1 will print stack levels [0,N-1) and so on. if the width is
//     greater than N, stack level 0 will be printed by itself.
//
//  %p prints the program name. vera seems to always print a blank string
//     for this. a width specification has no effect
//
//  %v prints the instance path of the shell. for vera_cs runs this prints
//     "vera_cs". for vcs runs, this prints something like "test_top.vshell"
//     a width specification has no effect
//
//  %_ prints the simulation path separator. i don't know that this is ever
//     anything besides ".". a width specification has no effect
//
//  %u this is identical to %d except all values are printed as unsigned and
//     the '+' flag is not respected
//
//  Any conversion specifier that is not defined silently returns the empty
//  string and does not consume an argument.
//
//Enumeration printing
//  enum values can be printed as either strings (%s) or numeric values. when
//  printed as strings, the enum value name is printed. this is equivalent
//  to java's Enum.toString(). when converted to a numeric value, the value
//  printed is "(ENUM_VAR: XXXX)" where XXXX is the enum's value (which is
//  always 32 bits). if the enum  has not been initialized, "(ENUM_VAR:X)" is
//  printed. the formatting of the enum's numeric value is handled
//  identically to printing any other type of number. The '-' and '+' flags
//  are respected when printing enums as numbers, however ' ' is not. Also,
//  any width specifier is disregarded and the numeric value is fully padded.
//  The '-' flag as well as width specifiers are respected when printing an
//  enum as a string. Enumerations printed as characters (%c) print the empty
//  string
//
//String conversions
//Character width disrespect
//The %c specifier does not respect width. When %c is specified, exactly one
//character is printed, regardless of any width specifier.
//
//Numeric conversions
//  Octal Padding
//  When octal numbers are printed, they are zero-padded to be a multiple of
//  3 bits. This means that 4'bx_x10 gets promoted to 6'b00x_x10 which prints
//  as "??" rather than the expected "X?"
//
//  Decimal conversion with X/Z values
//  Values containing X/Z, when printed as decimal print "?", no matter their
//  length.
//
//  Uninitialized integers are printed as "X", no matter the numeric conversion
//  specifier. Width is ignored except in the case of binary conversions, in
//  which case a non-zero width results in "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
//  (32 "x" values).
//
//  String Reversal
//  When strings are printed as numeric values, each character in the string
//  is converted to its ASCII value. For binary conversions, each byte of the
//  value is then bit-reversed before printing. For instance, the string "AF" is
//  16'h4146 or 16'b0100_0001_0100_0110. However if "AF" was printed as a binary
//  value (%b), "1000_0010_0110_0010" would be printed (underscores included
//  only for readability). Note that this is equivalent to
//  (><(16'h41)) << 8 | (><(16'h46)).
//
//Width specifiers
//  If the width specifier is 0 or less than the natural width, the minimal
//    number of characters required to print the value are used. Leading
//    X/Z characters are collapsed to exactly one X/Z. Leading zero characters
//    are removed entirely unless this would leave an empty string. In this
//    case, exactly one 0 is left.
//  Else if the type being printed is an enum and the conversion specifier is
//    numeric, the width specifier is not respected and the value is padded.
//  Else if the type being printed is not an integer, and the conversion
//    specifier is decimal (%d, %i, %u), no padding is performed.
//  Else padding is performed. For decimal conversions, the padding
//    consists of spaces. For non-decimal conversions, values are zero-padded
//    if the MSB character is 0-9, X, or Z. For non-decimal conversions where
//    the MSB character is '?', spaces are used to pad.
//
//Flag specifiers
//  ' ' flag
//  According to the VUM, the ' ' flag should prepend a space character
//  to a positive value printed via %d or %i. However, there seem to be a few
//  bugs in the vera implementation as of vera 6.3.30. Notably:
//      - A space is inserted only if the type being printed is an integer or
//        string
//      - When a string is printed and the space flag is present, a space is
//        inserted before each byte's value. For instance:
//        printf("% d", "AF") will print " 65 70"
//
//  '+' flag
//  For strings, the '+' flag has the same oddity as the ' ' flag:
//  printf("%+d", "AF") will print "+65+70". Note that the +/' ' flags are only
//  respected for signed, decimal conversions.
//
//Null arguments
//  If an argument passed to printf is null or void, the string "(NULL)" is
//  printed and the argument is consumed.

/**
 * Vera-compatible printf formatter.
 * 
 * @author Jon Nall
 */
public final class VeraPrintfFormatter
    extends PrintfFormatter
{
    // Static vera formatters
    private static ConversionFormatter vnFormatter = new VeraNumericFormatter();
    private static ConversionFormatter vsFormatter = new VeraStringFormatter();
    private static ConversionFormatter vNoArgFormatter = new VeraNoArgFormatter();
    private static ConversionFormatter vInvalidFormatter = new VeraInvalidConversionFormatter();

    private static ConversionFormatter[] formatters = new ConversionFormatter[128];

    private boolean leaveEscapes;

    public VeraPrintfFormatter(boolean leaveEscapes)
    {
        this.leaveEscapes = leaveEscapes;

        for (char c = 0; c < 128; ++c)
        {
            formatters[c] = getFormatter(c);
        }
    }

    private final boolean isNumericConversion(char conversionSpec)
    {
        switch (Character.toLowerCase(conversionSpec))
        {
        case 'b':
        case 'd':
        case 'h':
        case 'i':
        case 'o':
        case 'u':
        case 'x':
            return true;
        default:
            return false;
        }
    }

    private boolean isStringConversion(char conversionSpec)
    {
        switch (Character.toLowerCase(conversionSpec))
        {
        case 'c':
        case 's':
            return true;
        default:
            return false;
        }
    }

    private final boolean isValidConversion(char conversionSpec)
    {
        // vera adds some conversion specs
        switch (Character.toLowerCase(conversionSpec))
        {
        case '%': // escaped '%'
        case 'm': // stack trace
        case 'p': // program name
        case 'v': // shell instance name
        case '_': // simulator path separator character
            return true;
        default:
            return isNumericConversion(conversionSpec)
                || isStringConversion(conversionSpec);
        }
    }

    @Override
    protected final PrintfFlag getFlag(char conversionSpec)
    {
        // Vera supports only a subset of the normal printf flags
        switch (conversionSpec)
        {
        case '#':
            return PrintfFlag.ALTERNATE_FORM;
        case '-':
            return PrintfFlag.LEFT_JUSTIFY;
        case ' ':
            return PrintfFlag.SPACE_BEFORE_POSITIVE_VALUE;
        case '+':
            return PrintfFlag.PRINT_SIGN;
        default:
            return null;
        }
    }

    @Override
    protected final ConversionFormatter getFormatter(char conversionSpec)
    {
        if (isNumericConversion(conversionSpec))
        {
            return vnFormatter;
        }
        else if (isStringConversion(conversionSpec))
        {
            return vsFormatter;
        }
        else if (isValidConversion(conversionSpec))
        {
            return vNoArgFormatter;
        }
        else
        {
            return vInvalidFormatter;
        }
    }

    @Override
    protected final CharSequence postProcess(CharSequence buf)
    {
        if (leaveEscapes)
        {
            return buf;
        }

        return StringUnescaper.unescape(buf);
    }
}
