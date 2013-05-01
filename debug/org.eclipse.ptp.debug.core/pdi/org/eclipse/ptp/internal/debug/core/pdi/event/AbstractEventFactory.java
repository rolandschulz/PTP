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
package org.eclipse.ptp.internal.debug.core.pdi.event;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIConnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDICreatedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIOutputEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIStartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;

public abstract class AbstractEventFactory implements IPDIEventFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDataReadMemoryInfo
	 * (java.lang.String, long, long, long, long, long, long,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIMemory[])
	 */
	public Object newDataReadMemoryInfo(String address, long nextRow, long prevRow, long nextPage, long prevPage, long numBytes,
			long totalBytes, IPDIMemory[] memories) {
		return new DataReadMemoryInfo(address, nextRow, prevRow, nextPage, prevPage, numBytes, totalBytes, memories);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newBreakpointInfo
	 * (org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIBreakpointInfo newBreakpointInfo(IPDISession session, TaskSet tasks, IPDIBreakpoint bpt) {
		return new BreakpointInfo(session, tasks, bpt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newChangedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIChangedEvent newChangedEvent(IPDISessionObject reason) {
		return new ChangedEvent(reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newConnectedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject,
	 * org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIConnectedEvent newConnectedEvent(IPDISessionObject reason, TaskSet tasks) {
		return new ConnectedEvent(reason, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newCreatedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDICreatedEvent newCreatedEvent(IPDISessionObject reason) {
		return new CreatedEvent(reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDestroyedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIDestroyedEvent newDestroyedEvent(IPDISessionObject reason) {
		return new DestroyedEvent(reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newDisconnectedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject,
	 * org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDisconnectedEvent newDisconnectedEvent(IPDISessionObject reason, TaskSet tasks) {
		return new DisconnectedEvent(reason, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newEndSteppingRangeInfo
	 * (org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIEndSteppingRangeInfo newEndSteppingRangeInfo(IPDISession session, TaskSet tasks, IPDILocator locator) {
		return new EndSteppingRangeInfo(session, tasks, locator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newErrorEvent(org
	 * .eclipse.ptp.debug.core.pdi.IPDISessionObject)
	 */
	public IPDIErrorEvent newErrorEvent(IPDISessionObject reason) {
		return new ErrorEvent(reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newErrorInfo(org
	 * .eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, int, java.lang.String,
	 * java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIErrorInfo newErrorInfo(IPDISession session, TaskSet tasks, int code, String msg, String detailMsg) {
		return new ErrorInfo(session, tasks, code, msg, detailMsg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newExitInfo(org
	 * .eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIExitInfo newExitInfo(IPDISession session, TaskSet tasks, int code) {
		return new ExitInfo(session, tasks, code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newLocationReachedInfo
	 * (org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	/**
	 * @since 4.0
	 */
	public IPDILocationReachedInfo newLocationReachedInfo(IPDISession session, TaskSet tasks, IPDILocator locator) {
		return new LocationReachedInfo(session, tasks, locator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newMemoryBlockInfo
	 * (org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, java.math.BigInteger[],
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISessionObject newMemoryBlockInfo(IPDISession session, TaskSet tasks, BigInteger[] bigIntegers, IPDIMemoryBlock block) {
		return new MemoryBlockInfo(session, tasks, bigIntegers, block);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newOutputEvent(
	 * org.eclipse.ptp.debug.core.pdi.IPDISessionObject,
	 * org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIOutputEvent newOutputEvent(IPDISessionObject reason, TaskSet tasks, String output) {
		return new OutputEvent(reason, tasks, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newResumedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject,
	 * org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIResumedEvent newResumedEvent(IPDISessionObject reason, TaskSet tasks, int type) {
		return new ResumedEvent(reason, tasks, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newSignalInfo(org
	 * .eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, java.lang.String, java.lang.String,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDISignal,
	 * org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISignalInfo newSignalInfo(IPDISession session, TaskSet tasks, String name, String desc, IPDISignal signal,
			IPDILocator locator) {
		return new SignalInfo(session, tasks, name, desc, signal, locator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newStartedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject,
	 * org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStartedEvent newStartedEvent(IPDISessionObject reason, TaskSet tasks) {
		return new StartedEvent(reason, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newSuspendedEvent
	 * (org.eclipse.ptp.debug.core.pdi.IPDISessionObject, java.lang.String[],
	 * int, int, int)
	 */
	public IPDISuspendedEvent newSuspendedEvent(IPDISessionObject reason, String[] vars, int thread_id, int level, int depth) {
		return new SuspendedEvent(reason, vars, thread_id, level, depth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newThreadInfo(org
	 * .eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, int,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIThread)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISessionObject newThreadInfo(IPDISession session, TaskSet tasks, int id, IPDIThread thread) {
		return new ThreadInfo(session, tasks, id, thread);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory#newVariableInfo
	 * (org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, java.lang.String,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariable)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIVariableInfo newVariableInfo(IPDISession session, TaskSet tasks, String name, IPDIVariable var) {
		return new VariableInfo(session, tasks, name, var);
	}

}
