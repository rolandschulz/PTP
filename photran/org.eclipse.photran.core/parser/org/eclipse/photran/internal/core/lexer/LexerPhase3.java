package org.eclipse.photran.internal.core.lexer;

import org.eclipse.photran.internal.core.parser.Parser;

/**
 * This is the lexical analyzer that is used directly in the Fortran parser.
 * 
 * This is connected to either <code>FreeFormLexerPhase2</code> or <code>FixedFormLexerPhase2</code>,
 * which does the "real" work; <code>LexerPhase3</code> accumulates tokens in an <code>ArrayList</code>
 * so that they can be binary searched and accessed by index.
 * 
 * @author Jeffrey Overbey
 * 
 * @see FreeFormLexerPhase2
 * @see FixedFormLexerPhase2
 * @see Parser
 */
class LexerPhase3 implements IAccumulatingLexer
{
    private ILexer phase2Lexer;
    private TokenList tokenList;
    
    public LexerPhase3(ILexer phase2Lexer)
    {
        this.phase2Lexer = phase2Lexer;
        this.tokenList = new TokenList();
    }

    public String getFilename()
    {
        return phase2Lexer.getFilename();
    }

    public int getLastTokenCol()
    {
        return phase2Lexer.getLastTokenCol();
    }

    public int getLastTokenLength()
    {
        return phase2Lexer.getLastTokenLength();
    }

    public int getLastTokenLine()
    {
        return phase2Lexer.getLastTokenLine();
    }

    public int getLastTokenOffset()
    {
        return phase2Lexer.getLastTokenOffset();
    }

    public Token yylex() throws Exception
    {
        Token token = phase2Lexer.yylex();
        tokenList.add(token);
        return token;
    }
    
    public TokenList getTokenList()
    {
        return tokenList;
    }
}
