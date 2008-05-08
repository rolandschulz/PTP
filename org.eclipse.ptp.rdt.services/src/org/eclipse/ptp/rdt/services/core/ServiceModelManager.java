/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.services.Activator;
import org.eclipse.ptp.rdt.services.internal.core.Service;
import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.ptp.rdt.services.ui.Messages;

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
	
	private final static ServiceModelManager fInstance = new ServiceModelManager();
	
	private Map<IProject, Set<IServiceConfiguration>> projectConfigurations = new HashMap<IProject, Set<IServiceConfiguration>>();
	private Map<IProject, IServiceConfiguration> activeConfigurations = new HashMap<IProject, IServiceConfiguration>();
	private Map<String, IServiceConfiguration> configurations = new HashMap<String, IServiceConfiguration>();
	private Map<IProject, Set<IService>> projectServices = new HashMap<IProject, Set<IService>>();

	private Map<String, IService> services = null;
	private Map<String, IServiceProviderDescriptor> serviceProviders = null;
	private Set<IService> serviceSet = null;
	private Map<String, Set<IService>> natureServices = null;
	
	
	public static ServiceModelManager getInstance() {
		return fInstance;
	}
	
	private ServiceModelManager() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#addConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.rdt.services.core.IServiceConfiguration)
	 */
	public void addConfiguration(IProject project, IServiceConfiguration conf) {
		Set<IServiceConfiguration> confs = projectConfigurations.get(project);
		if (confs == null) {
			confs = new HashSet<IServiceConfiguration>();
		}
		
		confs.add(conf);
		projectConfigurations.put(project, confs);
		configurations.put(conf.getName(), conf);
		
		if(activeConfigurations.get(project) == null) {
			activeConfigurations.put(project, conf);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getActiveConfiguration(org.eclipse.core.resources.IProject)
	 */
	public IServiceConfiguration getActiveConfiguration(IProject project) {
		return activeConfigurations.get(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getConfiguration(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	public IServiceConfiguration getConfiguration(IProject project, String name) {
		return configurations.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getConfigurations(org.eclipse.core.resources.IProject)
	 */
	public Set<IServiceConfiguration> getConfigurations(IProject project) {
		return projectConfigurations.get(project);
	}

	/**
	 * @param desc
	 * @return
	 */
	public IServiceProvider getServiceProvider(IServiceProviderDescriptor desc) {
		IServiceProvider provider = null;

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	this.PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension
						.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
							try {
								provider = (IServiceProvider) element.createExecutableExtension(ATTR_CLASS);
							} catch (Exception e) {
								return null;
							}
						}
					}
				}
			}
		}

		return provider;
	}

	/**
	 * @param desc
	 * @return
	 */
	public IServiceProviderConfiguration getServiceProviderConfigurationUI(IServiceProviderDescriptor desc) {
		IServiceProviderConfiguration config = null;
		IExtension extension = Platform.getExtensionRegistry().getExtension(desc.getId());
		if (extension != null) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
					try {
						config = (IServiceProviderConfiguration)element.createExecutableExtension(ATTR_UI_CLASS);
					} catch (Exception e) {
						return null;
					}
			
				}
			}
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getServices()
	 */
	public Set<IService> getServices() {
		loadServices();
		return serviceSet;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getServices(org.eclipse.core.resources.IProject)
	 */
	public Set<IService> getServices(IProject project) {
		return projectServices.get(project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#getServices(java.lang.String)
	 */
	public Set<IService> getServices(String natureId) {
		loadServices();
		return natureServices.get(natureId);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#removeConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.rdt.services.core.IServiceConfiguration)
	 */
	public void removeConfiguration(IProject project, IServiceConfiguration conf) {
		Set<IServiceConfiguration> confs = projectConfigurations.get(project);
		if (confs != null) {
			confs.remove(conf);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceModelManager#setActiveConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.rdt.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IProject project,
			IServiceConfiguration configuration) {
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
		return services.get(id);
	}
}
