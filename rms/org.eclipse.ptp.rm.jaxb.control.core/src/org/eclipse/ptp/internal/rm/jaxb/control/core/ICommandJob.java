/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.remote.core.IRemoteProcess;

/**
 * Abstraction of methods common among types of CommandJobs.
 * 
 * @author arossi
 * 
 */
public interface ICommandJob {
	/**
	 * @return the status of the command just after execution
	 */
	public ICommandJobStatus getJobStatus();

	/**
	 * @return the process wrapper (CommandJob).
	 */
	public IRemoteProcess getProcess();

	/**
	 * @return object wrapping stream monitors (CommandJob).
	 */
	public ICommandJobStreamsProxy getProxy();

	/**
	 * @return the status returned by the run() method
	 */
	public IStatus getRunStatus();

	/**
	 * @return if job is active
	 */
	public boolean isActive();

	/**
	 * @return if job is batch (CommandJob).
	 */
	public boolean isBatch();

	/**
	 * Job/Thread interface
	 */
	public void join() throws InterruptedException;

	/**
	 * @return any error recorded on the streams.
	 */
	public CoreException joinConsumers();

	/**
	 * Rerun the job. This resends input to the running job.
	 */
	public void rerun();

	/**
	 * Job/Thread interface
	 */
	public void schedule();

	/**
	 * Shuts off the proxy, terminates the process and cancels the job.
	 */
	public void terminate();

	/**
	 * @return whether to wait (CommandJob).
	 */
	public boolean waitForId();
}
