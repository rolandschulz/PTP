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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.NullProgressMonitor;
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
import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * 
 * This class implements a remote sync tool using git, as accessed through the jgit library.
 * 
 */
public class GitRemoteSyncConnection {
	private final static String remoteProjectName = "eclipse_auto"; //$NON-NLS-1$
	private final static String commitMessage = "Eclipse Automatic Commit"; //$NON-NLS-1$
	private final static String remotePushBranch = "ptp-push"; //$NON-NLS-1$
	private final IRemoteConnection connection;
	private final String localDirectory;
	private final String remoteDirectory;
	private Git git;
	private TransportGitSsh transport;

	/**
	 * Create a remote sync connection using git. Assumes that the local directory exists but not necessarily the remote directory.
	 * It is created if not.
	 * 
	 * @param conn
	 * @param localDir
	 * @param remoteDir
	 * @throws RemoteSyncException
	 *             on problems building the remote repository. Specific exception nested. Upon such an exception, the instance is
	 *             invalid and should not be used.
	 */
	public GitRemoteSyncConnection(IRemoteConnection conn, String localDir, String remoteDir) throws RemoteSyncException {
		connection = conn;
		localDirectory = localDir;
		remoteDirectory = remoteDir;

		// Build repo, creating it if it is not already present.
		try {
			buildRepo();
		} catch (final CoreException e) {
			throw new RemoteSyncException(e);
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		} catch (final RemoteExecutionException e) {
			throw new RemoteSyncException(e);
		}

		// Build transport
		final RemoteConfig remoteConfig = buildRemoteConfig(git.getRepository().getConfig());
		buildTransport(remoteConfig);
	}

