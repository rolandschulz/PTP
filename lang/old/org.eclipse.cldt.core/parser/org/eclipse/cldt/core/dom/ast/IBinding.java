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
 * @author Doug Schaefer
 */
public interface IBinding {

	/**
	 * The name of the binding.
	 * 
	 * @return name
	 */
	public String getName();
	public char[] getNameCharArray();
	
	/**
	 * Every name has a scope.
	 * 
	 * @return the scope of this name
	 */
	public IScope getScope() throws DOMException;

}
