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

import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
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
import org.eclipse.ptp.debug.core.pdi.request.IPDIGoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetCurrentStackFrameRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStartDebuggerRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepFinishRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepIntoInstructionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepIntoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepOverInstructionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepOverRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDITerminateRequest;
import org.eclipse.ptp.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.debug.sdm.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugExitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugOKEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugOutputEvent;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, final Object arg) {
		PDebugUtils.println("Msg: SDMEventManager - update(): Event: " + ((IProxyDebugEvent) arg).toString()); //$NON-NLS-1$
		IProxyDebugEvent event = (IProxyDebugEvent) arg;
		IPDIEventRequest request = getCurrentRequest();
		fireEvent(request, event);
		/*
		 * IProxyDebugOutputEvent can occur at any time, so make sure that it is
		 * not used to update a request.
		 */
		if (!(event instanceof IProxyDebugOutputEvent)) {
			verifyEvent(request, event);
		}
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
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(event.getBitSet());

		PDebugUtils.println("Msg: SDMEventManager - fireEvent(): event " + event); //$NON-NLS-1$

		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean add(IPDIEvent e) {
				PDebugUtils.println("Msg: SDMEventManager - fireEvent(): added PDIEvent: " + e); //$NON-NLS-1$
				return super.add(e);
			}
		};
		if (event instanceof IProxyDebugOKEvent) {
			if (request != null) {
				if (request instanceof IPDIGoRequest) {
					session.getTaskManager().setSuspendTasks(false, eTasks);
					session.processRunningEvent(eTasks.copy());
					eventList.add(session.getEventFactory().newResumedEvent(session, eTasks, IPDIResumedEvent.CONTINUE));
				} else if (request instanceof IPDIStepRequest) {
					int details;
					if (request instanceof IPDIStepIntoRequest)
						details = IPDIResumedEvent.STEP_INTO;
					else if (request instanceof IPDIStepOverRequest)
						details = IPDIResumedEvent.STEP_OVER;
					else if (request instanceof IPDIStepFinishRequest)
						details = IPDIResumedEvent.STEP_RETURN;
					else if (request instanceof IPDIStepIntoInstructionRequest)
						details = IPDIResumedEvent.STEP_INTO_INSTRUCTION;
					else if (request instanceof IPDIStepOverInstructionRequest)
						details = IPDIResumedEvent.STEP_OVER_INSTRUCTION;
					else {
						details = IPDIResumedEvent.CONTINUE;
					}
					session.getTaskManager().setSuspendTasks(false, eTasks);
					session.processRunningEvent(eTasks.copy());
					eventList.add(session.getEventFactory().newResumedEvent(session, eTasks, details));
				} else if (request instanceof IPDIEnableBreakpointRequest) {
				} else if (request instanceof IPDIDisableBreakpointRequest) {
				} else if (request instanceof IPDIConditionBreakpointRequest) {
				} else if (request instanceof IPDIDeleteBreakpointRequest) {
					if (((IPDIBreakpointRequest) request).isAllowUpdate()) {
						IPDIBreakpoint bpt = ((IPDIDeleteBreakpointRequest) request).getBreakpoint();
						if (bpt == null) {
							eventList.add(session.getEventFactory().newErrorEvent(
									session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL,
											Messages.SDMEventManager_0, Messages.SDMEventManager_1)));
						} else {
							eventList.add(session.getEventFactory().newDestroyedEvent(
									session.getEventFactory().newBreakpointInfo(session, eTasks, bpt)));
						}
					}
				} else if (request instanceof IPDITerminateRequest) {
					session.getTaskManager().setPendingTasks(false, eTasks);
					session.getTaskManager().setTerminateTasks(true, eTasks);
					eventList.add(session.getEventFactory().newDestroyedEvent(
							session.getEventFactory().newExitInfo(session, eTasks, 1)));
					if (session.getTaskManager().isAllTerminated(session.getTasks())) {
						eventList.add(session.getEventFactory().newDisconnectedEvent(session, session.getTasks()));
					}
				} else if (request instanceof IPDIStartDebuggerRequest) {
					eventList.add(session.getEventFactory().newConnectedEvent(session, eTasks));
				} else if (request instanceof IPDISetCurrentStackFrameRequest) {
				} else if (request instanceof IPDIDeleteVariableRequest) {
				} else if (request instanceof IPDICommandRequest) {
				} else {
				}
			}
		} else if (event instanceof IProxyDebugBreakpointSetEvent) {
			if (!(request instanceof IPDIBreakpointRequest) || !((IPDIBreakpointRequest) request).isAllowUpdate()) {
				IProxyDebugBreakpointSetEvent e = (IProxyDebugBreakpointSetEvent) event;
				IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
				if (bpt == null) {
					eventList.add(session.getEventFactory().newErrorEvent(
							session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL,
									Messages.SDMEventManager_2, Messages.SDMEventManager_1)));
				} else {
					eventList.add(session.getEventFactory().newCreatedEvent(
							session.getEventFactory().newBreakpointInfo(session, eTasks, bpt)));
				}
			}
		} else if (event instanceof IProxyDebugExitEvent) {
			IProxyDebugExitEvent e = (IProxyDebugExitEvent) event;
			IPDIExitInfo reason = session.getEventFactory().newExitInfo(session, eTasks, e.getExitStatus());
			checkTasksExited(eTasks, reason, eventList);
		} else if (event instanceof IProxyDebugErrorEvent) {
			IProxyDebugErrorEvent e = (IProxyDebugErrorEvent) event;
			if (request != null) {
				request.error(e.getErrorMessage());
			}
			int actionType = session.getDebugger().getErrorAction(e.getErrorCode());
			if (actionType == IPDIErrorInfo.DBG_FATAL) {
				session.getTaskManager().setPendingTasks(false, eTasks);
				session.getTaskManager().setTerminateTasks(true, eTasks);
			}
			eventList.add(session.getEventFactory().newErrorEvent(
					session.getEventFactory().newErrorInfo(session, eTasks, actionType, Messages.SDMEventManager_4,
							e.getErrorMessage())));
		} else if (event instanceof IProxyDebugOutputEvent) {
			IProxyDebugOutputEvent e = (IProxyDebugOutputEvent) event;
			eventList.add(session.getEventFactory().newOutputEvent(session, eTasks, e.getOutput()));
		} else if (event instanceof IProxyDebugMemoryInfoEvent) {
		} else if (event instanceof IProxyDebugSignalExitEvent) {
			IProxyDebugSignalExitEvent e = (IProxyDebugSignalExitEvent) event;
			IPDISignalInfo reason = session.getEventFactory().newSignalInfo(session, eTasks, e.getSignalName(),
					e.getSignalMeaning(), null, null);
			checkTasksExited(eTasks, reason, eventList);
		} else if (event instanceof IProxyDebugBreakpointHitEvent) {
			IProxyDebugBreakpointHitEvent e = (IProxyDebugBreakpointHitEvent) event;
			IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
			if (bpt == null) {
				eventList.add(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL,
								Messages.SDMEventManager_5, Messages.SDMEventManager_1)));
			} else {
				eventList.add(createSuspendedEvent(session.getEventFactory().newBreakpointInfo(session, eTasks, bpt),
						e.getThreadId(), 0, e.getDepth(), e.getChangedVars()));
			}
		} else if (event instanceof IProxyDebugSignalEvent) {
			IProxyDebugSignalEvent e = (IProxyDebugSignalEvent) event;
			ProxyDebugLocator loc = e.getFrame().getLocator();
			IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(),
					loc.getAddress());
			eventList.add(createSuspendedEvent(
					session.getEventFactory()
							.newSignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, locator), e
							.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		} else if (event instanceof IProxyDebugStepEvent) {
			IProxyDebugStepEvent e = (IProxyDebugStepEvent) event;
			ProxyDebugLocator loc = e.getFrame().getLocator();
			IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(),
					loc.getAddress());
			eventList.add(createSuspendedEvent(session.getEventFactory().newEndSteppingRangeInfo(session, eTasks, locator),
					e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		} else if (event instanceof IProxyDebugSuspendEvent) {
			IProxyDebugSuspendEvent e = (IProxyDebugSuspendEvent) event;
			ProxyDebugLocator loc = e.getFrame().getLocator();
			IPDILocator locator = PDILocationFactory.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(),
					loc.getAddress());
			eventList.add(createSuspendedEvent(session.getEventFactory().newLocationReachedInfo(session, eTasks, locator),
					e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		} else if (event instanceof IProxyDebugTypeEvent) {
		} else if (event instanceof IProxyDebugVarsEvent) {
		} else if (event instanceof IProxyDebugDataEvent) { // bypass
		} else if (event instanceof IProxyDebugArgsEvent) { // bypass
		} else if (event instanceof IProxyDebugInfoThreadsEvent) { // bypass
		} else if (event instanceof IProxyDebugSetThreadSelectEvent) { // bypass
		} else if (event instanceof IProxyDebugSignalsEvent) { // bypass
		} else if (event instanceof IProxyDebugStackframeEvent) { // bypass
		} else if (event instanceof IProxyDebugStackInfoDepthEvent) { // bypass
		} else {
		}
		fireEvents(eventList.toArray(new IPDIEvent[0]));
	}

	/**
	 * Handle an event generated when debugged tasks have exited. If all tasks
	 * have exited, the debugger will terminate.
	 * 
	 * @param tasks
	 *            task set of processes that received the event
	 * @param reason
	 *            reason the tasks exited
	 * @param eventList
	 *            events to be propagated to client debugger
	 */
	private void checkTasksExited(TaskSet tasks, IPDISessionObject reason, List<IPDIEvent> eventList) {
		/*
		 * Set the tasks to terminated state
		 */
		session.getTaskManager().setPendingTasks(false, tasks);
		session.getTaskManager().setTerminateTasks(true, tasks);

		/*
		 * Generate a destroy event to notify the cause of the tasks exiting
		 */
		eventList.add(session.getEventFactory().newDestroyedEvent(reason));

		/*
		 * If all tasks have exited, terminate the debug session.
		 */
		if (session.getTaskManager().isAllTerminated(session.getTasks())) {
			try {
				session.exit();
				eventList.add(session.getEventFactory().newDisconnectedEvent(session, session.getTasks()));
			} catch (PDIException ex) {
				eventList.add(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, ex.getTasks(), IPDIErrorInfo.DBG_FATAL,
								Messages.SDMEventManager_3, ex.getMessage())));
			}
		}
	}

	/**
	 * @param request
	 * @param result
	 */
	private void verifyEvent(IPDIEventRequest request, IProxyDebugEvent result) {
		if (request != null) {
			TaskSet eTasks = ProxyDebugClient.decodeTaskSet(result.getBitSet());
			if (request.completed(eTasks, result)) {
				request.done();
				notifyEventRequest(request);
			}
		}
	}
}
