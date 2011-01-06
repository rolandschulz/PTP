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
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
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
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ErrorAttributes;
import org.eclipse.ptp.core.elements.attributes.FilterAttributes;
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
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.PJob;
import org.eclipse.ptp.internal.core.elements.PMachine;
import org.eclipse.ptp.internal.core.elements.PNode;
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
public abstract class AbstractResourceManager extends Parent implements IPResourceManager, IResourceManagerControl {

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

	private final AttributeDefinitionManager attrDefManager = new AttributeDefinitionManager();
	private final ListenerList childListeners = new ListenerList();
	private IResourceManagerConfiguration config;
	private final Map<String, IPJobControl> jobsById = Collections.synchronizedMap(new HashMap<String, IPJobControl>());

	private final ListenerList listeners = new ListenerList();
	private final IMachineChildListener machineNodeListener;

	private final Map<String, IPMachineControl> machinesById = Collections.synchronizedMap(new HashMap<String, IPMachineControl>());
	private final Map<String, IPNodeControl> nodesById = Collections.synchronizedMap(new HashMap<String, IPNodeControl>());
	private final IQueueChildListener queueJobListener;
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
					jobsById.remove(job.getID());
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
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#addChildListener(org.
	 * eclipse .ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void addChildListener(IResourceManagerChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse
	 * .ptp.rm.IResourceManagerListener)
	 */
	public void addElementListener(IResourceManagerListener listener) {
		listeners.add(listener);
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
			jobsById.put(job.getID(), job);
		}

