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

import org.eclipse.cldt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C++.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier, ICPPASTDeclSpecifier {
	// Extra types
	public static final int t_bool = IASTSimpleDeclSpecifier.t_last + 1;
	public static final int t_wchar_t = IASTSimpleDeclSpecifier.t_last + 2;
	public static final int t_last = t_wchar_t;
	
}
