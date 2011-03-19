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
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2LaunchAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2MachineAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.EffectiveMPICH2ResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManagerConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class MPICH2RuntimeSystem extends AbstractToolRuntimeSystem {

	/** The machine where open mpi is running on. */
	private String machineID;
	/** Mapping of discovered hosts and their ID for IPNode elements. */
	private final Map<String, String> nodeNameToIDMap = new HashMap<String, String>();

	/**
	 * @since 2.0
	 */
	public MPICH2RuntimeSystem(MPICH2ResourceManager rm) {
		super(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * createRuntimeSystemJob(java.lang.String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	public Job createRuntimeSystemJob(String jobID, AttributeManager attrMgr) {
		return new MPICH2RuntimeSystemJob(jobID, Messages.MPICH2RuntimeSystem_JobName, this, attrMgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#getAttributes
	 * (org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public List<IAttribute<?, ?, ?>> getAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = super.getAttributes(configuration, mode);

		IPResourceManager rm = (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
		if (rm != null) {
			IPQueue[] queues = rm.getQueues();
			if (queues.length != 1) {
				throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getUniqueIdentifier(),
						Messages.MPICH2RuntimeSystem_NoDefaultQueue));
			}
			attrs.add(JobAttributes.getQueueIdAttributeDefinition().create(queues[0].getID()));
		}

		int numProcs = configuration.getAttribute(MPICH2LaunchConfiguration.ATTR_NUMPROCS,
				MPICH2LaunchConfigurationDefaults.ATTR_NUMPROCS);
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(Integer.valueOf(numProcs)));
		} catch (IllegalValueException e) {
			throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getUniqueIdentifier(),
					Messages.MPICH2RuntimeSystem_InvalidConfiguration, e));
		}

		attrs.add(MPICH2LaunchAttributes.getLaunchArgumentsAttributeDefinition().create(
				MPICH2LaunchConfiguration.calculateArguments(configuration)));

		return attrs;
	}

	public String getNodeIDforName(String hostname) {
		return nodeNameToIDMap.get(hostname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * retrieveEffectiveToolRmConfiguration()
	 */
	@Override
	public AbstractEffectiveToolRMConfiguration retrieveEffectiveToolRmConfiguration() {
		return new EffectiveMPICH2ResourceManagerConfiguration(getRmConfiguration());
	}

	public void setNodeIDForName(String hostname, String nodeID) {
		nodeNameToIDMap.put(hostname, nodeID);
	}

	private MPICH2ResourceManagerConfiguration getConfiguration() {
		return (MPICH2ResourceManagerConfiguration) getResourceManager().getConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * createContinuousMonitorJob(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Job createContinuousMonitorJob(IProgressMonitor monitor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#createDiscoverJob
	 * (org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Job createDiscoverJob(IProgressMonitor monitor) {
		if (!getConfiguration().hasDiscoverCmd()) {
			return null;
		}
		Job job = new MPICH2DiscoverJob(this, monitor);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * createPeriodicMonitorJob(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Job createPeriodicMonitorJob(IProgressMonitor monitor) {
		if (!getConfiguration().hasPeriodicMonitorCmd()) {
			return null;
		}
		Job job = new MPICH2PeriodicJob(this, monitor);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
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
		machineID = createMachine(getResourceManager().getName());
		createQueue(Messages.MPICH2DiscoverJob_defaultQueueName);
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

	/**
	 * @since 2.0
	 */
	protected String getMachineId() {
		return machineID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * notifyMonitorFailed
	 * (org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob)
	 */
	@Override
	protected void notifyMonitorFailed(AbstractRemoteCommandJob job, Exception exception) {
		/*
		 * Show message of all other exceptions and change machine status to
		 * error.
		 */
		AttributeManager attrManager = new AttributeManager();
		attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
		attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(
				NLS.bind(Messages.MPICH2MonitorJob_Exception_InternalError, exception.getMessage())));
		changeMachine(machineID, attrManager);
	}
}
