package org.eclipse.photran.internal.core.lexer;

public interface TokenFactory
{
    public IToken createToken(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter);
    public IToken createToken(Terminal terminal, String tokenText);
}
