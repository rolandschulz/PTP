/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;

/**
 * A minimal implementation of <code>IToken</code>.
 * 
 * @author Jeff Overbey
 */
public class SimpleToken implements IToken
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The Terminal that this token is an instance of
     */
    protected Terminal terminal = null;

    /**
     * The token text
     */
    protected String text = "";

    /**
     * The guarding directive
     */
    protected String preprocessorDirective = null;
    
    ///////////////////////////////////////////////////////////////////////////
    // Additional Fields - Not updated when refactoring
    ///////////////////////////////////////////////////////////////////////////
    
    protected IFile file = null;
    
    protected int line = -1, col = -1, fileOffset = -1, streamOffset = -1, length = -1;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
    
    public SimpleToken(Terminal terminal, String tokenText)
    {
        this.terminal = terminal;
        this.text     = tokenText   == null ? "" : tokenText;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the Terminal that this token is an instance of
     */
    public Terminal getTerminal() { return terminal; }

    /**
     * Sets the Terminal that this token is an instance of
     */
    public void setTerminal(Terminal value) { terminal = value; }

    /**
     * Returns the token text
     */
    public String getText() { return text; }

    /**
     * Sets the token text
     */
    public void setText(String value) { text = value == null ? "" : value; }

    /**
     * Returns whitespace and whitetext appearing before this token that should be associated with this token
     */
    public String getWhiteBefore() { return ""; }

    /**
     * Sets whitespace and whitetext appearing before this token that should be associated with this token
     */
    public void setWhiteBefore(String value) {;}

    /**
     * Returns whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public String getWhiteAfter() { return ""; }

    /**
     * Sets whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public void setWhiteAfter(String value) {;}
    
    public String getPreprocessorDirective() { return preprocessorDirective; }
    
    public void setPreprocessorDirective(String preprocessorDirective) { this.preprocessorDirective = preprocessorDirective; }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = col;
    }

    public IFile getFile()
    {
        return file;
    }

    public void setFile(IFile file)
    {
        this.file = file;
    }

    public int getFileOffset()
    {
        return fileOffset;
    }

    public void setFileOffset(int fileOffset)
    {
        this.fileOffset = fileOffset;
    }

    public int getStreamOffset()
    {
        return streamOffset;
    }

    public void setStreamOffset(int streamOffset)
    {
        this.streamOffset = streamOffset;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.toString() + ": \"" + text + "\""; }
    
    public String toString() { return text; }
}
