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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepFinishRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepIntoInstructionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepIntoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepOverInstructionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepOverRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStopDebuggerRequest;
import org.eclipse.ptp.debug.internal.core.pdi.manager.AbstractPDIManager;

public abstract class AbstractEventManager extends AbstractPDIManager implements IPDIEventManager {
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
		
		public void cancelTimeout() {
			eventTimer.cancel();
		}
		
		public IPDIEventRequest getRequest() {
			return request;
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
	
	private List<IPDIEventListener> listenerList = Collections.synchronizedList(new ArrayList<IPDIEventListener>());
	private List<EventRequestScheduledTask> requestList = Collections.synchronizedList(new ArrayList<EventRequestScheduledTask>());
	private RequestNotifier requestNotifier = new RequestNotifier();
	
	public AbstractEventManager(IPDISession session) {
		super(session, false);
		requestNotifier.addObserver(session.getEventRequestManager());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#addEventListener(org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener)
	 */
	public void addEventListener(IPDIEventListener listener) {
		if (!listenerList.contains(listener)) {
			listenerList.add(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#fireEvent(org.eclipse.ptp.debug.core.pdi.event.IPDIEvent)
	 */
	public void fireEvent(IPDIEvent event) {
		fireEvents(new IPDIEvent[] { event });
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#fireEvents(org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#notifyEventRequest(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void notifyEventRequest(IPDIEventRequest request) {
		synchronized (requestList) {
			if (!requestList.isEmpty()) {
				requestList.remove(0).cancelTimeout();
			}
			requestNotifier.notify(request);
			PDebugUtils.println("**** Msg: AbstractEventManager - notifyEventRequest(): Request: " + request);
			switch (request.getStatus()) {
			//case IPDIEventRequest.DONE:
			//case IPDIEventRequest.RUNNING:
			case IPDIEventRequest.UNKNOWN:
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL, request.getName() + " error!", "Unknown error")));
	   			break;
			case IPDIEventRequest.CANCELLED:
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL, request.getName() + " error!", "Request has been cancelled")));
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
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), errorType, request.getName() + " error!", request.getErrorMessage())));
	   			break;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#registerEventRequest(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
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
				fireEvents(new IPDIEvent[] { session.getEventFactory().newResumedEvent(session, request.getTasks(), details) });
			}
			else if (request instanceof IPDIStopDebuggerRequest) {
				request.done();
				notifyEventRequest(request);
				session.setStatus(IPDISession.EXITED);
				return;
			}
			else {
				PDebugUtils.println("**** Msg: AbstractEventManager - registerEventRequest(): Request: " + request);
				requestList.add(new EventRequestScheduledTask(request));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#removeAllRegisteredEventRequests()
	 */
	public void removeAllRegisteredEventRequests() {
		synchronized (requestList) {
			EventRequestScheduledTask[] scheduledTasks = requestList.toArray(new EventRequestScheduledTask[0]);
			for (EventRequestScheduledTask scheduledTask : scheduledTasks) {
				scheduledTask.cancelTimeout();
			}
			requestList.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#removeEventListener(org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener)
	 */
	public void removeEventListener(IPDIEventListener listener) {
		listenerList.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		requestNotifier.deleteObserver(session.getEventRequestManager());
		listenerList.clear();
		requestList.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.BitList)
	 */
	@Override
	public void update(BitList tasks) throws PDIException {
	}
	
	/**
	 * @return
	 */
	public IPDIEventRequest getCurrentRequest() {
		synchronized (requestList) {
			if (!requestList.isEmpty()) {
				return requestList.get(0).getRequest();
			}
			return null;
		}
	}
}
