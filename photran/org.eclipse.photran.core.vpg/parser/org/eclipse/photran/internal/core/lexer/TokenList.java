/*******************************************************************************
 * Copyright (c) 2007-2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

/**
 * A list of all of the tokens in a Fortran program; provides an efficient means of locating the
 * token at a particular offset.
 * 
 * @author Jeff Overbey
 * @see org.eclipse.rephraserengine.core.util.TokenList
 */
public final class TokenList extends org.eclipse.rephraserengine.core.util.TokenList<Token>
{
    public TokenList()
    {
    	super();
    }
    
    public TokenList(ASTExecutableProgramNode ast)
    {
    	this();
    	ast.accept(new ASTVisitorWithLoops()
    	{
			public void visitToken(Token token)
			{
				add(token);
			}
    	});
    }

    @Override
    protected Token[] createTokenArray(int size)
    {
        return new Token[size];
    }

    @Override
    protected int getStreamOffset(Token token)
    {
        return token.getStreamOffset();
    }

    @Override
    protected int getLength(Token token)
    {
        return token.getLength();
    }

    @Override
    protected int getLine(Token token)
    {
        return token.getLine();
    }
}
