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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.rm.core.ToolsRMPlugin;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.IOpenMpiProcessMapXml13ParserListener;

/**
 *
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIRuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	public Object lock1 = new Object();

	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	/** Information parsed from launch command. */
	protected OpenMPIProcessMap map;

	/**
	 * Process IDs created by this job. The first process (zero index) is special,
	 * because it is always created.
	 */
	private String processIDs[];

	/** Exception raised while parsing mpi map information. */
	protected IOException parserException = null;
	
	/** Error detected in mpirun output */
	protected boolean errorDetected = false;
	protected String errorMessage = null;

	public OpenMPIRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}
	
	@Override
	protected void doExecutionStarted(IProgressMonitor monitor) throws CoreException {
		/*
		 * Create a zero index job.
		 */
		final OpenMPIRuntimeSystem rtSystem = (OpenMPIRuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());
		final String zeroIndexProcessID = rtSystem.createProcess(getJobID(), Messages.OpenMPIRuntimeSystemJob_ProcessName, 0);
		processIDs = new String[] { zeroIndexProcessID } ;

		/*
		 * Listener that saves stdout.
		 */
		final PipedOutputStream stdoutOutputStream = new PipedOutputStream();
		final PipedInputStream stdoutInputStream = new PipedInputStream();
		try {
			stdoutInputStream.connect(stdoutOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}
		final InputStreamListenerToOutputStream stdoutPipedStreamListener = new InputStreamListenerToOutputStream(stdoutOutputStream);

		Thread stdoutThread = new Thread() {
			@Override
			public void run() {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: started", getJobID()); //$NON-NLS-1$
				BufferedReader stdoutBufferedReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stdoutBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							if (!errorDetected && OpenMPIErrorParser.parse(line)) {
								errorDetected = true;
								errorMessage = OpenMPIErrorParser.getErrorMessage();
							}
							ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
							DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", getJobID(), line); //$NON-NLS-1$
						}
						line = stdoutBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: {0}", e); //$NON-NLS-1$
					OpenMPIPlugin.log(e);
				} finally {
					stdoutPipedStreamListener.disable();
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: finished", getJobID()); //$NON-NLS-1$
			}
		};

		/*
		 * Listener that saves stderr.
		 */
		final PipedOutputStream stderrOutputStream = new PipedOutputStream();
		final PipedInputStream stderrInputStream = new PipedInputStream();
		try {
			stderrInputStream.connect(stderrOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}
		final InputStreamListenerToOutputStream stderrPipedStreamListener = new InputStreamListenerToOutputStream(stderrOutputStream);
		Thread stderrThread = new Thread() {
			@Override
			public void run() {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: started", getJobID()); //$NON-NLS-1$
				final BufferedReader stderrBufferedReader = new BufferedReader(new InputStreamReader(stderrInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stderrBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							if (!errorDetected && OpenMPIErrorParser.parse(line)) {
								errorDetected = true;
								errorMessage = OpenMPIErrorParser.getErrorMessage();
							}
							ipProc.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
							DebugUtil.error(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", getJobID(), line); //$NON-NLS-1$
						}
						line = stderrBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: {0}", e); //$NON-NLS-1$
					OpenMPIPlugin.log(e);
				} finally {
					stderrPipedStreamListener.disable();
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: finished", getJobID()); //$NON-NLS-1$
			}
		};

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
		final InputStreamListenerToOutputStream parserPipedStreamListener = new InputStreamListenerToOutputStream(parserOutputStream);
		Thread parserThread = new Thread() {
			@Override
			public void run() {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: started", getJobID()); //$NON-NLS-1$
				OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
				try {
					// Parse stdout or stderr, depending on mpi 1.2 or 1.3
					if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
						map = OpenMPIProcessMapText12Parser.parse(parserInputStream);
					} else if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
						map = OpenMPIProcessMapXml13Parser.parse(parserInputStream, new IOpenMpiProcessMapXml13ParserListener() {
							public void startDocument() {
								// Empty
							}
							public void endDocument() {
								/*
								 * Turn off listener that generates input for parser when parsing finishes.
								 * If not done, the parser will close the piped inputstream, making the listener
								 * get IOExceptions for closed stream.
								 */
								parserPipedStreamListener.disable();
								if (getStdoutObserver() != null) {
									getStdoutObserver().removeListener(parserPipedStreamListener);
								}
							}
						});
					} else {
						assert false;
					}
				} catch (IOException e) {
					/*
					 * If output could not be parsed, the kill the mpi process.
					 */
					parserException = e;
//					process.destroy();
					DebugUtil.error(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: {0}", e); //$NON-NLS-1$
				} finally {
					if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
						parserPipedStreamListener.disable();
						if (getStderrObserver() != null) {
							getStderrObserver().removeListener(parserPipedStreamListener);
						}
					}
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: finished", getJobID()); //$NON-NLS-1$
			}
		};

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: starting all threads", getJobID()); //$NON-NLS-1$
		/*
		 * Create and start listeners.
		 */
		stdoutThread.start();
		stderrThread.start();
		parserThread.start();

		setStderrObserver(new InputStreamObserver(process.getErrorStream()));
		getStderrObserver().addListener(stderrPipedStreamListener);
		setStdoutObserver(new InputStreamObserver(process.getInputStream()));
		getStdoutObserver().addListener(stdoutPipedStreamListener);

		// Parse stdout or stderr, depending on mpi 1.2 or 1.3
		OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
		if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
			 /* 
			  * Fix for bug #271810 
			  */
			if (!rtSystem.getRemoteServices().getId().equals("org.eclipse.ptp.remote.RSERemoteServices")) { //$NON-NLS-1$
				stderrObserver.addListener(parserPipedStreamListener);
			} else {
				stdoutObserver.addListener(parserPipedStreamListener);
			}
		} else if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
			getStdoutObserver().addListener(parserPipedStreamListener);
		} else {
			assert false;
		}

		getStderrObserver().start();
		getStdoutObserver().start();

		try {
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting for display-map parser thread to finish", getJobID()); //$NON-NLS-1$
			parserThread.join();
		} catch (InterruptedException e) {
			// Do nothing.
		}

		if (parserException != null) {
			/*
			 * If process completed with error, then it display map parsing failed because of the error message.
			 * If process did not complete, the destroy it.
			 */
			boolean parseError = true;
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: destroy process due error while parsing display map", getJobID()); //$NON-NLS-1$

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
			if (parseError) {
				throw OpenMPIPlugin.coreErrorException("Failed to parse output of Open MPI command. Check output for errors.", parserException); //$NON-NLS-1$
			}
			
			throw OpenMPIPlugin.coreErrorException("Open MPI failed to launch parallel application. Check output for errors."); //$NON-NLS-1$
		}

		/*
		 * Copy job attributes from map.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: updating model with display-map information", getJobID()); //$NON-NLS-1$
		rtSystem.changeJob(getJobID(), map.getAttributeManager());

		/*
		 * Copy process attributes from map.
		 */
		List<Process> newProcesses = map.getProcesses();
		processIDs = new String[newProcesses.size()];
		IPMachine ipMachine = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getMachineById(rtSystem.getMachineID());
		for (Process newProcess : newProcesses) {
			String nodename = newProcess.getNode().getResolvedName();
			String nodeID = rtSystem.getNodeIDforName(nodename);
			if (nodeID == null) {
				process.destroy();
				throw new CoreException(new Status(IStatus.ERROR, ToolsRMPlugin.getDefault().getBundle().getSymbolicName(), Messages.OpenMPIRuntimeSystemJob_Exception_HostnamesDoNotMatch, parserException));
			}
			
			String processName = newProcess.getName();
			int processIndex = newProcess.getIndex();
			String processID = null;
			if (processIndex == 0) {
				processID = zeroIndexProcessID;
			} else {
				processID = rtSystem.createProcess(getJobID(), processName, processIndex);
			}
			processIDs[processIndex] = processID;

			AttributeManager attrMgr = new AttributeManager();
			attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(processName));
			attrMgr.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodeID));
			attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
			try {
				attrMgr.addAttribute(ProcessAttributes.getIndexAttributeDefinition().create(new Integer(newProcess.getIndex())));
			} catch (IllegalValueException e) {
				// Is always valid.
				assert false;
			}
			attrMgr.addAttributes(newProcess.getAttributeManager().getAttributes());
			rtSystem.changeProcess(processID, attrMgr);

			IPProcessControl processControl = (IPProcessControl) ipJob.getProcessById(processID);
			IPNode node = ipMachine.getNodeById(nodeID);

			/*
			 * Although one could call processControl.addNode(node) to assign the process to the node, this does not work.
			 * It is necessary to call nodeControl.addProcesses(processControl) instead.
			 */
			IPNodeControl nodeControl = (IPNodeControl) node;
			nodeControl.addProcesses(Arrays.asList(new IPProcessControl[] {processControl} ));
		}
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: finished updating model", getJobID()); //$NON-NLS-1$
	}

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
		 * Still experience has shown that remote process might not have yet terminated, although stdout and stderr is closed.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting mpi process to finish completely", getJobID()); //$NON-NLS-1$
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: completely finished", getJobID()); //$NON-NLS-1$
	}

	@Override
	protected void doTerminateJob() {
		// Empty implementation.
	}

	@Override
	protected JobAttributes.State doExecutionFinished(IProgressMonitor monitor) throws CoreException {
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
		if (process.exitValue() != 0) {
			if (!terminateJobFlag) {
				if ((process.exitValue() & 0177) == 0) {
					int exit_code = (process.exitValue()>>8) & 0xff;
					changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailedWithExitValue, new Integer(exit_code)));
				} else {
					int signal = process.exitValue() & 0177;
					changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailedWithSignal, new Integer(signal)));
				}
				return JobAttributes.State.ERROR;
			}
			
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING, "RTS job #{0}: ignoring exit value {1} because job was forced to terminate by user", getJobID(), new Integer(process.exitValue())); //$NON-NLS-1$
		} else if (errorDetected) {
			changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailureDetected, errorMessage));
			return JobAttributes.State.ERROR;
		}
		return JobAttributes.State.TERMINATED;
	}

	/**
	 * Change the state of all processes in a job.
	 * 
	 * @param newState
	 */
	private void changeAllProcessesStatus(State newState) {
		final OpenMPIRuntimeSystem rtSystem = (OpenMPIRuntimeSystem) getRtSystem();
		final IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID());
		if (rm != null) {
			final IPQueue queue = rm.getQueueById(getQueueID());
			if (queue != null) {
				final IPJob ipJob = queue.getJobById(getJobID());
				if (ipJob != null) {

					/*
					 * Mark all running and starting processes as finished.
					 */
					List<String> ids = new ArrayList<String>();
					for (IPProcess ipProcess : ipJob.getProcesses()) {
						switch (ipProcess.getState()) {
						case EXITED:
						case ERROR:
						case EXITED_SIGNALLED:
							break;
						case RUNNING:
						case STARTING:
						case SUSPENDED:
						case UNKNOWN:
							ids.add(ipProcess.getID());
							break;
						}
					}
			
					AttributeManager attrMrg = new AttributeManager();
					attrMrg.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(newState));
					for (String processId : ids) {
						rtSystem.changeProcess(processId, attrMrg);
					}
				}
			}
		}
	}

	@Override
	protected void doExecutionCleanUp(IProgressMonitor monitor) {
		if (process != null) {
			process.destroy();
			process = null;
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
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
	}

	@Override
	protected void doBeforeExecution(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	@Override
	protected IAttribute<?, ?, ?>[] doRetrieveToolBaseSubstitutionAttributes() throws CoreException {
		// TODO make macros available for environment variables and work directory.
		return null;
	}

	@Override
	protected IAttribute<?, ?, ?>[] doRetrieveToolCommandSubstitutionAttributes(
			AttributeManager baseSubstitutionAttributeManager,
			String directory, Map<String, String> environment) {

		List<IAttribute<?, ?, ?>> newAttributes = new ArrayList<IAttribute<?,?,?>>();

		/*
		 * An OpenMPI specific attribute.
		 * Attribute that contains a list of names of environment variables.
		 */
		int p = 0;
		String keys[] = new String[environment.size()];
		for (String key : environment.keySet()) {
			keys[p++] = key;
		}
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentKeysAttributeDefinition().create(keys));

		/*
		 * An OpenMPI specific attribute.
		 * A shortcut that generates arguments for the OpenMPI run command.
		 */
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentArgsAttributeDefinition().create());
		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
	}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment()
	throws CoreException {
		// No extra environment variables needs to be set for OpenMPI.
		return null;
	}

	@Override
	protected void doPrepareExecution(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/**
	 * @return the stderrObserver
	 */
	protected InputStreamObserver getStderrObserver() {
		return stderrObserver;
	}

	/**
	 * @param stderrObserver the stderrObserver to set
	 */
	protected void setStderrObserver(InputStreamObserver stderrObserver) {
		this.stderrObserver = stderrObserver;
	}

	/**
	 * @return the stdoutObserver
	 */
	protected InputStreamObserver getStdoutObserver() {
		return stdoutObserver;
	}

	/**
	 * @param stdoutObserver the stdoutObserver to set
	 */
	protected void setStdoutObserver(InputStreamObserver stdoutObserver) {
		this.stdoutObserver = stdoutObserver;
	}

}
