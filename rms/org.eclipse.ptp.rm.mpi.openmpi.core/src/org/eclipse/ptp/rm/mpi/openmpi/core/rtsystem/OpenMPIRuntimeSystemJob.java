/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.MPIJobAttributes;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.IInputStreamListener;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;

/**
 * Master job that implements the Open MPI runtime system. This job must
 * implement each of the various phases of the runtime system:
 * 
 * doPrepareExecution = do any debugger startup actions doBeforeExecution -
 * merge streams if necessary doExecutionStarted - parse output from the mpirun
 * command doWaitExectuion - wait until execution has completed
 * doExecutionFinished - deal with any issues from program termination
 * doExecutionCleanup - cleanup after execution
 * 
 * The type/format of output depends on a range of factors, including the OMPI
 * version and the capabilities of the remote service provider being used.
 * 
 * OMPI 1.2 generates map data in textual form that must be parsed to extract
 * the relevant information. Map information is sent to stderr, but RSE does not
 * separate stdout/stderr so this must be handled as a special case.
 * 
 * OMPI 1.3.x generates map data in (malformed) XML format so we use an XML
 * parser to extract information.
 * 
 * OMPI 1.3.[1,2] wrap stdout and stderr from the program in XML tags, but they
 * are sill sent to the respective streams.
 * 
 * OMPI 1.3 does not wrap stdout and stderr from the program in XML tags.
 * 
 * OMPI 1.3.2 adds <noderesolve> elements to the XML map data.
 * 
 * OMPI 1.3.[1,2,3] malform the XML by dropping </stdout> tags on some lines.
 * 
 * OMPI 1.3.4 and 1.4 add <mpirun> and </mpirun> root tags
 * 
 * @author Daniel Felix Ferber
 * @author Greg Watson
 * 
 */
