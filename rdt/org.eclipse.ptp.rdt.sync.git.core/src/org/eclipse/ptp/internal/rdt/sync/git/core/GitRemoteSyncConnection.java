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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
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
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.internal.rdt.sync.git.core.GitSyncFileFilter.DiffFiles;
import org.eclipse.ptp.internal.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncMergeConflictException;
import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * 
 * This class implements a remote sync tool using git, as accessed through the
 * jgit library.
 * 
 */
public class GitRemoteSyncConnection {
	private static final String instanceScopeSyncNode = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String GIT_LOCATION_NODE_NAME = "git-location"; //$NON-NLS-1$

	private final int MAX_FILES = 100;
	private static final String remoteProjectName = "eclipse_auto"; //$NON-NLS-1$
	private static final String commitMessage = Messages.GRSC_CommitMessage;
	public static final String gitDir = ".ptp-sync"; //$NON-NLS-1$
	private static final String gitArgs = "--git-dir=" + gitDir + " --work-tree=."; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String remotePushBranch = "ptp-push"; //$NON-NLS-1$
	private final String localDirectory;
	private final SyncConfig syncConfig;
	GitSyncFileFilter fileFilter;
	private Git git;
	private TransportGitSsh transport;
	private final IProject project;

	private boolean mergeMapInitialized = false; // Call "readMergeConflictFiles" at least once before using the map.
	private final Map<IPath, String[]> FileToMergePartsMap = new HashMap<IPath, String[]>();
	private int remoteGitVersion;
	
	// Static storage for each project's local Git repository (one repo per project)
	private static Map<IProject, Repository> projectToRepoMap = new HashMap<IProject, Repository>();

	/**
	 * Get the local Git repository for the given project, creating it if necessary.
	 *
	 * @param project
	 * @param localDirectory
	 * 				project location on local host
	 * @return new local Git repository - should never be null.
	 * @throws IOException
	 */
	public static Repository getLocalRepo(IProject project, String localDirectory) throws IOException {
		if (projectToRepoMap.containsKey(project)) {
			return projectToRepoMap.get(project);
		} else {
			Repository repository = createLocalRepo(project, localDirectory);
			projectToRepoMap.put(project, repository);
			return repository;
		}
	}

	private static Repository createLocalRepo(final IProject project, String localDirectory) throws IOException {
		final File localDir = new File(localDirectory);
		final FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		File gitDirFile = new File(localDirectory + File.separator + gitDir);
		Repository repository = repoBuilder.setWorkTree(localDir).setGitDir(gitDirFile).build();

		// Create and configure local repository if it is not already present
		if (!(gitDirFile.exists())) {
			repository.create(false);
		}

		return repository;
	}

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
	public GitRemoteSyncConnection(IProject proj, String localDir, SyncConfig bs, AbstractSyncFileFilter filter, IProgressMonitor monitor)
			throws RemoteSyncException, MissingConnectionException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			project = proj;
			localDirectory = localDir;
			syncConfig = bs;
			fileFilter = (GitSyncFileFilter)filter;

