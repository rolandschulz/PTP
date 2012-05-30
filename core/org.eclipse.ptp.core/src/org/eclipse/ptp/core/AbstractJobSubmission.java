/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class to manage coordination of job submission with receipt of the corresponding event.
 * 
 * Because model IDs are always server generated, we require some way to link a job submission with the corresponding INewJobEvent,
 * which is done using the jobSubID attribute. However, it is often necessary to wait for the event to be received without blocking
 * the UI. This class provides a waitFor method for this purpose.
 * 
 * The INewJobEvent handler should set the status to SUBMITTED once the event is received.
 * 
 * @since 5.0
 */
@Deprecated
public abstract class AbstractJobSubmission {
	/**
	 * Status of the job submission (NOT the job itself)
	 */
	public enum JobSubStatus {
		/**
		 * @since 4.0
		 */
		UNSUBMITTED,
		SUBMITTED,
		/**
		 * @since 5.0
		 */
		CANCELLED,
		ERROR
	}

	private final String fId;
	private String fError = null;
	private JobSubStatus fStatus = JobSubStatus.UNSUBMITTED;
	private final ReentrantLock fSubLock = new ReentrantLock();
	private final Condition fSubCondition = fSubLock.newCondition();

	public AbstractJobSubmission(int count) {
		fId = "JOB_" + Long.toString(System.currentTimeMillis()) + Integer.toString(count); //$NON-NLS-1$
	}

	public AbstractJobSubmission(String id) {
		fId = id;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return fError;
	}

	/**
	 * @return the job submission id
	 */
	public String getId() {
		return fId;
	}

	/**
	 * set the error
	 */
	public void setError(String error) {
		fError = error;
		setStatus(JobSubStatus.ERROR);
	}

	/**
	 * set the current status
	 */
	public void setStatus(JobSubStatus status) {
		fSubLock.lock();
		try {
			fStatus = status;
			fSubCondition.signalAll();
		} finally {
			fSubLock.unlock();
		}
	}

	/**
	 * Wait for the job state to change
	 * 
	 * @return the state
	 */
	public JobSubStatus waitFor(IProgressMonitor monitor) {
		fSubLock.lock();
		try {
			while (!monitor.isCanceled() && fStatus == JobSubStatus.UNSUBMITTED) {
				try {
					fSubCondition.await(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is canceled
				}
			}

			if (monitor.isCanceled()) {
				setStatus(JobSubStatus.CANCELLED);
			}

			return fStatus;
		} finally {
			fSubLock.unlock();
		}
	}

}
