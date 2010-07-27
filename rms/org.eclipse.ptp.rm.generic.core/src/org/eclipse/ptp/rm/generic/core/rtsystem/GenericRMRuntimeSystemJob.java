/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.core.rtsystem;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.MPIJobAttributes;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.IInputStreamListener;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.generic.core.GenericRMCorePlugin;
import org.eclipse.ptp.rm.generic.core.messages.Messages;

public class GenericRMRuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	public GenericRMRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}

	/**
	 * Terminate all processes.
	 */
	private void terminateProcesses() {
		final GenericRMRuntimeSystem rtSystem = (GenericRMRuntimeSystem) getRtSystem();
		final IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID());
		if (rm != null) {
			final IPQueue queue = rm.getQueueById(getQueueID());
			if (queue != null) {
				final IPJob ipJob = queue.getJobById(getJobID());
				if (ipJob != null) {

					/*
					 * Mark all running and starting processes as finished.
					 */

					AttributeManager attrMrg = new AttributeManager();
					attrMrg.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.COMPLETED));
					final BitSet procJobRanks = ipJob.getProcessJobRanks();
					rtSystem.changeProcesses(ipJob.getID(), procJobRanks, attrMrg);
				}
			}
		}
	}

	/**
	 * Add a process to the job
	 * 
	 * @param job
	 * @param proc
	 */
	protected void addProcess(IPJob job) {
		GenericRMRuntimeSystem rts = (GenericRMRuntimeSystem) getRtSystem();
		rts.createProcesses(job.getID(), 1);
		final BitSet processIndices = new BitSet();
		processIndices.set(0);
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(rts.getNodeID()));
		attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
		getRtSystem().changeProcesses(job.getID(), processIndices, attrMgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doBeforeExecution(org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.ptp.remote.core.IRemoteProcessBuilder)
	 */
	@Override
	protected void doBeforeExecution(IProgressMonitor monitor, IRemoteProcessBuilder builder) throws CoreException {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doExecutionCleanUp(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doExecutionCleanUp(IProgressMonitor monitor) {
		if (getProcess() != null) {
			getProcess().destroy();
			setProcess(null);
		}
		if (getStderrObserver() != null) {
			getStderrObserver().kill();
			setStderrObserver(null);
		}
		if (getStdoutObserver() != null) {
			getStdoutObserver().kill();
			setStdoutObserver(null);
		}
		// TODO: more cleanup?
		terminateProcesses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doExecutionFinished(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doExecutionFinished(IProgressMonitor monitor) throws CoreException {
		terminateProcesses();
		if (getProcess().exitValue() != 0) {
			if (!terminateJobFlag) {
				changeJobStatusMessage(NLS.bind(Messages.GenericRMRuntimeSystemJob_Exception_ExecutionFailedWithExitValue,
						new Integer(getProcess().exitValue())));
				changeJobStatus(MPIJobAttributes.Status.ERROR);
			}

			DebugUtil
					.trace(DebugUtil.RTS_JOB_TRACING,
							"RTS job #{0}: ignoring exit value {1} because job was forced to terminate by user", getJobID(), new Integer(getProcess().exitValue())); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doExecutionStarted(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doExecutionStarted(IProgressMonitor monitor) throws CoreException {
		/*
		 * Create processes for the job.
		 */
		final IPJob job = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRtSystem().getRmID())
				.getQueueById(getQueueID()).getJobById(getJobID());
		addProcess(job);

		/*
		 * We only require procZero if we're using OMPI 1.2.x or 1.3.[0-3].
		 * Other versions use XML for stdout and stderr.
		 */
		final BitSet procZero = new BitSet();
		if (job.hasProcessByJobRank(0)) {
			procZero.set(0);
		}

		/*
		 * 
		 * Listener that saves stdout.
		 */
		final IInputStreamListener stdoutListener = new IInputStreamListener() {
			public void newBytes(byte[] bytes, int length) {
				String line = new String(bytes, 0, length);
				if (!procZero.isEmpty()) {
					final AttributeManager attributes = new AttributeManager(ProcessAttributes.getStdoutAttributeDefinition()
							.create(line));
					((IPJobControl) job).addProcessAttributes(procZero, attributes);
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}: {1}", getJobID(), line); //$NON-NLS-1$
			}

			public void streamClosed() {
				// No need to do anything
			}

			public void streamError(Exception e) {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout stream: {0}", e); //$NON-NLS-1$
				GenericRMCorePlugin.log(e);
			}
		};

		/*
		 * 
		 * Listener that saves stderr.
		 */
		final IInputStreamListener stderrListener = new IInputStreamListener() {
			public void newBytes(byte[] bytes, int length) {
				String line = new String(bytes, 0, length);
				if (!procZero.isEmpty()) {
					final AttributeManager attributes = new AttributeManager(ProcessAttributes.getStderrAttributeDefinition()
							.create(line));
					((IPJobControl) job).addProcessAttributes(procZero, attributes);
				}
				DebugUtil.error(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}: {1}", getJobID(), line); //$NON-NLS-1$
			}

			public void streamClosed() {
				//
			}

			public void streamError(Exception e) {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr stream: {0}", e); //$NON-NLS-1$
				GenericRMCorePlugin.log(e);
			}
		};

		setStderrObserver(new InputStreamObserver(getProcess().getErrorStream()));
		getStderrObserver().addListener(stderrListener);
		getStderrObserver().start();

		setStdoutObserver(new InputStreamObserver(getProcess().getInputStream()));
		getStdoutObserver().addListener(stdoutListener);
		getStdoutObserver().start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doPrepareExecution(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doPrepareExecution(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doRetrieveToolBaseSubstitutionAttributes()
	 */
	@Override
	protected IAttribute<?, ?, ?>[] doRetrieveToolBaseSubstitutionAttributes() throws CoreException {
		// TODO make macros available for environment variables and work
		// directory.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#
	 * doRetrieveToolCommandSubstitutionAttributes
	 * (org.eclipse.ptp.core.attributes.AttributeManager, java.lang.String,
	 * java.util.Map)
	 */
	@Override
	protected IAttribute<?, ?, ?>[] doRetrieveToolCommandSubstitutionAttributes(AttributeManager baseSubstitutionAttributeManager,
			String directory, Map<String, String> environment) {
		// No extra variables need to be set.
		return null;
	}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment() throws CoreException {
		// No extra environment variable needs to be set.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#doTerminateJob
	 * ()
	 */
	@Override
	protected void doTerminateJob() {
		// Empty implementation.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob#doWaitExecution
	 * (org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doWaitExecution(IProgressMonitor monitor) throws CoreException {
		/*
		 * Wait until both stdout and stderr stop because stream are closed.
		 * This means that the process has finished.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stderr thread to finish", getJobID()); //$NON-NLS-1$
		try {
			getStderrObserver().join();
		} catch (InterruptedException e1) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stdout thread to finish", getJobID()); //$NON-NLS-1$
		try {
			getStdoutObserver().join();
		} catch (InterruptedException e1) {
			// Ignore
		}

		/*
		 * Still experience has shown that remote process might not have yet
		 * terminated, although stdout and stderr is closed.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting mpi process to finish completely", getJobID()); //$NON-NLS-1$
		try {
			getProcess().waitFor();
		} catch (InterruptedException e) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: completely finished", getJobID()); //$NON-NLS-1$
	}

	/**
	 * @return the stderrObserver
	 */
	protected InputStreamObserver getStderrObserver() {
		return stderrObserver;
	}

	/**
	 * @return the stdoutObserver
	 */
	protected InputStreamObserver getStdoutObserver() {
		return stdoutObserver;
	}

	/**
	 * @param stderrObserver
	 *            the stderrObserver to set
	 */
	protected void setStderrObserver(InputStreamObserver stderrObserver) {
		this.stderrObserver = stderrObserver;
	}

	/**
	 * @param stdoutObserver
	 *            the stdoutObserver to set
	 */
	protected void setStdoutObserver(InputStreamObserver stdoutObserver) {
		this.stdoutObserver = stdoutObserver;
	}
}
