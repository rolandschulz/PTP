/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIRuntimeSystem extends AbstractToolRuntimeSystem {

	private Parameters params = new Parameters();

	/** The machine where open mpi is running on. */
	private String machineID;
	/** The queue that dispatches jobs to mpi. */
	private String queueID;
	/** List of hosts discovered for the machine. */
	private OpenMPIHostMap hostMap;
	/** Mapping of discovered hosts and their ID for IPHost elements. */
	private Map<String,String> hostToElementMap = new HashMap<String, String>();

	public OpenMPIRuntimeSystem(Integer openmpi_rmid,
			OpenMPIResourceManagerConfiguration config,
			AttributeDefinitionManager attrDefMgr) {
		super(openmpi_rmid, config, attrDefMgr);
	}

	protected void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	protected void setQueueID(String queueID) {
		this.queueID = queueID;
	}

	public String getMachineID() {
		return machineID;
	}

	public String getQueueID() {
		return queueID;
	}

	public Parameters getParameters() {
		return params;
	}

	public String getNodeIDforName(String hostname) {
		return hostToElementMap.get(hostname);
	}

	public OpenMPIHostMap getHostMap() {
		return hostMap;
	}

	protected void setHostMap(OpenMPIHostMap hostMap) {
		this.hostMap = hostMap;
	}

	public Map<String, String> getHostToElementMap() {
		return hostToElementMap;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doShutdown(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doShutdown(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartEvents()
	 */
	@Override
	protected void doStartEvents() throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStopEvents()
	 */
	@Override
	protected void doStopEvents() throws CoreException {
		// Nothing to do
	}

	@Override
	protected Job createDiscoverJob() {
		if (! rmConfiguration.hasDiscoverCmd())
			return null;
		Job job = new OpenMPIDiscoverJob(this);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
	}

	/**
	 * Creates a job that periodically monitors the remote machine. The default implementation runs the periodic monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	@Override
	protected Job createPeriodicMonitorJob() {
		//		if (! rmConfiguration.hasPeriodicMonitorCmd()) {
		//			return null;
		//		}
		//		Job job = new CommandJob(NLS.bind("Periodic monitor on {0}", rmConfiguration.getName()),
		//				rmConfiguration.getPeriodicMonitorCmd(),
		//				"Interrupted while running periodic monitor command.",
		//				"Failed to create remote process for periodic monitor command.",
		//				"Failed to parse output of periodic monitor command.",
		//				rmConfiguration.getPeriodicMonitorTime()) {
		//
		//					@Override
		//					protected void parse(BufferedReader output) throws CoreException {
		//						doParsePeriodicMonitorCommand(output);
		//					}
		//		};
		//		job.setPriority(Job.SHORT);
		//		job.setSystem(true);
		//		job.setUser(false);
		//		return job;
		return null;
	}

	/**
	 * Creates a job that keeps monitoring the remote machine. The default implementation runs the continuous monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	@Override
	protected Job createContinuousMonitorJob() {
		//		if (! rmConfiguration.hasContinuousMonitorCmd()) {
		//			return null;
		//		}
		//		Job job = new CommandJob(NLS.bind("Continuous monitor on {0}", rmConfiguration.getName()),
		//				rmConfiguration.getContinuousMonitorCmd(),
		//				"Interrupted while running continuous monitor command.",
		//				"Failed to create remote process for continuous monitor command.",
		//				"Failed to parse output of continuous monitor command.") {
		//
		//					@Override
		//					protected void parse(BufferedReader output) throws CoreException {
		//						doParseContinuousMonitorCommand(output);
		//					}
		//		};
		//		job.setPriority(Job.LONG);
		//		job.setSystem(true);
		//		job.setUser(false);
		//		return job;
		return null;
	}

	@Override
	public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) {
		return new OpenMPIRuntimeSystemJob(jobID, queueID, Messages.OpenMPIRuntimeSystem_JobName, this, attrMgr);
	}
}
