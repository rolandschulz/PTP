package org.eclipse.photran.internal.core.lexer;

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
    
    int getLastTokenOffset();
    
    int getLastTokenLength();
}
