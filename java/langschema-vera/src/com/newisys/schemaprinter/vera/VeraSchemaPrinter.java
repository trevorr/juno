/*
 * LangSchema-Vera - Programming Language Modeling Classes for OpenVera (TM)
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

package com.newisys.schemaprinter.vera;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.newisys.langschema.vera.VeraSchema;
import com.newisys.langschema.vera.VeraSchemaObject;
import com.newisys.schemaprinter.SchemaPrinter;
import com.newisys.schemaprinter.WrappedIOException;
import com.newisys.util.text.TokenFormatter;

/**
 * Generates Vera source text corresponding to a schema. Currently, this class
 * simply dumps the entire schema to an output stream; it does not attempt to
 * generate individual source files, #include directives, etc.
 * 
 * @author Trevor Robinson
 */
public class VeraSchemaPrinter
    extends SchemaPrinter
{
    public void print(VeraSchema schema, Writer writer)
        throws IOException
    {
        try
        {
            TokenFormatter fmt = getTokenFormatter(writer);
            SchemaMemberPrinter smp = new SchemaMemberPrinter(fmt, this);
            smp.printMembers(schema.getMembers());
            fmt.flush();
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public String toString(VeraSchema schema)
    {
        StringWriter writer = new StringWriter(200);
        try
        {
            print(schema, writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    public void print(VeraSchemaObject obj, Writer writer)
        throws IOException
    {
        try
        {
            TokenFormatter fmt = getTokenFormatter(writer);
            SchemaObjectPrinter smp = new SchemaObjectPrinter(fmt, this);
            obj.accept(smp);
            fmt.flush();
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public String toString(VeraSchemaObject obj)
    {
        StringWriter writer = new StringWriter(200);
        try
        {
            print(obj, writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }
}
