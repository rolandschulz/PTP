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
package org.eclipse.ptp.rm.core.rtsystem;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.Activator;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rtsystem.AbstractRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeEventFactory;

/*
 * TODO: Synchronize methods to avoid race conditions
 * TODO: Split this class into two: the tools RTS and a command tools RTS.
 */
public abstract class AbstractToolRuntimeSystem extends AbstractRuntimeSystem {
	/**
	 * Executes jobs from the queue.
	 * @author dfferber
	 * TODO: Is this JobRunner really required? Why not dispatching the jobs immediately?
	 */
	private class JobRunner implements Runnable {
		public void run() {
			DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: started job thread", rmConfiguration.getName()); //$NON-NLS-1$
			try {
				while (connection != null) {
					Job job = pendingJobQueue.take();
					if (job instanceof IToolRuntimeSystemJob)
						DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: schedule job #{1}", rmConfiguration.getName(), ((IToolRuntimeSystemJob)job).getJobID()); //$NON-NLS-1$
					else
						DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: schedule job #{1}", rmConfiguration.getName(), job.getName());
					job.schedule();
				}
			} catch (InterruptedException e) {
				// Ignore
			} catch (Exception e) {
				DebugUtil.error(DebugUtil.JOB_TRACING, "RTS {0}: {1}", rmConfiguration.getName(), e); //$NON-NLS-1$
				PTPCorePlugin.log(e);
			}
			DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: terminated job thread", rmConfiguration.getName()); //$NON-NLS-1$
		}
	}

	/** The RM id of the RM manager that created the RTS. */
	private String rmID;

	/** Id generator for machines, queues and nodes. */
	private Integer nextID;

	/** Id generator for jobs. */
	private Integer jobNumber;

	/** A local reference of the RM configuration used by the RM manager that created the RTS. */
	protected AbstractToolRMConfiguration rmConfiguration;

	/** Attribute definitions for the RTS. */
	protected AttributeDefinitionManager attrMgr;

	/** Remote Service used to run commands on the remote host. */
	protected IRemoteServices remoteServices = null;

	/** Track if events are enabled. */
	private boolean eventsEnabled = false;


	protected IRemoteConnection connection = null;

	private Thread jobQueueThread = null;

	/** Job to monitor remote system and is executed periodically. */
	private Job periodicMonitorJob;

	/** Job to monitor remote system and is executed continuously. */
	private Job continousMonitorJob;

	/** Jobs created by this RTS. */
	Map<String, Job> jobs = Collections.synchronizedMap(new HashMap<String, Job>());

	/** Jobs created, but not yet started. */
	private LinkedBlockingQueue<Job> pendingJobQueue = new LinkedBlockingQueue<Job>();

	/** Jobs created and started. */
//	ConcurrentLinkedQueue<IToolRuntimeSystemJob> runningJobQueue = new ConcurrentLinkedQueue<IToolRuntimeSystemJob>();

	/** Helper object to create events for the RM. */
	private IRuntimeEventFactory eventFactory = new RuntimeEventFactory();

