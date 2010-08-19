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
import java.util.BitSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.core.RMCorePlugin;
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

	public MPICH2PeriodicJob(MPICH2RuntimeSystem rts, IProgressMonitor monitor) {
		super(rts, NLS.bind(Messages.MPICH2MonitorJob_name, rts.getRmConfiguration().getName()), rts
				.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorCmd(), Messages.MPICH2MonitorJob_interruptedErrorMessage,
				Messages.MPICH2MonitorJob_processErrorMessage, Messages.MPICH2MonitorJob_parsingErrorMessage, rts
						.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorTime(), monitor);
		this.rts = rts;
	}

	@Override
	protected IStatus parse(BufferedReader output) {
		/*
		 * MPI resource manager have only one machine and one queue. There they
		 * are implicitly "discovered".
		 */
		IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rts.getRmID());
		IPMachine machine = rm.getMachineById(rts.getMachineID());
		IPQueue queue = rm.getQueueById(rts.getQueueID());

		/*
		 * We may be called before the model has been set up properly. Do
		 * nothing if this is the case.
		 */
		if (machine == null || queue == null) {
			return Status.OK_STATUS;
		}

		/*
		 * Any exception from now on is caught in order to add the error message
		 * as an attribute to the machine. Then, the exception is re-thrown.
		 */
		try {
			/*
			 * Parse output of mpdlistjobs command.
			 */
			MPICH2ListJobsParser parser = new MPICH2ListJobsParser();
			MPICH2JobMap jobMap = parser.parse(output);
			if (jobMap == null) {
				return new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(), parser.getErrorMessage());
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
					final int jobRank = job.getRank();
					boolean hasProcess = pJob.hasProcessByJobRank(jobRank);
					final String processNodeId = hasProcess ? pJob.getProcessNodeId(jobRank) : null;
					boolean processHasNode = processNodeId != null;
					if (hasProcess && !processHasNode) {
						String nodeID = rts.getNodeIDforName(job.getHost());
						if (nodeID == null) {
							return new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(),
									Messages.MPICH2RuntimeSystemJob_Exception_HostnamesDoNotMatch, null);
						}
						AttributeManager attrMrg = new AttributeManager();
						attrMrg.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodeID));
						BitSet processJobRanks = new BitSet();
						processJobRanks.set(jobRank);
						rts.changeProcesses(pJob.getID(), processJobRanks, attrMrg);
					}
				}
			}
		} catch (Exception e) {
			/*
			 * Show message of all other exceptions and change machine status to
			 * error.
			 */
			AttributeManager attrManager = new AttributeManager();
			attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
			attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(
					NLS.bind(Messages.MPICH2MonitorJob_Exception_InternalError, e.getMessage())));
			rts.changeMachine(machine.getID(), attrManager);
			return new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(), NLS.bind(
					Messages.MPICH2MonitorJob_Exception_InternalError, e.getMessage()), e);
		}

		return Status.OK_STATUS;
	}
}
