/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.core;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * An object which provides access to an environment management system on a remote machine.
 * <p>
 * All contributions to the {@value #ENV_MANAGER_EXTENSION_POINT_ID} extension point must implement this interface.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public interface IEnvManager {

	/** ID for the envmanager extension point */
	public static final String ENV_MANAGER_EXTENSION_POINT_ID = "org.eclipse.ptp.ems.core.envmanager"; //$NON-NLS-1$

	/**
	 * Returns a human-readable name for this environment management system.
	 * 
	 * @return String (non-<code>null</code>)
	 */
	String getName();

	/**
	 * Returns a {@link Comparator} used to sort the strings returned by {@link #determineAvailableElements()} and
	 * {@link #determineDefaultElements()} when displaying them to the user.
	 * 
	 * @return {@link Comparator} (non-<code>null</code>)
	 */
	public Comparator<String> getComparator();

	/**
	 * Returns a short sentence that will be displayed to the user to request that items be selected from a checklist.
	 * <p>
	 * For example, &quot;Select modules to be loaded.&quot;
	 * 
	 * @return String (non-<code>null</code>)
	 */
	public String getInstructions();

	/**
	 * Sets the {@link IRemoteServices} and {@link IRemoteConnection} which will be used to run commands on a remote machine.
	 * <p>
	 * This method must be invoked before {@link #checkForCompatibleInstallation()}, {@link #getDescription()},
	 * {@link #determineAvailableElements()}, {@link #determineDefaultElements()}, or {@link #createBashScript(Set, String)}.
	 * 
	 * @param remoteServices
	 *            {@link IRemoteServices} (non-<code>null</code>)
	 * @param remoteConnection
	 *            {@link IRemoteConnection} (non-<code>null</code>)
	 */
	void configure(IRemoteServices remoteServices, IRemoteConnection remoteConnection);

	/**
	 * Returns true iff the remote machine is running an environment management system supported by this {@link IEnvManager}.
	 * 
	 * @return true iff the remote machine is running an environment management system supported by this {@link IEnvManager}
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteServices, IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public boolean checkForCompatibleInstallation() throws RemoteConnectionException, IOException;

	/**
	 * If the remote machine is running an environment management system supported by this {@link IEnvManager}, returns a short
	 * description of the environment management system (e.g., &quot;Modules 3.2.7&quot;); otherwise, returns <code>null</code>.
	 * 
	 * @return a short, human-readable description of the environment configuration system (e.g., &quot;SoftEnv 1.6.2&quot;), or
	 *         <code>null</code> if a compatible environment configuration system is not present on the remote machine
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteServices, IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public String getDescription() throws RemoteConnectionException, IOException;

	/**
	 * Returns the set of all environment configuration elements available on the remote machine (e.g., the result of
	 * <tt>module -t avail</tt>).
	 * 
	 * @return unmodifiable Set (non-<code>null</code>)
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteServices, IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public Set<String> determineAvailableElements() throws RemoteConnectionException, IOException;

	/**
	 * Returns the set of all environment configuration elements loaded by default upon login (e.g., the result of
	 * <tt>module -t list</tt> in a login shell).
	 * 
	 * @return unmodifiable Set (non-<code>null</code>)
	 * 
	 * @throws NullPointerException
	 *             if {@link #configure(IRemoteServices, IRemoteConnection)} has not been called
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public Set<String> determineDefaultElements() throws RemoteConnectionException, IOException;

	/**
	 * Returns a single Bash shell command which will configure the remote environment with the given elements and then execute the
	 * given command.
	 * <p>
	 * The returned command may include sequencing, piping, I/O redirection, etc.; however, it must be possible to concatenate
	 * additional Bash commands by appending a semicolon.
	 * 
	 * @param separator
	 *            string that will be inserted between consecutive Bash commands: typically, either a semicolon or a newline
	 * @param echo
	 *            true iff the script should &quot;echo&quot; each command prior to execution
	 * @param config
	 *            environment manager configuration (non-<code>null</code>)
	 * @param commandToExecuteAfterward
	 *            a Bash shell command to execute after the environment has been configured
	 * 
	 * @return a single Bash shell command which will configure the remote environment with the given elements and then execute the
	 *         given command (non-<code>null</code>)
	 */
	public String getBashConcatenation(String separator, boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward);

	/**
	 * Creates a temporary file on the remote machine and writes a Bash shell script into that file which will configure the remote
	 * environment with the given elements, execute the given command, and then delete the temporary file (shell script).
	 * 
	 * @param echo
	 *            true iff the script should &quot;echo&quot; each command prior to execution
	 * @param config
	 *            environment manager configuration (non-<code>null</code>)
	 * @param commandToExecuteAfterward
	 *            a Bash shell command to execute after the environment has been configured
	 * 
	 * @return path to the shell script on the remote machine (non-<code>null</code>)
	 */
	public String createBashScript(boolean echo, IEnvManagerConfig config, String commandToExecuteAfterward)
			throws RemoteConnectionException, IOException;
}
