/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.core.dom.ast.cpp;

import org.eclipse.cldt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cldt.core.dom.ast.IASTExpression;
import org.eclipse.cldt.core.dom.ast.IASTInitializer;

/**
 * This is an initializer that is a call to the constructor for the
 * declarator.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTConstructorInitializer extends IASTInitializer {

	/**
	 * Get the arguments to the constructor.
	 * 
	 * @return IASTExpression
	 */
    public static final ASTNodeProperty EXPRESSION = new ASTNodeProperty( "Expression"); //$NON-NLS-1$
	public IASTExpression getExpression();
	public void setExpression( IASTExpression expression );
	
}
