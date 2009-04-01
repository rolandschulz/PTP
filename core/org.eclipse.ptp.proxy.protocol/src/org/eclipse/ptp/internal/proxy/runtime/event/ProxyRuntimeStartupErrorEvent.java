/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.proxy.runtime.event;

import org.eclipse.ptp.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.AbstractProxyRuntimeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeStartupErrorEvent;

public class ProxyRuntimeStartupErrorEvent 
	extends AbstractProxyRuntimeEvent 
		implements IProxyRuntimeStartupErrorEvent {

	public ProxyRuntimeStartupErrorEvent(String message) {
		super(STARTUP_ERROR, 0, new String[] {
				IProxyErrorEvent.ERROR_CODE_ATTR + "=" + 0, //$NON-NLS-1$
				IProxyErrorEvent.ERROR_MESSAGE_ATTR + "=" + message //$NON-NLS-1$
		});
	}

	public ProxyRuntimeStartupErrorEvent(String[] attrs) {
		super(STARTUP_ERROR, 0, attrs);
	}
}
