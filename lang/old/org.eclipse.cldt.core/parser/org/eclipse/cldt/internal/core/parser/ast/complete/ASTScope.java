/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.core.parser.ast.complete;

import java.util.Iterator;

import org.eclipse.cldt.core.parser.ast.IASTDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTScope;
import org.eclipse.cldt.internal.core.parser.ast.SymbolIterator;
import org.eclipse.cldt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cldt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public abstract class ASTScope extends ASTSymbol implements IASTScope 
{
    /**
     * @param symbol
     */
    public ASTScope(ISymbol symbol)
    {
        super(symbol);
    }
    
    public IContainerSymbol getContainerSymbol()
    {
    	return (IContainerSymbol)symbol;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations()
	{
		if( getContainerSymbol() != null ){
			return new SymbolIterator( getContainerSymbol().getContentsIterator() );
		}
		return null;
	}
	
	public void addDeclaration(IASTDeclaration declaration)
	{
	}
	
	public void initDeclarations()
	{	
	}
}
