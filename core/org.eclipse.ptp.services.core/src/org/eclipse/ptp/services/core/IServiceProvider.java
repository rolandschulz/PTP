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


/**
 * An interface that represents the provider of a service.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 * @see IService
 */
public interface IServiceProvider extends IServiceProviderDescriptor {
	/**
	 * Returns provider specific information for the current configuration.
	 * 
	 * @return information on current configuration for this provider
	 */
	public String getConfigurationString();

	/**
	 * Returns the string value of the given key.
	 * 
	 * @param key the key
	 * @param defaultValue The value to return if key not stored
	 * @return the value or defaultValue if no value was found
	 */
	public String getString(String key, String defaultValue);

	/**
	 * Test if this service provider has been configured.
	 * 
	 * @return true if provider has been configured
	 */
	public boolean isConfigured();
	
	/**
	 * Sets the value of the given key to the given string
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void putString(String key, String value);
}
