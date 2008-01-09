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

package org.eclipse.ptp.proxy.runtime.server;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.runtime.command.ProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.server.AbstractProxyServer;

public abstract class AbstractProxyRuntimeServer extends AbstractProxyServer {

	/*
	 * Event queue for incoming events.
	 */
	private LinkedBlockingQueue<IProxyEvent>	events = 
		new LinkedBlockingQueue<IProxyEvent>();

	public AbstractProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeCommandFactory());
	}
	
	public void sendEvent(IProxyEvent event) {
		synchronized (events) {
			events.add(event);
		}
	}
}
