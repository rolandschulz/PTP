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

package org.eclipse.fdt.debug.core.cdi;

import org.eclipse.fdt.debug.core.cdi.event.ICDIEventListener;

/**
 * 
 * Clients interested in the CDI model change notification may
 * register with this object.
 * 
 * @since Jul 10, 2002
 */
public interface ICDIEventManager extends ICDISessionObject {

	/**
	 * Adds the given listener to the collection of registered 
	 * event listeners. Has no effect if an identical listener is 
	 * already registered. 
	 * 
	 * @param listener - the listener to add
	 */
	void addEventListener( ICDIEventListener listener );

	/**
	 * Removes the given listener from the collection of registered 
	 * event listeners. Has no effect if an identical listener is not 
	 * already registered. 
	 * 
	 * @param listener - the listener to remove
	 */
	void removeEventListener( ICDIEventListener listener );

}
