/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.core.rtsystem;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.generic.core.GenericRMCorePlugin;
import org.eclipse.ptp.rm.generic.core.messages.Messages;
import org.eclipse.ptp.rm.generic.core.rmsystem.EffectiveGenericRMConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

public class GenericRMRuntimeSystem extends AbstractToolRuntimeSystem {

	/** The node we are running on. */
	private String fNodeID;

	public GenericRMRuntimeSystem(IResourceManagerControl rm, IToolRMConfiguration config) {
		super(rm, config);
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
		return new GenericRMRuntimeSystemJob(jobID, Messages.GenericRMRuntimeSystem_JobName, this, attrMgr);
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
				throw new CoreException(new Status(IStatus.ERROR, GenericRMCorePlugin.getUniqueIdentifier(),
						Messages.GenericRMRuntimeSystem_noDefaultQueue));
			}
			attrs.add(JobAttributes.getQueueIdAttributeDefinition().create(queues[0].getID()));
		}

		/*
		 * Always set the number of processes to 1
		 */
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(Integer.valueOf(1)));
		} catch (IllegalValueException e) {
			// Should never happen
		}

		return attrs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * retrieveEffectiveToolRmConfiguration()
	 */
	@Override
	public AbstractEffectiveToolRMConfiguration retrieveEffectiveToolRmConfiguration() {
		return new EffectiveGenericRMConfiguration(getRmConfiguration());
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#
	 * createPeriodicMonitorJob(org.eclipse.core.runtime.IProgressMonitor)
	 */
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
		String machineId = createMachine(connection.getName());
		fNodeID = createNode(machineId, connection.getAddress(), 0);
		createQueue(Messages.GenericRMRuntimeSystem_0);
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

	protected String getNodeId() {
		return fNodeID;
	}
}
