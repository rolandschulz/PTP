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
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTPointer;

/**
 * This is a pointer to member pointer operator for declarators.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTPointerToMember extends IASTPointer {

    public static final ASTNodeProperty NAME = new ASTNodeProperty( "Name"); //$NON-NLS-1$
    public void setName( IASTName name );
    public IASTName getName();
	
}
