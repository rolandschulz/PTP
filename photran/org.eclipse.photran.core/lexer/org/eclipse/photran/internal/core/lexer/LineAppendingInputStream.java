package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.InputStream;

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

    public int read() throws IOException
    {
        int result = currentInputStream.read();
        
        if (result == -1 && currentInputStream == originalInputStream && !isEOL(lastChar))
        {
            currentInputStream = eolInputStream;
            result = currentInputStream.read();
            currentInputStream.available();
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

        public int read() throws IOException
        {
            return eolByte < EOL.length ? EOL[eolByte++] : -1;
        }
        
    }
}
