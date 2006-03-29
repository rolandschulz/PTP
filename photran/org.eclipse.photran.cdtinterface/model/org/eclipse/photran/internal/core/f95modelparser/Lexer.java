package org.eclipse.photran.internal.core.f95modelparser;

import java.io.InputStream;
import java.io.Reader;

/**
 * Provides factory methods for creating Fortran lexers
 * 
 * @author joverbey
 */
public final class Lexer
{
    private Lexer() {;}
    
    public static ILexer createFreeFormLexer(InputStream in, String filename)
    {
        return new FreeFormLexerPhase2(in, filename);
    }
    
    public static ILexer createFreeFormLexer(Reader in, String filename)
    {
        return new FreeFormLexerPhase2(in, filename);
    }
    
    public static ILexer createFixedFormLexer(InputStream in, String filename)
    {
        return new FixedFormLexerPhase2(in, filename);
    }
    
    public static ILexer createLexer(InputStream in, String filename, boolean isFixedForm)
    {
        if (isFixedForm)
            return createFixedFormLexer(in, filename);
        else
            return createFreeFormLexer(in, filename);
    }
    
//    public static ILexer createFixedFormLexer(Reader in, String filename)
//    {
//        return new FixedFormLexerPhase2(in, filename);
//    }
}
