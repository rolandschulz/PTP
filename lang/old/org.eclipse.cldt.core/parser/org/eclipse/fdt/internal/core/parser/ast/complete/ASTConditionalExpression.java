/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.fdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ITokenDuple;
import org.eclipse.fdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.fdt.core.parser.ast.ASTUtil;
import org.eclipse.fdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTConditionalExpression extends ASTBinaryExpression {
	private final IASTExpression thirdExpression;

	/**
	 * @param kind
	 * @param references
	 * @param lhs
	 * @param rhs
	 */
	public ASTConditionalExpression(Kind kind, List references,
			IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression) {
		super(kind, references, lhs, rhs);
		this.thirdExpression = thirdExpression;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression#getThirdExpression()
	 */
	public IASTExpression getThirdExpression() {
		return thirdExpression;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ASTExpression#findOwnerExpressionForIDExpression(org.eclipse.fdt.core.parser.ITokenDuple)
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		if( isIDExpressionForDuple( thirdExpression, duple )  )
			return this;
		ASTExpression result = recursiveFindExpressionForDuple(thirdExpression, duple);
		if( result != null )
			return result;
		return super.findOwnerExpressionForIDExpression(duple);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException {
		super.purgeReferences();
		thirdExpression.purgeReferences();
		purgeSubExpression( (ASTExpression) thirdExpression );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression#reconcileReferences()
	 */
	public void reconcileReferences() throws ASTNotImplementedException {
		super.reconcileReferences();
		thirdExpression.reconcileReferences();
		reconcileSubExpression((ASTExpression) thirdExpression);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks()
	 */
	protected void processCallbacks( ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor );
		thirdExpression.acceptElement( requestor );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.fdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		thirdExpression.freeReferences();
	}
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
