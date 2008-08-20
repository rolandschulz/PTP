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

import org.eclipse.core.resources.IFile;

/**
 * This is the lexical analyzer that is used directly in the Fortran parser.
 * 
 * This is connected to either <code>FreeFormLexerPhase2</code> or <code>FixedFormLexerPhase2</code>,
 * which does the "real" work; <code>LexerPhase3</code> accumulates tokens in an array
 * so that they can be binary searched and accessed by their source offsets.
 * 
 * @author Jeffrey Overbey
 * 
 * @see FreeFormLexerPhase2
 * @see FixedFormLexerPhase2
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
    
    public TokenFactory getTokenFactory()
    {
    	return phase2Lexer.getTokenFactory();
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

    public IFile getLastTokenFile()
    {
        return phase2Lexer.getLastTokenFile();
    }

    public int getLastTokenFileOffset()
    {
        return phase2Lexer.getLastTokenFileOffset();
    }

    public int getLastTokenStreamOffset()
    {
        return phase2Lexer.getLastTokenStreamOffset();
    }

    public Token yylex() throws Exception
    {
        Token token = (Token)phase2Lexer.yylex();
        tokenList.add(token);
        return token;
    }
    
    public TokenList getTokenList()
    {
        return tokenList;
    }
}
