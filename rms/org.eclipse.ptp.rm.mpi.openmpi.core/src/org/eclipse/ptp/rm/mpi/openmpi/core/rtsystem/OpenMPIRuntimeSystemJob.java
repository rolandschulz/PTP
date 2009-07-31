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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.IInputStreamListener;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;

/**
 * Deal with output from the mpirun command.
 * 
 * The type/format of output depends on a range of factors, including the OMPI version and the capabilities of the
 * remote service provider being used.
 * 
 * OMPI 1.2 generates map data in textual form that must be parsed to extract the relevant information. Map information is
 * sent to stderr, but RSE does not separate stdout/stderr so this must be handled as a special case.
 * 
 * OMPI 1.3.x generates map data in (malformed) XML format so we use an XML parser to extract information. 
 * 
 * OMPI 1.3.[1,2] wrap stdout and stderr from the program in XML tags, but they are sill sent to the respective streams.
 * 
 * OMPI 1.3 does not wrap stdout and stderr from the program in XML tags.
 * 
 * OMPI 1.3.2 adds <noderesolve> elements to the XML map data.
 * 
 * OMPI 1.3.[1,2,3] malform the XML by dropping </stdout> tags on some lines.   
 * 
 * @author Daniel Felix Ferber
 * @author Greg Watson
 *
 */
