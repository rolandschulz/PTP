package org.eclipse.photran.internal.core.lexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * A collection of (static) factory methods for creating Fortran lexers.
 * 
 * @author joverbey
 */
public final class LexerFactory
{
    private LexerFactory() {;}
    
    public static ILexer createLexer(InputStream in, String filename, int options) throws FileNotFoundException
    {
        if ((options & LexerOptions.FIXED_FORM) != 0)
            return new FixedFormLexerPhase2(in, filename, options);
        else if ((options & LexerOptions.FREE_FORM) != 0)
            return new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename), options);
        else // FIXME: JEFF: Automatically detect lexer type from filename extension
            return new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename), options);
    }
    
    public static ILexer createLexer(File file, int options) throws FileNotFoundException
    {
        return createLexer(new BufferedInputStream(new FileInputStream(file)), file.getAbsolutePath(), options);
    }
    
    public static ILexer createLexer(IFile file, int options) throws FileNotFoundException, CoreException
    {
        return createLexer(file.getContents(), file.getName(), options);
    }
}
