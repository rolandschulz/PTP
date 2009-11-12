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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Observer responsible for updating the list of running process.
 * 
 * @author Richard Maciel
 * @since 1.1
 */
class ExecutionObserver extends Job {
	private Connection fConnection;

	public ExecutionObserver(Connection connection) {
		super(
				Messages.ExecutionObserver_ExecutionObserver_RemoteCommandObserver);
		fConnection = connection;
	}
	
	/**
	 * Check for process that finished and remove then from the table until
	 * there are no more process.
	 * 
	 * @param monitor
	 * @return Status of the run.
	 */
	protected IStatus run(IProgressMonitor monitor) {
		while (!monitor.isCanceled()) {
			KillableExecution finished = null;
			if (fConnection.isConnected()
					&& fConnection.getActiveProcessTable() != null) {
				synchronized (fConnection) {
					Iterator<Entry<Integer, KillableExecution>> iterator = fConnection
							.getActiveProcessTable().entrySet().iterator();

					while (iterator.hasNext()) {
						Entry<Integer, KillableExecution> entry = iterator
								.next();

						if (!entry.getValue().isRunning()) {
							finished = entry.getValue();
							break;
						}
					}
				}
			}
			if (finished != null) {
				// Notify threads that were
				finished.notifyFinish();
				finished = null;
			} else {
				synchronized (fConnection) {
					try {
						fConnection.wait(100);
					} catch (InterruptedException e) {
					}
				}
			}

		}
		return Status.OK_STATUS;
	}

}