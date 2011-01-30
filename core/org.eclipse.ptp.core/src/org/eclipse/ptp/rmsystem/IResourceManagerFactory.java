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

import org.eclipse.ui.IMemento;

/**
 * @author rsqrd
 * 
 */
public interface IResourceManagerFactory {
	/**
	 * Copy a configuration. This can be used to provide a temporary working
	 * copy of a configuration
	 * 
	 * @param configuration
	 *            configuration to copy
	 * @return copy of the configuration
	 */
	public IResourceManagerConfiguration copyConfiguration(IResourceManagerConfiguration configuration);

	/**
	 * Create a resource manager using the supplied configuration
	 * 
	 * @param configuration
	 *            configuration to use when creating resource manager
	 * @return resource manager control
	 * @since 5.0
	 */
	public IResourceManagerControl create(IResourceManagerConfiguration configuration);

	/**
	 * Create a default configuration
	 * 
	 * @return default configuration
	 */
	public IResourceManagerConfiguration createConfiguration();

	/**
	 * Get the ID of this factory
	 * 
	 * @return factory ID
	 */
	public String getId();

	/**
	 * Get the name of this factory
	 * 
	 * @return factory name
	 */
	public String getName();

	/**
	 * Load a resource manager configuration from saved state
	 * 
	 * @param memento
	 *            saved state of configuration
	 * @return resource manager configuration
	 */
	public IResourceManagerConfiguration loadConfiguration(IMemento memento);

	/**
	 * Replace a resource manager configuration with a working copy.
	 * 
	 * @param configuration
	 *            working copy
	 * @param resourceManager
	 *            resource manager
	 * @since 5.0
	 */
	public void saveConfiguration(IResourceManagerConfiguration configuration, IResourceManagerControl resourceManager);
}
