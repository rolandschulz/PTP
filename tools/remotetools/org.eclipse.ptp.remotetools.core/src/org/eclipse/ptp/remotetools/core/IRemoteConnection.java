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

import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


/**
 * A connection to a remote host. Serves as a starting point for creating
 * {@link IRemoteExecutionManager}, that to operations on the remote host.
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.1
 * <b>Review OK</b>
 */
public interface IRemoteConnection
{
	void connect() throws RemoteConnectionException;

	/**
	 * Terminate connection to remote machine.
	 * <p>
	 * TODO: Decide: Block until all pending operations are finished or cancels
	 * pending operations?
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
	 * @throws RemoteConnectionException if the {@link IRemoteExecutionManager} could not be created.
	 */
	public IRemoteExecutionManager createRemoteExecutionManager() throws RemoteConnectionException;
}	
