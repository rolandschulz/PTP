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
package org.eclipse.ptp.debug.core.pdi.manager;

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;

/**
 * Represent breakpoint manager to manage breakpoints
 * @author clement
 *
 */
public interface IPDIBreakpointManager extends IPDIManager {
	/**
	 * Called when new tasks are added to a set. Updates the breakpoint so that
	 * it is set on the new tasks.
	 * 
	 * @param tasks tasks that have been added
	 * @param breakpoint breakpoint to update
	 * @throws PDIException on failure 
	 */
	public void addSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException;
	
	/**
	 * Create address location
	 * @param address
	 * @return
	 */
	public IPDIAddressLocation createAddressLocation(BigInteger address);

	/**
	 * Creates condition
	 * @param ignoreCount
	 * @param expression
	 * @param tids
	 * @return
	 */
	public IPDICondition createCondition(int ignoreCount, String expression, String[] tids);

	/**
	 * Create function location
	 * @param file
	 * @param function
	 * @return
	 */
	public IPDIFunctionLocation createFunctionLocation(String file, String function);
	
	/**
	 * Creates line location
	 * @param file
	 * @param line
	 * @return
	 */
	public IPDILineLocation createLineLocation(String file, int line);

	/**
	 * Deletes all breakpoint of all processes
	 * @throws PDIException on failure
	 */
	public void deleteAllBreakpoints() throws PDIException;

	/**
	 * Deletes all breakpoints of specify process
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	public void deleteAllBreakpoints(BitList tasks) throws PDIException;

	/**
	 * Deletes breakpoint
	 * @param tasks
	 * @param breakpoint
	 * @throws PDIException on failure 
	 */
	public void deleteBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException;
	
	/**
	 * Deletes a breakpoint that is different from original task
	 * @param tasks latest tasks
	 * @param breakpoint breakpoint to be deleted
	 * @throws PDIException on failure 
	 */
	public void deleteSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException;

	/**
	 * Disable breakpoint
	 * @param tasks
	 * @param breakpoint
	 * @throws PDIException
	 */
	public void disableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException;

	/**
	 * Enables breakpoint
	 * @param tasks
	 * @param breakpoint
	 * @throws PDIException
	 */
	public void enableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException;
	
	/**
	 * @param id
	 * @return
	 */
	public IPDIBreakpoint getBreakpoint(int id);
	
	/**
	 * Sets address breakpoint
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @param enabled
	 * @return
	 * @throws PDIException
	 */
	public IPDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, IPDIAddressLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException;

	/**
	 * Sets condition
	 * @param tasks
	 * @param breakpoint
	 * @param newCondition
	 * @throws PDIException
	 */
	public void setCondition(BitList tasks, IPDIBreakpoint breakpoint, IPDICondition newCondition) throws PDIException;

	/**
	 * Sets exceptionpoint
	 * @param tasks
	 * @param clazz
	 * @param stopOnThrow
	 * @param stopOnCatch
	 * @param enabled
	 * @return
	 * @throws PDIException
	 */
	public IPDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch, boolean enabled) throws PDIException;
	
	/**
	 * Sets function breakpoint
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @param enabled
	 * @return
	 * @throws PDIException
	 */
	public IPDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, IPDIFunctionLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException;

	/**
	 * Sets line breakpoint
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param deferred
	 * @param enabled
	 * @return
	 * @throws PDIException
	 */
	public IPDILineBreakpoint setLineBreakpoint(BitList tasks, int type, IPDILineLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException;

	/**
	 * Sets watchpoint
	 * @param tasks
	 * @param type
	 * @param watchType
	 * @param expression
	 * @param condition
	 * @param enabled
	 * @return
	 * @throws PDIException
	 */
	public IPDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, IPDICondition condition, boolean enabled) throws PDIException;

	/**
	 * Update any pending breakpoints. This should be called when the debugger enters suspended state
	 * to update any breakpoints that could not be set or deleted because tasks were running.
	 * 
	 * @throws PDIException
	 */
	public void updatePendingBreakpoints() throws PDIException;
}
