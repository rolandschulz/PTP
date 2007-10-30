/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteOperation;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.core.IRemoteTunnel;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.RemoteTunnel;


/**
 * @author Richard Maciel
 */
public class ExecutionManager implements IRemoteExecutionManager {
	/**
	 * Tunnels created by this manager.
	 */
	Set tunnels = new HashSet();

	/**
	 * Remote executions created by this manager.
	 */
	Set executions = new HashSet();
	
	/**
	 * Connection created the execution manager.
	 */
	protected Connection connection;

	/**
	 * The instance that provides execution facility methods.
	 */
	protected IRemoteExecutionTools executionTools;

	/**
	 * The instance that provides file manipulation facility methods.
	 */
	protected IRemoteFileTools fileTools;

	/**
	 * The instance that provides file transfer facility methods.
	 */
	protected IRemoteCopyTools copyTools;
	
	/**
	 * The instance that provides the path converter tool.
	 */
	protected IRemotePathTools pathTools;

	/**
	 * The instance that provides the status tool.
	 */
	protected IRemoteStatusTools statusTools;
	
	/**
	 * Automatic-generated port attribute
	 */
	private static final int minPort = 10000;
	private static final int maxPort = 65000;
	private static int autoActualPort = minPort;

	/**
	 * Cancel flag. If true, no more operations are allowed on the this execution manager.
	 */
	private boolean cancelFlag = false;

	
	/**
	 * Method that updates and return a new port number
	 */
	private synchronized static int getNewPortNumber() {
		if(autoActualPort == maxPort)
			autoActualPort = minPort;
		
		return autoActualPort++;
	}
	
	/**
	 * Class constructor. This method initializes control data and allocs a control terminal.
	 * 
	 * @throws RemoteCommandException The manager could not be created.
	 * 
	 */
	protected ExecutionManager(Connection connection) throws RemoteConnectionException {
		this.connection = connection;
		this.fileTools = new FileTools(this);
		this.executionTools = new ExecutionTools(this);
		this.copyTools = new CopyTools(this);
		this.pathTools = new PathTools(this);
		this.statusTools = new StatusTools(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteExecutionManager#cancel()
	 */
	public synchronized void cancel() {
		/*
		 * Cancel all operations. Each operation implements its own
		 * logic how to cancel. This simply broadcasts the cancel to
		 * all running operations.
		 */
		Iterator operationEnum = executions.iterator();
		while (operationEnum.hasNext()) {
			IRemoteOperation operation = (IRemoteOperation) operationEnum.next();
			operation.cancel();
		}
		cancelFlag = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#resetCancel()
	 */
	public synchronized void resetCancel() { 
		cancelFlag = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#close()
	 */
	public synchronized void close() {
		/*
		 * Mark manager as canceled and interrupt ongoing operations.
		 */
		cancel();

		/*
		 * Close all tunnels.
		 */
		Iterator iterator = tunnels.iterator();
		while (iterator.hasNext()) {
			IRemoteTunnel tunnel = (IRemoteTunnel) iterator.next();
			try {
				releaseTunnel(tunnel);
			} catch (RemoteConnectionException e) {
			}
		}
		tunnels.clear();
		tunnels = null;
		
		/*
		 * Close all channels for remote executions.
		 */
		iterator = executions.iterator();
		while (iterator.hasNext()) {
			IRemoteOperation operation = (IRemoteOperation) iterator.next();
			operation.close();
		}
		executions.clear();
		executions = null;
		
		/*
		 * Unregister execution manager (callback).
		 */
		connection.releaseExcutionManager(this);
		
		this.fileTools = null;
		this.executionTools = null;
		this.copyTools = null;
		this.pathTools = null;
		this.statusTools = null;

	}
	
	/**
	 * Get the connection used by the execution manager.
	 */
	protected Connection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getExecutionTools()
	 */
	public IRemoteExecutionTools getExecutionTools() throws RemoteConnectionException {
		return executionTools;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteFileTools()
	 */
	public IRemoteFileTools getRemoteFileTools() throws RemoteConnectionException {
		return fileTools;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteCopyTools()
	 */
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException {
		return copyTools;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemotePathTools()
	 */
	public IRemotePathTools getRemotePathTools() {
		return pathTools;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteStatusTools()
	 */
	public IRemoteStatusTools getRemoteStatusTools() throws RemoteConnectionException {
		return statusTools ;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#createTunnel(int, java.lang.String, int)
	 */
	public synchronized IRemoteTunnel createTunnel(int localPort, String addressOnRemoteHost, int portOnRemoteHost)
			throws RemoteConnectionException, LocalPortBoundException, CancelException {
		test();
		RemoteTunnel tunnel = connection.createTunnel(localPort, addressOnRemoteHost, portOnRemoteHost);
		tunnels.add(tunnel);
		return tunnel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#createTunnel(java.lang.String, int)
	 */
	public synchronized IRemoteTunnel createTunnel(String addressOnRemoteHost, int portOnRemoteHost) 
		throws RemoteConnectionException, LocalPortBoundException, CancelException {
		// Generate a local port automatically, before calling the createTunnel method passing it as parameter.
		test();
		int storedPort = getNewPortNumber();
		int newGeneratedPort = storedPort;
		while(true) {
			try {
				IRemoteTunnel tunnel = createTunnel(newGeneratedPort, addressOnRemoteHost, portOnRemoteHost);
				return tunnel;
			} catch(LocalPortBoundException e) {
				// If the new port generated is equal the port we stored before the loop, all ports are probably busy
				// so, generate an exception.
				newGeneratedPort = getNewPortNumber();
				if(newGeneratedPort == storedPort)
					throw new LocalPortBoundException(Messages.ExecutionManager_CreateTunnel_AllLocalPortsBusy);
			}
		}
		
	}

	public synchronized void releaseTunnel(IRemoteTunnel tunnel) throws RemoteConnectionException {
		try {
			test();
		} catch (CancelException e) {
			// Ignore.
		}
		connection.releaseTunnel((RemoteTunnel) tunnel);
		tunnels.remove(tunnel);
	}
	
	protected synchronized void registerOperation(IRemoteOperation operation) {
		executions.add(operation);
	}

	protected synchronized void unregisterOperation(IRemoteOperation operation) {
		executions.remove(operation);
	}

	/**
	 * Throw an exception if the manager cannot execute commands.
	 * @throws RemoteConnectionException The connection was lost.
	 * @throws CancelException The manager is canceled.
	 */
	protected void test() throws RemoteConnectionException, CancelException {
		connection.test();
		if (cancelFlag) {
			throw new CancelException();
		}
	}
}
