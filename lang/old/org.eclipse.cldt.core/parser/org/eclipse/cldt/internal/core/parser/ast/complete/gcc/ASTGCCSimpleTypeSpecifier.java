/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cldt.internal.core.parser.ast.complete.gcc;

import java.util.List;

import org.eclipse.cldt.core.parser.ast.IASTExpression;
import org.eclipse.cldt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cldt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;
import org.eclipse.cldt.internal.core.parser.ast.complete.ASTSimpleTypeSpecifier;
import org.eclipse.cldt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTGCCSimpleTypeSpecifier extends ASTSimpleTypeSpecifier implements IASTGCCSimpleTypeSpecifier {
	
	private final IASTExpression expression;

	/**
	 * @param s
	 * @param b
	 * @param string
	 * @param references
	 */
	public ASTGCCSimpleTypeSpecifier(ISymbol s, boolean b, char[] string, List references, IASTExpression typeOfExpression ) {
		super(s, b, string, references);
		expression = typeOfExpression;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getType()
	 */
	public IASTSimpleTypeSpecifier.Type getType() {
		if( expression != null  ) return IASTGCCSimpleTypeSpecifier.Type.TYPEOF;
		return super.getType();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier#getTypeOfExpression()
	 */
	public IASTExpression getTypeOfExpression() {
		return expression;
	}
}
