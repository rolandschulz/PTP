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
package org.eclipse.ptp.rm.core.rmsystem;

import java.util.BitSet;
import java.util.Collection;

import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolResourceManager extends AbstractRuntimeResourceManager {

	public AbstractToolResourceManager(String id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id, universe, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#
	 * getAttributeDefinitionManager()
	 */
	@Override
	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return super.getAttributeDefinitionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doBeforeCloseConnection()
	 */
	@Override
	protected void doBeforeCloseConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doAfterCloseConnection()
	 */
	@Override
	protected void doAfterCloseConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doAfterOpenConnection
	 * ()
	 */
	@Override
	protected void doAfterOpenConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doBeforeOpenConnection()
	 */
	@Override
	protected void doBeforeOpenConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateJob(java
	 * .lang.String, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected IPJobControl doCreateJob(String jobId, AttributeManager attrs) {
		return newJob(jobId, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateMachine
	 * (java.lang.String, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected IPMachineControl doCreateMachine(String machineId, AttributeManager attrs) {
		return newMachine(machineId, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateNode(
	 * org.eclipse.ptp.core.elementcontrols.IPMachineControl, java.lang.String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected IPNodeControl doCreateNode(IPMachineControl machine, String nodeId, AttributeManager attrs) {
		return newNode(machine, nodeId, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateQueue
	 * (java.lang.String, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected IPQueueControl doCreateQueue(String queueId, AttributeManager attrs) {
		return newQueue(queueId, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateJobs(
	 * java.util.Collection, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateJobs(Collection<IPJobControl> jobs, AttributeManager attrs) {
		return updateJobs(jobs, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateMachines
	 * (java.util.Collection, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateMachines(Collection<IPMachineControl> machines, AttributeManager attrs) {
		return updateMachines(machines, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateNodes
	 * (org.eclipse.ptp.core.elementcontrols.IPMachineControl,
	 * java.util.Collection, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateNodes(IPMachineControl machine, Collection<IPNodeControl> nodes, AttributeManager attrs) {
		return updateNodes(machine, nodes, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateProcesses
	 * (org.eclipse.ptp.core.elementcontrols.IPJobControl, java.util.BitSet,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateProcesses(IPJobControl job, BitSet processJobRanks, AttributeManager attrs) {
		return updateProcessesByJobRanks(job, processJobRanks, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateQueues
	 * (java.util.Collection, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateQueues(Collection<IPQueueControl> queues, AttributeManager attrs) {
		return updateQueues(queues, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateRM(org
	 * .eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateRM(AttributeManager attrs) {
		return updateRM(attrs);
	}
}
