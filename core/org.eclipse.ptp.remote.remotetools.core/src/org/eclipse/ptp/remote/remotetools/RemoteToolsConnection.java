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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.exception.UnableToForwardPortException;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetJob;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;

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
	public void close() {
		jobLock.lock();
		try {
			exeMgr = null;
			jobCondition.signal();
		} finally {
			jobLock.unlock();
		}
		try {
			control.kill(new NullProgressMonitor());
		} catch (CoreException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalTCPPort(int, java.lang.String, int)
	 */
	public void forwardLocalTCPPort(int localPort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemoteTCPPort(int, java.lang.String, int)
	 */
	public void forwardRemoteTCPPort(int remotePort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
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
	public void open() throws RemoteConnectionException {
		if (control.query() == ITargetStatus.STOPPED) {
			try {
				control.create(new NullProgressMonitor());
			} catch (CoreException e) {
				throw new RemoteConnectionException(e.getMessage());
			}
		}
		
		if (exeMgr == null) {
			try {
				control.startJob(new ITargetJob() {
					public void run(IRemoteExecutionManager manager) {
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
					}
				});
			} catch (CoreException e1) {
				throw new RemoteConnectionException(e1.getMessage());
			}
			
			jobLock.lock();
			try {
				while (exeMgr == null) {
					try {
						jobCondition.await();
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
