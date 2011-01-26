/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 * 		Jie Jiang, 	National University of Defense Technology
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import java.util.BitSet;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMNodeAttributes;
import org.eclipse.ptp.rm.slurm.core.messages.Messages;
import org.eclipse.ptp.rm.slurm.core.rtsystem.SLURMProxyRuntimeClient;
import org.eclipse.ptp.rm.slurm.core.rtsystem.SLURMRuntimeSystem;
import org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SLURMResourceManager extends AbstractRuntimeResourceManager {

	private final Integer SLURMRMID;

	public SLURMResourceManager(Integer id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id.toString(), universe, config);
		SLURMRMID = id;
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
		IRuntimeSystem slurmRMS;
		ISLURMResourceManagerConfiguration config = (ISLURMResourceManagerConfiguration) getConfiguration();
		/* load up the control and monitoring systems for SLURM */
		SLURMProxyRuntimeClient runtimeProxy = new SLURMProxyRuntimeClient(config, SLURMRMID);
		AttributeDefinitionManager attrDefMgr = getAttributeDefinitionManager();
		attrDefMgr.setAttributeDefinitions(SLURMJobAttributes.getDefaultAttributeDefinitions());
		attrDefMgr.setAttributeDefinitions(SLURMNodeAttributes.getDefaultAttributeDefinitions());
		slurmRMS = new SLURMRuntimeSystem(runtimeProxy, attrDefMgr);
		return slurmRMS;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;)
	 */
	@Override
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		final String title = Messages.SLURMResourceManager_0;
		final String msg = e.getErrorMessage();

		// System.out.println("Job submit error!");
		// System.out.println(msg);
		/*
		 * see showErrorDialog(title, msg, status) in UIUtils.java;
		 */
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final Shell shell = Display.getDefault().getActiveShell();
				MessageDialog.openError(shell, title, msg);
			}
		});
	}
}