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
package org.eclipse.fdt.core.dom.ast;


/**
 * 
 * @author Doug Schaefer
 */
public interface IScope {

	/**
	 * Scopes are arranged hierarchically. Lookups will generally
	 * flow upward to find resolution.
	 * 
	 * @return
	 */
	public IScope getParent() throws DOMException;

	/**
	 * This is the general lookup entry point. It returns the list of
	 * valid bindings for a given name.
	 * 
	 * @param searchString
	 * @return List of IBinding
	 */
	public IBinding[] find(String name) throws DOMException;

    /**
     * @return
     */
    public IASTNode getPhysicalNode() throws DOMException;
}
