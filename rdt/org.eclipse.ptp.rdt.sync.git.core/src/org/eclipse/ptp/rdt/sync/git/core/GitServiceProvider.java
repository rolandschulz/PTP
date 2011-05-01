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
package org.eclipse.ptp.rdt.sync.git.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.services.core.ServiceProvider;

public class GitServiceProvider extends ServiceProvider implements ISyncServiceProvider {
	public static final String ID = "org.eclipse.ptp.rdt.sync.git.core.GitServiceProvider"; //$NON-NLS-1$

	private static final String GIT_LOCATION = "location"; //$NON-NLS-1$

	private static final String GIT_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String GIT_SERVICES_ID = "servicesId"; //$NON-NLS-1$
	private static final String GIT_PROJECT_NAME = "projectName"; //$NON-NLS-1$
	private IProject fProject = null;
	private String fLocation = null;
	private IRemoteConnection fConnection = null;
	private GitRemoteSyncConnection fSyncConnection = null;
	
	private final ReentrantLock syncLock = new ReentrantLock();
	private Integer syncTaskId = -1;  //ID for most recent synchronization task, functions as a time-stamp 
	private int finishedSyncTaskId = -1; //all synchronizations up to this ID (including it) have finished

	/**
	 * Get the remote directory that will be used for synchronization
	 * 
	 * @return path
	 */
	public String getLocation() {
		if (fLocation == null) {
			fLocation = getString(GIT_LOCATION, null);
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
			final String name = getString(GIT_PROJECT_NAME, null);
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
			final String name = getString(GIT_CONNECTION_NAME, null);
			if (name != null) {
				final IRemoteServices services = getRemoteServices();
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
		final String id = getString(GIT_SERVICES_ID, null);
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
	public boolean isConfigured() {
		return getLocation() != null && getRemoteConnection() != null && getProject() != null;
	}

	/**
	 * Set the remote directory that will be used for synchronization
	 * 
	 * @param location
	 *            directory path
	 * @throws RuntimeException if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setLocation(String location) {
		if (fLocation != null) {
			throw new RuntimeException("Changing of remote location is not supported."); //$NON-NLS-1$
		}
		fLocation = location;
		putString(GIT_LOCATION, location);
	}

	/**
	 * Set the project that will be synchronized
	 * 
	 * @param project
	 *            project to synchronize
	 */
	public void setProject(IProject project) {
		if (fProject != null) {
			throw new RuntimeException("Changing of project is not supported."); //$NON-NLS-1$
		}
		fProject = project;
		putString(GIT_PROJECT_NAME, project.getName());
	}

	/**
	 * set the remote connection used for synchronization
	 * 
	 * @param conn
	 *            remote connection
	 * @throws RuntimeException if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setRemoteConnection(IRemoteConnection conn) {
		if (fConnection != null) {
			throw new RuntimeException("Changing of remote connection is not supported."); //$NON-NLS-1$
		}
		fConnection = conn;
		putString(GIT_CONNECTION_NAME, conn.getName());
	}

	/**
	 * Set the remote services used for the connection
	 * 
	 * @param services
	 *            remote services
	 * @throws RuntimeException if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setRemoteServices(IRemoteServices services) {
		putString(GIT_SERVICES_ID, services.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#
	 * synchronize(org.eclipse.core.resources.IResourceDelta, org.eclipse.core.runtime.IProgressMonitor, boolean)
	 * TODO: use the force
	 */
	public void synchronize(IResourceDelta delta, IProgressMonitor monitor, EnumSet<SyncFlag> syncFlags) throws CoreException {
		
		// TODO: Note that here SyncFlag.FORCE is interpreted as sync always, even if not needed otherwise. This is different
		// from the original intent of FORCE, which was to do an immediate, blocking sync. We may need to split those two
		// functions and introduce more flags.
		// TODO: Also, note that we are not using the individual "sync to local" and "sync to remote" flags yet.
		if ((syncFlags == SyncFlag.NO_FORCE) && (!(syncNeeded(delta)))) {
			return;
		}
		
		int mySyncTaskId;
		synchronized (syncTaskId) {
			syncTaskId++;    
			mySyncTaskId=syncTaskId;
			//suggestion for Deltas: add delta to list of deltas
		}
		
		if (syncLock.hasQueuedThreads() && syncFlags == SyncFlag.NO_FORCE)
			return;   //the queued Thread will do the work for us. And we don't have to wait because of NO_FORCE
		
		syncLock.lock();
		try {
			if (mySyncTaskId<=finishedSyncTaskId)  //some other thread has already done the work for us 
				return;

			
			// TODO: Use delta information
			// switch (delta.getKind()) {
			// case IResourceDelta.ADDED:
			// System.out.println("ensureSync kind=ADDED");
			// break;
			// case IResourceDelta.REMOVED:
			// System.out.println("ensureSync kind=REMOVED");
			// break;
			// case IResourceDelta.CHANGED:
			// System.out.println("ensureSync kind=CHANGED");
			// break;
			// default:
			// System.out.println("ensureSync kind=OTHER");
			// }
			// for (IResourceDelta child : delta.getAffectedChildren()) {
			// IResource resource = child.getResource();
			// if (resource instanceof IProject) {
			// System.out.println("ensureSync project=" + child.getResource().getName());
			// synchronize(child, monitor,
			// force);
			// } else if (resource instanceof IFolder) {
			// System.out.println("ensureSync folder=" +
			// child.getResource().getName());
			// synchronize(child, monitor, force);
			// } else if (resource instanceof IFile) {
			// System.out.println("ensureSync file=" + child.getResource().getName());
			// }
			// }

			// TODO: Review exception handling
			if (fSyncConnection == null) {
				// Open a remote sync connection
				try {
					fSyncConnection = new GitRemoteSyncConnection(this.getRemoteConnection(),
							this.getProject().getLocation().toString(),	this.getLocation());
				} catch (final RemoteSyncException e) {
					throw e;
				}
			}

			// Open remote connection if necessary
			if (this.getRemoteConnection().isOpen() == false) {
				try {
					this.getRemoteConnection().open(monitor);
				} catch (final RemoteConnectionException e) {
					throw new RemoteSyncException(e);
				}
			}
			
			int willFinishTaskId;
			synchronized (syncTaskId) {
				willFinishTaskId = syncTaskId;  //This synchronization operation will include all tasks up to current syncTaskId 
			                                    //syncTaskId can be larger than mySyncTaskId (than we do also the work for other threads)
												//we might synchronize even more than that if a file is already saved but syncTaskId wasn't increased yet
												//thus we cannot guarantee a maximum but we can guarantee syncTaskId as a minimum
				//suggestion for Deltas: make local copy of list of deltas, remove list of deltas
			}

			// Sync local and remote. For now, do both ways each time.
			// TODO: Sync more efficiently and appropriately to the situation.
			try {
				fSyncConnection.syncLocalToRemote();
			} catch (final RemoteSyncException e) {
				throw e;
			}

			try {
				fSyncConnection.syncRemoteToLocal();
			} catch (final RemoteSyncException e) {
				throw e;
			}
			finishedSyncTaskId = willFinishTaskId;

		} finally {
			syncLock.unlock();
		}
		IProject project = this.getProject();
		if (project != null) {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}
	
	// Are any of the changes in delta relevant for sync'ing?
	private boolean syncNeeded(IResourceDelta delta) {
		String[] relevantChangedResources = getRelevantChangedResources(delta);
		if (relevantChangedResources.length == 0) {
			return false;
		}
		return true;
	}
	
	// This function and the next recursively compile a list of relevant resources that have changed.
	private String[] getRelevantChangedResources(IResourceDelta delta) {
		ArrayList<String> res = new ArrayList<String>();
		getRelevantChangedResourcesRecursive(delta, res);
		return res.toArray(new String[0]);
	}
	
	private void getRelevantChangedResourcesRecursive(IResourceDelta delta, ArrayList<String> res) {
		// Prune recursion if this is a directory or file of no interest (such as the ".git" directory)
		if (irrelevantPath(delta)) {
			return;
		}
		
		// Recursion logic
		IResourceDelta[] resChildren = delta.getAffectedChildren();
		if (resChildren.length == 0) {
			res.add(delta.getFullPath().toString());
			return;
		} else {
			for (IResourceDelta resChild : resChildren) {
				getRelevantChangedResourcesRecursive(resChild, res);
			}
		}
	}
	
	// Paths that the Git sync provider can ignore. For now, only the ".git" directory is excluded. This function should expand
	// later to include other paths, such as those in Git's ignore list.
	private boolean irrelevantPath(IResourceDelta delta) {
		String path = delta.getFullPath().toString();
		if (path.endsWith("/.git")) { //$NON-NLS-1$
			return true;
		} else if (path.endsWith("/.git/")){ //$NON-NLS-1$
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return fConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getConfigLocation()
	 */
	public String getConfigLocation() {
		return fLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#setRemoteToolsConnection()
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection) {
		syncLock.lock();
		try {
			fConnection = connection;
			putString(GIT_CONNECTION_NAME, connection.getName());
			fSyncConnection = null;  //get reinitialized by next synchronize call
		} finally {
			syncLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#setConfigLocation()
	 */
	public void setConfigLocation(String configLocation) {
		syncLock.lock();
		try {
			fLocation = configLocation;
			putString(GIT_LOCATION, configLocation);
			fSyncConnection = null;  //get reinitialized by next synchronize call
		} finally {
			syncLock.unlock();
		}
	}
}
