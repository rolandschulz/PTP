/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * @since 5.0
 */
public class RSEConnectionWorkingCopy extends RSEConnection implements IRemoteConnectionWorkingCopy {

	private final RSEConnection fConnection;

	public RSEConnectionWorkingCopy(RSEConnection conn) {
		super(conn.getHost(), conn.getRemoteServices());
		fConnection = conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnection#getWorkingCopy()
	 */
	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#save()
	 */
	public IRemoteConnection save() {
		return fConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		fConnection.getHost().setHostName(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setName(java.lang.String)
	 */
	public void setName(String name) {
		fConnection.getHost().setAliasName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPort(int)
	 */
	public void setPort(int port) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setUsername(java.lang.String)
	 */
	public void setUsername(String userName) {
		fConnection.getHost().setDefaultUserId(userName);
	}
}
