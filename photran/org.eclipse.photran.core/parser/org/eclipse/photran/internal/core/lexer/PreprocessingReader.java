package org.eclipse.photran.internal.core.lexer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

public class PreprocessingReader extends Reader
{
    protected final Stack/*<Reader>*/ readerStack = new Stack/*<Reader>*/();
    protected final Stack/*<String>*/ filenameStack = new Stack/*<String>*/();
    
    protected final Reader currentReader()
    {
        return (Reader)readerStack.lastElement();
    }
    
    protected final String currentFilename()
    {
        return (String)filenameStack.lastElement();
    }
    
    public PreprocessingReader(InputStream in, String filename)
    {
        readerStack.push(new InputStreamReader(in));
        filenameStack.push(filename);
    }
    
    public void pushStream(InputStream in, String filename)
    {
        readerStack.push(new InputStreamReader(in));
        filenameStack.push(filename);
    }
    
    public void pushReader(Reader r, String filename)
    {
        readerStack.push(r);
        filenameStack.push(filename);
    }
    
    public void pushReader(String filename) throws FileNotFoundException
    {
        readerStack.push(new FileReader(filename));
        filenameStack.push(filename);
    }
    
    // === Reader implementation ===

    public int read(char[] cbuf, int off, int len) throws IOException
    {
        int result = currentReader().read(cbuf, off, len);
        while (result == -1 && readerStack.size() > 1)
        {
            readerStack.pop();
            result = currentReader().read(cbuf, off, len);
        }
        return result;
    }

    public void close() throws IOException
    {
        while (!readerStack.isEmpty())
            ((Reader)readerStack.pop()).close();
    }
}
