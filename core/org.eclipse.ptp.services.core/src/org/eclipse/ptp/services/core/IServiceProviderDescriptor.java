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

/**
 * An IServiceProviderDescriptor represents the description (but not an
 * instance of) of a service provider. IServiceProviderDescriptors contain
 * the immutable information about a service provider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 */
public interface IServiceProviderDescriptor {
	/**
	 * Get the ID of this service provider, or null if this is a null provider.
	 * 
	 * @return ID of this service provider
	 */
	public String getId();
	
	/**
	 * Get the name of this service provider, or null if this is a null provider.
	 * 
	 * @return name of this service provider
	 */
	public String getName();

	/**
	 * Get the service this service provider is for.
	 * 
	 * @return service
	 */
	public String getServiceId();
	
	/**
	 * Get the priority for the the provider. 
	 * Lower values have higher priority.
	 * 
	 * @return priority
	 */
	public Integer getPriority();
}
