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
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.PJob;
import org.eclipse.ptp.internal.core.elements.PMachine;
import org.eclipse.ptp.internal.core.elements.PNode;
import org.eclipse.ptp.internal.core.elements.PQueue;
import org.eclipse.ptp.internal.core.elements.Parent;
import org.eclipse.ptp.internal.core.elements.events.ChangedJobEvent;
import org.eclipse.ptp.internal.core.elements.events.ChangedMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ChangedQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.NewJobEvent;
import org.eclipse.ptp.internal.core.elements.events.NewMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.NewQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveJobEvent;
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
	private final Map<String, IPQueueControl> queuesById = Collections.synchronizedMap(new HashMap<String, IPQueueControl>());

	public AbstractResourceManager(IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(universe.getNextResourceManagerId(), universe, getDefaultAttributes(config));
		this.config = config;

		machineNodeListener = new IMachineChildListener() {
			public void handleEvent(IChangedNodeEvent e) {
				// OK to ignore
			}

			public void handleEvent(INewNodeEvent e) {
				// OK to ignore
			}

			public void handleEvent(IRemoveNodeEvent e) {
				synchronized (nodesById) {
					for (IPNode node : e.getNodes()) {
						nodesById.remove(node.getID());
					}
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
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		}
		if (adapter == IPResourceManager.class) {
			return this;
		}
		if (adapter == IResourceManagerConfiguration.class) {
			return getConfiguration();
		}
		return super.getAdapter(adapter);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getJobById(java.lang.
	 * String)
	 */
	/**
	 * @since 5.0
	 */
	public IPJob getJobById(String id) {
		synchronized (jobsById) {
			return jobsById.get(id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getJobs()
	 */
	/**
	 * @since 5.0
	 */
	public IPJob[] getJobs() {
		return getJobControls().toArray(new IPJobControl[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getMachineById(java.lang
	 * .String)
	 */
	public IPMachine getMachineById(String id) {
		synchronized (machinesById) {
			return machinesById.get(id);
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
		synchronized (nodesById) {
			return nodesById.get(id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#getQueueById(java.lang
	 * .String)
	 */
	public IPQueue getQueueById(String id) {
		synchronized (queuesById) {
			return queuesById.get(id);
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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getResourceManager()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManager() {
		return this;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPResourceManager#removeTerminatedJobs()
	 */
	/**
	 * @since 5.0
	 */
	public void removeTerminatedJobs() {
		doRemoveTerminatedJobs();
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
		return doSubmitJob(configuration, attrMgr, monitor);
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

	private void addJobAttributes(Collection<IPJobControl> jobControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		for (IPJobControl job : jobControls) {
			job.addAttributes(attrs);
			jobs.add(job);
		}

		fireChangedJobs(jobs);
	}

	private void addMachineAttributes(Collection<IPMachineControl> machineControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		for (IPMachineControl machine : machineControls) {
			machine.addAttributes(attrs);
			machines.add(machine);
		}

		fireChangedMachines(machines);
	}

	private void addQueueAttributes(Collection<IPQueueControl> queueControls, IAttribute<?, ?, ?>[] attrs) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		for (IPQueueControl queue : queueControls) {
			queue.addAttributes(attrs);
			queues.add(queue);
		}

		fireChangedQueues(queues);
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
	 * Add a collection of jobs to the model. This will result in a INewJobEvent
	 * being propagated to listeners on the RM. Supports either the old
	 * hierarchy where a parent queue is supplied, or each job can contain a
	 * queue attribute specifying the job queue.
	 * 
	 * @param queue
	 *            jobs will be added to the queue if specified
	 * @param jobs
	 *            collection of jobs to add
	 */
	protected void addJobs(IPQueueControl queue, Collection<IPJobControl> jobControls) {
		Map<IPQueueControl, List<IPJobControl>> map = new HashMap<IPQueueControl, List<IPJobControl>>();
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		for (IPJobControl job : jobControls) {
			StringAttribute queueIdAttr = job.getAttribute(JobAttributes.getQueueIdAttributeDefinition());
			if (queueIdAttr != null) {
				IPQueueControl jQueue = getQueueControl(queueIdAttr.getValue());
				List<IPJobControl> qJobs = map.get(jQueue);
				if (qJobs == null) {
					qJobs = new ArrayList<IPJobControl>();
					map.put(queue, qJobs);
				}
			}
			jobsById.put(job.getID(), job);
			jobs.add(job);
		}

		/*
		 * Add jobs to any queues that were specified as attributes on the job
		 */
		for (Map.Entry<IPQueueControl, List<IPJobControl>> entry : map.entrySet()) {
			entry.getKey().addJobs(entry.getValue());
		}

		/*
		 * Add jobs to the parent queue if supplied
		 */
		if (queue != null) {
			queue.addJobs(jobControls);
		}

		fireNewJobs(jobs);
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

		synchronized (nodesById) {
			for (IPNodeControl node : nodes) {
				nodesById.put(node.getID(), node);
			}
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

	/**
	 * Add a collection of processes to the model. This will result in a
	 * INewProcessEvent being propagated to listeners on the job.
	 * 
	 * @param queues
	 *            collection of IPQueueControls
	 */
	protected void addQueues(Collection<IPQueueControl> queueControls) {
		List<IPQueue> queues = new ArrayList<IPQueue>(queueControls.size());

		synchronized (queuesById) {
			for (IPQueueControl queue : queueControls) {
				queuesById.put(queue.getID(), queue);
				queues.add(queue);
			}
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
	 * @return list of removed jobs
	 * @since 5.0
	 */
	protected abstract List<IPJobControl> doRemoveTerminatedJobs();

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
	 * Submit a job to the resource manager. Returns a job that represents the
	 * submitted job, or null if the progress monitor was canceled.
	 * 
	 * @param attrMgr
	 * @param monitor
	 * @throws CoreException
	 * @since 5.0
	 */
	protected abstract IPJob doSubmitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
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
	 * Send IChangedJobEvent to registered listeners
	 * 
	 * @param jobs
	 *            jobs that have changed
	 * @since 5.0
	 */
	protected void fireChangedJobs(Collection<IPJob> jobs) {
		IChangedJobEvent e = new ChangedJobEvent(this, jobs);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
		}
	}

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
	 * Send INewJobEvent to registered listeners
	 * 
	 * @param jobs
	 *            new jobs
	 * @since 5.0
	 */
	protected void fireNewJobs(Collection<IPJob> jobs) {
		INewJobEvent e = new NewJobEvent(this, jobs);

		for (Object listener : childListeners.getListeners()) {
			((IResourceManagerChildListener) listener).handleEvent(e);
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
	 * Send IRemoveJobEvent to registered listeners
	 * 
	 * @param job
	 *            removed jobs
	 * @since 5.0
	 */
	protected void fireRemoveJobs(Collection<IPJob> jobs) {
		IRemoveJobEvent e = new RemoveJobEvent(this, jobs);

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

	/**
	 * @param jobId
	 * @return
	 */
	protected IPJobControl getJobControl(String jobId) {
		synchronized (jobsById) {
			return jobsById.get(jobId);
		}
	}

	/**
	 * @since 5.0
	 */
	protected Collection<IPJobControl> getJobControls() {
		synchronized (jobsById) {
			List<IPJobControl> jobs = new ArrayList<IPJobControl>(jobsById.values().size());
			for (IPJobControl job : jobsById.values()) {
				jobs.add(job);
			}
			return jobs;
		}
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPMachineControl getMachineControl(String machineId) {
		synchronized (machinesById) {
			return machinesById.get(machineId);
		}
	}

	protected Collection<IPMachineControl> getMachineControls() {
		synchronized (machinesById) {
			List<IPMachineControl> machines = new ArrayList<IPMachineControl>(machinesById.values().size());
			for (IPMachineControl machine : machinesById.values()) {
				machines.add(machine);
			}
			return machines;
		}
	}

	/**
	 * @param nodeId
	 * @return
	 */
	protected IPNodeControl getNodeControl(String nodeId) {
		synchronized (nodesById) {
			return nodesById.get(nodeId);
		}
	}

	/**
	 * @param machineId
	 * @return
	 */
	protected IPQueueControl getQueueControl(String machineId) {
		synchronized (queuesById) {
			return queuesById.get(machineId);
		}
	}

	protected Collection<IPQueueControl> getQueueControls() {
		synchronized (queuesById) {
			List<IPQueueControl> queues = new ArrayList<IPQueueControl>(queuesById.values().size());
			for (IPQueueControl queue : queuesById.values()) {
				queues.add(queue);
			}
			return queues;
		}
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
	 * @param jobId
	 *            job ID for the job
	 * @param attrs
	 *            initial attributes for the job
	 * @return newly created job model element
	 * @since 5.0
	 */
	protected IPJobControl newJob(String jobId, AttributeManager attrs) {
		return new PJob(jobId, this, attrs.getAttributes());
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
		return new PMachine(machineId, this, this, attrs.getAttributes());
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
		return new PQueue(queueId, this, this, attrs.getAttributes());
	}

	/**
	 * @param job
	 * @since 5.0
	 */
	protected void removeJobs(Collection<IPJobControl> jobControls) {
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		synchronized (jobsById) {
			for (IPJobControl job : jobControls) {
				job.removeProcessesByJobRanks(job.getProcessJobRanks());
				jobsById.remove(job.getID());
				jobs.add(job);
			}
		}

		/*
		 * Remove jobs from any queues
		 */
		for (IPQueueControl queue : getQueueControls()) {
			queue.removeJobs(jobControls);
		}

		fireRemoveJobs(jobs);
	}

	/**
	 * @param machine
	 * @since 5.0
	 */
	protected void removeMachines(IPResourceManager rm, Collection<IPMachineControl> machineControls) {
		List<IPMachine> machines = new ArrayList<IPMachine>(machineControls.size());

		synchronized (machinesById) {
			for (IPMachineControl machine : machineControls) {
				machine.removeNodes(machine.getNodeControls());
				machine.removeChildListener(machineNodeListener);
				machinesById.remove(machine.getID());
				machines.add(machine);
			}
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

		synchronized (queuesById) {
			for (IPQueueControl queue : queueControls) {
				queue.removeJobs(queue.getJobControls());
				queuesById.remove(queue.getID());
				queues.add(queue);
			}
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
	 * @param jobs
	 *            jobs to update
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 * @since 5.0
	 */
	protected boolean updateJobs(Collection<IPJobControl> jobs, AttributeManager attrs) {
		addJobAttributes(jobs, attrs.getAttributes());
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