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

import java.io.IOException;

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;


public interface IRemoteExecutionTools {
	/**
	 * Create a object that describes a bash script to be executed on the remote host.
	 * @return a pre-configured instance of IRemoteScript
	 */
	public IRemoteScript createScript();

	/**
	 * Execute a bash script described by an {@link IRemoteScript}.
	 * <p>
	 * The script will start executing immediately, The method does not block.
	 * <p>
	 * The {@link IRemoteScriptExecution} instance returned by this method describes the
	 * execution properties, completion status and its result (once finished).
	 * The {@link IRemoteScriptExecution} has a method for blocking the current thread
	 * until the script finishes execution and another to cancel the script individually.
	 * 
	 * @param remoteScript The {@link IRemoteScript} that describes the bash script to be executed
	 * @return a proper instance of {@link IRemoteScriptExecution}. 
	 * @throws RemoteConnectionException If not possible to request the execution on remote host. 
	 * @throws  
	 */
	public IRemoteScriptExecution executeScript(IRemoteScript remoteScript) throws RemoteExecutionException, CancelException, RemoteConnectionException;

	public RemoteProcess executeProcess(IRemoteScript remoteScript) throws RemoteExecutionException, CancelException, RemoteConnectionException;

	/**
	 * Run a command and wait until it finishes.
	 * @return command exit status.
	 */
	public int executeWithExitValue(String command) throws RemoteExecutionException, CancelException, RemoteConnectionException;

	/**
	 * Run a command and return its output.
	 * @throws IOException 
	 */
	public String executeWithOutput(String command) throws RemoteExecutionException, CancelException, RemoteConnectionException;

	/**
	 * Run a bash command. Assume that non zero exit status means error.
	 */
	public void executeBashCommand(String command) throws RemoteExecutionException, CancelException, RemoteConnectionException;
}