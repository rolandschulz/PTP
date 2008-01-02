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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.exception.AddressInUseException;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.exception.UnableToForwardPortException;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetJob;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;

public class RemoteToolsConnection implements IRemoteConnection {
	private String connName;
	private String hostName;
	private String userName;
	private IRemoteExecutionManager exeMgr = null;
	private ITargetControl control;

	private final ReentrantLock jobLock = new ReentrantLock();
	private final Condition jobCondition = jobLock.newCondition();

	public RemoteToolsConnection(String name, String hostName, String userName, ITargetControl control) {
		this.control = control;
		this.connName = name;
		this.hostName = hostName;
		this.userName = userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#close()
	 */
	public synchronized void close(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		jobLock.lock();
		try {
			exeMgr = null;
			jobCondition.signal();
		} finally {
			jobLock.unlock();
		}
		while (control.getJobCount() > 0) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				return;
			}
			if (monitor.isCanceled()) {
				break;
			}
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
		if (exeMgr == null) {
			throw new RemoteConnectionException("Connection is not open");
		}
		try {
			exeMgr.createTunnel(localPort, fwdAddress, fwdPort);
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
		if (exeMgr == null) {
			throw new RemoteConnectionException("Connection is not open");
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort)
			throws RemoteConnectionException {
		if (exeMgr == null) {
			throw new RemoteConnectionException("Connection is not open");
		}
		try {
			exeMgr.getPortForwardingTools().forwardRemotePort(remotePort, fwdAddress, fwdPort);
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
		return exeMgr;
	}
	
	public String getHostname() {
		return hostName;
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
		return exeMgr != null;
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
		
		if (exeMgr == null) {
			try {
				control.startJob(new ITargetJob() {
					public void run(IRemoteExecutionManager manager) {
						if (PTPRemotePlugin.getDefault().isDebugging()) {
							System.out.println("Remote tools fake job starting");
						}
						jobLock.lock();
						try {
							exeMgr = manager;
							jobCondition.signal();
						} finally {
							jobLock.unlock();
						}
						jobLock.lock();
						try {
							while (exeMgr != null) {
								try {
									jobCondition.await();
								} catch (InterruptedException e) {
									break;
								}
							}
						} finally {
							jobLock.unlock();
						}
						if (PTPRemotePlugin.getDefault().isDebugging()) {
							System.out.println("Remote tools fake job exiting");
						}
					}
				});
			} catch (CoreException e1) {
				throw new RemoteConnectionException(e1.getMessage());
			}
			
			jobLock.lock();
			try {
				while (exeMgr == null) {
					try {
						jobCondition.await(500, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				jobLock.unlock();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setHostname(java.lang.String)
	 */
	public void setHostname(String hostName) {
		this.hostName = hostName;
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
			return new URI("remotetools", getHostname(), path.toPortableString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
