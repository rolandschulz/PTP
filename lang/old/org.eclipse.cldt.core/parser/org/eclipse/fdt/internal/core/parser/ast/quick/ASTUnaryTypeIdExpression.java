/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.fdt.internal.core.parser.ast.quick;

import org.eclipse.fdt.core.parser.ast.ASTUtil;
import org.eclipse.fdt.core.parser.ast.IASTExpression;
import org.eclipse.fdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTUnaryTypeIdExpression extends ASTUnaryExpression
		implements
			IASTExpression {

	private final IASTTypeId typeId;

	/**
	 * @param kind
	 * @param lhs
	 * @param typeId
	 */
	public ASTUnaryTypeIdExpression(Kind kind, IASTExpression lhs, IASTTypeId typeId) {
		super( kind, lhs );
		this.typeId = typeId;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return typeId;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
