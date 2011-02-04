/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.rsync.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.ServiceProvider;

public class RSyncServiceProvider extends ServiceProvider implements ISyncServiceProvider {
	public static final String ID = "org.eclipse.ptp.rdt.sync.rsync.core.RSyncServiceProvider"; //$NON-NLS-1$

	private static final String RSYNC_LOCATION = "location"; //$NON-NLS-1$

	private static final String RSYNC_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String RSYNC_SERVICES_ID = "servicesId"; //$NON-NLS-1$
	private static final String RSYNC_PROJECT_NAME = "projectName"; //$NON-NLS-1$
	private IProject fProject = null;

	private String fLocation = null;
	private IRemoteConnection fConnection = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#
	 * ensureSync(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void ensureSync(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			System.out.println("ensureSync kind=ADDED");
			break;
		case IResourceDelta.REMOVED:
			System.out.println("ensureSync kind=REMOVED");
			break;
		case IResourceDelta.CHANGED:
			System.out.println("ensureSync kind=CHANGED");
			break;
		default:
			System.out.println("ensureSync kind=OTHER");
		}
		for (IResourceDelta child : delta.getAffectedChildren()) {
			IResource resource = child.getResource();
			if (resource instanceof IProject) {
				System.out.println("ensureSync project=" + child.getResource().getName());
				ensureSync(child, monitor);
			} else if (resource instanceof IFolder) {
				System.out.println("ensureSync folder=" + child.getResource().getName());
				ensureSync(child, monitor);
			} else if (resource instanceof IFile) {
				System.out.println("ensureSync file=" + child.getResource().getName());
			}
		}
	}

	/**
	 * Get the remote directory that will be used for synchronization
	 * 
	 * @return path
	 */
	public String getLocation() {
		if (fLocation == null) {
			fLocation = getString(RSYNC_LOCATION, null);
		}
		return fLocation;
	}

	/**
	 * Get the project to be synchronized
	 * 
	 * @return project
	 */
	public IProject getProject() {
		if (fProject == null) {
			String name = getString(RSYNC_PROJECT_NAME, null);
			if (name != null) {
				fProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			}
		}
		return fProject;
	}

	/**
	 * Get the remote connection used for synchronization
	 * 
	 * @return remote connection
	 */
	public IRemoteConnection getRemoteConnection() {
		if (fConnection == null) {
			String name = getString(RSYNC_CONNECTION_NAME, null);
			if (name != null) {
				IRemoteServices services = getRemoteServices();
				if (services != null) {
					fConnection = services.getConnectionManager().getConnection(name);
				}
			}
		}
		return fConnection;
	}

	/**
	 * Get the remote services used for the connection
	 * 
	 * @return remote services
	 */
	public IRemoteServices getRemoteServices() {
		String id = getString(RSYNC_SERVICES_ID, null);
		if (id != null) {
			return PTPRemoteCorePlugin.getDefault().getRemoteServices(id);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return getLocation() != null && getRemoteConnection() != null && getProject() != null;
	}

	/**
	 * Set the remote directory that will be used for synchronization
	 * 
	 * @param location
	 *            directory path
	 */
	public void setLocation(String location) {
		fLocation = location;
		putString(RSYNC_LOCATION, location);
	}

	/**
	 * Set the project that will be synchronized
	 * 
	 * @param project
	 *            project to synchronize
	 */
	public void setProject(IProject project) {
		fProject = project;
		putString(RSYNC_PROJECT_NAME, project.getName());
	}

	/**
	 * set the remote connection used for synchronization
	 * 
	 * @param conn
	 *            remote connection
	 */
	public void setRemoteConnection(IRemoteConnection conn) {
		fConnection = conn;
		putString(RSYNC_CONNECTION_NAME, conn.getName());
	}

	/**
	 * Set the remote services used for the connection
	 * 
	 * @param services
	 *            remote services
	 */
	public void setRemoteServices(IRemoteServices services) {
		putString(RSYNC_SERVICES_ID, services.getId());
	}

}
