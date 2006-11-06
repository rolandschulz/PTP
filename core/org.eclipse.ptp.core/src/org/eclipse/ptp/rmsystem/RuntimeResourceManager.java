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

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.PTPCorePlugin;
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

	private final class LocalQueue extends Parent implements IPQueueControl {

		private LocalQueue() {
			super(RuntimeResourceManager.this, "localQueue", "0", 0);
		}
		
		public synchronized void addJob(IPJobControl job) {
			addChild(job);
		}

		public synchronized void clearContents() {
			removeChildren();
		}
		
		public synchronized IPJob getJob(String ID) {
			return getJobControl(ID);
		}

		public synchronized IPJobControl getJobControl(String ID) {
			IPJobControl job = (IPJobControl) findChild(ID);
			return job;
		}

		public synchronized IPJobControl[] getJobControls() {
			return (IPJobControl[]) getCollection().toArray(new IPJobControl[0]);
		}

		public synchronized IPJob[] getJobs() {
			return getJobControls();
		}

		public synchronized void removeJob(IPJobControl job) {
			removeChild(job);
		}
	}

	private IControlSystem controlSystem = null;
	private final LocalQueue localQueue = new LocalQueue();
	private final HashMap machines = new HashMap();
	private IMonitoringSystem monitoringSystem = null;
	private int jobID = 1;
	private IRuntimeProxy runtimeProxy = null;
	
	public RuntimeResourceManager(IPUniverseControl universe, 
			IResourceManagerConfiguration config) {
		super(universe, config);
	}

	public void abortJob(String jobName) throws CoreException {
		/* we have a job name, so let's find it in the ResourceManager - if it exists */
		IPJob j = localQueue.getJob(jobName);
		if(j == null) {
			System.err.println("ERROR: tried to delete a job that was not found '"+jobName+"'");
			return;
		}
		try {
			controlSystem.terminateJob(j);
		} catch(CoreException e) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"The PTP Control System is down.", null);
			return;
		}

		System.err.println("aborted");
		fireEvent(new ModelRuntimeNotifierEvent(j.getIDString(),
				IModelRuntimeNotifierEvent.TYPE_JOB, IModelRuntimeNotifierEvent.ABORTED));
	}

	public void dispose() {
		super.dispose();
	}

	public synchronized IPMachine getMachine(String ID) {
		return (IPMachine) machines.get(ID);
	}

	public synchronized IPMachineControl[] getMachineControls() {
		return (IPMachineControl[]) machines.values().toArray(new IPMachineControl[0]);
	}

	public IPMachine[] getMachines() {
		return getMachineControls();
	}

	public IPQueue getQueue(int id) {
		if (id == 0)
			return localQueue;
		else
			return null;
	}

	public synchronized IPQueueControl[] getQueueControls() {
		return new IPQueueControl[]{ localQueue };
	}

	public IPQueue[] getQueues() {
		return getQueueControls();
	}

	public void refreshRuntimeSystems(IProgressMonitor monitor,
			boolean force) throws CoreException {
		if (!force) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		refreshRuntimeSystems(monitor);
	}

	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException {
		if (!getStatus().equals(ResourceManagerStatus.STARTED)) {
			throw makeCoreException("Resource Manager can not run a job," +
					" it is not currently in the STARTED state.");
		}
		pm.setTaskName("Creating the job...");
		
		IPJob job = newJob(jobRunConfig.getNumberOfProcesses(), jobRunConfig.isDebug(), pm);
		System.out.println("RuntimeResourceManager.run() - new JobID = "+job.getJobNumberInt());

		controlSystem.run(job.getJobNumberInt(), jobRunConfig);
		
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

	public void stop() throws CoreException {
		modelListeners.clear();
		if(monitoringSystem != null)
			monitoringSystem.shutdown();
		if(controlSystem != null)
			controlSystem.shutdown();
		if (runtimeProxy != null)
			runtimeProxy.shutdown();
		super.stop();
	}

	private synchronized void addMachine(String ID, IPMachineControl machine) {
		machines.put(ID, machine);
	}

	private synchronized void clearContents() {
		machines.clear();
		localQueue.clearContents();
	}

	private IPMachineControl getMachineControl(String ID) {
		return (IPMachineControl) machines.get(ID);
	}

	private synchronized IPProcess getProcess(String ID) {
		IPJob[] jobs = localQueue.getJobs();
		for (int i = 0; i < jobs.length; ++i) {
			IPJob job = jobs[i];
			IPProcess proc = job.findProcessByName(ID);
			if (proc != null)
				return proc;
		}
		return null;
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
			}
			if (monitoringSystem != null) {
				monitor.subTask("Shutting down monitor system...");
				monitoringSystem.shutdown();
				monitoringSystem = null;
				monitor.worked(10);
			}
			if (runtimeProxy != null) {
				monitor.subTask("Shutting down runtime proxy...");
				runtimeProxy.shutdown();
				runtimeProxy = null;
				monitor.worked(10);
			}
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			monitor.worked(10);
	
			/* load up the control and monitoring systems for the simulation */
			doStartRuntime(monitor);
	
			clearContents();
			
			monitor.subTask("Starting up monitor system...");
			monitoringSystem.startup();
			monitor.worked(10);
			monitor.subTask("Starting up control system...");
			controlSystem.startup();
			monitor.worked(10);
			try {
				monitor.subTask("Setup the monitoring system...");
				monitor.beginTask("", 1);
				monitoringSystem.addRuntimeListener(this);
				controlSystem.addRuntimeListener(this);		
				monitor.worked(1);
				monitoringSystem.initiateDiscovery();
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

	protected void doDispose() {
		modelListeners.clear();
		if(monitoringSystem != null)
			monitoringSystem.shutdown();
		if(controlSystem != null)
			controlSystem.shutdown();
		if (runtimeProxy != null)
			runtimeProxy.shutdown();
	}

	protected void doStart() throws CoreException {
		refreshRuntimeSystems(new NullProgressMonitor(), true);
	}

	protected abstract void doStartRuntime(IProgressMonitor monitor) throws CoreException;

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
