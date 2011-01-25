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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;

/**
 * Resource manager in the IP* hierarchy
 * 
 * @since 5.0
 */
public interface IPResourceManager extends IPElement, IAdaptable, IResourceManagerMenuContribution {

	/**
	 * Add a listener for child events.
	 * 
	 * @param listener
	 *            listener to add to the list of listeners
	 */
	public void addChildListener(IResourceManagerChildListener listener);

	/**
	 * Add a listener for events related to this resource manager.
	 * 
	 * @param listener
	 *            listener to add to the list of listeners
	 */
	public void addElementListener(IResourceManagerListener listener);

	/**
	 * Get the attribute definition corresponding to the attrId. This will only
	 * check for attribute definitions that the RM knows about.
	 * 
	 * @param attrId
	 *            ID of the attribute definition
	 * @return the attribute definition corresponding to the attribute
	 *         definition ID
	 */
	public IAttributeDefinition<?, ?, ?> getAttributeDefinition(String attrId);

	/**
	 * Get a string description of this RM
	 * 
	 * @return string describing the RM
	 */
	public String getDescription();

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
	 * Find a machine object using its ID Returns null if no machine is found.
	 * 
	 * @param id
	 *            ID of the machine object
	 * @return machine object corresponding to the ID
	 */
	public IPMachine getMachineById(String id);

	/**
	 * Get an array containing all the machines known by this RM. If there are
	 * no machines, an empty array is returned.
	 * 
	 * @return array of machines known by this RM
	 */
	public IPMachine[] getMachines();

	/**
	 * Get the name of this RM
	 * 
	 * @return string name of the RM
	 */
	public String getName();

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
	 * Get an array containing all the queues known by this RM. If there are no
	 * queues, an empty array is returned.
	 * 
	 * @return array of queues known by this RM
	 */
	public IPQueue[] getQueues();

	/**
	 * Returns the id of the resource manager
	 * 
	 * @return the id of the resource manager
	 */
	public String getResourceManagerId();

	/**
	 * Get the state of this RM
	 * 
	 * @return state value representing the state of the RM
	 */
	public ResourceManagerAttributes.State getState();

	/**
	 * Get a unique name that can be used to identify this resource manager
	 * persistently between PTP invocations. Used by the
	 * ResourceManagerPersistence.
	 * 
	 * @return string representing a unique name for the resource manager
	 */
	public String getUniqueName();

	/**
	 * Remove listener for child events
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeChildListener(IResourceManagerChildListener listener);

	/**
	 * Remove listener for events relating to this resource manager
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeElementListener(IResourceManagerListener listener);

	/**
	 * Remove all terminated jobs from the resource manager. A terminated job is
	 * determined by its state attribute.
	 */
	public void removeTerminatedJobs();

	/**
	 * Shutdown the resource manager.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the shutdown command fails
	 */
	public void shutdown() throws CoreException;

	/**
	 * Start up the resource manager. This could potentially take a long time
	 * (or forever), particularly if the RM is located on a remote system.
	 * 
	 * Callers can assume that the operation was successful if no exception is
	 * thrown and the monitor was not cancelled. However, the resource manager
	 * may still fail later due to some other condition.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts null, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @throws CoreException
	 *             this exception is thrown if the resource manager fails to
	 *             start
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException;

	/**
	 * Submit a job. The attribute manager must contain the appropriate
	 * attributes for a successful job launch (e.g. the queue, etc.).
	 * 
	 * The method will return after an INewJobEvent has been received confirming
	 * that the job has been submitted.
	 * 
	 * @param configuration
	 *            launch configuration used to submit the job. Can be null.
	 * @param attrMgr
	 *            attribute manager containing the job launch attributes
	 * @param monitor
	 *            progress monitor
	 * @return a job object representing the submitted job or null if the
	 *         submission failed
	 * @throws CoreException
	 */
	public IPJob submitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Submit a job with the supplied submission ID. This ID can be used to
	 * identify the job when an INewJobEvent is received.
	 * 
	 * The method will return immediately. It is up to the calling thread to
	 * check for successful submission by registering a listener for
	 * INewJobEvent.
	 * 
	 * @param subId
	 *            job submission ID
	 * @param configuration
	 *            launch configuration used to submit the job. Can be null.
	 * @param attrMgr
	 *            attribute manager containing the job launch attributes
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	public void submitJob(String subId, ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Terminate the job. The action this takes depends on the RM implementation
	 * and the state of the job. For queued but not running jobs, this would be
	 * equivalent to canceling the job. For running jobs, this would mean
	 * halting its execution.
	 * 
	 * @param job
	 *            job object representing the job to be canceled.
	 * @throws CoreException
	 */
	public void terminateJob(IPJob job) throws CoreException;
}
