/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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

public class RemoteFastIndexerUpdateEvent implements IRemoteFastIndexerUpdateEvent {
	private EventType 			eventType;
	private RemoteIndexerTask 	task;
	private Scope				scope;
	
	public RemoteFastIndexerUpdateEvent(EventType type, RemoteIndexerTask task, Scope scope) {
		this.eventType = type;
		this.task = task;
		this.scope = scope;
	}

	public EventType getType() {
		return eventType;
	}

	public Scope getScope() {
		return scope;
	}

	public RemoteIndexerTask getTask() {
		return task;
	}
}
