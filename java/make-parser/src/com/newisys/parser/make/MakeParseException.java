/*
 * Makefile Parser and Model Builder
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.parser.make;

/**
 * Exception thrown when a fatal error is encountered parsing a makefile.
 * 
 * @author Trevor Robinson
 */
public class MakeParseException
    extends Exception
{
    private String filename;
    private int lineNumber = -1;

    public MakeParseException()
    {
        super();
    }

    public MakeParseException(String message)
    {
        super(message);
    }

    public MakeParseException(Throwable cause)
    {
        super(cause);
    }

    public MakeParseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    public String getMessage()
    {
        return formatMessage(filename, lineNumber, super.getMessage());
    }

    public static String formatMessage(
        String filename,
        int lineNumber,
        String message)
    {
        StringBuffer result = new StringBuffer();
        if (filename != null)
        {
            result.append(filename);
            result.append(':');
        }
        if (lineNumber >= 0)
        {
            result.append(lineNumber);
            result.append(':');
        }
        result.append(message);
        return result.toString();
    }
}
