/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.internal.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * A named configuration which consists of a mapping of services to providers.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 * @see IService
 * @see IServiceProvider 
 * @author crecoskie
 *
 */
public class ServiceConfiguration implements IServiceConfiguration {
	
	protected final String fId;
	protected String fName;
	protected Map<IService, IServiceProvider> fServiceToProviderMap = new HashMap<IService, IServiceProvider>();

	public ServiceConfiguration(String id, String name) {
		fId = id;
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#getId()
	 */
	public String getId() {
		return fId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#getServiceProvider(org.eclipse.ptp.services.core.IService)
	 */
	public IServiceProvider getServiceProvider(IService service) {
		return fServiceToProviderMap.get(service);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#getServices()
	 */
	public Set<IService> getServices() {
		return Collections.unmodifiableSet(fServiceToProviderMap.keySet());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#getServicesByPriority()
	 */
	public SortedSet<IService> getServicesByPriority() {
		SortedSet<IService> sortedServices = 
			new TreeSet<IService>(new Comparator<IService>() {
				public int compare(IService o1, IService o2) {
					int cmp = o1.getPriority().compareTo(o2.getPriority());
					if (cmp != 0) {
						return cmp;
					}
					return o1.getId().compareTo(o2.getId());
				}
			});
		for (IService s : getServices()) {
			sortedServices.add(s);
		}
		
		return sortedServices;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#removeService(org.eclipse.ptp.services.core.IService)
	 */
	public void removeService(IService service) {
		if (fServiceToProviderMap.containsKey(service)) {
			fServiceToProviderMap.remove(service);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#setName(java.lang.String)
	 */
	public void setName(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#setServiceProvider(org.eclipse.ptp.services.core.IService, org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public void setServiceProvider(IService service, IServiceProvider provider) {
		// Remove old mapping if one exists
		fServiceToProviderMap.remove(service);
		fServiceToProviderMap.put(service, provider);

	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ServiceConfiguration: " + fName + " -> " + fServiceToProviderMap; //$NON-NLS-1$ //$NON-NLS-2$
	}
}