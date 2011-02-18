/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

 *******************************************************************************/
package org.eclipse.ptp.rm.proxy.core.event;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;

/**
 * A factory for creating NodeEvent objects.
 */
public class NodeEventFactory implements IEventFactory {

	private final ProxyRuntimeEventFactory fEventFactory = new ProxyRuntimeEventFactory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createChangeEvent(java
	 * .lang.String[])
	 */
	public IProxyEvent createChangeEvent(String[] args) {
		return fEventFactory.newProxyRuntimeNodeChangeEvent(-1, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createNewEvent(java.
	 * lang.String[])
	 */
	public IProxyEvent createNewEvent(String[] args) {
		return fEventFactory.newProxyRuntimeNewNodeEvent(-1, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createRemoveEvent(java
	 * .lang.String[])
	 */
	public IProxyEvent createRemoveEvent(String[] args) {
		return fEventFactory.newProxyRuntimeRemoveNodeEvent(-1, args);
	}
}
