/*******************************************************************************
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.text;



/*
 * Color constants that we use for the preferences
 */
 
public interface IFortranColorConstants {
	/* The prefix all color constants start with */
	String PREFIX= "fortran_"; //$NON-NLS-1$
	
	/* The color key for multi-line comments in C code. */
	String FORTRAN_MULTI_LINE_COMMENT= "fortran_multi_line_comment"; //$NON-NLS-1$
	/* The color key for single-line comments in C code. */
	String FORTRAN_SINGLE_LINE_COMMENT= "fortran_single_line_comment"; //$NON-NLS-1$
	/* The color key for keywords in C code. */
	String FORTRAN_KEYWORD= "fortran_keyword"; //$NON-NLS-1$
	/* The color key for builtin types in C code. */
	String FORTRAN_TYPE= "fortran_type"; //$NON-NLS-1$
	/* The color key for string and character literals in C code. */
	String FORTRAN_STRING= "fortran_string"; //$NON-NLS-1$
    /** The color key for operators. */
    String FORTRAN_OPERATOR = "fortran_operators";
    /** The color key for braces. */
    String FORTRAN_BRACES = "fortran_braces";
    /** The color key for numbers. */
    String FORTRAN_NUMBER = "fortran_numbers";
	/* The color key for everthing in C code for which no other color is specified. */
	String FORTRAN_DEFAULT= "fortran_default"; //$NON-NLS-1$
    
    /**
     * The color key for task tags in C comments
     * (value <code>"c_comment_task_tag"</code>).
     */
    String TASK_TAG= "fortran_comment_task_tag"; //$NON-NLS-1$
}


