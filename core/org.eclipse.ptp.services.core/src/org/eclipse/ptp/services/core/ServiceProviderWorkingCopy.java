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


/**
 * An abstract base class for service provider working copy implementations.
 *
 */
public class ServiceProviderWorkingCopy extends ServiceProvider implements IServiceProviderWorkingCopy {
	private IServiceProvider fProvider;
	
	public ServiceProviderWorkingCopy(IServiceProvider provider) {
		fProvider = provider;
		setProperties(provider.getProperties());
		setDescriptor(provider.getDescriptor());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return fProvider.isConfigured();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#save()
	 */
	public void save() {
		fProvider.setProperties(getProperties());
	}
}
