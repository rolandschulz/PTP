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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

public abstract class AbstractRemoteProcessBuilder implements IRemoteProcessBuilder {
	private List<String> commandArgs;
	private IRemoteConnection remoteConnection;
	private IFileStore remoteDir = null;
	private boolean redirectErrorStream = false;
	
	public AbstractRemoteProcessBuilder(IRemoteConnection conn, List<String> command) {
		remoteConnection = conn;
		commandArgs = command;
	}
	
	public AbstractRemoteProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#connection()
	 */
	public IRemoteConnection connection() {
		return remoteConnection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#connection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteProcessBuilder connection(IRemoteConnection conn) {
		remoteConnection = conn;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command()
	 */
	public List<String> command() {
		return commandArgs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command(java.util.List)
	 */
	public IRemoteProcessBuilder command(List<String> command) {
		commandArgs = command;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command(java.lang.String)
	 */
	public IRemoteProcessBuilder command(String... command) {
		commandArgs = Arrays.asList(command);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#directory()
	 */
	public IFileStore directory() {
		return remoteDir;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#directory(org.eclipse.core.filesystem.IFileStore)
	 */
	public IRemoteProcessBuilder directory(IFileStore directory) {
		remoteDir = directory;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#environment()
	 */
	public abstract Map<String, String> environment();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#redirectErrorStream()
	 */
	public boolean redirectErrorStream() {
		return redirectErrorStream;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#redirectErrorStream(boolean)
	 */
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.redirectErrorStream = redirectErrorStream;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start()
	 */
	public abstract IRemoteProcess start() throws IOException;
}
