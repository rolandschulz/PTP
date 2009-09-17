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
 * An IServiceConfiguration represents a particular set of service providers
 * for each known service. 
 * 
 * An IServiceConfiguration represents a mapping from IService to IServiceProvider.
 * If there exists a mapping for a particular IService then that service is
 * considered "enabled". If there is no mapping for a service then the service 
 * is considered "disabled" in this configuration. Disabling a service removes 
 * the service from the configuration.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public interface IServiceConfiguration extends IAdaptable {
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
	 * If the service is disabled and the service has a null-provider
	 * then the null-provider will be returned, if the service does not
	 * have a null-provider then null will be returned.
	 * 
	 * @param service service for which provider is required
	 * @return service provider for the service
	 */
	public IServiceProvider getServiceProvider(IService service);
	
	/**
	 * Returns all of the services that are enabled in this configuration.
	 * @return all of the services that are part of this configuration.
	 */
	public Set<IService> getServices();
	
	/**
	 * Return the set of services that are enabled in this configuration
	 * sorted by priority.
	 * 
	 * @param service service containing providers
	 * @return sorted providers
	 */
	public SortedSet<IService> getServicesByPriority();
	
	/**
	 * Set the name for this configuration.
	 * 
	 * @param name the name of this configuration
	 */
	public void setName(String name);
	
	/**
	 * Set the service provider for a particular service in this configuration.
	 * If the service was formerly disabled it becomes enabled.
	 * 
	 * If the service already has a provider associated with it it will be replaced
	 * with the given provider, the old provider will be remembered and will be
	 * returned from getFormerServicePRoviders().
	 * 
	 * @param service service to set the provider for
	 * @param provider provider for this service
	 * 
	 * @throws NullPointerException if service or provider is null
	 * @throws IllegalArgumentException
	 */
	public void setServiceProvider(IService service, IServiceProvider provider);
	
	/**
	 * Returns true of the given service is not part of this configuration.
	 * Equivalent to:
	 * {@code !getServices().contains(service)}
	 * 
	 * If this method returns false then {@code getServiceProvider()}
	 * will not return null.
	 * 
	 */
	public boolean isDisabled(IService service);
	
	/**
	 * Disables (removes) the service provider from this configuration.
	 */
	public void disable(IService service);
}
