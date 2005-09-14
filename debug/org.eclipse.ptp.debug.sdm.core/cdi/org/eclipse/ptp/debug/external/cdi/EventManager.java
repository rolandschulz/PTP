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
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.external.cdi.event.AbstractEvent;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	List list = Collections.synchronizedList(new ArrayList(1));
	
	public EventManager(Session session) {
		super(session);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#addEventListener(ICDIEventListener)
	 */
	public void addEventListener(ICDIEventListener listener) {
		System.out.println("EventManager.addEventListener()");
		if (!list.contains(listener))
			list.add(listener);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#removeEventListener(ICDIEventListener)
	 */
	public void removeEventListener(ICDIEventListener listener) {
		System.out.println("EventManager.removeEventListener()");
		if (list.contains(listener))
			list.remove(listener);
	}

	public void removeEventListeners() {
		System.out.println("EventManager.removeEventListeners()");
		list.clear();
	}

	/**
	 * Send ICDIEvent to the listeners.
	 */
	public void fireEvents(ICDIEvent[] cdiEvents) {
		if (cdiEvents != null && cdiEvents.length > 0) {
			ICDIEventListener[] listeners = (ICDIEventListener[])list.toArray(new ICDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvents(cdiEvents);
			}			
		}
	}

	/**
	 * Process the event from the back end debugger, do any state work on the CDI,
	 * and fire the corresponding CDI event.
	 */
	public void update(Observable obs, Object ev) {
		System.out.println("EventManager.update()");
		
		IPCDIEvent event = (IPCDIEvent) ev;
		
		List cdiList = new ArrayList(1);
		
		cdiList.add(event);
		
		if (event instanceof ICDISuspendedEvent) {
			processSuspendedEvent(event);
		}
		
		// Fire the event;
		ICDIEvent[] cdiEvents = (ICDIEvent[])cdiList.toArray(new ICDIEvent[0]);
		fireEvents(cdiEvents);
	}

	/**
	 * When suspended arrives, reset managers and target.
	 * Alse the variable and the memory needs to be updated and events
	 * fired for changes.
	 */
	boolean processSuspendedEvent(IPCDIEvent event) {
		Session session = (Session) getSession();
		
		int[] procs = ((AbstractEvent) event).getAllRegisteredProcesses().toIntArray();
		
		for (int i = 0; i < procs.length; i++) {
			Target currentTarget = (Target) session.getTarget(procs[i]);
			currentTarget.setSuspended(true);

			//int threadId = threadId = stopped.getThreadId();
			//currentTarget.updateState(threadId);
			currentTarget.updateState();
			
			try {
				Thread cthread = (Thread) currentTarget.getCurrentThread();
				if (cthread != null) {
					cthread.getCurrentStackFrame();
				} else {
					return true;
				}
			} catch (CDIException e1) {
				//e1.printStackTrace();
				return true;
			}
		}

		// Update the managers.
		// For the Variable/Expression Managers call only the updateManager.
		VariableManager varMgr = session.getVariableManager();
		ExpressionManager expMgr  = session.getExpressionManager();		
		BreakpointManager bpMgr = session.getBreakpointManager();
		try {
			if (varMgr.isAutoUpdate()) {
				varMgr.update(null);
			}
			if (expMgr.isAutoUpdate()) { 
				expMgr.update(null);
			}
			if (bpMgr.isAutoUpdate()) {
				bpMgr.update(null);
			}
		} catch (CDIException e) {
			//System.out.println(e);
		}
		return true;
	}
}
