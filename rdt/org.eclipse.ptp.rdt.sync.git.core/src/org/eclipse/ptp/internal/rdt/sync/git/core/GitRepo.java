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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.internal.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteLocation;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Class for a "remote" (external) Git repository - a repository synchronized to a local Eclipse project and handled by executing
 * C git commands on the host.
 */
public class GitRepo {
	public static final String gitArgs = "--git-dir=" + GitSyncService.gitDir + " --work-tree=."; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String instanceScopeSyncNode = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String GIT_LOCATION_NODE_NAME = "git-location"; //$NON-NLS-1$
	private final int MAX_FILES = 100;

	private final RemoteLocation remoteLoc;
	private int remoteGitVersion;

	/**
	 * Create a new Git repository to the specified host, initialized with the given local JGit repository.
	 * See {@code buildRepo} for more details.
	 *
	 * @param localRepo
	 * 				not stored and only needed for initializing file filtering.
	 * @param rl
	 * 				remote location information
	 * @param monitor
	 * @throws RemoteSyncException
	 *             on problems building the remote repository (specific exception is nested). The instance is invalid.
	 * @throws MissingConnectionException
	 *             when connection missing. The instance is invalid.
	 */
	public GitRepo(JGitRepo localRepo, RemoteLocation rl, IProgressMonitor monitor)
			throws RemoteSyncException, MissingConnectionException {
		RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			remoteLoc = rl;

			// Build repo, creating it if it is not already present.
			try {
				subMon.subTask(Messages.GitRemoteSyncConnection_21);
				remoteGitVersion = getRemoteGitVersion(subMon.newChild(10));
				subMon.subTask(Messages.GitRemoteSyncConnection_20);
				buildRepo(localRepo, subMon.newChild(80));
			} catch (final IOException e) {
				throw new RemoteSyncException(e);
			} catch (final RemoteExecutionException e) {
				throw new RemoteSyncException(e);
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Create the Git repository - creating directories, Git-specific files, and other resources as needed.
	 *
	 * @param localRepo
	 *				The local JGit repository used only for initializing file filtering.
	 * @param monitor
	 *
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
	private void buildRepo(JGitRepo localRepo, IProgressMonitor monitor) throws IOException, RemoteExecutionException,
	RemoteSyncException, MissingConnectionException {
		final RecursiveSubMonitor subMon = RecursiveSubMonitor.convert(monitor, 100);
		try {
			// Create remote directory if necessary.
			try {
				subMon.subTask(Messages.GitRemoteSyncConnection_24);
				CommandRunner.createRemoteDirectory(remoteLoc.getConnection(), remoteLoc.getDirectory(),
						subMon.newChild(5));
			} catch (final CoreException e) {
				throw new RemoteSyncException(e);
			}

			// Initialize remote directory if necessary
			subMon.subTask(Messages.GitRemoteSyncConnection_25);
			doInit(subMon.newChild(5));

			subMon.subTask(Messages.GitRemoteSyncConnection_27);
			uploadFilter(localRepo, subMon.newChild(5));
			commitRemoteFiles(subMon.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Create and configure repository if it is not already present. Note that "git init" is "safe" on a repo already created, so
	 * we can simply rerun it each time.
	 * 
	 * @param monitor
	 * @throws IOException
	 * @throws RemoteExecutionException
	 * @throws RemoteSyncException
	 * @throws MissingConnectionException
	 * @return whether this repo already existed
	 */
	private void doInit(IProgressMonitor monitor) throws IOException, RemoteExecutionException, RemoteSyncException,
			MissingConnectionException {
		try {
			String commands = "git --git-dir=" + GitSyncService.gitDir + " init && " + //$NON-NLS-1$ //$NON-NLS-2$
					"git --git-dir=" + GitSyncService.gitDir + " config core.preloadindex true"; //$NON-NLS-1$ //$NON-NLS-2$
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
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Upload the file filter from the given JGit repository
	 *
	 * @param localJGitRepo
	 * @param monitor
	 *
	 * @throws MissingConnectionException
	 * 			on missing connection
	 * @throws RemoteSyncException
	 * 			on problems executing remote commands
	 */
	public void uploadFilter(JGitRepo localJGitRepo, IProgressMonitor monitor) throws MissingConnectionException, RemoteSyncException {
		IRemoteConnection conn = remoteLoc.getConnection();
		IRemoteServices remoteServices = conn.getRemoteServices();
		Repository repository = localJGitRepo.getRepository();

		try {
			//copy info/exclude to remote
			File exclude = repository.getFS().resolve(repository.getDirectory(),
					Constants.INFO_EXCLUDE);
			IFileStore local = EFS.getLocalFileSystem().getStore(new Path(exclude.getAbsolutePath()));
			String remoteExclude = remoteLoc.getDirectory() + "/" + GitSyncService.gitDir + "/" + Constants.INFO_EXCLUDE;  //$NON-NLS-1$ //$NON-NLS-2$
			IFileStore remote = remoteServices.getFileManager(conn).getResource(remoteExclude);
			local.copy(remote, EFS.OVERWRITE, monitor);

			//remove ignored files from index
			if (remoteGitVersion>=1080102) {
				final String  command = gitCommand() + " ls-files -X " + GitSyncService.gitDir + "/" + Constants.INFO_EXCLUDE + " -i | " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						gitCommand() + " update-index --force-remove --stdin ; " + //$NON-NLS-1$
						gitCommand() + " commit --allow-empty -m \"" + GitSyncService.commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
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
				try {
					if (objectId!=null)
						ref = new RevWalk(repository).parseTree(objectId);
				} catch (Exception e){
					//ignore. Can happen if the local repo doesn't yet have the remote commit
				}
				if (ref!=null) {
					Set<String> filesToRemove = localJGitRepo.getFilter().getIgnoredFiles(ref);
					deleteRemoteFiles(filesToRemove,monitor);
				}
			}
		} catch(RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		} catch (CoreException e) {
			throw new RemoteSyncException(e);
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteExecutionException e) {
			throw new RemoteSyncException(e);
		}
	}

	/**
	 * Commit changed files on the remote to the remote Git repository
	 *
	 * @param monitor
	 *
	 * @throws MissingConnectionException
	 * 			if the connection is unresolved
	 * @throws RemoteSyncException
	 * 			on problems executing the necessary remote commands.
	 */
	public void commitRemoteFiles(IProgressMonitor monitor) throws MissingConnectionException, RemoteSyncException {
		try {
			final String command = gitCommand() + " ls-files -X " + GitSyncService.gitDir + "/" + Constants.INFO_EXCLUDE + " -o -m | " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					gitCommand() + " update-index --add --remove --stdin ; " + //$NON-NLS-1$
					gitCommand() + " commit -m \"" + GitSyncService.commitMessage + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			CommandResults commandResults = this.executeRemoteCommand(command, monitor);
			if (commandResults.getExitCode() != 0 && !commandResults.getStdout().contains("nothing to commit")) { //$NON-NLS-1$
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
    			List<String> commandList = stringToList(gitCommand() + " rm --cached -f --"); //$NON-NLS-1$ 
    			int count = 1;
    			//the MAX_FILES trick could be avoided by sending files to stdin instead of passing as arguments
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

    /**
     * Merge files that have been pushed to the remote
     *
     * @param monitor
     *
     * @throws RemoteSyncException
	 * 			on problems executing the necessary remote commands.
	 * @throws MissingConnectionException
	 * 			if the connection is unresolved
     */
    public void merge(IProgressMonitor monitor) throws RemoteSyncException, MissingConnectionException {
		CommandResults mergeResults;
		// ff-only prevents accidental corruption of the remote repository but is supported only in recent Git versions.
		// final String command = gitCommand + " merge --ff-only " + remotePushBranch; //$NON-NLS-1$
		final String command = gitCommand() + " merge " + GitSyncService.remotePushBranch; //$NON-NLS-1$

		try {
			mergeResults = this.executeRemoteCommand(command, monitor);
			if (mergeResults.getExitCode() != 0) {
				throw new RemoteSyncException(new RemoteExecutionException(Messages.GRSC_GitMergeFailure + mergeResults.getStderr()));
			}
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} catch (InterruptedException e) {
			throw new RemoteSyncException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(e);
		}
    }

    private List<String> stringToList(String command) {
    	return new ArrayList<String>(Arrays.asList(command.split(" "))); //$NON-NLS-1$
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
					gitBinary = prefGitNode.get(remoteLoc.getConnection().getName(), "git"); //$NON-NLS-1$
				}
			} catch (BackingStoreException e) {
				Activator.log(Messages.GitRemoteSyncConnection_29, e);
			} catch (MissingConnectionException e) {
				// nothing to do
			}
		}

		return gitBinary + " " + gitArgs; //$NON-NLS-1$
	}

	/**
	 * Get the remote location of this repository
	 * @return remote location
	 */
	public RemoteLocation getRemoteLocation() {
		return remoteLoc;
	}

	/**
	 * Return the Git version used for this repository
	 *
	 * @param monitor
	 * @return Git version as a single int in the format: MMMmmmrrr (Major, minor, and revision)
	 *
	 * @throws IOException
	 * @throws RemoteExecutionException
	 * @throws RemoteSyncException
	 * @throws MissingConnectionException
	 */
	public int getRemoteGitVersion(IProgressMonitor monitor) throws RemoteSyncException, MissingConnectionException {
		String command = gitCommand() + " --version"; //$NON-NLS-1$
		CommandResults commandResults = null;

		try {
			// Skip class execute function, which attempts to run the command in the remote directory.
			// (This directory may not yet exist, since reading the Git version is one of the first operations.)
			IRemoteConnection conn = remoteLoc.getConnection();
			commandResults = CommandRunner.executeRemoteCommand(conn, command, null, monitor);
		} catch (final InterruptedException e) {
			throw new RemoteSyncException(new RemoteExecutionException(e));
		} catch (RemoteConnectionException e) {
			throw new RemoteSyncException(new RemoteExecutionException(e));
		} catch (IOException e) {
			throw new RemoteSyncException(e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		if (commandResults.getExitCode() != 0) {
			throw new RemoteSyncException(new RemoteExecutionException(Messages.GRSC_GitInitFailure + commandResults.getStderr()));
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
	
	private CommandResults executeRemoteCommand(String command, IProgressMonitor monitor) throws RemoteSyncException, IOException,
	InterruptedException, RemoteConnectionException, MissingConnectionException {
		IRemoteConnection conn = remoteLoc.getConnection();
		String remoteDirectory = remoteLoc.getDirectory();
		return CommandRunner.executeRemoteCommand(conn, command, remoteDirectory, monitor);
	}

	private CommandResults executeRemoteCommand(List<String> command, IProgressMonitor monitor) throws RemoteSyncException,
	IOException, InterruptedException, RemoteConnectionException, MissingConnectionException {
		IRemoteConnection conn = remoteLoc.getConnection();
		String remoteDirectory = remoteLoc.getDirectory();
		return CommandRunner.executeRemoteCommand(conn, command, remoteDirectory, monitor);
	}
}
