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
package org.eclipse.ptp.debug.sdm.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.AbstractEventManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.request.IPDIBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDICommandRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIConditionBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDeleteBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDeleteVariableRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDisableBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEnableBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetCurrentStackFrameRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStartDebuggerRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDITerminateRequest;
import org.eclipse.ptp.debug.sdm.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataExpValueEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugExitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugOKEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugPartialAIFEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalExitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStackframeEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStepEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSuspendEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugTypeEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugVarsEvent;

public class SDMEventManager extends AbstractEventManager {
	public SDMEventManager(IPDISession session) {
		super(session);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, final Object arg) {
		PDebugUtils.println("Msg: SDMEventManager - update(): Event: " + ((IProxyDebugEvent)arg).toString());
		IProxyDebugEvent event = (IProxyDebugEvent) arg;
		IPDIEventRequest request = getCurrentRequest();
		fireEvent(request, event);
		verifyEvent(request, event);
	}
	
	/**
	 * @param reason
	 * @param thread_id
	 * @param level
	 * @param depth
	 * @param vars
	 * @return
	 */
	private IPDIEvent createSuspendedEvent(IPDISessionObject reason, int thread_id, int level, int depth, String[] vars) {
		if (depth > 1) {
			session.getTaskManager().setCanStepReturnTasks(true, reason.getTasks());
		}
		session.getTaskManager().setSuspendTasks(true, reason.getTasks());
		session.processSupsendedEvent(reason.getTasks().copy(), thread_id, vars);
		return session.getEventFactory().newSuspendedEvent(reason, vars, thread_id, level, depth);
	}
	
