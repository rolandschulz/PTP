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
package org.eclipse.ptp.services.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.services.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * An abstract base class for service provider implementations. 
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 *
 */
public abstract class ServiceProvider extends PlatformObject implements IServiceProvider, IServiceProviderDescriptor {
	
	private IServiceProviderDescriptor fDescriptor;
	private final HashMap<String, String> fAttributes = new HashMap<String, String>();
	
	public ServiceProvider() {
	}

	public ServiceProvider(ServiceProvider provider) {
		setDescriptor(provider.getDescriptor());
		fAttributes.putAll(provider.getAttributes());
	}

	/**
	 * Get the attributes for this service provider
	 * 
	 * @return service provider attributes
	 */
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(fAttributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = getString(key, null);
		boolean result = defaultValue;
		if (value != null) {
			result = Boolean.parseBoolean(value);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		return isConfigured() ? Messages.ServiceProvider_0 : Messages.ServiceProvider_1;
	}
	
	/**
	 * Get the descriptor for this service provider
	 * 
	 * @return service provider descriptor
	 */
	public IServiceProviderDescriptor getDescriptor() {
		return fDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getId()
	 */
	public String getId() {
		if (fDescriptor == null) {
			return null;
		}
		return fDescriptor.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getInt(java.lang.String, int)
	 */
	public int getInt(String key, int defaultValue) {
		String value = getString(key, null);
		int result = defaultValue;
		if (value != null) {
			try {
				result = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Use default
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getName()
	 */
	public String getName() {
		if (fDescriptor == null) {
			return null;
		}
		return fDescriptor.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getPriority()
	 */
	public Integer getPriority() {
		if (fDescriptor == null) {
			return null;
		}
		return fDescriptor.getPriority();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getServiceId()
	 */
	public String getServiceId() {
		if (fDescriptor == null) {
			return null;
		}
		return fDescriptor.getServiceId();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String key, String defaultValue)
	{
		String value = fAttributes.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#putBoolean(java.lang.String, boolean)
	 */
	public void putBoolean(String key, boolean value) {
		String strVal = Boolean.toString(value);
		putString(key, strVal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#putInt(java.lang.String, int)
	 */
	public void putInt(String key, int value) {
		String strVal = Integer.toString(value);
		putString(key, strVal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#putString(java.lang.String, java.lang.String)
	 */
	public void putString(String key, String value) {
		fAttributes.put(key, value);
	}

	
	public Set<String> keySet() {
		return fAttributes.keySet();
	}
	
	/**
	 * Restores the state of this provider from the
	 * given <code>IMemento</code>.
	 * 
	 * NOTE: This should only be implemented if a provider 
	 * wishes to override the default behavior.
	 * 
	 * @param memento for restoring the provider's state.
	 */
	public void restoreState(IMemento memento) {
		fAttributes.clear();
		for (String key : memento.getAttributeKeys()) {
			fAttributes.put(key, memento.getString(key));
		}
	}

	/**
	 * Saves the state of this provider in the given
	 * <code>IMemento</code>. 
	 * 
	 * NOTE: This should only be implemented if a provider  
	 * wishes to override the default behavior.
	 * 
	 * @param memento for saving the provider's state.
	 */
	public void saveState(IMemento memento) {
		for (String key : fAttributes.keySet()) {
			memento.putString(key, fAttributes.get(key));
		}
	}

	/**
	 * Set the descriptor for this provider.
	 * 
	 * @param descriptor descriptor to set
	 */
	public void setDescriptor(IServiceProviderDescriptor descriptor) {
		this.fDescriptor = descriptor;
	}

	// generated by eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fDescriptor == null) ? 0 : fDescriptor.hashCode());
		return result;
	}

	// generated by eclipse
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceProvider other = (ServiceProvider) obj;
		if (fDescriptor == null) {
			if (other.fDescriptor != null)
				return false;
		} else if (!fDescriptor.equals(other.fDescriptor))
			return false;
		return true;
	}

}
