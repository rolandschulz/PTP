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
package org.eclipse.cldt.core.dom.ast;

/**
 * @author jcamelon
 */
public interface IASTProblemHolder {
    public static final ASTNodeProperty PROBLEM = new ASTNodeProperty( "Problem"); //$NON-NLS-1$
    public IASTProblem getProblem();
    public void setProblem(IASTProblem p);    
}