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

import org.eclipse.ptp.services.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * A service provider that does nothing.
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
	
	private IServiceProviderDescriptor descriptor = null;
	
	public ServiceProvider() {
	}
	
	public ServiceProvider(IServiceProviderDescriptor descriptor) {
		this.descriptor = descriptor;
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
		if (descriptor == null) {
			return null;
		}
		return descriptor.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getName()
	 */
	public String getName() {
		if (descriptor == null) {
			return null;
		}
		return descriptor.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getPriority()
	 */
	public String getPriority() {
		if (descriptor == null) {
			return null;
		}
		return descriptor.getPriority();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProviderDescriptor#getServiceId()
	 */
	public String getServiceId() {
		if (descriptor == null) {
			return null;
		}
		return descriptor.getServiceId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// does not restore anything
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// does not save anything
	}

	public void setDescriptor(IServiceProviderDescriptor descriptor) {
		this.descriptor = descriptor;
	}

}
