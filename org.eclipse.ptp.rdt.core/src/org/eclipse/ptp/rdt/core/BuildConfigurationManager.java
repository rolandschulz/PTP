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
package org.eclipse.ptp.rdt.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.IRemoteServiceProvider;
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
 * Static class to map CDT build configurations (IConfigurations) to service configurations 
 * @since 3.1
 */
public class BuildConfigurationManager {
	private static final String DEFAULT_SAVE_FILE_NAME = "BuildConfigurationData.xml";  //$NON-NLS-1$
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
	private static final String LOCAL_CONFIGURATION_NAME = "Workspace"; //$NON-NLS-1$
	private static final String LOCAL_CONFIGURATION_DES = "Build in local Eclipse workspace"; //$NON-NLS-1$

	private static final Map<IProject, IServiceConfiguration> fProjectToTemplateConfiguration =
																					new HashMap<IProject, IServiceConfiguration>();
	private static final Map<IServiceConfiguration, IProject> fTemplateToProjectMap =
																					new HashMap<IServiceConfiguration, IProject>();
	private static final Map<String, BuildScenario> fBuildConfigToBuildScenarioMap =
																							new HashMap<String,BuildScenario>();
	private static final Map<BuildScenario, IServiceConfiguration> fBuildScenarioToSConfigMap =
																			new HashMap<BuildScenario, IServiceConfiguration>();

