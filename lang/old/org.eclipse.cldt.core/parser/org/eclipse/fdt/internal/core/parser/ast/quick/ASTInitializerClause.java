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
import org.eclipse.fdt.core.parser.ITokenDuple;
import org.eclipse.fdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.fdt.core.parser.ast.IASTExpression;
import org.eclipse.fdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.fdt.core.parser.ast.IASTVariable;
import org.eclipse.fdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class ASTInitializerClause implements IASTInitializerClause {

	private final IASTInitializerClause.Kind kind; 
	private IASTVariable ownerDeclaration = null;
	/**
	 * @param kind
	 * @param assignmentExpression
	 * @param initializerClauses
	 */
	public ASTInitializerClause(Kind kind ) {
		this.kind = kind; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#getKind()
	 */
	public Kind getKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#getInitializerList()
	 */
	public Iterator getInitializers() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#getAssigmentExpression()
	 */
	public IASTExpression getAssigmentExpression() {
		return null;
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
     * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#getDesignators()
     */
    public Iterator getDesignators()
    {
        return EmptyIterator.EMPTY_ITERATOR;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#setOwnerDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
     */
    public void setOwnerVariableDeclaration(IASTVariable declaration)
    {
        ownerDeclaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#getOwnerDeclaration()
     */
    public IASTVariable getOwnerVariableDeclaration()
    {
        return ownerDeclaration;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTInitializerClause#findExpressionForDuple(org.eclipse.fdt.core.parser.ITokenDuple)
	 */
	public IASTExpression findExpressionForDuple(ITokenDuple finalDuple) throws ASTNotImplementedException {
		throw new ASTNotImplementedException();
	}

}
