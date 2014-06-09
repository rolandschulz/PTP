/*******************************************************************************
 * Copyright (c) 2011, 2014 Oak Ridge National Laboratory and others.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
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

/**
 * A Git-based synchronization service for synchronized projects
 */
public class GitSyncService extends AbstractSynchronizeService {
	public static final String gitDir = ".ptp-sync"; //$NON-NLS-1$
	// Name of file placed in empty directories to force sync'ing of those directories.
	public static final String emptyDirectoryFileName = ".ptp-sync-folder"; //$NON-NLS-1$
	public static final String commitMessage = Messages.GitSyncService_0;
	public static final String remotePushBranch = "ptp-push"; //$NON-NLS-1$

	// Implement storage of local JGit repositories and Git repositories.
	private static final Map<IPath, JGitRepo> localDirectoryToJGitRepoMap = new HashMap<IPath, JGitRepo>();
	private static final Map<RemoteLocation, GitRepo> remoteLocationToGitRepoMap = new HashMap<RemoteLocation, GitRepo>();

	// Variables for managing sync threads
	private static final ReentrantLock syncLock = new ReentrantLock();
	private static final ConcurrentMap<ProjectAndRemoteLocationPair, AtomicLong> syncLRPending =
			new ConcurrentHashMap<ProjectAndRemoteLocationPair, AtomicLong>();
	private static final ConcurrentMap<ProjectAndRemoteLocationPair, AtomicLong> syncRLPending =
			new ConcurrentHashMap<ProjectAndRemoteLocationPair, AtomicLong>();

	// Entry indicates that the remote location has a clean (up-to-date) file filter for the project
	private static final Set<LocalAndRemoteLocationPair> cleanFileFilterMap = new HashSet<LocalAndRemoteLocationPair>();
	// Entry indicates that the remote location contains the most recently committed local changes
	private static final Set<LocalAndRemoteLocationPair> localChangesPushed = new HashSet<LocalAndRemoteLocationPair>();

	// Boilerplate class for IPath and RemoteLocation Pair
	private class LocalAndRemoteLocationPair {
		IPath localDir;
		RemoteLocation remoteLoc;

		/**
		 * Create new pair
		 * @param ld
		 * 			local directory
		 * @param rl
		 * 			remote location
		 */
		public LocalAndRemoteLocationPair(IPath ld, RemoteLocation rl) {
			localDir = ld;
			remoteLoc = rl;
		}

		/**
		 * Get the local directory
		 * @return directory
		 */
		public IPath getLocal() {
			return localDir;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((localDir == null) ? 0 : localDir.hashCode());
			result = prime * result
					+ ((remoteLoc == null) ? 0 : remoteLoc.hashCode());
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
			LocalAndRemoteLocationPair other = (LocalAndRemoteLocationPair) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (localDir == null) {
				if (other.localDir != null) {
					return false;
				}
			} else if (!localDir.equals(other.localDir)) {
				return false;
			}
			if (remoteLoc == null) {
				if (other.remoteLoc != null) {
					return false;
				}
			} else if (!remoteLoc.equals(other.remoteLoc)) {
				return false;
			}
			return true;
		}

		private GitSyncService getOuterType() {
			return GitSyncService.this;
		}
	}

	// Boilerplate class for IProject and RemoteLocation Pair
	private class ProjectAndRemoteLocationPair {
		IProject project;
		RemoteLocation remoteLoc;

		/**
		 * Create new pair
		 * @param p
		 * 			project
		 * @param rl
		 * 			remote location
		 */
		public ProjectAndRemoteLocationPair(IProject p, RemoteLocation rl) {
			project = p;
			remoteLoc = rl;
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
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ProjectAndRemoteLocationPair other = (ProjectAndRemoteLocationPair) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (project == null) {
				if (other.project != null) {
					return false;
				}
			} else if (!project.equals(other.project)) {
				return false;
			}
			if (remoteLoc == null) {
				if (other.remoteLoc != null) {
					return false;
				}
			} else if (!remoteLoc.equals(other.remoteLoc)) {
				return false;
			}
			return true;
		}

		private GitSyncService getOuterType() {
			return GitSyncService.this;
		}
	}

