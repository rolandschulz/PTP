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

package org.eclipse.ptp.internal.rtsystem.proxy.event;

import org.eclipse.ptp.rtsystem.proxy.event.AbstractProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveQueueEvent;

public class ProxyRuntimeRemoveQueueEvent 
	extends AbstractProxyRuntimeEvent 
		implements IProxyRuntimeRemoveQueueEvent {

	public ProxyRuntimeRemoveQueueEvent(int transID, String[] args) {
		super(PROXY_RUNTIME_REMOVE_QUEUE_EVENT, transID, args);
	}
}
