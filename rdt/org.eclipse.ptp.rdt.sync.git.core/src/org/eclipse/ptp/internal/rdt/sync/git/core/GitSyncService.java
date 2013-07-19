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
package org.eclipse.ptp.internal.rdt.sync.git.core;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.ptp.internal.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteLocation;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncMergeConflictException;
import org.eclipse.ptp.rdt.sync.core.services.AbstractSynchronizeService;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor;

public class GitSyncService extends AbstractSynchronizeService {
	public static final String gitDir = ".ptp-sync"; //$NON-NLS-1$
	// Name of file placed in empty directories to force sync'ing of those directories.
	public static final String emptyDirectoryFileName = ".ptp-sync-folder"; //$NON-NLS-1$
	public static final String commitMessage = Messages.GRSC_CommitMessage;
	public static final String remotePushBranch = "ptp-push"; //$NON-NLS-1$

	// Implement storage of local JGit repositories and Git repositories.
	private static final Map<IProject, JGitRepo> projectToJGitRepoMap = new HashMap<IProject, JGitRepo>();
	private static final Map<RemoteLocation, GitRepo> remoteLocationToGitRepoMap = new HashMap<RemoteLocation, GitRepo>();

	// Boilerplate class for IProject and RemoteLocation Pair.
	// Why doesn't Java have a pair class?
	private class ProjectAndRemotePair {
		IProject project;
		RemoteLocation remoteLoc;

		/**
		 * Create new pair
		 * @param p
		 * 			project
		 * @param rl
		 * 			remote location
		 */
		public ProjectAndRemotePair(IProject p, RemoteLocation rl) {
			project = p;
			remoteLoc = rl;
		}

