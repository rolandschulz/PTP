/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDescription;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDescription;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.IModelSysChangedEvent;
import org.eclipse.ptp.core.events.INodeEvent;
import org.eclipse.ptp.core.events.IProcessEvent;
import org.eclipse.ptp.core.events.ModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.ModelSysChangedEvent;
import org.eclipse.ptp.core.events.NodeEvent;
import org.eclipse.ptp.core.events.ProcessEvent;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.internal.core.PJob;
import org.eclipse.ptp.internal.core.PMachine;
import org.eclipse.ptp.internal.core.PNode;
import org.eclipse.ptp.internal.core.PProcess;
import org.eclipse.ptp.internal.core.Parent;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public abstract class RuntimeResourceManager extends AbstractResourceManager
		implements IRuntimeListener {

	final class LocalQueue extends Parent implements IPQueueControl {

		private LocalQueue() {
			super(RuntimeResourceManager.this, "localQueue", "0", 0);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#addJob(org.eclipse.ptp.core.elementcontrols.IPJobControl)
		 */
		public synchronized void addJob(IPJobControl job) {
			addChild(job);
		}

		public synchronized void clearContents() {
			removeChildren();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#findJobById(java.lang.String)
		 */
		public IPJobControl findJobById(String job_id) {
			IPJobControl job = (IPJobControl) findChild(job_id);
			return job;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.IPQueue#getJob(java.lang.String)
		 */
		public synchronized IPJob getJob(String name) {
			return getJobControl(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControl(java.lang.String)
		 */
		public synchronized IPJobControl getJobControl(String name) {
			Collection col = getCollection();
			Iterator it = col.iterator();
			while (it.hasNext()) {
				Object ob = it.next();
				if (ob instanceof IPJobControl) {
					IPJobControl job = (IPJobControl) ob;
					if (job.getElementName().equals(name))
						return job;
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControls()
		 */
		public synchronized IPJobControl[] getJobControls() {
			return (IPJobControl[]) getCollection().toArray(new IPJobControl[0]);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.IPQueue#getJobs()
		 */
		public synchronized IPJob[] getJobs() {
			return getJobControls();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.IPQueue#getName()
		 */
		public String getName() {
			return getElementName();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.IPQueue#getResourceManager()
		 */
		public IResourceManager getResourceManager() {
			return RuntimeResourceManager.this;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#removeJob(org.eclipse.ptp.core.elementcontrols.IPJobControl)
		 */
		public synchronized void removeJob(IPJobControl job) {
			removeChild(job);
		}
	}
	private static final IAttributeDescription firstNodeAttrDesc =
		new AttributeDescription("FirstNodeNumber", "First Node Number");
	private static final IAttributeDescription nProcsAttrDesc = 
		new AttributeDescription("NumProcs", "Number of Processes");
	private static final IAttributeDescription nProcsPerNodeAttrDesc =
		new AttributeDescription("NumProcsPerNode", "Number of Procs Per Node");

	private IControlSystem controlSystem = null;
	private int jobID = 1;
	private LocalQueue localQueue = null;
	private IMonitoringSystem monitoringSystem = null;
	private IRuntimeProxy runtimeProxy = null;
	
	public RuntimeResourceManager(IPUniverseControl universe, 
			IResourceManagerConfiguration config) {
		super(universe, config);
		localQueue = new LocalQueue();
		addQueue(localQueue.getIDString(), localQueue);
	}

	public synchronized boolean abortJob(String jobName) throws CoreException {
		/* we have a job name, so let's find it in the ResourceManager - if it exists */
		IPJob j = localQueue.getJob(jobName);
		if (j == null) {
			return false;
		}
		try {
			controlSystem.terminateJob(j);
		} catch(CoreException e) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"The PTP Control System is down.", null);
			return false;
		}

		System.err.println("aborted");
		fireEvent(new ModelRuntimeNotifierEvent(j.getIDString(),
				IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.ABORTED));
		return true;
	}

	public void dispose() {
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getLaunchAttributes(org.eclipse.ptp.core.elementcontrols.IPMachineControl, org.eclipse.ptp.core.elementcontrols.IPQueueControl)
	 */
	public IAttribute[] getLaunchAttributes(IPMachineControl machine, IPQueueControl queue) {
		IAttribute[] attrs = new IAttribute[3];
		try {
			IntegerAttribute nProcsPerNodeAttr = new IntegerAttribute(nProcsPerNodeAttrDesc, 1);
			nProcsPerNodeAttr.setValidRange(1, Integer.MAX_VALUE);
			IntegerAttribute firstNodeAttr = new IntegerAttribute(firstNodeAttrDesc, 0);
			firstNodeAttr.setValidRange(0, Integer.MAX_VALUE);
			IntegerAttribute nProcsAttr = new IntegerAttribute(nProcsAttrDesc, 1);
			nProcsAttr.setValidRange(1, Integer.MAX_VALUE);
			attrs[0] = nProcsAttr;
			attrs[1] = firstNodeAttr;
			attrs[2] = nProcsPerNodeAttr;
		} catch (IllegalValue e) {
			throw new RuntimeException(e);
		}
		return attrs;
	}

	public void removeJob(IPJob job) {
		localQueue.removeJob((IPJobControl) job);
	}
	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException {
		if (!getStatus().equals(ResourceManagerStatus.STARTED)) {
			throw makeCoreException("Resource Manager can not run a job," +
					" it is not currently in the STARTED state.");
		}
		pm.setTaskName("Creating the job...");

		int firstNodeNum = -1;
		int nProcs = -1;
		int nProcsPerNode = -1;
		IAttribute[] attrs = jobRunConfig.getLaunchAttributes();
		for (int i=0; i<attrs.length; ++i) {
			IAttributeDescription desc = attrs[i].getDescription();
			if (desc.equals(firstNodeAttrDesc)) {
				firstNodeNum = Integer.parseInt(attrs[i].getStringRep());
			}
			else if (desc.equals(nProcsAttrDesc)) {
				nProcs = Integer.parseInt(attrs[i].getStringRep());
			}
			else if (desc.equals(nProcsPerNodeAttrDesc)) {
				nProcsPerNode = Integer.parseInt(attrs[i].getStringRep());
			}
		}
		IPJob job = newJob(nProcs, jobRunConfig.isDebug(), pm);
		System.out.println("RuntimeResourceManager.run() - new JobID = "+job.getJobNumberInt());

		controlSystem.run(job.getJobNumberInt(), nProcs, firstNodeNum, nProcsPerNode,
				jobRunConfig);
		
		return job;
	}

	public void runtimeJobExited(String ID) {
		// TODO not used
	}

	public void runtimeJobStateChanged(String ID, String state) {
		System.out.println("*********** JOB STATE CHANGE: "+state+" (job = "+ID+")");
		IPJob job = localQueue.getJob(ID);
		if (job != null) {
			IPProcess[] procs = job.getProcesses();
			if (procs != null) {
				for (int i = 0; i < procs.length; i++) {
					procs[i].setStatus(state);
					fireEvent(new ProcessEvent(procs[i], IProcessEvent.STATUS_CHANGE_TYPE,
							procs[i].getStatus()));
					if (procs[i].getStatus().equals(IPProcess.EXITED)) {
						IPNode node = procs[i].getNode();
						//FIXME why node can be null???
						if (node != null) {
							fireEvent(new NodeEvent(node, INodeEvent.STATUS_UPDATE_TYPE, null));
						}
					}
				}
			}
			if (state.equals("running")) {
				fireEvent(new ModelRuntimeNotifierEvent(job.getIDString(),
						IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.RUNNING));
			} else if (state.equals("exited")) {
				fireEvent(new ModelRuntimeNotifierEvent(job.getIDString(),
						IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.STOPPED));
			} else if (state.equals("starting")) {
				fireEvent(new ModelRuntimeNotifierEvent(job.getIDString(),
						IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.STARTED));
			}
		}
	}

	public void runtimeNewJob(String ID) {
		// TODO not used		
	}

	public void runtimeNodeGeneralChange(String[] keys, String[] values) {
		boolean newEntity = false;
		IPMachineControl curmachine = null;
		IPNodeControl curnode = null;
		
		boolean one_node_changed = false;
		IPNodeControl the_one_changed_node = null;
		
		System.out.println("RuntimeResourceManager.runtimeNodeGeneralName - #keys = " +
				keys.length+"," + " #values = "+values.length);
		for(int i=0; i<keys.length; i++) {
			String key = keys[i];
			String value = values[i];
			if (key.equals(AttributeConstants.ATTRIB_MACHINEID)) {
				/* ok, so we're switching to this new machine.  Let's find it. */
				curmachine = getMachineControl(value);
				if(curmachine == null) {
					System.out.println("\t\tUnknown machine ID ("+value+"), adding to the model.");
					curmachine = new PMachine(this,
							AttributeConstants.ATTRIB_MACHINE_NAME_PREFIX + value, value);
					addMachine(value, curmachine);
					newEntity = true;
				}
			} else if (curmachine != null && key.equals(AttributeConstants.ATTRIB_NODE_NUMBER)) {
				/* ok so we've got a machine that's not null, and we think we have a node
				 * number to look for in that machine.  So let's find it!
				 */
				curnode = (PNode)curmachine.findNodeByName(
						AttributeConstants.ATTRIB_NODE_NAME_PREFIX + value);
				if (curnode == null) {
					System.out.println("\t\tUnknown node number ("+value+"), " +
							" adding to the model.");
					curnode = new PNode(curmachine,
							AttributeConstants.ATTRIB_NODE_NAME_PREFIX + value, value);
					curmachine.addNode(curnode);
					newEntity = true;
				}
				if (the_one_changed_node == null) {
					the_one_changed_node = curnode;
					one_node_changed = true;
				} else {
					one_node_changed = false;
				}
			} else if (curmachine != null && curnode != null) {
				curnode.setAttribute(key, value);
			} else {
				System.err.println(
						"\t!!! ERROR: Received key/value attribute pair " +
						"but have no associated machine/node to assign it to.");
			}
		}
		
		if (newEntity) {
			fireEvent(new ModelSysChangedEvent(IModelSysChangedEvent.MAJOR_SYS_CHANGED, null));
		}
		if (one_node_changed && the_one_changed_node != null) {
			fireEvent(new NodeEvent(the_one_changed_node, INodeEvent.STATUS_UPDATE_TYPE, null));
		} else {
			/* ok more than 1 node changed, too complex let's just let them know to do a refresh */
			fireEvent(new ModelSysChangedEvent(IModelSysChangedEvent.SYS_STATUS_CHANGED, null));
		}
	}

	public void runtimeProcAttrChange(String nejob, BitList cprocs, String kv,
			int[] dprocs, String[] kvs) {
		System.out.println("*********** PROC ATTRIBUTE CHANGE: (job = "+nejob+")");
		IPJob job = localQueue.getJob(nejob);

		if (job == null) {
			return;
		}
		
		/* 
		 * First deal with common processes
		 */

		/*
		 * Now deal with different processes
		 */
		for (int i = 0; i < dprocs.length; i++) {
			IPProcess proc = job.findProcessByName(nejob+"_process"+dprocs[i]);
			String[] attr = kvs[i].split("=");
			if (attr.length == 2 && proc != null) {
				if (attr[0].equals(AttributeConstants.ATTRIB_PROCESS_PID)) {
					System.err.println("setting pid[" + proc.getName() + "]=" + attr[1]);
					proc.setPid(attr[1]);
				} else if (attr[0].equals(AttributeConstants.ATTRIB_PROCESS_NODE_NAME)) {
					IPMachine machine = (IPMachine) machines.values().iterator().next();
					if (attr[1].equals("localhost")) {
						IPNode node = machine.findNodeByName(
								AttributeConstants.ATTRIB_NODE_NAME_PREFIX + "0");
						proc.setNode(node);
					} else {
						IPNode[] nodes = machine.getNodes();
						for (int j = 0; j < nodes.length; j++) {
							IPNode node = nodes[j];
							final Object attribute = node.getAttribute(
									AttributeConstants.ATTRIB_NODE_NAME);
							if (attribute.equals(attr[1])) {
								System.err.println("setting node[" + proc.getName() + "]=" +
										attr[1] + "(" + node.getNodeNumber() + ")");
								proc.setNode(node);
								break;
							}
						}
					}
				}
			}
		}
	}

	public void runtimeProcessOutput(String ID, String output) {
		IPProcess p = getProcess(ID);
		if (p != null) {
			p.addOutput(output);
			fireEvent(new ProcessEvent(p, IProcessEvent.ADD_OUTPUT_TYPE, output + "\n"));
		}
	}

	public void shutdown() throws CoreException {
		if(monitoringSystem != null)
			monitoringSystem.shutdown();
		if(controlSystem != null)
			controlSystem.shutdown();
		if (runtimeProxy != null)
			runtimeProxy.shutdown();
		monitoringSystem = null;
		controlSystem = null;
		runtimeProxy = null;
		super.shutdown();
	}

	private synchronized void clearContents() {
		machines.clear();
		localQueue.clearContents();
	}

	private synchronized IPJob newJob(int numProcesses, boolean debug,
			IProgressMonitor monitor) throws CoreException {
		int jobID = newJobID();
		String jobName = "job"+jobID;
		System.out.println("Runtime ResourceManager: newJob("+jobID+")");
		PJob job = new PJob(localQueue, jobName, "" + (PJob.BASE_OFFSET + jobID) + "", jobID);		
		if (debug)
			job.setDebug();
		
		localQueue.addJob(job);
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask("", numProcesses);
		monitor.setTaskName("Creating processes....");
		/* we know that we succeeded, so we can create this many procs in the job.  we just
		 * need to run getProcsStatusForNewJob() to fill in the status later
		 */
		for (int i = 0; i < numProcesses; i++) {		
			IPProcessControl proc = new PProcess(job, jobName+"_process"+i, "" + i + "", "0", i, IPProcess.STARTING, "", "");
			job.addProcess(proc);			
		}
	
		/*
		 * This is needed for debug jobs because the runtimeJobStateChanged event is
		 * not generated (the debugger manages the process/job state) and as a consequence the
		 * UI JobManager listener is never called.
		 */
		if (debug) {
			fireEvent(new ModelRuntimeNotifierEvent(job.getIDString(), IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.STARTED));
		}
		return job;
	}

	private synchronized int newJobID() {
		return this.jobID++;
	}

	private synchronized void refreshRuntimeSystems(IProgressMonitor monitor) throws CoreException {
		System.err.println("refreshRuntimeSystems");
		try {
			monitor.beginTask("Refreshing runtime system...", 200);
			/*
			 * Shutdown runtime if it is already active
			 */
			System.out.println("SHUTTING DOWN CONTROL/MONITORING/PROXY systems where appropriate");
			if (controlSystem != null) {
				monitor.subTask("Shutting down control system...");
				controlSystem.shutdown();
				controlSystem = null;
				monitor.worked(10);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException("refresh runtime systems -- cancled");
				}
			}
			if (monitoringSystem != null) {
				monitor.subTask("Shutting down monitor system...");
				monitoringSystem.shutdown();
				monitoringSystem = null;
				monitor.worked(10);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException("refresh runtime systems -- cancled");
				}
			}
			if (runtimeProxy != null) {
				monitor.subTask("Shutting down runtime proxy...");
				runtimeProxy.shutdown();
				runtimeProxy = null;
				monitor.worked(10);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException("refresh runtime systems -- cancled");
				}
			}
			monitor.worked(10);
	
			/* load up the control and monitoring systems for the simulation */
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			doStartRuntime(subMonitor);

			if (monitor.isCanceled()) {
				throw new OperationCanceledException("refresh runtime systems -- cancled");
			}
	
			clearContents();
			
			monitor.subTask("Starting up monitor system...");
			monitoringSystem.startup();
			if (monitor.isCanceled()) {
				throw new OperationCanceledException("refresh runtime systems -- cancled");
			}
			monitor.worked(10);
			monitor.subTask("Starting up control system...");
			controlSystem.startup();
			if (monitor.isCanceled()) {
				throw new OperationCanceledException("refresh runtime systems -- cancled");
			}
			monitor.worked(10);
			try {
				monitor.subTask("Setup the monitoring system...");
				monitor.beginTask("", 1);
				monitoringSystem.addRuntimeListener(this);
				controlSystem.addRuntimeListener(this);	
				monitor.worked(1);
				monitoringSystem.initiateDiscovery();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException("refresh runtime systems -- cancled");
				}
				monitor.done();
			} catch (CoreException e) {
				clearContents();
				throw e;
			}
			monitor.worked(10);
			fireEvent(new ModelSysChangedEvent(IModelSysChangedEvent.MONITORING_SYS_CHANGED, null));
		} finally {
			if (!monitor.isCanceled())
				monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDisableEvents()
	 */
	protected void doDisableEvents() {
		// no-op		
	}

	protected void doDispose() {
		// stop should be called by super.dispose(), so there is
		// nothing left to do.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doEnableEvents()
	 */
	protected void doEnableEvents() {
		// no-op
	}

	protected abstract void doStartRuntime(IProgressMonitor monitor) throws CoreException;

	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Refresh Runtime Systems", 10);
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
		try {
			refreshRuntimeSystems(subMonitor);
		}
		finally {
			monitor.done();
		}
	}

	protected IControlSystem getControlSystem() {
		return controlSystem;
	}

	protected IMonitoringSystem getMonitoringSystem() {
		return monitoringSystem;
	}

	protected IRuntimeProxy getRuntimeProxy() {
		return runtimeProxy;
	}
	
	protected void setControlSystem(IControlSystem system) {
		controlSystem = system;
	}

	protected void setMonitoringSystem(IMonitoringSystem system) {
		monitoringSystem = system;
	}

	protected void setRuntimeProxy(IRuntimeProxy proxy) {
		runtimeProxy = proxy;
	}


}
