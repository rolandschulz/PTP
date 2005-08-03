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

import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.PTPObject;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EBreakpointCreated;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;

/**
 */
public class CreatedEvent implements ICDICreatedEvent {

	Session session;
	ICDIObject source;

	public CreatedEvent(Session s, EBreakpointCreated ev) {
		session = s;
		DebugSession dSession = ev.getDebugSession();
		// FIXME 
		//Target target = session.getTarget(dSession);
		Target target = (Target) session.getTarget(0);
		source = new PTPObject(target);
	}

	public CreatedEvent(Session s, EInferiorCreated ev) {
		session = s;
		source = null;
	}

	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("CreatedEvent.getSource()");
		return source;
	}
}
