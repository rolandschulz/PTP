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

	DebugEvent event;
	Session session;

	public SuspendedEvent(Session s, EBreakpointHit ev) {
		session = s;
		event = ev;
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
		// FIXME
		//Target target = session.getTarget(event.getDebugSession());
		Target target = (Target) session.getTarget(0);
		// We can send the target as the Source.  CDI
		// Will assume that all threads are supended for this.
		// This is true for gdb when it suspend the inferior
		// all threads are suspended.
		return target;
	}
}
