/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rmsystem;

/**
 * @since 5.0
 */
public interface IResourceManagerComponentConfiguration {
	/**
	 * Returns the name of the resource manager.
	 * 
	 * @return the name of the resource manager
	 */
	public String getName();

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
	 * Get the ID of the remote service provider used by this resource manager.
	 * If the resource manager is local only, then this will be the ID of the
	 * local service provider.
	 * 
	 * @return remote service provider ID
	 */
	public String getRemoteServicesId();

	/**
	 * Set the name of the connection used by this resource manager. The
	 * connection name is unique to a particular remote service provider.
	 * 
	 * @param connectionName
	 *            name of connection used by the resource manager
	 */
	public void setConnectionName(String connectionName);

	/**
	 * Set the remote service provider ID.
	 * 
	 * @param id
	 *            remote service provider extension ID
	 */
	public void setRemoteServicesId(String id);
}
