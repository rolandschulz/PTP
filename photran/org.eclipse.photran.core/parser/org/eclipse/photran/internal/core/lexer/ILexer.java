package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;

/**
 * Common interface implemented by fixed and free form Fortran lexers
 * 
 * @author joverbey
 */
public interface ILexer
{
    Token yylex() throws Exception;

    String getFilename();

    int getLastTokenLine();

    int getLastTokenCol();
    
    IFile getLastTokenFile();
    
    int getLastTokenFileOffset();
    
    int getLastTokenStreamOffset();
    
    int getLastTokenLength();
}
