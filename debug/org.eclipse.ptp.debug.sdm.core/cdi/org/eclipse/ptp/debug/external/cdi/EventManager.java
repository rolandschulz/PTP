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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDICreatedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {
	List list = Collections.synchronizedList(new ArrayList(1));

	public void update(Observable o, Object arg) {
		IPCDIEvent event = (IPCDIEvent) arg;
		List cdiList = new ArrayList(1);

		cdiList.add(event);
		if (event instanceof IPCDISuspendedEvent) {
			processSuspendedEvent((IPCDISuspendedEvent)event);
		}
		else if (event instanceof IPCDIResumedEvent) {
		}
		else if (event instanceof IPCDIExitedEvent) {
		}
		else if (event instanceof IPCDIDisconnectedEvent) {
		}
		else if (event instanceof IPCDICreatedEvent) {
		}
		else if (event instanceof IPCDIChangedEvent) {
			
		}
		// Fire the event;
		ICDIEvent[] cdiEvents = (ICDIEvent[])cdiList.toArray(new ICDIEvent[0]);
		fireEvents(cdiEvents);
	}
	
	public EventManager(Session session) {
		super(session);
	}
	public void shutdown() {
		list.clear();
	}
	public void addEventListener(ICDIEventListener listener) {
		if (!list.contains(listener))
			list.add(listener);
	}
	public void removeEventListener(ICDIEventListener listener) {
		if (list.contains(listener))
			list.remove(listener);
	}
	public void removeEventListeners() {
		list.clear();
	}
	public synchronized void fireEvents(ICDIEvent[] cdiEvents) {
		if (cdiEvents != null && cdiEvents.length > 0) {
			ICDIEventListener[] listeners = (ICDIEventListener[])list.toArray(new ICDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvents(cdiEvents);
			}			
		}
	}
	
	boolean processSuspendedEvent(IPCDISuspendedEvent event) {
		Session session = (Session)getSession();
		/*
		VariableManager varMgr = session.getVariableManager();
		ExpressionManager expMgr  = session.getExpressionManager();		
		BreakpointManager bpMgr = session.getBreakpointManager();
		SourceManager srcMgr = session.getSourceManager();
		*/
		int[] procs = event.getAllRegisteredProcesses().toArray();
		for (int i = 0; i < procs.length; i++) {
			Target currentTarget = (Target) session.getTarget(procs[i]);
			currentTarget.setSupended(true);
			/*
			if (processSharedLibEvent(event)) {
				return false;
			}
			if (processBreakpointHitEvent(event)) {
				return false;
			}
			*/
			//TODO -- no thread id provided
			//int threadId = threadId = event.getThreadId();
			currentTarget.updateState(0);
			try {
				Thread cthread = (Thread)currentTarget.getCurrentThread();
				if (cthread != null) {
					cthread.getCurrentStackFrame();
				}
			} catch (CDIException e1) {
				e1.printStackTrace();
			}
			/**
			 * TODO not quite important
			try {
				if (varMgr.isAutoUpdate()) {
					varMgr.update(currentTarget);
				}
				if (expMgr.isAutoUpdate()) { 
					expMgr.update(currentTarget);
				}
				if (bpMgr.isAutoUpdate()) {
					bpMgr.update(currentTarget);
				}
				if (srcMgr.isAutoUpdate()) {
					srcMgr.update(currentTarget);
				}
			} catch (CDIException e) {
				e.printStackTrace();
			}
			 */
		}
		return true;
	}
}
