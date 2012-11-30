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
package org.eclipse.ptp.core.elements;

import java.util.BitSet;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;

/**
 * Resource manager in the IP* hierarchy
 * 
 * @since 5.0
 */
public interface IPResourceManager extends IPElement, IAdaptable {

	/**
	 * Add a listener for child events.
	 * 
	 * @param listener
	 *            listener to add to the list of listeners
	 */
	public void addChildListener(IResourceManagerChildListener listener);

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
	public void addJobAttributes(Collection<IPJob> jobs, IAttribute<?, ?, ?>[] attrs);

	/**
	 * Add a collection of jobs to the model. This will result in a INewJobEvent being propagated to listeners on the RM. Supports
	 * either the old hierarchy where a parent queue is supplied, or each job can contain a queue attribute specifying the job
	 * queue.
	 * 
	 * @param queue
	 *            jobs will be added to the queue if specified
	 * @param jobs
	 *            collection of jobs to add
	 * @since 5.0
	 */
	public void addJobs(IPQueue queue, Collection<IPJob> jobs);

	/**
	 * Update attributes on a collection of machines.
	 * 
	 * @param machines
	 *            machines to update
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 */
	public void addMachineAttributes(Collection<IPMachine> machines, IAttribute<?, ?, ?>[] attrs);

	/**
	 * Add a collection of machines to the model. This will result in a INewMachineEvent being propagated to listeners on the RM.
	 * 
	 * @param machineControls
	 *            Collection of IMachineControls
	 */
	public void addMachines(Collection<IPMachine> machines);

	/**
	 * Add a collection of nodes to the model. This will result in a INewNodeEvent being propagated to listeners on the machine.
	 * 
	 * @param machine
	 *            parent of the nodes
	 * @param nodes
	 *            collection of IPNodeControls
	 * @since 5.0
	 */
	public void addNodes(IPMachine machine, Collection<IPNode> nodes);

	/**
	 * Add a collection of processes to the model. This involves adding the processes to a job *and* to a node. The node information
	 * is an attribute on the process. This will result in a INewProcessEvent being propagated to listeners on the job and nodes.
	 * 
	 * @param job
	 *            parent of the processes
	 * @param processJobRanks
	 *            set of job ranks within job
	 * @param attrs
	 * @since 5.0
	 */
	public void addProcessesByJobRanks(IPJob job, BitSet processJobRanks, AttributeManager attrs);

	/**
	 * Add attributes to a collection of queues.
	 * 
	 * @param queues
	 *            collection of queues
	 * @param attrs
	 *            array of attributes to add to each queue
	 * @since 5.0
	 */
	public void addQueueAttributes(Collection<IPQueue> queues, IAttribute<?, ?, ?>[] attrs);

	/**
	 * Add a collection of processes to the model. This will result in a INewProcessEvent being propagated to listeners on the job.
	 * 
	 * @param queues
	 *            collection of IPQueueControls
	 */
	public void addQueues(Collection<IPQueue> queues);

	/**
	 * Get the controlling resource manager
	 * 
	 * @return
	 * @since 6.0
	 */
	public String getControlId();

	/**
	 * Find a job object using its ID Returns null if no node is found.
	 * 
	 * @param id
	 *            ID of the job object
	 * @return job object corresponding to the ID
	 * @since 4.0
	 */
	public IPJob getJobById(String id);

	/**
	 * Get an array containing all the jobs known by this RM. If there are no jobs, an empty array is returned.
	 * 
	 * @return array of jobs known by this RM
	 */
	public IPJob[] getJobs();

	/**
	 * Find a machine object using its ID Returns null if no machine is found.
	 * 
	 * @param id
	 *            ID of the machine object
	 * @return machine object corresponding to the ID
	 */
	public IPMachine getMachineById(String id);

	/**
	 * Get an array containing all the machines known by this RM. If there are no machines, an empty array is returned.
	 * 
	 * @return array of machines known by this RM
	 */
	public IPMachine[] getMachines();

	/**
	 * Find a node object using its ID Returns null if no node is found.
	 * 
	 * @param id
	 *            ID of the node object
	 * @return node object corresponding to the ID
	 * @since 4.0
	 */
	public IPNode getNodeById(String id);

	/**
	 * Find a queue object using its ID. Returns null if no queue is found.
	 * 
	 * @param id
	 *            ID of the queue object
	 * @return queue object corresponding to the ID
	 */
	public IPQueue getQueueById(String id);

	/**
	 * Get an array containing all the queues known by this RM. If there are no queues, an empty array is returned.
	 * 
	 * @return array of queues known by this RM
	 */
	public IPQueue[] getQueues();

	/**
	 * Create a new job for the model.
	 * 
	 * @param jobId
	 *            job ID for the job
	 * @param attrs
	 *            initial attributes for the job
	 * @return newly created job model element
	 * @since 5.0
	 */
	public IPJob newJob(String jobId, AttributeManager attrs);

	/**
	 * Create a new machine for the model.
	 * 
	 * @param machineId
	 *            ID for the machine
	 * @param attrs
	 *            initial attributes for the machine
	 * @return newly created machine model element
	 * @since 5.0
	 */
	public IPMachine newMachine(String machineId, AttributeManager attrs);

	/**
	 * Create a new node for the model
	 * 
	 * @param machine
	 *            machine that this node belongs to
	 * @param nodeId
	 *            ID for the node
	 * @param attrs
	 *            initial attributes for the node
	 * @return newly created node model element
	 * @since 5.0
	 */
	public IPNode newNode(IPMachine machine, String nodeId, AttributeManager attrs);

	/**
	 * Create a new queue for the model
	 * 
	 * @param queueId
	 *            ID for the queue
	 * @param attrs
	 *            initial attributes for the queue
	 * @return newly created queue model element
	 * @since 5.0
	 */
	public IPQueue newQueue(String queueId, AttributeManager attrs);

	/**
	 * Remove listener for child events
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeChildListener(IResourceManagerChildListener listener);

	/**
	 * @param job
	 * @since 5.0
	 */
	public void removeJobs(Collection<IPJob> jobs);

	/**
	 * @param machine
	 * @since 5.0
	 */
	public void removeMachines(Collection<IPMachine> machines);

	/**
	 * Remove nodes from the machine.
	 * 
	 * @param machine
	 *            machine containing the nodes to remove
	 * @param nodes
	 *            nodes to remove from the model
	 * @since 5.0
	 */
	public void removeNodes(IPMachine machine, Collection<IPNode> nodes);

	/**
	 * @param queue
	 * @since 5.0
	 */
	public void removeQueues(Collection<IPQueue> queues);

	/**
	 * Remove all terminated jobs from the resource manager. A terminated job is determined by its state attribute.
	 */
	public void removeTerminatedJobs();
}
