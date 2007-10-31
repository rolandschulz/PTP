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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIEventManager;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
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
import org.eclipse.ptp.debug.core.pdi.request.IPDIStopDebuggerRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDITerminateRequest;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.internal.core.pdi.event.BreakpointInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ConnectedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.CreatedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DestroyedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DisconnectedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.EndSteppingRangeInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ErrorEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.ErrorInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ExitInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.LocationReachedInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.ResumedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.SignalInfo;
import org.eclipse.ptp.debug.internal.core.pdi.event.SuspendedEvent;
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

public class EventManager extends SessionObject implements IPDIEventManager, Observer {
	private List<IPDIEventListener> listenerList = Collections.synchronizedList(new ArrayList<IPDIEventListener>());
	private List<EventRequestScheduledTask> requestList = Collections.synchronizedList(new ArrayList<EventRequestScheduledTask>());
	private RequestNotifier requestNotifier = new RequestNotifier();

	public EventManager(Session session) {
		super(session, null);
		requestNotifier.addObserver(session.getEventRequestManager());
	}
	public void shutdown() {
		requestNotifier.deleteObserver(session.getEventRequestManager());
		listenerList.clear();
		requestList.clear();
	}
	public void addEventListener(IPDIEventListener listener) {
		if (!listenerList.contains(listener))
			listenerList.add(listener);
	}
	public void removeEventListener(IPDIEventListener listener) {
		listenerList.remove(listener);
	}
	public void removeEventListeners() {
		listenerList.clear();
	}
	private IPDIEventRequest getCurrentRequest() {
		synchronized (requestList) {
			if (!requestList.isEmpty()) {
				return requestList.get(0).getRequest();
			}
			return null;
		}
	}
	public void removeAllRegisteredEventRequests() {
		synchronized (requestList) {
			EventRequestScheduledTask[] scheduledTasks = requestList.toArray(new EventRequestScheduledTask[0]);
			for (EventRequestScheduledTask scheduledTask : scheduledTasks) {
				scheduledTask.cancelTimeout();
			}
			requestList.clear();
		}
	}
	public void registerEventRequest(IPDIEventRequest request) {
		synchronized (requestList) {
			if (request instanceof IPDIGoRequest || request instanceof IPDIStepRequest) {
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
				//Resume and step requests are special request because there is no event back from sdm
				//so it needs to notify manually
				request.done();
				notifyEventRequest(request);

				session.getTaskManager().setSuspendTasks(false, request.getTasks());
				session.processRunningEvent(request.getTasks().copy());
				fireEvents(new IPDIEvent[] { new ResumedEvent(session, request.getTasks(), details) });
			}
			else if (request instanceof IPDIStopDebuggerRequest) {
				request.done();
				notifyEventRequest(request);
				session.setStatus(IPDISession.EXITED);
				return;
			}
			else {
				PDebugUtils.println("**** Msg: EventManager - registerEventRequest(): Request: " + request);
				requestList.add(new EventRequestScheduledTask(request));
			}
		}
	}
	public void notifyEventRequest(IPDIEventRequest request) {
		synchronized (requestList) {
			if (!requestList.isEmpty()) {
				requestList.remove(0).cancelTimeout();
			}
			requestNotifier.notify(request);
			PDebugUtils.println("**** Msg: EventManager - notifyEventRequest(): Request: " + request);
			switch (request.getStatus()) {
			//case IPDIEventRequest.DONE:
			//case IPDIEventRequest.RUNNING:
			case IPDIEventRequest.UNKNOWN:
				fireEvent(new ErrorEvent(new ErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL, request.getName() + " error!", "Unknown error")));
	   			break;
			case IPDIEventRequest.CANCELLED:
				fireEvent(new ErrorEvent(new ErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL, request.getName() + " error!", "Request has been cancelled")));
	   			break;
			case IPDIEventRequest.ERROR:
				int errorType = IPDIErrorInfo.DBG_NORMAL;
				if (request.getResponseAction() == IPDIEventRequest.ACTION_TERMINATED) {
					errorType = IPDIErrorInfo.DBG_FATAL;
					try {
						session.terminate(request.getTasks().copy());
					}
					catch (PDIException ex) {
						//error on terminate, manually change tasks' status
						session.getTaskManager().setPendingTasks(false, request.getTasks());
						session.getTaskManager().setTerminateTasks(true, request.getTasks());
					}
				}
				fireEvent(new ErrorEvent(new ErrorInfo(session, request.getTasks(), errorType, request.getName() + " error!", request.getErrorMessage())));
	   			break;
			}
		}
	}
	public void update(Observable o, final Object arg) {
		PDebugUtils.println("Msg: EventManager - update(): Event: " + ((IProxyDebugEvent)arg).toString());
		IProxyDebugEvent event = (IProxyDebugEvent) arg;
		IPDIEventRequest request = getCurrentRequest();
		fireEvent(request, event);
		verifyEvent(request, event);
	}
	protected void verifyEvent(IPDIEventRequest request, IProxyDebugEvent result) {
//PDebugUtils.println("Msg: EventManager - verifyEvent(): Request: " + request);
		if (request != null) {
			BitList eTasks = ProxyDebugClient.decodeBitSet(result.getBitSet());
			if (request.completed(eTasks, result)) {
				request.done();
				notifyEventRequest(request);
			}
		}
	}
	public synchronized void fireEvent(IPDIEventRequest request, IProxyDebugEvent event) {
		BitList eTasks = ProxyDebugClient.decodeBitSet(event.getBitSet());
		
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>() {
			private static final long serialVersionUID = 1L;
			public boolean add(IPDIEvent e) {
				PDebugUtils.println("Msg: EventManager - fireEvent(): added PDIEvent: " + e);
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
							eventList.add(new ErrorEvent(new ErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Delete Breakpoint Error", "No PDI breakpoint found")));
						}
						else {
							eventList.add(new DestroyedEvent(new BreakpointInfo(session, eTasks, bpt)));
						}
					}
				}
				else if (request instanceof IPDITerminateRequest) {
					session.getTaskManager().setPendingTasks(false, eTasks);
					session.getTaskManager().setTerminateTasks(true, eTasks);
					eventList.add(new DestroyedEvent(new ExitInfo(session, eTasks, 1)));
					if (session.getTaskManager().isAllTerminated(session.getTasks())) {
						eventList.add(new DisconnectedEvent(session, session.getTasks()));
					}
				}
				else if (request instanceof IPDIStartDebuggerRequest) {
					eventList.add(new ConnectedEvent(session, eTasks));
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
					eventList.add(new ErrorEvent(new ErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Set Breakpoint Error", "No PDI breakpoint found")));
				}
				else {
					eventList.add(new CreatedEvent(new BreakpointInfo(session, eTasks, bpt)));
				}
			}
		}
		else if (event instanceof IProxyDebugExitEvent) {
			IProxyDebugExitEvent e = (IProxyDebugExitEvent)event;
			session.getTaskManager().setPendingTasks(false, eTasks);
			session.getTaskManager().setTerminateTasks(true, eTasks);
			eventList.add(new DestroyedEvent(new ExitInfo(session, eTasks, e.getExitStatus())));
			if (session.getTaskManager().isAllTerminated(session.getTasks())) {
				try {
					session.exit();
					eventList.add(new DisconnectedEvent(session, session.getTasks()));
				}
				catch (PDIException ex) {
					eventList.add(new ErrorEvent(new ErrorInfo(session, ex.getTasks(), IPDIErrorInfo.DBG_FATAL, "Shutdown debugger error", ex.getMessage())));
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
			eventList.add(new ErrorEvent(new ErrorInfo(session, eTasks, actionType, "Internal Error", e.getErrorMessage())));
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
			eventList.add(new DestroyedEvent(new SignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, null)));
			if (session.getTaskManager().isAllTerminated(session.getTasks())) {
				try {
					session.exit();
					eventList.add(new DisconnectedEvent(session, session.getTasks()));
				}
				catch (PDIException ex) {
					eventList.add(new ErrorEvent(new ErrorInfo(session, ex.getTasks(), IPDIErrorInfo.DBG_FATAL, "Shutdown debugger error", ex.getMessage())));
				}
			}
		}
		else if (event instanceof IProxyDebugBreakpointHitEvent) {
			IProxyDebugBreakpointHitEvent e = (IProxyDebugBreakpointHitEvent)event;
			IPDIBreakpoint bpt = session.getBreakpointManager().getBreakpoint(e.getBreakpointId());
			if (bpt == null) {
				eventList.add(new ErrorEvent(new ErrorInfo(session, eTasks, IPDIErrorInfo.DBG_NORMAL, "Hit Breakpoint Error", "No PDI breakpoint found")));
			}
			else {
				eventList.add(createSuspendedEvent(new BreakpointInfo(session, eTasks, bpt), e.getThreadId(), 0, e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugSignalEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugSignalEvent e = (IProxyDebugSignalEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = new Locator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(new SignalInfo(session, eTasks, e.getSignalName(), e.getSignalMeaning(), null, locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugStepEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugStepEvent e = (IProxyDebugStepEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = new Locator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(new EndSteppingRangeInfo(session, eTasks,  locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
			}
		}
		else if (event instanceof IProxyDebugSuspendEvent) {
			if (request == null || request.sendEvent()) {
				IProxyDebugSuspendEvent e = (IProxyDebugSuspendEvent)event;
				ProxyDebugLocator loc = e.getFrame().getLocator();
				IPDILocator locator = new Locator(loc.getFile(), loc.getFunction(), loc.getLineNumber(), loc.getAddress());
				eventList.add(createSuspendedEvent(new LocationReachedInfo(session, eTasks, locator), e.getThreadId(), e.getFrame().getLevel(), e.getDepth(), e.getChangedVars()));
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
	private IPDIEvent createSuspendedEvent(IPDISessionObject reason, int thread_id, int level, int depth, String[] vars) {
		if (depth > 1) {
			session.getTaskManager().setCanStepReturnTasks(true, reason.getTasks());
		}
		session.getTaskManager().setSuspendTasks(true, reason.getTasks());
		session.processSupsendedEvent(reason.getTasks().copy(), thread_id, vars);
		return new SuspendedEvent(reason, vars, thread_id, level, depth);
	}
	public void fireEvent(IPDIEvent event) {
		fireEvents(new IPDIEvent[] { event });
	}
	public void fireEvents(final IPDIEvent[] events) {
		if (events.length > 0) {
			IPDIEventListener[] listeners = (IPDIEventListener[])listenerList.toArray(new IPDIEventListener[0]);
			for (final IPDIEventListener listener : listeners) {
				Runnable runnable = new Runnable() {
					public void run() {
						listener.handleDebugEvents(events);
					}
				};
				session.queueRunnable(runnable);
			}
		}
	}
	/************************************ 
	 * event scheduler 
	 ***********************************/
	class EventRequestScheduledTask {
		Timer eventTimer = null;
		IPDIEventRequest request = null;
		public EventRequestScheduledTask(IPDIEventRequest request) {
			this.request = request;
			this.eventTimer = new Timer();
			scheduleTimeout();
		}
		public void scheduleTimeout() {
			eventTimer.schedule(new TimerTask() {
				public void run() {
					PDebugUtils.println("Msg: EventRequestScheduledTask - scheduleTimeout(): Request: " + request);
					request.error("Time out for this request.");
					notifyEventRequest(request);
				}
			}, session.getTimeout());
		}
		public void cancelTimeout() {
			eventTimer.cancel();
		}
		public IPDIEventRequest getRequest() {
			return request;
		}
	}
	/*****************************************
	 * Request Notifier
	 *****************************************/
	class RequestNotifier extends Observable {
		public void notify(IPDIEventRequest request) {
			setChanged();
			notifyObservers(request);
		}
	}
}
