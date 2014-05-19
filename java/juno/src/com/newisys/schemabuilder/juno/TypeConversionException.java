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

package com.newisys.schemabuilder.juno;

/**
 * Internal exception thrown when there is no conversion to the desired type.
 * 
 * @author Trevor Robinson
 */
class TypeConversionException
    extends RuntimeException
{
    private static final long serialVersionUID = 3257005436652498994L;

    public TypeConversionException()
    {
        super();
    }

    public TypeConversionException(String message)
    {
        super(message);
    }

    public TypeConversionException(Throwable cause)
    {
        super(cause);
    }

    public TypeConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
