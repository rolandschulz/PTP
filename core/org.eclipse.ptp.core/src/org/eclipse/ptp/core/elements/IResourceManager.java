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
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;

public interface IResourceManager extends IPElement,
	IAdaptable, IResourceManagerMenuContribution {

	/**
	 * Add a listener for child events.
	 * 
	 * @param listener listener to add to the list of listeners
	 */
	public void addChildListener(IResourceManagerChildListener listener);
	
	/**
	 * Add a listener for events related to this resource manager.
	 * 
	 * @param listener listener to add to the list of listeners
	 */
	public void addElementListener(IResourceManagerListener listener);

	/**
	 * Get the attribute definition corresponding to the attrId. This will
	 * only check for attribute definitions that the RM knows about.
	 * 
	 * @param attrId ID of the attribute definition
	 * @return the attribute definition corresponding to the attribute definition ID
	 */
	public IAttributeDefinition<?,?,?> getAttributeDefinition(String attrId);

	/**
	 * Get a string description of this RM
	 * 
	 * @return string describing the RM
	 */
	public String getDescription();
	
	/**
	 * Find a machine object using its ID
	 * 
	 * @param id ID of the machine object
	 * @return machine object corresponding to the ID
	 */
	public IPMachine getMachineById(String id);
	
	/**
	 * Get an array containing all the machines known by this RM
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
	 * Find a queue object using its ID
	 * 
	 * @param id ID of the queue object
	 * @return queue object corresponding to the ID
	 */
	public IPQueue getQueueById(String id);

	/**
	 * Fina a queue object using its name attribute
	 * 
	 * @param name name attribute of the queue object
	 * @return queue object corresponding to the name
	 */
	public IPQueue getQueueByName(String name);

	/**
	 * Get an array containing all the queues known by this RM
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
	 * Remove listener for child events
	 * 
	 * @param listener listener to remove
	 */
	public void removeChildListener(IResourceManagerChildListener listener);

	/**
	 * Remove listener for events relating to this resource manager
	 * 
	 * @param listener listener to remove
	 */
	public void removeElementListener(IResourceManagerListener listener);

	/**
	 * Remove all terminated jobs from the given queue. A terminated job is determined
	 * by its state attribute.
	 * 
	 * @param queue queue from which all terminated jobs will be removed
	 */
	public void removeTerminatedJobs(IPQueue queue);
	
	/**
	 * Shutdown the resource manager
	 */
	public void shutdown() throws CoreException;

	/**
	 * Start up the resource manager. This could potentially take a long time (or forever),
	 * particularly if the RM is located on a remote system.
	 * 
	 * @param monitor progress monitor to indicate startup progress
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException;

	/**
	 * Submit a job. The attribute manager must contain the appropriate attributes for
	 * a successful job launch (e.g. the queue, etc.)
	 * 
	 * @param attrMgr attribute manager containing the job launch attributes
	 * @param monitor progress monitor
	 * @return a job object representing the submitted job
	 * @throws CoreException
	 */
	public IPJob submitJob(AttributeManager attrMgr, IProgressMonitor monitor) 
		throws CoreException;

	/**
	 * Terminate the job. The action this takes depends on the RM implementation and
	 * the state of the job. For queued but not running jobs, this would be equivalent
	 * to canceling the job. For running jobs, this would mean halting its execution.
	 * 
	 * @param job job object representing the job to be canceled.
	 */
	public void terminateJob(IPJob job) throws CoreException;

	/**
	 * Get a unique name that can be used to identify this resource
	 * manager persistently between PTP invocations. Used by the 
	 * ResourceManagerPersistence.
	 * 
	 * @return string representing a unique name for the resource manager
	 */
	public String getUniqueName();
}
