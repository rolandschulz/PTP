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
package org.eclipse.ptp.remote.remotetools;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.exception.AddressInUseException;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.remotetools.environment.core.PTPTargetControl;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;

public class RemoteToolsConnection implements IRemoteConnection {
	private String connName;
	private String address;
	private String userName;
	private PTPTargetControl control;

	public RemoteToolsConnection(String name, String address, String userName, PTPTargetControl control) {
		this.control = control;
		this.connName = name;
		this.address = address;
		this.userName = userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#close()
	 */
	public synchronized void close(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			control.kill(monitor);
		} catch (CoreException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException("Connection is not open");
		}
		try {
			control.getExecutionManager().createTunnel(localPort, fwdAddress, fwdPort);
		} catch (LocalPortBoundException e) {
			throw new AddressInUseException(e.getMessage());
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (CancelException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException("Connection is not open");
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException("Connection is not open");
		}
		try {
			control.getExecutionManager().getPortForwardingTools().forwardRemotePort(remotePort, fwdAddress, fwdPort);
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (CancelException e) {
			throw new RemoteConnectionException(e.getMessage());
		} catch (PortForwardingException e) {
			throw new AddressInUseException(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		/*
		 * Start with a different port number, in case we're doing this all on localhost.
		 */
		int remotePort = fwdPort + 1;
		/*
		 * Try to find a free port on the remote machine. This take a while, so
		 * allow it to be canceled. If we've tried all ports (which could take a
		 * very long while) then bail out.
		 */
		while (!monitor.isCanceled()) {
			try {
				forwardRemotePort(remotePort, fwdAddress, fwdPort);
			} catch (AddressInUseException e) {
				if (++remotePort == fwdPort) {
					throw new UnableToForwardPortException("Could not allocate remote port");
				}
				monitor.worked(1);
			}
			return remotePort;
		}
		return -1;
	}
	
	/**
	 * @return execution manager
	 * @throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException 
	 */
	public IRemoteExecutionManager getExecutionManager() throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException {
		return control.getExecutionManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return connName;
	}

	public String getUsername() {
		return userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#isOpen()
	 */
	public synchronized boolean isOpen() {
		return control.query() == ITargetStatus.RESUMED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (control.query() == ITargetStatus.STOPPED) {
			try {
				control.create(monitor);
			} catch (CoreException e) {
				throw new RemoteConnectionException(e.getMessage());
			}
			if (monitor.isCanceled()) {
				throw new RemoteConnectionException("Remote connection canceled");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String userName) {
		this.userName = userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#toPath(java.net.URI)
	 */
	public IPath toPath(URI uri) {
		return new Path(uri.getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		try {
			String auth = getAddress();
			String user = getUsername();
			if (user != null && !user.equals("")) {
				auth = user + "@" + auth;
			}
			return new URI("remotetools", auth, path.toPortableString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
