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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;

public interface IResourceManagerConfiguration extends IAdaptable {
	/**
	 * Get the auto start flag for this resource manager. If the auto start flag
	 * is set to true, an attempt will be made to start the resource manager
	 * when Eclipse is launched.
	 * 
	 * @return state of the auto start flag
	 */
	public boolean getAutoStart();

	/**
	 * Get the connection name. This is a string used by the remote service
	 * provider to identify a particular connection. A resource manager only
	 * supports a single connection at time. If the resource manager is purely
	 * local, then this will be the name of the local connection.
	 * 
	 * @return connection name
	 */
	public String getConnectionName();

	/**
	 * Returns the description of the resource manager.
	 * 
	 * @return the description of the resource manager
	 */
	public String getDescription();

	/**
	 * Returns the name of the resource manager.
	 * 
	 * @return the name of the resource manager
	 */
	public String getName();

	/**
	 * Get the ID of the remote service provider used by this resource manager.
	 * If the resource manager is local only, then this will be the ID of the
	 * local service provider.
	 * 
	 * @return remote service provider ID
	 */
	public String getRemoteServicesId();

	/**
	 * Returns the id of the factory that created the resource manager.
	 * 
	 * @return the id of the factory that created the resource manager
	 */
	public String getResourceManagerId();

	/**
	 * Returns the type of the resource manager. This is the name of the
	 * resource manager factory.
	 * 
	 * @return the type of the resource manager
	 */
	public String getType();

	/**
	 * Get a unique name for this resource manager.
	 * 
	 * @return unique name
	 */
	public String getUniqueName();

	/**
	 * This resource manager needs the debugger to help with a debug launch.
	 * What this means depends on the type of the debugger. See the debugger
	 * implementation for details.
	 * 
	 * @return true if help is required
	 */
	public boolean needsDebuggerLaunchHelp();

	/**
	 * Save the state of the configuration.
	 * 
	 * @param memento
	 * @deprecated
	 */
	@Deprecated
	public void save(IMemento memento);

	/**
	 * Set the auto start flag for this resource manager. If the auto start flag
	 * is set to true, an attempt will be made to start the resource manager
	 * when Eclipse is launched.
	 * 
	 * @param flag
	 *            auto start flag
	 */
	public void setAutoStart(boolean flag);

	/**
	 * Set the name of the connection used by this resource manager. The
	 * connection name is unique to a particular remote service provider.
	 * 
	 * @param connectionName
	 *            name of connection used by the resource manager
	 */
	public void setConnectionName(String connectionName);

	/**
	 * The the name and description to default values.
	 */
	public void setDefaultNameAndDesc();

	/**
	 * Set the description of the resource manager
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * Set the name of the resource manager
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Set the remote service provider ID.
	 * 
	 * @param id
	 *            remote service provider extension ID
	 */
	public void setRemoteServicesId(String id);
}
