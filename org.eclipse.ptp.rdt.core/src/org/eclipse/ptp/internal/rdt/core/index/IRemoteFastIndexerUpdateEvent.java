/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import org.eclipse.ptp.internal.rdt.core.model.Scope;

public interface IRemoteFastIndexerUpdateEvent {
	public enum EventType { 
		EVENT_UPDATE, 
		EVENT_REINDEX;
	}
	
	/**
	 * Get the event type
	 * @return type of the event
	 */
	public EventType getType();
	
	/**
	 * Get the task associated with this update event
	 * @return The RemoteIndexerTask
	 */
	public RemoteIndexerTask getTask();
	
	/**
	 * Get the scope associated with this update event
	 * @return The Scope
	 */
	public Scope getScope();
}
