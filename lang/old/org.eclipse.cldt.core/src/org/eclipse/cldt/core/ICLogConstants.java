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

package org.eclipse.cldt.core;

/**
 * @author bgheorgh
 */
public interface ICLogConstants {
	public class LogConst{
		private LogConst( int value )
		{
			this.value = value;
		}
		private final int value;
	}
	
	
   public static final LogConst PDE = new LogConst( 1 );
   public static final LogConst CDT = new LogConst( 2 );
   
}