	public AbstractToolRuntimeSystem(Integer id, AbstractToolRMConfiguration config, AttributeDefinitionManager manager) {
		this.rmID = id.toString();
		this.nextID = id + 1;
		this.jobNumber = 0;
		this.rmConfiguration = config;
		this.attrMgr = manager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#startup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: startup", rmConfiguration.getName()); //$NON-NLS-1$
		remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmConfiguration.getRemoteServicesId());
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not find remote services for resource manager"));
		}
		IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
		Assert.isNotNull(connectionManager);

		connection = connectionManager.getConnection(rmConfiguration.getConnectionName());
		if (connection == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not find connection for resource manager"));
		}

		if (!connection.isOpen()) {
			try {
				connection.open(monitor);
			} catch (RemoteConnectionException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}

		if (monitor.isCanceled()) {
			connection.close(monitor);
			connection = null;
			return;
		}

		try {
			doStartup(monitor);
		} catch (CoreException e) {
			connection.close(monitor);
			connection = null;
			throw e;
		}

		Job discoverJob = createDiscoverJob();
		if (discoverJob != null) {
			discoverJob.schedule();
		}

		if (!monitor.isCanceled()) {
			fireRuntimeRunningStateEvent(eventFactory.newRuntimeRunningStateEvent());
			if (jobQueueThread == null) {
				jobQueueThread = new Thread(new JobRunner(), "Job Queue Manager");
				jobQueueThread.start();
			}
		} else {
			connection.close(monitor);
			connection = null;
		}
	}

	/**
	 * Template method to extend the startup procedure.
	 * @param monitor The progress monitor.
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#shutdown(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: shutdown", rmConfiguration.getName()); //$NON-NLS-1$
		doShutdown(monitor);

		stopEvents();

		/*
		 * Stop jobs that might be in the pending queue.
		 * Also stop the thread that dispatches pending jobs.
		 */
		jobQueueThread.interrupt();
		for (Job job : pendingJobQueue) {
			job.cancel();
		}

		/*
		 * Stop jobs that are running or that already finished.
		 */
		Iterator<Job> iterator = jobs.values().iterator();
		while (iterator.hasNext()) {
			Job job = iterator.next();
			job.cancel();
			iterator.remove();
		}

		/*
		 * Close the the connection.
		 */
		if (connection != null) {
			connection.close(monitor);
		}

		connection = null;
		jobQueueThread = null;
		fireRuntimeShutdownStateEvent(eventFactory.newRuntimeShutdownStateEvent());
	}

	/**
	 * Template method to extend the shutdown procedure.
	 * @param monitor The progress monitor.
	 * @throws CoreException
	 */
	protected abstract void doShutdown(IProgressMonitor monitor) throws CoreException;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#startEvents()
	 */
	public void startEvents() throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: start events", rmConfiguration.getName()); //$NON-NLS-1$
		/*
		 * Create monitor jobs, if they do not already exist. They may exist but be suspended.
		 * If the job is not applicate, then no job will be created, according to capabilities for the RM.
		 */
		if (periodicMonitorJob == null) {
			periodicMonitorJob = createPeriodicMonitorJob();
		}
		if (continousMonitorJob == null) {
			continousMonitorJob = createContinuousMonitorJob();
		}
		/*
		 * Only schedule the job if they are available. If the job does not exists, then it was
		 * not created because the capability is not defined in the RM.
		 */
		if (periodicMonitorJob != null) {
			periodicMonitorJob.schedule();
		}
		if (continousMonitorJob != null) {
			continousMonitorJob.schedule();
		}
		doStartEvents();
	}

	/**
	 * Template method to extend the startEvents procedure.
	 * @throws CoreException
	 */
	protected abstract void doStartEvents() throws CoreException;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#stopEvents()
	 */
	public void stopEvents() throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: stop events", rmConfiguration.getName()); //$NON-NLS-1$
		if (periodicMonitorJob != null) {
			periodicMonitorJob.cancel();
		}
		if (continousMonitorJob != null) {
			continousMonitorJob.cancel();
		}
		doStopEvents();
	}

	/**
	 * Template method to extend the stopEvents procedure.
	 * @throws CoreException
	 */
	protected abstract void doStopEvents() throws CoreException;

	/**
	 * Creates a job that discovers the remote machine. The default implementation runs the discover
	 * command if defined in the RM capability.
	 * @return
	 */
	protected abstract Job createDiscoverJob() throws CoreException;

	/**
	 * Creates a job that periodically monitors the remote machine. The default implementation runs the periodic monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	protected abstract Job createPeriodicMonitorJob() throws CoreException;

	/**
	 * Creates a job that keeps monitoring the remote machine. The default implementation runs the continuous monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	protected abstract Job createContinuousMonitorJob() throws CoreException;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#submitJob(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void submitJob(String subId, AttributeManager attrMgr) throws CoreException {
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource manager has not be initialized"));
		}

		/*
		 * Add some more attributes to the launch information.
		 */
		attrMgr.addAttribute(JobAttributes.getSubIdAttributeDefinition().create(subId));

		/*
		 * Create the IPJob.
		 */
		String queueID = attrMgr.getAttribute(JobAttributes.getQueueIdAttributeDefinition()).getValue();
		String jobID = createJob(queueID, attrMgr);
		attrMgr.addAttribute(JobAttributes.getJobIdAttributeDefinition().create(jobID));

		DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: job submission #{0}, job id #{1}, queue id @{2}", rmConfiguration.getName(), subId, jobID, queueID); //$NON-NLS-1$

		/*
		 * Create the job that runs the application.
		 */
		Job job = createRuntimeSystemJob(jobID, queueID, attrMgr);
		jobs.put(jobID, job);
		try {
			pendingJobQueue.put(job);
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
	}

	abstract public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) throws CoreException;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#terminateJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob ipJob) throws CoreException {
		DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: terminate job #{1}", rmConfiguration.getName(), ipJob.getID()); //$NON-NLS-1$
		Job j = jobs.get(ipJob.getID());
		j.cancel();
		// TODO implement properly
		//		IToolRuntimeSystemJob rjob = jobs.get(job.getID());
