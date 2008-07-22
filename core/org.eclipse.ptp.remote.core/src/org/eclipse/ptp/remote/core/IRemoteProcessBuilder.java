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
package org.eclipse.ptp.remote.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

public interface IRemoteProcessBuilder {

	/**
	 * Returns this process builder's connection.
	 * 
	 * @return the connection used by this process builder
	 */
	public abstract IRemoteConnection connection();

	/**
	 * Sets this process builder's connection.
	 * 
	 * @return This process builder
	 */
	public abstract IRemoteProcessBuilder connection(
			IRemoteConnection conn);

	/**
	 * Returns this process builder's operating system program and arguments.
	 * 
	 * @return a list containing the program and arguments
	 */
	public abstract List<String> command();

	/**
	 * Sets this process builder's operating system program and arguments.
	 * 
	 * @param command
	 * @return This process builder
	 */
	public abstract IRemoteProcessBuilder command(List<String> command);

	/**
	 * Sets this process builder's operating system program and arguments.
	 * 
	 * @param command
	 * @return this process builder
	 */
	public abstract IRemoteProcessBuilder command(String... command);

	/**
	 * Returns this process builder's working directory.
	 * 
	 * @return an IFileStore reference to the working directory
	 */
	public abstract IFileStore directory();

	/**
	 * Sets this process builder's working directory.
	 * 
	 * @param directory
	 * @return This process builder
	 */
	public abstract IRemoteProcessBuilder directory(IFileStore directory);

	/**
	 * Returns a string map view of this process builder's environment. The returned map
	 * behaves in the same manner as described in {@link java.lang.ProcessBuilder#environment()}.
	 * 
	 * @return the process builder's environment
	 */
	public abstract Map<String, String> environment();

	/**
	 * Tells whether this process builder merges standard error and standard
	 * output.
	 * 
	 * @return true if standard error and standard output will be merged
	 */
	public abstract boolean redirectErrorStream();

	/**
	 * Sets this process builder's redirectErrorStream property.
	 * 
	 * @param redirectErrorStream
	 * @return This process builder
	 */
	public abstract IRemoteProcessBuilder redirectErrorStream(
			boolean redirectErrorStream);

	/**
	 * Starts a new process using the attributes of this process builder.
	 * 
	 * @return remote process object
	 * @throws IOException
	 */
	public abstract IRemoteProcess start() throws IOException;
}