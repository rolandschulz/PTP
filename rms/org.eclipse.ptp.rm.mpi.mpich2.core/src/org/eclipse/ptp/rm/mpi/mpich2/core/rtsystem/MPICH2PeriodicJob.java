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
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
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
		super(rts, NLS.bind(Messages.MPICH2MonitorJob_name, rts.getRmConfiguration().getName()), rts
				.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorCmd(), Messages.MPICH2MonitorJob_interruptedErrorMessage,
				Messages.MPICH2MonitorJob_processErrorMessage, Messages.MPICH2MonitorJob_parsingErrorMessage, rts
						.retrieveEffectiveToolRmConfiguration().getPeriodicMonitorTime());
		this.rts = rts;
	}

	@Override
	protected void parse(BufferedReader output) throws CoreException {
		/*
		 * MPI resource manager have only one machine and one queue. There they are implicitly "discovered".
		 */
		IPResourceManager rm = rts.getPResourceManager();

		/*
		 * Parse output of mpdlistjobs command.
		 */
		MPICH2ListJobsParser parser = new MPICH2ListJobsParser();
		MPICH2JobMap jobMap;
		try {
			jobMap = parser.parse(output);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(),
					parser.getErrorMessage()));
		}

		/*
		 * Update model according to data. First create any new jobs.
		 */
		for (List<MPICH2JobMap.Job> jobs : jobMap.getJobs()) {
			for (MPICH2JobMap.Job job : jobs) {
				IPJob pJob = rm.getJobById(job.getJobAlias());
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
						throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(),
								Messages.MPICH2RuntimeSystemJob_Exception_HostnamesDoNotMatch));
					}
					AttributeManager attrMrg = new AttributeManager();
					attrMrg.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodeID));
					BitSet processJobRanks = new BitSet();
					processJobRanks.set(jobRank);
					rts.changeProcesses(pJob.getID(), processJobRanks, attrMrg);
				}
			}
		}
	}
}
