/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.core;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.remote.core.IRemoteResource;

public class SynchronizedResource implements IRemoteResource {
	private IResource fResource;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#getDefaultLocationURI(org.eclipse.core.resources.IResource)
	 */
	@Override
	public URI getActiveLocationURI() {
		try {
			return SyncConfigManager.getActiveSyncLocationURI(fResource);
		} catch (CoreException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#refresh(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void refresh(IProgressMonitor monitor) throws CoreException {
		SyncManager.syncBlocking(null, fResource.getProject(), SyncFlag.FORCE, monitor, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#getResource()
	 */
	@Override
	public IResource getResource() {
		return fResource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteResource#setResource(org.eclipse.core.resources.IResource)
	 */
	@Override
	public void setResource(IResource resource) {
		fResource = resource;
	}
}