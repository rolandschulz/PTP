/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.ompi.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.util.ArgumentParser;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.ompi.core.Activator;
import org.eclipse.ptp.rm.ompi.core.OMPIAttributes;
import org.eclipse.ptp.rm.ompi.core.parameters.Parameters;
import org.eclipse.ptp.rm.ompi.core.rmsystem.OMPIResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory;
import org.eclipse.ptp.rtsystem.events.RuntimeEventFactory;

public class OMPIRuntimeSystem extends AbstractRuntimeSystem {
	private class JobQueueManager implements Runnable {
		public void run() {
			try {
				while (connection != null) {
					RTSJob job = pendingJobQueue.take();
					job.start();
				}
			} catch (Exception e) {
			}
		}
	}
	
	private class RTSJob {
		private String jobSubID;
		private String jobID;
		private String name;
		private String user;
		private IRemoteProcessBuilder processBuilder;
		private IRemoteProcess process = null;
		private AttributeManager attrMgr;
		private Thread jobMonitor;
		EnumeratedAttribute<JobAttributes.State> state;
		
		public RTSJob(AttributeManager attrMgr) {
			this.attrMgr = attrMgr;
			this.name = generateJobName();
			this.user = System.getenv("USER");
			this.jobID = generateID().toString();
			this.jobSubID = "JOBSUB_" + jobID;
			this.state = JobAttributes.getStateAttributeDefinition().create(JobAttributes.State.PENDING);
		}
		
		public String getJobID() {
			return jobID;
		}
		
		public String getName() {
			return name;
		}
		
		public IRemoteProcess getRemoteProcess() {
			return process;
		}
		
		public String getSubmissionID() {
			return jobSubID;
		}
		
		public void start() throws CoreException {
			state.setValue(JobAttributes.State.STARTED);
			createJob(queueID, jobID, jobSubID, name, state, user);

			/*
			 * Get attributes required to start job
			 */
			IntegerAttribute nprocs = attrMgr.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
			StringAttribute args = attrMgr.getAttribute(OMPIAttributes.getLaunchArgumentsAttributeDefinition());
			StringAttribute program = attrMgr.getAttribute(JobAttributes.getExecutableNameAttributeDefinition());
			StringAttribute path = attrMgr.getAttribute(JobAttributes.getExecutablePathAttributeDefinition());

			if (nprocs == null || args == null || program == null || path == null) {
				state.setValue(JobAttributes.State.ERROR);
				changeJobAttributes(jobID, state);
				return;
			}
			
			/*
			 * Create launch command
			 */
			processBuilder = remoteServices.getProcessBuilder(connection, "mpirun", args.getValue(),
					path.getValue() + "/" + program.getValue());

			/*
			 * Launch the job
			 */
			try {
				process = processBuilder.start();
			} catch (IOException e) {
				state.setValue(JobAttributes.State.ERROR);
				changeJobAttributes(jobID, state);
				return;
			}

			state.setValue(JobAttributes.State.RUNNING);
			changeJobAttributes(jobID, state, nprocs);
			runningJobQueue.add(this);

			jobMonitor = new Thread(new Runnable() {
				public void run() {
					final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IOException e) {
					}
					
					state.setValue(JobAttributes.State.TERMINATED);
					changeJobAttributes(jobID, state);
					
					RTSJob job = jobs.get(jobID);
					runningJobQueue.remove(job);
					jobs.remove(job);
				}
			});
			jobMonitor.start();
		}
		
