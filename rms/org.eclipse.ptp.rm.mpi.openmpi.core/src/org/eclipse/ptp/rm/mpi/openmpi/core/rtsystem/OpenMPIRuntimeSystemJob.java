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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.rm.core.Activator;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.IOpenMpiProcessMapXml13ParserListener;

public class OpenMPIRuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	Object lock1 = new Object();

	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	/** Information parsed from launch command. */
	OpenMPIProcessMap map;

	/**
	 * Process IDs created by this job. The first process (zero index) is special,
	 * because it is always created.
	 */
	String processIDs[];

	/** Exception raised while parsing mpi map information. */
	IOException parserException = null;

	public OpenMPIRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}

	@Override
	protected void doExecutionStarted() throws CoreException {
		/*
		 * Create a zero index job.
		 */
		final OpenMPIRuntimeSystem rtSystem = (OpenMPIRuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());
		final String zeroIndexProcessID = rtSystem.createProcess(getJobID(), "Open MPI run", 0);
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
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: started", jobID); //$NON-NLS-1$
				BufferedReader stdoutBufferedReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stdoutBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
							DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", jobID, line); //$NON-NLS-1$
						}
						line = stdoutBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: {0}", e); //$NON-NLS-1$
					PTPCorePlugin.log(e);
				} finally {
					stdoutPipedStreamListener.disable();
//					if (stdoutObserver != null) {
//						stdoutObserver.removeListener(stdoutPipedStreamListener);
//					}
//					try {
//						stdoutOutputStream.close();
//					} catch (IOException e) {
//						PTPCorePlugin.log(e);
//					}
//					try {
//						stdoutInputStream.close();
//					} catch (IOException e) {
//						PTPCorePlugin.log(e);
//					}
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: finished", jobID); //$NON-NLS-1$
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
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: started", jobID); //$NON-NLS-1$
				final BufferedReader stderrBufferedReader = new BufferedReader(new InputStreamReader(stderrInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stderrBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							ipProc.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
//							ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
							DebugUtil.error(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", jobID, line); //$NON-NLS-1$
						}
						line = stderrBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: {0}", e); //$NON-NLS-1$
					PTPCorePlugin.log(e);
				} finally {
					stderrPipedStreamListener.disable();
//					if (stderrObserver != null) {
//						stderrObserver.removeListener(stderrPipedStreamListener);
//					}
//					try {
//						stderrOutputStream.close();
//					} catch (IOException e) {
//						PTPCorePlugin.log(e);
//					}
//					try {
//						stderrInputStream.close();
//					} catch (IOException e) {
//						PTPCorePlugin.log(e);
//					}
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: finished", jobID); //$NON-NLS-1$
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
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: started", jobID); //$NON-NLS-1$
				OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
				try {
					// Parse stdout or stderr, depending on mpi 1.2 or 1.3
					if (configuration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
						map = OpenMPIProcessMapText12Parser.parse(parserInputStream);
					} else if (configuration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
						map = OpenMPIProcessMapXml13Parser.parse(parserInputStream, new IOpenMpiProcessMapXml13ParserListener() {
							public void startDocument() {
								// Empty
							}
							public void endDocument() {
								/*
								 * Turn of listener that generates input for parser when parsing finishes.
								 * If not done, the parser will close the piped inputstream, making the listener
								 * get IOExceptions for closed stream.
								 */
								if (stderrObserver != null) {
									parserPipedStreamListener.disable();
									stderrObserver.removeListener(parserPipedStreamListener);
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
					process.destroy();
					DebugUtil.error(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: {0}", e); //$NON-NLS-1$
				} finally {
					parserPipedStreamListener.disable();
					if (stderrObserver != null) {
						stderrObserver.removeListener(parserPipedStreamListener);
					}
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: finished", jobID); //$NON-NLS-1$
			}
		};

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: starting all threads", jobID); //$NON-NLS-1$
		/*
		 * Create and start listeners.
		 */
		stdoutThread.start();
		stderrThread.start();
		parserThread.start();

		stderrObserver = new InputStreamObserver(process.getErrorStream());
		stdoutObserver = new InputStreamObserver(process.getInputStream());

		stdoutObserver.addListener(stdoutPipedStreamListener);
		stderrObserver.addListener(stderrPipedStreamListener);

		// Parse stdout or stderr, depending on mpi 1.2 or 1.3
		OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
		if (configuration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
			stderrObserver.addListener(parserPipedStreamListener);
		} else if (configuration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
			stdoutObserver.addListener(parserPipedStreamListener);
		} else {
			assert false;
		}

		stderrObserver.start();
		stdoutObserver.start();

		try {
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting for display-map parser thread to finish", jobID); //$NON-NLS-1$
			parserThread.join();
		} catch (InterruptedException e) {
			// Do nothing.
		}

		if (parserException != null) {
			process.destroy();
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to parse Open Mpi run command output.", parserException));
		}

		/*
		 * Copy job attributes from map.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: updating model with display-map information", jobID); //$NON-NLS-1$
		rtSystem.changeJob(getJobID(), map.getAttributeManager());

		/*
		 * Copy process attributes from map.
		 */
		List<Process> newProcesses = map.getProcesses();
		processIDs = new String[newProcesses.size()];
		IPMachine ipMachine = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getMachineById(rtSystem.getMachineID());
		for (Process newProcess : newProcesses) {
			String nodename = newProcess.getNode().getName();
			String nodeID = rtSystem.getNodeIDforName(nodename);
			if (nodeID == null) {
				process.destroy();
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Hostnames from Open MPI output do not match expected hostname.", parserException));
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
				attrMgr.addAttribute(ProcessAttributes.getIndexAttributeDefinition().create(newProcess.getIndex()));
			} catch (IllegalValueException e) {
				// Is always valid.
				assert false;
			}
			attrMgr.addAttributes(newProcess.getAttributeManager().getAttributes());
			rtSystem.changeProcess(processID, attrMgr);

			IPProcessControl control = (IPProcessControl) ipJob.getProcessById(processID);
			IPNode node = ipMachine.getNodeById(nodeID);
			control.addNode(node);
		}
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: finished updating model", jobID); //$NON-NLS-1$
	}

	@Override
	protected void doWaitExecution() throws CoreException {
		/*
		 * Wait until both stdout and stderr stop because stream are closed.
		 * This means that the process has finished.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stderr thread to finish", jobID); //$NON-NLS-1$
		try {
			stderrObserver.join();
		} catch (InterruptedException e1) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stdout thread to finish", jobID); //$NON-NLS-1$
		try {
			stdoutObserver.join();
		} catch (InterruptedException e1) {
			// Ignore
		}

		/*
		 * Still experience has shown that remote process might not have yet terminated, although stdout and stderr is closed.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting mpi process to finish completely", jobID); //$NON-NLS-1$
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: completely finished", jobID); //$NON-NLS-1$
	}

	@Override
	protected void doTerminateJob() {
	}

	@Override
	protected void doExecutionFinished() throws CoreException {
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
	}

	private void changeAllProcessesStatus(State newState) {
		final OpenMPIRuntimeSystem rtSystem = (OpenMPIRuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());

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

	@Override
	protected void doExecutionCleanUp() {
		if (process != null) {
			process.destroy();
		}
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}
		// TODO: more cleanup?
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
	}

	@Override
	protected void doBeforeExecution() throws CoreException {
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
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentKeysDefinition().create(keys));

		/*
		 * An OpenMPI specific attribute.
		 * A shortcut that generates arguments for the OpenMPI run command.
		 */
		newAttributes.add(OpenMPILaunchAttributes.getEnvironmentArgsDefinition().create());
		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment()
			throws CoreException {
		// No extra environment variables needs to be set for OpenMPI.
		return null;
	}

	@Override
	protected void doPrepareExecution() throws CoreException {
		// Nothing to do
	}
}
