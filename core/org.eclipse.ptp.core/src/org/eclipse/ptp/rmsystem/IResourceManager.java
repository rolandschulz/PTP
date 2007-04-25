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
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public interface IResourceManager extends IPElement,
IAdaptable, IResourceManagerMenuContribution {

	/**
	 * @param listener
	 */
	public void addResourceManagerListener(IResourceManagerListener listener);
	
	/**
	 * @param job_id
	 * @return
	 */
	public IPJob findJobById(String job_id);
	
	/**
	 * @return
	 */
	public String getDescription();

	/**
	 * @param queueName
	 * @param currentAttrs 
	 * @return
	 */
	public IAttribute[] getLaunchAttributes(String queueName, IAttribute[] currentAttrs);

	/**
	 * 
	 * @param attrId
	 * @return
	 */
	public IAttributeDefinition getAttributeDefinition(String attrId);
	
	/**
	 * @param ID
	 * @return
	 */
	public IPMachine getMachine(int ID);

	/**
	 * @return
	 */
	public IPJob[] getJobs();

	/**
	 * @return
	 */
	public IPMachine[] getMachines();

	/**
	 * @return
	 */
	public IPProcess[] getProcesses();

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @return
	 */
	public IPQueue[] getQueues();

	/**
	 * @return
	 */
	public ResourceManagerState.State getState();

	/**
	 * @param job
	 */
	public void removeJob(IPJob job);
	
	/**
	 * @param listener
	 */
	public void removeResourceManagerListener(IResourceManagerListener listener);
	
	/**
	 * @param launch
	 * @param jobRunConfig
	 * @param pm
	 * @return
	 * @throws CoreException
	 */
	public IPJob submitJob(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException;

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
	 * @param job
	 */
	public void terminateJob(IPJob job) throws CoreException;

}
