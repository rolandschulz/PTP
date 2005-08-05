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

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;

/**
 */
public class BreakpointHit extends SessionObject implements ICDIBreakpointHit {

	EBreakpointHit breakEvent;

	public BreakpointHit(Session session, EBreakpointHit e) {
		super(session);
		breakEvent = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit#getBreakpoint()
	 */
	public ICDIBreakpoint getBreakpoint() {
		return null;
	}

}
