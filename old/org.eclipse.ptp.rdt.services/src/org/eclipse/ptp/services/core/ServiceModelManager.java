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
import org.eclipse.core.resources.IProjectNature;

public class ServiceModelManager implements IServiceModelManager {
	private Map<IProject, Set<IServiceConfiguration>> projectConfigurations = new HashMap<IProject, Set<IServiceConfiguration>>();
	private Map<IProject, IServiceConfiguration> activeConfigurations = new HashMap<IProject, IServiceConfiguration>();
	private Map<String, IServiceConfiguration> configurations = new HashMap<String, IServiceConfiguration>();
	private Set<IService> services = new HashSet<IService>();
	private Map<IProject, Set<IService>> projectServices = new HashMap<IProject, Set<IService>>();
	private Map<IProjectNature, Set<IService>> natureServices = new HashMap<IProjectNature, Set<IService>>();
	
	
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
		return services;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(org.eclipse.core.resources.IProject)
	 */
	public Set<IService> getServices(IProject project) {
		return projectServices.get(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices(org.eclipse.core.resources.IProjectNature)
	 */
	public Set<IService> getServices(IProjectNature nature) {
		return natureServices.get(nature);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#setActiveConfiguration(org.eclipse.core.resources.IProject, org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IProject project,
			IServiceConfiguration configuration) {
		activeConfigurations.put(project, configuration);
	}

}
