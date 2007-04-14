package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contains constants enumerating the various Fortran source forms.
 * 
 * Internally, this class is used as a Strategy object in <code>LexerFactory</code>.
 * 
 * @author joverbey
 */
public abstract class SourceForm
{
    private SourceForm() {;}
    
    abstract IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException;
    
    public static final SourceForm UNPREPROCESSED_FREE_FORM = new SourceForm()
    {
        IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
        {
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename)));
        }
    };
    
    public static final SourceForm FIXED_FORM = new SourceForm()
    {
        IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
        {
            return new LexerPhase3(new FixedFormLexerPhase2(in, filename));
        }
    };
    
    // FIXME: JEFF: Automatically detect lexer type from filename extension
    public static final SourceForm AUTO_DETECT_SOURCE_FORM = UNPREPROCESSED_FREE_FORM;
    
    public static SourceForm preprocessedFreeForm(final IncludeLoaderCallback callback)
    {
        return new SourceForm()
        {
            IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
            {
                return new LexerPhase3(new FreeFormLexerPhase2(new PreprocessingFreeFormLexerPhase1(in, filename, callback)));
            }
        };
    }
}
