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
 * A <code>TokenFactory</code> that creates tokens used in the AST.
 * 
 * @author Jeff Overbey
 */
public class ASTTokenFactory implements TokenFactory
{
	private ASTTokenFactory() {}
	
	private static ASTTokenFactory instance = null;
	
	public static ASTTokenFactory getInstance()
	{
		if (instance == null) instance = new ASTTokenFactory();
		return instance;
	}
	
	public IToken createToken(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
	{
		return new Token(terminal, whiteBefore, tokenText, whiteAfter);
	}

	public IToken createToken(Terminal terminal, String tokenText)
	{
		return new Token(terminal, tokenText);
	}

}
