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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
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
 * Main static class to map CDT build configurations (IConfigurations) to service configurations. For building or sync'ing, this
 * class keeps track of the appropriate service configuration to use. The mapping is actually a three-way map, from build
 * configuration to build scenario to service configuration. The creation and storage of service configurations is mostly
 * transparent to the user. The user only needs to specify the build scenario for the build configuration. On project
 * initialization, though, a default service configuration must be given.
 * 
 */
public class BuildConfigurationManager {
	private static final String projectScopeSyncNode = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String CONFIG_NODE_NAME = "config"; //$NON-NLS-1$
	private static final String TEMPLATE_KEY = "template"; //$NON-NLS-1$
	private final Map<IConfiguration, IServiceConfiguration> fBConfigToSConfigMap = Collections
			.synchronizedMap(new HashMap<IConfiguration, IServiceConfiguration>());
	
	// Setup as a singleton
	private BuildConfigurationManager() {
	}

	private static BuildConfigurationManager fInstance = null;

	public static synchronized BuildConfigurationManager getInstance() {
		if (fInstance == null) {
			fInstance = new BuildConfigurationManager();
		}
		return fInstance;
	}

	/**
	 * Create a build scenario for configurations that build in the local Eclipse workspace
	 * 
	 * @param project
	 * @return the build scenario
	 * @throws CoreException
	 *             on problems getting local resources, either the local connection or local services
	 */
	public BuildScenario createLocalBuildScenario(IProject project) throws CoreException {
		IRemoteServices localService = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				"org.eclipse.ptp.remote.LocalServices", null); //$NON-NLS-1$

		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection("Local"); //$NON-NLS-1$
			if (localConnection != null) {
				return new BuildScenario(null, localConnection, project.getLocation().toString());
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
	 * directory. It has a default name and description.
	 * 
	 * @param project
	 *            The project needing a local configuration
	 */
	public IConfiguration createLocalConfiguration(IProject project) {
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
	 * Create a remote build configuration.
	 * 
	 * @param project
	 *            The project needing a remote configuration
	 * @param remoteBuildScenario
	 *            Configuration's build scenario
	 * @param configName
	 *            Configuration's name
	 * @param configDesc
	 *            Configuration's description
	 */
	public IConfiguration createRemoteConfiguration(IProject project, BuildScenario remoteBuildScenario, String configName,
			String configDesc) {
		return this.createConfiguration(project, remoteBuildScenario, configName, configDesc);
	}

	/**
	 * Get the synchronize location URI of the resource associated with the active build configuration. Returns null if the project
	 * containing the resource is not a synchronized project.
	 * 
	 * @param resource
	 *            target resource
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
	 *            The build configuration
	 * @return service configuration for the build configuration
	 * @throws RuntimeException
	 *             if the build scenario cannot be mapped to a service configuration. This should never happen.
	 */
	public IServiceConfiguration getConfigurationForBuildConfiguration(IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}

		IProject project = bconf.getOwner().getProject();
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}

		IServiceConfiguration sconf = fBConfigToSConfigMap.get(bconf);
		if (sconf == null) {
			BuildScenario bs = this.getBuildScenarioForBuildConfiguration(bconf);
			if (bs == null) {
				return null;
			}
            sconf = copyTemplateServiceConfiguration(project);
            modifyServiceConfigurationForBuildScenario(sconf, bs);
            fBConfigToSConfigMap.put(bconf, sconf);
		}
		
		return sconf;
	}

