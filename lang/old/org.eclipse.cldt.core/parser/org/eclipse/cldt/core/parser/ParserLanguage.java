/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cldt.core.parser;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ParserLanguage extends Enum {
	public final static ParserLanguage C   = new ParserLanguage( 1 );
	public final static ParserLanguage CPP = new ParserLanguage( 2 );
	public final static ParserLanguage FORTRAN = new ParserLanguage( 3 );

	private ParserLanguage( int value )
	{
		super( value ); 
	}

	/**
	 * @return
	 */
	public boolean isCPP() {
		return ( this == CPP );
	}
}
