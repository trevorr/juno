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

package com.newisys.parser.vera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langsource.vera.CompilationUnitDeclMember;
import com.newisys.parser.util.PreprocessedToken;

/**
 * Extends PreprocessedToken to include leading and trailing comment tokens
 * and adjacent preprocessor directives.
 * 
 * @author Trevor Robinson
 */
public class VeraToken
    extends PreprocessedToken
{
    public PreprocessedToken leadingComments;
    public PreprocessedToken trailingComments;

    public List<CompilationUnitDeclMember> preprocDecls;

    public void addPreprocDecl(CompilationUnitDeclMember decl)
    {
        if (preprocDecls == null)
            preprocDecls = new LinkedList<CompilationUnitDeclMember>();
        preprocDecls.add(decl);
    }

    public void addPreprocDecls(Collection<CompilationUnitDeclMember> decls)
    {
        if (preprocDecls == null)
            preprocDecls = new LinkedList<CompilationUnitDeclMember>();
        preprocDecls.addAll(decls);
    }
}
