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

import org.eclipse.fdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.fdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.fdt.debug.mi.core.event.MISignalEvent;

/**
 */
public class SignalReceived extends SessionObject implements ICDISignalReceived {

	ICDISignal signal;
	public SignalReceived(Session session, MISignalEvent event) {
		super(session);
		SignalManager mgr = session.getSignalManager();
		signal = mgr.getSignal(event.getMISession(), event.getName());
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.ICDISignalReceived#getSignal()
	 */
	public ICDISignal getSignal() {
		return signal;
	}

}
