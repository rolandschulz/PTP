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
package org.eclipse.ptp.debug.external.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.event.EExit;

/**
 */
public class ExitedEvent implements ICDIExitedEvent {

	Session session;
	ICDIObject source;

	public ExitedEvent(Session s, EExit ev) {
		session = s;
		source = null;
	}
	
	public ICDISessionObject getReason() {
		// Auto-generated method stub
		System.out.println("ExitedEvent.getReason()");
		return null;
	}

	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("ExitedEvent.getSource()");
		return null;
	}
}
