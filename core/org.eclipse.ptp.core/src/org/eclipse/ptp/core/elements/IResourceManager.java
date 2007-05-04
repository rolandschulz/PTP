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
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;

public interface IResourceManager extends IPElement,
IAdaptable, IResourceManagerMenuContribution {

	/**
	 * @param listener
	 */
	public void addChildListener(IResourceManagerMachineListener listener);
	
	/**
	 * @param listener
	 */
	public void addChildListener(IResourceManagerQueueListener listener);

	/**
	 * @param listener
	 */
	public void addElementListener(IResourceManagerListener listener);

	/**
	 * 
	 * @param attrId
	 * @return
	 */
	public IAttributeDefinition getAttributeDefinition(String attrId);

	/**
	 * @return
	 */
	public String getDescription();
	
	/**
	 * @param id
	 * @return
	 */
	public IPMachine getMachineById(String id);
	
	/**
	 * @return
	 */
	public IPMachine[] getMachines();

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @param id
	 * @return IPQueue
	 */
	public IPQueue getQueueById(String id);

	/**
	 * @param name
	 * @return IPQueue
	 */
	public IPQueue getQueueByName(String name);

	/**
	 * @return
	 */
	public IPQueue[] getQueues();

	/**
	 * @return
	 */
	public ResourceManagerAttributes.State getState();
	
	/**
	 * @param listener
	 */
	public void removeChildListener(IResourceManagerMachineListener listener);

	/**
	 * @param listener
	 */
	public void removeChildListener(IResourceManagerQueueListener listener);

	/**
	 * @param listener
	 */
	public void removeElementListener(IResourceManagerListener listener);

	/**
	 * @param queue
	 */
	public void removeTerminatedJobs(IPQueue queue);
	
	/**
	 * 
	 */
	public void shutdown() throws CoreException;

	/**
	 * @param monitor
	 * 
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException;

	/**
	 * @param attrMgr
	 * @param pm
	 * @return IPJob
	 * @throws CoreException
	 */
	public IPJob submitJob(AttributeManager attrMgr, IProgressMonitor pm) 
		throws CoreException;
	
	/**
	 * @param job
	 */
	public void terminateJob(IPJob job) throws CoreException;
}
