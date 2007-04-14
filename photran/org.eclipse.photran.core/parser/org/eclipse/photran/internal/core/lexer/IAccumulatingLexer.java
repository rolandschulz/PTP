package org.eclipse.photran.internal.core.lexer;

/**
 * <code>ILexer</code> is the common interface implemented by fixed and free form Fortran lexers.
 * An <code>IAccumulatingLexer</code> is a lexer which maintains a list of tokens which can be
 * iterated through, binary searched, etc.
 * 
 * @author joverbey
 */
public interface IAccumulatingLexer extends ILexer
{
    TokenList getTokenList();
}
