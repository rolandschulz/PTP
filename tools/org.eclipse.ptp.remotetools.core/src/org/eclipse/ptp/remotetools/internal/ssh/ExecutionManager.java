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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteOperation;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.core.IRemoteTunnel;
import org.eclipse.ptp.remotetools.core.messages.Messages;
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
	Set<IRemoteTunnel> tunnels = new HashSet<IRemoteTunnel>();

	/**
	 * Remote executions created by this manager.
	 */
	Set<IRemoteOperation> executions = new HashSet<IRemoteOperation>();

	/**
	 * Connection created the execution manager.
	 */
	protected final Connection connection;

	/**
	 * The instance that provides execution facility methods.
	 */
	protected IRemoteExecutionTools executionTools = null;

	/**
	 * The instance that provides file manipulation facility methods.
	 */
	protected IRemoteFileTools fileTools = null;

	/**
	 * The instance that provides file transfer facility methods.
	 */
	protected IRemoteCopyTools copyTools = null;

	/**
	 * The instance that provides the path converter tool.
	 */
	protected IRemotePathTools pathTools = null;

	/**
	 * The instance that provides the status tool.
	 */
	protected IRemoteStatusTools statusTools = null;

	/**
	 * The instance that provides the port forwarding.
	 */
	protected IRemotePortForwardingTools portForwardingTools = null;

	/**
	 * Automatic-generated port attribute
	 */
	private static final int minPort = 10000;
	private static final int maxPort = 65000;
	private static int autoActualPort = minPort;

	/**
	 * Cancel flag. If true, no more operations are allowed on the this
	 * execution manager.
	 */
	private boolean cancelFlag = false;

	/**
	 * Method that updates and return a new port number
	 */
	private synchronized static int getNewPortNumber() {
		if (autoActualPort == maxPort)
			autoActualPort = minPort;

		return autoActualPort++;
	}

	/**
	 * Class constructor. This method initializes control data and allocs a
	 * control terminal.
	 * 
	 * @throws RemoteCommandException
	 *             The manager could not be created.
	 * 
	 */
	protected ExecutionManager(Connection connection) throws RemoteConnectionException {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteExecutionManager#cancel()
	 */
	public synchronized void cancel() {
		/*
		 * Cancel all operations. Each operation implements its own logic how to
		 * cancel. This simply broadcasts the cancel to all running operations.
		 */
		for (IRemoteOperation execution : new ArrayList<IRemoteOperation>(executions)) {
			execution.cancel();
		}
		cancelFlag = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#resetCancel()
	 */
	public synchronized void resetCancel() {
		cancelFlag = false;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		for (IRemoteTunnel tunnel : new ArrayList<IRemoteTunnel>(tunnels)) {
			try {
				// releaseTunnel() already removes the entry from the list.
				releaseTunnel(tunnel);
			} catch (RemoteConnectionException e) {
			}
		}
		connection.getForwardingPool().disconnect(this);

		/*
		 * Close all channels for remote executions.
		 */
		for (IRemoteOperation execution : new ArrayList<IRemoteOperation>(executions)) {
			execution.close();
		}
	}

	/**
	 * Get the connection used by the execution manager.
	 */
	protected Connection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getExecutionTools
	 * ()
	 */
	public IRemoteExecutionTools getExecutionTools() throws RemoteConnectionException {
		if (executionTools == null) {
			executionTools = new ExecutionTools(this);
		}
		return executionTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteFileTools
	 * ()
	 */
	public IRemoteFileTools getRemoteFileTools() throws RemoteConnectionException {
		if (fileTools == null) {
			fileTools = new FileTools(this);
		}
		return fileTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteCopyTools
	 * ()
	 */
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException {
		if (copyTools == null) {
			copyTools = new CopyTools(this);
		}
		return copyTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemotePathTools
	 * ()
	 */
	public IRemotePathTools getRemotePathTools() {
		if (pathTools == null) {
			pathTools = new PathTools(this);
		}
		return pathTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#getRemoteStatusTools
	 * ()
	 */
	public IRemoteStatusTools getRemoteStatusTools() throws RemoteConnectionException {
		if (statusTools == null) {
			statusTools = new StatusTools(this);
		}
		return statusTools;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#createTunnel
	 * (int, java.lang.String, int)
	 */
	public synchronized IRemoteTunnel createTunnel(int localPort, String addressOnRemoteHost, int portOnRemoteHost)
			throws RemoteConnectionException, LocalPortBoundException, CancelException {
		test();
		testCancel();
		RemoteTunnel tunnel = connection.createTunnel(localPort, addressOnRemoteHost, portOnRemoteHost);
		tunnels.add(tunnel);
		return tunnel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteExecutionManager#createTunnel
	 * (java.lang.String, int)
	 */
	public synchronized IRemoteTunnel createTunnel(String addressOnRemoteHost, int portOnRemoteHost)
			throws RemoteConnectionException, LocalPortBoundException, CancelException {
		// Generate a local port automatically, before calling the createTunnel
		// method passing it as parameter.
		test();
		testCancel();
		int storedPort = getNewPortNumber();
		int newGeneratedPort = storedPort;
		while (true) {
			try {
				IRemoteTunnel tunnel = createTunnel(newGeneratedPort, addressOnRemoteHost, portOnRemoteHost);
				return tunnel;
			} catch (LocalPortBoundException e) {
				// If the new port generated is equal the port we stored before
				// the loop, all ports are probably busy
				// so, generate an exception.
				newGeneratedPort = getNewPortNumber();
				if (newGeneratedPort == storedPort)
					throw new LocalPortBoundException(Messages.ExecutionManager_CreateTunnel_AllLocalPortsBusy);
			}
		}

	}

	public synchronized void releaseTunnel(IRemoteTunnel tunnel) throws RemoteConnectionException {
		try {
			test();
			connection.releaseTunnel((RemoteTunnel) tunnel);
		} finally {
			tunnels.remove(tunnel);
		}
	}

	protected synchronized void registerOperation(IRemoteOperation operation) {
		executions.add(operation);
	}

	protected synchronized void unregisterOperation(IRemoteOperation operation) {
		executions.remove(operation);
	}

	/**
	 * Throw an exception if the manager cannot execute commands.
	 * 
	 * @throws RemoteConnectionException
	 *             The connection was lost.
	 * @throws CancelException
	 *             The manager is canceled.
	 */
	protected void test() throws RemoteConnectionException {
		connection.test();
	}

	protected void testCancel() throws CancelException {
		if (cancelFlag) {
			throw new CancelException();
		}
	}

	public IRemotePortForwardingTools getPortForwardingTools() throws RemoteConnectionException {
		if (portForwardingTools == null) {
			portForwardingTools = new PortForwardingTools(this);
		}
		return portForwardingTools;
	}
}
