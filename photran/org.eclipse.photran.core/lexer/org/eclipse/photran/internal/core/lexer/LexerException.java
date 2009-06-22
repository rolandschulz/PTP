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
package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;

/**
 * Exception thrown when an {@link ILexer} discovers an error in the lexical syntax.
 * <p>
 * This class includes methods for determining what {@link IFile}
 * (if applicable) was being parsed and where in the file the
 * syntax error occurred (see, e.g., {@link #getTokenLine()}).
 * 
 * @author Jeff Overbey
 */
public class LexerException extends Exception
{
    private static final long serialVersionUID = 1L;

    private ILexer lexer;
    
    public LexerException(ILexer lexer, String message)
    {
        super(message);
        this.lexer = lexer;
    }
    
    /** May return <code>null</code> */
    public IFile getFile()
    {
        return lexer.getLastTokenFile();
    }
    
    public int getTokenLine()
    {
        return lexer.getLastTokenLine();
    }
    
    public int getTokenColumn()
    {
        return lexer.getLastTokenCol();
    }
    
    public int getTokenOffset()
    {
        return Math.max(0, lexer.getLastTokenFileOffset());
    }
    
    public int getTokenLength()
    {
        return Math.max(0, lexer.getLastTokenLength());
    }
}
