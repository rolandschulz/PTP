/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


/**
 * Standard implementation of <code>ITargetControlledJob</code>.
 * @see ITargetControlledJob
 * @author Daniel Felix Ferber
 * @since 1.2
 */
class TargetControlledJob {
	
	protected static final int UNDEFINED = 0;
	protected static final int RUNNING = 1;
	protected static final int FINISHED = 2;
	
	JobRunner jobRunner;
	IRemoteExecutionManager executionManager;
	ITargetJob job;
	SSHTargetControl targetControl;
	int status = UNDEFINED;
	
	public TargetControlledJob(SSHTargetControl controller, ITargetJob job) throws CoreException {
		super();
		this.targetControl = controller;
		this.job = job;
		try {
			this.executionManager = controller.createRemoteExecutionManager();
		} catch (RemoteConnectionException e) {
			CoreException ce = new CoreException(new Status(IStatus.ERROR,
					EnvironmentPlugin.getUniqueIdentifier(), 0, e
							.getLocalizedMessage(), e));
			throw ce;
		}
		this.jobRunner = new JobRunner(this);
	}

	protected void start() {
		jobRunner.start();
	}
	
	protected class JobRunner extends Thread {
		private TargetControlledJob controller;
		
		protected JobRunner(TargetControlledJob controller) {
			this.controller = controller;
		}
		public void run() {
			controller.targetControl.notifyStartingJob(job);
			synchronized (controller) {
				controller.status = RUNNING;
			}
			try {
				job.run(controller.executionManager);
			} finally {
				synchronized (controller) {
					controller.status = FINISHED;
					controller.executionManager.close();
					controller.targetControl.notifyFinishedJob(job);
				}
			}
		}
	}

	public ITargetJob getJob() {
		return job;
	}

	public synchronized boolean isFinished() {
		return status == FINISHED;
	}
	
	public synchronized void cancelExecution() {
		if (status != FINISHED) {
			executionManager.cancel();
		}
	}
}
