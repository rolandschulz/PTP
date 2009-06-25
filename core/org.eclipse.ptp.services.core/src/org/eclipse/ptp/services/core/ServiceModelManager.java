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
package org.eclipse.ptp.services.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.services.core.messages.Messages;
import org.eclipse.ptp.services.internal.core.Service;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * A singleton class which is the entry point to a service model which represents:
 * - the set of contributed services
 * - the set of providers which provide those services
 * - the service fProjectConfigurations for each project which specify which services are
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
	private final static String ATTR_PRIORITY = "priority"; //$NON-NLS-1$
	private final static String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	private final static String ATTR_CLASS = "class"; //$NON-NLS-1$
	private final static String SERVICE_MODEL_ELEMENT_NAME = "service-model"; //$NON-NLS-1$
	private final static String PROJECT_ELEMENT_NAME = "project"; //$NON-NLS-1$
	private final static String SERVICE_CONFIGURATION_ELEMENT_NAME = "service-configuration"; //$NON-NLS-1$
	private final static String PROVIDER_CONFIGURATION_ELEMENT_NAME = "provider-configuration"; //$NON-NLS-1$
	private final static String ATTR_PROVIDER_ID = "provider-id"; //$NON-NLS-1$
	private final static String DEFAULT_SAVE_FILE_NAME = ".service_model";  //$NON-NLS-1$
	
	/** Default location to save service model configuration */
	private final IPath defaultSaveFile; 
	
	private Map<IProject, Map<String, IServiceConfiguration>> fProjectConfigurations = new HashMap<IProject, Map<String, IServiceConfiguration>>();
	private Map<IProject, IServiceConfiguration> fActiveConfigurations = new HashMap<IProject, IServiceConfiguration>();
	private Map<IProject, Set<IService>> fProjectServices = new HashMap<IProject, Set<IService>>();
	private Map<IServiceConfiguration, IProject> fConfigurationToProject = new HashMap<IServiceConfiguration, IProject>();
	private Map<String, IServiceProvider> fServiceProviders = new HashMap<String, IServiceProvider>();

	private Map<String, IService> fServices = null;
	private Set<IService> fServiceSet = null;
	private Map<String, Set<IService>> fNatureServices = null;
	
	
	private static ServiceModelManager fInstance;
	
	public static synchronized ServiceModelManager getInstance() {
		if(fInstance == null)
			fInstance = new ServiceModelManager();
		return fInstance;
	}
	
	private static <T> T getConf(Map<IProject, T> map, IProject project) {
		if(project == null)
			throw new NullPointerException();
		T value = map.get(project);
		if(value == null)
			throw new ProjectNotConfiguredException();
		return value;
	}
	
	private static void saveModelConfiguration(Map<String, IServiceConfiguration> model, Writer writer) throws IOException {
		XMLMemento rootMemento = XMLMemento.createWriteRoot(SERVICE_MODEL_ELEMENT_NAME);
		
		for (IServiceConfiguration config : model.values()) {
			String configurationName = config.getName();
			
			IMemento configMemento = rootMemento.createChild(SERVICE_CONFIGURATION_ELEMENT_NAME);
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
			}
		}
			
		rootMemento.save(writer);
	}

	private ServiceModelManager() {
		defaultSaveFile = Activator.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getActiveConfiguration(org.eclipse.core.resources.IProject)
	 */
	public IServiceConfiguration getActiveConfiguration(IProject project) {
		return getConf(fActiveConfigurations, project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getConfiguration(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	public IServiceConfiguration getConfiguration(IProject project, String name) {
		return getConf(fProjectConfigurations, project).get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getConfigurations(org.eclipse.core.resources.IProject)
	 */
	public Set<IServiceConfiguration> getConfigurations(IProject project) {
		return new HashSet<IServiceConfiguration>(getConf(fProjectConfigurations, project).values());
	}
	
	public IProject getProject(IServiceConfiguration configuration) {
		return fConfigurationToProject.get(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getService(java.lang.String)
	 */
	public IService getService(String id) {
		loadServices();
		return fServices.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServiceProvider(org.eclipse.ptp.services.core.IServiceProviderDescriptor)
	 */
	public IServiceProvider getServiceProvider(IServiceProviderDescriptor desc) {
		IServiceProvider provider = fServiceProviders.get(desc.getId());
		if (provider != null) {
			return provider;
		}
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
							try {
								provider = (IServiceProvider) element.createExecutableExtension(ATTR_CLASS);
								if (provider instanceof ServiceProvider) {
									((ServiceProvider)provider).setDescriptor(desc);
									fServiceProviders.put(desc.getId(), provider);
									return provider;
								}
							} catch (Exception e) {
								Activator.getDefault().log(e);
								return null;
							}
						}
					}
				}
			}
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices()
	 */
	public Set<IService> getServices() {
		loadServices();
		return fServiceSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(org.eclipse.core.resources.IProject)
	 */
	public Set<IService> getServices(IProject project) {
		return getConf(fProjectServices, project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(java.lang.String)
	 */
	public Set<IService> getServices(String natureId) {
		loadServices();
		return fNatureServices.get(natureId);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getWorkspaceConfigurations()
	 */
	public Set<IServiceConfiguration> getWorkspaceConfigurations() {
		Set<IServiceConfiguration> configs = new HashSet<IServiceConfiguration>();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (IProject project : workspace.getRoot().getProjects()) {
			if (project.isOpen()) {
				try {
					for (IServiceConfiguration config : getConfigurations(project)) {
						configs.add(config);
					}
				} catch (ProjectNotConfiguredException e) {
					// No configurations to include
				}
			}
		}
		return configs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#isConfigured(org.eclipse.core.resources.IProject)
	 */
	public boolean isConfigured(IProject project) {
		return fProjectConfigurations.containsKey(project);
	}
	
	/**
	 * Loads the service model configuration for a project
	 * If the file does not exist then this method does nothing.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * @param project the project containing the configuration to load
	 * @throws IOException
	 * @throws CoreException 
	 */
	public void loadModelConfiguration(IProject project) throws IOException, CoreException {
		if (project.exists()) {
			IFile file = project.getFile(DEFAULT_SAVE_FILE_NAME);
			if (file.exists()) {
				InputStream stream = file.getContents(true);
				Reader reader = new BufferedReader(new InputStreamReader(stream)); 
				try {
					loadModelConfiguration(project, reader);
				} finally {
					reader.close();
				}
			}
		}
	}
	
	/**
	 * Loads a service model configuration for a project using
	 * the supplied reader.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * @param project the project this configuration is for
	 * @param reader the configuration information source
	 * @throws IOException 
	 */
	public void loadModelConfiguration(IProject project, Reader reader) throws IOException, CoreException {
		XMLMemento rootMemento = XMLMemento.createReadRoot(reader);
		
		for (IMemento configMemento : rootMemento.getChildren(SERVICE_CONFIGURATION_ELEMENT_NAME)) {
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
	
	/**
	 * Prints the current service model to the console, for debugging purposes.
	 */
	public void printServiceModel() {
		System.out.println("Service Model: "); //$NON-NLS-1$
		if(fProjectConfigurations.isEmpty())
			System.out.println("  Service Model is empty"); //$NON-NLS-1$
		
		for(Entry<IProject, Map<String, IServiceConfiguration>> entry : fProjectConfigurations.entrySet()) {
			IProject project = entry.getKey();
			System.out.println("  Project: " + project.getName()); //$NON-NLS-1$
			for(IServiceConfiguration conf : entry.getValue().values()) {
				System.out.println("      " + conf); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#putConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void putConfiguration(IProject project, IServiceConfiguration conf) {
		if(project == null || conf == null)
			throw new NullPointerException();
		
		Map<String, IServiceConfiguration> confs = fProjectConfigurations.get(project);
		if(confs == null) {
			confs = new HashMap<String, IServiceConfiguration>();
			fProjectConfigurations.put(project, confs);
			fActiveConfigurations.put(project, conf);
		}
		
		confs.put(conf.getName(), conf);
		
		Set<IService> services = fProjectServices.get(project);
		if(services == null) {
			services = new HashSet<IService>();
			fProjectServices.put(project, services);
		}
		for(IServiceConfiguration config : confs.values()) {
			for(IService service : config.getServices()) {
				services.add(service);
			}
		}
		
		fConfigurationToProject.put(conf, project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#remove(org.eclipse.core.resources.IProject)
	 */
	public void remove(IProject project) {
		if(project == null) {
			throw new NullPointerException();
		}
		for (IServiceConfiguration conf : getConfigurations(project)) {
			fConfigurationToProject.remove(conf);
		}
		fProjectConfigurations.remove(project);
		fActiveConfigurations.remove(project);
		fProjectServices.remove(project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#removeConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void removeConfiguration(IProject project, IServiceConfiguration conf) {
		Map<String, IServiceConfiguration> confs = getConf(fProjectConfigurations, project);
		if(confs != null) {
			confs.remove(conf.getName());
			fConfigurationToProject.remove(conf);
		}
	}
	
	/**
	 * Saves the model configuration into the plugin's metadata area using
	 * the default file name.
	 * Will not save data for projects that do not exist.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * @throws IOException
	 */
	public void saveModelConfiguration(IProject project) throws IOException, CoreException {
		if (project.exists()) {
			StringWriter writer = new StringWriter();
			try {
				saveModelConfiguration(project, writer);
			} finally {
				writer.close();
			}
			IFile file = project.getFile(DEFAULT_SAVE_FILE_NAME);
			if (file.exists()) {
				file.delete(true, null);
			}
			file.create(new ByteArrayInputStream(writer.toString().getBytes()), IResource.FORCE, null);
		}
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
	public void saveModelConfiguration(IProject project, Writer writer) throws IOException {
		if(writer == null)
			throw new NullPointerException();
		saveModelConfiguration(fProjectConfigurations.get(project), writer);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#setActiveConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IProject project, IServiceConfiguration configuration) {
		Map<String, IServiceConfiguration> confs = getConf(fProjectConfigurations, project);
		
		if(!confs.containsKey(configuration.getName()))
			throw new IllegalArgumentException();
		
		fActiveConfigurations.put(project, configuration);
	}
	
	/**
	 * Locate and initialize service extensions.
	 */
	private void loadServices() {
		if (fServices != null) {
			return;
		}
		fServices = new HashMap<String, IService>();
		fServiceSet = new HashSet<IService>();
		fNatureServices = new HashMap<String, Set<IService>>();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(SERVICE_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						String priority = element.getAttribute(ATTR_PRIORITY);
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
						IService service = new Service(id, name, priority, natures);
						fServiceSet.add(service);
						fServices.put(id, service);
						for (String nature : natures) {
							Set<IService> svcs = fNatureServices.get(nature);
							if (svcs == null) {
								svcs = new HashSet<IService>();
								fNatureServices.put(nature, svcs);
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
						String priority = element.getAttribute(ATTR_PRIORITY);
						String serviceId = element.getAttribute(ATTR_SERVICE_ID);
						IServiceProviderDescriptor desc = new ServiceProviderDescriptor(id, name, serviceId, priority);
						IService service = fServices.get(serviceId);
						if (service != null) {
							service.addServiceProvider(desc);
						} else {
							Activator.getDefault().logErrorMessage(
									NLS.bind(Messages.Services_invalidServiceId, serviceId));
						}
					}
				}
			}
		}	
	}

	
}
