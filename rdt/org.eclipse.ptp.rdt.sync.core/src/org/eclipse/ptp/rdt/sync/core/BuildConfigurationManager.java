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
package org.eclipse.ptp.rdt.sync.core;

import java.net.URI;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.core.services.ISyncServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Singleton that mainly serves as an interface to core-level sync information and operations, specifically those concerning CDT
 * build configurations and specific build scenarios. During creation of a sync project, a build scenario should be set for all
 * build configurations using the methods provided. As of Juno, this class no longer stores and manages sync data. Instead, it
 * relies on storage space offered by Eclipse and CDT. This greatly simplifies the logic and makes sync projects more portable.
 */
public class BuildConfigurationManager {
	private static final String projectLocationPathVariable = "${project_loc}"; //$NON-NLS-1$
	private static final String syncServiceProviderID = "org.eclipse.ptp.rdt.sync.git.core.GitServiceProvider"; //$NON-NLS-1$
	private final ISyncServiceProvider provider;

	// Setup as a singleton
	private BuildConfigurationManager() {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);

		// Refactoring - July 2012
		// Use a single provider instance for all syncs. This does not preclude support for additional sync tools other than Git.
		// Such support can easily be added by mapping the "syncProvider" attribute of BuildScenario to the appropriate provider
		// instance (one per tool).
		provider = (ISyncServiceProvider) smm.getServiceProvider(syncService.getProviderDescriptor(syncServiceProviderID));
		if (provider == null) {
			throw new RuntimeException(Messages.BuildConfigurationManager_25);
		}
	}

	private static BuildConfigurationManager fInstance = null;

	/**
	 * Get the single BuildConfigurationManager instance
	 * 
	 * @return instance
	 */
	public static synchronized BuildConfigurationManager getInstance() {
		if (fInstance == null) {
			fInstance = new BuildConfigurationManager();
		}
		return fInstance;
	}

	/**
	 * Get the synchronize location URI of the resource associated with the active build configuration. Returns null if the project
	 * containing the resource is not a synchronized project.
	 * 
	 * @param resource
	 *            target resource - cannot be null
	 * @return URI or null if not a sync project
	 * @throws CoreException
	 */
	public URI getActiveSyncLocationURI(IResource resource) throws CoreException {
		BuildScenario scenario = getActiveBuildScenario(resource);
		if (scenario != null) {
			return getSyncLocationURI(scenario, resource.getProject());
		}
		return null;
	}

	/**
	 * Get the build scenario associated with the active configuration. Returns null if not a synchronized project.
	 * 
	 * @param resource
	 * @return
	 * @throws CoreException
	 */
	public BuildScenario getActiveBuildScenario(IResource resource) throws CoreException {
		IProject project = resource.getProject();
		if (project.hasNature(RemoteSyncNature.NATURE_ID)) {
			BuildScenario[] scenarios = SyncManager.getSynchronizePolicy(project, SyncMode.ACTIVE);
			if (scenarios != null) {
				return scenarios[0];
			}
		}
		return null;
	}

	/**
	 * Get the synchronize location URI of the resource associated with the build scenario. Returns null if the scenario
	 * does not contain synchronization information or no connection has been configured.
	 * 
	 * @param scenario
	 *            build configuration with sync provider
	 * @param resource
	 *            target resource
	 * @return URI or null if not a sync configuration
	 * @throws CoreException
	 */
	public URI getSyncLocationURI(BuildScenario scenario, IResource resource) throws CoreException {
		if (scenario != null) {
			IPath path = new Path(scenario.getLocation()).append(resource.getProjectRelativePath());
			IRemoteConnection conn;
			try {
				conn = scenario.getRemoteConnection();
			} catch (MissingConnectionException e) {
				return null;
			}
			IRemoteFileManager fileMgr = conn.getRemoteServices().getFileManager(conn);
			return fileMgr.toURI(path);
		}
		return null;
	}

	/**
	 * Create a build scenario for configurations that build in the local Eclipse workspace.
	 * This function makes no changes to the internal data structures and is of little value for most clients.
	 * 
	 * @param project
	 *            - cannot be null
	 * @return the build scenario - never null
	 * @throws CoreException
	 *             on problems getting local resources, either the local connection or local services
	 */
	public BuildScenario createLocalBuildScenario(IProject project) throws CoreException {
		IRemoteServices localService = RemoteServices.getLocalServices();

		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection(
					IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
			if (localConnection != null) {
				return new BuildScenario(null, localConnection, projectLocationPathVariable);
			} else {
				throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID, Messages.BCM_LocalConnectionError));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID, Messages.BCM_LocalServiceError));
		}
	}

	/**
	 * Return the sync provider for this project.
	 * 
	 * @param project
	 *            - cannot be null
	 * @return sync provider
	 */
	public ISyncServiceProvider getProjectSyncProvider(IProject project) {
		return provider;
	}

	/**
	 * The node flushing mechanism fails if the workspace is locked. So calling "Node.flush()" is not enough. Instead, spawn a
	 * thread that flushes once the workspace is unlocked.
	 * 
	 * @param prefNode
	 *            node to flush
	 */

	public static void flushNode(final Preferences prefNode) {
		Throwable firstException = null;
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		// Avoid creating a thread if possible.
		try {
			if (!ws.isTreeLocked()) {
				prefNode.flush();
				return;
			}
		} catch (BackingStoreException e) {
			// Proceed to create thread
			firstException = e;
		} catch (IllegalStateException e) {
			// Can occur if the project has been moved or deleted, so the preference node no longer exists.
			firstException = e;
			return;
		}

		final Throwable currentException = firstException;
		Thread flushThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int sleepCount = 0;
				Throwable lastException = currentException;
				while (true) {
					try {
						Thread.sleep(1000);
						// Give up after 30 sleeps - this should never happen
						sleepCount++;
						if (sleepCount > 30) {
							if (lastException != null) {
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_17, lastException);
							} else {
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_17);
							}
							break;
						}
						if (!ws.isTreeLocked()) {
							prefNode.flush();
							break;
						}
					} catch (InterruptedException e) {
						lastException = e;
					} catch (BackingStoreException e) {
						// This can happen in the rare case that the lock is locked between the check and the flush.
						lastException = e;
					} catch (IllegalStateException e) {
						// Can occur if the project has been moved or deleted, so the preference node no longer exists.
						return;
					}
				}
			}
		}, "Flush project data thread"); //$NON-NLS-1$
		flushThread.start();
	}

	/**
	 * Get the current list of merge-conflicted files for the passed project and build scenario
	 * 
	 * @param project
	 * @param buildScenario
	 * @return set of files as project-relative IPaths. This may be an empty set but never null.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public Set<IPath> getMergeConflictFiles(IProject project, BuildScenario buildScenario) throws CoreException {
		return provider.getMergeConflictFiles(project, buildScenario);
	}

	/**
	 * Get the three parts of the merge-conflicted file (left, right, and ancestor, respectively)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param file
	 * @return the three parts as strings. Either three strings (some may be empty) or null if file is not merge-conflicted or
	 *         on some problems retrieving the sync provider.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public String[] getMergeConflictParts(IProject project, BuildScenario buildScenario, IFile file) throws CoreException {
		return provider.getMergeConflictParts(project, buildScenario, file);
	}

	/**
	 * Set the given path as resolved (no merge conflict)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 *             for system-level problems setting the state
	 */
	public void setMergeAsResolved(IProject project, BuildScenario buildScenario, IPath path) throws CoreException {
		this.setMergeAsResolved(project, buildScenario, new IPath[] { path });
	}

	/**
	 * Set the given paths as resolved (no merge conflict)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 *             for system-level problems setting the state
	 */
	public void setMergeAsResolved(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException {
		provider.setMergeAsResolved(project, buildScenario, paths);
	}

	/**
	 * Replace given file with the most recent version in the repository
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 */
	public void checkout(IProject project, BuildScenario buildScenario, IPath path) throws CoreException {
		this.checkout(project, buildScenario, new IPath[] { path });
	}

	/**
	 * Replace given files with the most recent versions in the repository
	 * 
	 * @param project
	 * @param buildScenario
	 * @param paths
	 * @throws CoreException
	 */
	public void checkout(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException {
		provider.checkout(project, buildScenario, paths);
	}

	/**
	 * Replace given file with the most recent local copy of the remote (not necessarily the same as the current remote)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 */
	public void checkoutRemoteCopy(IProject project, BuildScenario buildScenario, IPath path) throws CoreException {
		this.checkoutRemoteCopy(project, buildScenario, new IPath[] { path });
	}

	/**
	 * Replace given files with the most recent local copies of the remote (not necessarily the same as the current remotes)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 */
	public void checkoutRemoteCopy(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException {
		provider.checkoutRemoteCopy(project, buildScenario, paths);
	}

	/**
	 * Do any necessary actions to shutdown the given project.
	 * 
	 * @param project
	 */
	public void shutdown(IProject project) {
		provider.close(project);
	}
}