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
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CProjectNature;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.listeners.ISyncConfigListener;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Singleton that mainly serves as an interface to core-level sync information and operations, specifically those concerning CDT
 * build configurations and specific build scenarios. During creation of a sync project, a build scenario should be set for all
 * build configurations using the methods provided.
 */
public class BuildConfigUtils {
	private static final String SYNC_CONFIG_ID = "org.eclipse.ptp.rdt.sync.cdt.core"; //$NON-NLS-1$
	private static final String localConfigAnnotation = "_local"; //$NON-NLS-1$
	private static final String remoteConfigAnnotation = "_remote"; //$NON-NLS-1$

	private static final String SYNC_BUILDER_ID = "org.eclipse.ptp.rdt.sync.cdt.core.SyncBuilder"; //$NON-NLS-1$
	private static final String SYNC_BUILDER_NAME = "Sync Builder";//$NON-NLS-1$

	private static final String ATTR_SYNC_CONFIG_NAME = "sync-config-name"; //$NON-NLS-1$

	private static final ISyncConfigListener fSyncConfigListener = new ISyncConfigListener() {

		@Override
		public void configAdded(IProject project, SyncConfig config) {
			// TODO Auto-generated method stub

		}

		@Override
		public void configRemoved(IProject project, SyncConfig config) {
			// TODO Auto-generated method stub

		}

		@Override
		public void configSelected(IProject project, SyncConfig newConfig, SyncConfig oldConfig) {
			// TODO Auto-generated method stub

		}

	};

	// Run standard checks on project and throw the appropriate exception if it is not valid
	// All public methods should call this for any passed project or any passed configuration's project.
	// Private methods assume projects have been checked.
	private static void checkProject(IProject project) {
		try {
			if (!project.hasNature(RemoteSyncNature.NATURE_ID)) {
				throw new IllegalArgumentException(Messages.BuildConfigurationManager_6);
			}
		} catch (CoreException e) {
			throw new IllegalArgumentException(Messages.BuildConfigurationManager_8);
		}
	}

	/**
	 * Create a configuration for the given project with the given build scenario, name, and description using the given build
	 * configuration as parent.
	 * 
	 * @param project
	 * @param configParent
	 * @param syncConfig
	 * @param configName
	 * @param configDesc
	 * @return the new configuration
	 */
	public static IConfiguration createConfiguration(IProject project, Configuration configParent, SyncConfig syncConfig,
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
			try {
				setConfigData(config, syncConfig.getName());
			} catch (CoreException e) {
				Activator.log(e);
			}
		} else {
			creationError = Messages.BCM_CreateConfigError;
		}

		if (!configAdded) {
			if (creationError == null && creationException != null) {
				creationError = creationException.getMessage();
			}
			Activator.log(Messages.BCM_CreateConfigFailure + creationError, creationException);
			return null;
		}

