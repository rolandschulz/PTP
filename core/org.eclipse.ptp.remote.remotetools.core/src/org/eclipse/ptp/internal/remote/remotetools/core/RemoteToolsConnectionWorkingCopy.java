/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.util.Map;

import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * @since 5.0
 */
public class RemoteToolsConnectionWorkingCopy extends RemoteToolsConnection implements IRemoteConnectionWorkingCopy {

	private final RemoteToolsConnection fConnection;
	private String fName;

	public RemoteToolsConnectionWorkingCopy(RemoteToolsConnection conn) {
		super(conn.getName(), new TargetElement(RemoteToolsServices.getTargetTypeElement(), conn.getName(), conn.getTargetControl()
				.getConfig().getAttributes(), EnvironmentPlugin.getDefault().getEnvironmentUniqueID()), conn.getRemoteServices());
		fConnection = conn;
		fName = conn.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnection#getAddress()
	 */
	@Override
	public String getAddress() {
		return getTargetControl().getConfig().getConnectionAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnection#getAttributes()
	 */
	@Override
	public Map<String, String> getAttributes() {
		return getTargetControl().getConfig().getAttributes().getAttributesAsMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getPort()
	 */
	@Override
	public int getPort() {
		return getTargetControl().getConfig().getConnectionPort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	@Override
	public String getUsername() {
		return getTargetControl().getConfig().getLoginUsername();
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
		fConnection.getTargetElement().setAttributes(getTargetControl().getConfig().getAttributes());
		if (!fName.equals(fConnection.getName())) {
			fConnection.setName(fName);
		}
		((RemoteToolsConnectionManager) fConnection.getRemoteServices().getConnectionManager()).addConnection(fConnection);
		return fConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		getTargetControl().getConfig().setConnectionAddress(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		getTargetControl().getConfig().setAttribute(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnection#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		fName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		getTargetControl().getConfig().setLoginPassword(password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPort(int)
	 */
	public void setPort(int port) {
		getTargetControl().getConfig().setConnectionPort(port);
	}

	public void setTimeout(int timeout) {
		getTargetControl().getConfig().setConnectionTimeout(timeout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setUsername(java.lang.String)
	 */
	public void setUsername(String userName) {
		getTargetControl().getConfig().setLoginUsername(userName);
	}
}
