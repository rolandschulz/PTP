/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IAdaptable;

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
public interface IService extends IAdaptable {

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
	 * Get the priority of this service. Lower values have higher priority.
	 * 
	 * @return a string representing the service priority
	 */
	public Integer getPriority();
	
	/**
	 * Gets a specific provider of this service.
	 * 
	 * @param id The unique ID of the service requested.
	 * @return IServiceProviderDescriptor
	 */
	public IServiceProviderDescriptor getProviderDescriptor(String id);

	/**
	 * Get the set of all providers for this service.
	 * 
	 * @return set of providers for this service
	 */
	public Set<IServiceProviderDescriptor> getProviders();
	
	/**
	 * Return the set of providers sorted by priority
	 * 
	 * @param service service containing providers
	 * @return sorted providers
	 */
	public SortedSet<IServiceProviderDescriptor> getProvidersByPriority();
	
	/**
	 * Remove service provider from list of providers for this service.
	 * 
	 * @param provider provider to remove
	 */
	public void removeServiceProvider(IServiceProviderDescriptor provider);
	
	/**
	 * Returns the service category that contains this service or
	 * null if the service is not associated with a category.
	 */
	public IServiceCategory getCategory();
	
	
	/**
	 * Returns the special null "no-op" service provider, or null if there is none.
	 */
	public IServiceProvider getNullProvider();
}
