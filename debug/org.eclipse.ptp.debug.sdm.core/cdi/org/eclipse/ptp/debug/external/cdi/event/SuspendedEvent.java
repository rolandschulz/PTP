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
import org.eclipse.ptp.debug.external.cdi.BreakpointHit;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;

/**
 *
 */
public class SuspendedEvent extends AbstractEvent implements ICDISuspendedEvent {

	public SuspendedEvent(Session s, EBreakpointHit ev) {
		super(s, ev);
	}
	
	public ICDISessionObject getReason() {
		// Auto-generated method stub
		System.out.println("SuspendedEvent.getReason()");
		if (event instanceof EBreakpointHit) {
			return new BreakpointHit(session, (EBreakpointHit)event);
		}
		return session;
	}
}
