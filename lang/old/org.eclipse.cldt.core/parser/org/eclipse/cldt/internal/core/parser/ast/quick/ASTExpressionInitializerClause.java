/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.parser.ast.quick;

import org.eclipse.cldt.core.parser.ast.IASTExpression;
import org.eclipse.cldt.core.parser.ast.IASTInitializerClause;

/**
 * @author jcamelon
 */
public class ASTExpressionInitializerClause extends ASTInitializerClause
		implements
			IASTInitializerClause {

	private final IASTExpression expression;

	/**
	 * @param kind
	 * @param assignmentExpression
	 */
	public ASTExpressionInitializerClause(Kind kind, IASTExpression assignmentExpression) {
		super( kind );
		this.expression = assignmentExpression;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getAssigmentExpression()
	 */
	public IASTExpression getAssigmentExpression() {
		return expression;
	}
}
