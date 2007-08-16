package org.eclipse.photran.internal.core.lexer;

public class SimpleTokenFactory implements TokenFactory
{
    private SimpleTokenFactory() {}
    
    private static SimpleTokenFactory instance = null;
    
    public static SimpleTokenFactory getInstance()
    {
        if (instance == null) instance = new SimpleTokenFactory();
        return instance;
    }
    
    public IToken createToken(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
    {
        return new SimpleToken(terminal, tokenText);
    }
    
    public IToken createToken(Terminal terminal, String tokenText)
    {
        return new SimpleToken(terminal, tokenText);
    }
}
