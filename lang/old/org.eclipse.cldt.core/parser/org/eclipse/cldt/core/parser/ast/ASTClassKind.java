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
package org.eclipse.cldt.core.parser.ast;

import org.eclipse.cldt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class ASTClassKind extends Enum {

	public final static ASTClassKind CLASS = new ASTClassKind( 1 );
	public final static ASTClassKind STRUCT = new ASTClassKind( 2 );
	public final static ASTClassKind UNION = new ASTClassKind( 3 );
	public final static ASTClassKind ENUM = new ASTClassKind( 4 );

	private ASTClassKind( int value )
	{
		super( value ); 
	}

}
