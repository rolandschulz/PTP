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
package org.eclipse.fdt.core.parser;

/**
 * @author jcamelon
 */
public class Directives {

	public static final String POUND_DEFINE = "#define"; //$NON-NLS-1$
	public static final String POUND_UNDEF = "#undef"; //$NON-NLS-1$
	public static final String POUND_IF = "#if"; //$NON-NLS-1$
	public static final String POUND_IFDEF = "#ifdef"; //$NON-NLS-1$
	public static final String POUND_IFNDEF = "#ifndef"; //$NON-NLS-1$
	public static final String POUND_ELSE = "#else"; //$NON-NLS-1$
	public static final String POUND_ENDIF = "#endif"; //$NON-NLS-1$
	public static final String POUND_INCLUDE = "#include"; //$NON-NLS-1$
	public static final String POUND_LINE = "#line"; //$NON-NLS-1$
	public static final String POUND_ERROR = "#error"; //$NON-NLS-1$
	public static final String POUND_PRAGMA = "#pragma"; //$NON-NLS-1$
	public static final String POUND_ELIF = "#elif"; //$NON-NLS-1$
	public static final String POUND_BLANK = "#"; //$NON-NLS-1$
	public static final String _PRAGMA = "_Pragma"; //$NON-NLS-1$
	
}
