/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 * 
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import java.util.BitSet;
import java.util.Collection;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rm.ibm.pe.core.rtsystem.PEProxyRuntimeClient;
import org.eclipse.ptp.rm.ibm.pe.core.rtsystem.PERuntimeSystem;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class PEResourceManager extends AbstractRuntimeResourceManager {

	private final Integer PERMID;

	public PEResourceManager(Integer id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id.toString(), universe, config);
		PERMID = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterCloseConnection
	 * ()
	 */
	@Override
	protected void doAfterCloseConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterOpenConnection
	 * ()
	 */
	@Override
	protected void doAfterOpenConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeCloseConnection
	 * ()
	 */
	@Override
	protected void doBeforeCloseConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeOpenConnection
	 * ()
	 */
	@Override
	protected void doBeforeOpenConnection() {
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
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem
	 * ()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() {
		IPEResourceManagerConfiguration config = (IPEResourceManagerConfiguration) getConfiguration();
		/* load up the control and monitoring systems for OMPI */
		PEProxyRuntimeClient runtimeProxy = new PEProxyRuntimeClient(config, PERMID);
		return new PERuntimeSystem(runtimeProxy, getAttributeDefinitionManager());
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