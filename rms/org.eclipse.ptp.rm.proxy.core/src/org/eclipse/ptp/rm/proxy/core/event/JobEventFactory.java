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

import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewJobEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.proxy.event.IProxyEvent;

/**
 * A factory for creating JobEvent objects.
 */
public class JobEventFactory implements IEventFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createChangeEvent(java
	 * .lang.String[])
	 */
	public IProxyEvent createChangeEvent(String[] args) {
		return new ProxyRuntimeJobChangeEvent(-1, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createNewEvent(java.
	 * lang.String[])
	 */
	public IProxyEvent createNewEvent(String[] args) {
		return new ProxyRuntimeNewJobEvent(-1, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IEventFactory#createRemoveEvent(java
	 * .lang.String[])
	 */
	public IProxyEvent createRemoveEvent(String[] args) {
		return new ProxyRuntimeRemoveJobEvent(-1, args);
	}

}
