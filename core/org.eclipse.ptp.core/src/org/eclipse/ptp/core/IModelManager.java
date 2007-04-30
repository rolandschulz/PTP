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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerChangedListener;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;

public interface IModelManager extends IModelPresentation {

	public void addResourceManager(IResourceManagerControl addedManager);
	
	public void addResourceManagerChangedListener(IResourceManagerChangedListener listener);
	
	public void addResourceManagers(IResourceManagerControl[] addedManagers);
	public ILaunchConfiguration getPTPConfiguration();

	/**
	 * @return
	 */
	public IResourceManagerFactory[] getResourceManagerFactories();

	/**
	 * Find the resource manager factory corresponding to the supplied ID.
	 * @param id
	 * @return the requested resource manager factory
	 */
	public IResourceManagerFactory getResourceManagerFactory(String id);

	/**
	 * Loads and, if necessary, starts saved resource managers.
	 * @param monitor TODO
	 * @throws CoreException
	 */
	public void loadResourceManagers(IProgressMonitor monitor) throws CoreException;
	public void removeResourceManager(IResourceManager removedManager);
	public void removeResourceManagerChangedListener(IResourceManagerChangedListener listener);
	public void removeResourceManagers(IResourceManager[] removedRMs);
	
	public void saveResourceManagers();

	public void setPTPConfiguration(ILaunchConfiguration config);

	public void shutdown() throws CoreException;

	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * stops all of the resource managers.
	 * 
	 * @throws CoreException
	 */
	public void stopResourceManagers() throws CoreException;


}