		queue.addJobs(jobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#
	 * addMachineAttributes(java.util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addMachineAttributes(Collection<IPMachineControl> machineControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		for (IPMachineControl machine : machineControls) {
			machine.addAttributes(attrs);
			machines.add(machine);
		}

		fireChangedMachines(machines);
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
	 * @param processJobRanks
	 *            set of job ranks within job
	 * @param attrs
	 * @since 4.0
	 */
	protected void addProcessesByJobRanks(IPJobControl job, BitSet processJobRanks, AttributeManager attrs) {

		// actually add the processes to the job
		// with the given attributes
		job.addProcessesByJobRanks(processJobRanks, attrs);

		// retrieve the set of nodes on which these processes are running
		Set<StringAttribute> nodeIdAttrs = job.getProcessAttributes(ProcessAttributes.getNodeIdAttributeDefinition(),
				processJobRanks);

		for (StringAttribute nodeIdAttr : nodeIdAttrs) {
			/*
			 * Add the jobs containing the node's processes to the nodes
			 */
			final IPNodeControl node = getNodeControl(nodeIdAttr.getValue());
			if (node != null) {
				final BitSet nodesProcessJobRanks = job.getProcessJobRanks(nodeIdAttr);
				node.addJobProcessRanks(job, nodesProcessJobRanks);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#
	 * addQueueAttributes(java.util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addQueueAttributes(Collection<IPQueueControl> queueControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		for (IPQueueControl queue : queueControls) {
			queue.addAttributes(attrs);
			queues.add(queue);
		}

		fireChangedQueues(queues);
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
	 * @see
	 * org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java
	 * .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		/*
		 * The resource manager name is stored in the configuration so that it
		 * persists. Map name attributes to the configuration.
		 */
		StringAttribute nameAttr = attrs.getAttribute(ElementAttributes.getNameAttributeDefinition());
		if (nameAttr != null) {
			getConfiguration().setName(nameAttr.getValue());
		}
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
	 * Submit a job with the supplied submission ID.Returns a job that
	 * represents the submitted job, or null if the progress monitor was
	 * canceled.
	 * 
	 * @param subId
	 * @param attrMgr
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract IPJob doSubmitJob(String subId, ILaunchConfiguration configuration, AttributeManager attrMgr,
			IProgressMonitor monitor) throws CoreException;

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
	 * Propagate IResourceManagerSubmitJobErrorEvent to listeners
	 * 
	 * @param id
	 *            job submission id
	 */
	protected void fireSubmitJobError(String id, String message) {
		IResourceManagerSubmitJobErrorEvent e = new ResourceManagerSubmitJobErrorEvent(this, id, message);

		for (Object listener : listeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
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
	 * @return attribute definition manager for this resource manager
	 */
	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return attrDefManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getConfiguration
	 * ()
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

	/**
	 * @param jobId
	 * @return
	 */
	protected IPJobControl getJobControl(String jobId) {
		return jobsById.get(jobId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getMachineById(java.lang
	 * .String)
	 */
	public IPMachine getMachineById(String id) {
		return machinesById.get(id);
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPMachineControl getMachineControl(String machineId) {
		return machinesById.get(machineId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#
	 * getMachineControls()
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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getMachines()
	 */
	public IPMachine[] getMachines() {
		return getMachineControls().toArray(new IPMachineControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#getName()
	 */
	@Override
	public String getName() {
		return getConfiguration().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getNodeById(java.lang.
	 * String)
	 */
	/**
	 * @since 4.0
	 */
	public IPNode getNodeById(String id) {
		return nodesById.get(id);
	}

	/**
	 * @param nodeId
	 * @return
	 */
	protected IPNodeControl getNodeControl(String nodeId) {
		return nodesById.get(nodeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getQueueById(java.lang
	 * .String)
	 */
	public IPQueue getQueueById(String id) {
		return queuesById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getQueueByName(java.lang
	 * .String)
	 */
	public IPQueue getQueueByName(String name) {
		return queuesByName.get(name);
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPQueueControl getQueueControl(String machineId) {
		return queuesById.get(machineId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#getQueueControls
	 * ()
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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getQueues()
	 */
	public IPQueue[] getQueues() {
		return getQueueControls().toArray(new IPQueueControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getResourceManagerId()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getUniqueName()
	 */
	public String getUniqueName() {
		return getConfiguration().getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return getMachines().length > 0 || getQueues().length > 0;
	}

	/**
	 * Initialize the resource manager. This is called each time the resource
	 * manager is started.
	 */
	private void initialize() {
		attrDefManager.clear();
		attrDefManager.setAttributeDefinitions(ElementAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ErrorAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(FilterAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MessageAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
	}

	/**
	 * @param string
	 * @return
	 */
	protected CoreException makeCoreException(String string) {
		IStatus status = new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, string, null);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#removeChildListener(org
	 * .eclipse.ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void removeChildListener(IResourceManagerChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org
	 * .eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void removeElementListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param job
	 */
	protected void removeJobs(IPQueueControl queue, Collection<IPJobControl> jobs) {
		queue.removeJobs(jobs);
	}

	/**
	 * @param machine
	 * @since 5.0
	 */
	protected void removeMachines(IPResourceManager rm, Collection<IPMachineControl> machineControls) {
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
	 * @param queue
	 * @since 5.0
	 */
	protected void removeQueues(IPResourceManager rm, Collection<IPQueueControl> queueControls) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#removeTerminatedJobs(org
	 * .eclipse.ptp.core.elements.IPQueue)
	 */
	public void removeTerminatedJobs(IPQueue queue) {
		doRemoveTerminatedJobs((IPQueueControl) queue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IResourceManagerControl#setConfiguration
	 * (org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
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
			try {
				doShutdown();
			} finally {
				setState(ResourceManagerAttributes.State.STOPPED);
				cleanUp();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#startUp(IProgressMonitor
	 * monitor)
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		if (getState() == ResourceManagerAttributes.State.STOPPED || getState() == ResourceManagerAttributes.State.ERROR) {
			setState(ResourceManagerAttributes.State.STARTING);
			monitor.subTask(Messages.AbstractResourceManager_1 + getName());
			try {
				initialize();
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
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#submitJob(org.eclipse.
	 * debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.core.attributes.AttributeManager,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPJob submitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		return doSubmitJob(null, configuration, attrMgr, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#submitJob(java.lang.String
	 * , org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.core.attributes.AttributeManager,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void submitJob(String subId, ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		doSubmitJob(subId, configuration, attrMgr, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#terminateJob(org.eclipse
	 * .ptp.core.elements.IPJob)
	 */
	public void terminateJob(IPJob job) throws CoreException {
		doTerminateJob(job);
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
	 * Update attributes on a collection of processes. If the nodeId attribute
	 * is specified then the processes will be moved to the new node.
	 * 
	 * @param job
	 *            parent of processes
	 * @param processJobRanks
	 *            set of job ranks for processes
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 * @since 4.0
	 */
	protected boolean updateProcessesByJobRanks(IPJobControl job, BitSet processJobRanks, AttributeManager attrs) {
		final StringAttributeDefinition nodeIdAttributeDefinition = ProcessAttributes.getNodeIdAttributeDefinition();

		StringAttribute newNodeId = attrs.getAttribute(nodeIdAttributeDefinition);

		// if we are update a node attribute we must move processes from one
		// node
		// to another
		if (newNodeId != null) {

			IPNodeControl newNode = getNodeControl(newNodeId.getValue());
			// add the job and the process job ranks to the new node
			newNode.addJobProcessRanks(job, processJobRanks);

			// Remove the process job ranks from the nodes that no longer
			// contain these processes
			Set<StringAttribute> oldNodeIds = job.getProcessAttributes(nodeIdAttributeDefinition, processJobRanks);
			oldNodeIds.remove(newNodeId);
			for (StringAttribute oldNodeId : oldNodeIds) {
				IPNodeControl oldNode = getNodeControl(oldNodeId.getValue());
				if (oldNode != null) {
					oldNode.removeJobProcessRanks(job, processJobRanks);
				}
			}
		}

		/*
		 * Update attributes on the job (where they live for the processes)
		 */
		job.addProcessAttributes(processJobRanks, attrs);

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

	/**
	 * Update attributes for this RM
	 * 
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	protected boolean updateRM(AttributeManager attrs) {
		addAttributes(attrs.getAttributes());
		return true;
	}
}