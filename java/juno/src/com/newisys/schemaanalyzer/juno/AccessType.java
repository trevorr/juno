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

package com.newisys.schemaanalyzer.juno;

/**
 * Enumeration of data access types.
 * 
 * @author Trevor Robinson
 */
final class AccessType
{
    public static final AccessType VOID = new AccessType(false, false);
    public static final AccessType READ = new AccessType(true, false);
    public static final AccessType WRITE = new AccessType(false, true);
    public static final AccessType READ_WRITE = new AccessType(true, true);

    private final boolean read;
    private final boolean write;

    private AccessType(final boolean read, final boolean write)
    {
        this.read = read;
        this.write = write;
    }

    public static AccessType getInstance(boolean read, boolean write)
    {
        if (read)
        {
            return write ? READ_WRITE : READ;
        }
        else
        {
            return write ? WRITE : VOID;
        }
    }

    public boolean isRead()
    {
        return read;
    }

    public boolean isWrite()
    {
        return write;
    }

    @Override
    public String toString()
    {
        if (read)
        {
            return write ? "read/write" : "read";
        }
        else
        {
            return write ? "write" : "void";
        }
    }
}
