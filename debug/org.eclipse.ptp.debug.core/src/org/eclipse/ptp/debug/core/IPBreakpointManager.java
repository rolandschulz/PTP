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
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;

public interface IPBreakpointManager {

	/**
	 * Called when tasks are added to a task set. This causes the breakpoints
	 * to be set on the new tasks also.
	 * 
	 * @param tasks new tasks being added to the set
	 * @param breakpoints breakpoints that are to be updated
	 */
	public void addSetBreakpoints(BitList tasks, IPBreakpoint[] breakpoints);

	/**
	 * @param tasks
	 * @param breakpoints
	 */
	public void deleteSetBreakpoints(BitList tasks, IPBreakpoint[] breakpoints);

	/**
	 * @param pdiBpt
	 * @return
	 */
	public IBreakpoint getBreakpoint(IPDIBreakpoint pdiBpt);

	/**
	 * @param breakpoint
	 * @return
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint);

	/**
	 * @param enabled
	 */
	public void skipBreakpoints(boolean enabled);

	/**
	 * @param tasks
	 * @param pdiWatchpoint
	 */
	public void watchpointOutOfScope(BitList tasks, IPDIWatchpoint pdiWatchpoint);

}
