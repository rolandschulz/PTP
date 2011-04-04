package org.eclipse.ptp.rdt.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
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
		BuildScenario buildScenario = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		while (buildScenario == null) {
			bconf = bconf.getParent();
			buildScenario = fBuildConfigToBuildScenarioMap.get(bconf.getId());
		}
		
		return buildScenario;
	}
	
	/**
	 * Associate the given configuration with the given build scenario
	 *
	 * @param buildScenario
	 * @param bconf
	 * 				the build configuration
	 * @since 2.1
	 */
	public static void setBuildScenarioForBuildConfiguration(BuildScenario bs, IConfiguration bconf) {
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
}
