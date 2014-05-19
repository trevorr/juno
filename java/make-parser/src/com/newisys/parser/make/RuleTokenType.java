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
 * Enumeration of makefile rule token types.
 * 
 * @author Trevor Robinson
 */
final class RuleTokenType
{
    private RuleTokenType()
    {
    }

    public static final RuleTokenType EOL = new RuleTokenType();
    public static final RuleTokenType COLON = new RuleTokenType();
    public static final RuleTokenType DOUBLE_COLON = new RuleTokenType();
    public static final RuleTokenType SEMICOLON = new RuleTokenType();
    public static final RuleTokenType ASSIGN_OP = new RuleTokenType();
    public static final RuleTokenType TEXT = new RuleTokenType();
    public static final RuleTokenType VAR_REF = new RuleTokenType();
}
