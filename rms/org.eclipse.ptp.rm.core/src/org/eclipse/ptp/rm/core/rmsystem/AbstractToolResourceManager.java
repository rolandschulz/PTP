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
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 3.0
	 */
	public AbstractToolResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
		super(universe, config);
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
	protected IPJob doCreateJob(String jobId, AttributeManager attrs) {
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
	protected IPMachine doCreateMachine(String machineId, AttributeManager attrs) {
		return newMachine(machineId, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateNode(
	 * org.eclipse.ptp.core.elementcontrols.IPMachine, java.lang.String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected IPNode doCreateNode(IPMachine machine, String nodeId, AttributeManager attrs) {
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
	protected IPQueue doCreateQueue(String queueId, AttributeManager attrs) {
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
	protected boolean doUpdateJobs(Collection<IPJob> jobs, AttributeManager attrs) {
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
	protected boolean doUpdateMachines(Collection<IPMachine> machines, AttributeManager attrs) {
		return updateMachines(machines, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateNodes
	 * (org.eclipse.ptp.core.elementcontrols.IPMachine, java.util.Collection,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateNodes(IPMachine machine, Collection<IPNode> nodes, AttributeManager attrs) {
		return updateNodes(machine, nodes, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doUpdateProcesses
	 * (org.eclipse.ptp.core.elementcontrols.IPJob, java.util.BitSet,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected boolean doUpdateProcesses(IPJob job, BitSet processJobRanks, AttributeManager attrs) {
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
	protected boolean doUpdateQueues(Collection<IPQueue> queues, AttributeManager attrs) {
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
