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
package org.eclipse.cldt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cldt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author jcamelon
 *
 */
public class NamespaceSymbolExtension extends AbstractSymbolExtension
{
    /**
     * @author jcamelon
     *
     */
    private class LocalIterator implements Iterator
    {
    	private boolean donePrimary = false;
    	private Iterator secondaries = otherDefinitions.iterator();
        /**
         * 
         */
        public LocalIterator()
        {
            super();
        }
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            if( ! donePrimary ) return true;
            return secondaries.hasNext();
        }
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
        	if( ! hasNext() )
        		throw new NoSuchElementException();
        		
        	if( ! donePrimary )
        	{
        		donePrimary = true;
        		return primaryDeclaration;
        	}
            
            return secondaries.next();
        }
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
	protected List otherDefinitions = new ArrayList(); 
    /**
     * @param symbol
     * @param primaryDeclaration
     */
    public NamespaceSymbolExtension(
        ISymbol symbol,
        ASTSymbol primaryDeclaration)
    {
        super(symbol, primaryDeclaration);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#getAllDefinitions()
     */
    public Iterator getAllDefinitions()
    {
        return new LocalIterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#addDefinition(org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol)
     */
    public void addDefinition(ASTSymbol definition)
    {
        otherDefinitions.add( definition );
    }
}
