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
package org.eclipse.ptp.rdt.services.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor;

/**
 * @author greg
 *
 */
public class Service implements IService {
	private String serviceId;
	private String serviceName;
	private Set<String> serviceNatures;
	private Set<IServiceProviderDescriptor> serviceProviderDescriptors = new HashSet<IServiceProviderDescriptor>();
	private Map<String, IServiceProviderDescriptor> idToServiceProviderDescriptorMap = new HashMap<String, IServiceProviderDescriptor>();
	
	public Service(String id, String name, Set<String>natures) {
		this.serviceId = id;
		this.serviceName = name;
		this.serviceNatures = natures;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#addServiceProvider(org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor)
	 */
	public void addServiceProvider(IServiceProviderDescriptor provider) {
		serviceProviderDescriptors.add(provider);
		idToServiceProviderDescriptorMap.put(provider.getId(), provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#getId()
	 */
	public String getId() {
		return serviceId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#getName()
	 */
	public String getName() {
		return serviceName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#getNatures()
	 */
	public Set<String> getNatures() {
		return serviceNatures;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#getProviders()
	 */
	public Set<IServiceProviderDescriptor> getProviders() {
		return serviceProviderDescriptors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#removeServiceProvider(org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor)
	 */
	public void removeServiceProvider(IServiceProviderDescriptor provider) {
		serviceProviderDescriptors.remove(provider);
		idToServiceProviderDescriptorMap.remove(provider.getId());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IService#getProvider(java.lang.String)
	 */
	public IServiceProviderDescriptor getProviderDescriptor(String id) {
		return idToServiceProviderDescriptorMap.get(id);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IService) {
			return ((IService) o).getId().equals(serviceId);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return serviceId.hashCode();
	}
	
	public String toString() {
		return "Service(" + serviceId + ")"; //$NON-NLS-1$
	}
}
