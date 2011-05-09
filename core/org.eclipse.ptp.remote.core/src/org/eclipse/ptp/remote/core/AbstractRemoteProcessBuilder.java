/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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

/**
 * @since 5.0
 */
public abstract class AbstractRemoteProcessBuilder implements IRemoteProcessBuilder {
	private List<String> fCommandArgs;
	private IRemoteConnection fRemoteConnection;
	private IFileStore fRemoteDir = null;
	private boolean fRedirectErrorStream = false;

	public AbstractRemoteProcessBuilder(IRemoteConnection conn, List<String> command) {
		fRemoteConnection = conn;
		fCommandArgs = command;
	}

	public AbstractRemoteProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command()
	 */
	public List<String> command() {
		return fCommandArgs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command(java.util.List)
	 */
	public IRemoteProcessBuilder command(List<String> command) {
		fCommandArgs = command;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#command(java.lang.String
	 * )
	 */
	public IRemoteProcessBuilder command(String... command) {
		fCommandArgs = Arrays.asList(command);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#connection()
	 */
	public IRemoteConnection connection() {
		return fRemoteConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#connection(org.eclipse
	 * .ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteProcessBuilder connection(IRemoteConnection conn) {
		fRemoteConnection = conn;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#directory()
	 */
	public IFileStore directory() {
		return fRemoteDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#directory(org.eclipse
	 * .core.filesystem.IFileStore)
	 */
	public IRemoteProcessBuilder directory(IFileStore directory) {
		fRemoteDir = directory;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#environment()
	 */
	public abstract Map<String, String> environment();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#getSupportedFlags()
	 */
	/**
	 * @since 5.0
	 */
	public abstract int getSupportedFlags();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#redirectErrorStream()
	 */
	public boolean redirectErrorStream() {
		return fRedirectErrorStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder#redirectErrorStream
	 * (boolean)
	 */
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.fRedirectErrorStream = redirectErrorStream;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start()
	 */
	public IRemoteProcess start() throws IOException {
		return start(NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start(int)
	 */
	/**
	 * @since 5.0
	 */
	public abstract IRemoteProcess start(int flags) throws IOException;
}
