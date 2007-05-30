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

package org.eclipse.ptp.rtsystem.proxy.event;

public class ProxyRuntimeTerminateJobErrorEvent 
	extends AbstractProxyRuntimeEvent 
		implements IProxyRuntimeTerminateJobErrorEvent {

	public ProxyRuntimeTerminateJobErrorEvent(int transID, String[] args) {
		super(PROXY_RUNTIME_TERMINATEJOB_ERROR_EVENT, transID, args);
	}
}