		return config;
	}

	/**
	 * Create a configuration for the given project with the given build scenario, name, and description using the project's
	 * default build configuration as parent.
	 * 
	 * @param project
	 * @param syncConfig
	 * @param configName
	 * @param configDesc
	 * @return the new configuration
	 */
	public static IConfiguration createConfiguration(IProject project, SyncConfig syncConfig, String configName, String configDesc) {
		return createConfiguration(project, null, syncConfig, configName, configDesc);
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
	public static IConfiguration createLocalConfiguration(IProject project, String configName) {
		checkProject(project);
		try {
			SyncConfig localConfig = SyncConfigManager.createLocal(project);
			return createConfiguration(project, localConfig, configName, Messages.BCM_WorkspaceConfigDes);
		} catch (CoreException e) {
			Activator.log(Messages.BCM_CreateConfigFailure + e.getMessage(), e);
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
	public static IConfiguration createRemoteConfiguration(IProject project, SyncConfig remoteBuildScenario, String configName,
			String configDesc) {
		checkProject(project);
		return createConfiguration(project, remoteBuildScenario, configName, configDesc);
	}

	/**
	 * Remove sync config listeners on all synchronized CDT projects in the workspace. Should only be called on plugin shutdown.
	 */
	public static void finalizeListeners() {
		SyncConfigManager.removeSyncConfigListener(CProjectNature.C_NATURE_ID, fSyncConfigListener);
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
								Activator.log(Messages.BuildConfigurationManager_17, lastException);
							} else {
								Activator.log(Messages.BuildConfigurationManager_17);
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

	// The below two functions give us an easy mechanism to store build scenario data inside build configurations
	/**
	 * Get the sync config name from build configuration data
	 * 
	 * @param config
	 *            build config
	 * @return sync config name
	 * @throws CoreException
	 *             on problems retrieving data
	 */
	private static String getConfigData(Configuration config) throws CoreException {
		ICConfigurationDescription configDesc = config.getConfigurationDescription();
		if (configDesc == null) {
			// Should never happen
			throw new RuntimeException(Messages.BuildConfigurationManager_18);
		}

		ICStorageElement storage = configDesc.getStorage(SYNC_CONFIG_ID, false);
		if (storage == null) {
			return null;
		}
		return storage.getAttribute(ATTR_SYNC_CONFIG_NAME);
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
	 * Return the sync config for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "updateConfigurations." For configurations still unknown (perhaps newly created configurations not yet recorded in CDT),
	 * return the build scenario for the closest known ancestor.
	 * 
	 * @param bconf
	 *            - the build configuration - cannot be null
	 * @return sync config or null if there are problems accessing configuration's information
	 */
	public static SyncConfig getSyncConfigForBuildConfiguration(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		checkProject(project);
		return getSyncConfigForBuildConfigurationInternal(bconf);
	}

	// Return the sync config stored for the passed id or the sync config of its nearest ancestor.
	// Return null if not found.
	private static SyncConfig getSyncConfigForBuildConfigurationInternal(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		try {
			IConfiguration config = bconf;
			String configId = config.getId();
			String syncConfigName = getConfigData((Configuration) config);
			while (syncConfigName == null) {
				configId = getParentId(configId);
				if (configId == null) {
					break;
				}
				config = buildInfo.getManagedProject().getConfiguration(configId);
				syncConfigName = getConfigData((Configuration) config);
			}

			if (configId != null) {
				SyncConfig sconf = SyncConfigManager.getConfig(project, syncConfigName);
				if (sconf != null) {
					return sconf;
				}
				Activator.log(Messages.BuildConfigurationManager_14 + configId + Messages.BuildConfigurationManager_11
						+ project.getName());
			}
			return null;
		} catch (CoreException e) {
			Activator.log(Messages.BuildConfigurationManager_19, e);
			return null;
		}
	}

	public static IConfiguration getBuildConfigurationForSyncConfig(IProject project, String configName) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			return null;
		}
		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			String name = null;
			try {
				name = getConfigData((Configuration) config);
			} catch (CoreException e) {
				// Ignore
			}
			if (configName.equals(name)) {
				return config;
			}
		}
		return null;
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
	public static URI getSyncLocationURI(IConfiguration configuration, IResource resource) throws CoreException {
		// Project checked inside this function call
		SyncConfig config = getSyncConfigForBuildConfiguration(configuration);
		if (config != null) {
			IPath path = new Path(config.getLocation()).append(resource.getProjectRelativePath());
			IRemoteConnection conn;
			try {
				conn = config.getRemoteConnection();
			} catch (MissingConnectionException e) {
				return null;
			}
			IRemoteFileManager fileMgr = conn.getRemoteServices().getFileManager(conn);
			return fileMgr.toURI(path);
		}
		return null;
	}

	/**
	 * Initialize sync config listeners on all synchronized CDT projects in the workspace. Should only be called on plugin startup.
	 */
	public static void initializeListeners() {
		SyncConfigManager.addSyncConfigListener(CProjectNature.C_NATURE_ID, fSyncConfigListener);
	}

	/**
	 * Make the given configuration a local configuration
	 * 
	 * @param config
	 */
	public static void modifyConfigurationAsSyncLocal(IConfiguration config) {
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
	public static void modifyConfigurationAsSyncRemote(IConfiguration config) {
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

		IBuilder syncBuilder = ManagedBuildManager.getExtensionBuilder(SYNC_BUILDER_ID);
		config.changeBuilder(syncBuilder, SYNC_BUILDER_ID, SYNC_BUILDER_NAME);
		// turn off append contributed (local) environment variables for the build configuration of the remote project
		ICConfigurationDescription c_mb_confgDes = ManagedBuildManager.getDescriptionForConfiguration(config);
		if (c_mb_confgDes != null) {
			EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(false, c_mb_confgDes);
		}

		ManagedBuildManager.saveBuildInfo(config.getOwner().getProject(), true);
	}

	/**
	 * Store the sync config name in the build configuration.
	 * 
	 * @param config
	 *            build config
	 * @param configName
	 *            sync config name
	 * @throws CoreException
	 *             on problems retrieving data
	 */
	private static void setConfigData(Configuration config, String configName) throws CoreException {
		// The commented code gets a read-only description.
		// ICConfigurationDescription configDesc = config.getConfigurationDescription();
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(config.getOwner().getProject());
		ICConfigurationDescription configDesc = projectDesc.getConfigurationById(config.getId());
		if (configDesc == null) {
			// Should never happen
			throw new RuntimeException(Messages.BuildConfigurationManager_18);
		}

		ICStorageElement storage = configDesc.getStorage(SYNC_CONFIG_ID, true);
		storage.clear(); // Certain attributes can be intentionally unmapped, such as the sync provider for non-sync'ed configs.
		storage.setAttribute(ATTR_SYNC_CONFIG_NAME, configName);
		config.setDirty(true); // Fixes case where "Workspace" configuration does not compile after project rename.
		setProjectDescription(config.getOwner().getProject(), projectDesc);
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
								Activator.log(Messages.BuildConfigurationManager_24, lastException);
							} else {
								Activator.log(Messages.BuildConfigurationManager_24);
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
	 * Set all build configurations to the passed build scenario. This is usually called as part of project creation.
	 * 
	 * @param project
	 *            - cannot be null
	 * @param bs
	 *            - the build scenario - cannot be null
	 */
	public static void setSyncConfigForAllBuildConfigurations(IProject project, SyncConfig sconf) {
		if (sconf == null) {
			throw new NullPointerException();
		}
		checkProject(project);

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			try {
				setConfigData((Configuration) config, sconf.getName());
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	/**
	 * Associate the given build configuration with the given sync config.
	 * 
	 * @param sconf
	 *            - cannot be null
	 * @param bconf
	 *            - the build configuration - cannot be null
	 */
	public static void setSyncConfigForBuildConfiguration(SyncConfig sconf, IConfiguration bconf) {
		if (sconf == null) {
			throw new NullPointerException();
		}
		IProject project = bconf.getOwner().getProject();
		checkProject(project);
		// Update so that unknown children of the given configuration are set properly to use the previous build scenario
		updateConfigurations(project);
		try {
			setConfigData((Configuration) bconf, sconf.getName());
		} catch (CoreException e) {
			Activator.log(Messages.BuildConfigurationManager_20, e);
		}
	}

	// Set all unknown configurations (those without a sync config) to use the sync config of their closest ancestor
	private static void updateConfigurations(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			String name = null;
			try {
				name = getConfigData((Configuration) config);
			} catch (CoreException e) {
				// Ignore
			}
			if (name == null) {
				String parentId = getParentId(config.getId());
				if (parentId != null) {
					IConfiguration parentConfig = buildInfo.getManagedProject().getConfiguration(parentId);
					if (parentConfig != null) {
						SyncConfig sconf = getSyncConfigForBuildConfigurationInternal(parentConfig);
						if (sconf == null) {
							return; // Errors handled by prior function call
						}
						try {
							setConfigData((Configuration) config, sconf.getName());
						} catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
	}
}