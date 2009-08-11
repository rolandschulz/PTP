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
package org.eclipse.ptp.services.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;


public class ServiceModelEventManager {
	private List<ListenerList> fEventListeners = new ArrayList<ListenerList>();
	
	public ServiceModelEventManager() {
		for (int index = 1; index <= IServiceModelEvent.ALL_EVENTS; index <<= 1) {
			fEventListeners.add(new ListenerList());
		}
	}

	/**
	 * Adds the given listener for service model events. Has no effect if an 
	 * identical listener is already registered. 
	 * <p>
	 * Listeners can listen for several types of event as defined in
	 * <code>IServiceModelEvent</code>. Clients are free to register for
	 * any number of event types. Clients are guaranteed to only receive
	 * event types for which they are registered.
	 * </p>
	 * 
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the
	 * listener
	 * @see IServiceModelEventListener
	 * @see IServiceModelEvent
	 * @see #removeEventListener(IServiceModelEventListener)
	 */
	public void addEventListener(IServiceModelEventListener listener, int type) {
		for (int index = 1, pos = 0; index <= IServiceModelEvent.ALL_EVENTS; index <<= 1, pos++) {
			if ((type & index) == index) {
				ListenerList list = fEventListeners.get(pos);
				if (list != null) { // should never be null
					list.add(listener);
				}
			}
		}
	}
	
	/**
	 * Notify listeners of an event occurrence. Only listeners for the specific
	 * event type will be notified.
	 * 
	 * @param event event to notify
	 */
	public void notifyListeners(IServiceModelEvent event) {
		for (int index = 1, pos = 0; index <= IServiceModelEvent.ALL_EVENTS; index <<= 1, pos++) {
			if ((event.getType() & index) == index) {
				ListenerList list = fEventListeners.get(pos);
				if (list != null) { // should never be null
					for (Object obj : list.getListeners()) {
						((IServiceModelEventListener)obj).handleEvent(event);
					}
				}
			}
		}
	}

	/**
	 * Removes the given listener for service model events. Has no effect if the 
	 * listener is not registered. 
	 * <p>
	 * @param listener the listener
	 * @see IServiceModelEventListener
	 * @see IServiceModelEvent
	 * @see #addEventListener(IServiceModelEventListener)
	 */
	public void removeEventListener(IServiceModelEventListener listener) {
		for (ListenerList list : fEventListeners) {
			list.remove(listener);
		}
	}
}
