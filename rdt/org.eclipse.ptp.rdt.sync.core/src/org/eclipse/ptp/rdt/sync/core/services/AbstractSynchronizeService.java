/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * @since 3.0
 */
public abstract class AbstractSynchronizeService implements ISynchronizeService {

	private final ISynchronizeServiceDescriptor fDescriptor;

	private String fLocation;
	private IRemoteConnection fConnection;

	public AbstractSynchronizeService(ISynchronizeServiceDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	@Override
	public String getId() {
		return fDescriptor.getId();
	}

	/**
	 * Get the remote directory that will be used for synchronization
	 * 
	 * @return path
	 */
	@Override
	public String getLocation() {
		return fLocation;
	}

	@Override
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#getRemoteConnection()
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public IRemoteConnection getRemoteConnection() {
		return fConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor#getService()
	 */
	@Override
	public ISynchronizeService getService() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#setLocation(java.lang.String)
	 */
	@Override
	public void setLocation(String location) {
		if (fLocation != null) {
			throw new RuntimeException(Messages.AbstractSynchronizeService_Change_remote_location);
		}
		fLocation = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#setRemoteConnection(org.eclipse.remote.core.IRemoteConnection)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void setRemoteConnection(IRemoteConnection conn) {
		if (fConnection != null) {
			throw new RuntimeException(Messages.AbstractSynchronizeService_Change_remote_connection);
		}
		fConnection = conn;
	}

}
