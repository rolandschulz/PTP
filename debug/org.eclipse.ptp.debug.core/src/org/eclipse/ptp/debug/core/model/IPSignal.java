/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Represents a debugger signal handler
 * 
 * @author Clement chu
 */

public interface IPSignal extends IPDebugElement {
	/**
	 * Returns the name of this signal
	 * 
	 * @return this signal's name
	 * @throws DebugException
	 *             if this method fails.
	 */
	public String getName() throws DebugException;

	/**
	 * Returns the description of this signal.
	 * 
	 * @return this signal's description
	 * @throws DebugException
	 *             if this method fails.
	 */
	public String getDescription() throws DebugException;

	/**
	 * Returns whether "pass" is in effect for this signal.
	 * 
	 * @return whether "pass" is in effect for this signal
	 * @throws DebugException
	 *             if this method fails.
	 */
	public boolean isPassEnabled() throws DebugException;

	/**
	 * Returns whether "stop" is in effect for this signal.
	 * 
	 * @return whether "stop" is in effect for this signal
	 * @throws DebugException
	 *             if this method fails.
	 */
	public boolean isStopEnabled() throws DebugException;

	/**
	 * Enables/disables the "pass" flag of this signal.
	 * 
	 * @param enable
	 *            the flag value to set
	 * @throws DebugException
	 *             if this method fails.
	 */
	public void setPassEnabled(boolean enable) throws DebugException;

	/**
	 * Enables/disables the "stop" flag of this signal.
	 * 
	 * @param enable
	 *            the flag value to set
	 * @throws DebugException
	 *             if this method fails.
	 */
	public void setStopEnabled(boolean enable) throws DebugException;

	/**
	 * Resumes execution, but immediately gives the target this signal.
	 * 
	 * @throws DebugException
	 *             if this method fails.
	 */
	public void signal() throws DebugException;

	/**
	 * Returns whether modification is allowed for this signal's parameters.
	 * 
	 * @return whether modification is allowed for this signal's parameters
	 */
	public boolean canModify();
}
