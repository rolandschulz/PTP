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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
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
	 * Find a job object using its ID Returns null if no node is found.
	 * 
	 * @param id
	 *            ID of the job object
	 * @return job object corresponding to the ID
	 * @since 4.0
	 */
	public IPJob getJobById(String id);

	/**
	 * Get an array containing all the jobs known by this RM. If there are no
	 * jobs, an empty array is returned.
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
	 * Get an array containing all the machines known by this RM. If there are
	 * no machines, an empty array is returned.
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
	 * Get an array containing all the queues known by this RM. If there are no
	 * queues, an empty array is returned.
	 * 
	 * @return array of queues known by this RM
	 */
	public IPQueue[] getQueues();

	/**
	 * Get the controlling resource manager
	 * 
	 * @return
	 */
	public IResourceManagerControl getResourceManager();

	/**
	 * Get the state of this RM
	 * 
	 * @return state value representing the state of the RM
	 */
	public ResourceManagerAttributes.State getState();

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
}
