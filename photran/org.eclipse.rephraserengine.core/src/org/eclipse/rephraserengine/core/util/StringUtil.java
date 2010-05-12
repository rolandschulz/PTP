/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility methods for working with strings.
 * 
 * @author Jeff Overbey
 */
public class StringUtil
{
    private StringUtil() {;}
    
    public static long countLines(String s)
    {
        if (s.length() == 0) return 0L;

        long numLines = 1L;
        int lastIndex = 0;
        int nextIndex = s.indexOf('\n');
        while (nextIndex >= 0)
        {
            numLines++;
            lastIndex = nextIndex;
            if (lastIndex + 1 >= s.length())
                nextIndex = -1;
            else
                nextIndex = s.indexOf('\n', lastIndex + 1);
        }
        return numLines;
    }

    public static String read(Reader in) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (int ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
        }
        in.close();
        return sb.toString();
    }
    
    public static String read(InputStream in) throws IOException
    {
        return read(new InputStreamReader(in));
    }
    
    public static String read(IFile file) throws IOException, CoreException
    {
        return read(new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())));
    }

    public static String read(File file) throws IOException
    {
        return read(new BufferedReader(new FileReader(file)));
    }

    public static String readOrReturnNull(IFile file)
    {
        try
        {
            return read(file);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Returns the (0-based) offset of the character at the given (1-based) line and column.
     * @param line the line number (the first line is line 1) 
     * @param col the column number (the first column is column 1)
     * @return the character offset (the first character is at offset 0)
     */
    public static int offsetOf(int line, int col, String string)
    {
        assert line >= 1 && col >= 1 && string != null;
        
        int offset = offsetOfLine(line, string);
        if (offset < 0)
            return -1;
        else
            return offset + col - 1;
    }

    /**
     * Returns the (0-based) offset of the first character on the given (1-based) line.
     * @param line the line number (the first line is line 1) 
     * @return the character offset (the first character is at offset 0)
     */
    public static int offsetOfLine(int line, String string)
    {
        assert line >= 1 && string != null;

        if (line == 1) return 0;

        int precedingLF = -1;
        int curLine = 1;
        while (precedingLF >= 0 || curLine == 1)
        {
            precedingLF = string.indexOf('\n', precedingLF+1);
            curLine++;
            if (curLine == line) return precedingLF < 0 ? -1 : precedingLF+1;
        }
        
        return -1;
    }

    public static String stripNonASCIICharsAndCRs(String s)
    {
        return stripNonASCIIChars(s, true);
    }

    public static String stripNonASCIIChars(String s)
    {
        return stripNonASCIIChars(s, false);
    }

    protected static String stripNonASCIIChars(String s, boolean stripCRs)
    {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0, len = s.length(); i < len; i++)
        {
            char ch = s.charAt(i);
            if (stripCRs && i == '\r')
                continue;
            else if (i < 256)
                sb.append(ch);
            else
                sb.append('?');
        }
        return sb.toString();
    }
}
