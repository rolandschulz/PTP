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
 * Wraps another <code>InputStream</code>, and, if the wrapped stream does not end
 * with an end-of-line character, appends the OS-dependent end-of-line sequence to
 * the end of the stream.
 * 
 * @author Jeff Overbey
 */
public final class LineAppendingInputStream extends InputStream
{
    private InputStream originalInputStream;
    private InputStream eolInputStream;
    private InputStream currentInputStream;
    
    private int lastChar = -1;
    
    public LineAppendingInputStream(InputStream streamToWrap)
    {
        this.originalInputStream = streamToWrap;
        this.eolInputStream = new EOLInputStream();
        
        this.currentInputStream = originalInputStream;
    }

    @Override
    public int read() throws IOException
    {
        int result = currentInputStream.read();
        
        if (result == -1 && currentInputStream == originalInputStream && !isEOL(lastChar))
        {
            currentInputStream = eolInputStream;
            result = currentInputStream.read();
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

    private static class EOLInputStream extends InputStream
    {
        private static final byte[] EOL = System.getProperty("line.separator").getBytes();
        private int eolByte = 0;

        @Override
        public int read() throws IOException
        {
            return eolByte < EOL.length ? EOL[eolByte++] : -1;
        }
        
    }
}
