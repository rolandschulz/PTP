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
package org.eclipse.fdt.internal.core.parser.ast.quick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.fdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTExceptionSpecification implements IASTExceptionSpecification
{
	private final List typeIds; 
    /**
     * @param typeIds
     */
    public ASTExceptionSpecification(List typeIds)
    {
    	Iterator i = typeIds.iterator();
    	this.typeIds = new ArrayList();
    	while( i.hasNext() )
    		this.typeIds.add( ((IASTTypeId)i.next()).getTypeOrClassName());
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTExceptionSpecification#getTypeIds()
     */
    public Iterator getTypeIds()
    {
        return typeIds.iterator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor )
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor )
    {
    }
}
