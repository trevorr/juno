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

import com.newisys.langschema.java.JavaExpression;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaStructuredType;

/**
 * Interface used to generate calls to factory methods when translating a Vera
 * instance creation expression.
 * 
 * @author Trevor Robinson
 */
public interface FactoryCallBuilder
{
    /**
     * Returns an expression that constructs a new instance of a class derived
     * from the given class using the given argument expressions.
     *
     * @param cls translated Java class for the original instance creation
     * @param args List of JavaExpression constructor arguments
     * @param containingType Java class/interface containing the instance
     *            creation expression
     * @return an expression returning a new instance of a class derived from
     *         the given Java class
     */
    JavaExpression callFactory(
        JavaRawClass cls,
        List<JavaExpression> args,
        JavaStructuredType containingType);
}
