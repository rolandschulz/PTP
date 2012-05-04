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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.XMLMemento;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Singleton that handles the storing of information about CDT build configurations. This includes the configuration's build
 * scenario (information on the sync point for the configuration), and its service configuration. New projects should call
 * "initProject" and specify a template service configuration and a build scenario, which is assigned to all existing
 * configurations. The template service configuration should specify all non-sync services. (Normally, the project's active
 * configuration should be used.) This template is copied as needed to create service configurations for build configurations.
 */
public class BuildConfigurationManager {
	private static final String projectScopeSyncNode = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String configSyncDataStorageName = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String TEMPLATE_KEY = "template-service-configuration"; //$NON-NLS-1$
	private static final String projectLocationPathVariable = "${project_loc}"; //$NON-NLS-1$
	
	// Setup as a singleton
	private BuildConfigurationManager() {
	}

	private static BuildConfigurationManager fInstance = null;

	/**
	 * Get the single BuildConfigurationManager instance
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
	 * @param project - cannot be null
	 * @return the build scenario - never null
	 * @throws CoreException
	 *             on problems getting local resources, either the local connection or local services
	 */
	public BuildScenario createLocalBuildScenario(IProject project) throws CoreException {
		IRemoteServices localService = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				"org.eclipse.ptp.remote.LocalServices", null); //$NON-NLS-1$

		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection("Local"); //$NON-NLS-1$
			if (localConnection != null) {
				return new BuildScenario(null, localConnection, projectLocationPathVariable);
			} else {
				throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.ptp.rdt.sync.core", //$NON-NLS-1$
						Messages.BCM_LocalConnectionError));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.ptp.rdt.sync.core", Messages.BCM_LocalServiceError)); //$NON-NLS-1$
		}
	}

	/**
	 * Create a local build configuration. The corresponding build scenario has no sync provider and points to the project's working
	 * directory. It has a default name and description. This function is normally used as part of the setup when creating a new project
	 * and is of little value to most clients.
	 * 
	 * @param project
	 *            The project needing a local configuration - cannot be null
	 * @return the new configuration - can be null on problems during creation
	 */
	public IConfiguration createLocalConfiguration(IProject project) {
		checkProject(project);
		try {
			BuildScenario localBuildScenario = this.createLocalBuildScenario(project);
			if (localBuildScenario != null) {
				return this.createConfiguration(project, localBuildScenario, Messages.WorkspaceConfigName,
						Messages.BCM_WorkspaceConfigDes);
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
	 * Returns the service configuration set for the given build configuration, or null if it is unavailable.
	 * 
	 * @param bconf
	 *            The build configuration - cannot be null
	 * @return service configuration for the build configuration or null on problems retrieving the configuration.
	 * @deprecated This method is inefficient and can easily be used incorrectly. It is inefficient because it requires a copy of
	 * 				the project's template service configuration. Also, sync'ing with the contained provider precludes optimizations
	 * 				done by the true provider in the template. Finally, changing data on this copy has no effect, except for the
	 * 				data stored in the copy. Instead, use {@link #getBuildScenarioForBuildConfiguration} when you need data about
	 * 				the configuration and use {@link #getSyncRunnerForBuildConfiguration(IConfiguration)} when you need to use the
	 * 				contained sync provider for sync'ing.
	 * 				
	 */
	public IServiceConfiguration getConfigurationForBuildConfiguration(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		checkProject(project);

		BuildScenario bs = this.getBuildScenarioForBuildConfiguration(bconf);
		// Should never happen, but if it does do not continue. (Function call should have invoked error handling.)
		if (bs == null) {
			return null;
		}
		
        IServiceConfiguration sconf = copyTemplateServiceConfiguration(project);
        this.modifyServiceConfigurationForBuildScenario(sconf, project, bs);
		return sconf;
	}
	
	/**
	 * Get a SyncRunner object that can be used to do sync'ing.
	 *
	 * @param bconf
	 * @return SyncRunner
	 */
	public SyncRunner getSyncRunnerForBuildConfiguration(IConfiguration bconf) {
		IProject project = bconf.getOwner().getProject();
		checkProject(project);

		ISyncServiceProvider provider = this.getProjectSyncServiceProvider(project);
		if (provider == null) { // Error handled in call
			return null;
		} else {
			return new SyncRunner(provider);
		}
	}
	
    // Does the low-level work of creating a copy of a service configuration
    // Returned configuration is never null.
    // This method supports deprecated code and can be removed once {@link #getConfigurationForBuildConfiguration} is removed.
    private IServiceConfiguration copyTemplateServiceConfiguration(IProject project) {
            IServiceConfiguration newConfig = ServiceModelManager.getInstance().newServiceConfiguration(""); //$NON-NLS-1$
            if (newConfig == null) {
                    throw new RuntimeException(Messages.BuildConfigurationManager_15);
            }
            String oldConfigId = getTemplateServiceConfigurationId(project);
            IServiceConfiguration oldConfig = ServiceModelManager.getInstance().getConfiguration(oldConfigId);
            if (oldConfig == null) {
                    RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_10 + oldConfigId + Messages.BuildConfigurationManager_11 + project.getName());
                    return null;
            }

            for (IService service : oldConfig.getServices()) {
                    ServiceProvider oldProvider = (ServiceProvider) oldConfig.getServiceProvider(service);
                    try {
                            // The memento creation methods seem the most robust way to copy state. It is more robust than
                    	    // getProperties() and setProperties(), which saveState() and restoreState() use by default but which
                    	    // can be overriden by subclasses.
                            ServiceProvider newProvider = oldProvider.getClass().newInstance();
                            XMLMemento oldProviderState = XMLMemento.createWriteRoot("provider"); //$NON-NLS-1$
                            oldProvider.saveState(oldProviderState);
                            newProvider.restoreState(oldProviderState);
                            newConfig.setServiceProvider(service, newProvider);
                    } catch (InstantiationException e) {
                            throw new RuntimeException(Messages.BCM_ProviderError + oldProvider.getClass());
                    } catch (IllegalAccessException e) {
                            throw new RuntimeException(Messages.BCM_ProviderError + oldProvider.getClass());
                    }
            }

            return newConfig;
    }

    // Does the low-level work of changing a service configuration for a new build scenario.
    // This method supports deprecated code and can be removed once {@link #getConfigurationForBuildConfiguration} is removed.
	private void modifyServiceConfigurationForBuildScenario(IServiceConfiguration sConfig, IProject project, BuildScenario bs) {
		IService syncService = null; // Only set if sync service should be disabled
		for (IService service : sConfig.getServices()) {
			ServiceProvider provider = (ServiceProvider) sConfig.getServiceProvider(service);
			if (provider instanceof IRemoteExecutionServiceProvider) {
				// For local configuration, for example, that does not need to sync
				if (provider instanceof ISyncServiceProvider && bs.getSyncProvider() == null) {
					syncService = service;
				} else {
					((IRemoteExecutionServiceProvider) provider).setRemoteToolsConnection(bs.getRemoteConnection());
					((IRemoteExecutionServiceProvider) provider).setConfigLocation(bs.getLocation(project));

				}
			}
		}
		if (syncService != null) {
			sConfig.disable(syncService);
		}
	}


	/**
	 * Return the name of the sync provider for this project, as stored in the project's template service configuration.
	 * 
	 * @param project - cannot be null
	 * @return sync provider name or null if provider cannot be loaded (should not normally happen)
	 */
	public String getProjectSyncProvider(IProject project) {
		ISyncServiceProvider provider = this.getProjectSyncServiceProvider(project);
		if (provider == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_13 + project.getName());
			return null;
		}

		return provider.getName();
	}
	
	/**
	 * Return the sync service provider for this project, as stored in the project's template service configuration
	 *
	 * @param project
	 * @return the service provider
	 */
	private ISyncServiceProvider getProjectSyncServiceProvider(IProject project) {
		checkProject(project);
		String serviceConfigId = getTemplateServiceConfigurationId(project);
		IServiceConfiguration serviceConfig = ServiceModelManager.getInstance().getConfiguration(serviceConfigId);
		if (serviceConfig == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_10 + serviceConfigId + Messages.BuildConfigurationManager_11 + project.getName());
			return null;
		}

		IService syncService = ServiceModelManager.getInstance().getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		if (syncService == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_12);
			return null;
		}
		
		return (ISyncServiceProvider) serviceConfig.getServiceProvider(syncService);
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
			IRemoteConnection conn = scenario.getRemoteConnection();
			if (conn != null) {
				IRemoteFileManager fileMgr = scenario.getRemoteConnection().getRemoteServices().getFileManager(conn);
				return fileMgr.toURI(path);
			}
		}
		return null;
	}

	/**
	 * Initialize a project. Set the project's template service configuration to the passed configuration and set all current build
	 * configurations to use the passed build scenario. This function must be called before any calls to get or set methods.
	 * 
	 * The template service configuration is the one that is copied and modified to create a custom configuration for each build
	 * configuration.
	 * 
	 * @param project - cannot be null
	 * @param sc - the service configuration - cannot be null
	 * @param bs - the build scenario - cannot be null
	 */
	public void initProject(IProject project, IServiceConfiguration sc, BuildScenario bs) {
		if (project == null || sc == null || bs == null) {
			throw new NullPointerException();
		}
		
		// Store configuration independently of project, which can be useful if the project is deleted.
		ServiceModelManager smm = ServiceModelManager.getInstance();
		smm.addConfiguration(sc);
		try {
			smm.saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(e.toString(), e);
		}

		// Cannot call "checkProject" because project not yet initialized
		try {
			if (!project.hasNature(RemoteSyncNature.NATURE_ID)) {
				throw new IllegalArgumentException(Messages.BuildConfigurationManager_6);
			}
		} catch (CoreException e) {
			throw new IllegalArgumentException(Messages.BuildConfigurationManager_8);
		}

		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(projectScopeSyncNode);
		if (node == null) {
			throw new RuntimeException(Messages.BuildConfigurationManager_0);
		}
		node.put(TEMPLATE_KEY, sc.getId());
		flushNode(node);

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
	 * Set the template service configuration for the given project to the given configuration
	 * 
	 * @param project
	 * @param sc
	 */
	public void setTemplateServiceConfiguration(IProject project, IServiceConfiguration sc) {
		checkProject(project);
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(projectScopeSyncNode);
		if (node == null) {
			throw new RuntimeException(Messages.BuildConfigurationManager_0);
		}
		node.put(TEMPLATE_KEY, sc.getId());
		flushNode(node);
	}

	/**
	 * Indicate if the project has yet been initialized.
	 * 
	 * @param project - cannot be null
	 * @return whether or not the project has been initialized
	 */
	public boolean isInitialized(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		if (getTemplateServiceConfigurationId(project) == null) {
			return false;
		} else {
			return true;
		}
	}

	private IConfiguration createConfiguration(IProject project, BuildScenario buildScenario, String configName, String configDesc) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		// For recording of problems during attempt
		Throwable creationException = null;
		String creationError = null;
		boolean configAdded = false;

		ManagedProject managedProject = (ManagedProject) buildInfo.getManagedProject();
		Configuration configParent = (Configuration) buildInfo.getDefaultConfiguration();
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
				RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_10 + parentConfigInfo.configId +
						Messages.BuildConfigurationManager_11 + project.getName());
			}
		}
	}
	
	// Return ID of the project's template service configuration, or null if not found (project not initialized)
	// Returned value is never null
	private static String getTemplateServiceConfigurationId(IProject project) {
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(projectScopeSyncNode);
		if (node == null) {
			throw new RuntimeException(Messages.BuildConfigurationManager_0);
		}
		
		String configId = node.get(TEMPLATE_KEY, null);
		if (configId == null) {
			throw new RuntimeException(Messages.BuildConfigurationManager_9);
		}
		
		return configId;
	}
	
	/**
	 * Return the build scenario for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "updateConfigurations." For configurations still unknown (perhaps newly created configurations not yet recorded in CDT),
	 *  return the build scenario for the closest known ancestor.
	 * 
	 * @param bconf - the build configuration - cannot be null
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
	 * @param buildScenario - cannot be null
	 * @param bconf - the build configuration - cannot be null
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
	 * @param storageName - name of storage module
	 *
	 * @return values in named storage location or null if the storage location does not exist.
	 * @throws CoreException on problems retrieving data
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
	 * @param storageName - name of storage module
	 *
	 * @throws CoreException on problems retrieving data
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
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BuildConfigurationManager_7);
		}
	}
	
	/**
	 * The node flushing mechanism fails if the workspace is locked. So calling "Node.flush()" is not enough. Instead, spawn a
	 * thread that flushes once the workspace is unlocked.
	 *
	 * @param prefNode node to flush
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
					} catch(InterruptedException e) {
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
					} catch(InterruptedException e) {
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
}