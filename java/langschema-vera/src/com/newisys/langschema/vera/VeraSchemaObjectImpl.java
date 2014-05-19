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

package com.newisys.langschema.vera;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.Schema;

/**
 * Base implementation for all Vera schema objects.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraSchemaObjectImpl
    implements VeraSchemaObject
{
    protected final VeraSchema schema;
    private List<Annotation> annotations;

    public VeraSchemaObjectImpl(VeraSchema schema)
    {
        this.schema = schema;
    }

    public final Schema getSchema()
    {
        return schema;
    }

    public final VeraSchema getVeraSchema()
    {
        return schema;
    }

    public List<Annotation> getAnnotations()
    {
        if (annotations == null) return Collections.emptyList();
        return annotations;
    }

    public void addAnnotation(Annotation annotation)
    {
        if (annotations == null) annotations = new LinkedList<Annotation>();
        annotations.add(annotation);
    }

    public void addAnnotations(Collection<Annotation> c)
    {
        if (!c.isEmpty())
        {
            if (annotations == null)
                annotations = new LinkedList<Annotation>();
            annotations.addAll(c);
        }
    }

    public final String toSourceString()
    {
        return schema.getDefaultPrinter().toString(this);
    }

    public final String toString()
    {
        return schema.isUseSourceString() ? toSourceString() : toDebugString();
    }
}
