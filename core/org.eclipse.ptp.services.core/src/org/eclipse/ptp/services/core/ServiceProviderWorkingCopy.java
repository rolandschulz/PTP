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

import java.util.Map;

/**
 * A base class for service provider working copy implementations.
 * 
 */
public class ServiceProviderWorkingCopy extends ServiceProvider implements IServiceProviderWorkingCopy {
	private IServiceProvider fProvider;
	private boolean fIsDirty = false;

	public ServiceProviderWorkingCopy(IServiceProvider provider) {
		if (provider instanceof IServiceProviderWorkingCopy) {
			fProvider = ((IServiceProviderWorkingCopy) provider).getOriginal();
		} else {
			fProvider = provider;
		}
		internalSetProperties(provider.getProperties());
		setDescriptor(provider.getDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#getOriginal()
	 */
	public IServiceProvider getOriginal() {
		return fProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return fProvider.isConfigured();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return fIsDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#putString(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void putString(String key, String value) {
		fIsDirty = true;
		super.putString(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#save()
	 */
	public void save() {
		fProvider.setProperties(getProperties());
		fIsDirty = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#setProperties(java.util
	 * .Map)
	 */
	@Override
	public void setProperties(Map<String, String> properties) {
		fIsDirty = true;
		super.setProperties(properties);
	}
}
