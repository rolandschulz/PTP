/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.listeners.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.services.core.ServiceModelManager;

public interface IModelManager extends IModelPresentation {
	/**
	 * Add a listener for resource manager events.
	 * 
	 * @param listener
	 * @since 5.0
	 */
	public void addListener(IResourceManagerListener listener);

	/**
	 * Add a resource manager to the model.
	 * 
	 * @param addedManager
	 * @since 5.0
	 */
	public void addResourceManager(IResourceManagerControl addedManager);

	/**
	 * Add resource managers to the model.
	 * 
	 * @param addedManagers
	 * @since 5.0
	 */
	public void addResourceManagers(IResourceManagerControl[] addedManagers);

	/**
	 * Find the resource manager with the supplied unique name
	 * 
	 * @param rmUniqueName
	 * @return resource manager
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManagerFromUniqueName(String rmUniqueName);

	/**
	 * Get resource managers from the model.
	 * 
	 * @return all resource managers know by the model
	 * @since 5.0
	 */
	public IResourceManagerControl[] getResourceManagers();

	/**
	 * Loads saved resource managers. Loading of resource manager configuration
	 * is now handled by the {@link ServiceModelManager}. This method now just
	 * starts any resource managers that require autostart.
	 * 
	 * @throws CoreException
	 */
	public void loadResourceManagers() throws CoreException;

	/**
	 * Remove listener for resource manager events.
	 * 
	 * @param listener
	 * @since 5.0
	 */
	public void removeListener(IResourceManagerListener listener);

	/**
	 * Remove a resource manager from the model.
	 * 
	 * @param removedManager
	 * @since 5.0
	 */
	public void removeResourceManager(IResourceManagerControl removedManager);

	/**
	 * Remove resource managers from the model.
	 * 
	 * @param removedRMs
	 * @since 5.0
	 */
	public void removeResourceManagers(IResourceManagerConfiguration[] removedRMs);

	/**
	 * Save the resource manager configurations
	 * 
	 * @deprecated Resource manager persistence is handled by
	 *             {@link ServiceModelManager}
	 */
	@Deprecated
	public void saveResourceManagers();

	/**
	 * Shuts down the model manager. Should only be called at plugin shutdown.
	 * 
	 * @throws CoreException
	 */
	public void shutdown() throws CoreException;

	/**
	 * Start the model manager. Should only be called once at plugin startup.
	 * 
	 * @throws CoreException
	 */
	public void start() throws CoreException;

	/**
	 * stops all of the resource managers.
	 * 
	 * @throws CoreException
	 */
	public void stopResourceManagers() throws CoreException;
}