	// TODO: Figure out if this is the best way to do initialization and figure out best way to handle exceptions.
	static {
		try {
			loadConfigurationData();
		} catch (WorkbenchException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Add a new build scenario, creating a new service configuration for that scenario if necessary.
	 *
	 * @param buildScenario
	 * @since 2.1
	 */
	private static synchronized void addBuildScenario(IProject project, BuildScenario buildScenario) {
		// Check if build scenario already known
		if (fBuildScenarioToSConfigMap.containsKey(buildScenario)) {
			return;
		}
		IServiceConfiguration sConfig =copyTemplateServiceConfiguration(project);
		modifyServiceConfigurationForBuildScenario(sConfig, buildScenario);
		fBuildScenarioToSConfigMap.put(buildScenario, sConfig);
	}
	
	/**
	 * Delete a build scenario, also handling removal of unneeded service configurations
	 * @param project
	 * @param buildScenario
	 */
	private static synchronized void deleteBuildScenario(IProject project, BuildScenario buildScenario) {
		IServiceConfiguration oldConfig = fBuildScenarioToSConfigMap.get(buildScenario);
		if (oldConfig == null) {
			throw new RuntimeException("Unable to find service configuration for build scenario"); //$NON-NLS-1$
		}
		fBuildScenarioToSConfigMap.remove(buildScenario);
	}
	
	/**
	 * Return the build scenario for the passed configuration. Any newly created configurations should be recorded by the call to
	 * "updateConfigurations".
	 * 
	 * @param bconf
	 * 				The build configuration
	 * @return build scenario
	 * @since 2.1
	 */
	public static synchronized BuildScenario getBuildScenarioForBuildConfiguration(IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToTemplateConfiguration.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException("Project configurations not yet initialized."); //$NON-NLS-1$
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		return fBuildConfigToBuildScenarioMap.get(bconf.getId());
	}
	
	/**
	 * Associate the given configuration with the given build scenario. It is very important that we update configurations first,
	 * so that children of the changed configuration will be properly set to use the prior build scenario. This is not possible,
	 * though, until the project has been initialized (root configurations have been inserted).
	 *
	 * @param buildScenario
	 * @param bconf
	 * 				the build configuration
	 * @since 2.1
	 */
	public static synchronized void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
		if (bs == null || bconf == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToTemplateConfiguration.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException("Project configurations not yet initialized."); //$NON-NLS-1$
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		setBuildScenarioForBuildConfigurationInternal(bs, bconf);
	}
	
	// Actual internal code for setting a build scenario
	private static synchronized void setBuildScenarioForBuildConfigurationInternal(BuildScenario bs, IConfiguration bconf) {
		BuildScenario oldbs = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		fBuildConfigToBuildScenarioMap.put(bconf.getId(), bs);
		// Remove build scenarios no longer referenced. This ensures that, at least, there are no more build scenarios than there
		// are build configurations.
		if ((oldbs != null) && (!(fBuildConfigToBuildScenarioMap.containsValue(oldbs)))) {
			deleteBuildScenario(bconf.getOwner().getProject(), oldbs);
		}
		addBuildScenario(bconf.getOwner().getProject(), bs);
	}

	/**
	 * Returns the service configuration set for the given build configuration, or null if it is unavailable.
	 * 
	 * @param bconf
	 * 			The build configuration
	 * @return service configuration for the build configuration
	 * @throws RuntimeException if the build scenario cannot be mapped to a service configuration. This should never happen.
	 * @since 2.1
	 */
	public static synchronized IServiceConfiguration getConfigurationForBuildConfiguration(IConfiguration bconf) {
		if (bconf == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToTemplateConfiguration.containsKey(bconf.getOwner().getProject()))) {
			throw new RuntimeException("Project configurations not yet initialized."); //$NON-NLS-1$
		}
		initializeOrUpdateConfigurations(bconf.getOwner().getProject(), null);
		BuildScenario bs = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		if (bs == null) {
			return null;
		}
		 
		 IServiceConfiguration conf = fBuildScenarioToSConfigMap.get(bs);
		 if (conf == null) {
			 throw new RuntimeException("Unable to find a service configuration for the build scenario"); //$NON-NLS-1$
		 }
		 
		 return conf;
	}

	/**
	 * Return the template service configuration for the given project. Returns null if no template for the project
	 *
	 * @param project
	 * @return template configuration for the project
	 * @since 2.1
	 */
	public static synchronized IServiceConfiguration getBuildSystemTemplateConfiguration(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		return fProjectToTemplateConfiguration.get(project);
	}
	
	private static synchronized void modifyServiceConfigurationForBuildScenario(IServiceConfiguration sConfig, BuildScenario bs) {
		for (IService service : sConfig.getServices()) {
			ServiceProvider provider = (ServiceProvider) sConfig.getServiceProvider(service);
			if (provider instanceof IRemoteServiceProvider) {
				// For local configuration, for example, that does not need to sync
				if (provider instanceof ISyncServiceProvider && bs.getSyncProvider() == null) {
					sConfig.disable(service);
				} else {
					((IRemoteServiceProvider) provider).changeRemoteInformation(bs.getRemoteConnection(), bs.getLocation());
				}
			}
		}
	}
	private static synchronized IServiceConfiguration copyTemplateServiceConfiguration(IProject project) {
		IServiceConfiguration newConfig = ServiceModelManager.getInstance().newServiceConfiguration(""); //$NON-NLS-1$
		IServiceConfiguration oldConfig = fProjectToTemplateConfiguration.get(project);
		if (oldConfig == null) {
			throw new RuntimeException("No template service configuration set for project " + project.getName()); //$NON-NLS-1$
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
				throw new RuntimeException("Cannot instantiate provider class: " + oldProvider.getClass()); //$NON-NLS-1$
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot instantiate provider class: " + oldProvider.getClass()); //$NON-NLS-1$
			}
		}
		
		return newConfig;
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
	 * 			The service configuration
	 * @param bs
	 * 			The build scenario
	 */
	public static synchronized void initProject(IProject project, IServiceConfiguration sc, BuildScenario bs) {
		if (project == null || sc == null || bs == null) {
			throw new NullPointerException();
		}
		fProjectToTemplateConfiguration.put(project, sc);
		fTemplateToProjectMap.put(sc, project);
		initializeOrUpdateConfigurations(project, bs);
	}

	// If build scenario is not null, then set all configurations to use that build scenario (initialize). If null, set all
	// configurations to the build scenario of their nearest ancestor (update).
	private static synchronized void initializeOrUpdateConfigurations(IProject project, BuildScenario bs) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		
		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			// Update
			if (bs == null) {
				if (!(fBuildConfigToBuildScenarioMap.containsKey(config.getId()))) {
					String parentConfig = findAncestorConfig(config.getId());
					if (parentConfig == null) {
						throw new RuntimeException("Failed to find an ancestor for build configuration " + config.getId()); //$NON-NLS-1$
					}
					setBuildScenarioForBuildConfigurationInternal(fBuildConfigToBuildScenarioMap.get(parentConfig), config);
					
				}
			// Initialize
			} else {
				setBuildScenarioForBuildConfigurationInternal(bs, config);
			}
		}
	}
	
	// Find the closest ancestor of the configuration that we have recorded.
	private static String findAncestorConfig(String configId) {
		while ((configId = getParentId(configId)) != null) {
			if (fBuildConfigToBuildScenarioMap.containsKey(configId)) {
				return configId;
			}
		}
		return null;
		
	}
	
	// Each new configuration id appends a number to the parent id. So we strip off the last id number to get the parent. We assume
	// the configuration does not have a parent and return null if the result does not end with a number.
	private static String getParentId(String configId) {
		String idRegEx = "\\.\\d+$"; //$NON-NLS-1$
		Pattern idPattern = Pattern.compile(idRegEx);
		String parentConfigId = configId.replaceFirst(idRegEx, ""); //$NON-NLS-1$
		
		if (idPattern.matcher(parentConfigId).find()) {
			return parentConfigId;
		} else {
			return null;
		}
	}

	/*
	 * Indicate if the project has yet been initialized.
	 * 
	 * @return whether or not the project has been initialized
	 */
	public static boolean isInitialized(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		return fProjectToTemplateConfiguration.containsKey(project);
	}
	
	/**
	 * Save configuration data to plugin metadata area
	 * 
	 * @throws IOException
	 * 						on problems writing configuration data to file
	 */
	public static synchronized void saveConfigurationData() throws IOException {
		IPath savePath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File saveFile = savePath.toFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
		XMLMemento rootMemento = XMLMemento.createWriteRoot(BUILD_CONFIGURATION_ELEMENT_NAME);
		
		// First, save template service configurations
		IServiceConfiguration[] templateServiceConfigurationArray = fProjectToTemplateConfiguration.values().
																							toArray(new IServiceConfiguration[0]);
		saveServiceConfigurations(rootMemento, templateServiceConfigurationArray, TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME);
		
		// Now save build scenarios, including creating and storing an id number for each.
		Map<BuildScenario, Integer> buildScenarioToIdMap = new HashMap<BuildScenario, Integer>();
		int idNumber = 0;
		for (BuildScenario bs : fBuildScenarioToSConfigMap.keySet()) {
			IMemento bsMemento = rootMemento.createChild(BUILD_SCENARIO_ELEMENT_NAME);
			bs.saveScenario(bsMemento);
			bsMemento.putString(ATTR_SERVICE_ID, fBuildScenarioToSConfigMap.get(bs).getId());
			bsMemento.putInteger(ATTR_ID, idNumber);
			buildScenarioToIdMap.put(bs, idNumber);
			++idNumber;
		}
		
		// Finally save a map of configuration ids to build scenario ids
		for (String configId : fBuildConfigToBuildScenarioMap.keySet()) {
			IMemento configMemento = rootMemento.createChild(CONFIG_ID_ELEMENT_NAME);
			configMemento.putString(ATTR_ID, configId);
			configMemento.putInteger(ATTR_BUILD_SCENARIO_ID, buildScenarioToIdMap.get(fBuildConfigToBuildScenarioMap.get(configId)));
		}
		
		rootMemento.save(writer);
	}
	
	/**
	 * Load configuration data. All previously stored data is erased.
	 *
	 * @throws IOException
	 */
	public static synchronized void loadConfigurationData() throws IOException, WorkbenchException {
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
		fProjectToTemplateConfiguration.clear();
		fTemplateToProjectMap.clear();
		fBuildConfigToBuildScenarioMap.clear();
		fBuildScenarioToSConfigMap.clear();
		
		// Load service configurations
		doLoadConfigurations(rootMemento, TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME);

		// Load all configurations for all projects and stash into a temporary map
		Map<String, IConfiguration> confSet = new HashMap<String, IConfiguration>();
		for (IProject project : fProjectToTemplateConfiguration.keySet()) {
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
			setBuildScenarioForBuildConfigurationInternal(IdToBuildScenarioMap.get(buildScenarioId), config);
		}
	}
	
	/**
	 * Save a collection of service configurations to the memento NOTE: does not actually save the memento.
	 * This method is copied from org.eclipse.ptp.services.core.ServiceModelManager.saveConfigurations, which could not be used
	 * directly because it is private. It has been modified to input a memento name and to save project name.
	 * 
	 * @param memento
	 *            memento used to save configurations
	 * @param configs
	 *            collection of service configurations to save
	 */
	private static synchronized void saveServiceConfigurations(IMemento memento, IServiceConfiguration[] configs,
																										String mementoChildName) {
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
	
	/**
	 * Do the actual job of loading configurations. This method is copied from
	 * org.eclipse.ptp.services.core.ServiceModelManager.doLoadConfigurations, which is private. It has been modified to load
	 * project names and map projects to templates. The import parameter has been removed. (We do not care about the id numbers,
	 * since these configurations are for our own internal purposes.) Also, the memento name is now a parameter. Other minor
	 * modifications made to satisfy the compiler.
	 * 
	 * If the configurations are being imported then a new ID is generated for
	 * each configuration. This is to avoid the import causing duplicate
	 * configuration IDs.
	 * 
	 * @param rootMemento
	 * @return
	 */
	private static synchronized void doLoadConfigurations(IMemento rootMemento, String mementoChildName) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		Set<IServiceConfiguration> configs = new HashSet<IServiceConfiguration>();

		for (IMemento configMemento : rootMemento.getChildren(mementoChildName)) {
			String configName = configMemento.getString(ATTR_NAME);
			String projectName = configMemento.getString(ATTR_PROJECT);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				throw new RuntimeException("Project " + project + " not found in workspace while loading configuration"); //$NON-NLS-1$ //$NON-NLS-2$
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
			fProjectToTemplateConfiguration.put(project, config);
			fTemplateToProjectMap.put(config, project);
		}
	}
	
	/**
	 * Save the state of a service provider
	 * This method is a copy of org.eclipse.ptp.services.core.ServiceModelManager.saveProviderState
	 * 
	 * @param provider
	 * @param parentMemento
	 */
	private static synchronized void saveProviderState(IServiceProvider provider, IMemento parentMemento) {
		if (provider instanceof ServiceProvider) {
			IMemento providerConfigMemento = parentMemento.createChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
			((ServiceProvider) provider).saveState(providerConfigMemento);
		}
	}
	
	/**
	 * Load a service provider from persistent state
	 * This method is a copy of org.eclipse.ptp.services.core.ServiceModelManager.loadServiceProvider with minor modifications
	 * made to satisfy the compiler.
	 *
	 * @param providerMemento
	 * @param service
	 * @return
	 */
	private static synchronized IServiceProvider loadServiceProvider(IMemento providerMemento, IService service) {
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
	 * Create a local build configuration. The corresponding build scenario has no sync provider and points to the project's
	 * working directory. We take a conservative approach. Failure at any point means we abort the attempt to create a local
	 * configuration.
	 *
	 * On project creation, CDT removes superfluous configurations. Thus, we place the functionality here, to be invoked at some
	 * point after initial project creation.
	 * 
	 * @param project
	 * 				The project needing a local configuration
	 */

	public static synchronized void createLocalConfiguration(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			return;
		}
		ManagedProject managedProject = (ManagedProject) buildInfo.getManagedProject();
		Configuration localConfigParent = (Configuration) buildInfo.getDefaultConfiguration();
		String localConfigId = ManagedBuildManager.calculateChildId(localConfigParent.getId(), null);
		Configuration localConfig = new Configuration(managedProject, localConfigParent, localConfigId, true, false);
		if (localConfig != null) {
			CConfigurationData localConfigData = localConfig.getConfigurationData();
			ICProjectDescription projectDes = CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription localConfigDes = null;
			try {
				localConfigDes = projectDes.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, localConfigData);
			} catch (WriteAccessException e) {
				// Nothing to do
			} catch (CoreException e) {
				// Nothing to do
			}

			if (localConfigDes != null) {
				boolean configAdded = false;
				localConfig.setConfigurationDescription(localConfigDes);
				localConfigDes.setName(LOCAL_CONFIGURATION_NAME);
				localConfigDes.setDescription(LOCAL_CONFIGURATION_DES);
				localConfig.getToolChain().getBuilder().setBuildPath(project.getLocation().toString());
				IRemoteServices localService = PTPRemoteCorePlugin.getDefault().
				getRemoteServices("org.eclipse.ptp.remote.LocalServices", null); //$NON-NLS-1$
				if (localService != null) {
					IRemoteConnection localConnection = localService.getConnectionManager().getConnection("local"); //$NON-NLS-1$
					if (localConnection != null) {
						BuildScenario localBuildScenario = new BuildScenario(null, localConnection, project.getLocation().toString());
						BuildConfigurationManager.setBuildScenarioForBuildConfigurationInternal(localBuildScenario, localConfig);
						configAdded = true;
					}
				}
				if (!configAdded) {
					projectDes.removeConfiguration(localConfigDes);
				} else {
					try {
						// ManagedBuildManager.resetConfiguration(project, localConfig);
						CoreModel.getDefault().setProjectDescription(project, projectDes, true, null);
						// ManagedBuildManager.addExtensionConfiguration(localConfig);
						// ManagedBuildManager.saveBuildInfo(project, true);
					} catch (CoreException e) {
						projectDes.removeConfiguration(localConfigDes);
						configAdded = false;
					}
				}
				if (configAdded) {
					try {
						BuildConfigurationManager.saveConfigurationData();
					} catch (IOException e) {
						projectDes.removeConfiguration(localConfigDes);
					}
				}
			}
		}
	}
}
