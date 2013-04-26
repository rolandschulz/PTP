/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.core.IAuthInfo;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

/**
 * Controls an instance of a target created from the Environment.
 * 
 * @author Ricardo M. Matinata, Daniel Felix Ferber, Richard Maciel
 * @since 1.1
 */
public interface ITargetControl {
	public class TargetSocket {
		public String host;
		public int port;
	}

	/**
	 * Called to create an instance of the target.
	 * 
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException;

	/**
	 * Called to create an instance of the target. Allow client to supply auth info.
	 * 
	 * @param authInfo
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean create(IAuthInfo authInfo, IProgressMonitor monitor) throws CoreException;

	/**
	 * Create a new execution manager. This is required for script execution
	 * because KillableExecution closes the channel after execution.
	 * 
	 * @return new execution manager
	 * @since 2.0
	 */
	public IRemoteExecutionManager createExecutionManager() throws RemoteConnectionException;

	/**
	 * Create a bridge to a listening socket on the target environment.
	 * 
	 * @throws CoreException
	 */
	public TargetSocket createTargetSocket(int port) throws CoreException;

	/**
	 * Called to remove information associated with the instance of the target
	 * because the it was removed.
	 * 
	 * @throws CoreException
	 */
	public void destroy() throws CoreException;

	/**
	 * Get the target configuration information
	 * 
	 * @return ITargetConfig
	 * @since 2.0
	 */
	public ITargetConfig getConfig();

	/**
	 * Get the main execution manager for this control.
	 * 
	 * @return execution manager
	 * @since 2.0
	 */
	public IRemoteExecutionManager getExecutionManager();

	/**
	 * Called to halt the instance of the target.
	 * 
	 * @throws CoreException
	 * @since 2.0
	 */
	public void kill() throws CoreException;

	/**
	 * Query the status of the target connection
	 * 
	 * @return status of connection as specified by {@link ITargetStatus}
	 */
	public int query();

	/**
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean resume(IProgressMonitor monitor) throws CoreException;

	/**
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean stop(IProgressMonitor monitor) throws CoreException;

	/**
	 * This method is a callback called when the ITargetControl implementing
	 * class must update its configuration because an external change occurred.
	 * 
	 * @throws CoreException
	 */
	public void updateConfiguration() throws CoreException;
}