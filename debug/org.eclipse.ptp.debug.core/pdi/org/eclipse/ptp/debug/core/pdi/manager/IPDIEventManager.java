/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;

/**
 * Clients interested in the PDI model change notification may register with
 * this object
 * 
 * @author clement
 * 
 */
public interface IPDIEventManager extends IPDIManager {
	/**
	 * Adds the given listener to the collection of registered event listeners.
	 * Has no effect if an identical listener is already registered
	 * 
	 * @param listener
	 *            - the listener to add
	 */
	public void addEventListener(IPDIEventListener listener);

	/**
	 * Fire event
	 * 
	 * @param event
	 */
	public void fireEvent(IPDIEvent event);

	/**
	 * Fire multiple events
	 * 
	 * @param events
	 */
	public void fireEvents(final IPDIEvent[] events);

	/**
	 * Get the current reques
	 * 
	 * @return
	 */
	public IPDIEventRequest getCurrentRequest();

	/**
	 * Notify the event request
	 * 
	 * @param request
	 */
	public void notifyEventRequest(IPDIEventRequest request);

	/**
	 * Register a request in event manager for lookup
	 * 
	 * @param request
	 *            an event request
	 */
	public void registerEventRequest(IPDIEventRequest request);

	/**
	 * remove all registered event requests
	 */
	public void removeAllRegisteredEventRequests();

	/**
	 * Removes the given listener from the collection of registered event
	 * listeners. Has no effect if an identical listener is not already
	 * registered
	 * 
	 * @param listener
	 *            - the listener to remove
	 */
	public void removeEventListener(IPDIEventListener listener);
}
