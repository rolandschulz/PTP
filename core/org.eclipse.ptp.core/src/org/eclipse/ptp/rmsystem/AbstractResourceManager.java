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

import java.util.Arrays;
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
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes.State;
import org.eclipse.ptp.core.elements.events.IJobChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IJobNewProcessEvent;
import org.eclipse.ptp.core.elements.events.IJobRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangedEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueNewJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IJobProcessListener;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;
import org.eclipse.ptp.core.elements.listeners.IMachineNodeListener;
import org.eclipse.ptp.core.elements.listeners.IQueueJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener;
import org.eclipse.ptp.internal.core.elements.PElement;
import org.eclipse.ptp.internal.core.elements.PJob;
import org.eclipse.ptp.internal.core.elements.PMachine;
import org.eclipse.ptp.internal.core.elements.PNode;
import org.eclipse.ptp.internal.core.elements.PProcess;
import org.eclipse.ptp.internal.core.elements.PQueue;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerChangedEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerChangedMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerChangedQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerErrorEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerNewMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerNewQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerRemoveMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerRemoveQueueEvent;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager extends PElement implements IResourceManager,
	IResourceManagerControl {
	
	private static IAttribute[] getDefaultAttributes(IResourceManagerConfiguration config) {
		IAttribute nameAttr = null;
		
		try {
			 nameAttr = ElementAttributes.getNameAttributeDefinition().create(config.getName());
		} catch (IllegalValueException e) {
		}
		
		return new IAttribute[]{nameAttr};
	}

	private final ListenerList listeners = new ListenerList();
	private final ListenerList queueListeners = new ListenerList();
	private final ListenerList machineListeners = new ListenerList();
	
	private final IQueueListener queueListener;
	private final IMachineListener machineListener;
	private final IQueueJobListener queueJobListener;
	private final IJobProcessListener jobProcessListener;
	private final IMachineNodeListener machineNodeListener;
	
	private final IResourceManagerConfiguration config;
	private EnumeratedAttribute<ResourceManagerAttributes.State> state;
	private AttributeDefinitionManager attrDefManager = new AttributeDefinitionManager();

	private final HashMap<String, IPJobControl> jobsById = new HashMap<String, IPJobControl>();
	private final HashMap<String, IPMachineControl> machinesById = new HashMap<String, IPMachineControl>();
	private final HashMap<String, IPNodeControl> nodesById = new HashMap<String, IPNodeControl>();
	private final HashMap<String, IPProcessControl> processesById = new HashMap<String, IPProcessControl>();
	private final HashMap<String, IPQueueControl> queuesById = new HashMap<String, IPQueueControl>();
	private final HashMap<String, IPQueueControl> queuesByName = new HashMap<String, IPQueueControl>();

	public AbstractResourceManager(String id, IPUniverseControl universe,
			IResourceManagerConfiguration config)
	{
		super(id, universe, P_RESOURCE_MANAGER, getDefaultAttributes(config));
		this.config = config;
		this.state = ResourceManagerAttributes.getStateAttributeDefinition().create(State.STOPPED);

		queueListener = new IQueueListener(){
			public void handleEvent(IQueueChangedEvent e) {
				fireQueueChanged(e.getSource(), e.getAttributes());				
			}
		};
		machineListener = new IMachineListener(){
			public void handleEvent(IMachineChangedEvent e) {
				fireMachineChanged(e.getSource(), e.getAttributes());				
			}
		};
		queueJobListener = new IQueueJobListener(){
			public void handleEvent(IQueueChangedJobEvent e) {
				// OK to ignore
			}
			public void handleEvent(IQueueNewJobEvent e) {
				// OK to ignore
			}
			public void handleEvent(IQueueRemoveJobEvent e) {
				IPJob job = e.getJob();
				job.removeChildListener(jobProcessListener);
				jobsById.remove(job.getID());
			}
		};
		jobProcessListener = new IJobProcessListener(){
			public void handleEvent(IJobChangedProcessEvent e) {
				// OK to ignore
			}
			public void handleEvent(IJobNewProcessEvent e) {
				// OK to ignore		
			}
			public void handleEvent(IJobRemoveProcessEvent e) {
				processesById.remove(e.getProcess().getID());
			}			
		};
		machineNodeListener = new IMachineNodeListener(){
			public void handleEvent(IMachineChangedNodeEvent e) {
				// OK to ignore
			}
			public void handleEvent(IMachineNewNodeEvent e) {
				// OK to ignore
			}
			public void handleEvent(IMachineRemoveNodeEvent e) {
				nodesById.remove(e.getNode().getID());
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#addChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener)
	 */
	public void addChildListener(IResourceManagerMachineListener listener) {
		machineListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#addChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener)
	 */
	public void addChildListener(IResourceManagerQueueListener listener) {
		queueListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void addElementListener(IResourceManagerListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#disableEvents()
	 */
	public void disableEvents() throws CoreException {
	    if (getState().equals(ResourceManagerAttributes.State.STARTED)) {
	        doDisableEvents();
	        setState(ResourceManagerAttributes.State.SUSPENDED);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#dispose()
	 */
	public void dispose() {
		listeners.clear();
		try {
			shutdown();
		} catch (CoreException e) {
		}
		doDispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#enableEvents()
	 */
	public void enableEvents() throws CoreException {
	    if (getState().equals(ResourceManagerAttributes.State.SUSPENDED)) {
	        doEnableEvents();
	        setState(ResourceManagerAttributes.State.STARTED);
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		} else {
			return super.getAdapter(adapter);
		}
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
	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return attrDefManager;
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
	public String getID() {
		// needed this to get around draconian plug-in
		// library restrictions
		return super.getID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getMachineById(java.lang.String)
	 */
	public synchronized IPMachine getMachineById(String id) {
		return machinesById.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getMachineControls()
	 */
	public synchronized IPMachineControl[] getMachineControls() {
		return (IPMachineControl[]) machinesById.values().toArray(new IPMachineControl[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getMachines()
	 */
	public IPMachine[] getMachines() {
		return getMachineControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getName()
	 */
	public String getName() {
		return config.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueueById(java.lang.String)
	 */
	public synchronized IPQueue getQueueById(String id) {
		return queuesById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueueByName(java.lang.String)
	 */
	public synchronized IPQueue getQueueByName(String name) {
		return queuesByName.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getQueueControls()
	 */
	public synchronized IPQueueControl[] getQueueControls() {
		return (IPQueueControl[]) queuesById.values().toArray(new IPQueueControl[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueues()
	 */
	public IPQueue[] getQueues() {
		return getQueueControls();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ptp.core.elements.IResourceManager#getResourceManagerId()
     */
    public String getResourceManagerId() {
        return getConfiguration().getResourceManagerId();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getStatus()
	 */
	synchronized public ResourceManagerAttributes.State getState() {
		return state.getValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getUniqueName()
	 */
	public String getUniqueName() {
		return getConfiguration().getUniqueName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	public boolean hasChildren() {
		return getMachines().length > 0 || getQueues().length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#removeChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener)
	 */
	public void removeChildListener(IResourceManagerMachineListener listener) {
		machineListeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#removeChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener)
	 */
	public void removeChildListener(IResourceManagerQueueListener listener) {
		queueListeners.remove(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void removeElementListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#removeTerminatedJobs(org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void removeTerminatedJobs(IPQueue queue) {
		doRemoveTerminatedJobs((IPQueueControl)queue);
	}
	
	/**
	 * Initialize the resource manager. This is called each time the
	 * resource manager is started.
	 */
	private void initialize() {
		attrDefManager.clear();
		attrDefManager.setAttributeDefinitions(ElementAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
	}
	
	/**
	 * Remove all the model elements below the RM. This is called when the RM
	 * shuts down and ensures that everything is cleaned up properly.
	 */
	private void cleanUp() {
		for (IPQueueControl queue : getQueueControls()) {
			removeQueue(queue);
		}
		for (IPMachineControl machine : getMachineControls()) {
			removeMachine(machine);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	public void shutdown() throws CoreException {
		IProgressMonitor monitor = null;
		switch (getState()) {
		case ERROR:
			setState(ResourceManagerAttributes.State.STOPPED);
			cleanUp();
			break;
		case STARTED:
			setState(ResourceManagerAttributes.State.STOPPING);
	        if (monitor == null) {
	            monitor = new NullProgressMonitor();
	        }
			monitor.beginTask("Stopping Resource Manager " + getName(), 10);
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			try {
				doShutdown(subMonitor);
			}
			finally {
				monitor.done();
				setState(ResourceManagerAttributes.State.STOPPED);
				cleanUp();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#start()
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException {
        if (getState() == ResourceManagerAttributes.State.STOPPED) {
        	setState(ResourceManagerAttributes.State.STARTING);
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
    		monitor.beginTask("Starting Resource Manager " + getName(), 10);
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			try {
				initialize();
				boolean started = doStartup(subMonitor);
				if (started) {
					setState(ResourceManagerAttributes.State.STARTED);
				} else if (!subMonitor.isCanceled()) {
					setState(ResourceManagerAttributes.State.ERROR);					
				} else {
		        	setState(ResourceManagerAttributes.State.STOPPED);					
				}
			}
			finally {
				monitor.done();
			}
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#submitJob(org.eclipse.ptp.core.attributes.AttributeManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPJob submitJob(AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		return doSubmitJob(attrMgr, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#terminateJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob job) throws CoreException {
		doTerminateJob(job);
	}
	
	/**
	 * @param attrs
	 */
	private void fireResourceManagerChanged(List<IAttribute> attrs) {
		IResourceManagerChangedEvent e = 
			new ResourceManagerChangedEvent(this, attrs);
    	
		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener)listener).handleEvent(e);
		}
	}
	
	/*
	 * Add new model elements to the model.
	 */
	protected synchronized void addJob(String jobId, IPJobControl job) {
		job.getQueueControl().addJob(job);
		job.addChildListener(jobProcessListener);
		jobsById.put(jobId, job);
	}

	/**
	 * @param machineId
	 * @param machine
	 */
	protected synchronized void addMachine(String machineId, IPMachineControl machine) {
		machinesById.put(machineId, machine);
		machine.addElementListener(machineListener);
		machine.addChildListener(machineNodeListener);
		fireNewMachine(machine);
	}

	/**
	 * @param nodeId
	 * @param node
	 */
	protected synchronized void addNode(String nodeId, IPNodeControl node) {
		node.getMachineControl().addNode(node);
		nodesById.put(nodeId, node);
	}
	
	/**
	 * @param processId
	 * @param process
	 */
	protected synchronized void addProcess(String processId, IPProcessControl process) {
		process.getJobControl().addProcess(process);
		processesById.put(processId, process);
	}

	/**
	 * @param queueId
	 * @param queue
	 */
	protected synchronized void addQueue(String queueId, IPQueueControl queue) {
		queuesById.put(queueId, queue);
		/*
		 * Keep a map of queue names, if the queue has a name (and it should).
		 */
		StringAttribute attr = (StringAttribute) queue.getAttribute(ElementAttributes.getNameAttributeDefinition());
		if (attr != null) {
			queuesByName.put(attr.getValue(), queue);
		}
		queue.addElementListener(queueListener);
		queue.addChildListener(queueJobListener);
		fireNewQueue(queue);
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
	 * @param queue
	 * @return
	 */
	protected abstract List<IPJob> doRemoveTerminatedJobs(IPQueueControl queue);

	/**
	 * @throws CoreException
	 */
	protected abstract void doShutdown(IProgressMonitor monitor) throws CoreException;

	/**
	 * Start the resource manager subsystem. Returns true if the system was started successfully
	 * or false if not. An error can be distinguished from user canceled by checking the monitor state.
	 * 
	 * @param monitor
	 * @return true if successful
	 * @throws CoreException
	 */
	protected abstract boolean doStartup(IProgressMonitor monitor) throws CoreException;

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

	/**
	 * @param message
	 */
	protected void fireError(String message) {
		IResourceManagerErrorEvent e = 
			new ResourceManagerErrorEvent(this, message);
    	
		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener)listener).handleEvent(e);
		}
    }

	/**
	 * @param machine
	 * @param attrs
	 */
	protected void fireMachineChanged(IPMachine machine, Collection<IAttribute> attrs) {
		IResourceManagerChangedMachineEvent e = 
			new ResourceManagerChangedMachineEvent(this, machine, attrs);

		for (Object listener : machineListeners.getListeners()) {
			((IResourceManagerMachineListener)listener).handleEvent(e);
		}
	}
	
	/**
	 * @param machine
	 */
	protected void fireNewMachine(IPMachine machine) {
		IResourceManagerNewMachineEvent e = 
			new ResourceManagerNewMachineEvent(this, machine);

		for (Object listener : machineListeners.getListeners()) {
			((IResourceManagerMachineListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param queue
	 */
	protected void fireNewQueue(IPQueue queue) {
		IResourceManagerNewQueueEvent e = 
			new ResourceManagerNewQueueEvent(this, queue);

		for (Object listener : queueListeners.getListeners()) {
			((IResourceManagerQueueListener)listener).handleEvent(e);
		}
    }

	/**
	 * @param queue
	 * @param attrs
	 */
	protected void fireQueueChanged(IPQueue queue, Collection<IAttribute> attrs) {
		IResourceManagerChangedQueueEvent e = 
			new ResourceManagerChangedQueueEvent(this, queue, attrs);

		for (Object listener : queueListeners.getListeners()) {
			((IResourceManagerQueueListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param machine
	 */
	protected void fireRemoveMachine(IPMachine machine) {
		IResourceManagerRemoveMachineEvent e = 
			new ResourceManagerRemoveMachineEvent(this, machine);

		for (Object listener : machineListeners.getListeners()) {
			((IResourceManagerMachineListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param queue
	 */
	protected void fireRemoveQueue(IPQueue queue) {
		IResourceManagerRemoveQueueEvent e = 
			new ResourceManagerRemoveQueueEvent(this, queue);

		for (Object listener : queueListeners.getListeners()) {
			((IResourceManagerQueueListener)listener).handleEvent(e);
		}
	}
	
	/**
	 * @param jobId
	 * @return
	 */
	protected IPJobControl getJobControl(String jobId) {
		return jobsById.get(jobId);
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPMachineControl getMachineControl(String machineId) {
		return machinesById.get(machineId);
	}

    /**
     * @param nodeId
     * @return
     */
    protected IPNodeControl getNodeControl(String nodeId) {
		return nodesById.get(nodeId);
	}

	/**
	 * @param processId
	 * @return
	 */
	protected IPProcessControl getProcessControl(String processId) {
		return processesById.get(processId);
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPQueueControl getQueueControl(String machineId) {
		return queuesById.get(machineId);
	}
	
	/**
	 * @param string
	 * @return
	 */
	protected CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPCorePlugin.getUniqueIdentifier(),
				Status.ERROR, string, null);
		return new CoreException(status);
	}

	/**
	 * @param queue
	 * @param jobId
	 * @param attrs
	 * @return
	 */
	protected IPJobControl newJob(IPQueueControl queue, String jobId, AttributeManager attrs) {
		IPJobControl job = new PJob(jobId, queue, attrs.getAttributes());
		return job;
	}

	/**
	 * @param machineId
	 * @param attrs
	 * @return
	 */
	protected IPMachineControl newMachine(String machineId, AttributeManager attrs) {
		return new PMachine(machineId, this, attrs.getAttributes());
	}

	/**
	 * @param machine
	 * @param nodeId
	 * @param attrs
	 * @return
	 */
	protected IPNodeControl newNode(IPMachineControl machine, String nodeId, AttributeManager attrs) {
		IPNodeControl node = new PNode(nodeId, machine, attrs.getAttributes());
		return node;
	}

	/**
	 * @param job
	 * @param processId
	 * @param attrs
	 * @return
	 */
	protected IPProcessControl newProcess(IPJobControl job, String processId, AttributeManager attrs) {
		IPNodeControl node = null;
		
		/*
		 * Find the node ID attribute. Remove it from the attribute manager as it will
		 * be dealt with specially.
		 */
		StringAttribute attr = (StringAttribute) attrs.getAttribute(ProcessAttributes.getNodeIdAttributeDefinition());
		if (attr != null) {
			node = getNodeControl(((StringAttribute)attr).getValue());
			attrs.removeAttribute(attr);
		}
		
		IPProcessControl process = new PProcess(processId, job, attrs.getAttributes());
		if (node != null) {
			process.addNode(node);
		}
		return process;
	}

	/**
	 * @param queueId
	 * @param attrs
	 * @return
	 */
	protected IPQueueControl newQueue(String queueId, AttributeManager attrs) {
		return new PQueue(queueId, this, attrs.getAttributes());
	}

	/**
	 * @param job
	 */
	protected synchronized void removeJob(IPJobControl job) {
		job.getQueueControl().removeJob(job);
	}

	/**
	 * @param machine
	 */
	protected synchronized void removeMachine(IPMachineControl machine) {
		for (IPNodeControl node : machine.getNodeControls()) {
			machine.removeNode(node);
		}
		machine.removeElementListener(machineListener);
		machine.removeChildListener(machineNodeListener);
		machinesById.remove(machine.getID());
		fireRemoveMachine(machine);
	}

	/**
	 * @param node
	 */
	protected synchronized void removeNode(IPNodeControl node) {
		node.getMachineControl().removeNode(node);
	}

	/**
	 * @param process
	 */
	protected synchronized void removeProcess(IPProcessControl process) {
		process.getJobControl().removeProcess(process);
	}

	/**
	 * @param queue
	 */
	protected synchronized void removeQueue(IPQueueControl queue) {
		for (IPJobControl job : queue.getJobControls()) {
			queue.removeJob(job);
		}
		queue.removeElementListener(queueListener);
		queue.removeChildListener(queueJobListener);
		queuesById.remove(queue.getID());
		StringAttribute attr = (StringAttribute) queue.getAttribute(ElementAttributes.getNameAttributeDefinition());
		if (attr != null) {
			queuesByName.remove(attr.getValue());
		}
		fireRemoveQueue(queue);
	}

	/**
	 * @param state
	 */
	synchronized protected void setState(ResourceManagerAttributes.State state) {
		if (this.state.getValue() != state) {
			this.state.setValue(state);
			fireResourceManagerChanged(Arrays.asList(new IAttribute[] {this.state}));
		}
	}

	/**
	 * @param job
	 * @param attrs
	 * @return
	 */
	protected boolean updateJob(IPJobControl job, AttributeManager attrs) {
		job.addAttributes(attrs.getAttributes());
		return true;
	}

	/**
	 * @param machine
	 * @param attrs
	 * @return
	 */
	protected boolean updateMachine(IPMachineControl machine, AttributeManager attrs) {
		machine.addAttributes(attrs.getAttributes());
		return true;
	}

	/**
	 * @param node
	 * @param attrs
	 * @return
	 */
	protected boolean updateNode(IPNodeControl node, AttributeManager attrs) {
		node.addAttributes(attrs.getAttributes());
		return true;
	}

	/**
	 * @param process
	 * @param attrs
	 * @return
	 */
	protected boolean updateProcess(IPProcessControl process, AttributeManager attrs) {
		process.addAttributes(attrs.getAttributes());
		return true;
	}

	/**
	 * @param queue
	 * @param attrs
	 * @return
	 */
	protected boolean updateQueue(IPQueueControl queue, AttributeManager attrs) {
		queue.addAttributes(attrs.getAttributes());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.List)
	 */
	@Override
	protected void doAddAttributeHook(List<IAttribute> attrs) {
		fireResourceManagerChanged(attrs);
	}
}