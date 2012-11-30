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
package org.eclipse.ptp.internal.core.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
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
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.internal.core.elements.events.ChangedMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.ChangedQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.NewJobEvent;
import org.eclipse.ptp.internal.core.elements.events.NewMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.NewQueueEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveJobEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveMachineEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveQueueEvent;

/**
 * @author rsqrd
 * 
 */
public class PResourceManager extends Parent implements IPResourceManager {

	private static IAttribute<?, ?, ?>[] getDefaultAttributes(String id) {
		ArrayList<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();
		return attrs.toArray(new IAttribute<?, ?, ?>[0]);
	}

	private final ListenerList childListeners = new ListenerList();
	private final String fControlId;
	private final String fName;

	private final IMachineChildListener machineNodeListener;

	private final Map<String, IPJob> jobsById = Collections.synchronizedMap(new HashMap<String, IPJob>());
	private final Map<String, IPMachine> machinesById = Collections.synchronizedMap(new HashMap<String, IPMachine>());
	private final Map<String, IPNode> nodesById = Collections.synchronizedMap(new HashMap<String, IPNode>());
	private final Map<String, IPQueue> queuesById = Collections.synchronizedMap(new HashMap<String, IPQueue>());

	/**
	 * @since 5.0
	 */
	public PResourceManager(IPUniverse universe, String name, String controlId) {
		super(universe.getNextResourceManagerId(), universe, getDefaultAttributes(controlId));
		fControlId = controlId;
		fName = name;

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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addChildListener(org.
	 * eclipse.ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void addChildListener(IResourceManagerChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addJobAttributes(java .util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addJobAttributes(Collection<IPJob> jobs, IAttribute<?, ?, ?>[] attrs) {
		for (IPJob job : jobs) {
			job.addAttributes(attrs);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addJobs(org.eclipse.ptp .core.elements.IPQueue, java.util.Collection)
	 */
	public void addJobs(IPQueue queue, Collection<IPJob> jobs) {
		Map<IPQueue, List<IPJob>> map = new HashMap<IPQueue, List<IPJob>>();

		for (IPJob job : jobs) {
			StringAttribute queueIdAttr = job.getAttribute(JobAttributes.getQueueIdAttributeDefinition());
			if (queueIdAttr != null) {
				IPQueue jQueue = getQueueById(queueIdAttr.getValue());
				List<IPJob> qJobs = map.get(jQueue);
				if (qJobs == null) {
					qJobs = new ArrayList<IPJob>();
					map.put(queue, qJobs);
				}
			}
			jobsById.put(job.getID(), job);
		}

		/*
		 * Add jobs to any queues that were specified as attributes on the job
		 */
		for (Map.Entry<IPQueue, List<IPJob>> entry : map.entrySet()) {
			entry.getKey().addJobs(entry.getValue());
		}

		/*
		 * Add jobs to the parent queue if supplied
		 */
		if (queue != null) {
			queue.addJobs(jobs);
		}

		fireNewJobs(jobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addMachineAttributes( java.util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addMachineAttributes(Collection<IPMachine> machines, IAttribute<?, ?, ?>[] attrs) {
		for (IPMachine machine : machines) {
			machine.addAttributes(attrs);
		}

		fireChangedMachines(machines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addMachines(java.util .Collection)
	 */
	public void addMachines(Collection<IPMachine> machines) {
		synchronized (machinesById) {
			for (IPMachine machine : machines) {
				machinesById.put(machine.getID(), machine);
				machine.addChildListener(machineNodeListener);
			}
		}

		fireNewMachines(machines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addNodes(org.eclipse. ptp.core.elements.IPMachine, java.util.Collection)
	 */
	public void addNodes(IPMachine machine, Collection<IPNode> nodes) {
		synchronized (nodesById) {
			for (IPNode node : nodes) {
				nodesById.put(node.getID(), node);
			}
		}

		machine.addNodes(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addProcessesByJobRanks (org.eclipse.ptp.core.elements.IPJob,
	 * java.util.BitSet, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void addProcessesByJobRanks(IPJob job, BitSet processJobRanks, AttributeManager attrs) {

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
			final IPNode node = getNodeById(nodeIdAttr.getValue());
			if (node != null) {
				final BitSet nodesProcessJobRanks = job.getProcessJobRanks(nodeIdAttr);
				node.addJobProcessRanks(job, nodesProcessJobRanks);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addQueueAttributes(java .util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addQueueAttributes(Collection<IPQueue> queues, IAttribute<?, ?, ?>[] attrs) {
		for (IPQueue queue : queues) {
			queue.addAttributes(attrs);
		}

		fireChangedQueues(queues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#addQueues(java.util. Collection)
	 */
	public void addQueues(Collection<IPQueue> queues) {
		synchronized (queuesById) {
			for (IPQueue queue : queues) {
				queuesById.put(queue.getID(), queue);
			}
		}

		fireNewQueues(queues);
	}

	public void dispose() {
		childListeners.clear();
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
		return super.getAdapter(adapter);
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getControlId()
	 */
	public String getControlId() {
		return fControlId;
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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getJobById(java.lang. String)
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
		synchronized (jobsById) {
			return jobsById.values().toArray(new IPJob[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getMachineById(java.lang .String)
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
		synchronized (machinesById) {
			return machinesById.values().toArray(new IPMachine[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getNodeById(java.lang. String)
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
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#getQueueById(java.lang .String)
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
		synchronized (queuesById) {
			return queuesById.values().toArray(new IPQueue[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#newJob(java.lang.String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public IPJob newJob(String jobId, AttributeManager attrs) {
		return new PJob(jobId, fControlId, this, attrs.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#newMachine(java.lang. String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public IPMachine newMachine(String machineId, AttributeManager attrs) {
		return new PMachine(machineId, fControlId, this, attrs.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#newNode(org.eclipse.ptp .core.elements.IPMachine, java.lang.String,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public IPNode newNode(IPMachine machine, String nodeId, AttributeManager attrs) {
		return new PNode(nodeId, machine, attrs.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#newQueue(java.lang.String ,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public IPQueue newQueue(String queueId, AttributeManager attrs) {
		return new PQueue(queueId, fControlId, this, attrs.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeChildListener(org
	 * .eclipse.ptp.core.elements.listeners.IResourceManagerChildListener)
	 */
	public void removeChildListener(IResourceManagerChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeJobs(java.util. Collection)
	 */
	public void removeJobs(Collection<IPJob> jobs) {
		synchronized (jobsById) {
			for (IPJob job : jobs) {
				job.removeProcessesByJobRanks(job.getProcessJobRanks());
				jobsById.remove(job.getID());
			}
		}

		/*
		 * Remove jobs from any queues
		 */
		for (IPQueue queue : getQueues()) {
			queue.removeJobs(jobs);
		}

		fireRemoveJobs(jobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeMachines(java.util .Collection)
	 */
	public void removeMachines(Collection<IPMachine> machines) {
		synchronized (machinesById) {
			for (IPMachine machine : machines) {
				machine.removeNodes(Arrays.asList(machine.getNodes()));
				machine.removeChildListener(machineNodeListener);
				machinesById.remove(machine.getID());
			}
		}

		fireRemoveMachines(machines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeNodes(org.eclipse .ptp.core.elements.IPMachine,
	 * java.util.Collection)
	 */
	public void removeNodes(IPMachine machine, Collection<IPNode> nodes) {
		machine.removeNodes(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeQueues(java.util .Collection)
	 */
	public void removeQueues(Collection<IPQueue> queues) {
		synchronized (queuesById) {
			for (IPQueue queue : queues) {
				queue.removeJobs(Arrays.asList(queue.getJobs()));
				queuesById.remove(queue.getID());
			}
		}

		fireRemoveQueues(queues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPResourceManager#removeTerminatedJobs()
	 */
	/**
	 * @since 5.0
	 */
	public void removeTerminatedJobs() {
		List<IPJob> terminatedJobs = new ArrayList<IPJob>();

		for (IPJob job : getJobs()) {
			if (job.getState() == JobAttributes.State.COMPLETED) {
				terminatedJobs.add(job);
			}
		}
		removeJobs(terminatedJobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
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
	 * @param string
	 * @return
	 */
	protected CoreException makeCoreException(String string) {
		IStatus status = new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, string, null);
		return new CoreException(status);
	}
}