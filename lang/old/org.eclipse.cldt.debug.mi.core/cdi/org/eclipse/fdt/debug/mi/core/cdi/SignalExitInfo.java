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

import org.eclipse.fdt.debug.core.cdi.ICDISignalExitInfo;
import org.eclipse.fdt.debug.mi.core.event.MIInferiorSignalExitEvent;

/**.
 */
public class SignalExitInfo extends SessionObject implements ICDISignalExitInfo {

	MIInferiorSignalExitEvent event;

	public SignalExitInfo(Session session, MIInferiorSignalExitEvent e) {
		super(session);
		event = e;
	}
	
	/**
	 * @see org.eclipse.fdt.debug.core.cdi.ICDISignalExitInfo#getName()
	 */
	public String getName() {
		return event.getName();
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.ICDISignalExitInfo#getDescription()
	 */
	public String getDescription() {
		return event.getMeaning();
	}

}