	/**
	 * @param request
	 * @param event
	 */
	private synchronized void fireEvent(IPDIEventRequest request, IProxyDebugEvent event) {
		BitList eTasks = ProxyDebugClient.decodeBitSet(event.getBitSet());
		
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>() {
			private static final long serialVersionUID = 1L;
			public boolean add(IPDIEvent e) {
				PDebugUtils.println("Msg: SDMEventManager - fireEvent(): added PDIEvent: " + e);
				return super.add(e);
			}
		};
		if (event instanceof IProxyDebugOKEvent) {
			if (request != null) {
				if (request instanceof IPDIEnableBreakpointRequest) {
				}
				else if (request instanceof IPDIDisableBreakpointRequest) {
				}
				else if (request instanceof IPDIConditionBreakpointRequest) {
				}
				else if (request instanceof IPDIDeleteBreakpointRequest) {
					if (((IPDIBreakpointRequest)request).isAllowUpdate()) {
						IPDIBreakpoint bpt = ((IPDIDeleteBreakpointRequest)request).getBreakpoint();
						if (bpt == null) {
							eventList.add(session.getEventFactory().newErrorEvent(
									session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Delete Breakpoint Error", "No PDI breakpoint found")));
						}
						else {
							eventList.add(session.getEventFactory().newDestroyedEvent(
									session.getEventFactory().newBreakpointInfo(session, eTasks, bpt)));
						}
					}
				}
				else if (request instanceof IPDITerminateRequest) {
					session.getTaskManager().setPendingTasks(false, eTasks);
					session.getTaskManager().setTerminateTasks(true, eTasks);
					eventList.add(session.getEventFactory().newDestroyedEvent(
							session.getEventFactory().newExitInfo(session, eTasks, 1)));
					if (session.getTaskManager().isAllTerminated(session.getTasks())) {
						eventList.add(session.getEventFactory().newDisconnectedEvent(session, session.getTasks()));
					}
				}
				else if (request instanceof IPDIStartDebuggerRequest) {
					eventList.add(session.getEventFactory().newConnectedEvent(session, eTasks));
				}
				else if (request instanceof IPDISetCurrentStackFrameRequest) {
				}
				else if (request instanceof IPDIDeleteVariableRequest) {
				}
				else if (request instanceof IPDICommandRequest) {
				}
				else {
				}
			}
		}
		else if (event instanceof IProxyDebugBreakpointSetEvent) {
			if (!(request instanceof IPDIBreakpointRequest) || !((IPDIBreakpointRequest)request).isAllowUpdate())  {
				IProxyDebugBreakpointSetEvent e = (IProxyDebugBreakpointSetEvent)event;
				IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
				if (bpt == null) {
					eventList.add(session.getEventFactory().newErrorEvent(
							session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Set Breakpoint Error", "No PDI breakpoint found")));
				}
				else {
					eventList.add(session.getEventFactory().newCreatedEvent(
							session.getEventFactory().newBreakpointInfo(session, eTasks, bpt)));
				}
			}
		}
		else if (event instanceof IProxyDebugExitEvent) {
			IProxyDebugExitEvent e = (IProxyDebugExitEvent)event;
			session.getTaskManager().setPendingTasks(false, eTasks);
			session.getTaskManager().setTerminateTasks(true, eTasks);
			eventList.add(session.getEventFactory().newDestroyedEvent(
					session.getEventFactory().newExitInfo(session, eTasks, e.getExitStatus())));
			if (session.getTaskManager().isAllTerminated(session.getTasks())) {
				try {
					session.exit();
					eventList.add(session.getEventFactory().newDisconnectedEvent(session, session.getTasks()));
				}
				catch (PDIException ex) {
					eventList.add(session.getEventFactory().newErrorEvent(
							session.getEventFactory().newErrorInfo(session, ex.getTasks(), IPDIErrorInfo.DBG_FATAL, "Shutdown debugger error", ex.getMessage())));
				}
			}
		}
		else if (event instanceof IProxyDebugErrorEvent) {
			IProxyDebugErrorEvent e = (IProxyDebugErrorEvent)event;
			if (request != null) {
				request.error(e.getErrorMessage());
			}
			int actionType = session.getDebugger().getErrorAction(e.getErrorCode());
			if (actionType == IPDIErrorInfo.DBG_FATAL) {
				session.getTaskManager().setPendingTasks(false, eTasks);
				session.getTaskManager().setTerminateTasks(true, eTasks);
			}
			eventList.add(session.getEventFactory().newErrorEvent(
					session.getEventFactory().newErrorInfo(session, eTasks, actionType, "Internal Error", e.getErrorMessage())));
		}
		else if (event instanceof IProxyDebugDataExpValueEvent) {
			
		}
		else if (event instanceof IProxyDebugInitEvent) {
			
		}
		else if (event instanceof IProxyDebugMemoryInfoEvent) {
			
		}
		else if (event instanceof IProxyDebugPartialAIFEvent) {
			
		}
		else if (event instanceof IProxyDebugSignalExitEvent) {
			IProxyDebugSignalExitEvent e = (IProxyDebugSignalExitEvent)event;
			session.getTaskManager().setPendingTasks(false, eTasks);
			session.getTaskManager().setTerminateTasks(true, eTasks);
			eventList.add(session.getEventFactory().newDestroyedEvent(
					session.getEventFactory().newSignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, null)));
			if (session.getTaskManager().isAllTerminated(session.getTasks())) {
				try {
					session.exit();
					eventList.add(session.getEventFactory().newDisconnectedEvent(session, session.getTasks()));
				}
				catch (PDIException ex) {
					eventList.add(session.getEventFactory().newErrorEvent(
							session.getEventFactory().newErrorInfo(session, ex.getTasks(), IPDIErrorInfo.DBG_FATAL, "Shutdown debugger error", ex.getMessage())));
				}
			}
		}
		else if (event instanceof IProxyDebugBreakpointHitEvent) {
			IProxyDebugBreakpointHitEvent e = (IProxyDebugBreakpointHitEvent)event;
			IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
			if (bpt == null) {
				eventList.add(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Hit Breakpoint Error", "No PDI breakpoint found")));
			}
			else {
				eventList.add(createSuspendedEvent(session.getEventFactory().newBreakpointInfo(session, eTasks, bpt), e.getThreadId(), 0, e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugSignalEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugSignalEvent e = (IProxyDebugSignalEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(session.getEventFactory().newSignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugStepEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugStepEvent e = (IProxyDebugStepEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(session.getEventFactory().newEndSteppingRangeInfo(session, eTasks,  locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugSuspendEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugSuspendEvent e = (IProxyDebugSuspendEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(session.getEventFactory().newLocationReachedInfo(session, eTasks, locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugTypeEvent) {
		}
		else if (event instanceof IProxyDebugVarsEvent) {
		}
		else if (event instanceof IProxyDebugDataEvent) { //bypass
		}
		else if (event instanceof IProxyDebugArgsEvent) { //bypass
		}
		else if (event instanceof IProxyDebugInfoThreadsEvent) { //bypass
		}
		else if (event instanceof IProxyDebugSetThreadSelectEvent) { //bypass
		}
		else if (event instanceof IProxyDebugSignalsEvent) { //bypass
		}
		else if (event instanceof IProxyDebugStackframeEvent) { //bypass
		}
		else if (event instanceof IProxyDebugStackInfoDepthEvent) { //bypass
		}
		else {
		}
		fireEvents(eventList.toArray(new IPDIEvent[0]));
	}
	
	/**
	 * @param request
	 * @param result
	 */
	private void verifyEvent(IPDIEventRequest request, IProxyDebugEvent result) {
		if (request != null) {
			BitList eTasks = ProxyDebugClient.decodeBitSet(result.getBitSet());
			if (request.completed(eTasks, result)) {
				request.done();
				notifyEventRequest(request);
			}
		}
	}
}
