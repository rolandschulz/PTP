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
package org.eclipse.fdt.core.dom.ast.cpp;

import org.eclipse.fdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTWhileStatement;

/**
 * @author jcamelon
 *
 */
public interface ICPPASTWhileStatement extends IASTWhileStatement {

	public static final ASTNodeProperty CONDITIONDECLARATION = new ASTNodeProperty("initDeclaration");  //$NON-NLS-1$
	public IASTDeclaration getConditionDeclaration();	
	public void setConditionDeclaration(IASTDeclaration declaration);

}
