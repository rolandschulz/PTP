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

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Provides a basic functionality for the location breakpoints, watchpoints and
 * catchpoints.
 * 
 * @author clement
 * 
 */
public interface IPDIBreakpoint extends IPDISessionObject {
	/**
	 * Regular type
	 */
	public final static int REGULAR = 0x0;

	/**
	 * Temporary type
	 */
	public final static int TEMPORARY = 0x1;

	/**
	 * Hardware type
	 */
	public final static int HARDWARE = 0x2;

	/**
	 * Returns breakpoint id of this breakpoint
	 * 
	 * @return breakpoint id of this breakpoint
	 */
	public int getBreakpointID();

	/**
	 * Returns the condition of this breakpoint or null if no condition in this
	 * breakpoint
	 * 
	 * @return the condition of this breakpoint
	 * @throws PDIException
	 *             on failure
	 */
	public IPDICondition getCondition() throws PDIException;

	/**
	 * Returns internal breakpoint id
	 * 
	 * @return internal breakpoint id
	 */
	public int getInternalID();

	/**
	 * This is the set of pending tasks for the current operation. If the
	 * breakpoint is marked for deletion, it's the tasks that still require the
	 * breakpoint to be removed. Otherwise, its the tasks on which the
	 * breakpoint still needs to be set.
	 * 
	 * @return pending tasks
	 * @since 4.0
	 */
	public TaskSet getPendingTasks();

	/**
	 * Breakpoint has been marked for deletion. The breakpoint is not actually
	 * removed until getPendingTasks().isEmpty() is true.
	 * 
	 * @return true if breakpoint is being deleted
	 */
	public boolean isDeleted();

	/**
	 * Determines whether this breakpoint is enabled
	 * 
	 * @return whether this breakpoint is enabled
	 * @throws PDIException
	 *             on failure
	 */
	public boolean isEnabled() throws PDIException;

	/**
	 * Determines whether this breakpoint is hardware-assisted
	 * 
	 * @return whether this breakpoint is hardware-assisted
	 */
	public boolean isHardware();

	/**
	 * Determines whether this breakpoint is temporary
	 * 
	 * @return whether this breakpoint is temporary
	 */
	public boolean isTemporary();

	/**
	 * Sets a breakpoint id for this breakpoint
	 * 
	 * @param bpid
	 *            breakpoint id
	 */
	public void setBreakpointID(int bpid);

	/**
	 * Marks the breakpoint for deletion. The breakpoint should be considered
	 * deleted at this point and no further operations permitted.
	 */
	public void setDeleted();

	/**
	 * Sets the breakpoint state to be enabled or disabled.
	 * 
	 * @param enabled
	 *            whether this breakpoint should be enabled
	 * @throws PDIException
	 *             on failure
	 */
	public void setEnabled(boolean enabled) throws PDIException;

	/**
	 * Sets the condition of this breakpoint.
	 * 
	 * @param condition
	 *            the condition to set
	 * @throws PDIException
	 *             on failure
	 */
	public void setCondition(IPDICondition condition) throws PDIException;
}
