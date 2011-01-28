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
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.OmpiInfo;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.EffectiveOpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIRuntimeSystem extends AbstractToolRuntimeSystem {

	private final OmpiInfo info = new OmpiInfo();

	/** The machine where open mpi is running on. */
	private String machineID;

	/** The queue that dispatches jobs to mpi. */
	private String queueID;
	/** Mapping of discovered hosts and their ID for IPNode elements. */
	private final Map<String, String> nodeToIDMap = new HashMap<String, String>();

	/**
	 * @since 4.0
	 */
	public OpenMPIRuntimeSystem(IResourceManagerControl rm, IOpenMPIResourceManagerConfiguration config,
			AttributeDefinitionManager attrDefMgr) {
		super(rm, config, attrDefMgr);
	}

	@Override
	public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) {
		return new OpenMPIRuntimeSystemJob(jobID, queueID, Messages.OpenMPIRuntimeSystem_JobName, this, attrMgr);
	}

	public String getMachineID() {
		return machineID;
	}

	public String getNodeIDforName(String hostname) {
		return nodeToIDMap.get(hostname);
	}

	public OmpiInfo getOmpiInfo() {
		return info;
	}

	public String getQueueID() {
		return queueID;
	}

	@Override
	public AbstractEffectiveToolRMConfiguration retrieveEffectiveToolRmConfiguration() {
		return new EffectiveOpenMPIResourceManagerConfiguration(getRmConfiguration());
	}

	public void setNodeIDForName(String name, String id) {
		nodeToIDMap.put(name, id);
	}

	@Override
	protected Job createContinuousMonitorJob(IProgressMonitor monitor) {
		return null;
	}

	@Override
	protected Job createDiscoverJob(IProgressMonitor monitor) {
		if (!rmConfiguration.hasDiscoverCmd())
			return null;
		Job job = new OpenMPIDiscoverJob(this, monitor);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
	}

	@Override
	protected Job createPeriodicMonitorJob(IProgressMonitor monitor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doFilterEvents
	 * (org.eclipse.ptp.core.elements.IPElement, boolean,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected void doFilterEvents(IPElement element, boolean filterChildren, AttributeManager filterAttributes)
			throws CoreException {
		// Not implemented yet
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartEvents
	 * ()
	 */
	@Override
	protected void doStartEvents() throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartup(
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStopEvents()
	 */
	@Override
	protected void doStopEvents() throws CoreException {
		// Nothing to do
	}

	protected void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	protected void setQueueID(String queueID) {
		this.queueID = queueID;
	}
}
