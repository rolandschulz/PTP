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
 * <code>ILexer</code> is the common interface implemented by fixed and free form Fortran lexers.
 * An <code>IAccumulatingLexer</code> is a lexer which maintains a list of tokens which can be
 * iterated through, binary searched, etc.
 * 
 * @author Jeff Overbey
 */
public interface IAccumulatingLexer extends ILexer
{
    TokenList getTokenList();
    
    Token yylex() throws Exception;
}
