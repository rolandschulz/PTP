/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.wizards.ServiceProviderConfigurationWizard;

/**
 * Transitional class to allow the RM configuration framework to be used with
 * the service model.
 * 
 * @author greg
 * 
 */
public class RMServiceProviderConfigurationWizard extends ServiceProviderConfigurationWizard implements IRMConfigurationWizard {

	public RMServiceProviderConfigurationWizard(IServiceProvider provider, IWizardPage page) {
		super(provider, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getBaseConfiguration()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerConfiguration getBaseConfiguration() {
		return ModelManager.getInstance().createBaseConfiguration(fProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getControlConfiguration
	 * ()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerComponentConfiguration getControlConfiguration() {
		return ModelManager.getInstance().createControlConfiguration(fProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getMonitorConfiguration
	 * ()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerComponentConfiguration getMonitorConfiguration() {
		return ModelManager.getInstance().createMonitorConfiguration(fProvider);
	}
}
