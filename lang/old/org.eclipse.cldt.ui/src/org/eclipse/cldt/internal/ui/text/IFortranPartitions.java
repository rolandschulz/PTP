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

/**
 * 
 * The name of the C partitioning.
 * @since 3.0
 */
public interface IFortranPartitions {

	public final static String SKIP= "__skip"; //$NON-NLS-1$
	public final static String FORTRAN_SINGLE_LINE_COMMENT= "fortran_single_line_comment"; //$NON-NLS-1$
	public final static String FORTRAN_MULTILINE_COMMENT= "fortran_multiline_comment"; //$NON-NLS-1$
	public final static String FORTRAN_STRING= "fortran_string"; //$NON-NLS-1$

}
