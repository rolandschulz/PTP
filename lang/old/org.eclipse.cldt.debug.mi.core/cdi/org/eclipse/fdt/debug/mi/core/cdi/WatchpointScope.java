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
package org.eclipse.fdt.debug.mi.core.cdi;

import org.eclipse.fdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.fdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.fdt.debug.mi.core.cdi.model.Watchpoint;
import org.eclipse.fdt.debug.mi.core.event.MIWatchpointScopeEvent;

/**
 */
public class WatchpointScope extends SessionObject implements ICDIWatchpointScope {

	MIWatchpointScopeEvent watchEvent;

	public WatchpointScope(Session session, MIWatchpointScopeEvent e) {
		super(session);
		watchEvent = e;
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.ICDIWatchpointScope#getWatchpoint()
	 */
	public ICDIWatchpoint getWatchpoint() {
		int number = watchEvent.getNumber();
		// Ask the breakpointManager for the breakpoint
		BreakpointManager mgr = ((Session)getSession()).getBreakpointManager();
		// We need to return the same object as the reason.
		Watchpoint point = mgr.getWatchpoint(watchEvent.getMISession(), number);
		// FIXME: if point ==null ??? Create a new breakpoint ?
		return point;
	}

}
