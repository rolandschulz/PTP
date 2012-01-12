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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ptp.services.core.ServicesCorePlugin;
import org.eclipse.ptp.services.internal.core.ServiceConfiguration;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Main static class to map CDT build configurations (IConfigurations) to service configurations. For building or sync'ing, this
 * class keeps track of the appropriate service configuration to use. The mapping is actually a three-way map, from build
 * configuration to build scenario to service configuration. The creation and storage of service configurations is mostly
 * transparent to the user. The user only needs to specify the build scenario for the build configuration. On project
 * initialization, though, a default service configuration must be given.
 * 
 */
public class BuildConfigurationManager {
	private static final String DEFAULT_SAVE_FILE_NAME = "BuildConfigurationData.xml"; //$NON-NLS-1$
	private static final String BUILD_CONFIGURATION_ELEMENT_NAME = "build-configuration-data"; //$NON-NLS-1$
	private static final String SERVICE_ELEMENT_NAME = "service"; //$NON-NLS-1$
	private static final String PROVIDER_ELEMENT_NAME = "provider"; //$NON-NLS-1$
	private static final String DISABLED_PROVIDERS_ELEMENT_NAME = "disabledProviders"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_BUILD_SCENARIO_ID = "build-id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_PROJECT = "project"; //$NON-NLS-1$
	private static final String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	private static final String PROVIDER_CONFIGURATION_ELEMENT_NAME = "provider-configuration"; //$NON-NLS-1$
	private static final String ATTR_PROVIDER_ID = "provider-id"; //$NON-NLS-1$
	private static final String BUILD_SCENARIO_ELEMENT_NAME = "build-scenario"; //$NON-NLS-1$
	private static final String CONFIG_ID_ELEMENT_NAME = "config-id-to-build-scenario"; //$NON-NLS-1$
	private static final String TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME = "template-service-configuration-element-name"; //$NON-NLS-1$

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
	 * Load a service provider from persistent state This method is a copy of
	 * org.eclipse.ptp.services.core.ServiceModelManager.loadServiceProvider with minor modifications made to satisfy the compiler.
	 * 
	 * @param providerMemento
	 * @param service
	 * @return the service provider
	 */
	private static IServiceProvider loadServiceProvider(IMemento providerMemento, IService service) {
		if (service == null) {
			return null;
		}

		String providerId = providerMemento.getString(ATTR_PROVIDER_ID);
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		if (descriptor != null) {
			IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(descriptor);
			if (provider != null) {
				if (provider instanceof ServiceProvider) {
					IMemento providerConfigMemento = providerMemento.getChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
					((ServiceProvider) provider).restoreState(providerConfigMemento);
				}
				return provider;
			} else {
				// TODO: What to do here?
				// ServicesCorePlugin.getDefault().logErrorMessage(Messages.ServiceModelManager_2);
			}
		} else {
			// TODO: What to do here?
			// ServicesCorePlugin.getDefault().logErrorMessage(Messages.ServiceModelManager_0 + providerId);
		}
		return null;
	}

	/**
	 * Save the state of a service provider This method is a copy of
	 * org.eclipse.ptp.services.core.ServiceModelManager.saveProviderState
	 * 
	 * @param provider
	 * @param parentMemento
	 */
	private static void saveProviderState(IServiceProvider provider, IMemento parentMemento) {
		if (provider instanceof ServiceProvider) {
			IMemento providerConfigMemento = parentMemento.createChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
			((ServiceProvider) provider).saveState(providerConfigMemento);
		}
	}

	private final Map<IProject, IServiceConfiguration> fProjectToTemplateConfigurationMap = Collections
			.synchronizedMap(new HashMap<IProject, IServiceConfiguration>());

	private final Map<IServiceConfiguration, IProject> fTemplateToProjectMap = Collections
			.synchronizedMap(new HashMap<IServiceConfiguration, IProject>());

	private final Map<String, BuildScenario> fBuildConfigToBuildScenarioMap = Collections
			.synchronizedMap(new HashMap<String, BuildScenario>());

