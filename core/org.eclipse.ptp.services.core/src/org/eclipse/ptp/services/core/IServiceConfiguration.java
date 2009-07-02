/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

/**
 *
 * An IServiceConfiguration represents a particular set of service providers
 * for each known service. 
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public interface IServiceConfiguration {
	/**
	 * Get the unique ID for this configuration.
	 * 
	 * @return the unique ID of this configuration
	 */
	public String getId();
	
	/**
	 * Get the name for this configuration.
	 * 
	 * @return the name of this configuration
	 */
	public String getName();

	/**
	 * Get the service provider for a particular service in this configuration.
	 * 
	 * @param service service for which provider is required
	 * @return service provider for the service
	 */
	public IServiceProvider getServiceProvider(IService service);
	
	/**
	 * Returns all of the services that are part of this configuration. 
	 * 
	 * @return all of the services that are part of this configuration.
	 */
	public Set<IService> getServices();
	
	/**
	 * Return the set of providers sorted by priority
	 * 
	 * @param service service containing providers
	 * @return sorted providers
	 */
	public SortedSet<IService> getServicesByPriority();
	
	/**
	 * Remove a service and its provider from this configuration
	 * 
	 * @param service service to remove
	 */
	public void removeService(IService service);
	
	/**
	 * Set the name for this configuration.
	 * 
	 * @param name the name of this configuration
	 */
	public void setName(String name);
	
	/**
	 * Set the service provider for a particular service in this configuration.
	 * 
	 * @param service service to set the provider for
	 * @param provider provider for this service
	 */
	public void setServiceProvider(IService service, IServiceProvider provider);
}
