/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core;

import java.math.BigInteger;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;

/**
 * Interface for managing debugger breakpoints
 */
public interface IPBreakpointManager {

	/**
	 * Called when tasks are added to a task set. This causes the breakpoints to
	 * be set on the new tasks also.
	 * 
	 * @param tasks
	 *            new tasks being added to the set
	 * @param breakpoints
	 *            breakpoints that are to be updated
	 * @since 4.0
	 */
	public void addSetBreakpoints(TaskSet tasks, IPBreakpoint[] breakpoints);

	/**
	 * Called when tasks are removed from a task set. This causes the
	 * breakpoints to be removed from the tasks that are no longer in the set.
	 * 
	 * @param tasks
	 *            tasks being removed from the set
	 * @param breakpoints
	 *            breakpoints that are to be updated
	 * @since 4.0
	 */
	public void deleteSetBreakpoints(TaskSet tasks, IPBreakpoint[] breakpoints);

	/**
	 * Delete a breakpoint. Removes the breakpoint and its marker.
	 * 
	 * @param breakpoint
	 *            breakpoint to remove
	 */
	public void deleteBreakpoint(IPBreakpoint breakpoint);

	/**
	 * Obtain the platform breakpoint interface from the PDI breakpoint.
	 * 
	 * @param pdiBpt
	 * @return breakpoint
	 */
	public IBreakpoint getBreakpoint(IPDIBreakpoint pdiBpt);

	/**
	 * Get the address defined by a line breakpoint
	 * 
	 * @param breakpoint
	 * @return breakpoint address
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint);

	/**
	 * Enable/disable breakpoint skipping.
	 * 
	 * @param enabled
	 */
	public void skipBreakpoints(boolean enabled);

	/**
	 * Notify that a watchpoint is out of scope
	 * 
	 * @param tasks
	 * @param pdiWatchpoint
	 * @since 4.0
	 */
	public void watchpointOutOfScope(TaskSet tasks, IPDIWatchpoint pdiWatchpoint);

}
