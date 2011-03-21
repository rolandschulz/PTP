/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.smoa.core.rmsystem.PoolingIntervalsAndStatic.SMOAJobState;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAFileStore;
import org.eclipse.ptp.rm.smoa.core.util.NotifyShell;

import com.smoa.comp.sdk.SMOAFactory;
import com.smoa.comp.sdk.exceptions.FileNotFoundException;
import com.smoa.comp.sdk.exceptions.NotAuthorizedException;
import com.smoa.comp.sdk.exceptions.UnknownActivityIdentifierException;
import com.smoa.comp.sdk.types.ActivityEndpointReference;
import com.smoa.comp.sdk.types.SMOAActivityStatus;

/**
 * Thread for monitoring a single job - checks state and transfers standard
 * output. Allows job's termination.
 */
class JobThread extends Thread {
	// The RM that created the job
	private final SMOAResourceManager rm;
	private final IPResourceManager pRM;

	// The eclipse job control object
	private final IPJob jobControl;

	// The SMOA job control object
	private final ActivityEndpointReference activityIdentifier;

	private final SMOAFactory factory;
	private final IRemoteFileManager fileManager;

	// Remote files used by launch
	private final IFileStore out;
	private final IFileStore err;
	private final IFileStore sh;
	private final IFileStore machinefile;

	// Offsets for the output files
	private int stdOutOffset = 0;
	private int stdErrOffset = 0;

	/**
	 * Constructs the job thread for monitoring a single job
	 */
	public JobThread(SMOAResourceManager rm, SMOAFactory factory, ActivityEndpointReference activityEndpointReference,
			IPJob jobControl, String out, String err, String sh, String machinefile) {
		this.rm = rm;
		this.pRM = (IPResourceManager) rm.getAdapter(IPResourceManager.class);
		this.factory = factory;
		this.activityIdentifier = activityEndpointReference;
		this.jobControl = jobControl;

		this.setPriority(MIN_PRIORITY);
		this.setName("JobListener for " + activityIdentifier.getActivityUUID()); //$NON-NLS-1$

		final IRemoteFileManager fileManager_t = PTPRemoteCorePlugin.getDefault()
				.getRemoteServices(rm.getControlConfiguration().getRemoteServicesId())
				.getFileManager(rm.getControlConfiguration().getConnection());

		fileManager = fileManager_t;

		this.out = fileManager.getResource(out);
		this.err = fileManager.getResource(err);
		if (sh != null) {
			this.sh = fileManager.getResource(sh);
		} else {
			this.sh = null;
		}
		if (machinefile != null) {
			this.machinefile = fileManager.getResource(machinefile);
		} else {
			this.machinefile = null;
		}

	}