	/**
	 * Builds the remote configuration for the connection, setting up fetch and push operations between local and remote master
	 * branches.
	 * 
	 * @param config
	 *            configuration for the local repository
	 * @return the remote configuration
	 * @throws RuntimeException
	 *             if the URI in the passed configuration is not properly formatted.
	 */
	private RemoteConfig buildRemoteConfig(StoredConfig config) {
		RemoteConfig rconfig = null;

		try {
			rconfig = new RemoteConfig(config, remoteProjectName);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final RefSpec refSpecFetch = new RefSpec("+refs/heads/master:refs/remotes/" +  //$NON-NLS-1$
												                      remoteProjectName + "/master"); //$NON-NLS-1$
		final RefSpec refSpecPush = new RefSpec("+master:" + remotePushBranch); //$NON-NLS-1$
		rconfig.addFetchRefSpec(refSpecFetch);
		rconfig.addPushRefSpec(refSpecPush);

		return rconfig;
	}

	/**
	 * 
	 * @param localDirectory
	 * @param remoteHost
	 * @return the repository
	 * @throws CoreException
	 *             on problems creating the remote directory.
	 * @throws IOException
	 *             on problems writing to the file system.
	 * @throws RemoteExecutionException
	 *             on failure to run remote commands.
	 * @throws RemoteSyncException
	 *             on problems with initial local commit.
	 * TODO: Consider the consequences of exceptions that occur at various points, which can leave the repo in a partial state.
	 * 		   For example, if the repo is created but the initial commit fails.
	 * TODO: Consider evaluating the output of "git init".
	 */
	private Git buildRepo() throws CoreException, IOException, RemoteExecutionException, RemoteSyncException {
		final File localDir = new File(localDirectory);
		final FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		Repository repository = null;

		try {
			repository = repoBuilder.setWorkTree(localDir).build();
		} catch (final IOException e) {
			throw e;
		}
		git = new Git(repository);

		// Create and configure local repository if it is not already present. Set the git instance.
		if (repoReady() == false) {
			try {
				repository.create(false);
			} catch (final IOException e) {
				throw e;
			}

			// An initial commit to create the master branch.
			try {
				doCommit();
			} catch (final RemoteSyncException e) {
				throw e;
			}
		}

		// Create remote directory if necessary.
		try {
			CommandRunner.createRemoteDirectory(connection, remoteDirectory);
		} catch (final CoreException e) {
			throw e;
		}
		
		// Initialize remote directory if necessary
		try {
			doRemoteInit();
		} catch (final IOException e) {
			throw e;
		} catch (final RemoteExecutionException e) {
			throw e;
		}

		// Commit remote files if necessary
		try {
			doRemoteCommit();
		} catch (final IOException e) {
			throw e;
		} catch (final RemoteExecutionException e) {
			throw e;
		}

		return new Git(repository);
	}
	
	/**
	* Create and configure remote repository if it is not already present. Note that "git init" is "safe" on a repo already
	* created, so we can simply rerun it each time.
	* @throws IOException
	* @throws RemoteExecutionException
	*/
	private void doRemoteInit() throws IOException, RemoteExecutionException {
		String command = null;
		CommandResults commandResults = null;

		command = "git init"; //$NON-NLS-1$
		try {
			commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}

		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git init failed with message: " + //$NON-NLS-1$
																						commandResults.getStderr());
		}
	}
	
	/*
	 * Do a commit on the remote repository. First we add and delete files from the git index as needed, and then we call "git commit"
	 * TODO: Modified files already added by "git add" will not be found by "getRemoteFileStatus". Thus, a commit may not happen even
	 * though there are outstanding changes. Note that this can only occur by accessing the repo outside of Eclipse.
	 */
	private void doRemoteCommit() throws IOException, RemoteExecutionException {
		Set<String> filesToAdd = new HashSet<String>();
		Set<String> filesToDelete = new HashSet<String>();
		boolean needToCommit = false;
		
		try {
			getRemoteFileStatus(filesToAdd, filesToDelete);
			for (String fileName : filesToDelete) {
				if (filesToAdd.contains(fileName)) {
					filesToAdd.remove(fileName);
				}
			}
			if (filesToAdd.size() > 0) {
				addRemoteFiles(filesToAdd);
				needToCommit = true;
			}
			if (filesToDelete.size() > 0) {
				deleteRemoteFiles(filesToDelete);
				needToCommit = true;
			}
			
			if (needToCommit) {
				commitRemoteFiles();
			}
		} catch (IOException e) {
			throw e;
		} catch (RemoteExecutionException e) {
			throw e;
		}
	}
	
	/*
	 * Do a "git commit" on the remote host
	 */
	private void commitRemoteFiles() throws IOException, RemoteExecutionException {
		final String command = "git commit -m \"" + commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		CommandResults commandResults = null;
		
		try {
			commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}
		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git commit failed with message: " + //$NON-NLS-1$
																						 commandResults.getStderr());
		}
	}

	/*
	 * Do a "git rm <Files>" on the remote host
	 */
	private void deleteRemoteFiles(Set<String> filesToDelete) throws IOException, RemoteExecutionException {
		String command = "git rm"; //$NON-NLS-1$
		for (String fileName : filesToDelete) {
			command = command.concat(" "); //$NON-NLS-1$
			command = command.concat(fileName);
		}
		
		CommandResults commandResults = null;
		try {
			commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}
		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git rm failed with message: " + //$NON-NLS-1$
																						 commandResults.getStderr());
		}
	}

	/*
	 * Do a "git add <Files>" on the remote host
	 */
	private void addRemoteFiles(Set<String> filesToAdd) throws IOException, RemoteExecutionException {
		String command = "git add"; //$NON-NLS-1$
		for (String fileName : filesToAdd) {
			command = command.concat(" "); //$NON-NLS-1$
			command = command.concat(fileName);
		}
		
		CommandResults commandResults = null;
		try {
			commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}
		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git add failed with message: " + //$NON-NLS-1$
																						 commandResults.getStderr());
		}
	}

	/*
	 * Use "git ls-files" to obtain a list of files that need to be added or deleted from the git index. 
	 */
	private void getRemoteFileStatus(Set<String> filesToAdd, Set<String> filesToDelete)
																					throws IOException, RemoteExecutionException {
		final String command = "git ls-files -t --modified --others --deleted"; //$NON-NLS-1$
		CommandResults commandResults = null;
		
		try {
			commandResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}
		if (commandResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git ls-files failed with message: " + //$NON-NLS-1$
																						 commandResults.getStderr());
		}
		
		BufferedReader statusReader = new BufferedReader(new StringReader(commandResults.getStdout()));
		String line = null;
		while ((line = statusReader.readLine()) != null) {
			String[] lineParts = line.split("\\s+"); //$NON-NLS-1$
			if (lineParts.length < 2) {
				continue;
			}
			if (lineParts[0].startsWith("R")) { //$NON-NLS-1$
				filesToDelete.add(lineParts[1]);
			} else {
				filesToAdd.add(lineParts[1]);
			}
		}
		statusReader.close();
	}

	// Subclass JGit's generic RemoteSession to set up running of remote commands using the available process builder.
	public class PTPSession implements RemoteSession {
		private URIish uri;

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
	 * Creates the transport object that JGit uses for executing commands remotely.
	 * 
	 * @param remoteConfig
	 * 				the remote configuration for our local Git repo
	 * @return the transport
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
			public RemoteSession getSession(URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms) throws TransportException {
				return new PTPSession(uri);
			}
		});

		transport.applyConfig(remoteConfig);
	}

	/**
	 * Build the URI for the remote host as needed by the transport. Since the transport will use an external SSH session, we do
	 * not need to provide user, host, or password. However, the function for opening a transport throws an exception if the host
	 * is null or empty length. So we set it to a dummy string.
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
	 * Commits files in working directory. For now, we just commit all files. So adding ".", handles all files, including newly
	 * created files, and setting the all flag (-a) ensures that deleted files are updated.
	 * TODO: Figure out how to do this more efficiently, as was done remotely (using git ls-files)
	 * 
	 * @throws RemoteSyncException
	 *             on problems committing.
	 */
	private void doCommit() throws RemoteSyncException {
		final AddCommand addCommand = git.add();
		addCommand.addFilepattern("."); //$NON-NLS-1$
		try {
			addCommand.call();
		} catch (final NoFilepatternException e) {
			throw new RemoteSyncException(e);
		}

		final CommitCommand commitCommand = git.commit();
		commitCommand.setAll(true);
		commitCommand.setMessage(commitMessage);

		try {
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
	 * @return If the repo has actually been initialized
	 * TODO: Consider the ways this could go wrong. What if the directory name already ends in a slash? What if ".git" is a file or
	 * does not contain the appropriate files?
	 */
	private boolean repoReady() {
		final String repoDirectory = localDirectory + "/.git"; //$NON-NLS-1$
		final File repoDir = new File(repoDirectory);
		return repoDir.exists();
	}

	/**
	 * Many of the listed exceptions appear to be unrecoverable, caused by errors in the initial setup. It is vital, though, that
	 * failed syncs are reported and handled. So all exceptions are checked exceptions, embedded in a RemoteSyncException.
	 * 
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. The specific exception is nested within the RemoteSyncException. 
	 * TODO: Consider possible platform dependency.
	 */
	public void syncLocalToRemote() throws RemoteSyncException {
		// First commit changes to the local repository.
		try {
			doCommit();
		} catch (final RemoteSyncException e) {
			throw e;
		}

		// Then push them to the remote site.
		try {
			transport.push(NullProgressMonitor.INSTANCE, null);
		} catch (final NotSupportedException e) {
			throw new RemoteSyncException(e);
		} catch (final TransportException e) {
			throw new RemoteSyncException(e);
		}

		// Now remotely merge changes with master branch
		CommandResults mergeResults;
		final String command = "git merge " + remotePushBranch; //$NON-NLS-1$
		try {
			mergeResults = CommandRunner.executeRemoteCommand(connection, command, remoteDirectory, null);
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		} catch (final InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		}

		if (mergeResults.getExitCode() != 0) {
			throw new RemoteSyncException(new RemoteExecutionException("Remote merge failed with message: " +  //$NON-NLS-1$
																									mergeResults.getStderr()));
		}
	}

	/**
	 * @throws RemoteSyncException
	 *             for various problems sync'ing. The specific exception is nested within the RemoteSyncException. Many of the
	 *             listed exceptions appear to be unrecoverable, caused by errors in the initial setup. It is vital, though, that
	 *             failed syncs are reported and handled. So all exceptions are checked exceptions, embedded in a
	 *             RemoteSyncException.
	 */
	public void syncRemoteToLocal() throws RemoteSyncException {

		// TODO: Figure out why pull doesn't work and why we have to fetch and merge instead.
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

		// First, commit in case any changes have occurred remotely.
		try {
			doRemoteCommit();
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteExecutionException e) {
			throw new RemoteSyncException(e);
		}
		
		// Next, fetch the remote repository
		try {
			transport.fetch(NullProgressMonitor.INSTANCE, null);
		} catch (final NotSupportedException e) {
			throw new RemoteSyncException(e);
		} catch (final TransportException e) {
			throw new RemoteSyncException(e);
		}

		// Now merge. Before merging we set the head for merging to master.
		Ref masterRef = null;
		try {
			masterRef = git.getRepository().getRef("refs/remotes/" + remoteProjectName + "/master"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		}

		final MergeCommand mergeCommand = git.merge().include(masterRef);
		try {
			mergeCommand.call();
		} catch (final NoHeadException e) {
			throw new RemoteSyncException(e);
		} catch (final ConcurrentRefUpdateException e) {
			throw new RemoteSyncException(e);
		} catch (final CheckoutConflictException e) {
			throw new RemoteSyncException(e);
		} catch (final InvalidMergeHeadsException e) {
			throw new RemoteSyncException(e);
		} catch (final WrongRepositoryStateException e) {
			throw new RemoteSyncException(e);
		} catch (final NoMessageException e) {
			throw new RemoteSyncException(e);
		}
	}
}
