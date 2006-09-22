package org.eclipse.photran.internal.core.lexer;

import org.eclipse.photran.internal.core.lexer.Token;

/**
 * Common interface implemented by fixed and free form Fortran lexers
 * 
 * @author joverbey
 */
public interface ILexer
{
    Token yylex() throws Exception;

    void setFilename(String filename);
}
