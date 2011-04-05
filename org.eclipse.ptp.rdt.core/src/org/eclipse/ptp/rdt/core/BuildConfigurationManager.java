package org.eclipse.ptp.rdt.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.services.core.IRemoteServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.XMLMemento;

/**
 * Static class to map CDT build configurations (IConfigurations) to service configurations 
 * @since 3.1
 */
public class BuildConfigurationManager {
	private static IServiceConfiguration fBuildSystemTemplateConfiguration = null;
	private static final Map<String, BuildScenario> fBuildConfigToBuildScenarioMap =
																							new HashMap<String,BuildScenario>();
	private static final Map<BuildScenario, IServiceConfiguration> fBuildScenarioToSConfigMap =
																			new HashMap<BuildScenario, IServiceConfiguration>();
	
	
	/**
	 * Add a new build scenario, creating a new service configuration for that scenario if necessary.
	 *
	 * @param buildScenario
	 * @since 2.1
	 */
	private static void addBuildScenario(BuildScenario buildScenario) {
		IServiceConfiguration sConfig =copyTemplateServiceConfiguration();
		modifyServiceConfigurationForBuildScenario(sConfig, buildScenario);
		fBuildScenarioToSConfigMap.put(buildScenario, sConfig);
		
		// Update service model manager data structures
		// TODO: Since we no longer input the project, we cannot do "addConfiguration(project, sConfig)", so some data is missing.
		ServiceModelManager.getInstance().addConfiguration(sConfig);
	}
	
	/**
	 * Return the build scenario for the passed configuration. If not present, use the build scenario for the nearest ancestor
	 * configuration. If still not found, return null.
	 * 
	 * @param bconf
	 * 				The build configuration
	 * @return build scenario
	 * @since 2.1
	 */
	public static BuildScenario getBuildScenarioForBuildConfiguration(IConfiguration bconf) {
		updateConfigurations(bconf.getOwner().getProject(), null);
		BuildScenario buildScenario = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		while (buildScenario == null) {
			bconf = bconf.getParent();
			buildScenario = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		}
		
		return buildScenario;
	}
	
	/**
	 * Associate the given configuration with the given build scenario. It is very important that we update configurations first,
	 * so that children of the changed configuration will be properly set to use the prior build scenario. 
	 *
	 * @param buildScenario
	 * @param bconf
	 * 				the build configuration
	 * @since 2.1
	 */
	public static void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
		updateConfigurations(bconf.getOwner().getProject(), null);
		fBuildConfigToBuildScenarioMap.put(bconf.getId(), bs);
		addBuildScenario(bs);
	}

	/**
	 * Returns the build scenario set for the given configuration, or null if it is unavailable (either the project or the
	 * configuration could be "bad" in this case).
	 * 
	 * @param bconf
	 * 			The build configuration
	 * @return build scenario for the configuration
	 * @throws RuntimeException if the build scenario cannot be mapped to a service configuration. This should never happen as it
	 * is an invariant enforced by this class. (We return null in the other cases as they could be the result of bad user input.)
	 * @since 2.1
	 */
	public static IServiceConfiguration getConfigurationForBuildConfiguration(IConfiguration bconf) {
		updateConfigurations(bconf.getOwner().getProject(), null);
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
	public static IServiceConfiguration getBuildSystemTemplateConfiguration() {
		return fBuildSystemTemplateConfiguration;
	}

	/**
	 * @param config
	 * @since 2.1
	 */
	public static void setBuildSystemTemplateConfiguration(IServiceConfiguration config) {
		fBuildSystemTemplateConfiguration = config;
	}
	
	private static void modifyServiceConfigurationForBuildScenario(IServiceConfiguration sConfig, BuildScenario bs) {
		for (IService service : sConfig.getServices()) {
			ServiceProvider provider = (ServiceProvider) sConfig.getServiceProvider(service);
			if (provider instanceof IRemoteServiceProvider) {
				((IRemoteServiceProvider) provider).changeRemoteInformation(bs.getRemoteConnectionName(), bs.getLocation());
			}
		}
	}
	private static IServiceConfiguration copyTemplateServiceConfiguration() {
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
	 * Return the service configuration that should be used for a given build scenario or null if none found.
	 * 
	 * @param buildScenario
	 * @return service configuration
	 * @since 2.1
	 */
	public static IServiceConfiguration getConfigurationForBuildScenario(BuildScenario buildScenario) {
		return fBuildScenarioToSConfigMap.get(buildScenario);
	}
	
	/**
	 * Set all configurations for this project to use the passed build scenario. This is meant to be used by clients to initialize
	 * the build configurations. 
	 *
	 * @param project
	 * @param bs
	 * 			The build scenario
	 */
	public static void setBuildScenarioForAllConfigurations(IProject project, BuildScenario bs) {
		if (bs == null) {
			throw new NullPointerException();
		}
		updateConfigurations(project, bs);
	}

	// If build scenario is not null, then set all configurations to use that build scenario (client interface). If null, set all
	// configurations to the build scenario of their nearest ancestor (for internal use only).
	private static void updateConfigurations(IProject project, BuildScenario bs) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		
		// The only way to retrieve all configurations is by name, and there is no function for mapping names to configurations.
		// Thus, in the loop we set each configuration to the default and then use "getDefaultConfiguration" to retrieve it. Before
		// starting, we store the current default and restore it after the loop.
		IConfiguration defaultConfig = buildInfo.getDefaultConfiguration();
		String[] allConfigNames = buildInfo.getConfigurationNames();
		for (String configName : allConfigNames) {
			buildInfo.setDefaultConfiguration(configName);
			IConfiguration config = buildInfo.getDefaultConfiguration();

			if (bs == null) {
				String parentConfig = findAncestorConfig(config.getId());
				if (parentConfig == null) {
					throw new RuntimeException("Failed to find an ancestor for build configuration " + config.getId()); //$NON-NLS-1$
				}
				setBuildScenarioForBuildConfiguration(fBuildConfigToBuildScenarioMap.get(parentConfig), config);
			} else {
				setBuildScenarioForBuildConfiguration(bs, config);
			}
		}
		buildInfo.setDefaultConfiguration(defaultConfig);
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
		String idPattern = "\\.\\d+$"; //$NON-NLS-1$
		String parentConfigId = configId.replaceFirst(idPattern, ""); //$NON-NLS-1$
		if (!(parentConfigId.matches(idPattern))) {
			return null;
		}
		return parentConfigId;
	}
}
