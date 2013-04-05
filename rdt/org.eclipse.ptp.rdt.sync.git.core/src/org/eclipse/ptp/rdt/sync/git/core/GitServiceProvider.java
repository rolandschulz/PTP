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

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncMergeConflictException;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.services.core.ServiceProvider;

public class GitServiceProvider extends ServiceProvider implements ISyncServiceProvider {
	public static final String ID = "org.eclipse.ptp.rdt.sync.git.core.GitServiceProvider"; //$NON-NLS-1$

	private static final String GIT_LOCATION = "location"; //$NON-NLS-1$

	private static final String GIT_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String GIT_SERVICES_ID = "servicesId"; //$NON-NLS-1$
	private String fLocation = null;
	private IRemoteConnection fConnection = null;
	private boolean hasBeenSynced = false;

	private static final ReentrantLock syncLock = new ReentrantLock();
	private Integer fWaitingThreadsCount = 0;
	private Integer syncTaskId = -1; // ID for most recent synchronization task, functions as a time-stamp
	private int finishedSyncTaskId = -1; // all synchronizations up to this ID (including it) have finished

	// Simple pair class for bundling a project and build scenario.
	// Since we use this as a key, equality testing is important.
	// Note that we use the project location in equality testing, as this can change even though the project object stays the same.
	private static class ProjectAndScenario {
		private final IProject project;
		private final BuildScenario scenario;
		private final String projectLocation;

		ProjectAndScenario(IProject p, BuildScenario bs) {
			project = p;
			scenario = bs;
			projectLocation = p.getLocation().toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((project == null) ? 0 : project.hashCode());
			result = prime * result + ((projectLocation == null) ? 0 : projectLocation.hashCode());
			result = prime * result + ((scenario == null) ? 0 : scenario.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ProjectAndScenario other = (ProjectAndScenario) obj;
			if (project == null) {
				if (other.project != null) {
					return false;
				}
			} else if (!project.equals(other.project)) {
				return false;
			}
			if (projectLocation == null) {
				if (other.projectLocation != null) {
					return false;
				}
			} else if (!projectLocation.equals(other.projectLocation)) {
				return false;
			}
			if (scenario == null) {
				if (other.scenario != null) {
					return false;
				}
			} else if (!scenario.equals(other.scenario)) {
				return false;
			}
			return true;
		}

	}

	private final Map<ProjectAndScenario, GitRemoteSyncConnection> syncConnectionMap = Collections
			.synchronizedMap(new HashMap<ProjectAndScenario, GitRemoteSyncConnection>());

	/**
	 * Get the remote directory that will be used for synchronization
	 * 
	 * @return path
	 */
	@Override
	public String getLocation() {
		if (fLocation == null) {
			fLocation = getString(GIT_LOCATION, null);
		}
		return fLocation;
	}

	/**
	 * Get the remote connection used for synchronization
	 * 
	 * @return remote connection
	 */
	@Override
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
	@Override
	public IRemoteServices getRemoteServices() {
		final String id = getString(GIT_SERVICES_ID, null);
		if (id != null) {
			return RemoteServices.getRemoteServices(id);
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
		return getLocation() != null && getRemoteConnection() != null;
	}

	/**
	 * Set the remote directory that will be used for synchronization
	 * 
	 * @param location
	 *            directory path
	 * @throws RuntimeException
	 *             if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setLocation(String location) {
		if (fLocation != null) {
			throw new RuntimeException(Messages.GSP_ChangeLocationError);
		}
		fLocation = location;
		putString(GIT_LOCATION, location);
	}

	/**
	 * set the remote connection used for synchronization
	 * 
	 * @param conn
	 *            remote connection
	 * @throws RuntimeException
	 *             if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setRemoteConnection(IRemoteConnection conn) {
		if (fConnection != null) {
			throw new RuntimeException(Messages.GSP_ChangeConnectionError);
		}
		fConnection = conn;
		putString(GIT_CONNECTION_NAME, conn.getName());
	}

	/**
	 * Set the remote services used for the connection
	 * 
	 * @param services
	 *            remote services
	 * @throws RuntimeException
	 *             if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setRemoteServices(IRemoteServices services) {
		putString(GIT_SERVICES_ID, services.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#synchronize(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.resources.IResourceDelta, org.eclipse.ptp.rdt.sync.core.SyncFileFilter,
	 * org.eclipse.core.runtime.IProgressMonitor, java.util.EnumSet)
	 */
	@Override
	public void synchronize(final IProject project, BuildScenario buildScenario, IResourceDelta delta, SyncFileFilter fileFilter,
			IProgressMonitor monitor, EnumSet<SyncFlag> syncFlags) throws CoreException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 1000);

