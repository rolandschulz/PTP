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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.core.messages.Messages;

public class LocalConnection implements IRemoteConnection {
	private String fName = IRemoteConnectionManager.DEFAULT_CONNECTION_NAME;
	private String fAddress = Messages.LocalConnection_1;
	private String fUsername = System.getProperty("user.name"); //$NON-NLS-1$
	private boolean fConnected = true;
	private IPath fWorkingDir = null;

	private final IRemoteConnection fConnection = this;
	private final IRemoteServices fRemoteServices;
	private final ListenerList fListeners = new ListenerList();

	public LocalConnection(IRemoteServices services) {
		fRemoteServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#addConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#close()
	 */
	public void close() {
		if (fConnected) {
			fConnected = false;
			fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_CLOSED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(int,
	 * java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(java.lang
	 * .String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(int,
	 * java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(java.
	 * lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return fAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		return System.getenv();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
	public String getEnv(String name) {
		return System.getenv(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getPort()
	 */
	public int getPort() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#getProperty(java.lang.String
	 * )
	 */
	public String getProperty(String key) {
		return System.getProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getRemoteServices()
	 */
	public IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return fUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#getWorkingDirectory(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	public String getWorkingDirectory() {
		if (fWorkingDir == null) {
			String cwd = System.getProperty("user.home"); //$NON-NLS-1$
			if (cwd == null) {
				cwd = System.getProperty("user.dir"); //$NON-NLS-1$;
			}
			if (cwd == null) {
				fWorkingDir = Path.ROOT;
			} else {
				fWorkingDir = new Path(cwd);
			}
		}
		return fWorkingDir.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return fConnected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (!fConnected) {
			fConnected = true;
			fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_OPENED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#removeConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#removePortForwarding(int)
	 */
	public void removePortForwarding(int port) throws RemoteConnectionException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setAddress(java.lang.String
	 * )
	 */
	public void setAddress(String address) {
		fAddress = address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setAttribute(java.lang.
	 * String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		fName = name;
		fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_RENAMED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setPassword(java.lang.String
	 * )
	 */
	public void setPassword(String password) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setPort(int)
	 */
	public void setPort(int port) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setUsername(java.lang.String
	 * )
	 */
	public void setUsername(String username) {
		fUsername = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#setWorkingDirectory(java
	 * .lang.String)
	 */
	public void setWorkingDirectory(String pathStr) {
		IPath path = new Path(pathStr);
		if (path.isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return false;
	}

	/**
	 * Notify all listeners when this connection's status changes.
	 * 
	 * @param event
	 */
	private void fireConnectionChangeEvent(final int type) {
		IRemoteConnectionChangeEvent event = new IRemoteConnectionChangeEvent() {
			public IRemoteConnection getConnection() {
				return fConnection;
			}

			public int getType() {
				return type;
			}
		};
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
	}

}
