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

import org.eclipse.fdt.core.dom.ast.DOMException;

/**
 * Represents a member of a class. Adds in the visibility attribute.
 * 
 * @author Doug Schaefer
 */
public interface ICPPMember {

	/**
	 * The visibility.
	 *  
	 * @return
	 */
	public int getVisibility() throws DOMException;
	
	public static final int v_private = ICPPASTVisiblityLabel.v_private;
	public static final int v_protected = ICPPASTVisiblityLabel.v_protected;
	public static final int v_public = ICPPASTVisiblityLabel.v_public;
	
}
