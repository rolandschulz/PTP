/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.fdt.core.parser;

/**
 * @author jcamelon
 *
 */
public interface IGCCToken extends IToken {
	
	public static final int t_typeof = tLAST + 1;
	public static final int t___alignof__ = tLAST + 2;
	public static final int tMAX = tLAST + 3;
	public static final int tMIN = tLAST + 4;
	
}
