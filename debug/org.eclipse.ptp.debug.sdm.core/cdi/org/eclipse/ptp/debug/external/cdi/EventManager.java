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

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.ptp.debug.external.cdi.event.CreatedEvent;
import org.eclipse.ptp.debug.external.cdi.event.ExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.SuspendedEvent;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.event.EBreakpointCreated;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;
import org.eclipse.ptp.debug.external.event.EExit;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	List list = Collections.synchronizedList(new ArrayList(1));
	
	public EventManager(Session session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#addEventListener(ICDIEventListener)
	 */
	public void addEventListener(ICDIEventListener listener) {
		System.out.println("EventManager.addEventListener()");
		list.add(listener);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#removeEventListener(ICDIEventListener)
	 */
	public void removeEventListener(ICDIEventListener listener) {
		System.out.println("EventManager.removeEventListener()");
		list.remove(listener);
	}

	public void removeEventListeners() {
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

	public void update(Observable obs, Object ev) {
		// Auto-generated method stub
		System.out.println("EventManager.update()");
		
		DebugEvent event = (DebugEvent) ev;
		Session session = (Session)getSession();
		
		List cdiList = new ArrayList(1);
		
		if (event instanceof EExit) {
			System.out.println("Exit Event Received");
			cdiList.add(new ExitedEvent(session, (EExit) event));
		} else if (event instanceof EBreakpointCreated) {
			System.out.println("BreakpointCreated Event Received");
			cdiList.add(new CreatedEvent(session, (EBreakpointCreated) event));
		} else if (event instanceof EBreakpointHit) {
			System.out.println("BreakpointHit Event Received");
			cdiList.add(new SuspendedEvent(session, (EBreakpointHit) event));
		} else if (event instanceof EInferiorCreated) {
			System.out.println("InferiorCreated Event Received");
			cdiList.add(new CreatedEvent(session, (EInferiorCreated) event));
		} else {
			System.out.println("Unknown Event");
		}
		
		// Fire the event;
		ICDIEvent[] cdiEvents = (ICDIEvent[])cdiList.toArray(new ICDIEvent[0]);
		fireEvents(cdiEvents);
	}
}
