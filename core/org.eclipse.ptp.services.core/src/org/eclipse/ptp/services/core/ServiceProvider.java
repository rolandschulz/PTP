/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.util.HashMap;

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
public abstract class ServiceProvider implements IServiceProvider, IServiceProviderDescriptor {
	
	private IServiceProviderDescriptor fDescriptor = null;
	private final HashMap<String, String> fAttributes = new HashMap<String, String>();
	
	public ServiceProvider() {
	}
	
	public ServiceProvider(IServiceProviderDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		return isConfigured() ? Messages.ServiceProvider_0 : Messages.ServiceProvider_1;
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#getString(java.lang.String)
	 */
	public String getString(String key) {
		return fAttributes.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#putString(java.lang.String, java.lang.String)
	 */
	public void putString(String key, String value) {
		fAttributes.put(key, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		fAttributes.clear();
		for (String key : memento.getAttributeKeys()) {
			fAttributes.put(key, memento.getString(key));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#saveState(org.eclipse.ui.IMemento)
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

}
