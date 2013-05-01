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
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestListener;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStopDebuggerRequest;
import org.eclipse.ptp.internal.debug.core.PDebugUtils;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

public abstract class AbstractEventManager extends AbstractPDIManager implements IPDIEventManager {
	/************************************
	 * event scheduler
	 ***********************************/
	/**
	 * @since 4.0
	 */
	private class EventRequestScheduledTask {
		private Timer eventTimer = null;
		private IPDIEventRequest request = null;

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
				@Override
				public void run() {
					PDebugUtils.println(Messages.AbstractEventManager_0 + request);
					request.error(Messages.AbstractEventManager_1);
					notifyEventRequest(request);
				}
			}, session.getTimeout());
		}
	}

	private final ListenerList listenerList = new ListenerList();
	private final ListenerList eventRequestListeners = new ListenerList();
	private final List<EventRequestScheduledTask> requestList = Collections
			.synchronizedList(new ArrayList<EventRequestScheduledTask>());

	public AbstractEventManager(IPDISession session) {
		super(session, false);
		eventRequestListeners.add(session.getEventRequestManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#addEventListener
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener)
	 */
	public void addEventListener(IPDIEventListener listener) {
		listenerList.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#fireEvent(org
	 * .eclipse.ptp.debug.core.pdi.event.IPDIEvent)
	 */
	public void fireEvent(IPDIEvent event) {
		fireEvents(new IPDIEvent[] { event });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#fireEvents(org
	 * .eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void fireEvents(final IPDIEvent[] events) {
		if (events.length > 0) {
			for (final Object listener : listenerList.getListeners()) {
				Runnable runnable = new Runnable() {
					public void run() {
						((IPDIEventListener) listener).handleDebugEvents(events);
					}
				};
				session.queueRunnable(runnable);
			}
		}
	}

	/**
	 * @param event
	 */
	private void notifyEventRequestListeners(IPDIEventRequest request) {
		for (Object listener : eventRequestListeners.getListeners()) {
			((IPDIEventRequestListener) listener).handleEventRequestChanged(request);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#notifyEventRequest
	 * (org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void notifyEventRequest(IPDIEventRequest request) {
		synchronized (requestList) {
			if (!(request instanceof IPDIStopDebuggerRequest)) {
				if (!requestList.isEmpty()) {
					requestList.remove(0).cancelTimeout();
				}
			}
			notifyEventRequestListeners(request);
			PDebugUtils.println(Messages.AbstractEventManager_2 + request);
			switch (request.getStatus()) {
			// case IPDIEventRequest.DONE:
			// case IPDIEventRequest.RUNNING:
			case IPDIEventRequest.UNKNOWN:
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL,
								request.getName() + Messages.AbstractEventManager_3, Messages.AbstractEventManager_4)));
				break;
			case IPDIEventRequest.CANCELLED:
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), IPDIErrorInfo.DBG_NORMAL,
								request.getName() + Messages.AbstractEventManager_5, Messages.AbstractEventManager_6)));
				break;
			case IPDIEventRequest.ERROR:
				int errorType = IPDIErrorInfo.DBG_NORMAL;
				if (request.getResponseAction() == IPDIEventRequest.ACTION_TERMINATED) {
					errorType = IPDIErrorInfo.DBG_FATAL;
					try {
						session.terminate(request.getTasks().copy());
					} catch (PDIException ex) {
						// error on terminate, manually change tasks' status
						session.getTaskManager().setPendingTasks(false, request.getTasks());
						session.getTaskManager().setTerminateTasks(true, request.getTasks());
					}
				}
				fireEvent(session.getEventFactory().newErrorEvent(
						session.getEventFactory().newErrorInfo(session, request.getTasks(), errorType,
								request.getName() + Messages.AbstractEventManager_7, request.getErrorMessage())));
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#registerEventRequest
	 * (org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void registerEventRequest(IPDIEventRequest request) {
		synchronized (requestList) {
			if (request instanceof IPDIStopDebuggerRequest) {
				request.done();
				notifyEventRequest(request);
				session.setStatus(IPDISession.EXITED);
				return;
			} else {
				PDebugUtils.println(Messages.AbstractEventManager_8 + request);
				requestList.add(new EventRequestScheduledTask(request));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#
	 * removeAllRegisteredEventRequests()
	 */
	public void removeAllRegisteredEventRequests() {
		synchronized (requestList) {
			Iterator<EventRequestScheduledTask> iter = requestList.iterator();
			while (iter.hasNext()) {
				EventRequestScheduledTask task = iter.next();

				if (task.getRequest().getStatus() != IPDIEventRequest.RUNNING) {
					task.cancelTimeout();
					iter.remove();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager#removeEventListener
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener)
	 */
	public void removeEventListener(IPDIEventListener listener) {
		listenerList.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	@Override
	public void shutdown() {
		eventRequestListeners.remove(session.getEventRequestManager());
		listenerList.clear();
		requestList.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org
	 * .eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void update(TaskSet tasks) throws PDIException {
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
