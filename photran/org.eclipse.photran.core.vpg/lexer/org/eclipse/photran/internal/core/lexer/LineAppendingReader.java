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
import java.io.Reader;

/**
 * Wraps another <code>Reader</code>, and, if the wrapped stream does not end
 * with an end-of-line character, appends the OS-dependent end-of-line sequence to
 * the end of the stream.
 * 
 * @author Jeff Overbey
 */
public final class LineAppendingReader extends SingleCharReader
{
    private Reader originalReader;
    private Reader eolReader;
    private Reader currentReader;
    
    private int lastChar = -1;
    
    public LineAppendingReader(Reader streamToWrap)
    {
        this.originalReader = streamToWrap;
        this.eolReader = new EOLReader();
        
        this.currentReader = originalReader;
    }

    @Override
    public int read() throws IOException
    {
        int result = currentReader.read();
        
        if (result == -1 && currentReader == originalReader && !isEOL(lastChar))
        {
            currentReader = eolReader;
            result = currentReader.read();
        }
        else
        {
            lastChar = result;
        }
        
        return result;
    }

    private boolean isEOL(int c)
    {
        return c =='\n' || c == '\r';
    }

    @Override
    public void close() throws IOException
    {
        originalReader.close();
    }

    private static class EOLReader extends SingleCharReader
    {
        private static final byte[] EOL = System.getProperty("line.separator").getBytes();
        private int eolByte = 0;

        @Override
        public int read() throws IOException
        {
            return eolByte < EOL.length ? EOL[eolByte++] : -1;
        }

        @Override
        public void close() throws IOException
        {
        }
    }
}
