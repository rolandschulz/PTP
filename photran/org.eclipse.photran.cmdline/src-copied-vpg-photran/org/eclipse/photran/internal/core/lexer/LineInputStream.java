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

import java.io.IOException;
import java.io.InputStream;

/**
 * Java's <code>BufferedReader</code> can read a line of text from another
 * reader, but it omits the end-of-line character(s).  This one doesn't.
 * Subclass if necessary.
 * 
 * @author Jeff Overbey
 */
public final class LineInputStream extends InputStream implements CharSequence
{
    protected InputStream in;
    
    protected String filename;
    protected int startOffset;
    protected int startLineNum;
    protected int restartLine = -1;
    protected int restartOffset = -1;
    
    protected char[] currentLine = new char[4096];
    protected int currentLineLength = -1;
    protected int offsetInCurrentLine = -1;
    protected int offsetInStream = 0;
    protected int lineNumInCurrentStream = 0;
    protected boolean atEOF = false;

    public LineInputStream(InputStream readFrom, String filename) throws IOException
    {
        this(readFrom, filename, 0, 0);
    }

    public LineInputStream(InputStream readFrom, String filename, int startOffset, int startLineNum) throws IOException
    {
        this.in = readFrom;
        this.filename = filename;
        this.startOffset = startOffset;
        this.startLineNum = startLineNum;
        advanceToNextLine();
    }

    /**
     * THIS IS EXPENSIVE -- The text of the line is COPIED into a <code>String</code>.
     * @return
     * @throws IOException
     */
    public String currentLine() throws IOException
    {
        ensureLineLoaded();
        return atEOF() ? null : String.valueOf(currentLine, offsetInCurrentLine, currentLineLength-offsetInCurrentLine);
    }
    
    public boolean atBOL() throws IOException
    {
        ensureLineLoaded();
        return offsetInCurrentLine == 0;
    }
    
    public boolean atEOF() throws IOException
    {
        return offsetInCurrentLine >= currentLineLength && atEOF;
    }

    protected void ensureLineLoaded() throws IOException
    {
        if (!atEOF && offsetInCurrentLine >= currentLineLength)
            advanceToNextLine();
    }

    protected int peek() throws IOException
    {
        return atEOF() ? -1 : currentLine[offsetInCurrentLine];
    }

    public void advanceToNextLine() throws IOException
    {
        int c;
        
        offsetInCurrentLine = 0;
        offsetInStream += Math.max(currentLineLength-offsetInCurrentLine, 0);
        
        if (atEOF) return;
        
        lineNumInCurrentStream++;
        
        if (currentLineLength < 0)
        {
            // First attempt to read a line
            currentLineLength = 0;
        }
        else
        {
            // Start with the extra character from the last read
            currentLine[0] = currentLine[currentLineLength];
            currentLineLength = 1;
        }
        
        for (c = in.read(); c != -1 && c != '\n' && c != '\r'; c = in.read())
            append(c);
        
        if (c == -1) append('\n'); // Always end files with a newline
        
        for ( ; c != -1 && (c == '\n' || c == '\r'); c = in.read())
            append(c);
        
        if (c == -1) atEOF = true;

        // To find the end of the line, we had to read an extra character; don't lose it!
        currentLine[currentLineLength] = (char)c;
    }
    
    private void append(int c)
    {
        if (currentLineLength + 1 == currentLine.length)
        {
            char[] newCurrentLine = new char[currentLine.length*2];
            System.arraycopy(currentLine, 0, newCurrentLine, 0, currentLine.length);
            currentLine = newCurrentLine;
        }
        
        currentLine[currentLineLength++] = (char)c;
    }
    
    public String getFilename()
    {
        return filename;
    }
    
    public int getFileOffsetOfNextRead()
    {
        return offsetInStream;
    }
    
    public int getFileLineOfNextRead()
    {
        return lineNumInCurrentStream;
    }
    
    public int getTotalOffsetOfNextRead()
    {
        return startOffset + offsetInStream;
    }
    
    public int getTotalLineOfNextRead()
    {
        return startLineNum + lineNumInCurrentStream;
    }

    public int getStartLine()
    {
        return startLineNum;
    }

    public int getStartOffset()
    {
        return startOffset;
    }

    public int getRestartLine()
    {
        return restartLine;
    }

    public void setRestartLine(int restartLine)
    {
        this.restartLine = restartLine;
    }

    public int getRestartOffset()
    {
        return restartOffset;
    }

    public void setRestartOffset(int restartOffset)
    {
        this.restartOffset = restartOffset;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // INPUTSTREAM IMPLEMENTATION
    ///////////////////////////////////////////////////////////////////////////

    public int read() throws IOException
    {
        ensureLineLoaded();
        int result = peek();
        if (result >= 0)
        {
            offsetInCurrentLine++;
            offsetInStream++;
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // CHARSEQUENCE IMPLEMENTATION
    ///////////////////////////////////////////////////////////////////////////
    
    public char charAt(int index)
    {
        return currentLine[offsetInCurrentLine+index];
    }

    public int length()
    {
        return currentLineLength-offsetInCurrentLine;
    }

    public CharSequence subSequence(final int start, final int end)
    {
        if (start < offsetInCurrentLine || end >= length())
            throw new IllegalArgumentException();
        
        return new SubSequence(start, end);
    }
    
    private final class SubSequence implements CharSequence
    {
        private final int start;
        private final int end;

        private SubSequence(int start, int end)
        {
            this.start = start;
            this.end = end;
        }

        public char charAt(int index)
        {
            return currentLine[offsetInCurrentLine+start];
        }

        public int length()
        {
            return end-start;
        }

        public CharSequence subSequence(int subseqStart, int subseqEnd)
        {
            return new SubSequence(start+subseqStart, start+subseqEnd);
        }
        
        public String toString()
        {
            return String.valueOf(currentLine, start, end-start);
        }
    }
}
