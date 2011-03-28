/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.core;

/**
 * @since 5.0
 */
public abstract class AbstractRemoteServices implements IRemoteServices {

	protected final IRemoteServicesDescriptor fDescriptor;

	public AbstractRemoteServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#canCreateConnections
	 * ()
	 */
	public boolean canCreateConnections() {
		return fDescriptor.canCreateConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getId()
	 */
	public String getId() {
		return fDescriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getName()
	 */
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	public String getScheme() {
		return fDescriptor.getScheme();
	}
}