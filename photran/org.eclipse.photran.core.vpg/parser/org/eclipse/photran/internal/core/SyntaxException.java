/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.FileOrIFile;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * Exception thrown when the {@link Parser} discovers a syntax error.
 * <p>
 * This class includes methods for determining what {@link IFile}
 * (if applicable) was being parsed and where in the file the
 * syntax error occurred (see, e.g., {@link #getTokenLine()}).
 * 
 * @author Jeff Overbey
 */
public class SyntaxException extends Exception
{
    private static final long serialVersionUID = 1L;

    private Token lookahead;
    
    public SyntaxException(Token lookahead)
    {
        super("Syntax error: Unexpected "
            + lookahead.getTerminal().toString()
            + " ("
            + lookahead.getPhysicalFile() + ", "
            + "line " + lookahead.getLine()
            + ", column " + lookahead.getCol()
            + ")");
        
        this.lookahead = lookahead;
    }
    
    /** May return <code>null</code> */
    public FileOrIFile getFile()
    {
        return lookahead.getPhysicalFile();
    }
    
    public int getTokenLine()
    {
        return lookahead.getLine();
    }
    
    public int getTokenColumn()
    {
        return lookahead.getCol();
    }
    
    public int getTokenOffset()
    {
        return Math.max(0, lookahead.getFileOffset());
    }
    
    public int getTokenLength()
    {
        return Math.max(0, lookahead.getLength());
    }
}
