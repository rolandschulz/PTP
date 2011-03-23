/*******************************************************************************
 * Copyright (c) 2011 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core;

import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * 
 * Interface to a remote sync connection. Defines basic operations that should be provided, most importantly the ability to sync in
 * either direction (local to remote or remote to local).
 * 
 */
public interface IRemoteSyncConnection {
	/**
	 * Close the connection
	 * 
	 * @param connection
	 * @throws RemoteSyncException
	 */
	public void close() throws RemoteSyncException;

	/**
	 * 
	 * @return connection
	 */
	public IRemoteConnection getConnection();

	/**
	 * 
	 * @return local directory
	 */
	public String getLocalDirectory();

	/**
	 * 
	 * @return remote host
	 */
	public String getRemoteDirectory();

	/**
	 * Synchronize local to remote
	 * 
	 * @param connection
	 * @throws RemoteSyncException
	 */
	public void syncLocalToRemote() throws RemoteSyncException;

	/**
	 * Synchronize remote to local
	 * 
	 * @param connection
	 * @throws RemoteSyncException
	 */
	public void syncRemoteToLocal() throws RemoteSyncException;
}
