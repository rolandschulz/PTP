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

package org.eclipse.fdt.core.parser;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FortranParserLanguage extends ParserLanguage {
	public final static FortranParserLanguage Fortran = new FortranParserLanguage( 3 );

	protected FortranParserLanguage( int value )
	{
		super( value ); 
	}

	/**
	 * @return
	 */
	public boolean isFortran() {
		return ( this == Fortran );
	}
    
    public String toString() {
        return "Fortran"; //$NON-NLS-1$
    }
}
