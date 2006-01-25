package org.eclipse.photran.internal.core.f95modelparser;

import java.util.List;

import org.eclipse.photran.internal.core.f95modelparser.Token;

/**
 * Common interface implemented by fixed and free form Fortran lexers
 * 
 * @author joverbey
 */
public interface ILexer
{
    Token yylex() throws Exception;

    void setFilename(String filename);

    public List/* <NonTreeToken> */getNonTreeTokens();
}
