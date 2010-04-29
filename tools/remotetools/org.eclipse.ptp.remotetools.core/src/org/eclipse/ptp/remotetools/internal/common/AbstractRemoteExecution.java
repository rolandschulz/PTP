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
package org.eclipse.ptp.remotetools.internal.common;

import org.eclipse.ptp.remotetools.core.IRemoteOperation;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.ssh.ExecutionManager;

/**
 * @author Richard Maciel, Daniel Felix Ferber
 */
public abstract class AbstractRemoteExecution implements IRemoteOperation {
	/**
	 * The execution manager that created the execution.
	 */
	private ExecutionManager executionManager;
	/**
	 * The execution was canceled by the manager.
	 */
	protected boolean cancelled;
	/**
	 * The execution finished by itself.
	 */
	protected boolean finished;

	/**
	 * Default constructor.
	 * <p>
	 * It is protected because only the RemoteExecutionManager can instantiate
	 * it and pass itself as the parameters to the constructor.
	 * 
	 * @param remExecManager
	 * @throws RemoteConnectionException
	 */
	public AbstractRemoteExecution(ExecutionManager executionManager)
			throws RemoteConnectionException {
		this.executionManager = executionManager;
		this.cancelled = false;
		this.finished = false;
	}

	public final synchronized void cancel() {
		notifyCancel();
	}

	public void close() {
	}

	public ExecutionManager getExecutionManager() {
		return executionManager;
	}

	protected synchronized void notifyCancel() {
		/*
		 * By default, no logic is implemented to cancel the execution. Only
		 * sets the cancel flag. Notifies all waiting thread that the execution
		 * was canceled.
		 */
		cancelled = true;
		this.notifyAll();
	}

	protected synchronized void notifyFinish() {
		/*
		 * By default, no logic is implemented to finish the execution. Notifies
		 * all waiting thread that the execution finished by itself.
		 */
		finished = true;
		this.notifyAll();
	}

	public synchronized void waitForEndOfExecution()
			throws RemoteConnectionException, CancelException {
		/*
		 * Block until the execution finishes or is canceled.
		 */
		while (!finished && !cancelled) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // set current thread flag
				// Ignore spurious interrupts
			}
		}
		if (wasCanceled()) {
			throw new CancelException();
		}
	}

	public synchronized boolean wasCanceled() {
		return cancelled;
	}

	public synchronized boolean wasFinished() {
		return finished;
	}
}
