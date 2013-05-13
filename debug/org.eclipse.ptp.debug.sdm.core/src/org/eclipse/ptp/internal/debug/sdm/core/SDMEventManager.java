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
package org.eclipse.ptp.internal.debug.sdm.core;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ptp.internal.debug.core.pdi.manager.AbstractEventManager;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.internal.debug.sdm.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener;
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

public class SDMEventManager extends AbstractEventManager implements IProxyDebugEventListener {
	public SDMEventManager(IPDISession session) {
		super(session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugArgsEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugArgsEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugArgsEvent(IProxyDebugArgsEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugBreakpointHitEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointHitEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugBreakpointHitEvent(IProxyDebugBreakpointHitEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
		if (bpt == null) {
			eventList.add(session.getEventFactory().newErrorEvent(
					session.getEventFactory().newErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, Messages.SDMEventManager_5,
							Messages.SDMEventManager_1)));
		} else {
			eventList.add(createSuspendedEvent(session.getEventFactory().newBreakpointInfo(session, eTasks, bpt), e.getThreadId(),
					0, e.getDepth(), e.getChangedVars()));
		}
		fireEvents(eventList.toArray(new IPDIEvent[0]));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugBreakpointSetEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointSetEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugBreakpointSetEvent(IProxyDebugBreakpointSetEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		if (!(request instanceof IPDIBreakpointRequest) || !((IPDIBreakpointRequest) request).isAllowUpdate()) {
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
		fireEvents(eventList.toArray(new IPDIEvent[0]));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugDataEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugDataEvent(IProxyDebugDataEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugErrorEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugErrorEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugErrorEvent(IProxyDebugErrorEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		if (request != null) {
			request.error(e.getErrorMessage());
		}
		int actionType = session.getDebugger().getErrorAction(e.getErrorCode());
		if (actionType == IPDIErrorInfo.DBG_FATAL) {
			session.getTaskManager().setPendingTasks(false, eTasks);
			session.getTaskManager().setTerminateTasks(true, eTasks);
		}
		fireEvent(session.getEventFactory().newErrorEvent(
				session.getEventFactory()
						.newErrorInfo(session, eTasks, actionType, Messages.SDMEventManager_4, e.getErrorMessage())));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugExitEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugExitEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugExitEvent(IProxyDebugExitEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		IPDIExitInfo reason = session.getEventFactory().newExitInfo(session, eTasks, e.getExitStatus());
		checkTasksExited(eTasks, reason, eventList);
		fireEvents(eventList.toArray(new IPDIEvent[0]));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugInfoThreadsEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugInfoThreadsEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugInfoThreadsEvent(IProxyDebugInfoThreadsEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugMemoryInfoEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugMemoryInfoEvent(IProxyDebugMemoryInfoEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugOKEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugOKEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugOKEvent(IProxyDebugOKEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
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
				eventList.add(session.getEventFactory()
						.newDestroyedEvent(session.getEventFactory().newExitInfo(session, eTasks, 1)));
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
		fireEvents(eventList.toArray(new IPDIEvent[0]));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugOutputEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugOutputEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugOutputEvent(IProxyDebugOutputEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		fireEvent(session.getEventFactory().newOutputEvent(session, eTasks, e.getOutput()));
		/*
		 * IProxyDebugOutputEvent can occur at any time, so make sure that it is
		 * NOT used to update a request. i.e. by not calling verifyEvent
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugSetThreadSelectEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugSetThreadSelectEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugSetThreadSelectEvent(IProxyDebugSetThreadSelectEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugSignalEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugSignalEvent(IProxyDebugSignalEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		ProxyDebugLocator loc = e.getFrame().getLocator();
		IPDILocator locator = PDILocationFactory
				.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
		fireEvent(createSuspendedEvent(
				session.getEventFactory().newSignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, locator),
				e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugSignalExitEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalExitEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugSignalExitEvent(IProxyDebugSignalExitEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		IPDISignalInfo reason = session.getEventFactory().newSignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(),
				null, null);
		checkTasksExited(eTasks, reason, eventList);
		fireEvents(eventList.toArray(new IPDIEvent[0]));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugSignalsEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalsEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugSignalsEvent(IProxyDebugSignalsEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugStackframeEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugStackframeEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugStackframeEvent(IProxyDebugStackframeEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugStackInfoDepthEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugStackInfoDepthEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugStackInfoDepthEvent(IProxyDebugStackInfoDepthEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugStepEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugStepEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugStepEvent(IProxyDebugStepEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		ProxyDebugLocator loc = e.getFrame().getLocator();
		IPDILocator locator = PDILocationFactory
				.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
		fireEvent(createSuspendedEvent(session.getEventFactory().newEndSteppingRangeInfo(session, eTasks, locator),
				e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugSuspendEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugSuspendEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugSuspendEvent(IProxyDebugSuspendEvent e) {
		TaskSet eTasks = ProxyDebugClient.decodeTaskSet(e.getBitSet());
		IPDIEventRequest request = getCurrentRequest();
		ProxyDebugLocator loc = e.getFrame().getLocator();
		IPDILocator locator = PDILocationFactory
				.newLocator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
		fireEvent(createSuspendedEvent(session.getEventFactory().newLocationReachedInfo(session, eTasks, locator), e.getThreadId(),
				e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
		verifyEvent(request, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugTypeEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugTypeEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugTypeEvent(IProxyDebugTypeEvent e) {
		verifyEvent(getCurrentRequest(), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener#
	 * handleProxyDebugVarsEvent
	 * (org.eclipse.ptp.proxy.debug.event.IProxyDebugVarsEvent)
	 */
	/**
	 * @since 5.0
	 */
	public void handleProxyDebugVarsEvent(IProxyDebugVarsEvent e) {
		verifyEvent(getCurrentRequest(), e);
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
	 * Helper method to create a suspended event.
	 * 
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
	 * Check if the request is completed and notify listeners if it is.
	 * 
	 * @param request
	 *            event request
	 * @param result
	 *            result of request
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
