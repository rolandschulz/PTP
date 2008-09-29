/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/

package org.eclipse.ptp.cell.utils.process;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * The extra complexity in this class (it is a thread that sleeps till timeout
 * and has a Job to watch for user cancelation of the process) is necessary
 * because, in some cases, the class that uses ProcessControler should want to
 * place a process.waitFor(). In this case, we need to have a separate thread
 * and a separate Job to do this work because having just a Job didn't worked
 * when ProcessControler is used when Eclipse is being launched (it seems that
 * Eclipse is unable to create a new Job while blocked in process.waitFor() due
 * to resources shortage during startup.
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class ProcessController extends Thread {

	class UserControl extends Job {

		private Thread parentThread;

		private long timeSlot;

		public UserControl(String processName, Thread parentThread,
				long timeSlot) {
			super(processName);
			this.parentThread = parentThread;
			this.timeSlot = timeSlot;
		}

		private boolean keepRunning = true;
		private boolean doCancel = false;
		
		synchronized void stopNow() {
			keepRunning = false;
		}
		
		public synchronized boolean isKeepRunning() {
			return keepRunning;
		}
		
		public boolean isDoCancel() {
			return doCancel;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Start UserControl."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			while (isKeepRunning()) {
				if (monitor != null && monitor.isCanceled()) {
					Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "UserControl cancelled by user."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					doCancel = true;
					this.parentThread.interrupt();
					return Status.CANCEL_STATUS;
				}
				try {
					process.exitValue();
					Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Detected that process terminated."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					this.parentThread.interrupt();
					return Status.OK_STATUS;
				} catch (IllegalThreadStateException e) {
					//	Ignore it.
				}
				try {
					Thread.sleep(this.timeSlot);
				} catch (InterruptedException ie) {
					// Ignore it
				}
			}
			Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Finished UserControl."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return Status.OK_STATUS;
		}

	}

	protected static final long TIME_SLOT = 500;

	private String processName;

	private Process process;

	private long timeout;

	/**
	 * 
	 */
	public ProcessController(String processName, Process process, long timeout) {
		this.processName = processName;
		this.process = process;
		this.timeout = timeout;
		Debug.read();
	}

	public void run() {
		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Started ProcessController.run()."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UserControl userControl = new UserControl(this.processName, Thread.currentThread(), TIME_SLOT);
		userControl.schedule();
		Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "UserControl scheduled."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		try {
			Thread.sleep(this.timeout);
			Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Timed out."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				process.exitValue();
			} catch (IllegalThreadStateException e) {
				this.process.destroy();
			}
		} catch (InterruptedException ie) {
			// ignore it
		} finally {
			userControl.stopNow();
			if (userControl.isDoCancel()) {
				Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + " killed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				this.process.destroy();
			}		
		}
		Debug.POLICY.trace(Debug.DEBUG_PROCESS, "("+process.hashCode()+")" + "Finished ProcessController.run()."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}

}
