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
package org.eclipse.ptp.rdt.services.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author crecoskie
 *
 */
public class ServiceConfiguration implements IServiceConfiguration {
	
	protected String fName;
	
	protected Map<IService, IServiceProvider> fServiceToProviderMap = new HashMap<IService, IServiceProvider>();
	
	public ServiceConfiguration(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceConfiguration#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceConfiguration#getServiceProvider(org.eclipse.ptp.rdt.services.core.IService)
	 */
	public IServiceProvider getServiceProvider(IService service) {
		return fServiceToProviderMap.get(service);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceConfiguration#setServiceProvider(org.eclipse.ptp.rdt.services.core.IService, org.eclipse.ptp.rdt.services.core.IServiceProvider)
	 */
	public void setServiceProvider(IService service, IServiceProvider provider) {
		// Remove old mapping if one exists
		fServiceToProviderMap.remove(service);
		fServiceToProviderMap.put(service, provider);

	}

	public Set<IService> getServices() {
		return Collections.unmodifiableSet(fServiceToProviderMap.keySet());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IServiceConfiguration) {
			return fName.equals(((IServiceConfiguration) o).getName());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return fName.hashCode();
	}
}