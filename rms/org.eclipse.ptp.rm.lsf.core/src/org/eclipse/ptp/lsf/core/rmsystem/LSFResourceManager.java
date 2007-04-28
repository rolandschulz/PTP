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
/**
 * 
 */
package org.eclipse.ptp.lsf.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.lsf.core.rtsystem.LSFProxyRuntimeClient;
import org.eclipse.ptp.lsf.core.rtsystem.LSFRuntimeSystem;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * @author rsqrd
 *
 */
public class LSFResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @param universe
	 * @param config
	 */
	public LSFResourceManager(int id, IPUniverseControl universe, LSFResourceManagerConfiguration config) {
		super(id, universe, config);
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	protected void doDispose() {
	}

	@Override
	protected IPJobControl doCreateJob(IPQueueControl queue, int jobId, AttributeManager attrs) {
		return newJob(queue, jobId, attrs);
	}

	@Override
	protected IPMachineControl doCreateMachine(int machineId, AttributeManager attrs) {
		return newMachine(machineId, attrs);
	}

	@Override
	protected IPNodeControl doCreateNode(IPMachineControl machine, int nodeId, AttributeManager attrs) {
		return newNode(machine, nodeId, attrs);
	}

	@Override
	protected IPProcessControl doCreateProcess(IPJobControl job, int processId, AttributeManager attrs) {
		return newProcess(job, processId, attrs);
	}

	@Override
	protected IPQueueControl doCreateQueue(int queueId, AttributeManager attrs) {
		return newQueue(queueId, attrs);
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

	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() throws CoreException {
		LSFResourceManagerConfiguration config = (LSFResourceManagerConfiguration) getConfiguration();
		String serverFile = config.getServerFile();
		int	rmId = getID();
		boolean launchManually = config.isLaunchManually();
		/* load up the control and monitoring systems for OMPI */
		LSFProxyRuntimeClient runtimeProxy = new LSFProxyRuntimeClient(serverFile, rmId, launchManually);
		return new LSFRuntimeSystem(runtimeProxy, getAttributeDefinitionManager());
	}

	public IntegerAttributeDefinition getNumProcsAttrDef(IPQueue queue) {
		// TODO Auto-generated method stub
		return null;
	}

}