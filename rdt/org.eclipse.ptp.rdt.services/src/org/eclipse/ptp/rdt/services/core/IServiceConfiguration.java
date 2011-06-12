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
package org.eclipse.ptp.rdt.services.core;

import java.util.Set;

/**
 *
 * An IServiceProviderConfiguration represents a particular set of service providers
 * for each known service. A project can have multiple configurations, each with
 * a different set of service providers.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author greg
 *
 */
public interface IServiceConfiguration {
	/**
	 * Get the name for this configuration. A configuration name must be unique for a particular project.
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
	 * Set the service provider for a particular service in this configuration.
	 * 
	 * @param service service to set the provider for
	 * @param provider provider for this service
	 */
	public void setServiceProvider(IService service, IServiceProvider provider);
	
	/**
	 * Returns all of the services that are part of this configuration. 
	 * @return all of the services that are part of this configuration.
	 */
	public Set<IService> getServices();
}
