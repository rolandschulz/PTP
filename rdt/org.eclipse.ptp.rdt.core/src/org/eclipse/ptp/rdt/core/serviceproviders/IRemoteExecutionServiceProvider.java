/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.serviceproviders;

import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;

/**
 * @author crecoskie
 * 
 * Provides execution services for shell commands over a remote connection, and remote file manipulation services.
 *
 */
public interface IRemoteExecutionServiceProvider extends IServiceProvider {
	
	/**
	 * Gets the provider of remote services.
	 * 
	 * @return IRemoteServices
	 */
	public IRemoteServices getRemoteServices();
	
	/**
	 * Gets the connection to use for this service.  The connection may not be open, so
	 * clients should check to make sure it is open before using it.
	 * 
	 * @return IRemoteConnection
	 */
	public IRemoteConnection getConnection();
}
