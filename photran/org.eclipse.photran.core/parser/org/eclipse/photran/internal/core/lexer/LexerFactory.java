package org.eclipse.photran.internal.core.lexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A collection of (static) factory methods for creating Fortran lexers.
 * 
 * @author joverbey
 */
public final class LexerFactory
{
    public static boolean AssociateLineCol = false;
    
    private LexerFactory() {;}
    
    public static ILexer createFreeFormLexer(InputStream in, String filename)
    {
        return new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename));
    }
    
    public static ILexer createFixedFormLexer(InputStream in, String filename)
    {
        return new FixedFormLexerPhase2(in, filename);
    }
    
    public static ILexer createLexer(File file, boolean isFixedForm) throws FileNotFoundException
    {
        String filename = file.getAbsolutePath();
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        return isFixedForm
            ? createFixedFormLexer(in, filename)
            : createFreeFormLexer(in, filename);
    }
}
