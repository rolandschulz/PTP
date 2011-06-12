/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.services.Activator;
import org.eclipse.ptp.rdt.services.internal.core.Service;
import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.ptp.rdt.services.ui.Messages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * A singleton class which is the entry point to a service model which represents:
 * - the set of contributed services
 * - the set of providers which provide those services
 * - the service configurations for each project which specify which services are
 * 		mapped to which providers.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public class ServiceModelManager implements IServiceModelManager {
	private final static String SERVICE_EXTENSION_ID = "services"; //$NON-NLS-1$
	private final static String PROVIDER_EXTENSION_ID = "providers"; //$NON-NLS-1$
	private final static String SERVICE_ELEMENT_NAME = "service"; //$NON-NLS-1$
	private final static String NATURE_ELEMENT_NAME = "nature"; //$NON-NLS-1$
	private final static String PROVIDER_ELEMENT_NAME = "provider"; //$NON-NLS-1$
	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private final static String ATTR_NAME = "name"; //$NON-NLS-1$
	private final static String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	private final static String ATTR_CLASS = "class"; //$NON-NLS-1$
	private final static String ATTR_UI_CLASS = "configurationUIClass"; //$NON-NLS-1$
	private final static String SERVICE_MODEL_ELEMENT_NAME = "service-model"; //$NON-NLS-1$
	private final static String PROJECT_ELEMENT_NAME = "project"; //$NON-NLS-1$
	private final static String SERVICE_CONFIGURATION_ELEMENT_NAME = "service-configuration"; //$NON-NLS-1$
	private final static String PROVIDER_CONFIGURATION_ELEMENT_NAME = "provider-configuration"; //$NON-NLS-1$
	private final static String ATTR_PROVIDER_ID = "provider-id";
	
	private final static String DEFAULT_SAVE_FILE_NAME = "service-model.xml"; 
	
	/** Default location to save service model configuration */
	private final IPath defaultSaveFile; 
	
	private Map<IProject, Map<String, IServiceConfiguration>> configurations;
	private Map<IProject, IServiceConfiguration> activeConfigurations;
	private Map<IProject, Set<IService>> projectServices;

	private Map<String, IService> services = null;
	private Map<String, IServiceProviderDescriptor> serviceProviders = null;
	private Set<IService> serviceSet = null;
	private Map<String, Set<IService>> natureServices = null;
	
	
	private static ServiceModelManager fInstance;
	
	public static synchronized ServiceModelManager getInstance() {
		if(fInstance == null)
			fInstance = new ServiceModelManager();
		return fInstance;
	}
	
	private ServiceModelManager() {
		defaultSaveFile = Activator.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		initialize();
	}
	
	private void initialize() {
		 activeConfigurations = new HashMap<IProject, IServiceConfiguration>();
		 configurations = new HashMap<IProject, Map<String, IServiceConfiguration>>();
		 projectServices = new HashMap<IProject, Set<IService>>();
	}


	public void putConfiguration(IProject project, IServiceConfiguration conf) {
		if(project == null || conf == null)
			throw new NullPointerException();
		
		Map<String, IServiceConfiguration> confs = configurations.get(project);
		if(confs == null) {
			confs = new HashMap<String, IServiceConfiguration>();
			configurations.put(project, confs);
			activeConfigurations.put(project, conf);
		}
		
		confs.put(conf.getName(), conf);
		
		Set<IService> services = projectServices.get(project);
		if(services == null) {
			services = new HashSet<IService>();
			projectServices.put(project, services);
		}
		for(IServiceConfiguration config : confs.values()) {
			for(IService service : config.getServices()) {
				services.add(service);
			}
		}
	}

	
	private static <T> T getConf(Map<IProject, T> map, IProject project) {
		if(project == null)
			throw new NullPointerException();
		T value = map.get(project);
		if(value == null)
			throw new ProjectNotConfiguredException();
		return value;
	}
	

	public IServiceConfiguration getActiveConfiguration(IProject project) {
		return getConf(activeConfigurations, project);
	}


	public IServiceConfiguration getConfiguration(IProject project, String name) {
		return getConf(configurations, project).get(name);
	}


	public Set<IServiceConfiguration> getConfigurations(IProject project) {
		return new HashSet<IServiceConfiguration>(getConf(configurations, project).values());
	}
	
	
	public boolean isConfigured(IProject project) {
		return configurations.containsKey(project);
	}
	
	/**
	 * @param desc
	 * @return
	 */
	public IServiceProvider getServiceProvider(IServiceProviderDescriptor desc) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
							try {
								return (IServiceProvider) element.createExecutableExtension(ATTR_CLASS);
							} catch (Exception e) {
								return null;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * @param desc
	 * @return
	 */
	public IServiceProviderConfiguration getServiceProviderConfigurationUI(IServiceProviderDescriptor desc) {
		IServiceProviderConfiguration config = null;
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
							try {
								config = (IServiceProviderConfiguration) element.createExecutableExtension(ATTR_UI_CLASS);
							} catch (Exception e) {
								return null;
							}
						}
					}
				}
			}
		}
		return config;
	}


	public Set<IService> getServices() {
		loadServices();
		return serviceSet;
	}
	

	public Set<IService> getServices(IProject project) {
		return getConf(projectServices, project);
	}
	

	public Set<IService> getServices(String natureId) {
		loadServices();
		return natureServices.get(natureId);
	}
	

	public void removeConfiguration(IProject project, IServiceConfiguration conf) {
		Map<String, IServiceConfiguration> confs = getConf(configurations, project);
		if(confs != null) {
			confs.remove(conf.getName());
		}
	}
	
	
	public void remove(IProject project) {
		if(project == null)
			throw new NullPointerException();
		configurations.remove(project);
		activeConfigurations.remove(project);
		projectServices.remove(project);
	}
	
	
	public void remap(IProject removedProject, IProject addedProject) {
		if(removedProject == null || addedProject == null)
			throw new NullPointerException();
		
		if(isConfigured(removedProject)) {
			configurations.put(addedProject, configurations.remove(removedProject));
			activeConfigurations.put(addedProject, activeConfigurations.remove(removedProject));
			projectServices.put(addedProject, projectServices.remove(removedProject));
		}
	}
	

	public void setActiveConfiguration(IProject project, IServiceConfiguration configuration) {
		Map<String, IServiceConfiguration> confs = getConf(configurations, project);
		
		if(!confs.containsKey(configuration.getName()))
			throw new IllegalArgumentException();
		
		activeConfigurations.put(project, configuration);
	}

	/**
	 * Locate and initialize service extensions.
	 */
	private void loadServices() {
		if (services != null) {
			return;
		}
		services = new HashMap<String, IService>();
		serviceProviders = new HashMap<String, IServiceProviderDescriptor>();
		serviceSet = new HashSet<IService>();
		natureServices = new HashMap<String, Set<IService>>();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(SERVICE_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						IConfigurationElement[] natureConf = element.getChildren(NATURE_ELEMENT_NAME);
						Set<String> natures = new HashSet<String>();
						if (natureConf != null) {
							for (IConfigurationElement nature : natureConf) {
								String natureId = nature.getAttribute(ATTR_ID);
								if (workspace.getNatureDescriptor(natureId) != null) {
									natures.add(natureId);
								}
							}
						}
						IService service = new Service(id, name, natures);
						serviceSet.add(service);
						services.put(id, service);
						for (String nature : natures) {
							Set<IService> svcs = natureServices.get(nature);
							if (svcs == null) {
								svcs = new HashSet<IService>();
								natureServices.put(nature, svcs);
							}
							svcs.add(service);
						}
					}
				}
			}
		}
        extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						String serviceId = element.getAttribute(ATTR_SERVICE_ID);
						IServiceProviderDescriptor desc = new ServiceProviderDescriptor(id, name, serviceId);
						IService service = services.get(serviceId);
						if (service != null) {
							serviceProviders.put(id, desc);
							service.addServiceProvider(desc);
						} else {
							Activator.getDefault().logErrorMessage(
									Messages.getFormattedString(Messages.Services_invalidServiceId, serviceId));
						}
					}
				}
			}
		}	
	}

	public IService getService(String id) {
		loadServices();
		return services.get(id);
	}
	
	/**
	 * Saves the service model configuration to the given <code>file</code>.
	 * Will not save data for projects that do not exist.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.rdt.services<code> plugin.
	 * @param file
	 * @throws IOException 
	 * @throws NullPointerException if file is null
	 */
	public void saveModelConfiguration(Writer writer) throws IOException {
		if(writer == null)
			throw new NullPointerException();
		saveModelConfiguration(configurations, writer);
	}
	
	/**
	 * Saves the model configuration into the plugin's metadata area using
	 * the default file name.
	 * Will not save data for projects that do not exist.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.rdt.services<code> plugin.
	 * @throws IOException
	 */
	public void saveModelConfiguration() throws IOException {
		File file = defaultSaveFile.toFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		try {
			saveModelConfiguration(writer);
		} finally {
			writer.close();
		}
	}
	
	
	private static void saveModelConfiguration(Map<IProject, Map<String, IServiceConfiguration>> model, Writer writer) throws IOException {
		XMLMemento rootMemento = XMLMemento.createWriteRoot(SERVICE_MODEL_ELEMENT_NAME);
		
		for (Entry<IProject, Map<String, IServiceConfiguration>> entry : model.entrySet()) {
			IProject project = entry.getKey();
			if (!project.exists()) // Skip over deleted projects
				continue;
			
			String projectName = project.getName();
			IMemento projectMemento = rootMemento.createChild(PROJECT_ELEMENT_NAME);
			projectMemento.putString(ATTR_NAME, projectName);
			
			Map<String, IServiceConfiguration> configurations = entry.getValue();
			for (IServiceConfiguration config : configurations.values()) {
				String configurationName = config.getName();
				
				IMemento configMemento = projectMemento.createChild(SERVICE_CONFIGURATION_ELEMENT_NAME);
				configMemento.putString(ATTR_NAME, configurationName);
				
				Set<IService> services = config.getServices();
				for (IService service : services) {
					IServiceProvider provider = config.getServiceProvider(service);
					if(provider != null) {
						IMemento serviceMemento = configMemento.createChild(SERVICE_ELEMENT_NAME);
						serviceMemento.putString(ATTR_ID, service.getId());
						serviceMemento.putString(ATTR_PROVIDER_ID, provider.getId());
					
						IMemento providerMemento = serviceMemento.createChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
						provider.saveState(providerMemento);
					}
					else {
						Activator.getDefault().logErrorMessage(
								Messages.getFormattedString(Messages.Services_saveServiceError, service.getName(), projectName));
					}
				}
			}
		}
			
		rootMemento.save(writer);
	}
	
	
	/**
	 * Replaces the current service model configuration with what is
	 * specified in the default save file. If the file does not exist
	 * then this method does nothing.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.rdt.services<code> plugin.
	 * @throws IOException
	 * @throws CoreException 
	 */
	public void loadModelConfiguration() throws IOException, CoreException {
		File file = defaultSaveFile.toFile();
		if(file.exists()) {
			Reader reader = new BufferedReader(new FileReader(file));
			try {
				loadModelConfiguration(reader);
			} finally {
				reader.close();
			}
		}
	}
	
	
	/**
	 * Replaces the current service model configuration with what is
	 * specified in the given <code>file</code>.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.rdt.services<code> plugin.
	 * @throws IOException 
	 */
	public void loadModelConfiguration(Reader reader) throws IOException, CoreException {
		initialize(); // Clear out the existing model
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		XMLMemento rootMemento = XMLMemento.createReadRoot(reader);
		
		for (IMemento projectMemento : rootMemento.getChildren(PROJECT_ELEMENT_NAME)) {
			String projectName = projectMemento.getString(ATTR_NAME);
			IProject project = root.getProject(projectName);
			
			// Skip over projects that aren't in the workspace.
			if (!project.exists())
				continue;
			
			for (IMemento configMemento : projectMemento.getChildren(SERVICE_CONFIGURATION_ELEMENT_NAME)) {
				String configName = configMemento.getString(ATTR_NAME);
				ServiceConfiguration config = new ServiceConfiguration(configName);
				for (IMemento serviceMemento : configMemento.getChildren(SERVICE_ELEMENT_NAME)) {
					String serviceId = serviceMemento.getString(ATTR_ID);
					String providerId = serviceMemento.getString(ATTR_PROVIDER_ID);
					
					IService service = getService(serviceId);
					IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
					IServiceProvider provider = getServiceProvider(descriptor);
					IMemento providerMemento = serviceMemento.getChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
					provider.restoreState(providerMemento);
					config.setServiceProvider(service, provider);
				}
				putConfiguration(project, config);
			}
		} 
	}

	
	/**
	 * Prints the current service model to the console, for debugging purposes.
	 */
	public void printServiceModel() {
		System.out.println("Service Model: ");
		if(configurations.isEmpty())
			System.out.println("  Service Model is empty");
		
		for(Entry<IProject, Map<String, IServiceConfiguration>> entry : configurations.entrySet()) {
			IProject project = entry.getKey();
			System.out.println("  Project: " + project.getName());
			for(IServiceConfiguration conf : entry.getValue().values()) {
				System.out.println("      " + conf);
			}
		}
	}

	

	
}
