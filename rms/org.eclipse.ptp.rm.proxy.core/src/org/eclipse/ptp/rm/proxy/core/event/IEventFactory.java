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

// TODO: Auto-generated Javadoc
/**
 * A factory for creating IEvent objects.
 */
public interface IEventFactory {

	/**
	 * Creates a new IEvent object.
	 * 
	 * @param args the args
	 * @return the proxy event
	 */
	IProxyEvent createChangeEvent(String[] array);

	/**
	 * Creates a new IEvent object.
	 * 
	 * @param args the args
	 * @return the proxy event
	 */
	IProxyEvent createNewEvent(String[] args);

	/**
	 * Creates a new IEvent object.
	 * 
	 * @param args the args
	 * @return the proxy event
	 */
	IProxyEvent createRemoveEvent(String[] args);

}
