/*******************************************************************************
 * Copyright (c) 2011 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.ptp.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.remotetools.core.RemoteToolsConnection;

import com.jcraft.jsch.Session;

/**
 * 
 * This class implements a remote sync tool using git, as accessed through the jgit library.
 * 
 */
public class GitRemoteSyncConnection implements IRemoteSyncConnection {
	private final static String remoteProjectName = "eclipse_auto"; //$NON-NLS-1$
	private final static String commitMessage = "Eclipse Automatic Commit"; //$NON-NLS-1$
	private final static String remotePushBranch = "ptp-push"; //$NON-NLS-1$
	private final IRemoteConnection connection;
	private final String localDirectory;
	private final String remoteDirectory;
	private Git git;
	private final Transport transport;

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
		transport = buildTransport(remoteConfig);
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

		final RefSpec refSpecFetch = new RefSpec("+refs/heads/master:refs/remotes/" + //$NON-NLS-1$
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
	 *             on problems writing to the file system
	 * @throws RemoteExecutionException
	 *             on failure to run "git init" command.
	 * @throws RemoteSyncException
	 *             on problems with initial commit. TODO: Consider the consequences of exceptions that occur at various points,
	 *             which can leave the repo in a partial state. For example, if the repo is created but the initial commit fails.
	 *             TODO: Consider evaluating the output of "git init". Thus, calling this method again will not help and the
	 *             repository is not usable. Thus, we need smarter error handling in that case.
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

		// Create remote directory if needed.
		try {
			CommandRunner.createRemoteDirectory(connection, remoteDirectory);
		} catch (final CoreException e) {
			throw e;
		}

		// Create and configure remote repository if it is not already present. Note that "git init" is "safe" on a repo already
		// created, so we can simply rerun it each time.
		final String command = "git init " + remoteDirectory; //$NON-NLS-1$
		CommandResults gitInitResults;
		try {
			gitInitResults = CommandRunner.executeRemoteCommand(connection, command);
		} catch (final IOException e) {
			throw e;
		} catch (final InterruptedException e) {
			throw new RemoteExecutionException(e);
		}

		if (gitInitResults.getExitCode() != 0) {
			throw new RemoteExecutionException("remote git init failed with message: " + //$NON-NLS-1$
					gitInitResults.getStderr());
		}

		return new Git(repository);
	}

	/**
	 * Creates the transport object used for all communication between the local and remote host for the connection.
	 * 
	 * @param remoteHost
	 * @return the transport
	 * @throws RuntimeException
	 *             if the requested transport is not supported by JGit.
	 */
	private Transport buildTransport(RemoteConfig remoteConfig) {
		final URIish uri = buildURI();
		SshTransport transport = null;

		try {
			transport = (SshTransport) Transport.open(git.getRepository(), uri);
		} catch (final NotSupportedException e) {
			throw new RuntimeException(e);
		}

		// Set transport to use the already available SSH session rather than creating a new one.
		transport.setSshSessionFactory(new SshSessionFactory() {
			@Override
			public Session getSession(String user, String pass, String host, int port, CredentialsProvider credentialsProvider,
					FS fs) {
				return ((RemoteToolsConnection) connection).getSession();
			}
		});

		transport.applyConfig(remoteConfig);

		return transport;
	}

	/**
	 * Build the URI for the remote host as needed by the transport. Since the transport will use an external SSH session, we do not
	 * need to provide user, host, or password. However, the function for opening a transport throws an exception if the host null.
	 * So we set it to the empty string.
	 * 
	 * @return URIish
	 */
	private URIish buildURI() {
		return new URIish()
				// .setUser(connection.getUsername())
				.setHost("") //$NON-NLS-1$
				// .setPass("")
				.setScheme("ssh") //$NON-NLS-1$
				.setPath(remoteDirectory);
	}

	@Override
	public void close() {
		transport.close();
	}

	/**
	 * Commits files in working directory. For now, we just commit all files. So adding ".", handles all files, including newly
	 * created files, and setting the all flag (-a) ensures that deleted files are updated.
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
	@Override
	public IRemoteConnection getConnection() {
		return connection;
	}

	/**
	 * @return the localDirectory
	 */
	@Override
	public String getLocalDirectory() {
		return localDirectory;
	}

	/**
	 * @return the remoteDirectory
	 */
	@Override
	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	/**
	 * 
	 * @param localDirectory
	 * @return If the repo has actually been initialized TODO: Consider the ways this could go wrong. What if the directory name
	 *         already ends in a slash? What if ".git" is a file or does not contain the appropriate files?
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
	 *             for various problems sync'ing. The specific exception is nested within the RemoteSyncException. TODO: Consider
	 *             possible platform dependency. TODO: See if we can change to working directory some other way than using the "cd"
	 *             command.
	 */
	@Override
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
		final String command = "cd " + remoteDirectory + "; git merge " + remotePushBranch; //$NON-NLS-1$ //$NON-NLS-2$
		try {
			mergeResults = CommandRunner.executeRemoteCommand(connection, command);
		} catch (final IOException e) {
			throw new RemoteSyncException(e);
		} catch (final InterruptedException e) {
			throw new RemoteSyncException(e);
		}

		if (mergeResults.getExitCode() != 0) {
			throw new RemoteSyncException(new RemoteExecutionException("Remote merge failed with message: " + //$NON-NLS-1$
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
	@Override
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

		// First, fetch the remote repository
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
