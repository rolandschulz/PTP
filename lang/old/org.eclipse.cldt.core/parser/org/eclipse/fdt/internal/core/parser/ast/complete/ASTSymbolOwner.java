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
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.internal.core.parser.pst.ISymbol;
import org.eclipse.fdt.internal.core.parser.pst.ISymbolOwner;

/**
 * @author jcamelon
 *
 */
public class ASTSymbolOwner extends ASTNode implements ISymbolOwner
{
    protected ISymbol symbol;
    /**
     * 
     */
    public ASTSymbolOwner( ISymbol symbol )
    {
    	this.symbol = symbol;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.parser.pst.ISymbolOwner#getSymbol()
     */
    public ISymbol getSymbol()
    {
        return symbol;
    }
    
    public void setSymbol( ISymbol symbol ) 
    {
    	this.symbol = symbol; 
    }
    
}
