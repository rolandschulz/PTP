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
    public static final int AUTO_DETECT_SOURCE_FORM = 0;
    public static final int FIXED_FORM = 1;
    public static final int FREE_FORM = 2;
    
    private LexerFactory() {;}
    
    public static IAccumulatingLexer createLexer(InputStream in, String filename, int options) throws FileNotFoundException
    {
        if ((options & FIXED_FORM) != 0)
            return new LexerPhase3(new FixedFormLexerPhase2(in, filename));
        else if ((options & FREE_FORM) != 0)
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename)));
        else // FIXME: JEFF: Automatically detect lexer type from filename extension
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename)));
    }
    
    public static IAccumulatingLexer createLexer(File file, int options) throws FileNotFoundException
    {
        return createLexer(new BufferedInputStream(new FileInputStream(file)), file.getAbsolutePath(), options);
    }
    
    public static IAccumulatingLexer createLexer(IFile file, int options) throws FileNotFoundException, CoreException
    {
        return createLexer(file.getContents(), file.getName(), options);
    }
}
