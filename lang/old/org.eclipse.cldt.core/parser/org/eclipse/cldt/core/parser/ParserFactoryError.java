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
package org.eclipse.cldt.core.parser;

/**
 * @author jcamelon
 *
 */
public class ParserFactoryError extends Error {

	public static class Kind extends Enum {
		
		public static final Kind NULL_READER 		= new Kind( 1 );
		public static final Kind NULL_FILENAME		= new Kind( 2 );
		public static final Kind NULL_CONFIG			= new Kind( 3 );
		public static final Kind NULL_LANGUAGE	= new Kind( 4 );
		public static final Kind NULL_SCANNER		= new Kind( 5 );
		public static final Kind BAD_DIALECT            = new Kind( 6 );
		
		protected Kind( int arg )
		{
			super( arg );
		}
	}
	
	public ParserFactoryError( Kind e )
	{
		kind = e;
	}
	
	public Kind getKind()
	{
		return kind;
	}
	
	private Kind kind;	
}
