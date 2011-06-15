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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.ptp.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * 
 * This class implements a remote sync tool using git, as accessed through the
 * jgit library.
 * 
 */
public class GitRemoteSyncConnection {

	private final int MAX_FILES = 100;
	private final static String remoteProjectName = "eclipse_auto"; //$NON-NLS-1$
	private final static String commitMessage = Messages.GRSC_CommitMessage;
	private final static String remotePushBranch = "ptp-push"; //$NON-NLS-1$
	private final IRemoteConnection connection;
	private final SyncFileFilter fileFilter;
	private final String localDirectory;
	private final String remoteDirectory;
	private Git git;
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
	 */
	public GitRemoteSyncConnection(IRemoteConnection conn, String localDir, String remoteDir, SyncFileFilter filter,
			IProgressMonitor monitor) throws RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		try {
			connection = conn;
			fileFilter = filter;
			localDirectory = localDir;
			remoteDirectory = remoteDir;

			// Build repo, creating it if it is not already present.
			try {
				buildRepo(subMon.newChild(10));
			} catch (final IOException e) {
				throw new RemoteSyncException(e);
			} catch (final RemoteExecutionException e) {
				throw new RemoteSyncException(e);
			}

			// Build transport
			final RemoteConfig remoteConfig = buildRemoteConfig(git.getRepository().getConfig());
			buildTransport(remoteConfig);
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
	 *             on problems with initial local commit. TODO: Consider the
	 *             consequences of exceptions that occur at various points,
	 *             which can leave the repo in a partial state. For example, if
	 *             the repo is created but the initial commit fails. TODO:
	 *             Consider evaluating the output of "git init".
	 */
	private Git buildRepo(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		subMon.subTask(Messages.GitRemoteSyncConnection_building_repo);
		try {
			final File localDir = new File(localDirectory);
			final FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
			Repository repository = repoBuilder.setWorkTree(localDir).build();
			git = new Git(repository);

			// Create and configure local repository if it is not already
			// present. Set the git instance.
			if (repoReady() == false) {
				repository.create(false);

				// An initial commit to create the master branch.
				doCommit();
			}

			// Create remote directory if necessary.
			try {
				CommandRunner.createRemoteDirectory(connection, remoteDirectory, subMon.newChild(5));
			} catch (final CoreException e) {
				throw new RemoteSyncException(e);
			}

			// Initialize remote directory if necessary
			boolean existingGitRepo = doRemoteInit(subMon.newChild(5));

			// Prepare remote site for committing (stage files using git) and
			// then commit remote files if necessary
			// Include untracked files for new git
			boolean needToCommitRemote = prepareRemoteForCommit(subMon.newChild(90), !existingGitRepo);
			// repos
			if (needToCommitRemote) {
				commitRemoteFiles(subMon.newChild(5));
			}

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
	 * @return whether or not this repo already existed
	 */
	private boolean doRemoteInit(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		try {
			String command = "git init"; //$NON-NLS-1$
			CommandResults commandResults = null;

			try {
				commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, subMon.newChild(10));
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
	 * Run "git add" and "git rm" as needed to prepare remote repo for commit.
	 * Return whether or not anything needs to be committed. TODO: Modified
	 * files already added by "git add" will not be found by
	 * "getRemoteFileStatus". Thus, this may return false even though there are
	 * outstanding changes. Note that this can only occur by accessing the repo
	 * outside of Eclipse.
	 * 
	 * @return whether or not there are changes to be committed.
	 */
	private boolean prepareRemoteForCommit(IProgressMonitor monitor) throws IOException, RemoteExecutionException,
			RemoteSyncException {
		return prepareRemoteForCommit(monitor, false); // Default to not
														// including untracked
														// files
	}

	private boolean prepareRemoteForCommit(IProgressMonitor monitor, boolean includeUntrackedFiles) throws IOException,
			RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			Set<String> filesToAdd = new HashSet<String>();
			Set<String> filesToDelete = new HashSet<String>();
			boolean needToCommit = false;

			getRemoteFileStatus(filesToAdd, filesToDelete, subMon.newChild(5), includeUntrackedFiles);
			for (String fileName : filesToDelete) {
				if (filesToAdd.contains(fileName)) {
					filesToAdd.remove(fileName);
				}
			}
			if (filesToAdd.size() > 0) {
				addRemoteFiles(filesToAdd, subMon.newChild(90));
				needToCommit = true;
			}
			if (filesToDelete.size() > 0) {
				deleteRemoteFiles(filesToDelete, subMon.newChild(5));
				needToCommit = true;
			}

			return needToCommit;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * Do a "git commit" on the remote host
	 */
	private void commitRemoteFiles(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_committing_remote);
		try {
			final String command = "git commit -m \"" + commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			CommandResults commandResults = null;

			try {
				commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, subMon.newChild(10));
			} catch (final InterruptedException e) {
				throw new RemoteExecutionException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteExecutionException(e);
			}
			if (commandResults.getExitCode() != 0) {
				throw new RemoteExecutionException(Messages.GRSC_GitCommitFailure + commandResults.getStderr());
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * Do a "git rm <Files>" on the remote host
	 */
	private void deleteRemoteFiles(Set<String> filesToDelete, IProgressMonitor monitor) throws IOException,
			RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		try {
			List<String> command = stringToList("git rm"); //$NON-NLS-1$
			for (String fileName : filesToDelete) {
				command.add(fileName);
			}

			CommandResults commandResults = null;
			try {
				commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, subMon.newChild(10));
			} catch (final InterruptedException e) {
				throw new RemoteExecutionException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteExecutionException(e);
			}
			if (commandResults.getExitCode() != 0) {
				throw new RemoteExecutionException(Messages.GRSC_GitRmFailure + commandResults.getStderr());
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

	/*
	 * Do a "git add <Files>" on the remote host
	 */
	private void addRemoteFiles(Set<String> filesToAdd, IProgressMonitor monitor) throws IOException, RemoteExecutionException,
			RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, filesToAdd.size());
		subMon.subTask(Messages.GitRemoteSyncConnection_adding_files);
		try {
			while (!filesToAdd.isEmpty()) {
				List<String> commandList = stringToList("git add"); //$NON-NLS-1$
				int count = 1;
				for (String fileName : filesToAdd.toArray(new String[0])) {
					if (count++ % MAX_FILES == 0) {
						break;
					}
					commandList.add(fileName);
					filesToAdd.remove(fileName);
				}

				CommandResults commandResults = null;
				try {
					commandResults = CommandRunner.executeRemoteCommand(connection, commandList, remoteDirectory,
							subMon.newChild(10));
				} catch (final InterruptedException e) {
					throw new RemoteExecutionException(e);
				} catch (RemoteConnectionException e) {
					throw new RemoteExecutionException(e);
				}
				if (commandResults.getExitCode() != 0) {
					throw new RemoteExecutionException(Messages.GRSC_GitAddFailure + commandResults.getStderr());
				}
				monitor.worked(count);
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * Use "git ls-files" to obtain a list of files that need to be added or
	 * deleted from the git index.
	 */
	private void getRemoteFileStatus(Set<String> filesToAdd, Set<String> filesToDelete, IProgressMonitor monitor,
			boolean includeUntrackedFiles) throws IOException, RemoteExecutionException, RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_getting_remote_file_status);
		try {
			final String command;
			if (includeUntrackedFiles) {
				command = "git ls-files -t --modified --others --deleted"; //$NON-NLS-1$
			} else {
				command = "git ls-files -t --modified --deleted"; //$NON-NLS-1$
			}
			CommandResults commandResults = null;

			try {
				commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, subMon.newChild(10));
			} catch (final InterruptedException e) {
				throw new RemoteExecutionException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteExecutionException(e);
			}
			if (commandResults.getExitCode() != 0) {
				throw new RemoteExecutionException(Messages.GRSC_GitLsFilesFailure + commandResults.getStdout());
			}

			BufferedReader statusReader = new BufferedReader(new StringReader(commandResults.getStdout()));
			String line = null;
			while ((line = statusReader.readLine()) != null) {
				if (line.charAt(0) == ' ' || line.charAt(1) != ' ' || line.charAt(2) == ' ') {
					continue;
				}
				char status = line.charAt(0);
				String fn = line.substring(2);
				if (status == 'R') {
					filesToDelete.add(fn);
				} else if (!fn.equals(".cproject") && !fn.equals(".project") && !fn.startsWith(".settings")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					filesToAdd.add(fn);
				}
			}
			statusReader.close();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * Use "git ls-files" to obtain a list of files that need to be added or
	 * deleted from the git index.
	 */
	private void getFileStatus(Set<String> filesToAdd, Set<String> filesToDelete, boolean includeUntrackedFiles)
			throws RemoteSyncException {
		StatusCommand statusCommand = git.status();
		Status status;
		try {
			status = statusCommand.call();
			filesToAdd.addAll(status.getAdded());
			filesToAdd.addAll(status.getModified());
			if (includeUntrackedFiles) {
				filesToAdd.addAll(status.getUntracked());
			}
			filesToDelete.addAll(status.getMissing());
		} catch (NoWorkTreeException e) {
			throw new RemoteSyncException(e);
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		}

		Set<String> filesToBeIgnored = new HashSet<String>();
		for (String fileName : filesToAdd) {
			if (fileFilter.shouldIgnore(fileName)) {
				filesToBeIgnored.add(fileName);
			}
		}
		filesToAdd.removeAll(filesToBeIgnored);
	}

	// Subclass JGit's generic RemoteSession to set up running of remote
	// commands using the available process builder.
	public class PTPSession implements RemoteSession {
		private final URIish uri;

		public PTPSession(URIish uri) {
			this.uri = uri;
		}

		public Process exec(String command, int timeout) throws TransportException {
			// TODO: Use a library for command splitting.
			List<String> commandList = new LinkedList<String>();
			commandList.add("sh"); //$NON-NLS-1$
			commandList.add("-c"); //$NON-NLS-1$
			commandList.add(command);

			try {
				if (!connection.isOpen()) {
					connection.open(null);
				}
				return (AbstractRemoteProcess) connection.getRemoteServices().getProcessBuilder(connection, commandList).start();
			} catch (IOException e) {
				throw new TransportException(uri, e.getMessage(), e);
			} catch (RemoteConnectionException e) {
				throw new TransportException(uri, e.getMessage(), e);
			}

		}

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
	private void buildTransport(RemoteConfig remoteConfig) {
		final URIish uri = buildURI();
		try {
			transport = (TransportGitSsh) Transport.open(git.getRepository(), uri);
		} catch (NotSupportedException e) {
			throw new RuntimeException(e);
		} catch (TransportException e) {
			throw new RuntimeException(e);
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
				.setPath(remoteDirectory);
	}

	public void close() {
		transport.close();
	}

	/**
	 * Commits files in working directory. For now, we just commit all files. So
	 * adding ".", handles all files, including newly created files, and setting
	 * the all flag (-a) ensures that deleted files are updated. TODO: Figure
	 * out how to do this more efficiently, as was done remotely (using git
	 * ls-files)
	 * 
	 * @throws RemoteSyncException
	 *             on problems committing.
	 */
	private void doCommit() throws RemoteSyncException {
		Set<String> filesToAdd = new HashSet<String>();
		Set<String> filesToRemove = new HashSet<String>();
		this.getFileStatus(filesToAdd, filesToRemove, true);

		try {
			if (!(filesToAdd.isEmpty())) {
				final AddCommand addCommand = git.add();
				for (String fileName : filesToAdd) {
					addCommand.addFilepattern(fileName);
				}
				addCommand.call();
			}

			if (!(filesToRemove.isEmpty())) {
				final RmCommand rmCommand = git.rm();
				for (String fileName : filesToRemove) {
					rmCommand.addFilepattern(fileName);
				}
				rmCommand.call();
			}

			final CommitCommand commitCommand = git.commit();
			commitCommand.setMessage(commitMessage);
			commitCommand.call();
		} catch (final GitAPIException e) {
			throw new RemoteSyncException(e);
		} catch (final UnmergedPathException e) {
			throw new RemoteSyncException(e);
		}
	}

	/**
	 * @return the connection (IRemoteConnection)
	 */
	public IRemoteConnection getConnection() {
		return connection;
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
		return remoteDirectory;
	}

	/**
	 * 
	 * @param localDirectory
	 * @return If the repo has actually been initialized TODO: Consider the ways
	 *         this could go wrong. What if the directory name already ends in a
	 *         slash? What if ".git" is a file or does not contain the
	 *         appropriate files?
	 */
	private boolean repoReady() {
		final String repoDirectory = localDirectory + "/.git"; //$NON-NLS-1$
		final File repoDir = new File(repoDirectory);
		return repoDir.exists();
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
	 *             nested within the RemoteSyncException. TODO: Consider
	 *             possible platform dependency.
	 */
	public void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_sync_local_to_remote);
		try {
			// First commit changes to the local repository.
			doCommit();

			// Then push them to the remote site.
			try {
				transport.push(new EclipseGitProgressTransformer(subMon.newChild(5)), null);

				// Now remotely merge changes with master branch
				CommandResults mergeResults;
				final String command = "git merge " + remotePushBranch; //$NON-NLS-1$

				mergeResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, subMon.newChild(5));

				if (mergeResults.getExitCode() != 0) {
					throw new RemoteSyncException(new RemoteExecutionException(Messages.GRSC_GitMergeFailure
							+ mergeResults.getStdout()));
				}
			} catch (final IOException e) {
				throw new RemoteSyncException(e);
			} catch (final InterruptedException e) {
				throw new RemoteSyncException(e);
			} catch (RemoteConnectionException e) {
				throw new RemoteSyncException(e);
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
	public void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException {

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
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		subMon.subTask(Messages.GitRemoteSyncConnection_sync_remote_to_local);
		try {
			// First, commit in case any changes have occurred remotely.
			prepareRemoteForCommit(subMon.newChild(5));

			// Next, fetch the remote repository
			transport.fetch(new EclipseGitProgressTransformer(subMon.newChild(5)), null);

			// Now merge. Before merging we set the head for merging to master.
			Ref masterRef = git.getRepository().getRef("refs/remotes/" + remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$

			final MergeCommand mergeCommand = git.merge().include(masterRef);

			mergeCommand.call();
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (GitAPIException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteExecutionException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
