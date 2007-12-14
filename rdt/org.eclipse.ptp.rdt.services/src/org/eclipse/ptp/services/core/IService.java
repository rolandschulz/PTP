/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

public interface IService {
	/**
	 * Get the ID of this service provider.
	 * 
	 * @return ID of this service provider
	 */
	public String getId();
	
	/**
	 * Get the name of this service provider.
	 * 
	 * @return name of this service provider
	 */
	public String getName();
	
	/**
	 * Get the set of natures that this service applies to. The
	 * default nature "all" means the services is generic an applies
	 * to all natures.
	 * 
	 * @return set of natures
	 */
	public Set<String> getNatures();
	
	/**
	 * Get the set of all providers for this service.
	 * 
	 * @return set of providers for this service
	 */
	public Set<IServiceProviderDescriptor> getProviders();
}
