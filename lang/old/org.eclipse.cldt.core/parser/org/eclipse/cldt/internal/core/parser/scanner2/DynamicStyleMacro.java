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
package org.eclipse.cldt.internal.core.parser.scanner2;

import org.eclipse.cldt.core.parser.IMacro;

/**
 * @author jcamelon
 *
 */
public abstract class DynamicStyleMacro implements IMacro{

	public abstract char [] execute();
	
	public DynamicStyleMacro( char [] n )
	{
		name = n;
	}
	public final char [] name; 

	public char[] getSignature()
	{
	    return name;
	}
	public char[] getName()
	{
	    return name;
	}
}
