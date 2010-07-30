/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.proxy.debug.event;

import org.eclipse.ptp.proxy.debug.event.AbstractProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugOutputEvent;

public class ProxyDebugOutputEvent extends AbstractProxyDebugEvent implements IProxyDebugOutputEvent {
	private String fOutput;

	public ProxyDebugOutputEvent(int transID, String bits, String output) {
		super(transID, EVENT_DBG_OUTPUT, bits);
		fOutput = output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.debug.event.IProxyDebugOutputEvent#getOutput()
	 */
	public String getOutput() {
		return fOutput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.AbstractProxyEvent#toString()
	 */
	@Override
	public String toString() {
		return "EVENT_DBG_OUTPUT transid=" + getTransactionID() + " " + this.getBitSet().toString() + " " + fOutput; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
