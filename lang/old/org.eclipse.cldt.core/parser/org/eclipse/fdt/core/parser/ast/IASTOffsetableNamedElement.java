/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.core.parser.ast;


/**
 * @author jcamelon
 *
 */
public interface IASTOffsetableNamedElement extends IASTOffsetableElement {

	public String getName(); 
	public char[] getNameCharArray();
	public int getNameOffset(); 
	public void setNameOffset( int o );
	public int  getNameEndOffset(); 
	public void setNameEndOffsetAndLineNumber( int offset, int lineNumber );
	public int  getNameLineNumber();
}
