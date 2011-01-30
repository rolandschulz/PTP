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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.MPIJobAttributes;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.eclipse.ptp.rm.core.messages.Messages;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeEventFactory;
import org.eclipse.ptp.utils.core.RangeSet;

/**
 * Implements the Runtime System to support calling command line tools to
 * discover, monitor and launch parallel applications. TODO: Synchronize methods
 * to avoid race conditions TODO: Split this class into two: the tools RTS and a
 * command tools RTS.
 * 
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolRuntimeSystem extends AbstractRuntimeSystem {
	/**
	 * Executes jobs from the queue.
	 * 
	 * @author dfferber TODO: Is this JobRunner really required? Why not
	 *         dispatching the jobs immediately?
	 */
	class JobRunner implements Runnable {
		public void run() {
			DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: started job thread", rmConfiguration.getName()); //$NON-NLS-1$
			try {
				while (connection != null) {
					Job job = pendingJobQueue.take();
					if (job instanceof IToolRuntimeSystemJob) {
						DebugUtil.trace(DebugUtil.JOB_TRACING,
								"RTS {0}: schedule job #{1}", rmConfiguration.getName(), ((IToolRuntimeSystemJob) job).getJobID()); //$NON-NLS-1$
					} else {
						DebugUtil.trace(DebugUtil.JOB_TRACING,
								"RTS {0}: schedule job #{1}", rmConfiguration.getName(), job.getName()); //$NON-NLS-1$
					}
					job.schedule();
				}
			} catch (InterruptedException e) {
				// Ignore
			} catch (Exception e) {
				DebugUtil.error(DebugUtil.JOB_TRACING, "RTS {0}: {1}", rmConfiguration.getName(), e); //$NON-NLS-1$
				RMCorePlugin.log(e);
			}
			DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: terminated job thread", rmConfiguration.getName()); //$NON-NLS-1$
		}
	}

	/** Job to monitor remote system and is executed continuously. */
	private Job continousMonitorJob;

	/** Helper object to create events for the RM. */
	private final IRuntimeEventFactory eventFactory = new RuntimeEventFactory();

	/** Id generator for jobs. */
	private int jobNumber;

	private Thread jobQueueThread = null;

	/** Jobs created by this RTS. */
	private final Map<String, Job> jobs = Collections.synchronizedMap(new HashMap<String, Job>());

	/** Id generator for machines, queues and nodes. */
	private int nextID;

	/** Job to monitor remote system and is executed periodically. */
	private Job periodicMonitorJob;

	/** Progress monitor for startup. Used to cancel startup if necessary */
	private IProgressMonitor startupMonitor = null;

	private final IResourceManagerControl fResourceManager;

	/** Attribute definitions for the RTS. */
	protected AttributeDefinitionManager attrMgr;

	protected IRemoteConnection connection = null;

	/** Jobs created, but not yet started. */
	protected LinkedBlockingQueue<Job> pendingJobQueue = new LinkedBlockingQueue<Job>();

	/** Remote Service used to run commands on the remote host. */
	protected IRemoteServices remoteServices = null;

	/**
	 * A local reference of the RM configuration used by the RM manager that
	 * created the RTS.
	 */
	protected IToolRMConfiguration rmConfiguration;

	/**
	 * @since 3.0
	 */
	public AbstractToolRuntimeSystem(IResourceManagerControl rm, IToolRMConfiguration config, AttributeDefinitionManager manager) {
		fResourceManager = rm;
		this.jobNumber = 0;
		this.rmConfiguration = config;
		this.attrMgr = manager;
	}

	/**
	 * Change attributes of a job
	 * 
	 * @param jobID
	 * @param changedAttrMgr
	 */
	public void changeJob(String jobID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(jobID), attrMgr);
		IRuntimeJobChangeEvent event = eventFactory.newRuntimeJobChangeEvent(elementAttrs);
		fireRuntimeJobChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil
					.trace(DebugUtil.RTS_TRACING,
							"RTS {0}, job #{1}: {2}={3}", rmConfiguration.getName(), jobID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	/**
	 * Change attributes of a machine
	 * 
	 * @param machineID
	 * @param changedAttrMgr
	 */
	public void changeMachine(String machineID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(machineID), attrMgr);
		IRuntimeMachineChangeEvent event = eventFactory.newRuntimeMachineChangeEvent(elementAttrs);
		fireRuntimeMachineChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil
					.trace(DebugUtil.RTS_TRACING,
							"RTS {0}, machine #{1}: {2}={3}", rmConfiguration.getName(), machineID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	/**
	 * Change attributes of a node
	 * 
	 * @param nodeID
	 * @param changedAttrMgr
	 */
	public void changeNode(String nodeID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(nodeID), attrMgr);
		IRuntimeNodeChangeEvent event = eventFactory.newRuntimeNodeChangeEvent(elementAttrs);
		fireRuntimeNodeChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil
					.trace(DebugUtil.RTS_TRACING,
							"RTS {0}, node #{1}: {2}={3}", rmConfiguration.getName(), nodeID, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	/**
	 * Change attributes of a process
	 * 
	 * @param jobId
	 *            id of processes' parent job
	 * @param processJobRanks
	 * @param changedAttrMgr
	 * @since 2.0
	 */
	public void changeProcesses(String jobId, BitSet processJobRanks, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(processJobRanks), attrMgr);
		IRuntimeProcessChangeEvent event = eventFactory.newRuntimeProcessChangeEvent(jobId, elementAttrs);
		fireRuntimeProcessChangeEvent(event);

		for (IAttribute<?, ?, ?> attr : changedAttrMgr.getAttributes()) {
			DebugUtil
					.trace(DebugUtil.RTS_TRACING,
							"RTS {0}, processes #{1}: {2}={3}", rmConfiguration.getName(), processJobRanks, attr.getDefinition().getId(), attr.getValueAsString()); //$NON-NLS-1$
		}
	}

	/**
	 * Notify RM to create a new job.
	 * 
	 * @param parentID
	 *            parent element ID
	 * @param jobID
	 *            the ID of the job
	 * @param attrMgr
	 *            attributes from the job thread
	 * @return job id of the newly created job
	 */
	public String createJob(String parentID, AttributeManager attrMgr) throws CoreException {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager jobAttrMgr = new AttributeManager();

		/*
		 * Add generated attributes.
		 */
		String jobID = generateID().toString();
		jobAttrMgr.addAttribute(JobAttributes.getJobIdAttributeDefinition().create(jobID));
		jobAttrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(parentID));
		jobAttrMgr.addAttribute(JobAttributes.getStatusAttributeDefinition().create(MPIJobAttributes.Status.NORMAL.toString()));
		jobAttrMgr.addAttribute(JobAttributes.getUserIdAttributeDefinition().create(System.getenv("USER"))); //$NON-NLS-1$
		jobAttrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(generateJobName()));

		/*
		 * Get mandatory launch attributes.
		 */
		String subId = getAttributeValue(JobAttributes.getSubIdAttributeDefinition(), attrMgr);
		String execName = getAttributeValue(JobAttributes.getExecutableNameAttributeDefinition(), attrMgr);
		String execPath = getAttributeValue(JobAttributes.getExecutablePathAttributeDefinition(), attrMgr);
		String workDir = getAttributeValue(JobAttributes.getWorkingDirectoryAttributeDefinition(), attrMgr);
		Integer numProcs = getAttributeValue(JobAttributes.getNumberOfProcessesAttributeDefinition(), attrMgr);
		List<? extends String> progArgs = getAttributeValue(JobAttributes.getProgramArgumentsAttributeDefinition(), attrMgr);

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
			RMCorePlugin.log(e);
		}
		jobAttrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(progArgs.toArray(new String[0])));

		/*
		 * Copy optional attributes
		 */
		BooleanAttribute debugAttr = attrMgr.getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		if (debugAttr != null) {
			jobAttrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(debugAttr.getValue()));
		}

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
	 * 
	 * @param name
	 *            name of the machine
	 * @return the id of the new machine
	 */
	public String createMachine(String name) {
		String id = generateID().toString();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.UP));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		IPResourceManager rm = (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
		fireRuntimeNewMachineEvent(eventFactory.newRuntimeNewMachineEvent(rm.getID(), mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new machine #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	/**
	 * Notify RM to create a new node.
	 * 
	 * @param parentID
	 *            The ID of the machine the node belongs to
	 * @param name
	 *            the name of the node
	 * @param number
	 *            the number of the node (rank)
	 * @return the id of the new node
	 */
	public String createNode(String parentID, String name, int number) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
		try {
			attrMgr.addAttribute(NodeAttributes.getNumberAttributeDefinition().create(new Integer(number)));
		} catch (IllegalValueException e) {
			/*
			 * This exception is not possible, since number is always valid.
			 */
			RMCorePlugin.log(e);
			assert false;
		}
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewNodeEvent(eventFactory.newRuntimeNewNodeEvent(parentID, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new node #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	/**
	 * Create a single process.
	 * 
	 * @param jobId
	 *            the parent job that the process belongs to
	 * @param the
	 *            index (job rank) of the new process
	 * @since 2.0
	 */
	public void createProcess(String jobId, int index) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.STARTING));
		mgr.setAttributeManager(new RangeSet(index), attrMgr);
		fireRuntimeNewProcessEvent(eventFactory.newRuntimeNewProcessEvent(jobId, mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new process #{1}", rmConfiguration.getName(), Integer.toString(index)); //$NON-NLS-1$
	}

	public IRemoteProcessBuilder createProcessBuilder(List<String> command) {
		return remoteServices.getProcessBuilder(connection, command);
	}

	public IRemoteProcessBuilder createProcessBuilder(List<String> command, String workdir) {
		IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, command);
		if (fileManager != null) {
			processBuilder.directory(fileManager.getResource(workdir));
		}
		return processBuilder;
	}

	/**
	 * Create num new processes with process indexes 0 to num-1.
	 * 
	 * @param job
	 *            the parent job that the processes belong to
	 * @param the
	 *            number of process to create
	 */
	public void createProcesses(String jobId, int num) {
		for (int index = 0; index < num; index++) {
			createProcess(jobId, index);
		}

		DebugUtil.trace(DebugUtil.RTS_TRACING,
				"RTS {0}: created {1} new processes", rmConfiguration.getName(), Integer.valueOf(num)); //$NON-NLS-1$
	}

	/**
	 * Notify RM to create a new queue.
	 * 
	 * @param name
	 *            the name of the queue
	 * @return the id of the new queue
	 */
	public String createQueue(String name) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		IPResourceManager rm = (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
		fireRuntimeNewQueueEvent(eventFactory.newRuntimeNewQueueEvent(rm.getID(), mgr));

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: new queue #{1}", rmConfiguration.getName(), id); //$NON-NLS-1$

		return id;
	}

	abstract public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) throws CoreException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IMonitoringSystem#filterEvents(org.eclipse.ptp
	 * .core.elements.IPElement, boolean,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void filterEvents(IPElement element, boolean filterChildren, AttributeManager filterAttributes) throws CoreException {
		doFilterEvents(element, filterChildren, filterAttributes);
	}

	public IRemoteConnection getConnection() {
		return connection;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	public IToolRMConfiguration getRmConfiguration() {
		return rmConfiguration;
	}

	/**
	 * @since 3.0
	 */
	public IResourceManagerControl getResourceManager() {
		return fResourceManager;
	}

	public abstract AbstractEffectiveToolRMConfiguration retrieveEffectiveToolRmConfiguration();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#shutdown()
	 */
	public void shutdown() throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: shutdown", rmConfiguration.getName()); //$NON-NLS-1$

		doShutdown();

		stopEvents();

		/*
		 * Stop jobs that might be in the pending queue. Also stop the thread
		 * that dispatches pending jobs.
		 */
		if (jobQueueThread != null) {
			jobQueueThread.interrupt();
			for (Job job : pendingJobQueue) {
				job.cancel();
			}
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

		synchronized (this) {
			if (startupMonitor != null) {
				startupMonitor.setCanceled(true);
			}
		}

		/*
		 * Close the the connection.
		 */
		if (connection != null) {
			connection.close();
		}

		jobQueueThread = null;
		fireRuntimeShutdownStateEvent(eventFactory.newRuntimeShutdownStateEvent());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#startEvents()
	 */
	public void startEvents() throws CoreException {
		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: start events", rmConfiguration.getName()); //$NON-NLS-1$
		/*
		 * Create monitor jobs, if they do not already exist. They may exist but
		 * be suspended. If the job is not applicable, then no job will be
		 * created, according to capabilities for the RM.
		 */
		if (periodicMonitorJob == null) {
			periodicMonitorJob = createPeriodicMonitorJob(null);
		}
		if (continousMonitorJob == null) {
			continousMonitorJob = createContinuousMonitorJob(null);
		}
		/*
		 * Only schedule the job if they are available. If the job does not
		 * exists, then it was not created because the capability is not defined
		 * in the RM.
		 */
		if (periodicMonitorJob != null) {
			periodicMonitorJob.schedule();
		}
		if (continousMonitorJob != null) {
			continousMonitorJob.schedule();
		}
		doStartEvents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeSystem#startup(org.eclipse.core.runtime
	 * .IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);

		synchronized (this) {
			startupMonitor = subMon;
		}

		subMon.subTask(Messages.AbstractToolRuntimeSystem_1);

		DebugUtil.trace(DebugUtil.RTS_TRACING, "RTS {0}: startup", rmConfiguration.getName()); //$NON-NLS-1$

		try {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmConfiguration.getRemoteServicesId(),
					subMon.newChild(10));
			if (remoteServices == null) {
				throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID,
						Messages.AbstractToolRuntimeSystem_Exception_NoRemoteServices));
			}

			IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
			Assert.isNotNull(connectionManager);

			subMon.worked(10);
			subMon.subTask(Messages.AbstractToolRuntimeSystem_2);

			connection = connectionManager.getConnection(rmConfiguration.getConnectionName());
			if (connection == null) {
				throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID,
						Messages.AbstractToolRuntimeSystem_Exception_NoConnection));
			}

			if (!connection.isOpen()) {
				try {
					connection.open(subMon.newChild(50));
				} catch (RemoteConnectionException e) {
					throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID, e.getMessage()));
				}
			}

			if (subMon.isCanceled()) {
				connection.close();
				return;
			}

			try {
				doStartup(subMon.newChild(40));
			} catch (CoreException e) {
				connection.close();
				throw e;
			}

			if (subMon.isCanceled()) {
				connection.close();
				return;
			}

			/*
			 * Wait for discover job to complete so we can check it's status and
			 * throw an exception if it fails.
			 */
			Job discoverJob = createDiscoverJob(subMon.newChild(50));
			if (discoverJob != null) {
				discoverJob.schedule();
				try {
					discoverJob.join();
				} catch (InterruptedException e) {
					// Just check result
				}
				IStatus status = discoverJob.getResult();
				if (!status.isOK()) {
					throw new CoreException(status);
				}
			}

			if (jobQueueThread == null) {
				jobQueueThread = new Thread(new JobRunner(), Messages.AbstractToolRuntimeSystem_JobQueueManagerThreadTitle);
				jobQueueThread.start();
			}

			fireRuntimeRunningStateEvent(eventFactory.newRuntimeRunningStateEvent());
		} finally {
			synchronized (this) {
				startupMonitor = null;
			}
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IControlSystem#submitJob(org.eclipse.ptp.core
	 * .attributes.AttributeManager)
	 */
	public void submitJob(String subId, AttributeManager attrMgr) throws CoreException {
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID,
					Messages.AbstractToolRuntimeSystem_Exception_ResourceManagerNotInitialized));
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

		DebugUtil.trace(DebugUtil.JOB_TRACING,
				"RTS {0}: job submission #{0}, job id #{1}, queue id @{2}", rmConfiguration.getName(), subId, jobID, queueID); //$NON-NLS-1$

		/*
		 * Create the job that runs the application.
		 */
		Job job = createRuntimeSystemJob(jobID, queueID, attrMgr);
		jobs.put(jobID, job);
		try {
			pendingJobQueue.put(job);
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID, e.getMessage()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IControlSystem#terminateJob(org.eclipse.ptp.
	 * core.elements.IPJob)
	 */
	public void terminateJob(IPJob ipJob) throws CoreException {
		DebugUtil.trace(DebugUtil.JOB_TRACING, "RTS {0}: terminate job #{1}", rmConfiguration.getName(), ipJob.getID()); //$NON-NLS-1$
		Job job = jobs.get(ipJob.getID());
		pendingJobQueue.remove(job);
		job.cancel();
	}

	/**
	 * Safely retrieve an attribute value.
	 * 
	 * @param attrDef
	 *            attribute definition of the attribute to retrieve
	 * @param attrMgr
	 *            attribute manager containing the attributes
	 * @return value of the attribute
	 * @throws CoreException
	 *             if the attribute does not exist.
	 */
	private <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> T getAttributeValue(D attrDef,
			AttributeManager attrMgr) throws CoreException {
		IAttribute<T, A, D> attr = attrMgr.getAttribute(attrDef);
		if (attr == null) {
			throw new CoreException(new Status(IStatus.ERROR, RMCorePlugin.PLUGIN_ID, NLS.bind(
					Messages.AbstractToolRuntimeSystem_3, attrDef.getName())));
		}
		return attr.getValue();
	}

	/**
	 * Creates a job that keeps monitoring the remote machine. The default
	 * implementation runs the continuous monitor command if defined in the RM
	 * capability.
	 * 
	 * @param monitor
	 *            progress monitor used to report progress, or null to use the
	 *            system provided progress monitor
	 * @return continuous monitor job
	 */
	protected abstract Job createContinuousMonitorJob(IProgressMonitor monitor) throws CoreException;

	/**
	 * Creates a job that discovers the remote machine. The default
	 * implementation runs the discover command if defined in the RM capability.
	 * 
	 * @param monitor
	 *            progress monitor used to report progress, or null to use the
	 *            system provided progress monitor
	 * @return discover job
	 */
	protected abstract Job createDiscoverJob(IProgressMonitor monitor) throws CoreException;

	/**
	 * Creates a job that periodically monitors the remote machine. The default
	 * implementation runs the periodic monitor command if defined in the RM
	 * capability.
	 * 
	 * @param monitor
	 *            progress monitor used to report progress, or null to use the
	 *            system provided progress monitor
	 * @return periodic monitor job
	 */
	protected abstract Job createPeriodicMonitorJob(IProgressMonitor monitor) throws CoreException;

	/**
	 * Template method to extend the filterEvents procedure.
	 * 
	 * @param element
	 *            The element to filter
	 * @param filterChildren
	 *            filter children
	 * @param filterAttributes
	 *            attributes to filter
	 * @throws CoreException
	 */
	protected abstract void doFilterEvents(IPElement element, boolean filterChildren, AttributeManager filterAttributes)
			throws CoreException;

	/**
	 * Template method to extend the shutdown procedure.
	 * 
	 * @param monitor
	 *            The progress monitor.
	 * @throws CoreException
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * Template method to extend the startEvents procedure.
	 * 
	 * @throws CoreException
	 */
	protected abstract void doStartEvents() throws CoreException;

	/**
	 * Template method to extend the startup procedure.
	 * 
	 * @param monitor
	 *            The progress monitor.
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * Template method to extend the stopEvents procedure.
	 * 
	 * @throws CoreException
	 */
	protected abstract void doStopEvents() throws CoreException;

	/**
	 * Generate a new element ID
	 * 
	 * @return new element ID
	 * @since 2.0
	 */
	protected String generateID() {
		// TODO: Add RM id?
		String id = Integer.toString(generateIntID());
		return id;
	}

	/**
	 * Generate a range set of count IDs
	 * 
	 * @param count
	 *            number of IDs to generate
	 * @return range set containing IDs
	 */
	protected RangeSet generateIdRange(int count) {
		int start = nextID;
		nextID += count;
		return new RangeSet(start, nextID - 1);
	}

	/**
	 * @since 2.0
	 */
	protected int generateIntID() {
		int id = nextID;
		nextID++;
		return id;
	}

	/**
	 * Generate a job name
	 * 
	 * @return job name
	 */
	protected String generateJobName() {
		return "job" + jobNumber++; //$NON-NLS-1$
	}
}
