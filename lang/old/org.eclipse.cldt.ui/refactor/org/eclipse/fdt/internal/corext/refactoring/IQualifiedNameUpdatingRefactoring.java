/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.corext.refactoring;

public interface IQualifiedNameUpdatingRefactoring {

	/**
	 * Performs a dynamic check whether this refactoring object is capable of
	 * updating qualified names in non C files. The return value of this
	 * method may change according to the state of the refactoring.
	 */
	public boolean canEnableQualifiedNameUpdating();
	
	/**
	 * If <code>canEnableQualifiedNameUpdating</code> returns <code>true</code>,
	 * then this method is used to ask the refactoring object whether references
	 * in non C files should be updated. This call can be ignored if
	 * <code>canEnableQualifiedNameUpdating</code> returns <code>false</code>.
	 */		
	public boolean getUpdateQualifiedNames();

	/**
	 * If <code>canEnableQualifiedNameUpdating</code> returns <code>true</code>,
	 * then this method is used to inform the refactoring object whether
	 * references in non C files should be updated. This call can be ignored
	 * if <code>canEnableQualifiedNameUpdating</code> returns <code>false</code>.
	 */	
	public void setUpdateQualifiedNames(boolean update);
	
	public String getFilePatterns();
	
	public void setFilePatterns(String patterns);
}


