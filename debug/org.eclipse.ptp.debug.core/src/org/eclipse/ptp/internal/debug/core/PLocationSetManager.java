/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.IPLocationSetManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPLocationSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocatorComparator;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest;
import org.eclipse.ptp.internal.debug.core.event.PDebugInfo;

public class PLocationSetManager implements IPLocationSetManager, IPDIEventListener {
	public static class PDebugLocationInfo extends PDebugInfo {
		public PDebugLocationInfo(IPLaunch launch, TaskSet allTasks, TaskSet allRegTasks, TaskSet allUnregTasks) {
			super(launch, allTasks, allRegTasks, allUnregTasks);
		}
	}

	public static final class PLocationSet implements IPLocationSet {
		public final TaskSet fTasks;
		public final IPDILocator fLocator;

		public PLocationSet(TaskSet tasks, IPDILocator locator) {
			fTasks = tasks;
			fLocator = locator;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.debug.core.model.IPLocationSet#getFile()
		 */
		public String getFile() {
			return fLocator.getFile();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.debug.core.model.IPLocationSet#getFunction()
		 */
		public String getFunction() {
			return fLocator.getFunction();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.debug.core.model.IPLocationSet#getLineNumber()
		 */
		public int getLineNumber() {
			return fLocator.getLineNumber();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.debug.core.model.IPLocationSet#getTasks()
		 */
		public TaskSet getTasks() {
			return fTasks;
		}

	}

	private final IPSession session;
	private final Map<IPDILocator, IPLocationSet> setsByLocation;

	public PLocationSetManager(IPSession session) {
		this.session = session;
		setsByLocation = new TreeMap<IPDILocator, IPLocationSet>(PDILocatorComparator.SINGLETON);
		getPDISession().getEventManager().addEventListener(this);
	}

	public void dispose(IProgressMonitor monitor) {
		getPDISession().getEventManager().removeEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPLocationSetManager#getLocationSets()
	 */
	public IPLocationSet[] getLocationSets() {
		return setsByLocation.values().toArray(new IPLocationSet[0]);
	}

	public IPSession getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (event instanceof IPDISuspendedEvent) {
				handleSuspendedEvent((IPDISuspendedEvent) event);
			} else if (event instanceof IPDIResumedEvent) {
				handleResumedEvent((IPDIResumedEvent) event);
			} else if (event instanceof IPDIDestroyedEvent) {// reason ExitInfo
				handleDestroyedEvent((IPDIDestroyedEvent) event);
			} else if (event instanceof IPDIDisconnectedEvent) {
				handleDisconnectedEvent((IPDIDisconnectedEvent) event);
			}
		}
	}

	private IPDISession getPDISession() {
		return getSession().getPDISession();
	}

	private void handleDestroyedEvent(IPDIDestroyedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIExitInfo)
			setsByLocation.clear();
	}

	private void handleDisconnectedEvent(IPDIDisconnectedEvent event) {
		setsByLocation.clear();
	}

	private void handleResumedEvent(IPDIResumedEvent event) {
		TaskSet tasks = event.getTasks();
		int[] members = tasks.toArray();

		// Remove members
		for (IPLocationSet ls : setsByLocation.values()) {
			ls.getTasks().clear(members);
		}
		// Remove empty sets
		for (Iterator<IPDILocator> iter = setsByLocation.keySet().iterator(); iter.hasNext();) {
			IPDILocator loc = iter.next();
			if (setsByLocation.get(loc).getTasks().cardinality() == 0)
				iter.remove();
		}

		session.fireDebugEvent(IPDebugEvent.CHANGE, IPDebugEvent.CONTENT,
				new PDebugLocationInfo(session.getLaunch(), event.getTasks(), event.getTasks(), event.getTasks()));
	}

	private void handleSuspendedEvent(IPDISuspendedEvent event) {
		IPDISessionObject reason = event.getReason();
		IPDILocator locator = null;
		if (reason instanceof IPDIBreakpointInfo) {
			IPDIBreakpoint bp = ((IPDIBreakpointInfo) reason).getBreakpoint();
			if (bp instanceof IPDILocationBreakpoint) {
				locator = ((IPDILocationBreakpoint) bp).getLocator();
				try {
					/*
					 * The IPDIBreakpointInfo associated with the
					 * IPDISuspendedEvent can only return a single breakpoint,
					 * so in practice separate IPDISuspendEvents are always
					 * generated for simultaneous breakpoints. ie: suspend
					 * events in different locations are never aggregated if
					 * they had a different reason (breakpoint), so it seems
					 * safe to only ask a single member of the associated
					 * TaskSet for the location info.
					 */
					TaskSet tasks = event.getTasks();
					assert !tasks.isEmpty();
					TaskSet onetask = new TaskSet(tasks.taskSize());
					onetask.set(tasks.nextSetBit(0));
					IPDISession session = reason.getSession();
					IPDIListStackFramesRequest request = session.getRequestFactory().getListStackFramesRequest(session, onetask, 0,
							1);
					session.getEventRequestManager().addEventRequest(request);
					IPDIStackFrameDescriptor[] frames = request.getStackFrames(tasks);
					locator = frames[0].getLocator();
				} catch (PDIException e) {
					throw new RuntimeException(e);
				}
			}
		} else if (reason instanceof IPDIEndSteppingRangeInfo)
			locator = ((IPDIEndSteppingRangeInfo) reason).getLocator();
		else if (reason instanceof IPDILocationReachedInfo)
			locator = ((IPDILocationReachedInfo) reason).getLocator();
		else if (reason instanceof IPDISignalInfo)
			locator = ((IPDISignalInfo) reason).getLocator();
		else if (reason instanceof IPDIFunctionFinishedInfo)
			locator = ((IPDIFunctionFinishedInfo) reason).getLocator();
		else if (reason instanceof IPDISharedLibraryInfo)
			;// locator = ((IPDISharedLibraryInfo) reason).getLocator();
		else if (reason instanceof IPDIWatchpointScopeInfo)
			;// locator = ((IPDIWatchpointScopeInfo)
				// reason).getWatchpoint().getLocator();
		else if (reason instanceof IPDIWatchpointTriggerInfo)
			;// locator = ((IPDIWatchpointTriggerInfo) reason).getLocator();

		if (locator != null) {
			TaskSet tasks = event.getTasks();
			int[] members = tasks.toArray();
			// Remove old members
			for (IPLocationSet ls : setsByLocation.values()) {
				ls.getTasks().clear(members);
			}
			// Remove empty sets
			for (Iterator<IPDILocator> iter = setsByLocation.keySet().iterator(); iter.hasNext();) {
				IPDILocator loc = iter.next();
				if (setsByLocation.get(loc).getTasks().cardinality() == 0)
					iter.remove();
			}

			if (setsByLocation.containsKey(locator))
				setsByLocation.get(locator).getTasks().set(members);
			else
				setsByLocation.put(locator, new PLocationSet(tasks, locator));
		}
		session.fireDebugEvent(IPDebugEvent.CHANGE, IPDebugEvent.CONTENT,
				new PDebugLocationInfo(session.getLaunch(), event.getTasks(), event.getTasks(), event.getTasks()));
	}
}