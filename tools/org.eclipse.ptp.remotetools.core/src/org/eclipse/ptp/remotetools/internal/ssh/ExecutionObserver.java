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

import java.util.Iterator;
import java.util.Map.Entry;

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
	}

	/**
	 * Check for process that finished and remove then from the table until
	 * there are no more process.
	 */
	@Override
	public void run() {
		while (!fCanceled) {
			KillableExecution finished = null;
			if (fConnection.isConnected() && fConnection.getActiveProcessTable() != null) {
				synchronized (fConnection) {
					Iterator<Entry<Integer, KillableExecution>> iterator = fConnection.getActiveProcessTable().entrySet()
							.iterator();

					while (iterator.hasNext()) {
						Entry<Integer, KillableExecution> entry = iterator.next();

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
						// Safe to ignore
					}
				}
			}

		}
	}

}