	/**
	 * Get JGit repository instance for the given project, creating it if necessary.
	 * @param project - cannot be null
	 * @param monitor
	 *
	 * @return JGit repository instance - never null
	 * @throws RemoteSyncException
	 * 				on problems creating the repository
	 */
	static JGitRepo getLocalJGitRepo(IProject project, IProgressMonitor monitor) throws RemoteSyncException {
		IPath localDir = project.getLocation();
		if (localDir == null) {
			throw new RemoteSyncException(Messages.GitSyncService_17 + project.getName());
		}
		JGitRepo repo = localDirectoryToJGitRepoMap.get(localDir);
		try {
			if (repo == null) {
				try {
					repo = new JGitRepo(localDir, monitor);
					localDirectoryToJGitRepoMap.put(localDir, repo);
					setRepoFilesAsDerived(project);
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
	 * @param rl
	 * 			remote location - cannot be null
	 * @param monitor
	 *
	 * @return Git repo instance - is null only if connection could not be resolved.
	 * @throws RemoteSyncException
	 * 			on problems creating the repository
	 */
	static GitRepo getGitRepo(RemoteLocation rl, IProgressMonitor monitor) throws RemoteSyncException {
		if (rl == null) {
			throw new NullPointerException();
		}
		GitRepo repo = remoteLocationToGitRepoMap.get(rl);
		try {
			if (repo == null) {
				RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
				subMon.subTask(Messages.GitSyncService_1);
				try {
					subMon.subTask(Messages.GitSyncService_2);
					repo = new GitRepo(rl, subMon.newChild(90));
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
		assert(!consCalled) : Messages.GitSyncService_3;
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
				doRefresh(project);
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
				doRefresh(project);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider#close(
	 * org.eclipse.core.resources.IProject)
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
		 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService#getMergeConflictFiles(
		 * org.eclipse.core.resources.IProject)
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
				return repo.getMergeConflictParts(file.getProjectRelativePath());
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
	public void synchronize(final IProject project, RemoteLocation rl, IResourceDelta delta, IProgressMonitor monitor,
			Set<SyncFlag> syncFlags) throws CoreException {
		if (project == null || rl == null) {
			throw new NullPointerException();
		}
		RemoteLocation remoteLoc = new RemoteLocation(rl);
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);

		try {
			/*
			 * A synchronize with SyncFlag.BOTH guarantees that both directories are in sync.
			 * 
			 * More precise: it guarantees that all changes written to disk at the moment of the call are guaranteed to be
			 * synchronized between both directories. No guarantees are given for changes occurring during the synchronize call.
			 * 
			 * To satisfy this guarantee, this call needs to make sure that both the current delta and all outstanding sync
			 * requests finish before this call returns.
			 * 
			 * Example: Why sync if current delta is empty? The RemoteMakeBuilder forces a sync before and after building. In some
			 * cases, we want to ensure repos are synchronized regardless of the passed delta, which can be set to null.
			 */
			
			ProjectAndRemoteLocationPair syncTarget = new ProjectAndRemoteLocationPair(project, remoteLoc);
		    Boolean syncLR = syncFlags.contains(SyncFlag.SYNC_LR);
		    Boolean syncRL = syncFlags.contains(SyncFlag.SYNC_RL);
			Set<SyncFlag> modifiedSyncFlags = new HashSet<SyncFlag>(syncFlags);

			// Do not sync LR (local-to-remote) if another thread is already waiting to do it.
			if (syncLR) {
				AtomicLong threadCount = syncLRPending.putIfAbsent(syncTarget, new AtomicLong(1));
				if (threadCount != null) {
					if (threadCount.get() > 0) {
						syncLR = false;
						modifiedSyncFlags.remove(SyncFlag.SYNC_LR);
					} else {
						threadCount.incrementAndGet();
					}
				}
			}

			// Do not sync RL (remote-to-local) if another thread is already waiting to do it.
			if (syncRL) {
				AtomicLong threadCount = syncRLPending.putIfAbsent(syncTarget, new AtomicLong(1));
				if (threadCount != null) {
					if (threadCount.get() > 0) {
						syncRL = false;
						modifiedSyncFlags.remove(SyncFlag.SYNC_RL);
					} else {
						threadCount.incrementAndGet();
					}
				}
			}

		    // Return if we have nothing to do.
		    if (!(syncLR || syncRL)) {
		    	return;
		    }

			// lock syncLock. interruptible by progress monitor
			try {
				while (!syncLock.tryLock(50, TimeUnit.MILLISECONDS)) {
					if (subMon.isCanceled()) {
						throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.GitSyncService_4));
					}
				}
			} catch (InterruptedException e1) {
				throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, Messages.GitSyncService_5));
			} finally {
				if (syncLR) {
					AtomicLong LRPending = syncLRPending.get(syncTarget);
					assert(LRPending != null) : Messages.GitSyncService_20;
					LRPending.decrementAndGet();
				}
				if (syncRL) {
					AtomicLong RLPending = syncRLPending.get(syncTarget);
					assert(RLPending != null) : Messages.GitSyncService_21;
					RLPending.decrementAndGet();
				}
			}

			try {
				subMon.subTask(Messages.GitSyncService_9);
				doSync(project, remoteLoc, modifiedSyncFlags, subMon.newChild(95));
			} catch (RemoteSyncMergeConflictException e) {
				subMon.subTask(Messages.GitSyncService_10);
				// Refresh after merge conflict since conflicted files are altered with markup.
				doRefresh(project);
				throw e;
			} finally {
				syncLock.unlock();
			}

			// Sync successful - re-enable error messages. This is really UI code, but there is no way at the moment to notify UI
			// of a successful sync.
			SyncManager.setShowErrors(project, true);

			// Refresh after sync to display changes
			subMon.subTask(Messages.GitSyncService_10);
			doRefresh(project);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Synchronize the given project to the given remote.
	 * The sync strategy follows these three high-level steps:
	 * 1) Commit local changes.
	 * 2) If sync RL is requested, commit and fetch remote changes and then merge them locally.
	 * 3) If sync LR is requested, push local changes to remote and merge. Only fast-forward merging is allowed to prevent remote
	 *    merge conflicts. On failure, do a sync RL, even if not requested, to capture remote changes, and repeat sync LR. It is
	 *    important that merge conflicts happen locally, so they can be handled in Eclipse with JGit. If a merge conflict occurs,
	 *    sync'ing is halted (no sync LR is done) and only re-enabled once the conflicts are resolved.
	 *
	 * @param project
	 * 				the local project
	 * @param remoteLoc
	 * 				the remote location
	 * @param syncFlags
	 * 				flags to modify sync behavior
	 * @param monitor
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. All exceptions are wrapped in a RemoteSyncException and thrown, so that clients
	 *             can always detect when a sync fails and why.
	 */
	private void doSync(IProject project, RemoteLocation remoteLoc, Set<SyncFlag> syncFlags, IProgressMonitor monitor)
			throws RemoteSyncException {
		boolean syncLR = syncFlags.contains(SyncFlag.SYNC_LR);
		boolean syncRL = syncFlags.contains(SyncFlag.SYNC_RL);

		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			subMon.subTask(Messages.GitSyncService_6);
			JGitRepo localRepo = getLocalJGitRepo(project, subMon.newChild(5));

			if (localRepo.inUnresolvedMergeState()) {
				throw new RemoteSyncMergeConflictException(Messages.GitSyncService_8);
			}

			LocalAndRemoteLocationPair lrpair = new LocalAndRemoteLocationPair(localRepo.getDirectory(), remoteLoc);

			// Commit local changes
			subMon.subTask(Messages.GitSyncService_12);
			if (localRepo.commit(subMon.newChild(5))) {
				// New changes - mark all local/remote pairs with current local as needing to be updated.
				Iterator<LocalAndRemoteLocationPair> it = localChangesPushed.iterator();
				while (it.hasNext()) {
					LocalAndRemoteLocationPair lrp = it.next();
					if (lrp.getLocal().equals(localRepo.getDirectory())) {
						it.remove();
					}
				}
			}

			// Return early if local changes have already been pushed and remote-to-local sync was not requested.
			if ((!syncLR || localChangesPushed.contains(lrpair)) && (!syncRL)) {
				return;
			}

			// Get remote repository. creating it if necessary
			subMon.subTask(Messages.GitSyncService_7);
			GitRepo remoteRepo = getGitRepo(remoteLoc, subMon.newChild(10));
			
			// Unresolved connection - abort
			if (remoteRepo == null) {
				return;
			}

			// Update remote file filter
			if (!cleanFileFilterMap.contains(lrpair)) {
				subMon.subTask(Messages.GitSyncService_11);
				remoteRepo.uploadFilter(localRepo, subMon.newChild(10));
				cleanFileFilterMap.add(lrpair);
			}

			// Sync remote to local
			if (syncRL) {
				subMon.subTask(Messages.GitSyncService_24);
				doSyncRL(localRepo, remoteRepo, subMon.newChild(30));
			}

			// Sync local to remote
			if (syncLR && (!localChangesPushed.contains(lrpair))) {
				subMon.subTask(Messages.GitSyncService_25);
				CommandResults results = doSyncLR(localRepo, remoteRepo, subMon.newChild(30));
				boolean success = (results.getExitCode() == 0);
				// If the remote merge fails and sync RL was not done, then the failure *most likely* occurred because files
				// changed on remote. So we do a sync RL to capture changes and then sync LR again.
				if (!success && !syncRL) {
					subMon.subTask(Messages.GitSyncService_26);
					doSyncRL(localRepo, remoteRepo, subMon.newChild(30));
					subMon.subTask(Messages.GitSyncService_27);
					results = doSyncLR(localRepo, remoteRepo, subMon.newChild(10));
					success = (results.getExitCode() == 0);
				}
				if (success) {
					localChangesPushed.add(lrpair);
				} else {
					// This should never happen since sync RL was done prior to sync LR. Only remote changes during the sync or
					// an unanticipated problem could lead to here.
					throw new RemoteSyncException(Messages.GitSyncService_23 + results.getStderr());
				}
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

	// Returns whether sync succeeds (true) or fails (false) in an expected way. Failure can occur normally if remote files
	// were altered, and thus the remote merge cannot be completed.
	private CommandResults doSyncLR(JGitRepo localRepo, GitRepo remoteRepo, IProgressMonitor monitor) throws RemoteSyncException,
	MissingConnectionException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 30);

		try {
			if (localRepo.getGit().branchList().call().size() > 0) { // check whether master was already created
				subMon.subTask(Messages.GitSyncService_18);
				localRepo.push(remoteRepo, subMon.newChild(20));
				subMon.subTask(Messages.GitSyncService_28);
				return remoteRepo.merge(subMon.newChild(10));
			}
			return new CommandResults(); // Successful result with no output
		} catch (TransportException e) {
			throw new RemoteSyncException(e);
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private void doSyncRL(JGitRepo localRepo, GitRepo remoteRepo, IProgressMonitor monitor) throws RemoteSyncException,
	MissingConnectionException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 90);

		try {
			// Commit remote changes
			subMon.subTask(Messages.GitSyncService_13);
			remoteRepo.commitRemoteFiles(subMon.newChild(40));

			// Download remote changes
			subMon.subTask(Messages.GitSyncService_14);
			try {
				localRepo.fetch(remoteRepo, subMon.newChild(40));
			} catch (TransportException e) {
				// Fetch can fail simply because the remote branch isn't set up yet. So just return in that case and throw an
				// exception otherwise.
				// Even for the first case, however, a proceeding merge will fail (remote ref is not created), so be sure to
				// always abort and not attempt a merge.
				if (e.getMessage().startsWith("Remote does not have ")) { //$NON-NLS-1$
					return;
				} else {
					throw new RemoteSyncException(e);
				}
			}

			// Merge remote changes into local repository
			subMon.subTask(Messages.GitSyncService_15);
			try {
				org.eclipse.jgit.api.MergeResult mergeResult = localRepo.merge(subMon.newChild(10));
				if (mergeResult.getFailingPaths() != null) {
					String message = Messages.GitSyncService_16;
					for (String s : mergeResult.getFailingPaths().keySet()) {
						message += System.getProperty("line.separator") + s; //$NON-NLS-1$
					}
					throw new RemoteSyncException(message);
				}
				if (localRepo.inUnresolvedMergeState()) {
					throw new RemoteSyncMergeConflictException(Messages.GitSyncService_8);
					// Even if we later decide not to throw an exception, it is important not to proceed after a merge conflict.
					// return;
				}
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			} catch (IOException e) {
				throw new RemoteSyncException(e);
			}
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
		Iterator<LocalAndRemoteLocationPair> it = cleanFileFilterMap.iterator();
		IPath localDir = project.getLocation();
		if (localDir == null) {
			throw new RemoteSyncException(Messages.GitSyncService_17 + project.getName());
		}
		while (it.hasNext()) {
			LocalAndRemoteLocationPair lp = it.next();
			if (lp.getLocal().equals(localDir)) {
				it.remove();
			}
		}
		JGitRepo localRepo = getLocalJGitRepo(project, null);
		localRepo.setFilter(filter);
	}
	
    // Refresh the workspace in a separate thread
    // Bug 374409 - this prevents deadlock caused by locking both the sync lock and the workspace lock.
    private static Thread doRefresh(final IProject project) {
            Thread refreshWorkspaceThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                    project.refreshLocal(IResource.DEPTH_INFINITE, null);
                            } catch (CoreException e) {
                                    Activator.log(Messages.JGitRepo_16, e);
                            }
                    }
            }, "Refresh workspace thread"); //$NON-NLS-1$
            refreshWorkspaceThread.start();
            return refreshWorkspaceThread;
    }

   	// Set Git repository files as derived.
	// This prevents user-level operations, such as searching, from considering the repository directory.
    private static void setRepoFilesAsDerived(final IProject project) {
    	// First refresh project so that files appear
    	final Thread refreshThread = doRefresh(project);
 
    	// Set derived only after refresh completes
    	Thread setDerivedThread = new Thread(new Runnable() {
    		@Override
    		public void run() {
    			try {
    				refreshThread.join();
    				project.getFolder(GitSyncService.gitDir).setDerived(true, null);
    			} catch (InterruptedException e) {
    				Activator.log(e);
    			} catch (CoreException e) {
    				Activator.log(e);
    			}
    		}
    	}, "Set repository as derived thread"); //$NON-NLS-1$
    	setDerivedThread.start();
    }
}