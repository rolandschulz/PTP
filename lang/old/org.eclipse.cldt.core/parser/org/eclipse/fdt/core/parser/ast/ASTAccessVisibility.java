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

import org.eclipse.fdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class ASTAccessVisibility extends Enum {

	public static final ASTAccessVisibility PUBLIC = new ASTAccessVisibility( 1 );
	public static final ASTAccessVisibility PROTECTED = new ASTAccessVisibility( 2 );
	public static final ASTAccessVisibility PRIVATE = new ASTAccessVisibility( 3 );

	private ASTAccessVisibility( int constant)
	{
		super( constant ); 
	}
	
	public boolean isLessThan( ASTAccessVisibility other )
	{
		return getEnumValue() < other.getEnumValue();
	}

	public boolean isGreaterThan( ASTAccessVisibility other )
	{
		return getEnumValue() > other.getEnumValue();
	}
	
	
	 
}
