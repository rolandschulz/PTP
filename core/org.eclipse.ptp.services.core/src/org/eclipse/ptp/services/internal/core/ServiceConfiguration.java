/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

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
public class ServiceConfiguration extends PlatformObject implements IServiceConfiguration {
	
	protected final String fId;
	protected String fName;
	protected ServiceModelManager fManager = ServiceModelManager.getInstance();
	protected Map<IService, IServiceProvider> fServiceToProviderMap = new HashMap<IService, IServiceProvider>();
	protected Map<IService, LinkedHashSet<IServiceProvider>> fFormerServiceProviders = new HashMap<IService, LinkedHashSet<IServiceProvider>>();

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
		IServiceProvider activeProvider = fServiceToProviderMap.get(service);
		return activeProvider == null ? service.getNullProvider() : activeProvider;
	}
	
	
	/**
	 * Returns service providers that used to be associated with the given
	 * service. 
	 *
	 * This method is here mainly for use by the NewServiceModelWidget.
	 * The service configuration will automatically remember old service
	 * providers and their state. That way if a user switches back to an
	 * old provider the state can be restored and the user doesn't have
	 * to set up the provider again.
	 * 
	 * @param service the service
	 * @return set of old service providers
	 * 
	 * @see NewServiceModelWidget in the services.ui plugin
	 */
	public Set<IServiceProvider> getFormerServiceProviders(IService service) {
		Set<IServiceProvider> disabledProviders = fFormerServiceProviders.get(service);
		return disabledProviders == null ? Collections.<IServiceProvider>emptySet() : disabledProviders;
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
		
		sortedServices.addAll(getServices());
		
		return sortedServices;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceConfiguration#removeService(org.eclipse.ptp.services.core.IService)
	 */
	public void removeService(IService service) {
		IServiceProvider oldProvider = fServiceToProviderMap.remove(service);
		fFormerServiceProviders.remove(service);

		fManager.notifyListeners(new ServiceModelEvent(this, IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED, oldProvider));
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
		if (provider != null && provider.equals(service.getNullProvider())) {
			provider = null;
		}
		
		IServiceProvider oldProvider;
		if (provider == null) {
			oldProvider = fServiceToProviderMap.remove(service);
		} else {
			oldProvider = fServiceToProviderMap.put(service, provider);
		}
		
		if (oldProvider != null) {
			addFormerServiceProvider(service, oldProvider);
			if (provider != null) {
				fFormerServiceProviders.get(service).remove(provider);
			}
			
			if (!oldProvider.equals(provider)) {
				fManager.notifyListeners(new ServiceModelEvent(this, IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED, oldProvider));
			}
		} else if (oldProvider != provider) {
			fManager.notifyListeners(new ServiceModelEvent(this, IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED, oldProvider));
		}
	}
	
	
	public void addFormerServiceProvider(IService service, IServiceProvider disabledProvider) {
		if(disabledProvider == null)
			return;
		
		LinkedHashSet<IServiceProvider> disabledServices = fFormerServiceProviders.get(service);
		if(disabledServices == null) {
			disabledServices = new LinkedHashSet<IServiceProvider>(); // very important to maintain insertion order
			fFormerServiceProviders.put(service, disabledServices);
		}
		disabledServices.add(disabledProvider);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ServiceConfiguration: " + fName + " -> " + fServiceToProviderMap; //$NON-NLS-1$ //$NON-NLS-2$
	}

	
	public void disable(IService service) {
		setServiceProvider(service, null);
	}

	public boolean isDisabled(IService service) {
		return !fServiceToProviderMap.containsKey(service);
	}

}