package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;

/**
 * Common interface implemented by fixed and free form Fortran lexers.
 * 
 * @author Jeff Overbey
 */
public interface ILexer
{
    IToken yylex() throws IOException, LexerException;
    
    String getFilename();

    int getLastTokenLine();

    int getLastTokenCol();
    
    FileOrIFile getLastTokenFile();
    
    int getLastTokenFileOffset();
    
    int getLastTokenStreamOffset();
    
    int getLastTokenLength();
    
    /** Only needed in Phase 1 lexers (called by <code>CPreprocessingLexer</code>) */
    void setTokenAsCurrent(IToken token);
}
