/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cldt.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * A watchpoint specific to the C/C++ debug model.
 */
public interface ICWatchpoint extends ICBreakpoint, ILineBreakpoint {

	/**
	 * Watchpoint attribute storing the expression associated with this 
	 * watchpoint (value <code>"org.eclipse.cldt.debug.core.expression"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String EXPRESSION = "org.eclipse.cldt.debug.core.expression"; //$NON-NLS-1$	

	/**
	 * Write access watchpoint attribute (value <code>"org.eclipse.cldt.debug.core.write"</code>).
	 * This attribute is a <code>boolean</code>.
	 */
	public static final String WRITE = "org.eclipse.cldt.debug.core.write"; //$NON-NLS-1$	

	/**
	 * Read access watchpoint attribute (value <code>"org.eclipse.cldt.debug.core.read"</code>).
	 * This attribute is a <code>boolean</code>.
	 */
	public static final String READ = "org.eclipse.cldt.debug.core.read"; //$NON-NLS-1$

	/**
	 * Returns whether this watchppoint is a write watchpoint.
	 * 
	 * @return whether this watchppoint is a write watchpoint
	 */
	boolean isWriteType() throws CoreException;

	/**
	 * Returns whether this watchppoint is a read watchpoint.
	 * 
	 * @return whether this watchppoint is a read watchpoint
	 */
	boolean isReadType() throws CoreException;

	/**
	 * Returns the watchpoint's expression.
	 * 
	 * @return the expression of this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getExpression() throws CoreException;
}