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
import org.eclipse.fdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICPPASTUsingDirective extends IASTDeclaration {
	public static final ICPPASTUsingDirective [] EMPTY_USINGDIRECTIVE_ARRAY = new ICPPASTUsingDirective[0];
    public static final ASTNodeProperty QUALIFIED_NAME = new ASTNodeProperty( "Name"); //$NON-NLS-1$
    
    public IASTName getQualifiedName();
    public void setQualifiedName( IASTName qualifiedName );
    
}
