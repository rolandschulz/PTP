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

import java.util.Iterator;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.fdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class ASTNewDescriptor implements IASTNewExpressionDescriptor {


	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getNewInitializerExpressions()
	 */
	public Iterator getNewInitializerExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getNewPlacementExpressions()
	 */
	public Iterator getNewPlacementExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getTypeIdExpressions()
	 */
	public Iterator getNewTypeIdExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
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
    public void enterScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#freeReferences(org.eclipse.fdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
	}

}
