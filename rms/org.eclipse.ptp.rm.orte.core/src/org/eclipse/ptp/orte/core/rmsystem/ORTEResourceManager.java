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
 *******************************************************************************/
package org.eclipse.ptp.orte.core.rmsystem;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.orte.core.rtsystem.ORTEProxyRuntimeClient;
import org.eclipse.ptp.orte.core.rtsystem.ORTERuntimeSystem;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class ORTEResourceManager extends AbstractRuntimeResourceManager {

	private Integer ORTERMID;
	
	public ORTEResourceManager(Integer id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id.toString(), universe, config);
		ORTERMID = id;
		getAttributeDefinitionManager().setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterCloseConnection()
	 */
	protected void doAfterCloseConnection() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterOpenConnection()
	 */
	protected void doAfterOpenConnection() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeCloseConnection()
	 */
	protected void doBeforeCloseConnection() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeOpenConnection()
	 */
	protected void doBeforeOpenConnection() {
	}

	@Override
	protected IPJobControl doCreateJob(IPQueueControl queue, String jobId, AttributeManager attrs) {
		return newJob(queue, jobId, attrs);
	}

	@Override
	protected IPMachineControl doCreateMachine(String machineId, AttributeManager attrs) {
		return newMachine(machineId, attrs);
	}

	@Override
	protected IPNodeControl doCreateNode(IPMachineControl machine, String nodeId, AttributeManager attrs) {
		return newNode(machine, nodeId, attrs);
	}

	@Override
	protected IPProcessControl doCreateProcess(IPJobControl job, String processId, AttributeManager attrs) {
		return newProcess(job, processId, attrs);
	}

	@Override
	protected IPQueueControl doCreateQueue(String queueId, AttributeManager attrs) {
		return newQueue(queueId, attrs);
	}

	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() {
		ORTEResourceManagerConfiguration config = (ORTEResourceManagerConfiguration) getConfiguration();
		String serverFile = config.getOrteServerFile();
		boolean launchManually = config.isLaunchManually();
		/* load up the control and monitoring systems for OMPI */
		ORTEProxyRuntimeClient runtimeProxy = new ORTEProxyRuntimeClient(serverFile, ORTERMID, launchManually);
		return new ORTERuntimeSystem(runtimeProxy, getAttributeDefinitionManager());
	}

	@Override
	protected boolean doUpdateJob(IPJobControl job, AttributeManager attrs) {
		return updateJob(job, attrs);
	}

	@Override
	protected boolean doUpdateMachine(IPMachineControl machine, AttributeManager attrs) {
		return updateMachine(machine, attrs);
	}

	@Override
	protected boolean doUpdateNode(IPNodeControl node, AttributeManager attrs) {
		return updateNode(node, attrs);
	}

	@Override
	protected boolean doUpdateProcess(IPProcessControl process, AttributeManager attrs) {
		return updateProcess(process, attrs);
	}

	@Override
	protected boolean doUpdateQueue(IPQueueControl queue, AttributeManager attrs) {
		return updateQueue(queue, attrs);
	}
}