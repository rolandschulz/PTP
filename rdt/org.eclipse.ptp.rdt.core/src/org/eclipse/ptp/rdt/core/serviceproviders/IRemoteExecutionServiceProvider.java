/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.serviceproviders;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Provides execution services for shell commands over a remote connection, and
 * remote file manipulation services.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 */
public interface IRemoteExecutionServiceProvider extends IServiceProvider {

	/**
	 * Gets the provider of remote services.
	 * 
	 * @return IRemoteServices
	 * @since 5.0
	 */
	public IRemoteServices getRemoteServices();

	/**
	 * Gets the connection to use for this service. The connection may not be
	 * open, so clients should check to make sure it is open before using it.
	 * 
	 * @return IRemoteConnection
	 * @since 5.0
	 */
	public IRemoteConnection getConnection();

	/**
	 * Gets the path to the configuration area on the server.
	 * 
	 * @return String
	 * @since 2.0
	 */
	public String getConfigLocation();
	
	/**
	 * Set the connection to use for this service.
	 * 
	 * @param connection remote connection
	 * @since 5.0
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection);
	
	/**
	 * Set the path to the configuration area on the server.
	 * 
	 * @param configLocation path to the configuration area
	 * @since 3.1
	 */
	public void setConfigLocation(String configLocation);
}
