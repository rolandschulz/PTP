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
package org.eclipse.cldt.debug.mi.core.cdi.event;

import org.eclipse.cldt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cldt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cldt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cldt.debug.mi.core.cdi.ExitInfo;
import org.eclipse.cldt.debug.mi.core.cdi.Session;
import org.eclipse.cldt.debug.mi.core.cdi.SignalExitInfo;
import org.eclipse.cldt.debug.mi.core.cdi.model.Target;
import org.eclipse.cldt.debug.mi.core.event.MIEvent;
import org.eclipse.cldt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cldt.debug.mi.core.event.MIInferiorSignalExitEvent;

/**
 */
public class ExitedEvent implements ICDIExitedEvent {

	MIEvent event;
	Session session;
	
	public ExitedEvent(Session s, MIInferiorExitEvent e) {
		session = s;
		event = e;
	}

	public ExitedEvent(Session s, MIInferiorSignalExitEvent e) {
		session = s;
		event = e;
	}
	
	/**
	 * @see org.eclipse.cldt.debug.core.cdi.event.ICDIExitedEvent#getExitInfo()
	 */
	public ICDISessionObject getReason() {
		if (event instanceof MIInferiorExitEvent) {
			return new ExitInfo(session, (MIInferiorExitEvent)event);
		} else if (event instanceof MIInferiorSignalExitEvent) {
			return new SignalExitInfo(session, (MIInferiorSignalExitEvent)event);
		}
		return session;
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		Target target = session.getTarget(event.getMISession());
		return target;
	}

}
