/*
 * Parser and Source Model for the OpenVera (TM) language
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

package com.newisys.schemabuilder.vera;

import com.newisys.langschema.util.SemanticException;
import com.newisys.langsource.SourceObject;

/**
 * Exception thrown for semantic errors with an associated source object.
 * 
 * @author Trevor Robinson
 */
public class SourceSemanticException
    extends SemanticException
{
    private static final long serialVersionUID = 3257291335462827573L;

    private SourceObject sourceObject;

    public SourceSemanticException()
    {
        super();
    }

    public SourceSemanticException(String message)
    {
        super(message);
    }

    public SourceSemanticException(Throwable cause)
    {
        super(cause);
    }

    public SourceSemanticException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SourceSemanticException(String message, SourceObject sourceObject)
    {
        super(message);
        this.sourceObject = sourceObject;
    }

    protected static final String EOL = System.getProperty("line.separator",
        "\n");

    public String getMessage()
    {
        String msg = super.getMessage();
        if (sourceObject != null)
        {
            StringBuffer buf = new StringBuffer(msg);
            buf.append(EOL);
            buf.append("\tat ");
            buf.append(sourceObject.getBeginFilename());
            buf.append(", line ");
            buf.append(sourceObject.getBeginLine());
            buf.append(", column ");
            buf.append(sourceObject.getBeginColumn());
            msg = buf.toString();
        }
        return msg;
    }

    public SourceObject getSourceObject()
    {
        return sourceObject;
    }

    public void setSourceObject(SourceObject sourceObject)
    {
        this.sourceObject = sourceObject;
    }
}
