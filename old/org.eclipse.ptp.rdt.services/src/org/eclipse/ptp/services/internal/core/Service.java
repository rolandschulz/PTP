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
package org.eclipse.ptp.services.internal.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;

/**
 * @author greg
 *
 */
public class Service implements IService {
	private String serviceId;
	private String serviceName;
	private Set<String> serviceNatures;
	private Set<IServiceProviderDescriptor> serviceProviderDescriptors = new HashSet<IServiceProviderDescriptor>();
	
	public Service(String id, String name, Set<String>natures) {
		this.serviceId = id;
		this.serviceName = name;
		this.serviceNatures = natures;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getId()
	 */
	public String getId() {
		return serviceId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getName()
	 */
	public String getName() {
		return serviceName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getNatures()
	 */
	public Set<String> getNatures() {
		return serviceNatures;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getProviders()
	 */
	public Set<IServiceProviderDescriptor> getProviders() {
		return serviceProviderDescriptors;
	}
	
	/**
	 * @param provider
	 */
	public void addServiceProvider(IServiceProviderDescriptor provider) {
		serviceProviderDescriptors.add(provider);
	}
}
