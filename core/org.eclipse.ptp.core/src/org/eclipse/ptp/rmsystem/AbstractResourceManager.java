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
/**
 * 
 */
package org.eclipse.ptp.rmsystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.internal.core.PElement;
import org.eclipse.ptp.internal.core.PJob;
import org.eclipse.ptp.internal.core.PMachine;
import org.eclipse.ptp.internal.core.PNode;
import org.eclipse.ptp.internal.core.PProcess;
import org.eclipse.ptp.internal.core.PQueue;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewQueuesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerChangedJobsEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerChangedMachinesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerChangedNodesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerChangedProcessesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerChangedQueuesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerErrorEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerNewJobsEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerNewMachinesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerNewNodesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerNewProcessesEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerNewQueuesEvent;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager extends PElement implements IResourceManager,
IResourceManagerControl {
	
	private static IAttribute[] getDefaultAttributes(IResourceManagerConfiguration config) {
		IAttribute nameAttr = null;
		
		try {
			 nameAttr = AttributeDefinitionManager.getNameAttributeDefinition().create(config.getName());
		} catch (IllegalValueException e) {
		}
		
		return new IAttribute[]{nameAttr};
	}

	private final ListenerList listeners = new ListenerList();

	private final IResourceManagerConfiguration config;
	private ResourceManagerAttributes.State state;
	private String statusMessage;
	private AttributeDefinitionManager attrDefManager;

	private final HashMap<Integer, IPJobControl> jobs = new HashMap<Integer, IPJobControl>();
	private final HashMap<Integer, IPMachineControl> machines = new HashMap<Integer, IPMachineControl>();
	private final HashMap<Integer, IPNodeControl> nodes = new HashMap<Integer, IPNodeControl>();
	private final HashMap<Integer, IPProcessControl> processes = new HashMap<Integer, IPProcessControl>();
	private final HashMap<Integer, IPQueueControl> queues = new HashMap<Integer, IPQueueControl>();

	public AbstractResourceManager(int id, IPUniverseControl universe, IResourceManagerConfiguration config)
	{
		super(id, universe, P_RESOURCE_MANAGER, getDefaultAttributes(config));
		this.config = config;
		this.state = ResourceManagerAttributes.State.STOPPED;
		this.statusMessage = this.state.toString();
		this.attrDefManager = new AttributeDefinitionManager();
		this.attrDefManager.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		this.attrDefManager.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		this.attrDefManager.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		this.attrDefManager.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		this.attrDefManager.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		this.attrDefManager.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void addResourceManagerListener(IResourceManagerListener listener) {
		System.out.println("listener: " + listener + " added to " + this);
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#disableEvents()
	 */
	public void disableEvents() throws CoreException {
	    if (getState().equals(ResourceManagerAttributes.State.STARTED)) {
	        doDisableEvents();
	        setState(ResourceManagerAttributes.State.SUSPENDED);
            fireSuspended();
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#dispose()
	 */
	public void dispose() {
		listeners.clear();
		SafeRunnable.run(new SafeRunnable(){
			public void run() throws Exception {
				shutdown();
			}});
		doDispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#enableEvents()
	 */
	public void enableEvents() throws CoreException {
	    if (getState().equals(ResourceManagerAttributes.State.SUSPENDED)) {
	        doEnableEvents();
	        setState(ResourceManagerAttributes.State.STARTED);
            fireStarted();
	    }
	}

	public synchronized IPJob findJobById(String job_id) {
		IPQueue[] queues = getQueues();
		for (int j = 0; j < queues.length; ++j) {
			IPJob job = queues[j].getJob(Integer.parseInt(job_id));
			if (job != null) {
				return job;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		} else {
			return super.getAdapter(adapter);
		}
	}

	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return attrDefManager;
	}
	
	/**
	 * Lookup an attribute definition
	 * 
	 * @param attrId
	 * @return attribute definition
	 */
	public IAttributeDefinition getAttributeDefinition(String attrId) {
		return attrDefManager.getAttributeDefinition(attrId);
	}
	
	/**
	 * @return
	 */
	public IResourceManagerConfiguration getConfiguration() {
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getDescription()
	 */
	public String getDescription() {
		return config.getDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.PElement#getID()
	 */
	@Override
	public int getID() {
		// needed this to get around draconian plug-in
		// library restrictions
		return super.getID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.PElement#getIDString()
	 */
	@Override
	public String getIDString() {
		return getConfiguration().getResourceManagerId();
	}

	/**
	 * @param ID
	 * @return
	 */
	public synchronized IPJob getJob(int id) {
		return jobs.get(id);
	}

	public synchronized IPJobControl[] getJobControls() {
		return (IPJobControl[]) jobs.values().toArray(new IPJobControl[0]);
	}

	public IPJob[] getJobs() {
		return getJobControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getLaunchAttributes(java.lang.String, org.eclipse.ptp.core.attributes.IAttribute[])
	 */
	public IAttribute[] getLaunchAttributes(String queueName, IAttribute[] currentAttrs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public synchronized IPMachine getMachine(int id) {
		return machines.get(id);
	}

	public synchronized IPMachineControl[] getMachineControls() {
		return (IPMachineControl[]) machines.values().toArray(new IPMachineControl[0]);
	}
	
	public IPMachine[] getMachines() {
		return getMachineControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getName()
	 */
	public String getName() {
		return config.getName();
	}
	
	public synchronized IPNode getNode(int id) {
		return nodes.get(id);
	}

	public synchronized IPNodeControl[] getNodeControls() {
		return (IPNodeControl[]) nodes.values().toArray(new IPNodeControl[0]);
	}
	
	public IPNode[] getNodes() {
		return getNodeControls();
	}

	public synchronized IPProcess getProcess(int id) {
		return processes.get(id);
	}
	
	public synchronized IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) processes.values().toArray(new IPProcessControl[0]);
	}

	public IPProcess[] getProcesses() {
		return getProcessControls();
	}
	
	public synchronized IPQueue getQueue(int id) {
		return queues.get(id);
	}
	
	public synchronized IPQueueControl[] getQueueControls() {
		return (IPQueueControl[]) queues.values().toArray(new IPQueueControl[0]);
	}
	
	public IPQueue[] getQueues() {
		return getQueueControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getStatus()
	 */
	synchronized public ResourceManagerAttributes.State getState() {
		return state;
	}
	
	/**
	 * @return the status message
	 */
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public boolean hasChildren() {
		return getMachines().length > 0 || getQueues().length > 0;
	}
	
	public boolean isAllStop() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void removeResourceManagerListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	public void shutdown() throws CoreException {
		IProgressMonitor monitor = null;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
		monitor.beginTask("Stopping Resource Manager " + getName(), 10);
        ResourceManagerAttributes.State state = getState();
		if (!state.equals(ResourceManagerAttributes.State.STOPPED)) {
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			try {
				doShutdown(subMonitor);
				if (!subMonitor.isCanceled()) {
					setState(ResourceManagerAttributes.State.STOPPED);
					fireShutdown();
				}
			}
			finally {
				monitor.done();
			}
		}
		else {
			monitor.done();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#start()
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
		monitor.beginTask("Starting Resource Manager " + getName(), 10);
        ResourceManagerAttributes.State state = getState();
		if (!state.equals(ResourceManagerAttributes.State.STARTED) &&
				!state.equals(ResourceManagerAttributes.State.ERROR)) {
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			try {
				doStartup(subMonitor);
				if (!subMonitor.isCanceled()) {
					setState(ResourceManagerAttributes.State.STARTED);
					fireStarted();
				}
			}
			finally {
				monitor.done();
			}
		}
		else {
			monitor.done();
		}
	}
	
	public IPJob submitJob(AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		return doSubmitJob(attrMgr, monitor);
	}
	
	public void terminateJob(IPJob job) throws CoreException {
		doTerminateJob(job);
	}

	/**
	 * @param statusMessage the status message to set
	 */
	private void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	/*
	 * Add new model elements to the model.
	 */
	protected synchronized void addJob(int jobId, IPJobControl job) {
		jobs.put(jobId, job);
	}
	
	protected synchronized void addMachine(int machineId, IPMachineControl machine) {
		machines.put(machineId, machine);
	}

	protected synchronized void addNode(int nodeId, IPNodeControl node) {
		nodes.put(nodeId, node);
	}

	protected synchronized void addProcess(int processId, IPProcessControl process) {
		processes.put(processId, process);
	}
	
	protected synchronized void addQueue(int queueId, IPQueueControl queue) {
		queues.put(queueId, queue);
	}
	
	/**
	 * 
	 */
	protected abstract void doDisableEvents();

	/**
	 * 
	 */
	protected abstract void doDispose();

	/**
	 * 
	 */
	protected abstract void doEnableEvents();

	/**
	 * @throws CoreException
	 */
	protected abstract void doShutdown(IProgressMonitor monitor) throws CoreException;

	/**
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * @param attrMgr
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	protected abstract IPJob doSubmitJob(AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException;

	/**
	 * @param job
	 * @throws CoreException
	 */
	protected abstract void doTerminateJob(IPJob job) throws CoreException;

	protected void fireError(String message) {
    	Object[] tmpListeners = listeners.getListeners();
    	
    	for (int i = 0, n = tmpListeners.length; i < n; ++i) {
    		final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
    		listener.handleErrorStateEvent(new ResourceManagerErrorEvent(this, message));
    	}
    }

	protected void fireJobsChanged(List<IPJob> jobs, Collection<? extends IAttribute> attrs) {
		IResourceManagerChangedJobsEvent e = 
			new ResourceManagerChangedJobsEvent(this, jobs, attrs);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleChangedJobsEvent(e);
		}
	}

	protected void fireMachinesChanged(List<IPMachine> macs) {
		IResourceManagerChangedMachinesEvent e = 
			new ResourceManagerChangedMachinesEvent(this, macs);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleChangedMachinesEvent(e);
		}
	}

	protected void fireNewJobs(List<IPJob> jobs) {
    	IResourceManagerNewJobsEvent e = new ResourceManagerNewJobsEvent(this, jobs);
    	Object[] tmpListeners = listeners.getListeners();
    	
    	for (int i = 0, n = tmpListeners.length; i < n; ++i) {
    		final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
    		listener.handleNewJobsEvent(e);
    	}
    }

	protected void fireNewMachines(List<IPMachine> macs) {
		IResourceManagerNewMachinesEvent e = new ResourceManagerNewMachinesEvent(this, macs);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleNewMachinesEvent(e);
		}
	}

    protected void fireNewNodes(List<IPNode> nodes) {
		IResourceManagerNewNodesEvent e = new ResourceManagerNewNodesEvent(this, nodes);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleNewNodesEvent(e);
		}
	}

    protected void fireNewProcesses(List<IPProcess> procs) {
    	IResourceManagerNewProcessesEvent e = new ResourceManagerNewProcessesEvent(this, procs);
    	Object[] tmpListeners = listeners.getListeners();
    	
    	for (int i = 0, n = tmpListeners.length; i < n; ++i) {
    		final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
    		listener.handleNewProcessesEvent(e);
    	}
    }

    protected void fireNewQueues(List<IPQueue> queues) {
    	IResourceManagerNewQueuesEvent e = new ResourceManagerNewQueuesEvent(this, queues);
    	Object[] tmpListeners = listeners.getListeners();
    	
    	for (int i = 0, n = tmpListeners.length; i < n; ++i) {
    		final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
    		listener.handleNewQueuesEvent(e);
    	}
    }

    protected void fireNodesChanged(List<IPNode> nodes) {
		IResourceManagerChangedNodesEvent e = new ResourceManagerChangedNodesEvent(this, nodes);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleChangedNodesEvent(e);
		}
	}

	protected void fireProcessesChanged(List<IPProcess> procs) {
		IResourceManagerChangedProcessesEvent e = 
			new ResourceManagerChangedProcessesEvent(this, procs);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleChangedProcessesEvent(e);
		}
	}

	protected void fireQueuesChanged(List<IPQueue> queues) {
		IResourceManagerChangedQueuesEvent e = new ResourceManagerChangedQueuesEvent(this, queues);
		Object[] tmpListeners = listeners.getListeners();

		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleChangedQueuesEvent(e);
		}
	}

	protected void fireShutdown() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleShutdownStateEvent(AbstractResourceManager.this);
		}
	}

	protected void fireStarted() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleStartupStateEvent(AbstractResourceManager.this);
		}
	}

	protected void fireSuspended() {
    	Object[] tmpListeners = listeners.getListeners();
    	
    	for (int i = 0, n = tmpListeners.length; i < n; ++i) {
    		final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
    		listener.handleSuspendedStateEvent(AbstractResourceManager.this);
    	}
    }

	protected IPJobControl getJobControl(int jobId) {
		return jobs.get(jobId);
	}
	
	protected IPMachineControl getMachineControl(int machineId) {
		return machines.get(machineId);
	}

	protected IPNodeControl getNodeControl(int nodeId) {
		return nodes.get(nodeId);
	}

	protected IPProcessControl getProcessControl(int processId) {
		return processes.get(processId);
	}

	protected IPQueueControl getQueueControl(int machineId) {
		return queues.get(machineId);
	}

	protected CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPCorePlugin.getUniqueIdentifier(),
				Status.ERROR, string, null);
		return new CoreException(status);
	}

	/*
	 * Create new model elements.
	 */
	protected IPJobControl newJob(IPQueueControl queue, int jobId, IAttribute[] attrs) {
		return new PJob(jobId, queue, attrs);
	}

	protected IPMachineControl newMachine(int machineId, IAttribute[] attrs) {
		return new PMachine(machineId, this, attrs);
	}

	protected IPNodeControl newNode(IPMachineControl machine, int nodeId, IAttribute[] attrs) {
		return new PNode(nodeId, machine, attrs);
	}

	protected IPProcessControl newProcess(IPJobControl job, int processId, IAttribute[] attrs) {
		IPProcessControl process = new PProcess(processId, job, attrs);
		/*
		 * If there is an ID attribute then it should be a node ID, so connect it up.
		 */
		for (IAttribute attr: attrs) {
			if (attr.getDefinition() == AttributeDefinitionManager.getIdAttributeDefinition()) {
				IPNodeControl node = getNodeControl(((IIntegerAttribute)attr).getValue());
				if (node != null) {
					node.addProcess(process);
				}
			}
		}
		return process;
	}

	protected IPQueueControl newQueue(int queueId, IAttribute[] attrs) {
		return new PQueue(queueId, this, attrs);
	}

	/**
	 * @param state
	 */
	synchronized protected void setState(ResourceManagerAttributes.State state) {
		this.state = state;
		setStatusMessage(state.toString());
	}

	/**
	 * @param state
	 * @param message
	 */
	synchronized protected void setState(ResourceManagerAttributes.State state, String message) {
		this.state = state;
		setStatusMessage(message);
	}

	/*
	 * Update attribute information in model elements.
	 */
	protected boolean updateJob(IPJobControl job, IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			job.setAttribute(attr.getDefinition().getId(), attr);
		}
		return true;
	}

	protected boolean updateMachine(IPMachineControl machine, IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			machine.setAttribute(attr.getDefinition().getId(), attr);
		}
		return true;
	}

	protected boolean updateNode(IPNodeControl node, IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			node.setAttribute(attr.getDefinition().getId(), attr);
		}
		return true;
	}

	protected boolean updateProcess(IPProcessControl process, IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			process.setAttribute(attr.getDefinition().getId(), attr);
		}
		return true;
	}

	protected boolean updateQueue(IPQueueControl queue, IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			queue.setAttribute(attr.getDefinition().getId(), attr);
		}
		return true;
	}
}