public class OpenMPIRuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	/** Exception raised while parsing mpi map information. */
	protected Exception parserException = null;
	
	/** Error detected in mpirun output */
	protected boolean errorDetected = false;
	protected String errorMessage = null;
	
	/** Used to signal map information completed */
	protected boolean mapCompleted = false;
	protected final ReentrantLock mapLock = new ReentrantLock();
	protected final Condition mapCondition = mapLock.newCondition();
	
	/** Main parser thread */
	protected Thread parserThread;
	protected InputStreamListenerToOutputStream parserListener;

	public OpenMPIRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
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
	
	protected void addProcess(IPJob job, Process proc) {
		OpenMPIRuntimeSystem rts = (OpenMPIRuntimeSystem)getRtSystem();
		String nodename = proc.getNode().getResolvedName();
		String nodeID = rts.getNodeIDforName(nodename);
		if (nodeID != null) {
			int processIndex = proc.getIndex();
			/*
			 * Use the index as the process name if the process name returned by the map is bogus
			 */
			String processName = proc.getName();
			if (processName.equals("")) { //$NON-NLS-1$
				processName = String.valueOf(processIndex);
			}
			IPProcessControl process = (IPProcessControl)job.getProcessByIndex(processIndex);
			if (process != null) {
				AttributeManager attrMgr = new AttributeManager();
				attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(processName));
				attrMgr.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(nodeID));
				attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
				attrMgr.addAttributes(proc.getAttributeManager().getAttributes());
				getRtSystem().changeProcess(process.getID(), attrMgr);
			}
		}
	}

	/**
	 * Create the parser thread
	 * 
	 */
	protected void createParser(final OpenMPIResourceManagerConfiguration configuration, final IPJob job) {
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
					if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
						OpenMPIProcessMapText12Parser.parse(parserInputStream, new IOpenMPIProcessMapParserListener() {
							public void finish() {
								// Empty
							}

							public void finishMap(AttributeManager manager) {
								/*
								 * Copy job attributes from map.
								 */
								DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: updating model with display-map information", getJobID()); //$NON-NLS-1$
								getRtSystem().changeJob(getJobID(), manager);
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
					} else if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)
								|| configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_14)) {
						InputStream is;
						if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_14)
								|| configuration.getServiceVersion() > 0) {
							is = new OpenMPI131InputStream(parserInputStream);
						} else {
							is = new OpenMPI130InputStream(parserInputStream);
						}
						OpenMPIProcessMapXml13Parser.parse(is, new IOpenMPIProcessMapParserListener() {
							public void finish() {
								/*
								 * Turn off listener that generates input for parser when parsing finishes.
								 * If not done, the parser will close the piped inputstream, making the listener
								 * get IOExceptions for closed stream.
								 */
								getParserListener().disable();
								if (getStdoutObserver() != null) {
									getStdoutObserver().removeListener(getParserListener());
								}
							}
							
							public void finishMap(AttributeManager manager) {
								/*
								 * Copy job attributes from map.
								 */
								DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: updating model with display-map information", getJobID()); //$NON-NLS-1$
								getRtSystem().changeJob(getJobID(), manager);
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
								if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)
										&& configuration.getServiceVersion() < 4) {
									stderr += "\n"; //$NON-NLS-1$
								}
								IPProcessControl process = (IPProcessControl)job.getProcessByIndex(proc.getIndex());
								if (process != null) {
									process.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(stderr));
								}
							}

							public void stdout(Process proc, String output) {
								String stdout = output;
								if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)
										&& configuration.getServiceVersion() < 4) {
									stdout += "\n"; //$NON-NLS-1$
								}
								IPProcessControl process = (IPProcessControl)job.getProcessByIndex(proc.getIndex());
								if (process != null) {
									process.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(stdout));
								}
							}
							
						});
					} else {
						assert false;
					}
				} catch (Exception e) {
					parserException = e;
					DebugUtil.error(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: {1}", getJobID(), e); //$NON-NLS-1$
				} finally {
					if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
						getParserListener().disable();
						if (getStderrObserver() != null) {
							getStderrObserver().removeListener(getParserListener());
						}
					}
					setMapCompleted();
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: display-map parser thread: finished", getJobID()); //$NON-NLS-1$
			}
		};
	}

	@Override
	protected void doBeforeExecution(IProgressMonitor monitor, IRemoteProcessBuilder builder) throws CoreException {
		final OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
		/*
		 * Merge stdout and stderr streams for OMPI 1.3.[1,2] since the streams are wrapped in the appropriate XML tags, but
		 * are still sent separately.
		 */
		if ((configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13) && 
				(configuration.getServiceVersion() > 0 || configuration.getServiceVersion() < 3))) {
			builder.redirectErrorStream(true);
		}
	}

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
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
	}

	@Override
	protected JobAttributes.State doExecutionFinished(IProgressMonitor monitor) throws CoreException {
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
		if (getProcess().exitValue() != 0) {
			if (!terminateJobFlag) {
				changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailedWithExitValue, new Integer(getProcess().exitValue())));
				return JobAttributes.State.ERROR;
			}
			
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING, "RTS job #{0}: ignoring exit value {1} because job was forced to terminate by user", getJobID(), new Integer(getProcess().exitValue())); //$NON-NLS-1$
		} else if (errorDetected) {
			changeJobStatusMessage(NLS.bind(Messages.OpenMPIRuntimeSystemJob_Exception_ExecutionFailureDetected, errorMessage));
			return JobAttributes.State.ERROR;
		}
		return JobAttributes.State.TERMINATED;
	}
	
	@Override
	protected void doExecutionStarted(IProgressMonitor monitor) throws CoreException {
		mapCompleted = false;
		
		/*
		 * Create processes for the job.
		 */
		final OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) getRtSystem().getRmConfiguration();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRtSystem().getRmID()).getQueueById(getQueueID()).getJobById(getJobID());
		IntegerAttribute numProcsAttr = ipJob.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		assert numProcsAttr != null;
		getRtSystem().createProcesses(getJobID(), numProcsAttr.getValue().intValue());
		
		/*
		 * We only require procZero if we're using OMPI 1.2.x or 1.3.0. Other versions use XML
		 * for stdout and stderr.
		 */
		final IPProcess procZero;
		if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)
				|| (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12) 
						&& configuration.getServiceVersion() < 1)) {
			procZero = ipJob.getProcessByIndex(0);
		} else {
			procZero = null;
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
				if (procZero != null) {
					procZero.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}: {1}", getJobID(), line); //$NON-NLS-1$
			}
			
			public void streamClosed() {
				//
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
				if (procZero != null) {
					procZero.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
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
		if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
			 /* 
			  * Fix for bug #271810 
			  */
			if (!getRtSystem().getRemoteServices().getId().equals("org.eclipse.ptp.remote.RSERemoteServices")) { //$NON-NLS-1$
				getStderrObserver().addListener(getParserListener());
			} else {
				getStdoutObserver().addListener(getParserListener());
			}
		} else if (configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_13)
					|| configuration.getDetectedVersion().equals(OpenMPIResourceManagerConfiguration.VERSION_14)) {
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
				
			throw OpenMPIPlugin.coreErrorException("Failed to parse output of Open MPI command. Check output for errors.", parserException); //$NON-NLS-1$
		}	
	}
	
	@Override
	protected void doPrepareExecution(IProgressMonitor monitor) throws CoreException {
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
	protected void doTerminateJob() {
		// Empty implementation.
	}

	@Override
	protected void doWaitExecution(IProgressMonitor monitor) throws CoreException {
		try {
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting for display-map parser thread to finish", getJobID()); //$NON-NLS-1$
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
			throw OpenMPIPlugin.coreErrorException("Failed to parse output of Open MPI command. Check output for errors.", parserException); //$NON-NLS-1$
		}

		/*
		 * Still experience has shown that remote process might not have yet terminated, although stdout and stderr is closed.
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
	 * @param stderrObserver the stderrObserver to set
	 */
	protected void setStderrObserver(InputStreamObserver stderrObserver) {
		this.stderrObserver = stderrObserver;
	}

	/**
	 * @param stdoutObserver the stdoutObserver to set
	 */
	protected void setStdoutObserver(InputStreamObserver stdoutObserver) {
		this.stdoutObserver = stdoutObserver;
	}

	/**
	 * Wait until the map has been read or some other
	 * error occurs.
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

}