	/**
	 * Return the name of the sync provider for this project, as stored in the project's template service configuration.
	 * 
	 * @param project
	 * @return sync provider name
	 */
	public String getProjectSyncProvider(IProject project) {
		String serviceConfigId = getTemplateServiceConfigurationId(project);
		if (serviceConfigId == null) {
			return null;
		}
		IServiceConfiguration serviceConfig = ServiceModelManager.getInstance().getConfiguration(serviceConfigId);
		if (serviceConfig == null) {
			return null;
		}

		IService syncService = ServiceModelManager.getInstance().getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		return serviceConfig.getServiceProvider(syncService).getName();
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
	 * scenario.
	 * 
	 * @param project
	 * @param sc
	 *            The service configuration
	 * @param bs
	 *            The build scenario
	 */
	public void initProject(IProject project, IServiceConfiguration sc, BuildScenario bs) {
		if (project == null || sc == null || bs == null) {
			throw new NullPointerException();
		}

		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(projectScopeSyncNode);
		if (node == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_0);
			return;
		}
		node.put(TEMPLATE_KEY, sc.getId());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_1, e);
		}

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
	 * Indicate if the project has yet been initialized.
	 * 
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

	// Does the low-level work of creating a copy of a service configuration
	private IServiceConfiguration copyTemplateServiceConfiguration(IProject project) {
		IServiceConfiguration newConfig = ServiceModelManager.getInstance().newServiceConfiguration(""); //$NON-NLS-1$
		IServiceConfiguration oldConfig = ServiceModelManager.getInstance().getConfiguration(getTemplateServiceConfigurationId(project));
		if (oldConfig == null) {
			throw new RuntimeException(Messages.BCM_TemplateError + project.getName());
		}

		for (IService service : oldConfig.getServices()) {
			ServiceProvider oldProvider = (ServiceProvider) oldConfig.getServiceProvider(service);
			try {
				// The memento creation methods seem the most robust way to copy state. It is more robust than getProperties() and
				// setProperties(), which saveState() and restoreState() use by default but which can be overriden by subclasses.
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

	private IConfiguration createConfiguration(IProject project, BuildScenario buildScenario, String configName, String configDesc) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			RDTSyncCorePlugin.log(Messages.BCM_CreateConfigFailure + Messages.BCM_GetBuildInfoError);
			return null;
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
			config.setConfigurationDescription(configDes);
			configDes.setName(configName);
			configDes.setDescription(configDesc);
			config.getToolChain().getBuilder().setBuildPath(project.getLocation().toString());
			configAdded = true;
			try {
				CoreModel.getDefault().setProjectDescription(project, projectDes, true, null);
			} catch (CoreException e) {
				projectDes.removeConfiguration(configDes);
				configAdded = false;
				creationException = e;
				creationError = Messages.BCM_SetConfigDescriptionError;
			}
			if (configAdded) {
				this.setBuildScenarioForBuildConfigurationInternal(buildScenario, config);
			}
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
		
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}

		// Get project root preference node
		IScopeContext context = new ProjectScope(project);
		Preferences prefRootNode = context.getNode(projectScopeSyncNode);
		if (prefRootNode == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_0);
			return;
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			BuildScenarioAndConfiguration parentConfigInfo = getBuildScenarioForBuildConfigurationInternal(config);
			if (parentConfigInfo == null) {
				throw new RuntimeException(Messages.BCM_AncestorError + config.getId());
			}
			if (parentConfigInfo.configId == config.getId()) {
				continue;
			}
			IConfiguration parentConfig = buildInfo.getManagedProject().getConfiguration(parentConfigInfo.configId);
			if (parentConfig != null) {
				setBuildScenarioForBuildConfigurationInternal(parentConfigInfo.bs, config);
			} else {
				RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_5 + config.getId());
			}
		}
	}

	// Does the low-level work of changing a service configuration for a new build scenario.
	private void modifyServiceConfigurationForBuildScenario(IServiceConfiguration sConfig, BuildScenario bs) {
		IService syncService = null;
		for (IService service : sConfig.getServices()) {
			ServiceProvider provider = (ServiceProvider) sConfig.getServiceProvider(service);
			if (provider instanceof IRemoteExecutionServiceProvider) {
				// For local configuration, for example, that does not need to sync
				if (provider instanceof ISyncServiceProvider && bs.getSyncProvider() == null) {
					syncService = service;
				} else {
					((IRemoteExecutionServiceProvider) provider).setRemoteToolsConnection(bs.getRemoteConnection());
					((IRemoteExecutionServiceProvider) provider).setConfigLocation(bs.getLocation());

				}
			}
		}
		if (syncService != null) {
			sConfig.disable(syncService);
		}
	}
	
	// Return ID of the project's template service configuration, or null if not found (project not initialized)
	private static String getTemplateServiceConfigurationId(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}

		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(projectScopeSyncNode);
		if (node == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_0);
			return null;
		}
		
		return node.get(TEMPLATE_KEY, null);
	}
	
	/**
	 * Return the build scenario for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "initializeOrUpdateConfigurations." For configurations still unknown (perhaps newly created configurations not yet recorded
	 *  in CDT), return the build scenario for the closest known ancestor.
	 * 
	 * @param bconf
	 *            The build configuration
	 * @return build scenario or null if there are problems accessing configuration's information
	 */
	public BuildScenario getBuildScenarioForBuildConfiguration(IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}

		IProject project = bconf.getOwner().getProject();
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}
		
		updateConfigurations(project);
		BuildScenario bs = this.getBuildScenarioForBuildConfigurationInternal(bconf).bs;
		if (bs == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_4 + bconf.getName());
		}
		
		return bs;
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
		if (bconf == null) {
			throw new NullPointerException();
		}
		
		IProject project = bconf.getOwner().getProject();
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}
		
		IScopeContext context = new ProjectScope(project);
		Preferences prefRootNode = context.getNode(projectScopeSyncNode);
		if (prefRootNode == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_0);
			return null;
		}

		try {
			if (!prefRootNode.nodeExists(CONFIG_NODE_NAME)) {
				RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_2);
				return null;
			}
			
			String configId = bconf.getId();
			Preferences prefGeneralConfigNode = prefRootNode.node(CONFIG_NODE_NAME);
			while (configId != null && !prefGeneralConfigNode.nodeExists(configId)) {
				configId = getParentId(configId);
			}
			
			if (configId != null) {
				BuildScenario bs = BuildScenario.loadScenario(prefGeneralConfigNode.node(configId));
				return new BuildScenarioAndConfiguration(bs, configId);
			} else {
				return null;
			}
		} catch (BackingStoreException e) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_2, e);
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
	 * Associate the given configuration with the given build scenario. It is very important that we update configurations first,
	 * so that children of the changed configuration will be properly set to use the prior build scenario. This is not possible,
	 * though, until the project has been initialized.
	 * 
	 * @param buildScenario
	 * @param bconf
	 *            the build configuration
	 */
	public void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}
		
		IProject project = bconf.getOwner().getProject();
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}

		updateConfigurations(project);
		this.setBuildScenarioForBuildConfigurationInternal(bs, bconf);
	}
	
	private void setBuildScenarioForBuildConfigurationInternal(BuildScenario bs, IConfiguration bconf) {
		if (bs == null || bconf == null) {
			throw new NullPointerException();
		}
		
		IProject project = bconf.getOwner().getProject();
		if (!isInitialized(project)) {
			throw new RuntimeException(Messages.BCM_InitError);
		}

		// Get project root preference node
		IScopeContext context = new ProjectScope(project);
		Preferences prefRootNode = context.getNode(projectScopeSyncNode);
		if (prefRootNode == null) {
			RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_0);
			return;
		}

		Preferences prefConfigNode = prefRootNode.node(CONFIG_NODE_NAME + "/" + bconf.getId()); //$NON-NLS-1$
		bs.saveScenario(prefConfigNode);
	}
}