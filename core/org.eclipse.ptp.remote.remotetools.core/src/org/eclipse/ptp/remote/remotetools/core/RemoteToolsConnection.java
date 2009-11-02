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
package org.eclipse.ptp.remote.remotetools.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.exception.AddressInUseException;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.remotetools.core.environment.PTPTargetControl;
import org.eclipse.ptp.remote.remotetools.core.messages.Messages;
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
	private final ListenerList listeners = new ListenerList();

	public RemoteToolsConnection(String name, String address, String userName, PTPTargetControl control) {
		this.control = control;
		this.connName = name;
		this.address = address;
		this.userName = userName;
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
	public synchronized void close() {
			IProgressMonitor monitor = new NullProgressMonitor();
			
			monitor.beginTask(Messages.RemoteToolsConnection_close, 1);
			
			if (isOpen()) {
				try {
					control.kill(monitor);
				} catch (CoreException e) {
			}
				
			monitor.done();
		}
	}
	
	/**
	 * Create a new execution manager. Required because script execution 
	 * closes channel after execution.
	 * 
	 * @return execution manager
	 * @throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException 
	 */
	public IRemoteExecutionManager createExecutionManager() throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException {
		return control.createExecutionManager();
	}
	
	/**
	 * Notify all listeners when this connection's status changes.
	 * 
	 * @param event
	 */
	public void fireConnectionChangeEvent(IRemoteConnectionChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRemoteConnectionChangeListener)listener).connectionChanged(event);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
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
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardLocalPort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.RemoteToolsConnection_forwarding, 10);
		/*
		 * Start with a different port number, in case we're doing this all on localhost.
		 */
		int localPort = fwdPort + 1;
		
		/*
		 * Try to find a free port on the remote machine. This take a while, so
		 * allow it to be canceled. If we've tried all ports (which could take a
		 * very long while) then bail out.
		 */
		try {
			while (!monitor.isCanceled()) {
				try {
					forwardLocalPort(localPort, fwdAddress, fwdPort);
				} catch (AddressInUseException e) {
					if (++localPort == fwdPort) {
						throw new UnableToForwardPortException(Messages.RemoteToolsConnection_remotePort);
					}
					monitor.worked(1);
				}
				return localPort;
			}
		} finally {
			monitor.done();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
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
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#forwardRemotePort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnection_connectionNotOpen);
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.RemoteToolsConnection_forwarding, 10);
		/*
		 * Start with a different port number, in case we're doing this all on localhost.
		 */
		int remotePort = fwdPort + 1;
		/*
		 * Try to find a free port on the remote machine. This take a while, so
		 * allow it to be canceled. If we've tried all ports (which could take a
		 * very long while) then bail out.
		 */
		try {
			while (!monitor.isCanceled()) {
				try {
					forwardRemotePort(remotePort, fwdAddress, fwdPort);
				} catch (AddressInUseException e) {
					if (++remotePort == fwdPort) {
						throw new UnableToForwardPortException(Messages.RemoteToolsConnection_remotePort);
					}
					monitor.worked(1);
				}
				monitor.done();
				return remotePort;
			}
		} finally {
			monitor.done();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return control.getAttributes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getName()
	 */
	public String getName() {
		return connName;
	}

	public String getUsername() {
		return userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#isOpen()
	 */
	public synchronized boolean isOpen() {
		return control.query() == ITargetStatus.RESUMED;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (control.query() == ITargetStatus.STOPPED) {
			monitor.beginTask(Messages.RemoteToolsConnection_open, 2);
			try {
				control.create(monitor);
			} catch (CoreException e) {
				throw new RemoteConnectionException(e);
			}
		}
		monitor.done();
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
	public void setUsername(String userName) {
		this.userName = userName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return true;
	}
}
