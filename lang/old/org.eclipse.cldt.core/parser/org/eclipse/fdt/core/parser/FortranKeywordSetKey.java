/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.core.parser;



public class FortranKeywordSetKey extends Enum
{
	public static final FortranKeywordSetKey EMPTY = new FortranKeywordSetKey( 0 );
	public static final FortranKeywordSetKey DECLARATION = new FortranKeywordSetKey( 1 );
	public static final FortranKeywordSetKey STATEMENT = new FortranKeywordSetKey( 2 );
	public static final FortranKeywordSetKey EXPRESSION = new FortranKeywordSetKey( 3 );
	public static final FortranKeywordSetKey ALL = new FortranKeywordSetKey( 4 );
	public static final FortranKeywordSetKey KEYWORDS = new FortranKeywordSetKey( 5 );
	public static final FortranKeywordSetKey TYPES = new FortranKeywordSetKey( 6 );
	/**
	 * @param enumValue
	 */
	protected FortranKeywordSetKey(int enumValue) {
		super(enumValue);
	}
	
}