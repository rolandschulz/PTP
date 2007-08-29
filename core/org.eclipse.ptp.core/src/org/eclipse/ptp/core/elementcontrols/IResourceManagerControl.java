/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.core.elementcontrols;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public interface IResourceManagerControl extends IResourceManager, IPElementControl {

	/**
	 * Disable event processing. The RM state will be set to SUSPENDED.
	 * 
	 * @throws CoreException
	 */
	public void disableEvents() throws CoreException;

	/**
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

	/**
	 * Enable event processing. The RM state will be set to STARTED.
	 * 
	 * @throws CoreException
	 */
	public void enableEvents() throws CoreException;
	
	/**
	 * Get the configuration associated with this resource manager.
	 * 
	 * @return resource manager configuration
	 */
	public IResourceManagerConfiguration getConfiguration();
	
	/**
	 * Get the IPMachineControl interfaces for machines that this resource manager knows about.
	 * 
	 * @return IPMachineControl interfaces
	 */
	public IPMachineControl[] getMachineControls();
	
	/**
	 * Get the IPQueueControl interfaces for queues that this resource manager knows about.
	 * 
	 * @return IPQueueControl interfaces
	 */
	public IPQueueControl[] getQueueControls();
	
	/**
	 * Set the configuration for this resource manager. This will replace the existing
	 * configuration with a new configuration. The method is responsible for dealing with
	 * any saved state that needs to be cleaned up.
	 * 
	 * @param config the new configuration
	 */
	public void setConfiguration(IResourceManagerConfiguration config);

}
