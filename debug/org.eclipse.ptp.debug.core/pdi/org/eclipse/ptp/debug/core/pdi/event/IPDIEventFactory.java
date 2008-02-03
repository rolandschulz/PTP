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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;


public interface IPDIEventFactory {
	/**
	 * @param session
	 * @param tasks
	 * @param bpt
	 * @return
	 */
	public IPDIBreakpointInfo newBreakpointInfo(IPDISession session, BitList tasks, IPDIBreakpoint bpt);
	
	/**
	 * @param reason
	 * @return
	 */
	public IPDIChangedEvent newChangedEvent(IPDISessionObject reason);
	
	/**
	 * @param reason
	 * @param tasks
	 * @return
	 */
	public IPDIConnectedEvent newConnectedEvent(IPDISessionObject reason, BitList tasks);
	
	/**
	 * @param reason
	 * @return
	 */
	public IPDICreatedEvent newCreatedEvent(IPDISessionObject reason);
	
	/**
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
	 * @param reason
	 * @return
	 */
	public IPDIDestroyedEvent newDestroyedEvent(IPDISessionObject reason);
	
	/**
	 * @param reason
	 * @param tasks
	 * @return
	 */
	public IPDIDisconnectedEvent newDisconnectedEvent(IPDISessionObject reason, BitList tasks);
	
	/**
	 * @param reason
	 * @param tasks
	 * @param locator
	 * @return
	 */
	public IPDIEndSteppingRangeInfo newEndSteppingRangeInfo(IPDISession session, BitList tasks, IPDILocator locator);
	
	/**
	 * @param reason
	 * @return
	 */
	public IPDIErrorEvent newErrorEvent(IPDISessionObject reason);
	
	/**
	 * @param session
	 * @param tasks
	 * @param code
	 * @param msg
	 * @param detailMsg
	 * @return
	 */
	public IPDIErrorInfo newErrorInfo(IPDISession session, BitList tasks, int code, String msg, String detailMsg);
	
	/**
	 * @param reason
	 * @param tasks
	 * @param code
	 * @return
	 */
	public IPDIExitInfo newExitInfo(IPDISession session, BitList tasks, int code);
	
	/**
	 * @param session
	 * @param tasks
	 * @param locator
	 * @return
	 */
	public IPDILocationReachedInfo newLocationReachedInfo(IPDISession session, BitList tasks, IPDILocator locator);
	
	/**
	 * @param session
	 * @param tasks
	 * @param bigIntegers
	 * @param block
	 * @return
	 */
	public IPDISessionObject newMemoryBlockInfo(IPDISession session, BitList tasks, BigInteger[] bigIntegers, IPDIMemoryBlock block);

	/**
	 * @param session
	 * @param tasks
	 * @param type
	 * @return
	 */
	public IPDIResumedEvent newResumedEvent(IPDISessionObject reason, BitList tasks, int type);

	/**
	 * @param reason
	 * @param tasks
	 * @param name
	 * @param desc
	 * @param signal
	 * @param locator
	 * @return
	 */
	public IPDISignalInfo newSignalInfo(IPDISession session, BitList tasks, String name, String desc, IPDISignal signal, IPDILocator locator);

	/**
	 * @param session
	 * @param tasks
	 * @return
	 */
	public IPDIStartedEvent newStartedEvent(IPDISessionObject reason, BitList tasks);
	
	/**
	 * @param reason
	 * @param vars
	 * @param thread_id
	 * @param level
	 * @param depth
	 * @return
	 */
	public IPDISuspendedEvent newSuspendedEvent(IPDISessionObject reason, String[] vars, int thread_id, int level, int depth);

	/**
	 * @param session
	 * @param tasks
	 * @param id
	 * @param thread
	 * @return
	 */
	public IPDISessionObject newThreadInfo(IPDISession session, BitList tasks, int id, IPDIThread thread);

	/**
	 * @param session
	 * @param tasks
	 * @param name
	 * @param var
	 */
	public IPDIVariableInfo newVariableInfo(IPDISession session, BitList tasks, String name, IPDIVariable var);
}
