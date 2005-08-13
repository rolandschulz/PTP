/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.ptp.debug.external.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.event.ETargetUnregistered;

/**
 */
public class DisconnectedEvent extends AbstractEvent implements ICDIDisconnectedEvent {

	public DisconnectedEvent(Session s, ETargetUnregistered ev) {
		super(s, ev);
	}
}
