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

import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

/**
 */
public class EventManager extends SessionObject implements ICDIEventManager, Observer {

	public EventManager(Session session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	public void addEventListener(ICDIEventListener listener) {
		// Auto-generated method stub
		System.out.println("EventManager.addEventListener()");
		
	}

	public void removeEventListener(ICDIEventListener listener) {
		// Auto-generated method stub
		System.out.println("EventManager.removeEventListener()");
		
	}

	public void update(Observable arg0, Object arg1) {
		// Auto-generated method stub
		System.out.println("EventManager.update()");
		
	}
}
