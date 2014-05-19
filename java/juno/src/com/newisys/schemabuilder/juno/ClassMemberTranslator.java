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

import java.util.List;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.constraint.ConsExpression;
import com.newisys.langschema.java.JavaAnnotation;
import com.newisys.langschema.java.JavaAnnotationArrayInitializer;
import com.newisys.langschema.java.JavaAnnotationType;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaFunction;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaSchemaObject;
import com.newisys.langschema.java.JavaStringLiteral;
import com.newisys.langschema.vera.VeraClassConstraint;
import com.newisys.langschema.vera.VeraClassMemberVisitor;
import com.newisys.langschema.vera.VeraEnumeration;
import com.newisys.langschema.vera.VeraEnumerationElement;
import com.newisys.langschema.vera.VeraMemberFunction;
import com.newisys.langschema.vera.VeraMemberVariable;

/**
 * Schema translator for class members.
 * 
 * @author Trevor Robinson
 */
final class ClassMemberTranslator
    extends TranslatorModule
    implements VeraClassMemberVisitor
{
    private final JavaPackage pkg;
    private final JavaRawClass cls;

    public ClassMemberTranslator(
        TranslatorModule xlatContext,
        JavaPackage pkg,
        JavaRawClass cls)
    {
        super(xlatContext);
        this.pkg = pkg;
        this.cls = cls;
    }

    public void visit(VeraClassConstraint obj)
    {
        final String id = obj.getName().getIdentifier();
        logEnter("Translating constraint: " + id);

        // translate constraint expression
        final ConsExpressionTranslator xlat = new ConsExpressionTranslator(this);
        obj.accept(xlat);
        final ConsExpression expr = xlat.getResult();

        // get or create Randomizable annotation
        JavaAnnotation randomizable = findAnnotation(cls,
            types.randomizableType);
        if (randomizable == null)
        {
            randomizable = new JavaAnnotation(types.randomizableType);
            cls.addAnnotation(randomizable);
        }

        // get or create constraint list element (Randomizable.value)
        final JavaFunction valueElem = types.randomizableType
            .getMethod("value");
        JavaAnnotationArrayInitializer consInit = (JavaAnnotationArrayInitializer) randomizable
            .getAssignedElementValue(valueElem);
        if (consInit == null)
        {
            final JavaArrayType consArrayType = schema.getArrayType(
                types.constraintType, 1);
            consInit = new JavaAnnotationArrayInitializer(consArrayType);
            randomizable.setElementValue(valueElem, consInit);
        }

        // add constraint to constraint list
        final JavaAnnotation cons = new JavaAnnotation(types.constraintType);
        cons.setElementValue("name", new JavaStringLiteral(schema, id));
        final String exprStr = ConsExpressionPrinter.print(expr, cls);
        cons.setElementValue("expr", new JavaStringLiteral(schema, exprStr));
        consInit.addElement(cons);

        // copy comments from constraint
        cons.addAnnotations(obj.getAnnotations());

        logExit();
    }

    private JavaAnnotation findAnnotation(
        JavaSchemaObject obj,
        JavaAnnotationType annType)
    {
        final List< ? extends Annotation> anns = obj.getAnnotations();
        for (final Annotation ann : anns)
        {
            if (ann instanceof JavaAnnotation)
            {
                JavaAnnotation jann = (JavaAnnotation) ann;
                if (jann.getType() == annType) return jann;
            }
        }
        return null;
    }

    public void visit(VeraEnumeration obj)
    {
        translateEnum(obj, pkg, cls);
    }

    public void visit(VeraEnumerationElement obj)
    {
        // ignored; processed as part of enumeration
    }

    public void visit(VeraMemberFunction obj)
    {
        translateMemberFunctionOrCtor(obj, cls);
    }

    public void visit(VeraMemberVariable obj)
    {
        translateMemberVariable(obj, cls);
    }
}
