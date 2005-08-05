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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.debug.external.cdi.BreakpointHit;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;

/**
 *
 */
public class SuspendedEvent implements ICDISuspendedEvent {
	Session session;	
	ICDIObject source;
	DebugEvent event;

	public SuspendedEvent(Session s, EBreakpointHit ev) {
		session = s;
		event = ev;
		int pId = ev.getProcessId();
		int tId = ev.getThreadId();
		
		if (!session.isRegistered(pId))
			source = null;
		else {
			try {
				source = ((Target) session.getTarget(pId)).getThread(tId);
			} catch (CDIException e) {
				source = null;
			}
		}
	}
	
	public ICDISessionObject getReason() {
		// Auto-generated method stub
		System.out.println("SuspendedEvent.getReason()");
		if (event instanceof EBreakpointHit) {
			return new BreakpointHit(session, (EBreakpointHit)event);
		}
		return session;
	}

	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("SuspendedEvent.getSource()");
		Target target = (Target) session.getTarget(event.getProcessId());
		
		// We can send the target as the Source.  CDI
		// Will assume that all threads are supended for this.
		// This is true for gdb when it suspend the inferior
		// all threads are suspended.
		return target;
	}
}
