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

import com.newisys.langsource.SourceObject;
import com.newisys.langsource.vera.MacroDecl;
import com.newisys.parser.util.Macro;

/**
 * Provides information from the preprocessor to the schema builder.
 * 
 * @author Trevor Robinson
 */
public interface VeraPreprocessorInfo
{
    /**
     * Returns whether the given source object is from a source file (.vr) that
     * was included in place of a header file (.vrh), or from a file included
     * from such a file.
     *
     * @param obj SourceObject
     * @return boolean
     */
    boolean isFromHeader(SourceObject obj);

    /**
     * Returns the compilation unit that the given source object should be
     * associated with. This will normally be the file itself, except in the
     * case of included .vr files, whose compilation unit will be the including
     * non-.vr file.
     *
     * @param obj SourceObject
     * @return String
     */
    String getCompilationUnit(SourceObject obj);

    /**
     * Attempts to parse the given macro into a source object. If the macro
     * expansion does not correspond to a supported source object, this method
     * returns null.
     *
     * @param macro the macro to parse
     * @return a corresponding source object, or null
     */
    MacroDecl parseMacro(Macro macro);
}
