/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Additional interface that must be implemented by service providers that operate on remote hosts
 * This method is called by the BuildConfigurationManager whenever the provider needs to adapt itself to a new remote location.
 * Thus, service providers operating on a remote host should implement this by changing internal data structures properly.
 * @since 2.1
 */
public interface IRemoteServiceProvider {
	public void changeRemoteInformation(IRemoteConnection remoteConnection, String location);

}