			// Build repo, creating it if it is not already present.
			try {
				subMon.subTask(Messages.GitRemoteSyncConnection_21);
				remoteGitVersion = getRemoteGitVersion(subMon.newChild(10));
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
			rconfig = new RemoteConfig(config, remoteProjectName);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final RefSpec refSpecFetch = new RefSpec("+refs/heads/master:refs/remotes/" + //$NON-NLS-1$
				remoteProjectName + "/master"); //$NON-NLS-1$
		final RefSpec refSpecPush = new RefSpec("+master:" + remotePushBranch); //$NON-NLS-1$
		rconfig.addFetchRefSpec(refSpecFetch);
		rconfig.addPushRefSpec(refSpecPush);

		return rconfig;
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
			git = new Git(getLocalRepo(project, localDirectory));
			
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
						project.getFolder(gitDir).setDerived(true, null);
					} catch (InterruptedException e) {
						Activator.log(e);
					} catch (CoreException e) {
						Activator.log(e);
					}
				}
			}, "Set repo as derived thread"); //$NON-NLS-1$
			setDerivedThread.start();

			// Create remote directory if necessary.
			try {
				subMon.subTask(Messages.GitRemoteSyncConnection_24);
				CommandRunner.createRemoteDirectory(syncConfig.getRemoteConnection(), syncConfig.getLocation(project),
						subMon.newChild(5));
			} catch (final CoreException e) {
				throw new RemoteSyncException(e);
			}

			// Initialize remote directory if necessary
			subMon.subTask(Messages.GitRemoteSyncConnection_25);
			doRemoteInit(subMon.newChild(5));

			subMon.subTask(Messages.GitRemoteSyncConnection_27);
			commitRemoteFiles(subMon.newChild(5));

			return git;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Create and configure remote repository if it is not already present. Note
	 * that "git init" is "safe" on a repo already created, so we can simply
	 * rerun it each time.
	 * 
	 * @param monitor
	 * @throws IOException
	 * @throws RemoteExecutionException
	 * @throws RemoteSyncException
	 * @throws MissingConnectionException
	 * @return whether this repo already existed
	 */
	private boolean doRemoteInit(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException,
			MissingConnectionException {
		try {
			String commands = "git --git-dir=" + gitDir + " init && " + //$NON-NLS-1$ //$NON-NLS-2$
					"git --git-dir=" + gitDir + " config core.preloadindex true"; //$NON-NLS-1$ //$NON-NLS-2$
			CommandResults commandResults = null;

			try {
				commandResults = this.executeRemoteCommand(commands, monitor);
			} catch (final InterruptedException e) {
				throw new RemoteExecutionException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteExecutionException(e);
			}

			if (commandResults.getExitCode() != 0) {
				throw new RemoteExecutionException(Messages.GRSC_GitInitFailure + commandResults.getStderr());
			}

			// Pattern matching is error prone, of course, so make this more
			// likely to return false. This will cause all files to be
			// added, which is better than leaving all files untracked. This is
			// better for users without knowledge of git, who would
			// likely not be connecting to a previous git repo.
			if (commandResults.getStdout().contains("existing")) { //$NON-NLS-1$
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

	/*
	 * Do a "git commit" on the remote host
	 */
	private void commitRemoteFiles(IProgressMonitor monitor) throws RemoteSyncException, MissingConnectionException {
		try {
			if (syncConfig.getProperty(GitSyncFileFilter.REMOTE_FILTER_IS_DIRTY).equals("TRUE")) { //$NON-NLS-1$
				IRemoteConnection conn = syncConfig.getRemoteConnection();
				IRemoteServices remoteServices = conn.getRemoteServices();
				Repository repository = git.getRepository();
				
				//copy info/exclude to remote
				File exclude = repository.getFS().resolve(repository.getDirectory(),
						Constants.INFO_EXCLUDE);
				IFileStore local = EFS.getLocalFileSystem().getStore(new Path(exclude.getAbsolutePath()));
				String remoteExclude = syncConfig.getLocation(project) + "/" + gitDir + "/" + Constants.INFO_EXCLUDE;  //$NON-NLS-1$ //$NON-NLS-2$
				IFileStore remote = remoteServices.getFileManager(conn).getResource(remoteExclude);
				local.copy(remote, EFS.OVERWRITE, monitor);
				
				//remove ignored files from index
				if (remoteGitVersion>=1080102) {
					final String  command = gitCommand() + " ls-files -X " + gitDir + "/" + Constants.INFO_EXCLUDE + " -i | " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							gitCommand() + " update-index --force-remove --stdin ; " + //$NON-NLS-1$
							gitCommand() + " commit --allow-empty -m \"" + commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					CommandResults commandResults = this.executeRemoteCommand(command, monitor);
					if (commandResults.getExitCode() != 0) {
						throw new RemoteSyncException(Messages.GRSC_GitRemoveFilteredFailure1 + commandResults.getStderr());
					}
				} else {
					final String  command = gitCommand() + " rev-parse HEAD"; //$NON-NLS-1$
					CommandResults commandResults = this.executeRemoteCommand(command, monitor); 
					ObjectId objectId = null;
					if (commandResults.getExitCode()==0)
						objectId = repository.resolve(commandResults.getStdout().trim());
					RevTree ref=null;
					if (objectId!=null)
						ref = new RevWalk(repository).parseTree(objectId);
					if (ref!=null) {
						Set<String> filesToRemove = fileFilter.getIgnoredFiles(ref);
						deleteRemoteFiles(filesToRemove,monitor);
					}
				}
				syncConfig.setProperty(GitSyncFileFilter.REMOTE_FILTER_IS_DIRTY, "FALSE"); //$NON-NLS-1$
			}
			
			//it would be nicer to  not do the commit instead of using allow-empty. But I can't find a way to check whether the list of files is empty (or update-index)
			//didn't do anything
		    final String command = gitCommand() + " ls-files -X " + gitDir + "/" + Constants.INFO_EXCLUDE + " -o -m | " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					gitCommand() + " update-index --add --remove --stdin ; " + //$NON-NLS-1$
					gitCommand() + " commit --allow-empty -m \"" + commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			CommandResults commandResults = this.executeRemoteCommand(command, monitor);
			if (commandResults.getExitCode() != 0) {
				throw new RemoteSyncException(Messages.GRSC_GitCommitFailure + commandResults.getStderr());
			}
		} catch (final InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (CoreException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteExecutionException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
	
    /*
     * Do a "git rm --cached -f <Files>" on the remote host
     */
    private void deleteRemoteFiles(Set<String> filesToDelete, IProgressMonitor monitor) throws IOException,
                    RemoteExecutionException, RemoteSyncException, MissingConnectionException {
    	try {
    		while (!filesToDelete.isEmpty()) {
    			List<String> commandList = stringToList(gitCommand() + " rm --cached -f --"); //$NON-NLS-1$ //the MAX_FILES trick can be avoided by sending files to stdin instead of passing as arguments
    			int count = 1;
    			for (String fileName : filesToDelete.toArray(new String[0])) {
    				if (count++ % MAX_FILES == 0) {
    					break;
    				}
    				commandList.add(fileName);
    				filesToDelete.remove(fileName);
    			}

    			CommandResults commandResults = null;
    			try {
    				commandResults = this.executeRemoteCommand(commandList, monitor);
    			} catch (final InterruptedException e) {
    				throw new RemoteExecutionException(e);
    			} catch (RemoteConnectionException e) {
    				throw new RemoteExecutionException(e);
    			}
    			if (commandResults.getExitCode() != 0) {
    				throw new RemoteExecutionException(Messages.GRSC_GitRmFailure + commandResults.getStderr());
    			}
    		}
    	} finally {
    		if (monitor != null) {
    			monitor.done();
    		}
    	}
    }

    private List<String> stringToList(String command) {
            return new ArrayList<String>(Arrays.asList(command.split(" "))); //$NON-NLS-1$
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
				IRemoteConnection connection = syncConfig.getRemoteConnection();
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
				.setPath(syncConfig.getLocation(project) + "/" + gitDir); //$NON-NLS-1$  // Should use remote path seperator but
																			// first 315720 has to be fixed
	}

	public void close() {
		transport.close();
		git.getRepository().close();
	}

	/**
	 * Commit files in working directory. Get the files to be added or removed, call the add and remove commands, and then commit
	 * if needed.
	 * 
	 * @throws RemoteSyncException
	 *             on problems committing.
	 * @return whether any changes were committed
	 */
	private boolean doCommit(IProgressMonitor monitor) throws RemoteSyncException {
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
				commitCommand.setMessage(commitMessage);
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
	private void readMergeConflictFiles() throws RemoteSyncException {
		String repoPath = git.getRepository().getWorkTree().getAbsolutePath();
		if (!repoPath.endsWith(java.io.File.separator)) { // The documentation does not say if the separator is added...
			repoPath += java.io.File.separator;
		}

		FileToMergePartsMap.clear();
		mergeMapInitialized = true;

		RevWalk walk = null;
		try {
			StatusCommand statusCommand = git.status();
			Status status = statusCommand.call();
			if (status.getConflicting().isEmpty()) {
				return;
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
	}

	/**
	 * @return the project
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @return the connection (IRemoteConnection)
	 * @throws MissingConnectionException
	 */
	public IRemoteConnection getConnection() throws MissingConnectionException {
		return syncConfig.getRemoteConnection();
	}

	/**
	 * @return the localDirectory
	 */
	public String getLocalDirectory() {
		return localDirectory;
	}

	/**
	 * @return the remoteDirectory
	 */
	public String getRemoteDirectory() {
		return syncConfig.getLocation(project);
	}

	/**
	 * Many of the listed exceptions appear to be unrecoverable, caused by
	 * errors in the initial setup. It is vital, though, that failed syncs are
	 * reported and handled. So all exceptions are checked exceptions, embedded
	 * in a RemoteSyncException.
	 * 
	 * @param monitor
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. The specific exception is
	 *             nested within the RemoteSyncException. 
	 *             TODO: Consider possible platform dependency.
	 */
	public void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_sync_local_to_remote);
		try {
			// First commit changes to the local repository.
			doCommit(subMon.newChild(1));

			// Then push them to the remote site.
			try {
				// TODO: we currently need to do this always because we don't keep track of failed commits. We first need to decide
				// whether we want to do commits based on delta or (as currently) based on git searching modified files
				// Than we can either keep track of deltas not transported yet or we can compare SHA-numbers (HEAD to remote-ref
				// from last pull) to see whether something needs to be transported
				// Than we can also get rid of this hack to check for existence of master
				if (git.branchList().call().size() > 0) { // check whether master was already created
					transport.push(new EclipseGitProgressTransformer(subMon.newChild(5)), null);

					// Now remotely merge changes with master branch
					CommandResults mergeResults;
					final String command = gitCommand() + " merge " + remotePushBranch; //$NON-NLS-1$

					mergeResults = this.executeRemoteCommand(command, subMon.newChild(5));

					if (mergeResults.getExitCode() != 0) {
						throw new RemoteSyncException(new RemoteExecutionException(Messages.GRSC_GitMergeFailure
								+ mergeResults.getStderr()));
					}
				}
			} catch (final IOException e) {
				throw new RemoteSyncException(e);
			} catch (final InterruptedException e) {
				throw new RemoteSyncException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteSyncException(e);
			} catch (GitAPIException e) {
				throw new RemoteSyncException(e);
			} catch (MissingConnectionException e) {
				// nothing to do
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * @param monitor
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. The specific exception is
	 *             nested within the RemoteSyncException. Many of the listed
	 *             exceptions appear to be unrecoverable, caused by errors in
	 *             the initial setup. It is vital, though, that failed syncs are
	 *             reported and handled. So all exceptions are checked
	 *             exceptions, embedded in a RemoteSyncException.
	 */
	public void syncRemoteToLocal(IProgressMonitor monitor, boolean includeUntrackedFiles) throws RemoteSyncException {

		// TODO: Figure out why pull doesn't work and why we have to fetch and
		// merge instead.
		// PullCommand pullCommand = gitConnection.getGit().pull().
		// try {
		// pullCommand.call();
		// } catch (WrongRepositoryStateException e) {
		// throw new RemoteSyncException(e);
		// } catch (InvalidConfigurationException e) {
		// throw new RemoteSyncException(e);
		// } catch (DetachedHeadException e) {
		// throw new RemoteSyncException(e);
		// } catch (InvalidRemoteException e) {
		// throw new RemoteSyncException(e);
		// } catch (CanceledException e) {
		// throw new RemoteSyncException(e);
		// }
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_sync_remote_to_local);
		try {
			commitRemoteFiles(subMon.newChild(5));
			// Next, fetch the remote repository
			// TODO: we currently need to do this always because we don't keep track of failed commits. We first need to decide
			// whether we want to do commits based on delta or (as currently) based on git searching modified files
			// Than we can either keep track of deltas not transported yet or we can compare SHA-numbers (HEAD to remote-ref from
			// last pull) to see whether something needs to be transported
			transport.fetch(new EclipseGitProgressTransformer(subMon.newChild(5)), null);

			// Now merge. Before merging we set the head for merging to master.
			Ref remoteMasterRef = git.getRepository().getRef("refs/remotes/" + remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$

			final MergeCommand mergeCommand = git.merge().include(remoteMasterRef);

			mergeCommand.call();
		} catch (TransportException e) {
			if (e.getMessage().startsWith("Remote does not have ")) { //$NON-NLS-1$
				// just means that the remote branch isn't set up yet (and thus nothing too fetch). Can be ignored.
			} else {
				throw new RemoteSyncException(e);
			}
		} catch (IOException e) {
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

	/**
	 * Set the file filter used. This method allows file filtering behavior to be changed after instance is created, which is
	 * necessary to support user changes to the filter.
	 * 
	 * @param sff
	 */
	public void setFileFilter(AbstractSyncFileFilter sff) {
		fileFilter = (GitSyncFileFilter)sff;
	}

	/**
	 * Synchronize local and remote repositories. Currently, this function just interleaves the work of "syncLocalToRemote" and
	 * "syncRemoteToLocal". Doing both simultaneously, though, gives room for more efficient code later. (See "syncInternal"
	 * for the actual implementation.)
	 * 
	 * Note that the remote is fetched and merged first. This is on purpose so that merge conflicts will occur locally, where
	 * they can be more easily managed. Previously, "syncLocalToRemote" was called first in "AbstractSynchronizeService", which
	 * would
	 * cause merge conflicts to occur remotely.
	 * 
	 * 
	 * @param monitor
	 * @param includeUntrackedFiles
	 *            Should currently untracked remote files be added to the repository?
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. The specific exception is
	 *             nested within the RemoteSyncException. Many of the listed
	 *             exceptions appear to be unrecoverable, caused by errors in
	 *             the initial setup. It is vital, though, that failed syncs are
	 *             reported and handled. So all exceptions are checked
	 *             exceptions, embedded in a RemoteSyncException.
	 */
	public void sync(IProgressMonitor monitor, boolean includeUntrackedFiles) throws RemoteSyncException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			// Commit local and remote changes
			subMon.subTask(Messages.GitRemoteSyncConnection_12);
			doCommit(subMon.newChild(5));
			subMon.subTask(Messages.GitRemoteSyncConnection_14);
			commitRemoteFiles(subMon.newChild(18));

			try {
				// Fetch the remote repository
				subMon.subTask(Messages.GitRemoteSyncConnection_15);
				transport.fetch(new EclipseGitProgressTransformer(subMon.newChild(18)), null);

				// Merge it with local
				subMon.subTask(Messages.GitRemoteSyncConnection_16);
				Ref remoteMasterRef = git.getRepository().getRef("refs/remotes/" + remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
				final MergeCommand mergeCommand = git.merge().include(remoteMasterRef);
				mergeCommand.call();

				// Handle merge conflict. Read in data needed to resolve the conflict, and then reset the repo.
				readMergeConflictFiles();
				if (!FileToMergePartsMap.isEmpty()) {
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
			if (git.branchList().call().size() > 0) { // check whether master was already created
				subMon.subTask(Messages.GitRemoteSyncConnection_17);
				transport.push(new EclipseGitProgressTransformer(subMon.newChild(18)), null);

				// Now remotely merge changes with master branch
				CommandResults mergeResults;
				// ff-only prevents accidental corruption of the remote repository but is supported only in recent Git versions.
				// final String command = gitCommand + " merge --ff-only " + remotePushBranch; //$NON-NLS-1$
				final String command = gitCommand() + " merge " + remotePushBranch; //$NON-NLS-1$

				subMon.subTask(Messages.GitRemoteSyncConnection_18);
				mergeResults = this.executeRemoteCommand(command, subMon.newChild(18));
				if (mergeResults.getExitCode() != 0) {
					throw new RemoteSyncException(new RemoteExecutionException(Messages.GRSC_GitMergeFailure
							+ mergeResults.getStderr()));
				}
			}
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		} catch (final InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
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

	public int getRemoteGitVersion(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException,
			MissingConnectionException {
		String command = gitCommand() + " --version"; //$NON-NLS-1$
		CommandResults commandResults = null;

		try {
			// Skip class execute function, which attempts to run the command in the remote directory.
			// (This directory may not yet exist, since reading the Git version is one of the first operations.)
			IRemoteConnection conn = syncConfig.getRemoteConnection();
			commandResults = CommandRunner.executeRemoteCommand(conn, command, null, monitor);
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException(Messages.GRSC_GitInitFailure + commandResults.getStderr());
		}

		Matcher m = Pattern.compile("git version ([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.?([0-9]*)").matcher(commandResults.getStdout().trim()); //$NON-NLS-1$

		if (m.matches()) {
			int patch=0;
			if (m.group(4).length()>0) patch=Integer.parseInt(m.group(4));
			return Integer.parseInt(m.group(1)) * 1000000 + Integer.parseInt(m.group(2)) * 10000 + Integer.parseInt(m.group(3)) * 100 + patch;
		} else {
			return 0;
		}
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
		checkoutCommand.setStartPoint("refs/remotes/" + remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			checkoutCommand.call();
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		}

		doRefresh(project, null);
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

	private CommandResults executeRemoteCommand(String command, IProgressMonitor monitor) throws RemoteSyncException, IOException,
			InterruptedException, RemoteConnectionException, MissingConnectionException {
		IRemoteConnection conn = syncConfig.getRemoteConnection();
		String remoteDirectory = syncConfig.getLocation(project);
		return CommandRunner.executeRemoteCommand(conn, command, remoteDirectory, monitor);
	}

	private CommandResults executeRemoteCommand(List<String> command, IProgressMonitor monitor) throws RemoteSyncException,
			IOException, InterruptedException, RemoteConnectionException, MissingConnectionException {
		IRemoteConnection conn = syncConfig.getRemoteConnection();
		String remoteDirectory = syncConfig.getLocation(project);
		return CommandRunner.executeRemoteCommand(conn, command, remoteDirectory, monitor);
	}

	// Get the base git command for this system, includes the git binary plus sync-specific arguments.
	private String gitCommand() {
		String gitBinary = "git"; //$NON-NLS-1$
		IScopeContext context = InstanceScope.INSTANCE;
		Preferences prefSyncNode = context.getNode(instanceScopeSyncNode);
		if (prefSyncNode == null) {
			Activator.log(Messages.GitRemoteSyncConnection_28);
		} else {
			try {
				// Avoid creating node if it doesn't exist
				if (prefSyncNode.nodeExists(GIT_LOCATION_NODE_NAME)) {
					Preferences prefGitNode = prefSyncNode.node(GIT_LOCATION_NODE_NAME);
					gitBinary = prefGitNode.get(syncConfig.getRemoteConnection().getName(), "git"); //$NON-NLS-1$
				}
			} catch (BackingStoreException e) {
				Activator.log(Messages.GitRemoteSyncConnection_29, e);
			} catch (MissingConnectionException e) {
				// nothing to do
			}
		}

		return gitBinary + " " + gitArgs; //$NON-NLS-1$
	}
}