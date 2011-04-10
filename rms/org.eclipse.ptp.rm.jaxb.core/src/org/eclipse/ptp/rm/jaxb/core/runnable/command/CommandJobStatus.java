/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * Extension of the IJobStatus class to handle resource manager command jobs.
 * 
 * @author arossi
 * 
 */
public class CommandJobStatus implements ICommandJobStatus {

	private String jobId;
	private ILaunchConfiguration launchConfig;
	private String state;
	private ICommandJobStreamsProxy proxy;
	private IRemoteProcess process;
	private final RMVariableMap rmVarMap;
	private boolean waitEnabled;

	public CommandJobStatus() {
		jobId = null;
		state = IJobStatus.UNDETERMINED;
		rmVarMap = RMVariableMap.getActiveInstance();
		waitEnabled = true;
	}

	/**
	 * @param jobId
	 *            either internal UUID or resource-specific id
	 * @param state
	 */
	public CommandJobStatus(String jobId, String state) {
		this.jobId = jobId;
		this.state = state;
		rmVarMap = RMVariableMap.getActiveInstance();
		waitEnabled = false;
	}

	/**
	 * Closes the proxy and calls destroy on the process. Used for interactive
	 * jobs cancellation.
	 */
	public synchronized void cancel() {
		if (proxy != null) {
			proxy.close();
		}
		if (process != null) {
			process.destroy();
		}
	}

	/**
	 * Notifies all callers of <code>waitForId</code> to exit wait.
	 */
	public void cancelWait() {
		synchronized (this) {
			waitEnabled = false;
			notifyAll();
		}
	}

	/**
	 * @return jobId either internal UUID or resource-specific id
	 */
	public synchronized String getJobId() {
		return jobId;
	}

	/**
	 * @return configuration used for this submission.
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfig;
	}

	/**
	 * @return state of the job (not of the submission process).
	 */
	public synchronized String getState() {
		return state;
	}

	/**
	 * @return more specific state identifier.
	 */
	public synchronized String getStateDetail() {
		return state;
	}

	/**
	 * Wrapper containing monitoring functionality for the associated output and
	 * error streams.
	 */
	public IStreamsProxy getStreamsProxy() {
		return proxy;
	}

	/**
	 * @return whether a process object has been attached to this status object
	 *         (in which case the submission is not through an asynchronous job
	 *         scheduler).
	 */
	public boolean isInteractive() {
		return process != null;
	}

	/**
	 * @param launchConfig
	 *            configuration used for this submission.
	 */
	public void setLaunchConfig(ILaunchConfiguration launchConfig) {
		this.launchConfig = launchConfig;
	}

	/**
	 * @param remote
	 *            process object (used for interactive cancellation)
	 */
	public void setProcess(IRemoteProcess process) {
		this.process = process;
	}

	/**
	 * @param proxy
	 *            Wrapper containing monitoring functionality for the associated
	 *            output and error streams.
	 */
	public void setProxy(ICommandJobStreamsProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * @param state
	 *            of the job (not of the submission process).
	 */
	public synchronized void setState(String state) {
		this.state = state;
	}

	/**
	 * Called by the resource manager when the (batch) job state changes to
	 * RUNNING.
	 */
	public synchronized void startProxy() {
		if (proxy != null) {
			proxy.startMonitors();
		}
	}

	/**
	 * Wait until the jobId has been set on the job id property in the
	 * environment.
	 * 
	 * @param uuid
	 *            key for the property containing as its name the
	 *            resource-specific jobId and as its value its initial state
	 *            (SUBMITTED)
	 */
	public void waitForJobId(String uuid) {
		synchronized (this) {
			while (waitEnabled && jobId == null) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
				}
				Property p = (Property) rmVarMap.get(uuid);
				if (p != null) {
					jobId = p.getName();
					String v = (String) p.getValue();
					if (v != null) {
						state = v;
					}
				}
			}
		}
	}
}
