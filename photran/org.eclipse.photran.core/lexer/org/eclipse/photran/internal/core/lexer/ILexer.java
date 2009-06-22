package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;

import org.eclipse.core.resources.IFile;

/**
 * Common interface implemented by fixed and free form Fortran lexers
 * 
 * @author Jeff Overbey
 */
public interface ILexer
{
    IToken yylex() throws IOException, LexerException;

    TokenFactory getTokenFactory();
    
    String getFilename();

    int getLastTokenLine();

    int getLastTokenCol();
    
    IFile getLastTokenFile();
    
    int getLastTokenFileOffset();
    
    int getLastTokenStreamOffset();
    
    int getLastTokenLength();
}