public class OpenMPIRuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	/** Error detected in mpirun output */
	protected boolean errorDetected = false;

	protected String errorMessage = null;
	/** Used to signal map information completed */
	protected boolean mapCompleted = false;

	protected final ReentrantLock mapLock = new ReentrantLock();
	protected final Condition mapCondition = mapLock.newCondition();
	/** Exception raised while parsing mpi map information. */
	protected Exception parserException = null;

	protected InputStreamListenerToOutputStream parserListener;
	/** Main parser thread */
	protected Thread parserThread;

	public OpenMPIRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}

	/**
	 * Terminate all processes.
	 */
	private void terminateProcesses() {
		final OpenMPIRuntimeSystem rtSystem = (OpenMPIRuntimeSystem) getRtSystem();
		IPResourceManager rm = (IPResourceManager) rtSystem.getResourceManager().getAdapter(IPResourceManager.class);
		if (rm != null) {
			final IPJob ipJob = rm.getJobById(getJobID());
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

	/**
	 * Add a process to the job
	 * 
	 * @param job
	 * @param proc
	 */
	protected void addProcess(IPJob job, Process proc) {
		OpenMPIRuntimeSystem rts = (OpenMPIRuntimeSystem) getRtSystem();
		String nodename = proc.getNode().getResolvedName();
		String nodeID = rts.getNodeIDforName(nodename);
		if (nodeID != null) {
			int processIndex = proc.getIndex();
			final BitSet processIndices = new BitSet();
			processIndices.set(processIndex);
			AttributeManager attrMgr = new AttributeManager();
			attrMgr.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodeID));
			attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
			attrMgr.addAttributes(proc.getAttributeManager().getAttributes());
			getRtSystem().changeProcesses(job.getID(), processIndices, attrMgr);
		}
	}

	/**
	 * Create the parser thread
	 * 
	 */
	protected void createParser(final IOpenMPIResourceManagerConfiguration configuration, final IPJob job) {
		/*
		 * Thread that parses map information.
		 */
		final PipedOutputStream parserOutputStream = new PipedOutputStream();
		final PipedInputStream parserInputStream = new PipedInputStream();
		try {
			parserInputStream.connect(parserOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}

		setParserListener(new InputStreamListenerToOutputStream(parserOutputStream));

		parserThread = new Thread() {
			@Override
			public void run() {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: started", getJobID()); //$NON-NLS-1$
				try {
					// Parse stdout or stderr, depending on mpi 1.2 or 1.3
					if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
						OpenMPIProcessMapText12Parser.parse(parserInputStream, new IOpenMPIProcessMapParserListener() {
							public void finish() {
								// Empty
							}

							public void finishMap(AttributeManager manager) {
								/*
								 * Copy job attributes from map.
								 */
								if (manager.getAttributes().length > 0) {
									DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE,
											"RTS job #{0}: updating model with display-map information", getJobID()); //$NON-NLS-1$
									getRtSystem().changeJob(getJobID(), manager);
								}
								setMapCompleted();
							}

							public void newProcess(Process proc) {
								addProcess(job, proc);
							}

							public void start() {
								// Empty
							}

							public void stderr(Process proc, String output) {
								// Empty
							}

							public void stdout(Process proc, String output) {
								// Empty
							}
						});
					} else if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)
							|| configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
						InputStream is = new OpenMPI13xInputStream(parserInputStream);
						OpenMPIProcessMapXml13Parser.parse(is, new IOpenMPIProcessMapParserListener() {
							public void finish() {
								/*
								 * Turn off listener that generates input for
								 * parser when parsing finishes. If not done,
								 * the parser will close the piped inputstream,
								 * making the listener get IOExceptions for
								 * closed stream.
								 */
								getParserListener().disable();
							}

							public void finishMap(AttributeManager manager) {
								/*
								 * Copy job attributes from map.
								 */
								if (manager.getAttributes().length > 0) {
									DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE,
											"RTS job #{0}: updating model with display-map information", getJobID()); //$NON-NLS-1$
									getRtSystem().changeJob(getJobID(), manager);
								}
								setMapCompleted();
							}

							public void newProcess(Process proc) {
								addProcess(job, proc);
							}

							public void start() {
								// Empty
							}

							public void stderr(Process proc, String output) {
								String stderr = output;
								if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)
										&& configuration.getServiceVersion() < 4) {
									stderr += "\n"; //$NON-NLS-1$
								}
								int index = 0;
								if (proc != null) {
									index = proc.getIndex();
								}
								final StringAttribute attr = ProcessAttributes.getStderrAttributeDefinition().create(stderr);
								setProcessAttribute(job, index, attr);
							}

							public void stdout(Process proc, String output) {
								String stdout = output;
								if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)
										&& configuration.getServiceVersion() < 4) {
									stdout += "\n"; //$NON-NLS-1$
								}
								int index = 0;
								if (proc != null) {
									index = proc.getIndex();
								}
								final StringAttribute attr = ProcessAttributes.getStdoutAttributeDefinition().create(stdout);
								setProcessAttribute(job, index, attr);
							}

						});
					} else {
						assert false;
					}
				} catch (Exception e) {
					parserException = e;
					DebugUtil.error(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: {1}", getJobID(), e); //$NON-NLS-1$
				} finally {
					if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
						getParserListener().disable();
					}
					setMapCompleted();
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: finished", getJobID()); //$NON-NLS-1$
			}
		};
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
		final IOpenMPIResourceManagerConfiguration configuration = (IOpenMPIResourceManagerConfiguration) getRtSystem()
				.getRmConfiguration();
		/*
		 * Merge stdout and stderr streams for OMPI 1.3.[1,2] since the streams
		 * are wrapped in the appropriate XML tags, but are still sent
		 * separately.
		 */
		if ((configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13) && (configuration
				.getServiceVersion() > 0 && configuration.getServiceVersion() < 3))) {
			builder.redirectErrorStream(true);
		}
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
				changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailedWithExitValue,
						new Integer(getProcess().exitValue())));
				changeJobStatus(MPIJobAttributes.Status.ERROR);
			}

			DebugUtil
					.trace(DebugUtil.RTS_JOB_TRACING,
							"RTS job #{0}: ignoring exit value {1} because job was forced to terminate by user", getJobID(), new Integer(getProcess().exitValue())); //$NON-NLS-1$
		} else if (errorDetected) {
			changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailureDetected, errorMessage));
			changeJobStatus(MPIJobAttributes.Status.ERROR);
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
		mapCompleted = false;

		/*
		 * Create processes for the job.
		 */
		final IOpenMPIResourceManagerConfiguration configuration = (IOpenMPIResourceManagerConfiguration) getRtSystem()
				.getRmConfiguration();
		IPResourceManager rm = (IPResourceManager) getRtSystem().getResourceManager().getAdapter(IPResourceManager.class);
		final IPJob ipJob = rm.getJobById(getJobID());
		IntegerAttribute numProcsAttr = ipJob.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		assert numProcsAttr != null;
		getRtSystem().createProcesses(getJobID(), numProcsAttr.getValue().intValue());

		/*
		 * We only require procZero if we're using OMPI 1.2.x or 1.3.[0-3].
		 * Other versions use XML for stdout and stderr.
		 */
		final BitSet procZero = new BitSet();
		if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)
				|| (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13) && configuration
						.getServiceVersion() < 4)) {
			if (ipJob.hasProcessByJobRank(0)) {
				procZero.set(0);
			}
		}

		/*
		 * 
		 * Listener that saves stdout.
		 */
		final IInputStreamListener stdoutListener = new IInputStreamListener() {
			public void newBytes(byte[] bytes, int length) {
				String line = new String(bytes, 0, length);
				if (!errorDetected && OpenMPIErrorParser.parse(line)) {
					errorDetected = true;
					errorMessage = OpenMPIErrorParser.getErrorMessage();
				}
				if (!procZero.isEmpty()) {
					final AttributeManager attributes = new AttributeManager(ProcessAttributes.getStdoutAttributeDefinition()
							.create(line));
					((IPJobControl) ipJob).addProcessAttributes(procZero, attributes);
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}: {1}", getJobID(), line); //$NON-NLS-1$
			}

			public void streamClosed() {
				// No need to do anything
			}

			public void streamError(Exception e) {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout stream: {0}", e); //$NON-NLS-1$
				OpenMPIPlugin.log(e);
			}
		};

		/*
		 * 
		 * Listener that saves stderr.
		 */
		final IInputStreamListener stderrListener = new IInputStreamListener() {
			public void newBytes(byte[] bytes, int length) {
				String line = new String(bytes, 0, length);
				if (!errorDetected && OpenMPIErrorParser.parse(line)) {
					errorDetected = true;
					errorMessage = OpenMPIErrorParser.getErrorMessage();
				}
				if (!procZero.isEmpty()) {
					final AttributeManager attributes = new AttributeManager(ProcessAttributes.getStderrAttributeDefinition()
							.create(line));
					((IPJobControl) ipJob).addProcessAttributes(procZero, attributes);
				}
				DebugUtil.error(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}: {1}", getJobID(), line); //$NON-NLS-1$
			}

			public void streamClosed() {
				//
			}

			public void streamError(Exception e) {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr stream: {0}", e); //$NON-NLS-1$
				OpenMPIPlugin.log(e);
			}
		};

		createParser(configuration, ipJob);

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: starting all threads", getJobID()); //$NON-NLS-1$

		/*
		 * Create and start listeners.
		 */
		getParser().start();

		setStderrObserver(new InputStreamObserver(getProcess().getErrorStream()));
		getStderrObserver().addListener(stderrListener);
		setStdoutObserver(new InputStreamObserver(getProcess().getInputStream()));
		getStdoutObserver().addListener(stdoutListener);

		// Parse stdout or stderr, depending on mpi 1.2 or 1.3
		if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
			/*
			 * Fix for bug #271810
			 */
			if (!getRtSystem().getRemoteServices().getId().equals("org.eclipse.ptp.remote.RSERemoteServices")) { //$NON-NLS-1$
				getStderrObserver().addListener(getParserListener());
			} else {
				getStdoutObserver().addListener(getParserListener());
			}
		} else if (configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)
				|| configuration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
			getStdoutObserver().addListener(getParserListener());
		} else {
			assert false;
		}

		getStderrObserver().start();
		getStdoutObserver().start();

		waitForMapCompleted();

		if (parserException != null) {
			if (!getProcess().isCompleted()) {
				getProcess().destroy();
			}

			/*
			 * Wait until both stdout and stderr stop because stream are closed.
			 * Error messages may be still queued in the stream.
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

			throw OpenMPIPlugin.coreErrorException(
					"Failed to parse output of Open MPI command. Check output for errors.", parserException); //$NON-NLS-1$
		}
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

		List<IAttribute<?, ?, ?>> newAttributes = new ArrayList<IAttribute<?, ?, ?>>();

		/*
		 * An OpenMPI specific attribute. Attribute that contains a list of
		 * names of environment variables.
		 */
		int p = 0;
		String keys[] = new String[environment.size()];
		for (String key : environment.keySet()) {
			keys[p++] = key;
		}
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentKeysAttributeDefinition().create(keys));

		/*
		 * An OpenMPI specific attribute. A shortcut that generates arguments
		 * for the OpenMPI run command.
		 */
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentArgsAttributeDefinition().create());
		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
	}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment() throws CoreException {
		// No extra environment variables needs to be set for OpenMPI.
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
		try {
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE,
					"RTS job #{0}: waiting for display-map parser thread to finish", getJobID()); //$NON-NLS-1$
			parserThread.join();
		} catch (InterruptedException e) {
			// Do nothing.
		}

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

		if (parserException != null) {
			throw OpenMPIPlugin.coreErrorException(
					"Failed to parse output of Open MPI command. Check output for errors.", parserException); //$NON-NLS-1$
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
	 * @return the parser thread
	 */
	protected Thread getParser() {
		return parserThread;
	}

	/**
	 * @return the parser listener
	 */
	protected InputStreamListenerToOutputStream getParserListener() {
		return parserListener;
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
	 * Signal that the map is complete.
	 */
	protected void setMapCompleted() {
		mapLock.lock();
		try {
			mapCompleted = true;
			mapCondition.signalAll();
		} finally {
			mapLock.unlock();
		}
	}

	/**
	 * @return the parser listener
	 */
	protected void setParserListener(InputStreamListenerToOutputStream listener) {
		parserListener = listener;
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

	/**
	 * Wait until the map has been read or some other error occurs.
	 */
	protected void waitForMapCompleted() {
		mapLock.lock();
		try {
			while (!mapCompleted) {
				try {
					mapCondition.await();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} finally {
			mapLock.unlock();
		}
	}

	void setProcessAttribute(final IPJob job, int index, final StringAttribute attr) {
		final BitSet processIndices = new BitSet();
		processIndices.set(index);
		IPJobControl jobCtl = (IPJobControl) job;
		final AttributeManager attrMgr = new AttributeManager(attr);
		jobCtl.addProcessAttributes(processIndices, attrMgr);
	}

}
