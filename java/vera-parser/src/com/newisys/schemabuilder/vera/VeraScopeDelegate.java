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

import com.newisys.langschema.Scope;

/**
 * Base class for scope implementations that delegate to other scopes,
 * including an optional enclosing scope.
 * 
 * @author Trevor Robinson
 */
public abstract class VeraScopeDelegate
    implements Scope
{
    protected final VeraScopeDelegate enclosingScope;

    protected VeraScopeDelegate(VeraScopeDelegate enclosingScope)
    {
        this.enclosingScope = enclosingScope;
    }
}