	private final Map<BuildScenario, IServiceConfiguration> fBuildScenarioToSConfigMap = Collections
			.synchronizedMap(new HashMap<BuildScenario, IServiceConfiguration>());

	private static BuildConfigurationManager fInstance = null;

	public static synchronized BuildConfigurationManager getInstance() {
		if (fInstance == null) {
			fInstance = new BuildConfigurationManager();
		}
		return fInstance;
	}

	// TODO: Decide if this is the best way to do initialization and decide best way to handle exceptions.
	private BuildConfigurationManager() {
		try {
			loadConfigurationData();
		} catch (WorkbenchException e) {
			this.handleInitError(e);
		} catch (IOException e) {
			this.handleInitError(e);
		}
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
	 * Return the build scenario for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "updateConfigurations". In addition, for unknown configurations (perhaps newly created configurations not yet recorded in
	 * CDT, return the build scenario for the closest known ancestor. If no ancestor, return null.
	 * 
	 * @param bconf
	 *            The build configuration
	 * @return build scenario
	 */
	public BuildScenario getBuildScenarioForBuildConfiguration(IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToTemplateConfigurationMap.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException(Messages.BCM_InitError);
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		BuildScenario buildScenario = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		if (buildScenario == null) {
			String parentConfigId = findAncestorConfig(bconf.getId());
			if (parentConfigId != null) {
				buildScenario = fBuildConfigToBuildScenarioMap.get(parentConfigId);
			}
		}
		return buildScenario;
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
		if (!(fProjectToTemplateConfigurationMap.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException(Messages.BCM_InitError);
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		BuildScenario bs = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		if (bs == null) {
			return null;
		}

		IServiceConfiguration conf = fBuildScenarioToSConfigMap.get(bs);
		if (conf == null) {
			throw new RuntimeException(Messages.BCM_ScenarioToServiceConfigError);
		}

		return conf;
	}

	/**
	 * Return the name of the sync provider for this project, as stored in the project's template service configuration.
	 * 
	 * @param project
	 * @return sync provider name
	 */
	public String getProjectSyncProvider(IProject project) {
		IServiceConfiguration serviceConfig = fProjectToTemplateConfigurationMap.get(project);
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
		fProjectToTemplateConfigurationMap.put(project, sc);
		fTemplateToProjectMap.put(sc, project);
		initializeOrUpdateConfigurations(project, bs);
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
		return fProjectToTemplateConfigurationMap.containsKey(project);
	}

	/**
	 * Save configuration data to plugin metadata area
	 * 
	 * @throws IOException
	 *             on problems writing configuration data to file
	 */
	public synchronized void saveConfigurationData() throws IOException {
		XMLMemento rootMemento = XMLMemento.createWriteRoot(BUILD_CONFIGURATION_ELEMENT_NAME);

		// First, save template service configurations
		IServiceConfiguration[] templateServiceConfigurationArray = fProjectToTemplateConfigurationMap.values().toArray(
				new IServiceConfiguration[0]);
		saveServiceConfigurations(rootMemento, templateServiceConfigurationArray, TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME);

		// Now save build scenarios, including creating and storing an id number for each.
		Map<BuildScenario, Integer> buildScenarioToIdMap = new HashMap<BuildScenario, Integer>();
		int idNumber = 0;
		synchronized (fBuildScenarioToSConfigMap) {
			for (BuildScenario bs : fBuildScenarioToSConfigMap.keySet()) {
				IMemento bsMemento = rootMemento.createChild(BUILD_SCENARIO_ELEMENT_NAME);
				bs.saveScenario(bsMemento);
				bsMemento.putString(ATTR_SERVICE_ID, fBuildScenarioToSConfigMap.get(bs).getId());
				bsMemento.putInteger(ATTR_ID, idNumber);
				buildScenarioToIdMap.put(bs, idNumber);
				++idNumber;
			}
		}

		// Finally save a map of configuration ids to build scenario ids
		synchronized (fBuildConfigToBuildScenarioMap) {
			for (String configId : fBuildConfigToBuildScenarioMap.keySet()) {
				IMemento configMemento = rootMemento.createChild(CONFIG_ID_ELEMENT_NAME);
				configMemento.putString(ATTR_ID, configId);
				configMemento.putInteger(ATTR_BUILD_SCENARIO_ID,
						buildScenarioToIdMap.get(fBuildConfigToBuildScenarioMap.get(configId)));
			}
		}

		IPath savePath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File saveFile = savePath.toFile();
		rootMemento.save(new FileWriter(saveFile));
	}

	/**
	 * Associate the given configuration with the given build scenario. It is very important that we update configurations first, so
	 * that children of the changed configuration will be properly set to use the prior build scenario. This is not possible,
	 * though, until the project has been initialized.
	 * 
	 * @param buildScenario
	 * @param bconf
	 *            the build configuration
	 */
	public void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
		if (bs == null || bconf == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToTemplateConfigurationMap.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException(Messages.BCM_InitError);
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		setBuildScenarioForBuildConfigurationInternal(bs, bconf);
	}

	/**
	 * Add a new build scenario, creating a new service configuration for that scenario if necessary.
	 * 
	 * @param buildScenario
	 */
	private void addBuildScenario(IProject project, BuildScenario buildScenario) {
		// Check if build scenario already known
		synchronized (fBuildScenarioToSConfigMap) {
			if (fBuildScenarioToSConfigMap.containsKey(buildScenario)) {
				return;
			}
			IServiceConfiguration sConfig = copyTemplateServiceConfiguration(project);
			modifyServiceConfigurationForBuildScenario(sConfig, buildScenario);
			fBuildScenarioToSConfigMap.put(buildScenario, sConfig);
		}
	}

	// Does the low-level work of creating a copy of a service configuration
	private IServiceConfiguration copyTemplateServiceConfiguration(IProject project) {
		IServiceConfiguration newConfig = ServiceModelManager.getInstance().newServiceConfiguration(""); //$NON-NLS-1$
		IServiceConfiguration oldConfig = fProjectToTemplateConfigurationMap.get(project);
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
			this.setBuildScenarioForBuildConfigurationInternal(buildScenario, config);
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
				try {
					this.saveConfigurationData();
				} catch (IOException e) {
					projectDes.removeConfiguration(configDes);
				}
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

	/**
	 * Delete a build scenario and also handle removal of unneeded service configurations
	 * 
	 * @param project
	 * @param buildScenario
	 */
	private void deleteBuildScenario(IProject project, BuildScenario buildScenario) {
		IServiceConfiguration oldConfig = fBuildScenarioToSConfigMap.get(buildScenario);
		if (oldConfig == null) {
			throw new RuntimeException(Messages.BCM_ScenarioToServiceConfigError);
		}
		fBuildScenarioToSConfigMap.remove(buildScenario);
	}

	/**
	 * Do the actual job of loading configurations. This method is copied from
	 * org.eclipse.ptp.services.core.ServiceModelManager.doLoadConfigurations, which is private. It has been modified to load
	 * project names and map projects to templates. The import parameter has been removed. (We do not care about the id numbers,
	 * since these configurations are for our own internal purposes.) Also, the memento name is now a parameter. Other minor
	 * modifications made to satisfy the compiler.
	 * 
	 * @param rootMemento
	 */
	private void doLoadConfigurations(IMemento rootMemento, String mementoChildName) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		Set<IServiceConfiguration> configs = new HashSet<IServiceConfiguration>();

		for (IMemento configMemento : rootMemento.getChildren(mementoChildName)) {
			String configName = configMemento.getString(ATTR_NAME);
			String projectName = configMemento.getString(ATTR_PROJECT);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				throw new RuntimeException(Messages.BCM_ProjectError + project);
			}
			// Interface IServiceConfiguration cannot be used because of "addFormerServiceProvider" method.
			ServiceConfiguration config = (ServiceConfiguration) smm.newServiceConfiguration(configName);

			for (IMemento serviceMemento : configMemento.getChildren(SERVICE_ELEMENT_NAME)) {
				String serviceId = serviceMemento.getString(ATTR_ID);
				IService service = smm.getService(serviceId);
				IServiceProvider provider = loadServiceProvider(serviceMemento, service);
				config.setServiceProvider(service, provider);
			}

			for (IMemento disabledMemento : configMemento.getChildren(DISABLED_PROVIDERS_ELEMENT_NAME)) {
				String serviceId = disabledMemento.getString(ATTR_ID);
				IService service = smm.getService(serviceId);
				for (IMemento providerMemento : disabledMemento.getChildren(PROVIDER_ELEMENT_NAME)) {
					IServiceProvider provider = loadServiceProvider(providerMemento, service);
					config.addFormerServiceProvider(service, provider);
				}
			}

			configs.add(config);
			fProjectToTemplateConfigurationMap.put(project, config);
			fTemplateToProjectMap.put(config, project);
		}
	}

	// Find the closest ancestor of the configuration that we have recorded.
	private String findAncestorConfig(String configId) {
		while ((configId = getParentId(configId)) != null) {
			if (fBuildConfigToBuildScenarioMap.containsKey(configId)) {
				return configId;
			}
		}
		return null;

	}

	private void handleInitError(Throwable e) {
		fInstance = null;
		RDTSyncCorePlugin.log(Messages.BCM_InitializationError, e);
	}

	// If build scenario is not null, then set all configurations to use that build scenario (initialize). If null, set all
	// configurations to the build scenario of their nearest ancestor (update).
	private void initializeOrUpdateConfigurations(IProject project, BuildScenario bs) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.BCM_BuildInfoError + project.getName());
		}

		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			// Update
			if (bs == null) {
				if (!(fBuildConfigToBuildScenarioMap.containsKey(config.getId()))) {
					String parentConfig = findAncestorConfig(config.getId());
					if (parentConfig == null) {
						throw new RuntimeException(Messages.BCM_AncestorError + config.getId());
					}
					setBuildScenarioForBuildConfigurationInternal(fBuildConfigToBuildScenarioMap.get(parentConfig), config);

				}
				// Initialize
			} else {
				setBuildScenarioForBuildConfigurationInternal(bs, config);
			}
		}
	}

	/**
	 * Load configuration data. All previously stored data is erased.
	 * 
	 * @throws IOException
	 */
	private void loadConfigurationData() throws IOException, WorkbenchException {
		// Setup root memento
		IPath loadPath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File loadFile = loadPath.toFile();
		if (!(loadFile.exists())) {
			return;
		}

		BufferedReader reader = new BufferedReader(new FileReader(loadFile));
		XMLMemento rootMemento;
		try {
			rootMemento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			throw e;
		}

		// Clear all data structures
		fProjectToTemplateConfigurationMap.clear();
		fTemplateToProjectMap.clear();
		fBuildConfigToBuildScenarioMap.clear();
		fBuildScenarioToSConfigMap.clear();

		// Load service configurations
		doLoadConfigurations(rootMemento, TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME);

		// Load all configurations for all projects and stash into a temporary map
		Map<String, IConfiguration> confSet = new HashMap<String, IConfiguration>();
		for (IProject project : fProjectToTemplateConfigurationMap.keySet()) {
			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
			if (buildInfo == null) {
				continue;
			}
			IConfiguration[] bconfs = buildInfo.getManagedProject().getConfigurations();
			for (IConfiguration bconf : bconfs) {
				confSet.put(bconf.getId(), bconf);
			}
		}

		// Load build scenarios - stash them into a temporary map also
		Map<Integer, BuildScenario> IdToBuildScenarioMap = new HashMap<Integer, BuildScenario>();
		for (IMemento bsMemento : rootMemento.getChildren(BUILD_SCENARIO_ELEMENT_NAME)) {
			BuildScenario bs = BuildScenario.loadScenario(bsMemento);
			Integer id = bsMemento.getInteger(ATTR_ID);
			IdToBuildScenarioMap.put(id, bs);
		}

		// Load config id to build scenario id mappings and use our available machinery to rebuild maps and custom service
		// configurations. We skip any configurations not found earlier.
		for (IMemento configMemento : rootMemento.getChildren(CONFIG_ID_ELEMENT_NAME)) {
			String configId = configMemento.getString(ATTR_ID);
			Integer buildScenarioId = configMemento.getInteger(ATTR_BUILD_SCENARIO_ID);
			IConfiguration config = confSet.get(configId);
			if (config == null) {
				continue;
			}
			BuildScenario bs = IdToBuildScenarioMap.get(buildScenarioId);
			if (bs == null) {
				continue;
			}
			setBuildScenarioForBuildConfigurationInternal(IdToBuildScenarioMap.get(buildScenarioId), config);
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

	/**
	 * Save a collection of service configurations to the memento NOTE: does not actually save the memento. This method is copied
	 * from org.eclipse.ptp.services.core.ServiceModelManager.saveConfigurations, which could not be used directly because it is
	 * private. It has been modified to input a memento name and to save project name.
	 * 
	 * @param memento
	 *            memento used to save configurations
	 * @param configs
	 *            collection of service configurations to save
	 */
	private void saveServiceConfigurations(IMemento memento, IServiceConfiguration[] configs, String mementoChildName) {
		for (IServiceConfiguration config : configs) {
			String configurationName = config.getName();
			String projectName = fTemplateToProjectMap.get(config).getName();

			IMemento configMemento = memento.createChild(mementoChildName);
			configMemento.putString(ATTR_NAME, configurationName);
			configMemento.putString(ATTR_PROJECT, projectName);

			Set<IService> services = config.getServices();
			for (IService service : services) {
				if (!config.isDisabled(service)) {
					IServiceProvider provider = config.getServiceProvider(service);
					IMemento serviceMemento = configMemento.createChild(SERVICE_ELEMENT_NAME);
					serviceMemento.putString(ATTR_ID, service.getId());
					serviceMemento.putString(ATTR_PROVIDER_ID, provider.getId());
					saveProviderState(provider, serviceMemento);
				}

				if (config instanceof ServiceConfiguration) {
					Set<IServiceProvider> disabledProviders = ((ServiceConfiguration) config).getFormerServiceProviders(service);
					if (!disabledProviders.isEmpty()) {
						IMemento disabledMemento = configMemento.createChild(DISABLED_PROVIDERS_ELEMENT_NAME);
						disabledMemento.putString(ATTR_ID, service.getId());
						for (IServiceProvider disabledProvider : disabledProviders) {
							IMemento providerMemento = disabledMemento.createChild(PROVIDER_ELEMENT_NAME);
							providerMemento.putString(ATTR_PROVIDER_ID, disabledProvider.getId());
							saveProviderState(disabledProvider, providerMemento);
						}
					}
				}
			}
		}
	}

	// Actual internal code for setting a build scenario
	private void setBuildScenarioForBuildConfigurationInternal(BuildScenario bs, IConfiguration bconf) {
		synchronized (fBuildConfigToBuildScenarioMap) {
			BuildScenario oldbs = fBuildConfigToBuildScenarioMap.get(bconf.getId());
			fBuildConfigToBuildScenarioMap.put(bconf.getId(), bs);
			// Remove build scenarios no longer referenced. This ensures that, at least, there are no more build scenarios than
			// there
			// are build configurations.
			if ((oldbs != null) && (!(fBuildConfigToBuildScenarioMap.containsValue(oldbs)))) {
				deleteBuildScenario(bconf.getOwner().getProject(), oldbs);
			}
			addBuildScenario(bconf.getOwner().getProject(), bs);
		}
	}
}
