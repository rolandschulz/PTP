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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;

/**
 * @author greg
 *
 */
public class Service implements IService {
	private String fServiceId;
	private String fServiceName;
	private Integer fServicePriority;
	private Set<String> fServiceNatures;
	private Set<IServiceProviderDescriptor> fServiceProviderDescriptors = new HashSet<IServiceProviderDescriptor>();
	private Map<String, IServiceProviderDescriptor> fIdToServiceProviderDescriptorMap = new HashMap<String, IServiceProviderDescriptor>();
	
	public Service(String id, String name, String priority, Set<String>natures) {
		fServiceId = id;
		fServiceName = name;
		try {
			fServicePriority = Integer.parseInt(priority);
		} catch (NumberFormatException e) {
			fServicePriority = Integer.MAX_VALUE;
		}
		fServiceNatures = natures;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#addServiceProvider(org.eclipse.ptp.services.core.IServiceProviderDescriptor)
	 */
	public void addServiceProvider(IServiceProviderDescriptor provider) {
		fServiceProviderDescriptors.add(provider);
		fIdToServiceProviderDescriptorMap.put(provider.getId(), provider);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IService) {
			return ((IService) o).getId().equals(fServiceId);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getId()
	 */
	public String getId() {
		return fServiceId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getName()
	 */
	public String getName() {
		return fServiceName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getNatures()
	 */
	public Set<String> getNatures() {
		return fServiceNatures;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getPriority()
	 */
	public Integer getPriority() {
		return fServicePriority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getProvider(java.lang.String)
	 */
	public IServiceProviderDescriptor getProviderDescriptor(String id) {
		return fIdToServiceProviderDescriptorMap.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#getProviders()
	 */
	public Set<IServiceProviderDescriptor> getProviders() {
		return fServiceProviderDescriptors;
	}
	
	@Override
	public int hashCode() {
		return fServiceId.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IService#removeServiceProvider(org.eclipse.ptp.services.core.IServiceProviderDescriptor)
	 */
	public void removeServiceProvider(IServiceProviderDescriptor provider) {
		fServiceProviderDescriptors.remove(provider);
		fIdToServiceProviderDescriptorMap.remove(provider.getId());
	}
	
	public String toString() {
		return "Service(" + fServiceId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