		/**
		 * Get project
		 * @return project
		 */
		public IProject getProject() {
			return project;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((project == null) ? 0 : project.hashCode());
			result = prime * result
					+ ((remoteLoc == null) ? 0 : remoteLoc.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProjectAndRemotePair other = (ProjectAndRemotePair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (project == null) {
				if (other.project != null)
					return false;
			} else if (!project.equals(other.project))
				return false;
			if (remoteLoc == null) {
				if (other.remoteLoc != null)
					return false;
			} else if (!remoteLoc.equals(other.remoteLoc))
				return false;
			return true;
		}
		private GitSyncService getOuterType() {
			return GitSyncService.this;
		}
	}
	// Entry indicates that the remote location has a clean (up-to-date) file filter for the project
	private static final Set<ProjectAndRemotePair> cleanFileFilterMap = new HashSet<ProjectAndRemotePair>();

	/**
	 * Get JGit repository instance for the given project, creating it if necessary.
	 * @param project
	 *
	 * @return JGit repository instance - never null
	 * @throws RemoteSyncException
	 * 				on problems creating the repository
	 */
	public static JGitRepo getLocalJGitRepo(IProject project, IProgressMonitor monitor) throws RemoteSyncException {
		JGitRepo repo = projectToJGitRepoMap.get(project);
		try {
			if (repo == null) {
				try {
					repo = new JGitRepo(project, monitor);
					projectToJGitRepoMap.put(project, repo);
				} catch (GitAPIException e) {
					throw new RemoteSyncException(e);
				} catch (IOException e) {
					throw new RemoteSyncException(e);
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
		return repo;
	}

	/**
	 * Get Git repository instance for given remote location, creating it if necessary.
	 *
	 * @param project
	 * 			For initializing remote repository if necessary - cannot be null.
	 * @param rl
	 * 			remote location - cannot be null
	 * @param monitor
	 *
	 * @return Git repo instance - is null only if connection could not be resolved.
	 * @throws RemoteSyncException
	 * 			on problems creating the repository
	 */
	public static GitRepo getGitRepo(IProject project, RemoteLocation rl, IProgressMonitor monitor) throws RemoteSyncException {
		if (project == null || rl == null) {
			throw new NullPointerException();
		}
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		GitRepo repo = remoteLocationToGitRepoMap.get(rl);
		try {
			if (repo == null) {
				subMon.subTask("Accessing local JGit repository");
				JGitRepo localRepo = getLocalJGitRepo(project, subMon.newChild(25));
				try {
					subMon.subTask("Creating Git repository");
					repo = new GitRepo(localRepo, rl, subMon.newChild(75));
					remoteLocationToGitRepoMap.put(rl, repo);
				} catch (MissingConnectionException e) {
					return null;
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
		return repo;
	}

	private static final ReentrantLock syncLock = new ReentrantLock();
	private Integer fWaitingThreadsCount = 0;
	private Integer syncTaskId = -1; // ID for most recent synchronization task, functions as a time-stamp
	private int finishedSyncTaskId = -1; // all synchronizations up to this ID (including it) have finished

	private static boolean consCalled = false;
	/**
	 * Create a new instance of the GitSyncService
	 * @param descriptor
	 * 				service descriptor
	 */
	public GitSyncService(ISynchronizeServiceDescriptor descriptor) {
		super(descriptor);
		// Constructor for each sync service should only be called once by design of synchronized projects
		// See bug 410106
		assert(!consCalled) : Messages.GitSyncService_1;
		consCalled = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#checkout(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IPath[])
	 */
	@Override
	public void checkout(IProject project, IPath[] paths) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo != null) {
			try {
				repo.checkout(paths);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#checkoutRemoteCopy(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IPath[])
	 */
	@Override
	public void checkoutRemoteCopy(IProject project, IPath[] paths) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo != null) {
			try {
				repo.checkoutRemoteCopy(paths);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#close(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void close(IProject project) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo != null) {
			repo.close();
		}
	}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#getMergeConflictFiles(org.eclipse.core.resources.IProject)
		 */
	@Override
	public Set<IPath> getMergeConflictFiles(IProject project) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo == null) {
			return new HashSet<IPath>();
		} else {
			try {
				return repo.getMergeConflictFiles();
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			} catch (IOException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#getMergeConflictParts(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.resources.IFile)
	 */
	@Override
	public String[] getMergeConflictParts(IProject project, IFile file) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo == null) {
			return null;
		} else {
			try {
				return repo.getMergeConflictParts(file);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			} catch (IOException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#setMergeAsResolved(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IPath[])
	 */
	@Override
	public void setMergeAsResolved(IProject project, IPath[] paths) throws RemoteSyncException {
		JGitRepo repo = getLocalJGitRepo(project, null);
		if (repo != null) {
			try {
				repo.setMergeAsResolved(paths);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#synchronize(org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.rdt.sync.core.RemoteLocation, org.eclipse.core.resources.IResourceDelta,
	 * org.eclipse.core.runtime.IProgressMonitor, java.util.EnumSet)
	 */
	@Override
	public void synchronize(final IProject project, RemoteLocation remoteLoc, IResourceDelta delta, IProgressMonitor monitor,
			EnumSet<SyncFlag> syncFlags) throws CoreException {
		if (project == null || remoteLoc == null) {
			throw new NullPointerException();
		}
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 1000);

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
				subMon.subTask(Messages.GitServiceProvider_7);
				JGitRepo localRepo = getLocalJGitRepo(project, null);
				GitRepo remoteRepo = getGitRepo(project, remoteLoc, null);
				// Unresolved connection - abort
				if (remoteRepo == null) {
					return;
				}

				// Do not sync if there are merge conflicts.
				Set<IPath> conflictedFiles;
				try {
					conflictedFiles = localRepo.getMergeConflictFiles();
				} catch (GitAPIException e) {
					throw new RemoteSyncException(e);
				} catch (IOException e) {
					throw new RemoteSyncException(e);
				}
				if (!(conflictedFiles.isEmpty())) {
					throw new RemoteSyncMergeConflictException(Messages.GitServiceProvider_4);
				}

				if (mySyncTaskId <= finishedSyncTaskId) { // some other thread has already done the work for us
					return;
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
					doSync(localRepo, remoteRepo, syncFlags, subMon.newChild(900));
				} catch (RemoteSyncMergeConflictException e) {
					subMon.subTask(Messages.GitServiceProvider_9);
					// Refresh after merge conflict since conflicted files are altered with markup.
					project.refreshLocal(IResource.DEPTH_INFINITE, subMon.newChild(1));
					throw e;
				}
				finishedSyncTaskId = willFinishTaskId;
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

	/**
	 * Synchronize the given local and remote repositories. Currently both directions are always synchronized.
	 * Note that the remote is fetched and merged first. This is on purpose so that merge conflicts will occur locally, where
	 * they can be more easily managed.
	 * 
	 * @param localRepo
	 * 				A local JGit repository
	 * @param remoteRepo
	 * 				A remote Git repository
	 * @param monitor
	 * @param includeUntrackedFiles
	 *            Should currently untracked remote files be added to the repository?
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. All exceptions are wrapped in a RemoteSyncException and thrown, so that clients
	 *             can always detect when a sync fails and why.
	 */
	private void doSync(JGitRepo localRepo, GitRepo remoteRepo, EnumSet<SyncFlag> syncFlags, IProgressMonitor monitor) throws RemoteSyncException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			// Commit local and remote changes
			subMon.subTask(Messages.GitRemoteSyncConnection_12);
			boolean hasChanges = localRepo.commit(subMon.newChild(5));
			if ((!hasChanges) && (syncFlags == SyncFlag.NO_FORCE)) {
				return;
			}
			subMon.subTask(Messages.GitRemoteSyncConnection_14);
			ProjectAndRemotePair parp = new ProjectAndRemotePair(localRepo.getProject(), remoteRepo.getRemoteLocation());
			if (!cleanFileFilterMap.contains(parp)) {
				remoteRepo.uploadFilter(localRepo, subMon.newChild(9));
				cleanFileFilterMap.add(parp);
			}
			remoteRepo.commitRemoteFiles(subMon.newChild(18));

			try {
				// Fetch the remote repository
				subMon.subTask(Messages.GitRemoteSyncConnection_15);
				localRepo.fetch(remoteRepo.getRemoteLocation(), subMon.newChild(18));

				// Merge it with local
				localRepo.merge(subMon.newChild(2));

				// Handle merge conflict. Read in data needed to resolve the conflict, and then reset the repo.
				if (localRepo.readMergeConflictFiles()) {
					throw new RemoteSyncMergeConflictException(Messages.GitRemoteSyncConnection_2);
					// Even if we later decide not to throw an exception, it is important not to proceed after a merge conflict.
					// return;
				}
			} catch (TransportException e) {
				if (e.getMessage().startsWith("Remote does not have ")) { //$NON-NLS-1$
					// Means that the remote branch isn't set up yet (and thus nothing to fetch). Can be ignored and local to
					// remote sync can proceed.
					// Note: It is important, though, that we do not merge if fetch fails. Merge will fail because remote ref is
					// not created.
				} else {
					throw new RemoteSyncException(e);
				}
			} finally {
				subMon.setWorkRemaining(100 - 64);
			}

			// Push local repository to remote
			if (localRepo.getGit().branchList().call().size() > 0) { // check whether master was already created
				subMon.subTask(Messages.GitRemoteSyncConnection_17);
				localRepo.push(remoteRepo.getRemoteLocation(), subMon.newChild(18));
				remoteRepo.merge(subMon.newChild(18));
			}
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		} catch (MissingConnectionException e) {
			// nothing to do
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#getSyncFileFilter(org.eclipse.core.resources.IProject)
	 */
	@Override
	public AbstractSyncFileFilter getSyncFileFilter(IProject project) throws RemoteSyncException {
		return getLocalJGitRepo(project, null).getFilter();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#setSyncFileFilter(org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter)
	 */
	@Override
	public void setSyncFileFilter(IProject project, AbstractSyncFileFilter filter) throws RemoteSyncException {
		Iterator<ProjectAndRemotePair> it = cleanFileFilterMap.iterator();
		while (it.hasNext()) {
			ProjectAndRemotePair parp = it.next();
			if (parp.getProject() == project) {
				it.remove();
			}
		}
		getLocalJGitRepo(project, null).setFilter(filter);
	}
}
