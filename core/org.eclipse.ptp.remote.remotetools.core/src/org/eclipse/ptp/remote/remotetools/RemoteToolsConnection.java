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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
		monitor.beginTask("Closing connection...", 1);
		try {
			control.kill(monitor);
		} catch (CoreException e) {
		}
		monitor.done();
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
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Setting up remote forwarding", 10);
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
			monitor.done();
			return remotePort;
		}
		monitor.done();
		return -1;
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
		monitor.beginTask("Opening connection...", 2);
		if (control.query() == ITargetStatus.STOPPED) {
			Job job = new Job("Start the  Environment") {
				protected IStatus run(IProgressMonitor monitor) {
					
					IStatus status = null;
					
					try {
						if (control.create(monitor)) {
							status = Status.OK_STATUS;
						}
					} catch (CoreException e) {
						status = e.getStatus();
					}
					
					return status;
				}
			};
			job.setUser(true);
			job.schedule();
			monitor.worked(1);
			/*
			 * Wait for the job to finish
			 */
			while (!monitor.isCanceled() && control.query() != ITargetStatus.RESUMED) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			
			if (monitor.isCanceled()) {
				job.cancel();
			}
		}
		monitor.done();
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
}
