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
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IProcessChangeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IProcessListener;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2JobAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2LaunchAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;

/**
 * 
 * @author Daniel Felix Ferber
 * @author Greg Watson
 *
 */
public class MPICH2RuntimeSystemJob extends AbstractToolRuntimeSystemJob {
	
	public Object lock1 = new Object();
	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	protected final ReentrantLock procsLock = new ReentrantLock();
	protected final Condition procsCondition = procsLock.newCondition();
	protected int numRunningProcs = 0;

	protected IProcessListener processListener = new IProcessListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.IProcessChangeEvent)
		 */
		public void handleEvent(IProcessChangeEvent e) {
			if (e.getAttributes().getAttribute(ProcessAttributes.getStateAttributeDefinition()) != null
					&& e.getSource().getState() == ProcessAttributes.State.RUNNING) {
				procsLock.lock();
				try {
					numRunningProcs++;
					procsCondition.signalAll();
				} finally {
					procsLock.unlock();
				}
			}
		}
	};
	
	protected IJobChildListener jobChildListener = new IJobChildListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
		 */
		public void handleEvent(IChangedProcessEvent e) {
			// ignore
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewProcessEvent)
		 */
		public void handleEvent(INewProcessEvent e) {
			for (IPProcess process : e.getProcesses()) {
				process.addElementListener(processListener);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
		 */
		public void handleEvent(IRemoveProcessEvent e) {
			for (IPProcess process : e.getProcesses()) {
				process.removeElementListener(processListener);
			}
		}
	};
	
	public MPICH2RuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}

	private void changeAllProcessesStatus(State newState) {
		final MPICH2RuntimeSystem rtSystem = (MPICH2RuntimeSystem) getRtSystem();
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
	protected void doBeforeExecution(IProgressMonitor monitor, IRemoteProcessBuilder builder) throws CoreException {
		final MPICH2RuntimeSystem rtSystem = (MPICH2RuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());
		ipJob.addChildListener(jobChildListener);
	}

	@Override
	protected void doExecutionCleanUp(IProgressMonitor monitor) {
		if (getProcess() != null) {
			getProcess().destroy();
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
	protected JobAttributes.State doExecutionFinished(IProgressMonitor monitor) throws CoreException {
		changeAllProcessesStatus(ProcessAttributes.State.EXITED);
		if (getProcess().exitValue() != 0) {
			if (!terminateJobFlag) {
				changeJobStatusMessage(NLS.bind(Messages.MPICH2RuntimeSystemJob_Exception_ExecutionFailedWithExitValue, new Integer(getProcess().exitValue())));
				return JobAttributes.State.ERROR;
			}
			
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING, "RTS job #{0}: ignoring exit value {1} because job was forced to terminate by user", getJobID(), new Integer(getProcess().exitValue())); //$NON-NLS-1$
		}
		return JobAttributes.State.TERMINATED;
	}

	@Override
	protected void doExecutionStarted(IProgressMonitor monitor) throws CoreException {
		final MPICH2RuntimeSystem rtSystem = (MPICH2RuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());

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
				try {
					String line = stdoutBufferedReader.readLine();
					while (line != null) {
						int index = 0;
						int pos = line.indexOf(": "); //$NON-NLS-1$
						if (pos > 0) {
							try {
								index = Integer.parseInt(line.substring(0, pos));
								line = line.substring(pos+1);
							} catch (NumberFormatException e) {
								// ignore
							}
						}
						synchronized (lock1) {
							IPProcess ipProc = ipJob.getProcessByIndex(index);
							if (ipProc != null) {
								ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
							}
							DebugUtil.trace(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", getJobID(), line); //$NON-NLS-1$
						}
						line = stdoutBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stdout thread: {0}", e); //$NON-NLS-1$
					MPICH2Plugin.log(e);
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
				try {
					String line = stderrBufferedReader.readLine();
					while (line != null) {
						int index = 0;
						int pos = line.indexOf(": "); //$NON-NLS-1$
						if (pos > 0) {
							try {
								index = Integer.parseInt(line.substring(0, pos));
								line = line.substring(pos+1);
							} catch (NumberFormatException e) {
								// ignore
							}
						}
						synchronized (lock1) {
							IPProcess ipProc = ipJob.getProcessByIndex(index);
							if (ipProc != null) {
								ipProc.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
							}
							DebugUtil.error(DebugUtil.RTS_JOB_OUTPUT_TRACING, "RTS job #{0}:> {1}", getJobID(), line); //$NON-NLS-1$
						}
						line = stderrBufferedReader.readLine();
					}
				} catch (IOException e) {
					DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: {1}", getJobID(), e); //$NON-NLS-1$
					MPICH2Plugin.log(e);
				} finally {
					stderrPipedStreamListener.disable();
				}
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: stderr thread: finished", getJobID()); //$NON-NLS-1$
			}
		};

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: starting all threads", getJobID()); //$NON-NLS-1$
		/*
		 * Create and start listeners.
		 */
		stdoutThread.start();
		stderrThread.start();

		stderrObserver = new InputStreamObserver(getProcess().getErrorStream());
		stdoutObserver = new InputStreamObserver(getProcess().getInputStream());

		stdoutObserver.addListener(stdoutPipedStreamListener);
		stderrObserver.addListener(stderrPipedStreamListener);

		stderrObserver.start();
		stdoutObserver.start();
		
		/*
		 * At this point we need to pause until all processes have started. This is because
		 * the model semantics are such that the job state must not be set to RUNNING until
		 * all the job's processes (if there are any) have been created and also set to RUNNING.
		 */
		
		/*
		 * We know that a MPICH2 job has a number of processes attribute
		 */
		int numProcs = 1;
		IntegerAttribute numProcsAttr = getAttrMgr().getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		if (numProcsAttr != null) {
			numProcs = numProcsAttr.getValue().intValue();
		}
		
		procsLock.lock();
		try {
			while (!monitor.isCanceled() && numRunningProcs < numProcs) {
				try {
					procsCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		} finally {
			procsLock.unlock();
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
		 * An MPICH2 specific attribute.
		 * Attribute that contains a list of names of environment variables.
		 */
		int p = 0;
		String keys[] = new String[environment.size()];
		for (String key : environment.keySet()) {
			keys[p++] = key;
		}
		newAttributes.add(MPICH2LaunchAttributes.getEnvironmentKeysAttributeDefinition().create(keys));

		/*
		 * An MPICH2 specific attribute.
		 * A shortcut that generates arguments for the MPICH2 run command.
		 */
		newAttributes.add(MPICH2LaunchAttributes.getEnvironmentArgsAttributeDefinition().create());
		
		/*
		 * The jobid is used to alias the MPICH2 job so that it can be matched later.
		 */
		newAttributes.add(MPICH2JobAttributes.getJobIdAttributeDefinition().create(getJobID()));
		
		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
	}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment()
	throws CoreException {
		// No extra environment variables needs to be set for MPICH2.
		return null;
	}

	@Override
	protected void doTerminateJob() {
		// Empty implementation.
	}

	@Override
	protected void doWaitExecution(IProgressMonitor monitor) throws CoreException {
		/*
		 * Wait until both stdout and stderr stop because stream are closed.
		 * This means that the process has finished.
		 */
		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stderr thread to finish", getJobID()); //$NON-NLS-1$
		try {
			stderrObserver.join();
		} catch (InterruptedException e1) {
			// Ignore
		}

		DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: waiting stdout thread to finish", getJobID()); //$NON-NLS-1$
		try {
			stdoutObserver.join();
		} catch (InterruptedException e1) {
			// Ignore
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

}
