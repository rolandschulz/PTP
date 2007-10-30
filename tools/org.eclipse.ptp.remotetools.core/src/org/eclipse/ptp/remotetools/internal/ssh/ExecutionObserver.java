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
import org.eclipse.ptp.remotetools.internal.core.ConnectionProperties;


/**
 * Observer responsible for updating the list of running process.
 * 
 * @author Richard Maciel
 * @since 1.1
 */
class ExecutionObserver extends Job {
	int inactivity;
	Connection connection;

	public ExecutionObserver(Connection connection) {
		super(Messages.ExecutionObserver_ExecutionObserver_RemoteCommandObserver);
		this.connection = connection;
	}

	/**
	 * (re)Enable polling for finished operations.
	 */
	public void newCommand() {
		inactivity = 0;
		schedule(ConnectionProperties.fastDutyCycle);
	}

	/**
	 * Check for process that finished and remove then from the table until there are no more process.
	 * 
	 * @param monitor
	 * @return Status of the run.
	 */
	protected IStatus run(IProgressMonitor monitor) {
		// In case of cancel...
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		synchronized (connection) {
			Iterator operationEnum = connection.activeProcessTable.entrySet().iterator();

			while (operationEnum.hasNext()) {
				Object o = operationEnum.next();
				Entry e = (Entry) o;
				
				
				if (e.getValue() instanceof KillableExecution) {
					KillableExecution operation = (KillableExecution) e.getValue();

					if (!operation.isRunning()) {
						operationEnum.remove();
	
						// Notify threads that were
						operation.notifyFinish();
	
						inactivity = 0;
					}
				}
			}

			// Only schedules it if there exists more items in the table.
			if (connection.activeProcessTable.size() > 0) {
				if (inactivity > ConnectionProperties.inactivityThreashold) {
					schedule(ConnectionProperties.longDutyCycle);
				} else {
					schedule(ConnectionProperties.fastDutyCycle);
					inactivity++;
				}
			}
		}

		return Status.OK_STATUS;
	}

}