/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.git.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.eclipse.ptp.internal.rdt.sync.git.core.GitSyncFileFilter.DiffFiles;
import org.eclipse.ptp.internal.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteLocation;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class JGitRepo {
	private final IProject project;
	private Git git;
	private final RemoteLocation remoteLoc;
	private final String localDirectory;
	private GitSyncFileFilter fileFilter;
	private boolean mergeMapInitialized = false; // Call "readMergeConflictFiles" at least once before using the map.
	private final Map<IPath, String[]> FileToMergePartsMap = new HashMap<IPath, String[]>();
	private TransportGitSsh transport;

	/**
	 * Create a remote sync connection using git. Assumes that the local
	 * directory exists but not necessarily the remote directory. It is created
	 * if not.
	 * 
	 * @param conn
	 * @param localDir
	 * @param remoteDir
	 * @throws RemoteSyncException
	 *             on problems building the remote repository. Specific
	 *             exception nested. Upon such an exception, the instance is
	 *             invalid and should not be used.
	 * @throws MissingConnectionException
	 *             when connection missing. In this case, the instance is
	 *             also invalid.
	 */
	public JGitRepo(IProject proj, String localDir, RemoteLocation rl, AbstractSyncFileFilter filter, IProgressMonitor monitor)
			throws RemoteSyncException, MissingConnectionException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			project = proj;
			localDirectory = localDir;
			remoteLoc = rl;
			fileFilter = (GitSyncFileFilter)filter;

			// Build repo, creating it if it is not already present.
			try {
				subMon.subTask(Messages.GitRemoteSyncConnection_20);
				buildRepo(subMon.newChild(80));
			} catch (final IOException e) {
				throw new RemoteSyncException(e);
			} catch (final RemoteExecutionException e) {
				throw new RemoteSyncException(e);
			}

			// Build transport
			final RemoteConfig remoteConfig = buildRemoteConfig(git.getRepository().getConfig());
			subMon.subTask(Messages.GitRemoteSyncConnection_4);
			buildTransport(remoteConfig, subMon.newChild(10));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * 
	 * @param monitor
	 * @param localDirectory
	 * @param remoteHost
	 * @return the repository
	 * @throws IOException
	 *             on problems writing to the file system.
	 * @throws RemoteExecutionException
	 *             on failure to run remote commands.
	 * @throws RemoteSyncException
	 *             on problems with initial local commit. 
	 *             TODO: Consider the consequences of exceptions that occur at various points,
	 *             which can leave the repo in a partial state. For example, if
	 *             the repo is created but the initial commit fails.
	 *             TODO: Consider evaluating the output of "git init".
	 * @throws MissingConnectionException
	 *             on missing connection.
	 */
	private Git buildRepo(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException,
			MissingConnectionException {
		final RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			subMon.subTask(Messages.GitRemoteSyncConnection_1);

			// Get local Git repository, creating it if necessary.
			File localDir = new File(localDirectory);
			FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
			File gitDirFile = new File(localDirectory + File.separator + GitSyncService.gitDir);
			Repository repository = repoBuilder.setWorkTree(localDir).setGitDir(gitDirFile).build();
			if (!(gitDirFile.exists())) {
				repository.create(false);
			}
			git = new Git(repository);
			
            // An initial commit to create the master branch.
            subMon.subTask(Messages.GitRemoteSyncConnection_22);
            doCommit(subMon.newChild(4));

			// Refresh project
			subMon.subTask(Messages.GitRemoteSyncConnection_23);
			final Thread refreshThread = doRefresh(project, subMon.newChild(1));

			// Set git repo as derived, which can only be done after refresh completes.
			// This prevents user-level operations, such as searching, from considering the repo directory.
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
			}, "Set repo as derived thread"); //$NON-NLS-1$
			setDerivedThread.start();

			return git;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Builds the remote configuration for the connection, setting up fetch and
	 * push operations between local and remote master branches.
	 * 
	 * @param config
	 *            configuration for the local repository
	 * @return the remote configuration
	 * @throws RuntimeException
	 *             if the URI in the passed configuration is not properly
	 *             formatted.
	 */
	private RemoteConfig buildRemoteConfig(StoredConfig config) {
		RemoteConfig rconfig = null;

		try {
			rconfig = new RemoteConfig(config, GitSyncService.remoteProjectName);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final RefSpec refSpecFetch = new RefSpec("+refs/heads/master:refs/remotes/" + //$NON-NLS-1$
				GitSyncService.remoteProjectName + "/master"); //$NON-NLS-1$
		final RefSpec refSpecPush = new RefSpec("+master:" + GitSyncService.remotePushBranch); //$NON-NLS-1$
		rconfig.addFetchRefSpec(refSpecFetch);
		rconfig.addPushRefSpec(refSpecPush);

		return rconfig;
	}

	/**
	 * Replace given file with the most recent version in the repository
	 * 
	 * @param path
	 * @throws RemoteSyncException
	 */
	public void checkout(IPath[] paths) throws RemoteSyncException {
		CheckoutCommand checkoutCommand = git.checkout();
		for (IPath p : paths) {
			checkoutCommand.addPath(p.toString());
		}
		checkoutCommand.setStartPoint("HEAD"); //$NON-NLS-1$
		try {
			checkoutCommand.call();
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		}

		doRefresh(project, null);
	}

	/**
	 * Replace given file with the most recent local copy of the remote (not necessarily the same as the current remote)
	 * 
	 * @param path
	 * @throws RemoteSyncException
	 */
	public void checkoutRemoteCopy(IPath[] paths) throws RemoteSyncException {
		CheckoutCommand checkoutCommand = git.checkout();
		for (IPath p : paths) {
			checkoutCommand.addPath(p.toString());
		}
		checkoutCommand.setStartPoint("refs/remotes/" + GitSyncService.remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			checkoutCommand.call();
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		}

		doRefresh(project, null);
	}
	/**
	 * Commit files in working directory. Get the files to be added or removed, call the add and remove command
ds, and then commit
	 * if needed.
	 * 
	 * @throws RemoteSyncException
	 *             on problems committing.
	 * @return whether any changes were committed
	 */
	public boolean doCommit(IProgressMonitor monitor) throws RemoteSyncException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		
		boolean addedOrRemovedFiles = false;

		try {
			DiffFiles diffFiles = fileFilter.getDiffFiles();
			
			subMon.subTask(Messages.GitRemoteSyncConnection_9);
			if (!diffFiles.added.isEmpty()) {
				final AddCommand addCommand = git.add();
				//Bug 401161 doesn't matter here because files are already filtered anyhow. It would be OK
				//if the tree iterator would always return false in isEntryIgnored
				addCommand.setWorkingTreeIterator(new SyncFileTreeIterator(git.getRepository(), fileFilter));
				for (String fileName : diffFiles.added) {
					addCommand.addFilepattern(fileName);
				}
				addCommand.call();
				addedOrRemovedFiles = true;
			}
			subMon.worked(10);

			subMon.subTask(Messages.GitRemoteSyncConnection_10);
			if (!diffFiles.removed.isEmpty()) {
				final RmCommandCached rmCommand = new RmCommandCached(git.getRepository());
				for (String fileName : diffFiles.removed) {
					rmCommand.addFilepattern(fileName);
				}
				rmCommand.call();
				addedOrRemovedFiles = true;
			}
			subMon.worked(10);

			// Check if a commit is required.
			subMon.subTask(Messages.GitRemoteSyncConnection_11);
			if (addedOrRemovedFiles || inMergeState()) {
				final CommitCommand commitCommand = git.commit();
				commitCommand.setMessage(GitSyncService.commitMessage);
				commitCommand.call();
				return true;
			} else {
				return false;
			}
		} catch (final GitAPIException e) {
			throw new RemoteSyncException(e);
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	// Is the repository currently in a merge state?
	private boolean inMergeState() throws IOException {
		try {
			if (git.getRepository().resolve("MERGE_HEAD") == null) { //$NON-NLS-1$
				return false;
			} else {
				return true;
			}
		} catch (AmbiguousObjectException e) {
			// Should never happen...
			Activator.log(e);
			return true;
		}
	}

	// Get the list of merge-conflicted files from jgit and parse each one, storing result in the cache.
	public boolean readMergeConflictFiles() throws RemoteSyncException {
		String repoPath = git.getRepository().getWorkTree().getAbsolutePath();
		if (!repoPath.endsWith(java.io.File.separator)) { // The documentation does not say if the separator is added...
			repoPath += java.io.File.separator;
		}

		FileToMergePartsMap.clear();
		mergeMapInitialized = true;

		RevWalk walk = null;
		try {
			if (!git.getRepository().getRepositoryState().equals(RepositoryState.MERGING))
				return false;
			
			StatusCommand statusCommand = git.status();
			Status status = statusCommand.call();
			if (status.getConflicting().isEmpty()) {
				return false;
			}

			walk = new RevWalk(git.getRepository());
			// Get the head, merge head, and merge base commits
			walk.setRevFilter(RevFilter.MERGE_BASE);
			ObjectId headSHA = git.getRepository().resolve("HEAD"); //$NON-NLS-1$
			ObjectId mergeHeadSHA = git.getRepository().resolve("MERGE_HEAD"); //$NON-NLS-1$
			RevCommit head = walk.parseCommit(headSHA);
			RevCommit mergeHead = walk.parseCommit(mergeHeadSHA);
			walk.markStart(head);
			walk.markStart(mergeHead);
			RevCommit mergeBase = walk.next();

			// For each merge-conflicted file, pull out and store its contents for each of the three commits
			// Would be much faster to use a treewalk and check whether entry is conflicting instead of using
			// status (which uses a treewalk) and then searching for those status found.
			for (String s : status.getConflicting()) {
				String localContents = ""; //$NON-NLS-1$
				TreeWalk localTreeWalk = TreeWalk.forPath(git.getRepository(), s, head.getTree());
				if (localTreeWalk != null) {
					ObjectId localId = localTreeWalk.getObjectId(0);
					localContents = new String(git.getRepository().open(localId).getBytes());
				}

				String remoteContents = ""; //$NON-NLS-1$
				TreeWalk remoteTreeWalk = TreeWalk.forPath(git.getRepository(), s, mergeHead.getTree());
				if (remoteTreeWalk != null) {
					ObjectId remoteId = remoteTreeWalk.getObjectId(0);
					remoteContents = new String(git.getRepository().open(remoteId).getBytes());
				}

				String ancestorContents = ""; //$NON-NLS-1$
				if (mergeBase != null) {
					TreeWalk ancestorTreeWalk = TreeWalk.forPath(git.getRepository(), s, mergeBase.getTree());
					if (ancestorTreeWalk != null) {
						ObjectId ancestorId = ancestorTreeWalk.getObjectId(0);
						ancestorContents = new String(git.getRepository().open(ancestorId).getBytes());
					}
				}

				String[] mergeParts = { localContents, remoteContents, ancestorContents };
				FileToMergePartsMap.put(new Path(s), mergeParts);
			}
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (walk != null) {
				walk.dispose();
			}
		}
		return FileToMergePartsMap.isEmpty();
	}

	/**
	 * Get the file filter for this repository
	 * @return filter
	 */
	public GitSyncFileFilter getFilter() {
		return fileFilter;
	}

	/**
	 * Get the Git instance for this repository
	 * @return Git instance
	 */
	public Git getGit() {
		return git;
	}

	/**
	 * Get the real JGit repository
	 * @return repository
	 */
	public Repository getRepository() {
		return git.getRepository();
	}

	/**
	 * Get the merge-conflicted files
	 * 
	 * @return set of project-relative paths of merge-conflicted files.
	 * @throws RemoteSyncException
	 *             on problems accessing repository
	 */
	public Set<IPath> getMergeConflictFiles() throws RemoteSyncException {
		if (!mergeMapInitialized) {
			this.readMergeConflictFiles();
		}
		return FileToMergePartsMap.keySet();
	}

	/**
	 * Return three strings representing the three parts of the given merge-conflicted file (local, remote, and ancestor,
	 * respectively)
	 * or null if the given file is not in a merge conflict.
	 * 
	 * @param localFile
	 * @return the three parts or null
	 * @throws RemoteSyncException
	 *             on problems accessing repository
	 */
	public String[] getMergeConflictParts(IFile localFile) throws RemoteSyncException {
		if (!mergeMapInitialized) {
			this.readMergeConflictFiles();
		}
		return FileToMergePartsMap.get(localFile.getProjectRelativePath());
	}

	public void setFilter(AbstractSyncFileFilter f) {
        fileFilter = new GitSyncFileFilter(project);
        fileFilter.initialize(f);
        try {
                fileFilter.saveFilter();
        } catch (IOException e) {
                Activator.log(Messages.JGitRepo_0 + project.getName(), e);
        }
	}

	/**
	 * Add the path to the repository, resolving the merge conflict (if any)
	 * 
	 * @param path
	 */
	public void setMergeAsResolved(IPath[] paths) throws RemoteSyncException {
		AddCommand addCommand = git.add();
		for (IPath p : paths) {
			addCommand.addFilepattern(p.toString());
		}
		try {
			addCommand.call();
			// Make sure each file is no longer conflicted before marking as resolved.
			// Sometimes JGit will silently fail to add.
			StatusCommand statusCommand = git.status();
			Status status = statusCommand.call();
			for (IPath p : paths) {
				if (!(status.getConflicting().contains(p.toString()))) {
					FileToMergePartsMap.remove(p);
				}
			}
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		}
	}

	// Subclass JGit's generic RemoteSession to set up running of remote
	// commands using the available process builder.
	public class PTPSession implements RemoteSession {
		private final URIish uri;

		public PTPSession(URIish uri) {
			this.uri = uri;
		}

		@Override
		public Process exec(String command, int timeout) throws TransportException {
			// TODO: Use a library for command splitting.
			List<String> commandList = new LinkedList<String>();
			commandList.add("sh"); //$NON-NLS-1$
			commandList.add("-c"); //$NON-NLS-1$
			commandList.add(command);

			try {
				IRemoteConnection connection = remoteLoc.getConnection();
				if (!connection.isOpen()) {
					connection.open(null);
				}
				return (AbstractRemoteProcess) connection.getRemoteServices().getProcessBuilder(connection, commandList).start();
			} catch (IOException e) {
				throw new TransportException(uri, e.getMessage(), e);
			} catch (RemoteConnectionException e) {
				throw new TransportException(uri, e.getMessage(), e);
			} catch (MissingConnectionException e) {
				throw new TransportException(uri, Messages.GitRemoteSyncConnection_3 + e.getConnectionName(), e);
			}

		}

		@Override
		public void disconnect() {
			// Nothing to do
		}
	}

	/**
	 * Creates the transport object that JGit uses for executing commands
	 * remotely.
	 * 
	 * @param remoteConfig
	 *            the remote configuration for our local Git repo
	 * @throws RuntimeException
	 *             if the requested transport is not supported by JGit.
	 */
	private void buildTransport(RemoteConfig remoteConfig, IProgressMonitor monitor) {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		final URIish uri = buildURI();
		try {
			subMon.subTask(Messages.GitRemoteSyncConnection_8);
			transport = (TransportGitSsh) Transport.open(git.getRepository(), uri);
		} catch (NotSupportedException e) {
			throw new RuntimeException(e);
		} catch (TransportException e) {
			throw new RuntimeException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		// Set the transport to use our own means of executing remote commands.
		transport.setSshSessionFactory(new SshSessionFactory() {
			@Override
			public RemoteSession getSession(URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms)
					throws TransportException {
				return new PTPSession(uri);
			}
		});

		transport.applyConfig(remoteConfig);
	}

	/**
	 * Build the URI for the remote host as needed by the transport. Since the
	 * transport will use an external SSH session, we do not need to provide
	 * user, host, or password. However, the function for opening a transport
	 * throws an exception if the host is null or empty length. So we set it to
	 * a dummy string.
	 * 
	 * @return URIish
	 */
	private URIish buildURI() {
		return new URIish()
		// .setUser(connection.getUsername())
				.setHost("none") //$NON-NLS-1$
				// .setPass("")
				.setScheme("ssh") //$NON-NLS-1$
				.setPath(remoteLoc.getDirectory(project) + "/" + GitSyncService.gitDir); //$NON-NLS-1$  // Should use remote path seperator
		                                                                                         // but first 315720 has to be fixed
	}

	public void close() {
		transport.close();
		git.getRepository().close();
	}

	// Refresh the workspace after creating new local files
	// Bug 374409 - run refresh in a separate thread to avoid possible deadlock from locking both the sync lock and the
	// workspace lock.
	private static Thread doRefresh(final IProject project, final IProgressMonitor subMon) {
		Thread refreshWorkspaceThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, subMon);
				} catch (CoreException e) {
					Activator.log(Messages.GitRemoteSyncConnection_0, e);
				}
			}
		}, "Refresh workspace thread"); //$NON-NLS-1$
		refreshWorkspaceThread.start();
		return refreshWorkspaceThread;
	}
}