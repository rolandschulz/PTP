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

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.LocalPortBoundException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


/**
 * Provides service to run commands on the remote host and to transfer files from/to the remote host. 
 * <p>>
 * Adds functionality for blocking operations until they finish execution.
 * Allows canceling the hole manager, that will cancel all current operation by raising CancelException
 * on each of them..
 * <p>
 * All paths MUST be specified as full absolute paths.
 * <p>
 * 
 * @author Richard Maciel, Daniel F. Ferber
 * @since 1.1
 */
public interface IRemoteExecutionManager {
	/**
	 * Cancels the all current scripts on execution at the remote machine that were created by this manager.
	 * Also cancels current file transfer operations.
	 * Does not allow any further operations.
	 */
	public void cancel();
	/**
	 * Removes the cancel conditions and allows further operations again.
	 */
	public void resetCancel();
	
	/**
	 * Free allocated resources for the execution.
	 */
	public void close();

	public IRemoteFileTools getRemoteFileTools() throws RemoteConnectionException;
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException;
	public IRemoteExecutionTools getExecutionTools() throws RemoteConnectionException;
	
	public IRemotePathTools getRemotePathTools();
	
	public IRemoteTunnel createTunnel(int localPort, String addressOnRemoteHost, int portOnRemoteHost) throws RemoteConnectionException, LocalPortBoundException, CancelException;
	
	public IRemoteStatusTools getRemoteStatusTools() throws RemoteConnectionException;
	
	/**
	 * Create and bind a tunnel from localhost port to a given remotehost port.
	 * This methods allocs the localhost port automatically.
	 * 
	 * @param addressOnRemoteHost Remote Host address
	 * @param portOnRemoteHost Remote Host port
	 * @return An IRemoteTunnel object representing the created tunnel
	 * @throws RemoteConnectionException
	 * @throws LocalPortBoundException No local port available to connect.
	 * @throws CancelException 
	 */
	public IRemoteTunnel createTunnel(String addressOnRemoteHost, int portOnRemoteHost) throws RemoteConnectionException, LocalPortBoundException, CancelException;
	public void releaseTunnel(IRemoteTunnel tunnel) throws RemoteConnectionException;

}
