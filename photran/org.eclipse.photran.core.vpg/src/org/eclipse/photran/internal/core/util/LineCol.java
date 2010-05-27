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
package org.eclipse.photran.internal.core.util;

/**
 * Stores a <b>line</b> and a <b>column</b> number (two integers, typically positive).
 * 
 * <code>LineCol</code>s are frequently used to store the position of a cursor
 * in a text editor or the position of a piece of text in a file.
 * 
 * @author Jeff Overbey
 */
public final class LineCol
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String toString(int line, int col)
    {
        return "line " + line + ", column " + col;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int line = 0, col = 0;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public LineCol(int line, int col)
    {
        this.line = line;
        this.col = col;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /** @return this line */
    public int getLine()
    {
        return line;
    }

    /** Sets this line */
    public void setLine(int line)
    {
        this.line = Math.max(line, 0);
    }

    /** @return this column */
    public int getCol()
    {
        return col;
    }

    /** Sets this column */
    public void setCol(int col)
    {
        this.col = Math.max(col, 0);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public String toString()
    {
        return toString(line, col);
    }
}
