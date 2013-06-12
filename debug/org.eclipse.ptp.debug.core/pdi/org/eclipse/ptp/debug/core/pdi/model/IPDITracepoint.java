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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Defines a point in the program execution when the specified data to be collected.
 * 
 * @author clement
 * 
 */
public interface IPDITracepoint extends IPDISessionObject {
	/**
	 * Represents an action to be taken when the tracepoint is hit.
	 * 
	 * @author clement
	 * 
	 */
	public interface IAction {
		// Empty
	}

	/**
	 * Returns the location of this tracepoint.
	 * 
	 * @return the location of this tracepoint
	 * @throws PDIException
	 *             on failure
	 */
	public IPDILocation getLocation() throws PDIException;

	/**
	 * Returns whether this tracepoint is enabled.
	 * 
	 * @return whether this tracepoint is enabled
	 * @throws PDIException
	 *             on failure
	 */
	public boolean isEnabled() throws PDIException;

	/**
	 * Sets the enabled state of this tracepoint. This has no effect
	 * if the current enabled state is the same as specified by the enabled parameter.
	 * 
	 * @param enabled
	 *            - whether this tracepoint should be enabled
	 * @throws PDIException
	 *             on failure
	 */
	public void setEnabled(boolean enabled) throws PDIException;

	/**
	 * Returns the pass count of this tracepoint.
	 * 
	 * @return the pass count of this tracepoint
	 * @throws PDIException
	 *             on failure
	 */
	public int getPassCount() throws PDIException;

	/**
	 * Sets the pass count of this tracepoint.
	 * 
	 * @param the
	 *            pass count to set
	 * @throws PDIException
	 *             on failure
	 */
	public void setPassCount(int passCount) throws PDIException;

	/**
	 * Adds the given actions to the action list of this tracepoint.
	 * 
	 * @param actions
	 *            to add
	 * @throws PDIException
	 *             on failure
	 */
	public void addActions(IPDITracepoint.IAction[] actions) throws PDIException;

	/**
	 * Removes the given actions from the action list of this tracepoint.
	 * 
	 * @param actions
	 *            to remove
	 * @throws PDIException
	 *             on failure
	 */
	public void removeActions(IPDITracepoint.IAction[] actions) throws PDIException;

	/**
	 * Clears the action list of this tracepoint.
	 * 
	 * @throws PDIException
	 *             on failure
	 */
	public void clearActions() throws PDIException;

	/**
	 * Returns the actions assigned to this tracepoint.
	 * 
	 * @return the actions of this tracepoint
	 */
	public IPDITracepoint.IAction[] getActions();
}
