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
import org.eclipse.ptp.debug.internal.core.pdi.event.BreakpointInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ChangedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.ConnectedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.CreatedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DataReadMemoryInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.DestroyedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DisconnectedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.EndSteppingRangeInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ErrorEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.ErrorInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ExitInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.LocationReachedInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.MemoryBlockInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ResumedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.SignalInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.StartedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.SuspendedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.ThreadInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.VariableInfo;


public abstract class AbstractEventFactory implements IPDIEventFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDataReadMemoryInfo(java.lang.String, long, long, long, long, long, long, org.eclipse.ptp.debug.core.pdi.model.IPDIMemory[])
	 */
	public Object newDataReadMemoryInfo(String address, long nextRow, long prevRow, long nextPage, long prevPage, long numBytes,
			long totalBytes, IPDIMemory[] memories) {
		return new DataReadMemoryInfo(address, nextRow, prevRow, nextPage, prevPage, numBytes, totalBytes, memories);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newBreakpointInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public IPDIBreakpointInfo newBreakpointInfo(IPDISession session, BitList tasks, IPDIBreakpoint bpt) {
		return new BreakpointInfo(session, tasks, bpt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newChangedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIChangedEvent newChangedEvent(IPDISessionObject reason) {
		return new ChangedEvent(reason);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newConnectedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject, org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIConnectedEvent newConnectedEvent(IPDISessionObject reason, BitList tasks) {
		return new ConnectedEvent(reason, tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newCreatedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDICreatedEvent newCreatedEvent(IPDISessionObject reason) {
		return new CreatedEvent(reason);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDestroyedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIDestroyedEvent newDestroyedEvent(IPDISessionObject reason) {
		return new DestroyedEvent(reason);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDisconnectedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject, org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIDisconnectedEvent newDisconnectedEvent(IPDISessionObject reason, BitList tasks) {
		return new DisconnectedEvent(reason, tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newEndSteppingRangeInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	public IPDIEndSteppingRangeInfo newEndSteppingRangeInfo(IPDISession session, BitList tasks, IPDILocator locator) {
		return new EndSteppingRangeInfo(session, tasks, locator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newErrorEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIErrorEvent newErrorEvent(IPDISessionObject reason) {
		return new ErrorEvent(reason);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newErrorInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, java.lang.String, java.lang.String)
	 */
	public IPDIErrorInfo newErrorInfo(IPDISession session, BitList tasks, int code, String msg, String detailMsg) {
		return new ErrorInfo(session, tasks, code, msg, detailMsg);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newExitInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDIExitInfo newExitInfo(IPDISession session, BitList tasks, int code) {
		return new ExitInfo(session, tasks, code);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newLocationReachedInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	public IPDILocationReachedInfo newLocationReachedInfo(IPDISession session, BitList tasks, IPDILocator locator) {
		return new LocationReachedInfo(session, tasks, locator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newMemoryBlockInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.math.BigInteger[], org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock)
	 */
	public IPDISessionObject newMemoryBlockInfo(IPDISession session, BitList tasks, BigInteger[] bigIntegers, IPDIMemoryBlock block) {
		return new MemoryBlockInfo(session, tasks, bigIntegers, block);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newResumedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject, org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDIResumedEvent newResumedEvent(IPDISessionObject reason, BitList tasks, int type) {
		return new ResumedEvent(reason, tasks, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newSignalInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String, org.eclipse.ptp.debug.core.pdi.model.IPDISignal, org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	public IPDISignalInfo newSignalInfo(IPDISession session, BitList tasks, String name, String desc, IPDISignal signal,
			IPDILocator locator) {
		return new SignalInfo(session, tasks, name, desc, signal, locator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newStartedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject, org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIStartedEvent newStartedEvent(IPDISessionObject reason, BitList tasks) {
		return new StartedEvent(reason, tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newSuspendedEvent(org.eclipse.ptp.debug.core.pdi.IPDISessionObject, java.lang.String[], int, int, int)
	 */
	public IPDISuspendedEvent newSuspendedEvent(IPDISessionObject reason, String[] vars, int thread_id, int level, int depth) {
		return new SuspendedEvent(reason, vars, thread_id, level, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newThreadInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.model.IPDIThread)
	 */
	public IPDISessionObject newThreadInfo(IPDISession session, BitList tasks, int id, IPDIThread thread) {
		return new ThreadInfo(session, tasks, id, thread);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newVariableInfo(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String, org.eclipse.ptp.debug.core.pdi.model.IPDIVariable)
	 */
	public IPDIVariableInfo newVariableInfo(IPDISession session, BitList tasks, String name, IPDIVariable var) {
		return new VariableInfo(session, tasks, name, var);
	}

}
