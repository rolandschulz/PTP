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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.core.messages.Messages;

public class LocalConnection implements IRemoteConnection {
	private String name;
	private String address;
	private String username;
	private boolean connected;
	private final IRemoteConnection connection = this;
	private final ListenerList listeners = new ListenerList();
	
	public LocalConnection() {
		this.name = Messages.LocalConnection_0;
		this.address = Messages.LocalConnection_1;
		this.username = System.getProperty("user.name"); //$NON-NLS-1$
		this.connected = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#addConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#close()
	 */
	public void close() {
		if (connected) {
			connected = false;
			
			fireConnectionChangeEvent(new IRemoteConnectionChangeEvent(){
				public IRemoteConnection getConnection() {
					return connection;
				}
	
				public int getType() {
					return IRemoteConnectionChangeEvent.CONNECTION_CLOSED;
				}
				
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return address;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return connected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (!connected) {
			connected = true;
			
			fireConnectionChangeEvent(new IRemoteConnectionChangeEvent(){
				public IRemoteConnection getConnection() {
					return connection;
				}
	
				public int getType() {
					return IRemoteConnectionChangeEvent.CONNECTION_OPENED;
				}
				
			});	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return false;
	}

	/**
	 * Notify all listeners when this connection's status changes.
	 * 
	 * @param event
	 */
	private void fireConnectionChangeEvent(IRemoteConnectionChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRemoteConnectionChangeListener)listener).connectionChanged(event);
		}
	}

}