		// On first sync, place .gitignore in directories. This is useful for folders that are already present and thus are never
		// captured by a resource add or change event. (This can happen for projects converted to sync projects.)
		if (!hasBeenSynced) {
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (irrelevantPath(project, resource)) {
						return false;
					}
					if (resource.getType() == IResource.FOLDER) {
						IFile emptyFile = project.getFile(resource.getProjectRelativePath().addTrailingSeparator() + ".gitignore"); //$NON-NLS-1$
						try {
							if (!(emptyFile.exists())) {
								emptyFile.create(new ByteArrayInputStream("".getBytes()), false, null); //$NON-NLS-1$
							}
						} catch (CoreException e) {
							// Nothing to do. Can happen if another thread creates the file between the check and creation.
						}
					}
					return true;
				}
			});
		}
		hasBeenSynced = true;

		// Make a visitor that explores the delta. At the moment, this visitor is responsible for two tasks (the list may grow in
		// the future):
		// 1) Find out if there are any "relevant" resource changes (changes that need to be mirrored remotely)
		// 2) Add an empty ".gitignore" file to new directories so that Git will sync them
		class SyncResourceDeltaVisitor implements IResourceDeltaVisitor {
			private boolean relevantChangeFound = false;

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				if (irrelevantPath(project, delta.getResource())) {
					return false;
				} else {
					if ((delta.getAffectedChildren().length == 0) && (delta.getFlags() != IResourceDelta.MARKERS)) {
						relevantChangeFound = true;
					}
				}

				// Add .gitignore to empty directories
				if (delta.getResource().getType() == IResource.FOLDER
						&& (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)) {
					IFile emptyFile = project.getFile(delta.getResource().getProjectRelativePath().addTrailingSeparator()
							+ ".gitignore"); //$NON-NLS-1$
					try {
						if (!(emptyFile.exists())) {
							emptyFile.create(new ByteArrayInputStream("".getBytes()), false, null); //$NON-NLS-1$
						}
					} catch (CoreException e) {
						// Nothing to do. Can happen if another thread creates the file between the check and creation.
					}
				}

				return true;
			}

			public boolean isRelevant() {
				return relevantChangeFound;
			}
		}

		// Explore delta only if it is not null
		boolean hasRelevantChangedResources = false;
		if (delta != null) {
			SyncResourceDeltaVisitor visitor = new SyncResourceDeltaVisitor();
			delta.accept(visitor);
			hasRelevantChangedResources = visitor.isRelevant();
		}

		try {
			/*
			 * A synchronize with SyncFlag.FORCE guarantees that both directories are in sync.
			 * 
			 * More precise: it guarantees that all changes written to disk at the moment of the call are guaranteed to be
			 * synchronized between both directories. No guarantees are given for changes occurring during the synchronize call.
			 * 
			 * To satisfy this guarantee, this call needs to make sure that both the current delta and all outstanding sync requests
			 * finish before this call returns.
			 * 
			 * Example: Why sync if current delta is empty? The RemoteMakeBuilder forces a sync before and after building. In some
			 * cases, we want to ensure repos are synchronized regardless of the passed delta, which can be set to null.
			 */
			// TODO: We are not using the individual "sync to local" and "sync to remote" flags yet.
			if (syncFlags.contains(SyncFlag.DISABLE_SYNC)) {
				return;
			}
			if ((syncFlags == SyncFlag.NO_FORCE) && (!(hasRelevantChangedResources))) {
				return;
			}

			int mySyncTaskId;
			synchronized (syncTaskId) {
				syncTaskId++;
				mySyncTaskId = syncTaskId;
				// suggestion for Deltas: add delta to list of deltas
			}

			synchronized (fWaitingThreadsCount) {
				if (fWaitingThreadsCount > 0 && syncFlags == SyncFlag.NO_FORCE) {
					return; // the queued thread will do the work for us. And we don't have to wait because of NO_FORCE
				} else {
					fWaitingThreadsCount++;
				}
			}

			// lock syncLock. interruptible by progress monitor
			try {
				while (!syncLock.tryLock(50, TimeUnit.MILLISECONDS)) {
					if (subMon.isCanceled()) {
						throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.GitServiceProvider_1));
					}
				}
			} catch (InterruptedException e1) {
				throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.GitServiceProvider_2));
			} finally {
				synchronized (fWaitingThreadsCount) {
					fWaitingThreadsCount--;
				}
			}

			try {
				// Do not sync if there are merge conflicts.
				// This check must be done after acquiring the sync lock. Otherwise, the merge may trigger a sync that sees no
				// conflicting files and proceeds to sync again - depending on how quickly the first sync records the data.
				if (!(this.getMergeConflictFiles(project, buildScenario).isEmpty())) {
					throw new RemoteSyncMergeConflictException(Messages.GitServiceProvider_4);
				}

				if (mySyncTaskId <= finishedSyncTaskId) { // some other thread has already done the work for us
					return;
				}

				if (buildScenario == null) {
					throw new RuntimeException(Messages.GitServiceProvider_3 + project.getName());
				}

				subMon.subTask(Messages.GitServiceProvider_7);
				GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario, fileFilter,
						subMon.newChild(98));
				if (fSyncConnection == null) {
					// Should never happen
					if (buildScenario.getSyncProvider() == null) {
						throw new RemoteSyncException(Messages.GitServiceProvider_5);
						// Happens whenever connection does not exist
					} else {
						return;
					}
				}

				// This synchronization operation will include all tasks up to current syncTaskId
				// syncTaskId can be larger than mySyncTaskId (than we do also the work for other threads)
				// we might synchronize even more than that if a file is already saved but syncTaskId wasn't increased yet
				// thus we cannot guarantee a maximum but we can guarantee syncTaskId as a minimum
				// suggestion for Deltas: make local copy of list of deltas, remove list of deltas
				int willFinishTaskId;
				synchronized (syncTaskId) {
					willFinishTaskId = syncTaskId;
				}

				try {
					subMon.subTask(Messages.GitServiceProvider_8);
					fSyncConnection.sync(subMon.newChild(900), true);
					// Unlike other exceptions, we need to do some post-sync activities after a merge exception.
					// TODO: Refactor code to get rid of duplication of post-sync activities.
				} catch (RemoteSyncMergeConflictException e) {
					subMon.subTask(Messages.GitServiceProvider_9);
					project.refreshLocal(IResource.DEPTH_INFINITE, subMon.newChild(1));
					throw e;
				}
				finishedSyncTaskId = willFinishTaskId;
				// TODO: review exception handling
			} finally {
				syncLock.unlock();
			}

			// Sync successful - re-enable error messages. This is really UI code, but there is no way at the moment to notify UI
			// of a successful sync.
			SyncManager.setShowErrors(project, true);

			// Refresh after sync to display changes
			subMon.subTask(Messages.GitServiceProvider_10);
			project.refreshLocal(IResource.DEPTH_INFINITE, subMon.newChild(1));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	// Return appropriate sync connection or null for scenarios with no sync provider or if the connection is missing.
	// Creates a new sync connection if necessary. This function must properly maintain the map of connections and also remember
	// to set the file filter (always, not just for new connections).
	// TODO: Create progress monitor if passed monitor is null.
	private synchronized GitRemoteSyncConnection getSyncConnection(IProject project, BuildScenario buildScenario,
			SyncFileFilter fileFilter, IProgressMonitor monitor) throws RemoteSyncException {
		try {
			if (buildScenario.getSyncProvider() == null) {
				return null;
			}
			ProjectAndScenario pas = new ProjectAndScenario(project, buildScenario);
			if (!syncConnectionMap.containsKey(pas)) {
				try {
					GitRemoteSyncConnection grsc = new GitRemoteSyncConnection(project, project.getLocation().toString(),
							buildScenario, fileFilter, monitor);
					syncConnectionMap.put(pas, grsc);
				} catch (MissingConnectionException e) {
					return null;
				}
			}
			GitRemoteSyncConnection fSyncConnection = syncConnectionMap.get(pas);
			fSyncConnection.setFileFilter(fileFilter);
			return fSyncConnection;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#getMergeConflictFiles()
	 */
	@Override
	public Set<IPath> getMergeConflictFiles(IProject project, BuildScenario buildScenario) throws RemoteSyncException {
		GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario,
				SyncManager.getFileFilter(project), null);
		if (fSyncConnection == null) {
			return new HashSet<IPath>();
		} else {
			return fSyncConnection.getMergeConflictFiles();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#getMergeConflictParts(org.eclipse.core.resources.IFile)
	 */
	@Override
	public String[] getMergeConflictParts(IProject project, BuildScenario buildScenario, IFile file) throws RemoteSyncException {
		GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario,
				SyncManager.getFileFilter(project), null);
		if (fSyncConnection == null) {
			return null;
		} else {
			return fSyncConnection.getMergeConflictParts(file);
		}
	}

	// Paths that the Git sync provider can ignore.
	private boolean irrelevantPath(IProject project, IResource resource) {
		if (SyncManager.getFileFilter(project).shouldIgnore(resource)) {
			return true;
		}

		String path = resource.getFullPath().toString();
		if (path.endsWith("/" + GitRemoteSyncConnection.gitDir)) { //$NON-NLS-1$
			return true;
		} else if (path.endsWith("/.git")) { //$NON-NLS-1$
			return true;
		} else if (path.endsWith("/.settings")) { //$NON-NLS-1$
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
	@Override
	public IRemoteConnection getConnection() {
		return fConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getConfigLocation()
	 */
	@Override
	public String getConfigLocation() {
		return fLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#setRemoteToolsConnection()
	 */
	@Override
	public void setRemoteToolsConnection(IRemoteConnection connection) {
		fConnection = connection;
		putString(GIT_CONNECTION_NAME, connection.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#setConfigLocation()
	 */
	@Override
	public void setConfigLocation(String configLocation) {
		fLocation = configLocation;
		putString(GIT_LOCATION, configLocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#close(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void close(IProject project) {
		for (Map.Entry<ProjectAndScenario, GitRemoteSyncConnection> entry : syncConnectionMap.entrySet()) {
			if (entry.getKey().project == project) {
				entry.getValue().close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#setResolved(org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.rdt.sync.core.BuildScenario, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setMergeAsResolved(IProject project, BuildScenario buildScenario, IPath[] paths) throws RemoteSyncException {
		GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario,
				SyncManager.getFileFilter(project), null);
		if (fSyncConnection != null) {
			fSyncConnection.setMergeAsResolved(paths);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#checkout(org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.rdt.sync.core.BuildScenario, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void checkout(IProject project, BuildScenario buildScenario, IPath[] paths) throws RemoteSyncException {
		GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario,
				SyncManager.getFileFilter(project), null);
		if (fSyncConnection != null) {
			fSyncConnection.checkout(paths);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#checkoutRemote(org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.rdt.sync.core.BuildScenario, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void checkoutRemoteCopy(IProject project, BuildScenario buildScenario, IPath[] paths) throws RemoteSyncException {
		GitRemoteSyncConnection fSyncConnection = this.getSyncConnection(project, buildScenario,
				SyncManager.getFileFilter(project), null);
		if (fSyncConnection != null) {
			fSyncConnection.checkoutRemoteCopy(paths);
		}
	}
}
