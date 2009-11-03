/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

/**
 * A {@link TokenFactory} that generates {@link SimpleToken} objects.
 * <p>
 * <b>This is probably not the {@link TokenFactory} you're looking for.</b>  The Fortran parser uses
 * a different factory (<code>ASTTokenFactory</code>); this one is only used if the lexer is used
 * in isolation.
 * 
 * @author Jeff Overbey
 */
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
