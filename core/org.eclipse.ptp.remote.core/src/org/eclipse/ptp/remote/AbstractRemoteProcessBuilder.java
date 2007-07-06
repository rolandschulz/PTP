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
package org.eclipse.ptp.remote;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.filesystem.IFileStore;

public abstract class AbstractRemoteProcessBuilder implements IRemoteProcessBuilder {
	private List<String> commandArgs;
	private IRemoteConnection remoteConnection;
	private IFileStore remoteDir;
	private Map<String, String> remoteEnv;
	private boolean redirectErrorStream;
	private IRemoteProcess process;
	private boolean error;
	private final ReentrantLock threadLock = new ReentrantLock();;
	private final Condition threadCondition = threadLock.newCondition();
	
	public AbstractRemoteProcessBuilder(IRemoteConnection conn, List<String> command) {
		remoteConnection = conn;
		commandArgs = command;
		remoteDir = null;
		remoteEnv = new HashMap<String, String>();
		remoteEnv.putAll(System.getenv());
		redirectErrorStream = false;
	}
	
	public AbstractRemoteProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#connection()
	 */
	public IRemoteConnection connection() {
		return remoteConnection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#connection(org.eclipse.ptp.remote.IRemoteConnection)
	 */
	public IRemoteProcessBuilder connection(IRemoteConnection conn) {
		remoteConnection = conn;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#command()
	 */
	public List<String> command() {
		return commandArgs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#command(java.util.List)
	 */
	public IRemoteProcessBuilder command(List<String> command) {
		commandArgs = command;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#command(java.lang.String)
	 */
	public IRemoteProcessBuilder command(String... command) {
		commandArgs = Arrays.asList(command);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#directory()
	 */
	public IFileStore directory() {
		return remoteDir;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#directory(org.eclipse.core.filesystem.IFileStore)
	 */
	public IRemoteProcessBuilder directory(IFileStore directory) {
		remoteDir = directory;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#environment()
	 */
	public Map<String, String> environment() {
		return remoteEnv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#redirectErrorStream()
	 */
	public boolean redirectErrorStream() {
		return redirectErrorStream;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#redirectErrorStream(boolean)
	 */
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.redirectErrorStream = redirectErrorStream;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#asyncStart()
	 */
	public IRemoteProcess asyncStart() throws IOException {
		threadLock.lock();
		try {
			if (process != null) {
				throw new IOException("Process is already running");
			}
			error = false;
		} finally {
			threadLock.unlock();
		}

		Thread runThread = new Thread(new Runnable() {
			public void run() {
				// Start process
				threadLock.lock();
				try {
					process = start();
					threadCondition.signal();
				} catch (IOException e) {
					error = true;
				} finally {
					threadLock.unlock();
				}
				
				// Wait for process to terminate
				if (!error) {
					try {
						process.waitFor();
					} catch (InterruptedException e) {
					}
					threadLock.lock();
					try {
						process = null;
					} finally {
						threadLock.unlock();
					}
				}
			}
		});
		
		runThread.start();
		
		threadLock.lock();
		try {
			while (process == null && !error) {
				try {
					threadCondition.await();
				} catch (InterruptedException e) {
				}
			}
		} finally {
			threadLock.unlock();
		}
		
		if (error) {
			return null;
		}
		
		return process;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#start()
	 */
	public abstract IRemoteProcess start() throws IOException;
}
