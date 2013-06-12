/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.event;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;

/**
 * Factory for creating debugger events
 * 
 */
public interface IPDIEventFactory {
	/**
	 * Create breakpoint info
	 * 
	 * @param session
	 * @param tasks
	 * @param bpt
	 * @return
	 * @since 4.0
	 */
	public IPDIBreakpointInfo newBreakpointInfo(IPDISession session, TaskSet tasks, IPDIBreakpoint bpt);

	/**
	 * Create a changed event
	 * 
	 * @param reason
	 * @return
	 */
	public IPDIChangedEvent newChangedEvent(IPDISessionObject reason);

	/**
	 * Create a connected event
	 * 
	 * @param reason
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIConnectedEvent newConnectedEvent(IPDISessionObject reason, TaskSet tasks);

	/**
	 * @param reason
	 * @return
	 */
	public IPDICreatedEvent newCreatedEvent(IPDISessionObject reason);

	/**
	 * Create data read memory
	 * 
	 * @param address
	 * @param nextRow
	 * @param prevRow
	 * @param nextPage
	 * @param prevPage
	 * @param numBytes
	 * @param totalBytes
	 * @param memories
	 * @return
	 */
	public Object newDataReadMemoryInfo(String address, long nextRow, long prevRow, long nextPage, long prevPage, long numBytes,
			long totalBytes, IPDIMemory[] memories);

	/**
	 * Create destroyed event
	 * 
	 * @param reason
	 * @return
	 */
	public IPDIDestroyedEvent newDestroyedEvent(IPDISessionObject reason);

	/**
	 * Create disconnected event
	 * 
	 * @param reason
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIDisconnectedEvent newDisconnectedEvent(IPDISessionObject reason, TaskSet tasks);

	/**
	 * Create end stepping range info
	 * 
	 * @param reason
	 * @param tasks
	 * @param locator
	 * @return
	 * @since 4.0
	 */
	public IPDIEndSteppingRangeInfo newEndSteppingRangeInfo(IPDISession session, TaskSet tasks, IPDILocator locator);

	/**
	 * Create error event
	 * 
	 * @param reason
	 * @return
	 */
	public IPDIErrorEvent newErrorEvent(IPDISessionObject reason);

	/**
	 * Create error info
	 * 
	 * @param session
	 * @param tasks
	 * @param code
	 * @param msg
	 * @param detailMsg
	 * @return
	 * @since 4.0
	 */
	public IPDIErrorInfo newErrorInfo(IPDISession session, TaskSet tasks, int code, String msg, String detailMsg);

	/**
	 * Create exit info
	 * 
	 * @param reason
	 * @param tasks
	 * @param code
	 * @return
	 * @since 4.0
	 */
	public IPDIExitInfo newExitInfo(IPDISession session, TaskSet tasks, int code);

	/**
	 * Create location reached info
	 * 
	 * @param session
	 * @param tasks
	 * @param locator
	 * @return
	 * @since 4.0
	 */
	public IPDILocationReachedInfo newLocationReachedInfo(IPDISession session, TaskSet tasks, IPDILocator locator);

	/**
	 * Create memory block info
	 * 
	 * @param session
	 * @param tasks
	 * @param bigIntegers
	 * @param block
	 * @return
	 * @since 4.0
	 */
	public IPDISessionObject newMemoryBlockInfo(IPDISession session, TaskSet tasks, BigInteger[] bigIntegers, IPDIMemoryBlock block);

	/**
	 * Create output event
	 * 
	 * @param session
	 * @param tasks
	 * @param output
	 * @return
	 * @since 4.0
	 */
	public IPDIOutputEvent newOutputEvent(IPDISessionObject reason, TaskSet tasks, String output);

	/**
	 * Create resumed event
	 * 
	 * @param session
	 * @param tasks
	 * @param type
	 * @return
	 * @since 4.0
	 */
	public IPDIResumedEvent newResumedEvent(IPDISessionObject reason, TaskSet tasks, int type);

	/**
	 * Create signal info
	 * 
	 * @param reason
	 * @param tasks
	 * @param name
	 * @param desc
	 * @param signal
	 * @param locator
	 * @return
	 * @since 4.0
	 */
	public IPDISignalInfo newSignalInfo(IPDISession session, TaskSet tasks, String name, String desc, IPDISignal signal,
			IPDILocator locator);

	/**
	 * Create started event
	 * 
	 * @param session
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIStartedEvent newStartedEvent(IPDISessionObject reason, TaskSet tasks);

	/**
	 * Create suspend event
	 * 
	 * @param reason
	 * @param vars
	 * @param thread_id
	 * @param level
	 * @param depth
	 * @return
	 */
	public IPDISuspendedEvent newSuspendedEvent(IPDISessionObject reason, String[] vars, int thread_id, int level, int depth);

	/**
	 * Create thread info
	 * 
	 * @param session
	 * @param tasks
	 * @param id
	 * @param thread
	 * @return
	 * @since 4.0
	 */
	public IPDISessionObject newThreadInfo(IPDISession session, TaskSet tasks, int id, IPDIThread thread);

	/**
	 * Create variable info
	 * 
	 * @param session
	 * @param tasks
	 * @param name
	 * @param var
	 * @since 4.0
	 */
	public IPDIVariableInfo newVariableInfo(IPDISession session, TaskSet tasks, String name, IPDIVariable var);
}
