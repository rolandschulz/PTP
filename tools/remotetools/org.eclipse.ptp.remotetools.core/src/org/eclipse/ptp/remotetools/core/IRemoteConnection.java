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
package org.eclipse.ptp.remotetools.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

/**
 * NOTE: this interface should not normally be used directly but called via the
 * org.eclipse.ptp.remote.core interfaces.
 * 
 * A connection to a remote host. Serves as a starting point for creating {@link IRemoteExecutionManager}, that to operations on the
 * remote host.
 * 
 * It is the responsibility of the caller to check that a remote connection is
 * open before using it. If the connection was open, but has been closed for
 * some reason, {@link #disconnect()} must be called before {@link #connect(AuthToken, String, int, String, int, IProgressMonitor)}
 * is
 * called again.
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.1 <b>Review OK</b>
 * 
 * @see org.eclipse.ptp.remote.core.IRemoteConnection
 */
public interface IRemoteConnection {
	/**
	 * Connect to the remote machine. The {@link #disconnect()} method must be
	 * called before this method if the connection was dropped for some reason.
	 * 
	 * @param authInfo
	 *            authentication information for connection
	 * @param connInfo
	 *            connection information
	 * @param monitor
	 *            progress monitor
	 * @throws RemoteConnectionException
	 * @since 6.0
	 */
	public void connect(IAuthInfo authInfo, IConnectionInfo connInfo, IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Terminate connection to remote machine.
	 * <p>
	 * TODO: Decide: Block until all pending operations are finished or cancels pending operations?
	 */
	public void disconnect();

	/**
	 * Check if successfully connected with the remote host.
	 * 
	 * @return True if is connected with remote host
	 */
	public boolean isConnected();

	/**
	 * Create a proper instance of {@link IRemoteExecutionManager}.
	 * 
	 * @return the {@link IRemoteExecutionManager} for this connection
	 * @throws RemoteConnectionException
	 *             if the {@link IRemoteExecutionManager} could not be created.
	 */
	public IRemoteExecutionManager createRemoteExecutionManager() throws RemoteConnectionException;
}