		public void terminate() {
			process.destroy();
		}
	}
	
	private String rmID;
	private String machineID;
	private String queueID;
	
	private Integer nextID;
	private Integer jobNumber;
	
	private OMPIResourceManagerConfiguration configuration;
	private AttributeDefinitionManager attrMgr;
	private IRemoteServices remoteServices = null;
	private volatile IRemoteConnection connection = null;
	private IRemoteProcessBuilder monitorBuilder;
	private IRemoteProcess monitor;
	private Thread jobQueueThread = null;

	private Map<String, RTSJob> jobs = Collections.synchronizedMap(new HashMap<String, RTSJob>());
	private LinkedBlockingQueue<RTSJob> pendingJobQueue = new LinkedBlockingQueue<RTSJob>();
	private ConcurrentLinkedQueue<RTSJob> runningJobQueue = new ConcurrentLinkedQueue<RTSJob>();

	private IRuntimeEventFactory eventFactory = new RuntimeEventFactory();
	private Parameters params = new Parameters();
	
	public OMPIRuntimeSystem(Integer id, OMPIResourceManagerConfiguration config, AttributeDefinitionManager manager) {
		this.rmID = id.toString();
		this.nextID = id + 1;
		this.jobNumber = 0;
		this.configuration = config;
		this.attrMgr = manager;
	}

	/**
	 * @return
	 */
	public Parameters getParameters() {
		return params;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#shutdown(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) throws CoreException {
		stopEvents();
		stopJobs();
		if (connection != null) {
			connection.close(monitor);
		}
		connection = null;
		jobQueueThread = null;
		fireRuntimeShutdownStateEvent(eventFactory.newRuntimeShutdownStateEvent());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#startEvents()
	 */
	public void startEvents() throws CoreException {
		if (remoteServices != null && connection != null) {
			/* FIXME not sure if we need this yet
			if (monitorBuilder == null) {
				monitorBuilder = remoteServices.getProcessBuilder(connection, "orte_monitor");
			}
			if (monitor == null) {
				try {
					monitor = monitorBuilder.start();
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
				//Start thread to read monitoring information
			}
			*/
			machineID = createMachine(connection.getName());
			queueID = createQueue("default");
			getNodes(machineID);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystem#startup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(configuration.getRemoteServicesId());
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not find remote services for resource manager"));
		}
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		if (connMgr != null) {
			connection = connMgr.getConnection(configuration.getConnectionName());
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
				initialize(monitor);
			} catch (CoreException e) {
				connection.close(monitor);
				connection = null;
				throw e;
			}
		
			if (!monitor.isCanceled()) {
				fireRuntimeRunningStateEvent(eventFactory.newRuntimeRunningStateEvent());
				if (jobQueueThread == null) {
					jobQueueThread = new Thread(new JobQueueManager(), "Job Queue Manager");
					jobQueueThread.start();
				}
			} else {
				connection.close(monitor);
				connection = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IMonitoringSystem#stopEvents()
	 */
	public void stopEvents() throws CoreException {
		if (monitor != null) {
			monitor.destroy();
			monitor = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#submitJob(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public String submitJob(AttributeManager attrMgr) throws CoreException {
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Resource manager has not be initialized"));
		}
		
		RTSJob job = new RTSJob(attrMgr);
		jobs.put(job.getJobID(), job);
		try {
			pendingJobQueue.put(job);
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		
		return job.getSubmissionID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IControlSystem#terminateJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob job) throws CoreException {
		RTSJob rjob = jobs.get(job.getID());
		if (rjob != null) {
			rjob.terminate();
			jobs.remove(rjob);
		}
	}

	/**
	 * Change job attributes
	 * 
	 * @param id	job ID
	 * @param attrs	new attributes
	 */
	private void changeJobAttributes(String id, IAttribute<?,?,?>... attrs) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		for (IAttribute<?,?,?> attr : attrs) {
			attrMgr.addAttribute(attr);
		}
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeJobChangeEvent(eventFactory.newRuntimeJobChangeEvent(mgr));
	}
	
	/**
	 * Special case of the job change event to change job state
	 * 
	 * @param id	job ID
	 * @param state	new state
	 */
	private void changeJobState(String id, JobAttributes.State state) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(JobAttributes.getStateAttributeDefinition().create(state));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeJobChangeEvent(eventFactory.newRuntimeJobChangeEvent(mgr));
	}
	
	/**
	 * Create a new job event and send to the resource manager
	 * 
	 * @param parentID	parent element ID
	 * @param name		name of the node
	 * @param number	node index (zero-based)
	 * @param state		job state
	 * @param nprocs	number of processes
	 */
	private void createJob(String parentID, String jobID, String jobSubID, String name, 
			EnumeratedAttribute<JobAttributes.State> state, String user) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(state);
		attrMgr.addAttribute(JobAttributes.getSubIdAttributeDefinition().create(jobSubID));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		attrMgr.addAttribute(JobAttributes.getUserIdAttributeDefinition().create(user));
		mgr.setAttributeManager(new RangeSet(jobID), attrMgr);
		fireRuntimeNewJobEvent(eventFactory.newRuntimeNewJobEvent(parentID, mgr));
	}
	
	/**
	 * Create a new machine event and send to the resource manager
	 * 
	 * @param name
	 */
	private String createMachine(String name) {
		String id = generateID().toString();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.UP));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewMachineEvent(eventFactory.newRuntimeNewMachineEvent(rmID, mgr));
		return id;
	}
	
	/**
	 * Create a new node event and send to the resource manager
	 * 
	 * @param parentID	parent element ID
	 * @param name		name of the node
	 * @param number	node index (zero-based)
	 */
	private void createNode(String parentID, String name, int number) {
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
		try {
			attrMgr.addAttribute(NodeAttributes.getNumberAttributeDefinition().create(number));
		} catch (IllegalValueException e) {
		}
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(generateID()), attrMgr);
		fireRuntimeNewNodeEvent(eventFactory.newRuntimeNewNodeEvent(parentID, mgr));
	}
	
	/**
	 * Create a new queue event and send to the resource manager
	 * 
	 * @param name
	 */
	private String createQueue(String name) {
		String id = generateID();
		ElementAttributeManager mgr = new ElementAttributeManager();
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttribute(QueueAttributes.getStateAttributeDefinition().create(QueueAttributes.State.NORMAL));
		attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		mgr.setAttributeManager(new RangeSet(id), attrMgr);
		fireRuntimeNewQueueEvent(eventFactory.newRuntimeNewQueueEvent(rmID, mgr));
		return id;
	}
	
	/**
	 * Generate a new element ID
	 * 
	 * @return new element ID
	 */
	private String generateID() {
		String id = nextID.toString();
		nextID++;
		return id;
	}
	
	/**
	 * Generate a job name
	 * 
	 * @return job name
	 */
	private String generateJobName() {
		return "job" + jobNumber++;
	}
	
	/**
	 * Get OMPI node information
	 * 
	 * @param parent
	 * @throws CoreException
	 */
	private void getNodes(String parentID) throws CoreException {
		int numNodes = 0;
		
		Parameters.Parameter param = params.getParameter("rds_hostfile_path");
		if (param != null) {
			String filename = param.getValue();
			
			if (filename != null) {		
				IProgressMonitor monitor = new NullProgressMonitor();
				
				IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
				IFileStore hostfile;
				try {
					hostfile = fileMgr.getResource(new Path(filename), monitor);
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
				
				/*
				 * Read hostfile
				 */
				final BufferedReader reader = new BufferedReader(new InputStreamReader(hostfile.openInputStream(EFS.NONE, monitor)));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						if (!line.startsWith("#") && !line.equals("")) {
							createNode(parentID, line, numNodes);
							numNodes++;
						}
					}
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
		
		/*
		 * Create fake node if necessary
		 */
		if (numNodes == 0) {
			createNode(parentID, connection.getName(), 0);
		}
	}
	
	/**
	 * Do any necessary initialization. For OMPI, we read the rds parameters to find the
	 * location of the hostfile.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void initialize(IProgressMonitor monitor) throws CoreException {
		String cmd = configuration.getDiscoverCmd();
		if (!cmd.equals("")) {
			ArgumentParser ap = new ArgumentParser(cmd);
			List<String> args = ap.getArguments();
			if (args != null) {
				readParams(args);
			}
		}
	}
	
	/**
	 * Get OMPI parameters
	 * 
	 * @throws CoreException
	 */
	private void readParams(List<String> args) throws CoreException {
		IRemoteProcessBuilder cmdBuilder = remoteServices.getProcessBuilder(connection, args);
		
		IRemoteProcess cmd;
		try {
			cmd = cmdBuilder.start();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		
		final BufferedReader stdout = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
		
		try {
			String line;
			while ((line = stdout.readLine()) != null) {
				int nameStart = line.indexOf(":param:");
				if (nameStart >= 0) {
					nameStart += 7;
					int pos = line.indexOf(":", nameStart);
					if (pos >= 0) {
						String name = line.substring(nameStart, pos);
						Parameters.Parameter param = params.getParameter(name);
						if (param == null) {
							param = params.addParameter(name);
						}
						int pos2;
						if ((pos2 = line.indexOf(":value:", pos)) >= 0) {
							param.setValue(line.substring(pos2 + 7));
						} else if ((pos2 = line.indexOf(":status:", pos)) >= 0) {
							if (line.substring(pos2 + 8).equals("read-only")) {
								param.setReadOnly(true);
							}
						} else if ((pos2 = line.indexOf(":help:", pos)) >= 0) {
							param.setHelp(line.substring(pos2 + 6));
						}
					}
				}
			}
		} catch (IOException e) {
			cmd.destroy();
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		
		cmd.destroy();
	}
	
	/**
	 * Stop all running jobs
	 */
	private void stopJobs() {
		for (RTSJob job : runningJobQueue) {
			job.terminate();
		}
	}
}
