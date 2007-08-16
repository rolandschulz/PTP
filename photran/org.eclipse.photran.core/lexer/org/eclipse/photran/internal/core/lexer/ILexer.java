package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;

/**
 * Common interface implemented by fixed and free form Fortran lexers
 * 
 * @author joverbey
 */
public interface ILexer
{
    IToken yylex() throws Exception;

    TokenFactory getTokenFactory();
    
    String getFilename();

    int getLastTokenLine();

    int getLastTokenCol();
    
    IFile getLastTokenFile();
    
    int getLastTokenFileOffset();
    
    int getLastTokenStreamOffset();
    
    int getLastTokenLength();
}
