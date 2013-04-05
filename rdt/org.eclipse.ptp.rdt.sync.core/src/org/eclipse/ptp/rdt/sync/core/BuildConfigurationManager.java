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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
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
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
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
	private static final String configSyncDataStorageName = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String projectLocationPathVariable = "${project_loc}"; //$NON-NLS-1$
	private static final String localConfigAnnotation = "_local"; //$NON-NLS-1$
	private static final String remoteConfigAnnotation = "_remote"; //$NON-NLS-1$
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
	 * Create a local build configuration. The corresponding build scenario has no sync provider and points to the project's working
	 * directory. It has a default name and description. This function is normally used as part of the setup when creating a new
	 * project
	 * and is of little value to most clients.
	 * 
	 * @param project
	 *            The project needing a local configuration - cannot be null
	 * @return the new configuration - can be null on problems during creation
	 */
	public IConfiguration createLocalConfiguration(IProject project, String configName) {
		checkProject(project);
		try {
			BuildScenario localBuildScenario = this.createLocalBuildScenario(project);
			if (localBuildScenario != null) {
				return this.createConfiguration(project, localBuildScenario, configName, Messages.BCM_WorkspaceConfigDes);
			}
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(Messages.BCM_CreateConfigFailure + e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Create a remote build configuration. This function is mainly used for internal sync purposes and is of little value to most
	 * clients.
	 * 
	 * @param project
	 *            The project needing a remote configuration - cannot be null
	 * @param remoteBuildScenario
	 *            Configuration's build scenario - cannot be null
	 * @param configName
	 *            Configuration's name
	 * @param configDesc
	 *            Configuration's description
	 * @return the new configuration - can be null on problems during creation
	 */
	public IConfiguration createRemoteConfiguration(IProject project, BuildScenario remoteBuildScenario, String configName,
			String configDesc) {
		checkProject(project);
		return this.createConfiguration(project, remoteBuildScenario, configName, configDesc);
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
		if (resource.getProject().hasNature(RemoteSyncNature.NATURE_ID)) {
			IConfiguration configuration = ManagedBuildManager.getBuildInfo(resource.getProject()).getDefaultConfiguration();
			return getSyncLocationURI(configuration, resource);
		}
		return null;
	}

	/**
	 * Get a SyncRunner object that can be used to do sync'ing.
	 * 
	 * @param bconf
	 * @return SyncRunner - can be null if this configuration does not require sync'ing, such as a local configuration, or if there
	 *         are problems retrieving the sync provider or information.
	 */
	public SyncRunner getSyncRunnerForBuildConfiguration(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		checkProject(project);

		BuildScenario buildScenario = this.getBuildScenarioForBuildConfigurationInternal(bconf).bs;
		if (buildScenario == null) { // Error handled in call
			return null;
		}

		if (buildScenario.getSyncProvider() == null) {
			return null;
		} else {
			return new SyncRunner(provider);
		}
	}

	/**
	 * Return the name of the sync provider for this project.
	 * 
	 * @param project
	 *            - cannot be null
	 * @return sync provider name
	 */
	public String getProjectSyncProvider(IProject project) {
		return provider.getName();
	}

	/**
	 * Get the synchronize location URI of the resource associated with the build configuration. Returns null if the configuration
	 * does not contain synchronization information or no connection has been configured.
	 * 
	 * @param configuration
	 *            build configuration with sync provider
	 * @param resource
	 *            target resource
	 * @return URI or null if not a sync configuration
	 * @throws CoreException
	 */
	public URI getSyncLocationURI(IConfiguration configuration, IResource resource) throws CoreException {
		// Project checked inside this function call
		BuildScenario scenario = getBuildScenarioForBuildConfiguration(configuration);
		if (scenario != null) {
			IPath path = new Path(scenario.location).append(resource.getProjectRelativePath());
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
	 * Set all build configurations to the passed build scenario. This is usually called as part of project creation.
	 * 
	 * @param project
	 *            - cannot be null
	 * @param bs
	 *            - the build scenario - cannot be null
	 */
	public void setBuildScenarioForAllBuildConfigurations(IProject project, BuildScenario bs) {
		if (bs == null) {
			throw new NullPointerException();
		}
		checkProject(project);

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			setBuildScenarioForBuildConfigurationInternal(bs, config);
		}
	}

	/**
	 * Create a configuration for the given project with the given build scenario, name, and description using the project's
	 * default build configuration as parent.
	 * 
	 * @param project
	 * @param buildScenario
	 * @param configName
	 * @param configDesc
	 * @return the new configuration
	 */
	public IConfiguration createConfiguration(IProject project, BuildScenario buildScenario, String configName, String configDesc) {
		return this.createConfiguration(project, null, buildScenario, configName, configDesc);
	}

	/**
	 * Create a configuration for the given project with the given build scenario, name, and description using the given build
	 * configuration as parent.
	 * 
	 * @param project
	 * @param configParent
	 * @param buildScenario
	 * @param configName
	 * @param configDesc
	 * @return the new configuration
	 */
	public IConfiguration createConfiguration(IProject project, Configuration configParent, BuildScenario buildScenario,
			String configName, String configDesc) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		// For recording of problems during attempt
		Throwable creationException = null;
		String creationError = null;
		boolean configAdded = false;

		ManagedProject managedProject = (ManagedProject) buildInfo.getManagedProject();
		if (configParent == null) {
			configParent = (Configuration) buildInfo.getDefaultConfiguration();
		}
		String configId = ManagedBuildManager.calculateChildId(configParent.getId(), null);
		Configuration config = new Configuration(managedProject, configParent, configId, true, false);
		CConfigurationData configData = config.getConfigurationData();
		ICProjectDescription projectDes = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription configDes = null;
		try {
			configDes = projectDes.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, configData);
		} catch (WriteAccessException e) {
			creationException = e;
		} catch (CoreException e) {
			creationException = e;
		}

		if (configDes != null) {
			configAdded = true;
			config.setConfigurationDescription(configDes);
			configDes.setName(configName);
			configDes.setDescription(configDesc);
			setProjectDescription(project, projectDes);
			this.setBuildScenarioForBuildConfigurationInternal(buildScenario, config);
		} else {
			creationError = Messages.BCM_CreateConfigError;
		}

		if (!configAdded) {
			if (creationError == null && creationException != null) {
				creationError = creationException.getMessage();
			}
			RDTSyncCorePlugin.log(Messages.BCM_CreateConfigFailure + creationError, creationException);
			return null;
		}

		return config;
	}

	// Set all unknown configurations (those without a build scenario) to use the build scenario of their closest ancestor
	private void updateConfigurations(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			BuildScenarioAndConfiguration parentConfigInfo = getBuildScenarioForBuildConfigurationInternal(config);
			if (parentConfigInfo == null) {
				return; // Errors handled by prior function call
			}
			if (parentConfigInfo.configId == config.getId()) {
				continue;
			}
			IConfiguration parentConfig = buildInfo.getManagedProject().getConfiguration(parentConfigInfo.configId);
			if (parentConfig != null) {
				setBuildScenarioForBuildConfigurationInternal(parentConfigInfo.bs, config);
			} else {
				RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_10 + parentConfigInfo.configId
						+ Messages.BuildConfigurationManager_11 + project.getName());
			}
		}
	}

	/**
	 * Return the build scenario for the passed project (actually for the project's current active or "default" configuration)
	 * 
	 * @param project
	 *            - cannot be null
	 * @return build scenario or null if there are problems accessing configuration's information
	 */
	public BuildScenario getBuildScenarioForProject(IProject project) {
		checkProject(project);
		IConfiguration bconf = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		return this.getBuildScenarioForBuildConfigurationInternal(bconf).bs;
	}

	/**
	 * Return the build scenario for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "updateConfigurations." For configurations still unknown (perhaps newly created configurations not yet recorded in CDT),
	 * return the build scenario for the closest known ancestor.
	 * 
	 * @param bconf
	 *            - the build configuration - cannot be null
	 * @return build scenario or null if there are problems accessing configuration's information
	 */
	public BuildScenario getBuildScenarioForBuildConfiguration(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		checkProject(project);
		return this.getBuildScenarioForBuildConfigurationInternal(bconf).bs;
	}

	// Simple class for bundling a build scenario with its configuration id
	private class BuildScenarioAndConfiguration {
		public final BuildScenario bs;
		public final String configId;

		BuildScenarioAndConfiguration(BuildScenario scenario, String configuration) {
			bs = scenario;
			configId = configuration;
		}
	}

	// Return the build scenario stored for the passed id or the build scenario of its nearest ancestor.
	// Return null if not found.
	private BuildScenarioAndConfiguration getBuildScenarioForBuildConfigurationInternal(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		try {
			IConfiguration config = bconf;
			String configId = config.getId();
			Map<String, String> scenarioData = this.getConfigData((Configuration) config, configSyncDataStorageName);
			while (scenarioData == null) {
				configId = getParentId(configId);
				if (configId == null) {
					break;
				}
				config = buildInfo.getManagedProject().getConfiguration(configId);
				scenarioData = this.getConfigData((Configuration) config, configSyncDataStorageName);
			}

			if (configId != null) {
				BuildScenario bs = BuildScenario.loadScenario(scenarioData);
				if (bs == null) {
					RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_14 + configId + Messages.BuildConfigurationManager_11
							+ project.getName());
					return null;
				} else {
					return new BuildScenarioAndConfiguration(bs, configId);
				}
			} else {
				return null;
			}
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_19, e);
			return null;
		}
	}

	// Each new configuration id appends a number to the parent id. So we strip off the last id number to get the parent. We assume
	// the configuration does not have a parent and return null if the result does not end with a number.
	private static String getParentId(String configId) {
		String idRegEx = "\\.\\d+$"; //$NON-NLS-1$
		Pattern idPattern = Pattern.compile(idRegEx);
		String parentConfigId = configId.replaceFirst(idRegEx, ""); //$NON-NLS-1$

		if (idPattern.matcher(parentConfigId).find()) {
			return parentConfigId;
		}

		return null;
	}

	/**
	 * Associate the given configuration with the given build scenario.
	 * 
	 * @param buildScenario
	 *            - cannot be null
	 * @param bconf
	 *            - the build configuration - cannot be null
	 */
	public void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
		if (bs == null) {
			throw new NullPointerException();
		}
		IProject project = bconf.getOwner().getProject();
		checkProject(project);
		// Update so that unknown children of the given configuration are set properly to use the previous build scenario
		updateConfigurations(project);
		this.setBuildScenarioForBuildConfigurationInternal(bs, bconf);
	}

	private void setBuildScenarioForBuildConfigurationInternal(BuildScenario bs, IConfiguration bconf) {
		Map<String, String> map = new HashMap<String, String>();
		bs.saveScenario(map);
		try {
			this.setConfigData((Configuration) bconf, map, configSyncDataStorageName);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_20, e);
			return;
		}
	}

	// The below two functions give us an easy mechanism to store build scenario data inside build configurations
	/**
	 * Get simple java map of configuration data
	 * 
	 * @param config
	 * @param storageName
	 *            - name of storage module
	 * 
	 * @return values in named storage location or null if the storage location does not exist.
	 * @throws CoreException
	 *             on problems retrieving data
	 */
	private Map<String, String> getConfigData(Configuration config, String storageName) throws CoreException {
		ICConfigurationDescription configDesc = config.getConfigurationDescription();
		if (configDesc == null) {
			// Should never happen
			throw new RuntimeException(Messages.BuildConfigurationManager_18);
		}

		Map<String, String> m = new HashMap<String, String>();
		ICStorageElement storage = configDesc.getStorage(storageName, false);
		if (storage == null) {
			return null;
		}
		for (String attr : storage.getAttributeNames()) {
			m.put(attr, storage.getAttribute(attr));
		}

		return m;
	}

	/**
	 * Store a simple java map as data in a configuration.
	 * 
	 * @param config
	 * @param map
	 * @param storageName
	 *            - name of storage module
	 * 
	 * @throws CoreException
	 *             on problems retrieving data
	 */
	private void setConfigData(Configuration config, Map<String, String> map, String storageName) throws CoreException {
		// The commented code gets a read-only description.
		// ICConfigurationDescription configDesc = config.getConfigurationDescription();
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(config.getOwner().getProject());
		ICConfigurationDescription configDesc = projectDesc.getConfigurationById(config.getId());
		if (configDesc == null) {
			// Should never happen
			throw new RuntimeException(Messages.BuildConfigurationManager_18);
		}

		ICStorageElement storage = configDesc.getStorage(storageName, true);
		storage.clear(); // Certain attributes can be intentionally unmapped, such as the sync provider for non-sync'ed configs.
		for (Map.Entry<String, String> entry : map.entrySet()) {
			storage.setAttribute(entry.getKey(), entry.getValue());
		}
		config.setDirty(true); // Fixes case where "Workspace" configuration does not compile after project rename.
		setProjectDescription(config.getOwner().getProject(), projectDesc);
	}

	// Run standard checks on project and throw the appropriate exception if it is not valid
	// All public methods should call this for any passed project or any passed configuration's project.
	// Private methods assume projects have been checked.
	private void checkProject(IProject project) {
		try {
			if (!project.hasNature(RemoteSyncNature.NATURE_ID)) {
				throw new IllegalArgumentException(Messages.BuildConfigurationManager_6);
			}
		} catch (CoreException e) {
			throw new IllegalArgumentException(Messages.BuildConfigurationManager_8);
		}
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
	 * Writing to the .cproject file fails if the workspace is locked. So calling CoreModel.getDefault().setProjectDescription() is
	 * not enough. Instead, spawn a thread that calls this function once the workspace is unlocked.
	 * The overall logic for this function and "nodeFlush" is the same.
	 * 
	 * @param project
	 * @param desc
	 */
	public static void setProjectDescription(final IProject project, final ICProjectDescription desc) {
		Throwable firstException = null;
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		// Avoid creating a thread if possible.
		try {
			if (!ws.isTreeLocked()) {
				CoreModel.getDefault().setProjectDescription(project, desc, true, null);
				return;
			}
		} catch (CoreException e) {
			// This can happen in the rare case that the lock is locked between the check and the flush but also for other reasons.
			// Be optimistic and proceed to create thread.
			firstException = e;
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
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_24, lastException);
							} else {
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_24);
							}
							break;
						}
						if (!ws.isTreeLocked()) {
							CoreModel.getDefault().setProjectDescription(project, desc, true, null);
							break;
						}
					} catch (InterruptedException e) {
						lastException = e;
					} catch (CoreException e) {
						// This can happen in the rare case that the lock is locked between the check and the flush but also for
						// other reasons.
						// Be optimistic and try again.
						lastException = e;
					}
				}
			}
		}, "Save project CDT data thread"); //$NON-NLS-1$
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
	 * Make the given configuration a local configuration
	 * 
	 * @param config
	 */
	public void modifyConfigurationAsSyncLocal(IConfiguration config) {
		String configName = config.getName();
		if (configName.endsWith(localConfigAnnotation)) {
			// nothing to do
		} else {
			if (configName.endsWith(remoteConfigAnnotation)) {
				configName = configName.substring(0, configName.length() - remoteConfigAnnotation.length());
			}
			configName += localConfigAnnotation;
			config.setName(configName);
		}

		ManagedBuildManager.saveBuildInfo(config.getOwner().getProject(), true);
	}

	/**
	 * Make the given configuration a remote configuration
	 * 
	 * @param config
	 */
	public void modifyConfigurationAsSyncRemote(IConfiguration config) {
		String configName = config.getName();
		if (configName.endsWith(remoteConfigAnnotation)) {
			// nothing to do
		} else {
			if (configName.endsWith(localConfigAnnotation)) {
				configName = configName.substring(0, configName.length() - localConfigAnnotation.length());
			}
			configName += remoteConfigAnnotation;
			config.setName(configName);
		}

		IBuilder syncBuilder = ManagedBuildManager.getExtensionBuilder("org.eclipse.ptp.rdt.sync.core.SyncBuilder"); //$NON-NLS-1$
		config.changeBuilder(syncBuilder, "org.eclipse.ptp.rdt.sync.core.SyncBuilder", "Sync Builder"); //$NON-NLS-1$ //$NON-NLS-2$
		// turn off append contributed (local) environment variables for the build configuration of the remote project
		ICConfigurationDescription c_mb_confgDes = ManagedBuildManager.getDescriptionForConfiguration(config);
		if (c_mb_confgDes != null) {
			EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(false, c_mb_confgDes);
		}

		ManagedBuildManager.saveBuildInfo(config.getOwner().getProject(), true);
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