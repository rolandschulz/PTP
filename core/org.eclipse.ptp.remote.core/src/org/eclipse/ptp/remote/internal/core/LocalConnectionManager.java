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
package org.eclipse.ptp.remote.internal.core;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;


public class LocalConnectionManager implements IRemoteConnectionManager {
	private final ListenerList listeners = new ListenerList();
	private IRemoteConnection localConnection = new LocalConnection(this);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#addConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#fireConnectionChangeEvent(org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent)
	 */
	public void fireConnectionChangeEvent(IRemoteConnectionChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRemoteConnectionChangeListener)listener).connectionChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		return localConnection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		return new IRemoteConnection[]{localConnection};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection connection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		listeners.remove(listener);
	}
}
