/******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.remotetools.core.messages.Messages;

/**
 * Observer responsible for updating the list of running process.
 * 
 * Note: This must be a Thread not a Job as it may be run very early on in
 * Eclipse startup.
 * 
 * @author Richard Maciel
 * @since 1.1
 */
public class ExecutionObserver extends Thread {
	private final Connection fConnection;
	private boolean fCanceled = false;

	public ExecutionObserver(Connection connection) {
		super(Messages.ExecutionObserver_ExecutionObserver_RemoteCommandObserver);
		fConnection = connection;
	}

	public void cancel() {
		fCanceled = true;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Check for executions and notify appropriately.
	 * 
	 * @param cancel
	 *            if true, cancel all running executions, otherwise notify any
	 *            finished executions
	 */
	private void checkAndNotify(boolean cancel) {
		List<KillableExecution> execs;
		synchronized (fConnection.getActiveProcessTable()) {
			execs = new ArrayList<KillableExecution>(fConnection.getActiveProcessTable().values());
		}
		for (KillableExecution exec : execs) {
			if (cancel && exec.isRunning()) {
				exec.notifyCancel();
			} else if (!cancel && !exec.isRunning()) {
				exec.notifyFinish();
			}
		}
	}

	/**
	 * Check for process that finished and remove then from the table until
	 * there are no more process.
	 */
	@Override
	public void run() {
		while (!fCanceled && fConnection.isConnected()) {
			checkAndNotify(false);
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					// Safe to ignore
				}
			}
		}
		checkAndNotify(true);
	}
}
