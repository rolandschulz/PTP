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

import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewQueuesEvent;


/**
 * @author rsqrd
 * 
 */
public interface IResourceManagerListener {
	
	/**
	 * @param e
	 */
	public void handleChangedJobsEvent(IResourceManagerChangedJobsEvent e);
	
	/**
	 * @param e
	 */
	public void handleChangedMachinesEvent(IResourceManagerChangedMachinesEvent e);
	
	/**
	 * @param e
	 */
	public void handleChangedNodesEvent(IResourceManagerChangedNodesEvent e);
	
	/**
	 * @param e
	 */
	public void handleChangedProcessesEvent(IResourceManagerChangedProcessesEvent e);
	
	/**
	 * @param e
	 */
	public void handleChangedQueuesEvent(IResourceManagerChangedQueuesEvent e);

	/**
     * @param e
     */
    public void handleErrorStateEvent(IResourceManagerErrorEvent e);

    /**
	 * @param e
	 */
	public void handleNewJobsEvent(IResourceManagerNewJobsEvent e);

    /**
	 * @param e
	 */
	public void handleNewMachinesEvent(IResourceManagerNewMachinesEvent e);

	/**
	 * @param e
	 */
	public void handleNewNodesEvent(IResourceManagerNewNodesEvent e);

	/**
	 * @param e
	 */
	public void handleNewProcessesEvent(IResourceManagerNewProcessesEvent e);

	/**
	 * @param e
	 */
	public void handleNewQueuesEvent(IResourceManagerNewQueuesEvent e);

	/**
	 * @param resourceManager
	 */
	public void handleShutdownStateEvent(IResourceManager resourceManager);

	/**
	 * @param resourceManager
	 */
	public void handleStartupStateEvent(IResourceManager resourceManager);

	/**
     * @param manager
     */
    public void handleSuspendedStateEvent(AbstractResourceManager manager);
}
