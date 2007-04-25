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
package org.eclipse.ptp.mpich2.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.mpich2.core.rtsystem.MPICH2ProxyRuntimeClient;
import org.eclipse.ptp.mpich2.core.rtsystem.MPICH2RuntimeSystem;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class MPICH2ResourceManager extends AbstractRuntimeResourceManager {

	public MPICH2ResourceManager(int id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id, universe, config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getLaunchAttributes(java.lang.String, java.lang.String)
	 */
	public IAttribute[] getLaunchAttributes(String queue, IAttribute[] currentAttrs) {
		return null;
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
	protected IPJobControl doCreateJob(IPQueueControl queue, int jobId, IAttribute[] attrs) {
		return newJob(queue, jobId, attrs);
	}

	@Override
	protected IPMachineControl doCreateMachine(int machineId, IAttribute[] attrs) {
		return newMachine(machineId, attrs);
	}

	@Override
	protected IPNodeControl doCreateNode(IPMachineControl machine, int nodeId, IAttribute[] attrs) {
		return newNode(machine, nodeId, attrs);
	}

	@Override
	protected IPProcessControl doCreateProcess(IPJobControl job, int processId, IAttribute[] attrs) {
		return newProcess(job, processId, attrs);
	}

	@Override
	protected IPQueueControl doCreateQueue(int queueId, IAttribute[] attrs) {
		return newQueue(queueId, attrs);
	}

	@Override
	protected boolean doUpdateJob(IPJobControl job, IAttribute[] attrs) {
		return updateJob(job, attrs);
	}

	@Override
	protected boolean doUpdateMachine(IPMachineControl machine, IAttribute[] attrs) {
		return updateMachine(machine, attrs);
	}

	@Override
	protected boolean doUpdateNode(IPNodeControl node, IAttribute[] attrs) {
		return updateNode(node, attrs);
	}

	@Override
	protected boolean doUpdateProcess(IPProcessControl process, IAttribute[] attrs) {
		return updateProcess(process, attrs);
	}

	@Override
	protected boolean doUpdateQueue(IPQueueControl queue, IAttribute[] attrs) {
		return updateQueue(queue, attrs);
	}

	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() throws CoreException {
		MPICH2ResourceManagerConfiguration config = (MPICH2ResourceManagerConfiguration) getConfiguration();
		String serverFile = config.getServerFile();
		int	rmId = getID();
		boolean launchManually = config.isLaunchManually();
		/* load up the control and monitoring systems for OMPI */
		MPICH2ProxyRuntimeClient runtimeProxy = new MPICH2ProxyRuntimeClient(serverFile, rmId, launchManually);
		return new MPICH2RuntimeSystem(runtimeProxy, getAttributeDefinitionManager());
	}

}
