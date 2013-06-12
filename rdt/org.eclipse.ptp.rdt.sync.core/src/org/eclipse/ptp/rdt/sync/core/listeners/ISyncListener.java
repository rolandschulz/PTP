/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.listeners;

import org.eclipse.ptp.rdt.sync.core.SyncEvent;

/**
 * Simple interface for clients wishing to register for sync events
 * 
 * @since 3.0
 */
public interface ISyncListener {
	/**
	 * Called to notify receiver of a sync event
	 * 
	 * @param event
	 *            event being raised
	 */
	public void handleSyncEvent(SyncEvent event);
}
