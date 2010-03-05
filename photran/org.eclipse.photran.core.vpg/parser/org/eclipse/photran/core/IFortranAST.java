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
package org.eclipse.photran.core;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;

/**
 * Interface implemented by a Fortran abstract syntax tree.
 * 
 * @author Jeff Overbey
 */
public interface IFortranAST extends Iterable<Token>
{
    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public void accept(IASTVisitor visitor);
    
    ///////////////////////////////////////////////////////////////////////////
    // Other Methods
    ///////////////////////////////////////////////////////////////////////////

    public IFile getFile();
    public ASTExecutableProgramNode getRoot();

    //public void rebuildTokenList();
    public Iterator<Token> iterator();
    public Token findTokenByStreamOffsetLength(int offset, int length);
    public Token findFirstTokenOnLine(int line);
    public Token findLastTokenOnLine(int line);
	public Token findTokenByFileOffsetLength(IFile file, int offset, int length);
}
