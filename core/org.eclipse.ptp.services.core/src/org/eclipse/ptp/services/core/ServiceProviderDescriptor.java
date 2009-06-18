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


/**
 * A description (but not an instance) of a service provider.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author greg
 */
public class ServiceProviderDescriptor implements IServiceProviderDescriptor {
	private String id;
	private String name;
	private String serviceId;
	private String priority;
	
	public ServiceProviderDescriptor(String id, String name, String serviceId, String priority) {
		this.id = id;
		this.name = name;
		this.serviceId = serviceId;
		this.priority = priority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getPriority()
	 */
	public String getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getServiceId()
	 */
	public String getServiceId() {
		return serviceId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IServiceProviderDescriptor)) {
			return false;
		}
		return id.equals(((IServiceProviderDescriptor) o).getId()); 
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public String toString() {
		return "ServiceProviderDescriptor(" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