//		if (rjob != null) {
//			rjob.terminate();
//			jobs.remove(rjob);
//		}
	}

	/**
	 * Notify RM to change job attributes.
	 *
	 * @param id job ID
	 * @param attrs new attributes
	 */
//	protected void changeJobAttributes(String id, IAttribute<?, ?, ?>... attrs) {
//		ElementAttributeManager mgr = new ElementAttributeManager();
//		AttributeManager attrMgr = new AttributeManager();
//		for (IAttribute<?, ?, ?> attr : attrs) {
//			attrMgr.addAttribute(attr);
//		}
//		mgr.setAttributeManager(new RangeSet(id), attrMgr);
//		fireRuntimeJobChangeEvent(eventFactory.newRuntimeJobChangeEvent(mgr));
//	}

//	/**
//	 * Notify RM to change job state.
//	 *
//	 * @param id job ID
//	 * @param state new state
//	 */
//	protected void changeJobState(String id, JobAttributes.State state) {
//		ElementAttributeManager mgr = new ElementAttributeManager();
//		AttributeManager attrMgr = new AttributeManager();
//		attrMgr.addAttribute(JobAttributes.getStateAttributeDefinition().create(state));
//		mgr.setAttributeManager(new RangeSet(id), attrMgr);
//		fireRuntimeJobChangeEvent(eventFactory.newRuntimeJobChangeEvent(mgr));
//	}

	/**
	 * Notify RM to create a new job.
	 *
	 * @param parentID parent element ID
	 * @param jobID the ID of the job
	 * @param attrMgr attributes from the job thread
	 */
	public String createJob(String parentID, AttributeManager attrMgr) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager jobAttrMgr = new AttributeManager();

		/*
		 * Add generated attributes.
		 */
		String jobID = generateID().toString();
		jobAttrMgr.addAttribute(JobAttributes.getJobIdAttributeDefinition().create(jobID));
		jobAttrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(parentID));
		jobAttrMgr.addAttribute(JobAttributes.getStateAttributeDefinition().create(JobAttributes.State.PENDING));
		jobAttrMgr.addAttribute(JobAttributes.getUserIdAttributeDefinition().create(System.getenv("USER")));
		jobAttrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(generateJobName()));

		/*
		 * Get relevant attributes from launch attributes.
		 */
		String subId = attrMgr.getAttribute(JobAttributes.getSubIdAttributeDefinition()).getValue();
		String execName  = attrMgr.getAttribute(JobAttributes.getExecutableNameAttributeDefinition()).getValue();
		String execPath = attrMgr.getAttribute(JobAttributes.getExecutablePathAttributeDefinition()).getValue();
		String workDir = attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue();
		Integer numProcs = attrMgr.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition()).getValue();
		List<String> progArgs = attrMgr.getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition()).getValue();
		Boolean debugFlag = attrMgr.getAttribute(JobAttributes.getDebugFlagAttributeDefinition()).getValue();

		/*
		 * Copy these relevant attributes to IPJob.
		 */
		jobAttrMgr.addAttribute(JobAttributes.getSubIdAttributeDefinition().create(subId));
		jobAttrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(execName));
		jobAttrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(execPath));
		jobAttrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(workDir));
		try {
			jobAttrMgr.addAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition().create(numProcs));
		} catch (IllegalValueException e) {
			PTPCorePlugin.log(e);
		}
		jobAttrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(progArgs.toArray(new String[0])));
		jobAttrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(debugFlag));
		
		/*
		 * Notify RM.
		 */
		mgr.setAttributeManager(new RangeSet(jobID), jobAttrMgr);
		fireRuntimeNewJobEvent(eventFactory.newRuntimeNewJobEvent(parentID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new job #{1}", rmConfiguration.getName(), jobID); //$NON-NLS-1$

		return jobID;
	}


	/**
	 * Notify RM to create a new machine.
	 * @param name name of the machine
	 * @return the id of the new machine
	 */
	public String createMachine(String name) {
		String id = generateID().toString();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.UP));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewMachineEvent(eventFactory.newRuntimeNewMachineEvent(rmID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new machine #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	/**
	 * Notify RM to create a new node.
	 * @param parentID The ID of the machine the node belongs to
	 * @param name the name of the node
	 * @param number the number of the node (rank)
	 * @return the id of the new node
	 */
	public String createNode(String parentID, String name, int number) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
		try {
			attrMgr.addAttribute(NodeAttributes.getNumberAttributeDefinition().create(number));
		} catch (IllegalValueException e) {
		}
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewNodeEvent(eventFactory.newRuntimeNewNodeEvent(parentID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new node #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	/**
	 * Notify RM to create a new queue.
	 *
	 * @param name the name of the queue
	 * @return the id of the new queue
	 */
	public String createQueue(String name) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(QueueAttributes.getStateAttributeDefinition().create(QueueAttributes.State.NORMAL));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewQueueEvent(eventFactory.newRuntimeNewQueueEvent(rmID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new queue #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	/**
	 * Notify RM to create a new process.
	 *
	 * @param parentID the id of the job the process belongs to
	 * @param name the name of the process
	 * @param index the index (rank) of the process
	 * @return the id of the new process
	 */
	public String createProcess(String parentID, String name, int index) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();

		attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.STARTING));
		try {
			attrMgr.addAttribute(ProcessAttributes.getIndexAttributeDefinition().create(index));
		} catch (IllegalValueException e) {
			// This is not possible
			Assert.isTrue(false);
		}
		attrMgr.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(parentID));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewProcessEvent(eventFactory.newRuntimeNewProcessEvent(parentID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new process #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	public void changeProcess(String processID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(processID), attrMgr);
		IRuntimeProcessChangeEvent event = eventFactory.newRuntimeProcessChangeEvent(elementAttrs);
		fireRuntimeProcessChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}, process #{1}: {2}={3}", rmConfiguration.getName(), processID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	public void changeJob(String jobID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(jobID), attrMgr);
		IRuntimeJobChangeEvent event = eventFactory.newRuntimeJobChangeEvent(elementAttrs);
		fireRuntimeJobChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}, job #{1}: {2}={3}", rmConfiguration.getName(), jobID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	public void changeNode(String nodeID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(nodeID), attrMgr);
		IRuntimeNodeChangeEvent event = eventFactory.newRuntimeNodeChangeEvent(elementAttrs);
		fireRuntimeNodeChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}, node #{1}: {2}={3}", rmConfiguration.getName(), nodeID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	public void changeMachine(String machineID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(machineID), attrMgr);
		IRuntimeMachineChangeEvent event = eventFactory.newRuntimeMachineChangeEvent(elementAttrs);
		fireRuntimeMachineChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}, machine #{1}: {2}={3}", rmConfiguration.getName(), machineID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	/**
	 * Generate a new element ID
	 *
	 * @return new element ID
	 */
	protected String generateID() {
		// TODO: Add RM id?
		String id = nextID.toString();
		nextID++;
		return id;
	}

	/**
	 * Generate a job name
	 *
	 * @return job name
	 */
	protected String generateJobName() {
		return "job" + jobNumber++;
	}

	public IRemoteProcessBuilder createProcessBuilder(List<String> command) {
		return remoteServices.getProcessBuilder(connection, command);
	}

	public IRemoteProcessBuilder createProcessBuilder(List<String> command, String workdir) {
		IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
		IFileStore directory = null;
		try {
			directory = fileManager.getResource(new Path(workdir), new NullProgressMonitor());
		} catch (IOException e) {
			e.printStackTrace();
		}
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, command);
		processBuilder.directory(directory);
		return processBuilder;
	}

	public String getRmID() {
		return rmID;
	}

	public AbstractToolRMConfiguration getRmConfiguration() {
		return rmConfiguration;
	}

	public IRemoteConnection getConnection() {
		return connection;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}
}
