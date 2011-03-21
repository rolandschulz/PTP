/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;

public class SMOAResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * Holds JobThread for each running job (identified by JobID), so that RM
	 * can inform JobThread to terminate the job
	 */
	private final Map<String, JobThread> jobThreadForID = new HashMap<String, JobThread>();

	public SMOAResourceManager(SMOAResourceManagerConfiguration config, SMOAResourceManagerControl control,
			SMOAResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}

	@Override
	public SMOAResourceManagerControl getControl() {
		return (SMOAResourceManagerControl) super.getControl();
	}

	@Override
	public SMOAResourceManagerConfiguration getControlConfiguration() {
		return (SMOAResourceManagerConfiguration) super.getControlConfiguration();
	}

	@Override
	public SMOAResourceManagerMonitor getMonitor() {
		return (SMOAResourceManagerMonitor) super.getMonitor();
	}

	@Override
	public SMOAResourceManagerConfiguration getMonitorConfiguration() {
		return (SMOAResourceManagerConfiguration) super.getMonitorConfiguration();
	}

	protected void addJobThread(String jobId, JobThread job) {
		jobThreadForID.put(jobId, job);
	}

	protected JobThread getJobThread(String jobId) {
		return jobThreadForID.get(jobId);
	}

	protected void removeJobThread(String jobId) {
		jobThreadForID.remove(jobId);
	}

}