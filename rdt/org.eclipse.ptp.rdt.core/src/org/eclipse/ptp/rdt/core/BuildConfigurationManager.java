package org.eclipse.ptp.rdt.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.services.core.IRemoteServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ptp.services.core.ServicesCorePlugin;
import org.eclipse.ptp.services.internal.core.ServiceConfiguration;
import org.eclipse.ui.IMemento;
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
	private static final String ATTR_BUILD_SCENARIO_ID = "buildId"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	private static final String SERVICE_CONFIGURATION_ELEMENT_NAME = "service-configuration"; //$NON-NLS-1$
	private static final String PROVIDER_CONFIGURATION_ELEMENT_NAME = "provider-configuration"; //$NON-NLS-1$
	private static final String ATTR_PROVIDER_ID = "provider-id"; //$NON-NLS-1$
	private static final String BUILD_SCENARIO_ELEMENT_NAME = "build-scenario"; //$NON-NLS-1$
	private static final String CONFIG_ID_ELEMENT_NAME = "config-id-to-build-scenario"; //$NON-NLS-1$
	private static final String TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME = "template-service-configuration-element-name"; //$NON-NLS-1$
	private static final String INITIALIZED_PROJECTS_ELEMENT_NAME = "initialized-projects"; //$NON-NLS-1$


	private static IServiceConfiguration fBuildSystemTemplateConfiguration = null;
	private static final Map<String, BuildScenario> fBuildConfigToBuildScenarioMap =
																							new HashMap<String,BuildScenario>();
	private static final Map<BuildScenario, IServiceConfiguration> fBuildScenarioToSConfigMap =
																			new HashMap<BuildScenario, IServiceConfiguration>();
	private static final Set<IProject> initializedProjects = new HashSet<IProject>();
	
	
	/**
	 * Add a new build scenario, creating a new service configuration for that scenario if necessary.
	 *
	 * @param buildScenario
	 * @since 2.1
	 */
	private static synchronized void addBuildScenario(IProject project, BuildScenario buildScenario) {
		IServiceConfiguration sConfig =copyTemplateServiceConfiguration();
		modifyServiceConfigurationForBuildScenario(sConfig, buildScenario);
		fBuildScenarioToSConfigMap.put(buildScenario, sConfig);
		
		// Update service model manager data structures
		ServiceModelManager.getInstance().addConfiguration(sConfig);
		ServiceModelManager.getInstance().addConfiguration(project, sConfig);
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
		if (!(fBuildScenarioToSConfigMap.containsValue(oldConfig))) {
			ServiceModelManager.getInstance().remove(oldConfig);
			ServiceModelManager.getInstance().removeConfiguration(project, oldConfig);
		}
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
		if (!(initializedProjects.contains(bconf.getOwner().getProject()))) {
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
		if (!(initializedProjects.contains(bconf.getOwner().getProject()))) {
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
		if (!(initializedProjects.contains(bconf.getOwner().getProject()))) {
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
	 * @since 2.1
	 */
	public static synchronized IServiceConfiguration getBuildSystemTemplateConfiguration() {
		return fBuildSystemTemplateConfiguration;
	}

	/**
	 * @param config
	 * @since 2.1
	 */
	public static synchronized void setBuildSystemTemplateConfiguration(IServiceConfiguration config) {
		fBuildSystemTemplateConfiguration = config;
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
	private static synchronized IServiceConfiguration copyTemplateServiceConfiguration() {
		IServiceConfiguration newConfig = ServiceModelManager.getInstance().newServiceConfiguration(""); //$NON-NLS-1$
		IServiceConfiguration oldConfig = fBuildSystemTemplateConfiguration;
		if (oldConfig == null) {
			throw new RuntimeException("No template service configuration set for build system"); //$NON-NLS-1$
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
	 * Set all configurations for this project to use the passed build scenario. This is to be used by clients to initialize
	 * the build configurations and must be called before any calls to other set or get methods for build configurations.
	 *
	 * @param project
	 * @param bs
	 * 			The build scenario
	 */
	public static synchronized void setInitialBuildScenarioForAllConfigurations(IProject project, BuildScenario bs) {
		if (bs == null) {
			throw new NullPointerException();
		}
		initializeOrUpdateConfigurations(project, bs);
		initializedProjects.add(project);
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
		return initializedProjects.contains(project);
	}
	
	/**
	 * Save configuration data to plugin metadata area
	 * 
	 * @throws IOException
	 * 						on problems writing configuration data to file
	 */
	public void saveConfigurationData() throws IOException {
		IPath savePath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File saveFile = savePath.toFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
		XMLMemento rootMemento = XMLMemento.createWriteRoot(BUILD_CONFIGURATION_ELEMENT_NAME);
		
		// First, save template service configuration
		IServiceConfiguration[] templateServiceConfigurationArray = new IServiceConfiguration[1];
		templateServiceConfigurationArray[0] = fBuildSystemTemplateConfiguration;
		saveServiceConfigurations(rootMemento, templateServiceConfigurationArray, TEMPLATE_SERVICE_CONFIGURATION_ELEMENT_NAME);
		
		// Next save service configurations
		saveServiceConfigurations(rootMemento, fBuildScenarioToSConfigMap.values().toArray(new IServiceConfiguration[0]),
																							SERVICE_CONFIGURATION_ELEMENT_NAME);
		
		// Save project list
		IMemento projectMemento = rootMemento.createChild(INITIALIZED_PROJECTS_ELEMENT_NAME);
		for (IProject project : initializedProjects) {
			projectMemento.putString(project.getName(), null);
		}

		// Now save build scenarios, including creating and storing an id number for each.
		Map<BuildScenario, Integer> buildScenarioToId = new HashMap<BuildScenario, Integer>();
		int idNumber = 0;
		for (BuildScenario bs : fBuildScenarioToSConfigMap.keySet()) {
			IMemento bsMemento = rootMemento.createChild(BUILD_SCENARIO_ELEMENT_NAME);
			bs.saveScenario(bsMemento);
			bsMemento.putString(ATTR_SERVICE_ID, fBuildScenarioToSConfigMap.get(bs).getId());
			bsMemento.putInteger(ATTR_ID, idNumber);
			buildScenarioToId.put(bs, idNumber);
			++idNumber;
		}
		
		// Finally save a map of configuration ids to build scenario ids
		for (String configId : fBuildConfigToBuildScenarioMap.keySet()) {
			IMemento configMemento = rootMemento.createChild(CONFIG_ID_ELEMENT_NAME);
			configMemento.putString(ATTR_ID, configId);
			configMemento.putInteger(ATTR_BUILD_SCENARIO_ID, buildScenarioToId.get(fBuildConfigToBuildScenarioMap.get(configId)));
		}
		
		rootMemento.save(writer);
	}
	
	/**
	 * Load configuration data. All previously stored data is erased.
	 *
	 * @throws IOException
	 */
	public void loadConfigurationData() throws IOException {
		
	}
	
	/**
	 * Save a collection of service configurations to the memento NOTE: does not actually save the memento.
	 * This method is a copy of org.eclipse.ptp.services.core.ServiceModelManager.saveConfigurations, which could not be used
	 * directly because it is private.
	 * 
	 * @param memento
	 *            memento used to save configurations
	 * @param configs
	 *            collection of service configurations to save
	 */
	private static void saveServiceConfigurations(IMemento memento, IServiceConfiguration[] configs, String mementoChildName) {
		for (IServiceConfiguration config : configs) {
			String configurationId = config.getId();
			String configurationName = config.getName();

			IMemento configMemento = memento.createChild(mementoChildName);
			configMemento.putString(ATTR_ID, configurationId);
			configMemento.putString(ATTR_NAME, configurationName);

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
	 * Save the state of a service provider
	 * This method is a copy of org.eclipse.ptp.services.core.ServiceModelManager.saveProviderState
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
}
