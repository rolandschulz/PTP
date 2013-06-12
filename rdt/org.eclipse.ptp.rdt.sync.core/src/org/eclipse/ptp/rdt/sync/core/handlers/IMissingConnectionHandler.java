/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.handlers;

import org.eclipse.ptp.remote.core.IRemoteServices;

/**
 * Class for encapsulating logic to handle missing connections. The UI can specify a default handler for the core to execute so
 * that the code does not need to be duplicated or invoked in all places where a connection is not found.
 * 
 * @since 3.0
 */
public interface IMissingConnectionHandler {
	/**
	 * Handle missing connection
	 * 
	 * @param remoteServices
	 *            remote services for the connection
	 * @param connectionName
	 *            connection name
	 */
	public void handle(IRemoteServices remoteServices, String connectionName);
}