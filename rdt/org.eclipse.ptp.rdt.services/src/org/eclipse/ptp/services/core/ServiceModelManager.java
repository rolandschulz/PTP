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
package org.eclipse.ptp.services.core;

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
import org.eclipse.ptp.services.Activator;
import org.eclipse.ptp.services.internal.core.Service;

public class ServiceModelManager implements IServiceModelManager {
	private final static String SERVICE_EXTENSION_ID = "service"; //$NON-NLS-1$
	private final static String SERVICE_ELEMENT_NAME = "service"; //$NON-NLS-1$
	private final static String SERVICE_ATTR_ID = "id"; //$NON-NLS-1$
	private final static String SERVICE_ATTR_NAME = "name"; //$NON-NLS-1$
	private final static String NATURE_ELEMENT_NAME = "nature"; //$NON-NLS-1$
	private final static String NATURE_ATTR_ID = "id"; //$NON-NLS-1$
	
	private Map<IProject, Set<IServiceConfiguration>> projectConfigurations = new HashMap<IProject, Set<IServiceConfiguration>>();
	private Map<IProject, IServiceConfiguration> activeConfigurations = new HashMap<IProject, IServiceConfiguration>();
	private Map<String, IServiceConfiguration> configurations = new HashMap<String, IServiceConfiguration>();
	private Set<IService> services = new HashSet<IService>();
	private Map<IProject, Set<IService>> projectServices = new HashMap<IProject, Set<IService>>();
	private Map<String, Set<IService>> natureServices = new HashMap<String, Set<IService>>();
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getActiveConfiguration(org.eclipse.core.resources.IProject)
	 */
	public IServiceConfiguration getActiveConfiguration(IProject project) {
		return activeConfigurations.get(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getConfiguration(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	public IServiceConfiguration getConfiguration(IProject project, String name) {
		return configurations.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getConfigurations(org.eclipse.core.resources.IProject)
	 */
	public Set<IServiceConfiguration> getConfigurations(IProject project) {
		return projectConfigurations.get(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices()
	 */
	public Set<IService> getServices() {
		loadServices();
		return services;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(org.eclipse.core.resources.IProject)
	 */
	public Set<IService> getServices(IProject project) {
		return projectServices.get(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(java.lang.String)
	 */
	public Set<IService> getServices(String natureId) {
		return natureServices.get(natureId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#setActiveConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IProject project,
			IServiceConfiguration configuration) {
		activeConfigurations.put(project, configuration);
	}
	
	/**
	 * Locate and initialize service extensions.
	 */
	private void loadServices() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(SERVICE_ELEMENT_NAME)) {
						String serviceID = element.getAttribute(SERVICE_ATTR_ID);
						String serviceName = element.getAttribute(SERVICE_ATTR_NAME);
						IConfigurationElement[] natureConf = element.getChildren(NATURE_ELEMENT_NAME);
						Set<String> natures = new HashSet<String>();
						if (natureConf != null) {
							for (IConfigurationElement nature : natureConf) {
								String natureId = nature.getAttribute(NATURE_ATTR_ID);
								if (workspace.getNatureDescriptor(natureId) != null) {
									natures.add(natureId);
								}
							}
						}
						IService service = new Service(serviceID, serviceName, natures);
						services.add(service);
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
	}

}
