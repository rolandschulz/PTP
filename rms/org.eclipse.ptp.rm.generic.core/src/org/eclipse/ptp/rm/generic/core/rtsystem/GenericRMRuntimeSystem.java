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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.generic.core.messages.Messages;
import org.eclipse.ptp.rm.generic.core.rmsystem.EffectiveGenericRMConfiguration;

public class GenericRMRuntimeSystem extends AbstractToolRuntimeSystem {

	/** The machine we are running on. */
	private String fMachineID;
	/** The node we are running on. */
	private String fNodeID;
	/** The queue that dispatches jobs */
	private String fQueueID;

	public GenericRMRuntimeSystem(Integer openmpi_rmid, IToolRMConfiguration config, AttributeDefinitionManager attrDefMgr) {
		super(openmpi_rmid, config, attrDefMgr);
	}

	@Override
	public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) {
		return new GenericRMRuntimeSystemJob(jobID, queueID, Messages.GenericRMRuntimeSystem_JobName, this, attrMgr);
	}

	public String getMachineID() {
		return fMachineID;
	}

	public String getNodeID() {
		return fNodeID;
	}

	public String getQueueID() {
		return fQueueID;
	}

	@Override
	public AbstractEffectiveToolRMConfiguration retrieveEffectiveToolRmConfiguration() {
		return new EffectiveGenericRMConfiguration(getRmConfiguration());
	}

	@Override
	protected Job createContinuousMonitorJob(IProgressMonitor monitor) {
		return null;
	}

	@Override
	protected Job createDiscoverJob(IProgressMonitor monitor) {
		return null;
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
		setMachineID(createMachine(connection.getName()));
		setNodeID(createNode(getMachineID(), connection.getAddress(), 0));
		setQueueID(createQueue(Messages.GenericRMRuntimeSystem_0));
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
		fMachineID = machineID;
	}

	protected void setNodeID(String nodeID) {
		fNodeID = nodeID;
	}

	protected void setQueueID(String queueID) {
		fQueueID = queueID;
	}
}
