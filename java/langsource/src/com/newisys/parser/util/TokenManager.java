/*
 * Based on source code generated by JavaCC (TM).
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * JavaCC is a trademark or registered trademark of Sun Microsystems, Inc. in
 * the U.S. or other countries.
 * See the supplied LICENSE-javacc.txt file for license information.
 */

package com.newisys.parser.util;

/**
 * An implementation for this interface is generated by
 * JavaCCParser.  The user is free to use any implementation
 * of their choice.
 */

public interface TokenManager
{

    /** This gets the next token from the input stream.
     *  A token of kind 0 (<EOF>) should be returned on EOF.
     */
    public Token getNextToken();

}