/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.remote.core.IRemoteConnection;

public interface IRemoteToolsIndexServiceProvider extends IIndexServiceProvider2 {
	/**
	 * Get the remote connection used by a service provider
	 * 
	 * @return remote connection
	 */
	public IRemoteConnection getConnection();

	/**
	 * Set the remote connection used by a service provider
	 * 
	 * @param conn
	 *            remote connection
	 */
	public void setConnection(IRemoteConnection conn);
}
