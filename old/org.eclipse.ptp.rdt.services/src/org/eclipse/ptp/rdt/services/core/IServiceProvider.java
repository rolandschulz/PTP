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
package org.eclipse.ptp.rdt.services.core;

import org.eclipse.ui.IMemento;

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
	 * Test if this service provider has been configured.
	 * 
	 * @return true if provider has been configured
	 */
	public boolean isConfigured();

	/**
	 * Saves the state of this provider in the given
	 * <code>IMemento</code>.
	 * 
	 * @param memento for saving the provider's state.
	 */
	public void saveState(IMemento memento);

	/**
	 * Restores the state of this provider from the
	 * given <code>IMemento</code>.
	 * 
	 * @param memento for restoring the provider's state.
	 */
	public void restoreState(IMemento memento);
	
	/**
	 * Returns provider specific information for the current configuration.
	 * @return information on current configuration for this provider
	 */
	public String getConfigurationString();
}
