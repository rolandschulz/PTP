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

import java.util.Set;

/**
 * An interface for arbitrary services.  Services can be implemented by one or more
 * providers.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 * @see IServiceProvider
 */
public interface IService {
	/**
	 * Add service provider to list of providers for this service.
	 * 
	 * @param provider provider to add
	 */
	public void addServiceProvider(IServiceProviderDescriptor provider);
	
	/**
	 * Get the ID of this service provider.
	 * 
	 * @return ID of this service provider
	 */
	public String getId();
	
	/**
	 * Get the name of this service provider.
	 * 
	 * @return name of this service provider
	 */
	public String getName();
	
	/**
	 * Get the set of natures that this service applies to. The
	 * default nature "all" means the services is generic an applies
	 * to all natures.
	 * 
	 * @return set of natures
	 */
	public Set<String> getNatures();
	
	/**
	 * Get the set of all providers for this service.
	 * 
	 * @return set of providers for this service
	 */
	public Set<IServiceProviderDescriptor> getProviders();
	
	/**
	 * Gets a specific provider of this service.
	 * 
	 * @param id The unique ID of the service requested.
	 * @return IServiceProviderDescriptor
	 */
	public IServiceProviderDescriptor getProviderDescriptor(String id);

	/**
	 * Remove service provider from list of providers for this service.
	 * 
	 * @param provider provider to remove
	 */
	public void removeServiceProvider(IServiceProviderDescriptor provider);

}
