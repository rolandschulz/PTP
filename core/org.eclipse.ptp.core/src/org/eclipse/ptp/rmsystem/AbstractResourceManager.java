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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
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
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ErrorAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.PJob;
import org.eclipse.ptp.internal.core.elements.PMachine;
import org.eclipse.ptp.internal.core.elements.PNode;
import org.eclipse.ptp.internal.core.elements.PProcess;
import org.eclipse.ptp.internal.core.elements.PQueue;
import org.eclipse.ptp.internal.core.elements.Parent;
import org.eclipse.ptp.internal.core.elements.events.ChangedMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ChangedQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.NewMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.NewQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerErrorEvent;
import org.eclipse.ptp.internal.core.elements.events.ResourceManagerSubmitJobErrorEvent;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager extends Parent implements IResourceManager, IResourceManagerControl {

	private static IAttribute<?, ?, ?>[] getDefaultAttributes(IResourceManagerConfiguration config) {
		ArrayList<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();
		attrs.add(ElementAttributes.getNameAttributeDefinition().create(config.getName()));
		attrs.add(ElementAttributes.getIdAttributeDefinition().create(config.getUniqueName()));
		attrs.add(ResourceManagerAttributes.getDescriptionAttributeDefinition().create(config.getDescription()));
		attrs.add(ResourceManagerAttributes.getTypeAttributeDefinition().create(config.getType()));
		attrs.add(ResourceManagerAttributes.getStateAttributeDefinition().create(ResourceManagerAttributes.State.STOPPED));
		attrs.add(ResourceManagerAttributes.getRmIDAttributeDefinition().create(config.getUniqueName()));
		return attrs.toArray(new IAttribute<?, ?, ?>[0]);
	}

	private final ListenerList listeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	private final IQueueChildListener queueJobListener;
	private final IJobChildListener jobProcessListener;
	private final IMachineChildListener machineNodeListener;

	private IResourceManagerConfiguration config;
	private AttributeDefinitionManager attrDefManager = new AttributeDefinitionManager();

	private final Map<String, IPJobControl> jobsById = Collections.synchronizedMap(new HashMap<String, IPJobControl>());
	private final Map<String, IPMachineControl> machinesById = Collections.synchronizedMap(new HashMap<String, IPMachineControl>());
	private final Map<String, IPNodeControl> nodesById = Collections.synchronizedMap(new HashMap<String, IPNodeControl>());
	private final Map<String, IPProcessControl> processesById = Collections
			.synchronizedMap(new HashMap<String, IPProcessControl>());
	private final Map<String, IPQueueControl> queuesById = Collections.synchronizedMap(new HashMap<String, IPQueueControl>());
	private final Map<String, IPQueueControl> queuesByName = Collections.synchronizedMap(new HashMap<String, IPQueueControl>());

	public AbstractResourceManager(String id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id, universe, P_RESOURCE_MANAGER, getDefaultAttributes(config));
		this.config = config;

		queueJobListener = new IQueueChildListener() {
			public void handleEvent(IChangedJobEvent e) {
				// OK to ignore
			}

			public void handleEvent(INewJobEvent e) {
				// OK to ignore
			}

			public void handleEvent(IRemoveJobEvent e) {
				for (IPJob job : e.getJobs()) {
					job.removeChildListener(jobProcessListener);
					jobsById.remove(job.getID());
				}
			}
		};
		jobProcessListener = new IJobChildListener() {
			public void handleEvent(IChangedProcessEvent e) {
				// OK to ignore
			}

			public void handleEvent(INewProcessEvent e) {
				// OK to ignore
			}

			public void handleEvent(IRemoveProcessEvent e) {
				for (IPProcess process : e.getProcesses()) {
					processesById.remove(process.getID());
				}
			}
		};
		machineNodeListener = new IMachineChildListener() {
			public void handleEvent(IChangedNodeEvent e) {
				// OK to ignore
			}

			public void handleEvent(INewNodeEvent e) {
				// OK to ignore
			}

			public void handleEvent(IRemoveNodeEvent e) {
				for (IPNode node : e.getNodes()) {
					nodesById.remove(node.getID());
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#addChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void addChildListener(IResourceManagerChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void addElementListener(IResourceManagerListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#addMachineAttributes(java.util.Collection,
	 *      org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addMachineAttributes(Collection<IPMachineControl> machineControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		for (IPMachineControl machine : machineControls) {
			machine.addAttributes(attrs);
			machines.add(machine);
		}

		fireChangedMachines(machines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#addQueueAttributes(java.util.Collection,
	 *      org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addQueueAttributes(Collection<IPQueueControl> queueControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		for (IPQueueControl queue : queueControls) {
			queue.addAttributes(attrs);
			queues.add(queue);
		}

		fireChangedQueues(queues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#disableEvents()
	 */
	public void disableEvents() throws CoreException {
		if (getState().equals(ResourceManagerAttributes.State.STARTED)) {
			doDisableEvents();
			setState(ResourceManagerAttributes.State.SUSPENDED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#enableEvents()
	 */
	public void enableEvents() throws CoreException {
		if (getState().equals(ResourceManagerAttributes.State.SUSPENDED)) {
			doEnableEvents();
			setState(ResourceManagerAttributes.State.STARTED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
	public IAttributeDefinition<?, ?, ?> getAttributeDefinition(String attrId) {
		return attrDefManager.getAttributeDefinition(attrId);
	}

	/**
	 * Returns the resource managers attribute definition manager
	 * 
	 * @return attribute definiton manager for this resource manager
	 */
	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return attrDefManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getConfiguration()
	 */
	public IResourceManagerConfiguration getConfiguration() {
		synchronized (this) {
			return config;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getDescription()
	 */
	public String getDescription() {
		StringAttributeDefinition descAttrDef = ResourceManagerAttributes.getDescriptionAttributeDefinition();
		StringAttribute descAttr = getAttribute(descAttrDef);
		if (descAttr != null) {
			return descAttr.getValue();
		}
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.PElement#getID()
	 */
	@Override
	public String getID() {
		// needed this to get around draconian plug-in
		// library restrictions
		return super.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getMachineById(java.lang.String)
	 */
	public IPMachine getMachineById(String id) {
		return machinesById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getMachineControls()
	 */
	public Collection<IPMachineControl> getMachineControls() {
		synchronized (machinesById) {
			List<IPMachineControl> machines = new ArrayList<IPMachineControl>(machinesById.values().size());
			for (IPMachineControl machine : machinesById.values()) {
				machines.add(machine);
			}		
			return machines;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getMachines()
	 */
	public IPMachine[] getMachines() {
		return getMachineControls().toArray(new IPMachineControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueueById(java.lang.String)
	 */
	public IPQueue getQueueById(String id) {
		return queuesById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueueByName(java.lang.String)
	 */
	public IPQueue getQueueByName(String name) {
		return queuesByName.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getQueueControls()
	 */
	public Collection<IPQueueControl> getQueueControls() {
		synchronized (queuesById) {
			List<IPQueueControl> queues = new ArrayList<IPQueueControl>(queuesById.values().size());
			for (IPQueueControl queue : queuesById.values()) {
				queues.add(queue);
			}		
			return queues;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getQueues()
	 */
	public IPQueue[] getQueues() {
		return getQueueControls().toArray(new IPQueueControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return getConfiguration().getResourceManagerId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getStatus()
	 */
	public synchronized ResourceManagerAttributes.State getState() {
		EnumeratedAttribute<State> stateAttr = getStateAttribute();
		return stateAttr.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#getUniqueName()
	 */
	public String getUniqueName() {
		return getConfiguration().getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	public boolean hasChildren() {
		return getMachines().length > 0 || getQueues().length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#removeChildListener(org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void removeChildListener(IResourceManagerChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void removeElementListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#removeTerminatedJobs(org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void removeTerminatedJobs(IPQueue queue) {
		doRemoveTerminatedJobs((IPQueueControl) queue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#setConfiguration(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public void setConfiguration(IResourceManagerConfiguration config) {
		synchronized (this) {
			this.config = config;
		}

		/*
		 * Update attributes from the new configuration
		 */
		AttributeManager attrs = new AttributeManager();

		StringAttributeDefinition nameAttrDef = ElementAttributes.getNameAttributeDefinition();
		StringAttribute nameAttr = getAttribute(nameAttrDef);
		if (nameAttr != null) {
			try {
				nameAttr.setValue(config.getName());
				attrs.addAttribute(nameAttr);
			} catch (IllegalValueException e) {
			}
		}
		StringAttributeDefinition descAttrDef = ResourceManagerAttributes.getDescriptionAttributeDefinition();
		StringAttribute descAttr = getAttribute(descAttrDef);
		if (descAttr != null) {
			try {
				descAttr.setValue(config.getDescription());
				attrs.addAttribute(descAttr);
			} catch (IllegalValueException e) {
			}
		}

		fireResourceManagerChanged(attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#shutdown()
	 */
	public void shutdown() throws CoreException {
		switch (getState()) {
		case ERROR:
			setState(ResourceManagerAttributes.State.STOPPED);
			cleanUp();
			break;
		case STARTING:
		case STARTED:
			setState(ResourceManagerAttributes.State.STOPPING);
			try {
				doShutdown();
			} finally {
				cleanUp();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#startUp(IProgressMonitor monitor)
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException {
		if (getState() == ResourceManagerAttributes.State.STOPPED) {
			setState(ResourceManagerAttributes.State.STARTING);
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(Messages.AbstractResourceManager_1 + getName(), 10);
			try {
				initialize();
				SubMonitor subMon = SubMonitor.convert(monitor);
				doStartup(subMon.newChild(100));
			} catch (CoreException e) {
				setState(ResourceManagerAttributes.State.ERROR);
				throw e;
			}
			if (monitor.isCanceled()) {
				setState(ResourceManagerAttributes.State.STOPPED);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#submitJob(org.eclipse.debug.core.ILaunchConfiguration,
	 *      org.eclipse.ptp.core.attributes.AttributeManager,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPJob submitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		return doSubmitJob(null, configuration, attrMgr, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IResourceManager#submitJob(java.lang.String, 
	 * 		org.eclipse.debug.core.ILaunchConfiguration, 
	 * 		org.eclipse.ptp.core.attributes.AttributeManager, 
	 * 		org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void submitJob(String subId, ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		doSubmitJob(subId, configuration, attrMgr, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IResourceManager#terminateJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob job) throws CoreException {
		doTerminateJob(job);
	}

	/**
	 * Fire an event to notify that some attributes have changed
	 * 
	 * @param attrs
	 *            attributes that have changed
	 */
	private void fireResourceManagerChanged(AttributeManager attrs) {
		IResourceManagerChangeEvent e = new ResourceManagerChangeEvent(this, attrs);

		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}

	/**
	 * Helper method to get the state attribute for this RM
	 * 
	 * @return state attribute
	 */
	private EnumeratedAttribute<State> getStateAttribute() {
		EnumeratedAttributeDefinition<State> stateAttrDef = ResourceManagerAttributes.getStateAttributeDefinition();
		EnumeratedAttribute<State> stateAttr = getAttribute(stateAttrDef);
		return stateAttr;
	}

	/**
	 * Initialize the resource manager. This is called each time the resource
	 * manager is started.
	 */
	private void initialize() {
		attrDefManager.clear();
		attrDefManager.setAttributeDefinitions(ElementAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ErrorAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MessageAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
	}

	/**
	 * Add a collection of jobs to the model. This will result in a INewJobEvent
	 * being propagated to listeners on the queue.
	 * 
	 * @param queue
	 * @param jobs
	 */
	protected void addJobs(IPQueueControl queue, Collection<IPJobControl> jobs) {

		for (IPJobControl job : jobs) {
			job.addChildListener(jobProcessListener);
			jobsById.put(job.getID(), job);
		}

		queue.addJobs(jobs);
	}

	/**
	 * Add a collection of machines to the model. This will result in a
	 * INewMachineEvent being propagated to listeners on the RM.
	 * 
	 * @param machineControls
	 *            Collection of IMachineControls
	 */
	protected void addMachines(Collection<IPMachineControl> machineControls) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		synchronized (machinesById) {
			for (IPMachineControl machine : machineControls) {
				machinesById.put(machine.getID(), machine);
				machine.addChildListener(machineNodeListener);
				machines.add(machine);
			}
		}

		fireNewMachines(machines);
	}

	/**
	 * Add a collection of nodes to the model. This will result in a
	 * INewNodeEvent being propagated to listeners on the machine.
	 * 
	 * @param machine
	 *            parent of the nodes
	 * @param nodes
	 *            collection of IPNodeControls
	 */
	protected void addNodes(IPMachineControl machine, Collection<IPNodeControl> nodes) {

		for (IPNodeControl node : nodes) {
			nodesById.put(node.getID(), node);
		}

		machine.addNodes(nodes);
	}

	/**
	 * Add a collection of processes to the model. This involves adding the
	 * processes to a job *and* to a node. The node information is an attribute
	 * on the process. This will result in a INewProcessEvent being propagated
	 * to listeners on the job and nodes.
	 * 
	 * @param job
	 *            parent of the processes
	 * @param processes
	 *            collection of IPProcesControls
	 */
	protected void addProcesses(IPJobControl job, Collection<IPProcessControl> processes) {
		/*
		 * Map containing a list of processes that are linked to nodes
		 */
		Map<IPNodeControl, List<IPProcessControl>> nodeProcMap = new HashMap<IPNodeControl, List<IPProcessControl>>();

		for (IPProcessControl process : processes) {
			/*
			 * Find the node ID attribute and add the process to the list for
			 * each node. Remove the attribute from the attribute manager.
			 */
			StringAttribute attr = (StringAttribute) process.getAttribute(ProcessAttributes.getNodeIdAttributeDefinition());
			if (attr != null) {
				IPNodeControl node = getNodeControl(attr.getValue());
				if (node != null) {
					List<IPProcessControl> procs = nodeProcMap.get(node);
					if (procs == null) {
						procs = new ArrayList<IPProcessControl>();
						nodeProcMap.put(node, procs);
					}
					procs.add(process);
				}
				process.removeAttribute(attr);
			}

			processesById.put(process.getID(), process);
		}

		job.addProcesses(processes);

		/*
		 * Bulk add the processes to the nodes
		 */
		for (Map.Entry<IPNodeControl, List<IPProcessControl>> entry : nodeProcMap.entrySet()) {
			entry.getKey().addProcesses(entry.getValue());
		}

	}

	/**
	 * Add a collection of processes to the model. This will result in a
	 * INewProcessEvent being propagated to listeners on the job.
	 * 
	 * @param queues
	 *            collection of IPQueueControls
	 */
	protected void addQueues(Collection<IPQueueControl> queueControls) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		for (IPQueueControl queue : queueControls) {
			queuesById.put(queue.getID(), queue);
			/*
			 * Keep a map of queue names, if the queue has a name (and it
			 * should).
			 */
			StringAttribute attr = queue.getAttribute(ElementAttributes.getNameAttributeDefinition());
			if (attr != null) {
				queuesByName.put(attr.getValue(), queue);
			}
			queue.addChildListener(queueJobListener);
			queues.add(queue);
		}

		fireNewQueues(queues);
	}

	/**
	 * Remove all the model elements below the RM. This is called when the RM
	 * shuts down and ensures that everything is cleaned up properly.
	 */
	protected void cleanUp() {
		doCleanUp();

		removeQueues(this, getQueueControls());
		removeMachines(this, getMachineControls());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		fireResourceManagerChanged(attrs);
	}

	/**
	 * Perform any cleanup activities
	 */
	protected abstract void doCleanUp();

	/**
	 * Perform any activities prior to disabling events
	 */
	protected abstract void doDisableEvents();

	/**
	 * Perform any activities prior to disposing of the resource manager.
	 */
	protected abstract void doDispose();

	/**
	 * Perform any activities prior to enabling events
	 */
	protected abstract void doEnableEvents();

	/**
	 * Remove terminated jobs
	 * 
	 * @param queue
	 * @return list of removed jobs
	 */
	protected abstract List<IPJobControl> doRemoveTerminatedJobs(IPQueueControl queue);

	/**
	 * Stop the resource manager subsystem.
	 * 
	 * @throws CoreException
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * Start the resource manager subsystem. 
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * Submit a job with the supplied submission ID.Returns a job that represents the submitted job, or null if
	 * the progress monitor was canceled.
	 * 
	 * @param subId
	 * @param attrMgr
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract IPJob doSubmitJob(String subId, ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Terminate a job.
	 * 
	 * @param job
	 *            job to terminate
	 * @throws CoreException
	 */
	protected abstract void doTerminateJob(IPJob job) throws CoreException;

	/**
	 * Propagate IChangedMachineEvent to listener
	 * 
	 * @param machine
	 * @param collection
	 */
	protected void fireChangedMachines(Collection<IPMachine> machines) {
		IChangedMachineEvent e = new ChangedMachineEvent(this, machines);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Propagate IChangedQueueEvent to listener
	 * 
	 * @param queue
	 * @param collection
	 */
	protected void fireChangedQueues(Collection<IPQueue> queues) {
		IChangedQueueEvent e = new ChangedQueueEvent(this, queues);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Propagate IResourceManagerErrorEvent to listener
	 * 
	 * @param message
	 */
	protected void fireError(String message) {
		IResourceManagerErrorEvent e = new ResourceManagerErrorEvent(this, message);

		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}
	
	/**
	 * Propagate IResourceManagerSubmitJobErrorEvent to listeners
	 * 
	 * @param id job submission id
	 */
	protected void fireSubmitJobError(String id, String message) {
		IResourceManagerSubmitJobErrorEvent e = new ResourceManagerSubmitJobErrorEvent(this, id, message);

		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}

	/**
	 * Propagate a IResourceManagerNewMachinesEvent to listeners.
	 * 
	 * @param machines
	 *            collection containing the new machines
	 */
	protected void fireNewMachines(Collection<IPMachine> machines) {
		INewMachineEvent e = new NewMachineEvent(this, machines);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param queue
	 */
	protected void fireNewQueues(Collection<IPQueue> queues) {
		INewQueueEvent e = new NewQueueEvent(this, queues);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param machine
	 */
	protected void fireRemoveMachines(Collection<IPMachine> machines) {
		IRemoveMachineEvent e = new RemoveMachineEvent(this, machines);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param queue
	 */
	protected void fireRemoveQueues(Collection<IPQueue> queues) {
		IRemoveQueueEvent e = new RemoveQueueEvent(this, queues);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
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
		IStatus status = new Status(Status.ERROR, PTPCorePlugin.getUniqueIdentifier(), Status.ERROR, string, null);
		return new CoreException(status);
	}

	/**
	 * Helper method to create a new job.
	 * 
	 * @param queue
	 *            queue that this job belongs to
	 * @param jobId
	 *            job ID for the job
	 * @param attrs
	 *            initial attributes for the job
	 * @return newly created job model element
	 */
	protected IPJobControl newJob(IPQueueControl queue, String jobId, AttributeManager attrs) {
		return new PJob(jobId, queue, attrs.getAttributes());
	}

	/**
	 * Helper method to create a new machine.
	 * 
	 * @param machineId
	 *            ID for the machine
	 * @param attrs
	 *            initial attributes for the machine
	 * @return newly created machine model element
	 */
	protected IPMachineControl newMachine(String machineId, AttributeManager attrs) {
		return new PMachine(machineId, this, attrs.getAttributes());
	}

	/**
	 * Helper method to create a new node
	 * 
	 * @param machine
	 *            machine that this node belongs to
	 * @param nodeId
	 *            ID for the node
	 * @param attrs
	 *            initial attributes for the node
	 * @return newly created node model element
	 */
	protected IPNodeControl newNode(IPMachineControl machine, String nodeId, AttributeManager attrs) {
		return new PNode(nodeId, machine, attrs.getAttributes());
	}

	/**
	 * Helper method to create a new process
	 * 
	 * @param job
	 *            job that this process belongs to
	 * @param processId
	 *            ID for the process
	 * @param attrs
	 *            initial attributes for the process
	 * @return newly created process model element
	 */
	protected IPProcessControl newProcess(IPJobControl job, String processId, AttributeManager attrs) {
		return new PProcess(processId, job, attrs.getAttributes());
	}

	/**
	 * Helper method to create a new queue
	 * 
	 * @param queueId
	 *            ID for the queue
	 * @param attrs
	 *            initial attributes for the queue
	 * @return newly created queue model element
	 */
	protected IPQueueControl newQueue(String queueId, AttributeManager attrs) {
		return new PQueue(queueId, this, attrs.getAttributes());
	}

	/**
	 * @param job
	 */
	protected void removeJobs(IPQueueControl queue, Collection<IPJobControl> jobs) {
		queue.removeJobs(jobs);
	}

	/**
	 * @param machine
	 */
	protected void removeMachines(IResourceManager rm, Collection<IPMachineControl> machineControls) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		for (IPMachineControl machine : machineControls) {
			machine.removeNodes(machine.getNodeControls());
			machine.removeChildListener(machineNodeListener);
			machinesById.remove(machine.getID());
			machines.add(machine);
		}

		fireRemoveMachines(machines);
	}

	/**
	 * Remove nodes from the machine.
	 * 
	 * @param machine
	 *            machine containing the nodes to remove
	 * @param nodes
	 *            nodes to remove from the model
	 */
	protected void removeNodes(IPMachineControl machine, Collection<IPNodeControl> nodes) {
		machine.removeNodes(nodes);
	}

	/**
	 * Remove processes from the job. This will also result in the processes
	 * being removed from the associated nodes, as the nodes were registered as
	 * child listeners on the job.
	 * 
	 * @param job
	 *            job containing the processes to remove
	 * @param processes
	 *            processes to remove from the model
	 */
	protected void removeProcesses(IPJobControl job, Collection<IPProcessControl> processes) {
		job.removeProcesses(processes);
	}

	/**
	 * @param queue
	 */
	protected void removeQueues(IResourceManager rm, Collection<IPQueueControl> queueControls) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		for (IPQueueControl queue : queueControls) {
			queue.removeJobs(queue.getJobControls());
			queue.removeChildListener(queueJobListener);
			queuesById.remove(queue.getID());
			StringAttribute attr = queue.getAttribute(ElementAttributes.getNameAttributeDefinition());
			if (attr != null) {
				queuesByName.remove(attr.getValue());
			}
			queues.add(queue);
		}

		fireRemoveQueues(queues);
	}

	/**
	 * @param state
	 */
	protected synchronized void setState(ResourceManagerAttributes.State state) {
		EnumeratedAttribute<State> stateAttr = getStateAttribute();
		if (stateAttr.getValue() != state) {
			stateAttr.setValue(state);
			AttributeManager attrs = new AttributeManager();
			attrs.addAttribute(stateAttr);
			fireResourceManagerChanged(attrs);
		}
	}

	/**
	 * Update attributes on a collection of jobs.
	 * 
	 * @param queue
	 *            parent of jobs
	 * @param jobs
	 *            jobs to update
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateJobs(IPQueueControl queue, Collection<IPJobControl> jobs, AttributeManager attrs) {
		queue.addJobAttributes(jobs, attrs.getAttributes());
		return true;
	}

	/**
	 * Update attributes on a collection of machines.
	 * 
	 * @param machines
	 *            machines to update
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateMachines(Collection<IPMachineControl> machines, AttributeManager attrs) {
		addMachineAttributes(machines, attrs.getAttributes());
		return true;
	}

	/**
	 * Update attributes on a collection of nodes.
	 * 
	 * @param machine
	 *            parent of nodes
	 * @param nodes
	 *            collection of nodes
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateNodes(IPMachineControl machine, Collection<IPNodeControl> nodes, AttributeManager attrs) {
		machine.addNodeAttributes(nodes, attrs.getAttributes());
		return true;
	}

	/**
	 * Update attributes on a collection of processes.
	 * 
	 * @param job
	 *            parent of processes
	 * @param processes
	 *            collection of processes
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateProcesses(IPJobControl job, Collection<IPProcessControl> processes, AttributeManager attrs) {
		job.addProcessAttributes(processes, attrs.getAttributes());
		return true;
	}
	
	/**
	 * Update attributes on a collection of queues.
	 * 
	 * @param queues
	 *            queues to update
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateQueues(Collection<IPQueueControl> queues, AttributeManager attrs) {
		addQueueAttributes(queues, attrs.getAttributes());
		return true;
	}
}