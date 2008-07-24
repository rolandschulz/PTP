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
package org.eclipse.ptp.remote.rse.core;

import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

public class RSEConnection implements IRemoteConnection {
	private IHost rseHost;
	private IFileSystem fileSystem;
	private IShellService shellService = null;
	private ISubSystem subSystem = null;
	
	public RSEConnection(IHost host, IFileSystem fileSystem) {
		this.rseHost = host;
		this.fileSystem = fileSystem;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#close()
	 */
	public void close(IProgressMonitor monitor) {
		if (subSystem != null && subSystem.isConnected()) {
			try {
				subSystem.disconnect();
			} catch (Exception e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}
	
	/**
	 * Get the file system for this connection
	 * 
	 * @return
	 */
	public IFileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * Get RSE host object 
	 * 
	 * @return IHost
	 */
	public IHost getHost() {
		return rseHost;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return rseHost.getHostName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return rseHost.getAliasName();
	}

	/**
	 * Get the shell service for this connection
	 * 
	 * @return shell service
	 */
	public IShellService getRemoteShellService() {
		return shellService;
	}
	
	public String getUsername() {
		return rseHost.getDefaultUserId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return subSystem != null && subSystem.isConnected();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#open()
	 */
	public void open(final IProgressMonitor monitor) throws RemoteConnectionException {
		if (subSystem == null) {
			ISubSystem[] subSystems = rseHost.getSubSystems();
			for (ISubSystem sub : subSystems) {
				if (sub instanceof IShellServiceSubSystem) {
					subSystem = sub;
					break;
				}
			}
			if (subSystem == null) {
				throw new RemoteConnectionException("Could not locate shell service");
			}
			
			shellService = ((IShellServiceSubSystem)subSystem).getShellService();
		}
		
		if (!subSystem.isConnected()) {
			try {
				subSystem.connect(monitor, false);
			} catch (Exception e) {
				// Ignore
			}
			
			if(!subSystem.isConnected()) {
				throw new RemoteConnectionException("Could not connect shell service");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		rseHost.setHostName(address);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		rseHost.setDefaultUserId(username);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return false;
	}

}
