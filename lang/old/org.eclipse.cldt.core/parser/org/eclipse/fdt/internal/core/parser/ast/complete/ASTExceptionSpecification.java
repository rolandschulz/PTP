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

import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.fdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 *
 */
public class ASTExceptionSpecification implements IASTExceptionSpecification
{
	private final List typeIds;
    /**
     * @param newTypeIds
     */
    public ASTExceptionSpecification(List newTypeIds)
    {
        this.typeIds = newTypeIds;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTExceptionSpecification#getTypeIds()
     */
    public Iterator getTypeIds()
    {
        if( typeIds == null ) return EmptyIterator.EMPTY_ITERATOR;
        return typeIds.iterator();
    }
}
