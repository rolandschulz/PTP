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
import org.eclipse.ptp.core.IModelModifier;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public interface IResourceManager extends IPElementControl,
IAdaptable, IResourceManagerMenuContribution, IModelModifier {
	
	/**
	 * @param id
	 * @throws CoreException
	 */
	public void abortJob(String id) throws CoreException;
	
	/**
	 * @param listener
	 */
	public void addResourceManagerListener(IResourceManagerListener listener);
	
	/**
	 * 
	 */
	public void dispose();
	
	/**
	 * @return
	 */
	public IResourceManagerConfiguration getConfiguration();

	/**
	 * @return
	 */
	public String getDescription();
	
	/**
	 * @param ID
	 * @return
	 */
	public IPMachine getMachine(String ID);

	/**
	 * @return
	 */
	public IPMachineControl[] getMachineControls();

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
	 * @return
	 */
	public IPQueue getQueue(int id);

	/**
	 * @return
	 */
	public IPQueueControl[] getQueueControls();

	/**
	 * @return
	 */
	public IPQueue[] getQueues();

	/**
	 * @return
	 */
	public ResourceManagerStatus getStatus();
	
	/**
	 * @param monitor
	 * @param force
	 * @throws CoreException
	 */
	public void refreshRuntimeSystems(IProgressMonitor monitor,
			boolean force) throws CoreException;
	
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
	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException;

	/**
	 * 
	 */
	public void start() throws CoreException;

	/**
	 * 
	 */
	public void stop() throws CoreException;
}
