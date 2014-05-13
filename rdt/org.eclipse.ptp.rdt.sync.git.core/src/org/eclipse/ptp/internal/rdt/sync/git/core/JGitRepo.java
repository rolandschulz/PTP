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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
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
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.AbstractRemoteProcess;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * Class for a local Git repository managed by JGit
 */
public class JGitRepo {
	public static final String remoteBranchName = "eclipse_auto"; //$NON-NLS-1$
	public static final String EMPTY_FILE_NAME = ".ptp-sync-folder"; //$NON-NLS-1$

	private IPath localDirectory;
	private Git git;
	private GitSyncFileFilter fileFilter = null;
	private boolean mergeMapInitialized = false; // If false, call "readMergeConflictFiles" to populate the map.
	private final Map<IPath, String[]> fileToMergePartsMap = new HashMap<IPath, String[]>();
	private final Map<RemoteLocation, TransportGitSsh> remoteToTransportMap = new HashMap<RemoteLocation, TransportGitSsh>();

	/**
	 * Create a JGit repository instance for the given local directory, creating resources, such as Git-specific files, if necessary.
	 * 
	 * @param localDir
	 * @param monitor
	 *
	 * @throws GitAPIException
	 * 				on JGit-specific problems - instance should be considered invalid
	 * @throws IOException
	 * 				on file system problems - instance should be considered invalid
	 */
	public JGitRepo(IPath localDir, IProgressMonitor monitor) throws GitAPIException, IOException {
		localDirectory = localDir;
		try {
			buildRepo(localDirectory.toOSString(), monitor);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Build the JGit repository - creating resources, such as Git-specific files, if necessary.
	 *
	 * @param localDirectory
	 * 			Repository location
	 * @param monitor
	 * @return
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems
	 */
	private Git buildRepo(String localDirectory, IProgressMonitor monitor) throws GitAPIException, IOException {
		final RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		try {
			// Get local Git repository, creating it if necessary.
			File localDir = new File(localDirectory);
			FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
			File gitDirFile = new File(localDirectory + File.separator + GitSyncService.gitDir);
			Repository repository = repoBuilder.setWorkTree(localDir).setGitDir(gitDirFile).build();
			boolean repoExists = gitDirFile.exists();
			if (!repoExists) {
				repository.create(false);
			}
			git = new Git(repository);
			
			if (!repoExists) {
				fileFilter = new GitSyncFileFilter(this, SyncManager.getDefaultFileFilter());
				fileFilter.saveFilter();
			} else {
				fileFilter = new GitSyncFileFilter(this);
				fileFilter.loadFilter();
			}
			subMon.worked(5);
			
            // An initial commit to create the master branch.
            subMon.subTask(Messages.JGitRepo_0);
            if (!repoExists) {
            	commit(subMon.newChild(4));
            } else {
            	subMon.worked(4);
            }

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
			rconfig = new RemoteConfig(config, remoteBranchName);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final RefSpec refSpecFetch = new RefSpec("+refs/heads/master:refs/remotes/" + //$NON-NLS-1$
				remoteBranchName + "/master"); //$NON-NLS-1$
		final RefSpec refSpecPush = new RefSpec("+master:" + GitSyncService.remotePushBranch); //$NON-NLS-1$
		rconfig.addFetchRefSpec(refSpecFetch);
		rconfig.addPushRefSpec(refSpecPush);

		return rconfig;
	}

	/**
	 * Replace given files with the most recent versions in the repository
	 * 
	 * @param paths
	 * 			Array of paths to checkout
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 */
	public void checkout(IPath[] paths) throws GitAPIException  {
		CheckoutCommand checkoutCommand = git.checkout();
		for (IPath p : paths) {
			checkoutCommand.addPath(p.toString());
		}
		checkoutCommand.setStartPoint("HEAD"); //$NON-NLS-1$
		checkoutCommand.call();
	}

	/**
	 * Replace given files with the most recent local copies from the remote (not necessarily the same as the current remote since
	 * no transferring of files is done)
	 * 
	 * @param paths
	 * 			Array of paths to replace
	 * @throws GitAPIException
	 */
	public void checkoutRemoteCopy(IPath[] paths) throws GitAPIException {
		CheckoutCommand checkoutCommand = git.checkout();
		for (IPath p : paths) {
			checkoutCommand.addPath(p.toString());
		}
		checkoutCommand.setStartPoint("refs/remotes/" + remoteBranchName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
		checkoutCommand.call();
	}

	/**
	 * Commit files in working directory.
	 * 
	 * assumes that no files are in conflict (do not call during merge)
	 *
	 * @param monitor
	 *
	 * @return whether any changes were committed
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems
	 */
	public boolean commit(IProgressMonitor monitor) throws GitAPIException, IOException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		
		boolean addedOrRemovedFiles = false;

		assert(!inUnresolvedMergeState());
		try {
			DiffFiles diffFiles = fileFilter.getDiffFiles();

			// Create and add an empty file to all synchronized directories to force sync'ing of empty directories
			for (String dirName : diffFiles.dirSet) {
				IPath emptyFilePath = new Path(this.getRepository().getWorkTree().getAbsolutePath());
				emptyFilePath = emptyFilePath.append(dirName);
				emptyFilePath = emptyFilePath.append(EMPTY_FILE_NAME);
				File emptyFile = new File(emptyFilePath.toOSString());
				boolean fileWasCreated = emptyFile.createNewFile();
				if (fileWasCreated) {
					diffFiles.added.add(emptyFilePath.toString());
				}
			}
			
			subMon.subTask(Messages.JGitRepo_2);
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
			subMon.worked(3);

			subMon.subTask(Messages.JGitRepo_3);
			if (!diffFiles.removed.isEmpty()) {
				final RmCommand rmCommand = new RmCommand(git.getRepository());
				rmCommand.setCached(true);
				for (String fileName : diffFiles.removed) {
					rmCommand.addFilepattern(fileName);
				}
				rmCommand.call();
				addedOrRemovedFiles = true;
			}
			subMon.worked(3);

			// Check if a commit is required.
			subMon.subTask(Messages.JGitRepo_4);
			if (addedOrRemovedFiles || inMergeState()) {
				final CommitCommand commitCommand = git.commit();
				commitCommand.setMessage(GitSyncService.commitMessage);
				commitCommand.call();
				return true;
			} else {
				return false;
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Check if the given commit exists.
	 *
	 * @return whether the commit exists
	 * @throws IOException
	 * 					on problems accessing the file system
	 * @throws IncorrectObjectTypeException 
	 * @throws AmbiguousObjectException 
	 * @throws RevisionSyntaxException
	 * 					exceptions that most likely indicate JGit had problems handling the passed id
	 */
	boolean commitExists(String commitId) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
	IOException {
		ObjectId commitObjectId = this.getRepository().resolve(commitId);
		return this.getRepository().hasObject(commitObjectId);
	}

	/**
	 * Fetch files from the given remote. This only transmits the files. It does not update the local repository.
	 *
	 * @param remoteLoc
	 * 			remote location
	 * @param monitor
	 *
	 * @throws TransportException
	 * 			on problem transferring files
	 */
	public void fetch(RemoteLocation remoteLoc, IProgressMonitor monitor) throws TransportException {
		int work = 10;
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, work);
		TransportGitSsh transport = remoteToTransportMap.get(remoteLoc);
		if (transport == null) {
			work /= 2;
			subMon.subTask(Messages.JGitRepo_5);
			transport = this.buildTransport(remoteLoc, subMon.newChild(work));
			remoteToTransportMap.put(remoteLoc, transport);
		}

		try {
			subMon.subTask(Messages.JGitRepo_6);
			transport.fetch(new EclipseGitProgressTransformer(subMon.newChild(work)), null);
		} catch (NotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Push local repository changes to remote. (Only committed changes are pushed). Does not update the remote repository.
	 *
	 * @param remoteLoc
	 * 			remote location
	 * @param monitor
	 *
	 * @throws TransportException
	 *			on problem transferring files
	 */
	public void push(RemoteLocation remoteLoc, IProgressMonitor monitor) throws TransportException {
		int work = 10;
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, work);
		TransportGitSsh transport = remoteToTransportMap.get(remoteLoc);
		if (transport == null) {
			work /= 2;
			subMon.subTask(Messages.JGitRepo_5);
			transport = this.buildTransport(remoteLoc, subMon.newChild(work));
			remoteToTransportMap.put(remoteLoc, transport);
		}
		try {
			subMon.subTask(Messages.JGitRepo_8);
			transport.push(new EclipseGitProgressTransformer(subMon.newChild(work)), null);
		} catch (NotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Is repository currently in a merge state, either resolved or unresolved?
	 *
	 * @return whether repository is in a merge state
	 * @throws IOException
	 */
	public boolean inMergeState() throws IOException {
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

	/**
	 * Is repository currently in an unresolved merge state?
	 *
	 * @return whether repository is in an unresolved merge state
	 * @throws IOException
	 */
	public boolean inUnresolvedMergeState() throws IOException {
		if (inMergeState() && !(git.getRepository().getRepositoryState().equals(RepositoryState.MERGING_RESOLVED))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Find and parse each merge-conflicted file, storing local, remote, and ancestor versions of each file in a cache.
	 *
	 * @param monitor
	 *
	 * @return whether any merge conflicts were found
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems
	 */
	public boolean readMergeConflictFiles(IProgressMonitor monitor) throws GitAPIException, IOException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);

		String repoPath = git.getRepository().getWorkTree().getAbsolutePath();
		if (!repoPath.endsWith(java.io.File.separator)) { // The documentation does not say if the separator is added...
			repoPath += java.io.File.separator;
		}

		fileToMergePartsMap.clear();
		mergeMapInitialized = true;

		RevWalk walk = null;
		try {
			if (!git.getRepository().getRepositoryState().equals(RepositoryState.MERGING))
				return false;

			subMon.subTask(Messages.JGitRepo_9);
			StatusCommand statusCommand = git.status();
			Status status = statusCommand.call();
			if (status.getConflicting().isEmpty()) {
				return false;
			}
			subMon.worked(30);

			subMon.subTask(Messages.JGitRepo_10);
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
			subMon.worked(30);

			// For each merge-conflicted file, pull out and store its contents for each of the three commits
			// Would be much faster to use a treewalk and check whether entry is conflicting instead of using
			// status (which uses a treewalk) and then searching for those status found.
			subMon.subTask(Messages.JGitRepo_11);
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
				fileToMergePartsMap.put(new Path(s), mergeParts);
			}
			subMon.worked(40);
		} finally {
			if (walk != null) {
				walk.dispose();
			}
			if (monitor != null) {
				monitor.done();
			}
		}
		return fileToMergePartsMap.isEmpty();
	}

	/**
	 * Get the local directory for this repository (an absolute path)
	 * @return directory
	 */
	public IPath getDirectory() {
		return localDirectory;
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
	 * Get the set of merge-conflicted files
	 * 
	 * @return set of relative (to localDirectory) paths of merge-conflicted files.
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems 
	 */
	public Set<IPath> getMergeConflictFiles() throws GitAPIException, IOException {
		if (!mergeMapInitialized) {
			this.readMergeConflictFiles(null);
		}
		return fileToMergePartsMap.keySet();
	}

	/**
	 * Return three strings representing the three parts of the given merge-conflicted file (local, remote, and ancestor,
	 * respectively) or null if the given file is not in a merge conflict.
	 * 
	 * @param localFile
	 * 				Must be a relative path to the file from the repository
	 * @return the three parts or null
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems 
	 */
	public String[] getMergeConflictParts(IPath localFile) throws GitAPIException, IOException {
		if (!mergeMapInitialized) {
			this.readMergeConflictFiles(null);
		}
		return fileToMergePartsMap.get(localFile);
	}

	/**
	 * Merge changes previously fetched from a remote repository
	 *
	 * @param monitor
	 *
	 * @return merge results
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 * @throws IOException
	 * 			on file system problems 
	 */
	public MergeResult merge(IProgressMonitor monitor) throws IOException, GitAPIException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		try {
		Ref remoteMasterRef = git.getRepository().
				getRef("refs/remotes/" + remoteBranchName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
		final MergeCommand mergeCommand = git.merge().include(remoteMasterRef);
		subMon.subTask(Messages.JGitRepo_12);
		// Bug 434783: Merge resolution only works once for each Eclipse session.
		// Need to reset flag after each merge.
		mergeMapInitialized = false;
		return mergeCommand.call();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Set filter for this repository
	 *
	 * @param filter
	 */
	public void setFilter(AbstractSyncFileFilter filter) {
        fileFilter = new GitSyncFileFilter(this);
        fileFilter.initialize(filter);
        try {
                fileFilter.saveFilter();
        } catch (IOException e) {
                Activator.log(Messages.JGitRepo_13 + localDirectory, e);
        }
	}

	/**
	 * Add the given path to the repository, resolving the merge conflict (if any)
	 * 
	 * @param paths
	 * @throws GitAPIException
	 * 			on JGit-specific problems
	 */
	public void setMergeAsResolved(IPath[] paths) throws GitAPIException {
		AddCommand addCommand = git.add();
		for (IPath p : paths) {
			addCommand.addFilepattern(p.toString());
		}
		addCommand.call();
		// Make sure each file is no longer conflicted before marking as resolved.
		// Sometimes JGit will silently fail to add.
		StatusCommand statusCommand = git.status();
		Status status = statusCommand.call();
		for (IPath p : paths) {
			if (!(status.getConflicting().contains(p.toString()))) {
				fileToMergePartsMap.remove(p);
			}
		}
	}

	// Subclass JGit's generic RemoteSession to run commands using the remote location's process builder
	private class PTPSession implements RemoteSession {
		private final RemoteLocation remoteLoc;
		private final URIish uri;

		/**
		 * Build new session instance for the given remote location
		 * @param remoteLoc
		 */
		public PTPSession(RemoteLocation remoteLoc) {
			this.remoteLoc = remoteLoc;
			this.uri = buildURI(remoteLoc.getDirectory());
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jgit.transport.RemoteSession#exec(java.lang.String, int)
		 */
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
				return (AbstractRemoteProcess) connection.getProcessBuilder(commandList).start();
			} catch (IOException e) {
				throw new TransportException(uri, e.getMessage(), e);
			} catch (RemoteConnectionException e) {
				throw new TransportException(uri, e.getMessage(), e);
			} catch (MissingConnectionException e) {
				throw new TransportException(uri, Messages.JGitRepo_14 + e.getConnectionName(), e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jgit.transport.RemoteSession#disconnect()
		 */
		@Override
		public void disconnect() {
			// Nothing to do
		}
	}

	/**
	 * Creates a new transport object for executing commands at the given remote location.
	 * 
	 * @param remoteLocation
	 *				the remote location
	 *            
	 * @return new transport instance - never null.
	 * @throws TransportException
	 *				on problem creating transport
	 * @throws RuntimeException
	 *				if the requested transport is not supported by JGit.
	 */
	private TransportGitSsh buildTransport(final RemoteLocation remoteLoc, IProgressMonitor monitor) throws TransportException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		RemoteConfig remoteConfig = buildRemoteConfig(git.getRepository().getConfig());
		final URIish uri = buildURI(remoteLoc.getDirectory());
		TransportGitSsh transport;
		try {
			subMon.subTask(Messages.JGitRepo_15);
			transport = (TransportGitSsh) Transport.open(git.getRepository(), uri);
		} catch (NotSupportedException e) {
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
				return new PTPSession(remoteLoc);
			}
		});

		transport.applyConfig(remoteConfig);
		return transport;
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
	private URIish buildURI(String directory) {
		return new URIish()
		// .setUser(connection.getUsername())
				.setHost("none") //$NON-NLS-1$
				// .setPass("")
				.setScheme("ssh") //$NON-NLS-1$
				.setPath(directory + "/" + GitSyncService.gitDir); //$NON-NLS-1$  // Should use remote path seperator but first
		                                                                          // 315720 has to be fixed
	}

	/**
	 * Releases all resources allocated by this JGit repository instance, including closing all transport objects and closing the
	 * repository itself. Instance should not be used after calling this method.
	 */
	public void close() {
		for (TransportGitSsh t : remoteToTransportMap.values()) {
			t.close();
		}
		remoteToTransportMap.clear();
		git.getRepository().close();
		git = null;
		fileFilter = null;
		fileToMergePartsMap.clear();
	}
}