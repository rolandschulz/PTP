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

import org.eclipse.core.runtime.IAdaptable;


/**
 * An IServiceProvider represents an instance of an IServiceProviderDescriptor, 
 * and there can be many instances for each IServiceProviderDescriptor. 
 * 
 * IServiceProvider contains attributes and other information for a particular
 * service provider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 * @see IService
 */
public interface IServiceProvider extends IServiceProviderDescriptor, IAdaptable {
	/**
	 * Returns the boolean value of the given key.
	 * 
	 * @param key the key
	 * @param defaultValue The value to return if key not stored
	 * @return the value or defaultValue if no value was found
	 */
	public boolean getBoolean(String key, boolean defaultValue);

	/**
	 * Returns provider specific information for the current configuration.
	 * 
	 * @return information on current configuration for this provider
	 * @deprecated
	 */
	public String getConfigurationString();

	/**
	 * Get the descriptor for this service provider
	 * 
	 * @return service provider descriptor
	 */
	public IServiceProviderDescriptor getDescriptor();

	/**
	 * Returns the int value of the given key.
	 * 
	 * @param key the key
	 * @param defaultValue The value to return if key not stored
	 * @return the value or defaultValue if no value was found
	 */
	public int getInt(String key, int defaultValue);
	
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
	 * Returns a set of all the property keys that apply to this
	 * service provider.
	 */
	public Set<String> keySet();

	/**
	 * Sets the value of the given key to the given boolean
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void putBoolean(String key, boolean value);
	
	
	/**
	 * Sets the value of the given key to the given int
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void putInt(String key, int value);
	
	/**
	 * Sets the value of the given key to the given string
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void putString(String key, String value);
	
	/**
	 * Set the descriptor for this provider.
	 * 
	 * @param descriptor descriptor to set
	 */
	public void setDescriptor(IServiceProviderDescriptor descriptor);
	
}
