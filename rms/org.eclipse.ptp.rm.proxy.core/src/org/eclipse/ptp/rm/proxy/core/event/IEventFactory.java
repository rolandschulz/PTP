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

/**
 * A factory for creating IEvent objects.
 */
public interface IEventFactory {

	/**
	 * Creates a change event.
	 * 
	 * @param args
	 *            event arguments
	 * @return the change event
	 */
	IProxyEvent createChangeEvent(String[] array);

	/**
	 * Creates a new-event.
	 * 
	 * @param args
	 *            event arguments
	 * @return the new-event
	 */
	IProxyEvent createNewEvent(String[] args);

	/**
	 * Creates a remove event.
	 * 
	 * @param args
	 *            event arguments
	 * @return the remove event
	 */
	IProxyEvent createRemoveEvent(String[] args);

}
