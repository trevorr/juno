/*
 * LangSource - Generic Programming Language Source Modeling Tools
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

package com.newisys.langsource;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.io.IndentPrintWriter;
import com.newisys.langschema.Annotation;
import com.newisys.langschema.Comment;
import com.newisys.parser.util.IncludeLocation;

/**
 * Base implementation for source objects.
 * 
 * @author Trevor Robinson
 */
public class SourceObjectImpl
    implements SourceObject
{
    private String beginFilename;
    private int beginLine;
    private int beginColumn;
    private String endFilename;
    private int endLine;
    private int endColumn;
    private IncludeLocation includeLocation;
    private List<Annotation> annotations;

    public void setBeginLocation(String filename, int line, int column)
    {
        this.beginFilename = filename;
        this.beginLine = line;
        this.beginColumn = column;
    }

    public String getBeginFilename()
    {
        return beginFilename;
    }

    public int getBeginLine()
    {
        return beginLine;
    }

    public int getBeginColumn()
    {
        return beginColumn;
    }

    public String getEndFilename()
    {
        return endFilename;
    }

    public void setEndLocation(String filename, int line, int column)
    {
        this.endFilename = filename;
        this.endLine = line;
        this.endColumn = column;
    }

    public int getEndLine()
    {
        return endLine;
    }

    public int getEndColumn()
    {
        return endColumn;
    }

    public void setIncludeLocation(IncludeLocation includeLocation)
    {
        this.includeLocation = includeLocation;
    }

    public IncludeLocation getIncludeLocation()
    {
        return includeLocation;
    }

    public void addAnnotation(Annotation annotation)
    {
        checkAnnotations();
        annotations.add(annotation);
    }

    public boolean hasAnnotations()
    {
        return annotations != null;
    }

    public List<Annotation> getAnnotations()
    {
        checkAnnotations();
        return annotations;
    }

    private void checkAnnotations()
    {
        if (annotations == null)
        {
            annotations = new LinkedList<Annotation>();
        }
    }

    public void copyMetadata(SourceObjectImpl other)
    {
        beginFilename = other.beginFilename;
        beginLine = other.beginLine;
        beginColumn = other.beginColumn;
        endFilename = other.endFilename;
        endLine = other.endLine;
        endColumn = other.endColumn;
        includeLocation = other.includeLocation;
        if (other.annotations != null)
        {
            checkAnnotations();
            annotations.addAll(other.annotations);
        }
    }

    protected void dumpComments(PrintWriter out)
    {
        if (annotations != null)
        {
            Iterator iter = annotations.iterator();
            while (iter.hasNext())
            {
                Annotation annotation = (Annotation) iter.next();
                if (annotation instanceof Comment)
                {
                    out.print("/*");
                    out.print(((Comment) annotation).getText());
                    out.println("*/");
                }
            }
        }
    }

    protected static void dumpCollection(IndentPrintWriter out, Collection c)
    {
        boolean onNewLine = false;
        out.incIndent();
        try
        {
            Iterator iter = c.iterator();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof SourceObject)
                {
                    if (!onNewLine)
                    {
                        out.println();
                    }
                    ((SourceObject) obj).dump(out);
                    onNewLine = true;
                }
                else
                {
                    out.print(obj);
                    if (iter.hasNext())
                    {
                        out.print(", ");
                    }
                    onNewLine = false;
                }
            }
        }
        finally
        {
            if (!onNewLine)
            {
                out.println();
            }
            out.decIndent();
        }
    }

    private static void dumpFields(
        IndentPrintWriter out,
        Object obj,
        Class cls,
        Class stopClass)
    {
        if (cls != null && !cls.equals(stopClass))
        {
            dumpFields(out, obj, cls.getSuperclass(), stopClass);

            Field[] fields = cls.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i)
            {
                Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers())) continue;
                String name = field.getName();
                try
                {
                    Class type = field.getType();
                    String accessorPrefix = type.equals(boolean.class) ? "is"
                        : "get";
                    String accessorName = accessorPrefix
                        + Character.toUpperCase(name.charAt(0))
                        + name.substring(1);
                    Method accessor = cls.getMethod(accessorName);
                    Object value = accessor.invoke(obj);
                    if (value instanceof Collection)
                    {
                        Collection c = (Collection) value;
                        if (!c.isEmpty())
                        {
                            out.print(name);
                            out.print(": ");
                            dumpCollection(out, c);
                        }
                    }
                    else if (value instanceof SourceObject)
                    {
                        out.print(name);
                        out.println(":");
                        out.incIndent();
                        try
                        {
                            ((SourceObject) value).dump(out);
                        }
                        finally
                        {
                            out.decIndent();
                        }
                    }
                    else if (value != null)
                    {
                        out.print(name);
                        out.print(": ");
                        out.println(value);
                    }
                }
                catch (Exception e)
                {
                    // ignored
                }
            }
        }
    }

    private static String getUnqualifiedName(String name)
    {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0)
        {
            return name.substring(lastDot + 1);
        }
        else
        {
            return name;
        }
    }

    protected void dumpContents(IndentPrintWriter out)
    {
        out.println();
        out.incIndent();
        try
        {
            dumpComments(out);
            dumpFields(out, this, getClass(), SourceObjectImpl.class);
        }
        finally
        {
            out.decIndent();
        }
    }

    public void dump(IndentPrintWriter out)
    {
        Class cls = getClass();
        out.print(getUnqualifiedName(cls.getName()));
        out.print(": ");
        dumpContents(out);
    }
}