	/** Adds given text to standard error, and makes it appear on console */
	void appendStdErr(String stderr) {
		final AttributeManager outManager = new AttributeManager();
		outManager.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(stderr));
		final BitSet bs = new BitSet();
		bs.set(jobControl.getProcessJobRanks().nextSetBit(0));
		jobControl.addProcessAttributes(bs, outManager);
	}

	/** Adds given text to standard output, and makes it appear on console */
	void appendStdOut(String stdout) {
		final AttributeManager outManager = new AttributeManager();
		outManager.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(stdout));
		final BitSet bs = new BitSet();
		bs.set(jobControl.getProcessJobRanks().nextSetBit(0));
		jobControl.addProcessAttributes(bs, outManager);
	}

	/**
	 * Changes job state
	 */
	void changeState(JobAttributes.State newState) {
		if (JobAttributes.State.RUNNING.equals(newState)) {
			enteredRunPhase();
		}

		final AttributeManager am = new AttributeManager();
		am.addAttribute(JobAttributes.getStateAttributeDefinition().create(newState));
		rm.getMonitor().getRuntimeSystem().changeJob(jobControl.getID(), am);
	}

	/**
	 * Called when the process state becomes RUNNING.
	 * 
	 * This is the proper moment for identifying where the processes are
	 * located, so that debug routing files may be written.
	 */
	private void enteredRunPhase() {
		if (machinefile != null) {
			try {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(machinefile.openInputStream(0, null)));

				final IPMachine machine = pRM.getMachines()[0];

				final Map<String, String> nodesByName = new HashMap<String, String>();
				for (final IPNode ipNode : machine.getNodes()) {
					nodesByName.put(ipNode.getName(), ipNode.getID());
				}

				String line;
				int i = 0;
				while ((line = reader.readLine()) != null) {
					final BitSet bs = new BitSet();
					bs.set(i++);
					final AttributeManager attrs = new AttributeManager();

					if (!nodesByName.containsKey(line)) {
						rm.getMonitor().getRuntimeSystem().addUnknownNode(machine, line);
						nodesByName.put(line, ((Integer) nodesByName.size()).toString());
						NotifyShell.open(Messages.JobThread_UnknownNode_title, Messages.JobThread_UnknownNode_text_1 + line
								+ Messages.JobThread_UnknownNode_text_2);
						attrs.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodesByName.get(line)));
					} else {
						attrs.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodesByName.get(line)));
					}
					jobControl.addProcessesByJobRanks(bs, attrs);
				}
				return;
			} catch (final CoreException e) {
				NotifyShell.open(Messages.JobThread_ErrorOpeningRemote, e.toString());

				final BitSet bs = new BitSet(1);
				bs.set(0);
				final AttributeManager jam = new AttributeManager();
				jam.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create("0")); //$NON-NLS-1$
				jobControl.addProcessesByJobRanks(bs, jam);
			} catch (final IOException e) {
				NotifyShell.open(Messages.JobThread_ErrorReadingRemote, e.toString());

				if (jobControl.getProcessJobRanks() != null && !jobControl.getProcessJobRanks().isEmpty()) {
					return;
				}

				final BitSet bs = new BitSet(1);
				bs.set(0);
				final AttributeManager jam = new AttributeManager();
				jam.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create("0")); //$NON-NLS-1$
				jobControl.addProcessesByJobRanks(bs, jam);
			}
		} else {
			final BitSet bs = new BitSet(1);
			bs.set(0);
			final AttributeManager jam = new AttributeManager();
			jam.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create("0")); //$NON-NLS-1$
			jobControl.addProcessesByJobRanks(bs, jam);
		}
	}

	/**
	 * Called if an exception has been thrown while monitoring job
	 */
	void exceptionCaught(Exception e) {
		final AttributeManager am = new AttributeManager();

		am.addAttribute(JobAttributes.getStatusAttributeDefinition().create(Messages.JobThread_ExceptionByMonitoring));

		am.addAttribute(JobAttributes.getStateAttributeDefinition().create(JobAttributes.State.COMPLETED));

		am.addAttribute(PoolingIntervalsAndStatic.exceptionAttrDef.create(e.getLocalizedMessage()));

		NotifyShell.open(Messages.JobThread_ExceptionByMonitoring, e.getLocalizedMessage());

		rm.getMonitor().getRuntimeSystem().changeJob(jobControl.getID(), am);
	}

	/** Executed after the job reached terminal state */
	private void jobFinished(SMOAActivityStatus status) {

		try {
			// If the job jumped from queued to finished state, we didn't
			// add any processes yet
			if (jobControl.getProcessJobRanks() == null || jobControl.getProcessJobRanks().isEmpty()) {
				final BitSet bs = new BitSet(1);
				bs.set(0);
				final AttributeManager jam = new AttributeManager();
				jam.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create("0")); //$NON-NLS-1$
				jobControl.addProcessesByJobRanks(bs, jam);
			}

			processOutAndErr();

			try {
				out.delete(0, null);
				err.delete(0, null);
				if (sh != null) {
					sh.delete(0, null);
				}
				if (machinefile != null) {
					machinefile.delete(0, null);
				}
			} catch (final CoreException e) {
				NotifyShell.open(Messages.JobThread_ErrorDeletingTempFiles_title, Messages.JobThread_ErrorDeletingTempFiles_text
						+ e.getLocalizedMessage());
				e.printStackTrace();
			}
		} catch (final RuntimeException e) {
			// Happens if there is a problem with stdout/err file
		}

		final AttributeManager am = new AttributeManager();

		String jobStatus = null;

		switch (PoolingIntervalsAndStatic.getEquivalentJobState(status)) {
		case Finished:
			jobStatus = Messages.JobThread_JobStateFinisedWithStatus + status.getEndStatus().getExitStatus();
			break;
		case Failed:
			jobStatus = Messages.JobThread_JobStateFailed;
			break;
		case Cancelled:
			jobStatus = Messages.JobThread_JobStateCancelled;
			break;
		default:
			jobStatus = Messages.JobThread_JobStateUnknown;
		}

		am.addAttribute(JobAttributes.getStatusAttributeDefinition().create(jobStatus));

		JobAttributes.State state;
		state = JobAttributes.State.COMPLETED;
		am.addAttribute(JobAttributes.getStateAttributeDefinition().create(state));

		rm.getMonitor().getRuntimeSystem().changeJob(jobControl.getID(), am);
	}

	/**
	 * Takes care about reading out and err streams and forwards them on console
	 */
	private void processOutAndErr() {

		/* Out */
		InputStream is;
		try {
			if (out instanceof SMOAFileStore) {
				is = ((SMOAFileStore) out).openInputStream(0, null, stdOutOffset);
			} else {
				is = out.openInputStream(0, null);
				is.skip(stdOutOffset);
			}

			final byte[] buffer = new byte[512];

			for (int count = is.read(buffer); count > 0; count = is.read(buffer)) {
				stdOutOffset += count;
				appendStdOut(new String(buffer).substring(0, count));
			}
		} catch (final CoreException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				return;
			}
			throw new RuntimeException(e);
		} catch (final IOException e) {
			NotifyShell.open(Messages.JobThread_ErrorOut, e.getLocalizedMessage());
		}

		/* Err */

		try {
			if (err instanceof SMOAFileStore) {
				is = ((SMOAFileStore) err).openInputStream(0, null, stdErrOffset);
			} else {
				is = err.openInputStream(0, null);
				is.skip(stdErrOffset);
			}

			final byte[] buffer = new byte[512];

			for (int count = is.read(buffer); count > 0; count = is.read(buffer)) {
				stdErrOffset += count;
				if (count == buffer.length) {
					appendStdErr(new String(buffer));
				} else {
					appendStdErr(new String(buffer).substring(0, count));
				}
			}
		} catch (final CoreException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				return;
			}
			throw new RuntimeException(e);
		} catch (final IOException e) {
			NotifyShell.open(Messages.JobThread_ErrorErr, e.getLocalizedMessage());
		}
	}

	/** Loop for monitoring task */
	@Override
	public void run() {
		try {

			SMOAActivityStatus status = factory.getActivityStatus(activityIdentifier);

			SMOAActivityStatus prevStatus = null;

			long nextStateCheck = System.currentTimeMillis();
			long nextOutCheck = nextStateCheck;

			// Till the state is not final, we pool the job and out streams
			while (true) {

				// Status
				if (nextStateCheck <= System.currentTimeMillis()) {
					nextStateCheck = System.currentTimeMillis() + PoolingIntervalsAndStatic.getPoolingIntervalTask();

					final SMOAJobState statusS = PoolingIntervalsAndStatic.getEquivalentJobState(status);

					SMOAJobState prevstatusS = null;

					if (prevStatus != null) {
						prevstatusS = PoolingIntervalsAndStatic.getEquivalentJobState(prevStatus);
					}

					if (prevStatus == null || !statusS.equals(prevstatusS)) {
						switch (statusS) {
						case Stage_in:
						case Held:
						case Queued:
							changeState(JobAttributes.State.STARTING);
							break;
						case Stage_out:
						case Executing:
							changeState(JobAttributes.State.RUNNING);
							break;
						case Suspended:
							changeState(JobAttributes.State.SUSPENDED);
							break;

						case Cancelled:
							break;
						case Failed:
							break;
						case Finished:
							break;
						}
						prevStatus = status;
					}
					status = factory.getActivityStatus(activityIdentifier);

					if (status.isFinalState()) {
						break;
					}
				}

				// Out
				final JobAttributes.State state = jobControl.getAttribute(JobAttributes.getStateAttributeDefinition()).getValue();
				if (state == JobAttributes.State.RUNNING && nextOutCheck <= System.currentTimeMillis()) {
					nextOutCheck = System.currentTimeMillis() + PoolingIntervalsAndStatic.getPoolingIntervalOut();

					processOutAndErr();
				}
				try {
					final long nextCheck = Math.min(nextOutCheck - System.currentTimeMillis(),
							nextStateCheck - System.currentTimeMillis());
					if (nextCheck > 0) {
						sleep(nextCheck);
					}
				} catch (final InterruptedException e) {
					// The job has been terminated, ignore
				}
			}

			jobFinished(status);

		} catch (final NotAuthorizedException e) {
			exceptionCaught(e);
		} catch (final UnknownActivityIdentifierException e) {
			exceptionCaught(e);
		}

		rm.removeJobThread(jobControl.getID());
	}

	/**
	 * Terminates the monitored job
	 */
	public boolean terminate() {
		try {
			factory.terminateActivity(activityIdentifier);
			this.interrupt();
		} catch (final Exception e) {
			return false;
		}
		return true;
	}
}
