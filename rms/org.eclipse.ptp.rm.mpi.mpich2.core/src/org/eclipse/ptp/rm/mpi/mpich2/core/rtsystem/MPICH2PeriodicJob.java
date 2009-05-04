/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.io.BufferedReader;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.core.ToolsRMPlugin;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2MachineAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;

/**
 * 
 * @author Greg Watson
 *
 */
public class MPICH2PeriodicJob extends AbstractRemoteCommandJob {
	MPICH2RuntimeSystem rts;

	public MPICH2PeriodicJob(MPICH2RuntimeSystem rts) {
		super(rts,
				NLS.bind(Messages.MPICH2MonitorJob_name, rts.getRmConfiguration().getName()),
				rts.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorCmd(),
				Messages.MPICH2MonitorJob_interruptedErrorMessage,
				Messages.MPICH2MonitorJob_processErrorMessage,
				Messages.MPICH2MonitorJob_parsingErrorMessage,
				rts.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorTime());
		this.rts = rts;
	}

	@Override
	protected void parse(BufferedReader output) throws CoreException {
		/*
		 * MPI resource manager have only one machine and one queue.
		 * There they are implicitly "discovered".
		 */
		IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rts.getRmID());
		IPMachine machine = rm.getMachineById(rts.getMachineID());
		IPQueue queue = rm.getQueueById(rts.getQueueID());
		
		/*
		 * We may be called before the model has been set up properly. Do nothing
		 * if this is the case.
		 */
		if (machine == null || queue == null) {
			return;
		}

		/*
		 * Any exception from now on is caught in order to add the error message as an attribute to the machine.
		 * Then, the exception is re-thrown.
		 */
		try {
			/*
			 * Parse output of mpdlistjobs command.
			 */
			MPICH2ListJobsParser parser = new MPICH2ListJobsParser();
			MPICH2JobMap jobMap = parser.parse(output);
			if (jobMap == null) {
				throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(), parser.getErrorMessage()));
			}
			
			/*
			 * Update model according to data. First create any new jobs.
			 */
			for (List<MPICH2JobMap.Job> jobs : jobMap.getJobs()) {
				for (MPICH2JobMap.Job job : jobs) {
					IPJob pJob = queue.getJobById(job.getJobAlias());
					if (pJob == null) {
						// Not one of our jobs
						continue;
					}
					IPProcessControl process = (IPProcessControl)pJob.getProcessByIndex(job.getRank());
					if (process != null) {
						// process already exists, don't need to do anything
						continue;
					}
					String nodeID = rts.getNodeIDforName(job.getHost());
					if (nodeID == null) {
						throw new CoreException(new Status(IStatus.ERROR, ToolsRMPlugin.getDefault().getBundle().getSymbolicName(), Messages.MPICH2RuntimeSystemJob_Exception_HostnamesDoNotMatch, null));
					}
	
					String processID = rts.createProcess(job.getJobAlias(), job.getRank(), nodeID);
					
					process = (IPProcessControl)pJob.getProcessById(processID);
					process.setState(ProcessAttributes.State.RUNNING);
				}
			}
		} catch (CoreException e) {
			/*
			 * Show message of core exception and change machine status to error.
			 */
			if (e.getStatus().getSeverity() == IStatus.ERROR) {
				AttributeManager attrManager = new AttributeManager();
				attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(NLS.bind(Messages.MPICH2MonitorJob_Exception_CommandFailed, e.getMessage())));
				rts.changeMachine(machine.getID(), attrManager);
			}
			throw e;
		} catch (Exception e) {
			/*
			 * Show message of all other exceptions and change machine status to error.
			 */
			AttributeManager attrManager = new AttributeManager();
			attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
			attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(NLS.bind(Messages.MPICH2MonitorJob_Exception_InternalError, e.getMessage())));
			rts.changeMachine(machine.getID(), attrManager);
		}
	}
}
